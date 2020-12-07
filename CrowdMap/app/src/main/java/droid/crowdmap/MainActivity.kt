package droid.crowdmap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.compat.Place
import com.google.android.libraries.places.compat.ui.PlaceAutocompleteFragment
import com.google.android.libraries.places.compat.ui.PlaceSelectionListener
import com.google.android.material.navigation.NavigationView
import droid.crowdmap.basededados.CrowdMapDatabase
import droid.crowdmap.basededados.Dados
import droid.crowdmap.basededados.PhoneData
import droid.crowdmap.fabricas.DrawAPI
import droid.crowdmap.modelos.Operadora
import droid.crowdmap.services.AlarmeColeta
import droid.crowdmap.viewmodel.PhoneDataViewModel
import droid.crowdmap.viewmodel.PhoneDataViewModelFactory

class MainActivity : FragmentActivity(), OnMapReadyCallback {
    private var alarme: AlarmeColeta? = null
    private var map: SupportMapFragment? = null
    private var l: Location? = null
    private var gmap: GoogleMap? = null
    private var myLocation: LatLng? = null
    private var operadora: Operadora? = null
    private var mDrawerLayout: DrawerLayout? = null
    var i = 0
    private val scales = doubleArrayOf(0.002, 0.004, 0.008, 0.016, 0.032, 0.064, 0.128, 0.256, 0.512, 1.024, 2.048, 4.096,
            8.192, 16.384, 32.768, 65.536)
    private val zooms = floatArrayOf(15.879104f, 14.905452f, 13.954487f, 12.933992f, 11.941226f, 10.953466f, 9.950232f,
            8.912568f, 7.995446f, 6.920520f, 5.936304f, 4.992997f, 3.915645f, 2.968640f, 2.000000f, 2.000000f)
    private var quads: List<LatLng>? = null
    private val dados: List<Dados>? = null
    private var horizontals: ArrayList<PolylineOptions>? = null
    private var verticals: ArrayList<PolylineOptions>? = null
    var IMEI: String? = null
    val phoneDataViewModel: PhoneDataViewModel by viewModels {
        PhoneDataViewModelFactory(this)
    }

    private fun hasLocationPermissions() = PermissionUtils.validate(
            this,
            1,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(!hasLocationPermissions()) finish()

        /*
         * String provider = Settings.Secure.getString(getContentResolver(),
         * Settings.Secure.LOCATION_MODE); if(provider == null){ startActivity( new
         * Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS) ); }
         */
        mDrawerLayout = findViewById(R.id.settings_menu)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            // mDrawerLayout.closeDrawers();
            val it = Intent(this@MainActivity, ConfiguracaoActivity::class.java)
            startActivity(it)
            true
        }
        val sp = getSharedPreferences("coleta", MODE_PRIVATE)
        alarme = AlarmeColeta(this)
        alarme!!.setMinutos(sp.getInt("minutos", 10))
        quads = ArrayList()
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
//        if (ActivityCompat.checkSelfPermission(this,
//                        Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            val REQUEST_PHONE = 2
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE),
//                    REQUEST_PHONE)
//        } else {
//            IMEI = tm.deviceId
//            map = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
//            if (map == null) Toast.makeText(this, "Mapa nulo", Toast.LENGTH_LONG).show()
//            horizontals = ArrayList()
//            verticals = ArrayList()
//            operadora = Operadora(this)
//            map!!.getMapAsync(this)
//        }


        if(PermissionUtils.validate(this, 1, Manifest.permission.READ_PHONE_STATE)) {
            IMEI = tm.deviceId
            map = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            if (map == null) Toast.makeText(this, "Mapa nulo", Toast.LENGTH_LONG).show()
            horizontals = ArrayList()
            verticals = ArrayList()
            operadora = Operadora(this)
            map!!.getMapAsync(this)
        }
        val autocompleteFragment = fragmentManager
                .findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val placeLatLng = place.latLng
                gmap!!.addMarker(MarkerOptions().position(placeLatLng))
                gmap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, zooms[i]))
            }

            override fun onError(status: Status) {}
        })
    }

    override fun onMapReady(mapa: GoogleMap) {
        gmap = mapa
        gmap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val REQUEST_LOCATION = 2
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION)
        } else {
            gmap!!.isMyLocationEnabled = true
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, true)
            l = locationManager.getLastKnownLocation(provider)
            myLocation = if (l != null) LatLng(l!!.latitude, l!!.longitude) else LatLng(0.0, 0.0)
            // myLocation = home;
            // CameraPosition cameraPosition = new
            // CameraPosition.Builder().target(myLocation).zoom(zooms[0]).build();
            gmap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16.35f))
            gmap!!.setOnCameraIdleListener {
                gmap!!.clear()
                val cameraPositionZoom = gmap!!.cameraPosition.zoom
                // int i;
                i = if (cameraPositionZoom > zooms[0]) {
                    0
                } else if (cameraPositionZoom > zooms[1]) {
                    1
                } else if (cameraPositionZoom > zooms[2]) {
                    2
                } else if (cameraPositionZoom > zooms[3]) {
                    3
                } else if (cameraPositionZoom > zooms[4]) {
                    4
                } else if (cameraPositionZoom > zooms[5]) {
                    5
                } else if (cameraPositionZoom > zooms[6]) {
                    6
                } else if (cameraPositionZoom > zooms[7]) {
                    7
                } else if (cameraPositionZoom > zooms[8]) {
                    8
                } else if (cameraPositionZoom > zooms[9]) {
                    9
                } else if (cameraPositionZoom > zooms[10]) {
                    10
                } else if (cameraPositionZoom > zooms[11]) {
                    11
                } else if (cameraPositionZoom > zooms[12]) {
                    12
                } else if (cameraPositionZoom > zooms[13]) {
                    13
                } else if (cameraPositionZoom > zooms[14]) {
                    14
                } else {
                    15
                }
                horizontals = DrawAPI.drawLinesX(gmap, scales[i])
                verticals = DrawAPI.drawLinesY(gmap, scales[i])
                handleDrawLines(horizontals)
                handleDrawLines(verticals)

                val latLngs: MutableList<LatLng> = mutableListOf()
                for (x in horizontals!!){
                    for (y in verticals!!) {
                        latLngs.add(LatLng(x.points[0].latitude, y.points[0].longitude))
                    }
                }
                val firstCoord = latLngs.first()
                val lastCoord = latLngs.last()

                phoneDataViewModel.getVisiblePhoneData(firstCoord, lastCoord, "claro").observe(this) { phoneDatas ->
                    phoneDatas.map {
                        fillQuads(it, 0.002)
                    }
                }
            }
        }
    }

    fun handleDrawLines(po: ArrayList<PolylineOptions>?) {
        for (ln in po!!) {
            if (ln.isVisible) {
                gmap!!.addPolyline(ln)
            }
        }
    }

    fun fillQuads(d: PhoneData, scale: Double) {
        val quad = LatLng(DrawAPI.getIdCoord(d.latitude, scale),
                DrawAPI.getIdCoord(d.longitude, scale))
        gmap!!.addPolygon(DrawAPI.fillQuad(quad, scale, d.signalStrength))
        Log.i("CMMainActivity", "QUAD ::lat: " + quad.latitude + ", lng: " + quad.longitude)
        Log.i("CMMainActivity", "DadosQuad :: lat: " + d.latitude + ", lng: " + d.longitude)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.config) {
            val configuracao = Intent(this, ConfiguracaoActivity::class.java)
            startActivity(configuracao)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}