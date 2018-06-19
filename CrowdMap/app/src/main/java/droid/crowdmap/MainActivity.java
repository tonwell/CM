package droid.crowdmap;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import droid.crowdmap.basededados.DB;
import droid.crowdmap.basededados.Dados;
import droid.crowdmap.fabricas.DrawAPI;
import droid.crowdmap.modelos.Operadora;
import droid.crowdmap.services.AlarmeColeta;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private AlarmeColeta alarme;
    private SupportMapFragment map;
    private Location l;
    private GoogleMap gmap;
    private LatLng myLocation;
    private Operadora operadora;
    private DrawerLayout mDrawerLayout;
    private static final LatLng uff = new LatLng(-22.902907, -43.116714);
    private static final LatLng o = new LatLng(0, 0);
    private static final LatLng home = new LatLng(-22.619415, -43.725953);
    private double[] scales = {
            0.002,
            0.004,
            0.008,
            0.016,
            0.032,
            0.064,
            0.128,
            0.256,
            0.512,
            1.024,
            2.048,
            4.096,
            8.192,
            16.384,
            32.768,
            65.536
    };
    private float[] zooms = {
            15.879104f,
            14.905452f,
            13.954487f,
            12.933992f,
            11.941226f,
            10.953466f,
            9.950232f,
            8.912568f,
            7.995446f,
            6.920520f,
            5.936304f,
            4.992997f,
            3.915645f,
            2.968640f,
            2.000000f,
            2.000000f
    };
    private List<LatLng> quads;
    private List<Dados> dados;

    private ArrayList<PolylineOptions> horizontals, verticals;

    String IMEI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_MODE);
        if(provider == null){
            startActivity( new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS) );
        }
        */

        mDrawerLayout = (DrawerLayout) findViewById(R.id.settings_menu);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                Intent it = new Intent(MainActivity.this, ConfiguracaoActivity.class);
                startActivity(it);
                return true;
            }
        });

        SharedPreferences sp = getSharedPreferences("coleta", MODE_PRIVATE);
        alarme = new AlarmeColeta(this);
        alarme.setMinutos(sp.getInt("minutos", 10));
        quads = new ArrayList<LatLng>();
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            final int REQUEST_PHONE=2;
            ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.READ_PHONE_STATE},
            REQUEST_PHONE);

        } else {
            IMEI = tm.getDeviceId();
            map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            if (map == null) Toast.makeText(this, "Mapa nulo", Toast.LENGTH_LONG).show();

            horizontals = new ArrayList<PolylineOptions>();
            verticals = new ArrayList<PolylineOptions>();

            operadora = new Operadora(this);

            map.getMapAsync(this);
        }

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {


            }

            @Override
            public void onError(Status status) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap mapa) {
        gmap = mapa;
        gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final int REQUEST_LOCATION = 2;

            ActivityCompat.requestPermissions(this,
                 new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                     REQUEST_LOCATION);
        } else {
            gmap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        String provider = locationManager.getBestProvider(criteria, true);
        l = locationManager.getLastKnownLocation(provider);
        myLocation = (l!=null) ? new LatLng(l.getLatitude(), l.getLongitude()) : uff;
        //myLocation = home;
        //CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(zooms[0]).build();
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zooms[0]));

        gmap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                gmap.clear();
                float cameraPositionZoom = gmap.getCameraPosition().zoom;
                int i;
                if(cameraPositionZoom > zooms[0]){
                    i = 0;
                } else if(cameraPositionZoom > zooms[1]){
                    i = 1;
                } else if(cameraPositionZoom > zooms[2]){
                    i = 2;
                } else if(cameraPositionZoom > zooms[3]){
                    i = 3;
                } else if(cameraPositionZoom > zooms[4]){
                    i = 4;
                } else if(cameraPositionZoom > zooms[5]){
                    i = 5;
                } else if(cameraPositionZoom > zooms[6]){
                    i = 6;
                } else if(cameraPositionZoom > zooms[7]){
                    i = 7;
                } else if(cameraPositionZoom > zooms[8]){
                    i = 8;
                } else if(cameraPositionZoom > zooms[9]){
                    i = 9;
                } else if(cameraPositionZoom > zooms[10]){
                    i = 10;
                } else if(cameraPositionZoom > zooms[11]){
                    i = 11;
                } else if(cameraPositionZoom > zooms[12]){
                    i = 12;
                } else if(cameraPositionZoom > zooms[13]){
                    i = 13;
                } else if(cameraPositionZoom > zooms[14]){
                    i = 14;
                } else {
                    i = 15;
                }
                horizontals = DrawAPI.drawLinesX(gmap, scales[i]);
                verticals = DrawAPI.drawLinesY(gmap, scales[i]);
                handleDrawLines(horizontals);
                handleDrawLines(verticals);

                for(PolylineOptions x : horizontals) {
                    for (PolylineOptions y : verticals) {
                        Dados d = new Dados();
                        //Quadrantes na tela
                        d.setLatitude(DrawAPI.getIdCoord(x.getPoints().get(0).latitude, scales[i]));
                        d.setLongitude(DrawAPI.getIdCoord(y.getPoints().get(0).longitude, scales[i]));
                        d.setOperadora(operadora.getNome());
                        new GetQuadTask(MainActivity.this,scales[i], horizontals, verticals).execute(d);
                    }
                }
            }
        });
    }


    }

    public void handleDrawLines(ArrayList<PolylineOptions> po) {
        for(PolylineOptions ln : po ) {
            if(ln.isVisible()) {
                gmap.addPolyline(ln);
            }
        }
    }
    public void fillQuads(Dados d, double scale){
        /*/
        LatLng quad = new LatLng(d.getLatitude(), d.getLongitude());
        gmap.addPolygon( DrawAPI.fillQuad(quad, scale, d.getSinal()));
        Log.i( "QUAD" , "lat: " + quad.latitude + "lng: " + quad.longitude);
        Log.i( "DadosQuad",  "lat: " + d.getLatitude() + "lng: " + d.getLongitude() );
        /*/
        LatLng quad = new LatLng(DrawAPI.getIdCoord(d.getLatitude(), scale), DrawAPI.getIdCoord(d.getLongitude(), scale));
        gmap.addPolygon( DrawAPI.fillQuad(quad, scale, d.getSinal()));
        Log.i( "QUAD" , "lat: " + quad.latitude + "lng: " + quad.longitude);
        Log.i( "DadosQuad",  "lat: " + d.getLatitude() + "lng: " + d.getLongitude() );
        //*/
    }


    protected void onResume(){
        super.onResume();
    }

    protected void onPause(){
        super.onPause();
        /*if(task1 != null && isFinishing())
            task1.cancel(false) ;*/
    }

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.config) {
			Intent configuracao = new Intent(this, ConfiguracaoActivity.class);
			startActivity(configuracao);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    class GetQuadTask extends AsyncTask<Dados,Void,Dados>{
        DB dao;
        MainActivity activity;
        ArrayList<PolylineOptions> horizontals, verticals;

        double scale;

        public GetQuadTask(MainActivity activity, double scale, ArrayList<PolylineOptions> horizontals, ArrayList<PolylineOptions> verticals){
            this.activity = activity;
            this.scale = scale;
            this.horizontals = horizontals;
            this.verticals = verticals;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            //handleDrawLines(horizontals);
            //handleDrawLines(verticals);
            dao = DB.getInstance(activity);

        }

        @Override
        protected void onPostExecute(Dados dados) {
            super.onPostExecute(dados);
            Log.d("dados", dados.toString());
            activity.fillQuads(dados, scale);
        }

        @Override
        protected Dados doInBackground(Dados... dados) {
            Dados dp = dados[0];
            int index = (int) (Math.log(scale/0.002)/Math.log(2));
            String table = "zoom"+index;
            Dados d = dao.getOne(dp.getLatitude(), dp.getLongitude(), dp.getOperadora(), table);

            d.setLatitude(DrawAPI.getIdCoord(d.getLatitude(),scale));
            d.setLongitude(DrawAPI.getIdCoord(d.getLongitude(), scale));

            return d;
        }
    }
}
