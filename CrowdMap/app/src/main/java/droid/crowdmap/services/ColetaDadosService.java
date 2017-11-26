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
import android.support.v4.app.ActivityCompat;
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


public class ColetaDadosService extends IntentService implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
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
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        mTelephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
        Log.i("Script", "Sinal: " + sinal);
        Log.i("Script", "Operadora: " + operadora.getNome());
        super.onCreate();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onConnectionFailed(ConnectionResult result){
        
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("SalvarService", "Service iniciado");
        turnGPSOn();
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		// locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Dados dados = recuperarDados();

        Log.i("script", "on start sinal " + sinal);
        if( dados != null ) {
            dados.setSinal(sinal);
            /*
            //
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setTicker(dados.getLatitude() + "|" + dados.getLongitude() + ">" + dados.getSinal());
            builder.setContentTitle(dados.getLatitude() + "|" + dados.getLongitude() + ">" + dados.getSinal());
            builder.setContentText("");
            builder.setSmallIcon(R.drawable.ic_launcher);
            Notification n = builder.build();
            n.flags = Notification.FLAG_AUTO_CANCEL;
            nm.notify(R.drawable.ic_launcher, n);

            try {
                Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone toque = RingtoneManager.getRingtone(this, som);
                toque.play();
            } catch (Exception e) {
            }
            //
            */
            Dados d = db.getOne(dados.getLatitude(), dados.getLongitude(), dados.getOperadora());
            d.setSinal(sinal);
            db.insert(d);
            for(int i=1; i<=15;i++) db.insert(d, "zoom"+i);
        }
		return START_REDELIVER_INTENT;
	}

	@Override
	protected void onHandleIntent(Intent intent) {

	}

	@Override
	public void onDestroy() {
		Log.d("Service", "Encerrando o Service");
		mTelephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_NONE);
		//locationManager.removeUpdates(this);
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient = null;
        turnGPSOff();
        super.onDestroy();
	}

	private Dados recuperarDados() {
        Dados dados = null;
        /* if (mLastLocation == null){
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 0, 0, this);
            } else {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
            mLastLocation = locationManager.getLastKnownLocation();
        } */
        if(mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            Log.d("Script", "ATUAL lat: " + latitude + ", lng: " + longitude);
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

    /** Method to turn on GPS **/
    public void turnGPSOn(){
        try {
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_MODE);

            if(!provider.contains("gps")){ //if gps is disabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                sendBroadcast(poke);
            }
        }
        catch (Exception e) {}
    }
    // Method to turn off the GPS
    public void turnGPSOff(){
        try {
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (provider.contains("gps")) { //if gps is enabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                sendBroadcast(poke);
            }
        } catch (Exception e){}
    }

}