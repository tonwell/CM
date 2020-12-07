package droid.crowdmap.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import droid.crowdmap.basededados.DB;
import droid.crowdmap.basededados.Dados;
import droid.crowdmap.fabricas.DrawAPI;
import droid.crowdmap.modelos.Operadora;

public class ColetaDadosService extends IntentService
        implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    DB db;
    double latitude, longitude;
    int sinal;
    LocationManager locationManager;
    private TelephonyManager mTelephonyManager;
    private PhoneStateListener mPhoneStateListener;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    Operadora operadora;

    public ColetaDadosService() {
        super("Servico de salvar dados");
    }

    @Override
    public void onCreate() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
        }

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();

        mTelephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);

        if(mTelephonyManager != null && mPhoneStateListener != null) mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                sinal = signalStrength.getGsmSignalStrength();
            }
        };
        operadora = new Operadora(this);
        db = DB.getInstance(this);
        Log.i("CMColetaDadosService", "Sinal: " + sinal);
        Log.i("CMColetaDadosService", "Operadora: " + operadora.getNome());
        super.onCreate();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           stopSelf();
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("CMColetaDadosService", "Service iniciado");
        turnGPSOn();
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        Dados dados = recuperarDados();

        Log.i("CMColetaDadosService", "on start sinal " + sinal);
        if (dados != null) {
            dados.setSinal(sinal);
            Dados d = db.getOne(dados.getLatitude(), dados.getLongitude(), dados.getOperadora());
            d.setSinal(sinal);
            db.insert(d);
            for (int i = 1; i <= 15; i++)
                db.insert(d, "zoom" + i);
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onDestroy() {
        Log.d("CMColetaDadosService", "Encerrando o Service");
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient = null;
        turnGPSOff();
        super.onDestroy();
    }

    private Dados recuperarDados() {
        Dados dados = null;
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            Log.d("CMColetaDadosService", "ATUAL lat: " + latitude + ", lng: " + longitude);
            dados = new Dados();
            dados.setLatitude(DrawAPI.getIdCoord(latitude, 0.002));
            dados.setLongitude(DrawAPI.getIdCoord(longitude, 0.002));
            dados.setOperadora(operadora.getNome());
        }

        return dados;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    public void turnGPSOn() {
        try {
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_MODE);

            if (!provider.contains("gps")) { // if gps is disabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                sendBroadcast(poke);
            }
        } catch (Exception e) {
        }
    }

    public void turnGPSOff() {
        try {
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (provider.contains("gps")) { // if gps is enabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                sendBroadcast(poke);
            }
        } catch (Exception e) {
        }
    }

}