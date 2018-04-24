package droid.crowdmap;

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import org.jetbrains.anko.*

// import java.util.ArrayList
// import java.util.List

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
// import android.os.AsyncTask
import android.os.Bundle
// import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

import droid.crowdmap.basededados.DB
import droid.crowdmap.basededados.Dados
import droid.crowdmap.fabricas.DrawAPI
import droid.crowdmap.modelos.Operadora
import droid.crowdmap.services.AlarmeColeta
import droid.crowdmap.PermissionUtils

class MainActivity : AppCompatActivity() {
    val gmap: GoogleMap
    val myLocation: Location
    val l: Location
    val operadora: Operadora
    val uff = new LatLng(-22.902907, -43.116714)
    val o = new LatLng(0, 0);
    val home = new LatLng(-22.619415, -43.725953)
    val scales = [
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
    ]
    val zooms = [
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
    ]
    var quads = mutableListOf<LatLng>()
    var dados = mutableListOf<Dados>()
    var horizontals = mutableListOf<PolylineOptions>()
    var verticals = mutableListOf<PolylineOptions>()
    val IMEI

    private fun setupPermissions() {
        val readPhoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        val accessFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        if (readPhoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to read phone state denied")
            toast("Permission to read phone state denied")
        }

        if (accessFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to access location denied")
            toast("Permission to access location denied")
        }
    }

    override fun onCreate(icicle: Bundle?){
        super.onCreate(icicle)
        setContentView(R.layout.activity_main)
        toast("Aqui ta funfando com kotlin")
        val sp = getSharedPreferences("coleta", MODE_PRIVATE)

        setupPermissions()

        val alarme = AlarmeColeta(this)
        val alarme.setMinutos(sp.getInt("minutos", 10))
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        IMEI = tm.getDeviceId()
        map = getSupportFragmentManager().findFragmentById(R.id.map) as SupportMapFragment
        if (map == null) toast("Mapa nulo")

        //horizontals = new ArrayList<PolylineOptions>();
        //verticals = new ArrayList<PolylineOptions>();

        operadora = Operadora(this)

        map.getMapAsync(this)
    }

    override fun onMapReady(mapa: GoogleMap){
        gmap = mapa
        gmap.setMyLocationEnabled(true)
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)
        l = locationManager.getLastKnownLocation(provider)
        myLocation = if(l!=null) LatLng(l.getLatitude(), l.getLongitude()) else uff
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zooms[0]))
        gmap.setOnCameraMoveListener {
            gmap.clear()
            override fun onCameraMove() {
                var cameraPositionZoom = gmap.getCameraPosition().zoom
                var i:Int
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

                for(x in horizontals) {
                    for (y in verticals) {
                        var d = Dados()
                        //Quadrantes na tela
                        d.setLatitude(DrawAPI.getIdCoord(x.getPoints().get(0).latitude, scales[i]))
                        d.setLongitude(DrawAPI.getIdCoord(y.getPoints().get(0).longitude, scales[i]))
                        d.setOperadora(operadora.getNome())
                        linesTask(this, d, scales[i])
                        // new GetQuadTask(MainActivityB.this,scales[i]).execute(d);
                    }
                }
                }
            }
        }
    }

    

    fun linesTask(context: Context, d: Dados, scale: Float) {
        doAsync {

            uiThread {

            }
        }
    }

    fun toast(message: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, length).show()
    }
}