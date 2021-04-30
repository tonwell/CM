package droid.crowdmap

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import droid.crowdmap.fabricas.DrawAPI
import droid.crowdmap.modelos.Operadora
import droid.crowdmap.modelos.PhoneData
import droid.crowdmap.viewmodel.LocationError
import droid.crowdmap.viewmodel.PhoneDataViewModel
import droid.crowdmap.viewmodel.PhoneDataViewModelFactory
import droid.crowdmap.workmanager.PhoneDataWorker
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : FragmentActivity(), EasyPermissions.PermissionCallbacks {
    private val fragment: CrowdMapFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.crowd_map) as CrowdMapFragment
    }
    val phoneDataViewModel: PhoneDataViewModel by viewModels {
        PhoneDataViewModelFactory(this)
    }

    private var isGpsDialogOpened: Boolean = false

//    private var gmap: GoogleMap? = null
//    private var myLocation: LatLng? = null
    private var operadora: Operadora? = null
    private var mDrawerLayout: DrawerLayout? = null
    var i = 0
    private val scales = doubleArrayOf(0.002, 0.004, 0.008, 0.016, 0.032, 0.064, 0.128, 0.256, 0.512, 1.024, 2.048, 4.096,
            8.192, 16.384, 32.768, 65.536)
    private val zooms = floatArrayOf(15.879104f, 14.905452f, 13.954487f, 12.933992f, 11.941226f, 10.953466f, 9.950232f,
            8.912568f, 7.995446f, 6.920520f, 5.936304f, 4.992997f, 3.915645f, 2.968640f, 2.000000f, 2.000000f)
    private var quads: List<LatLng>? = null
    private var horizontals: ArrayList<PolylineOptions>? = null
    private var verticals: ArrayList<PolylineOptions>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()
        isGpsDialogOpened = savedInstanceState?.getBoolean(EXTRA_GPS_DIALOG) ?: false
        fragment.getMapAsync(fragment)
        val autocompleteFragment = fragmentManager
                .findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment
        autocompleteFragment.setOnPlaceSelectedListener(fragment)

        mDrawerLayout = findViewById(R.id.settings_menu)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
             mDrawerLayout?.closeDrawers();
            val it = Intent(this@MainActivity, ConfiguracaoActivity::class.java)
            startActivity(it)
            true
        }
        /*
        quads = ArrayList()

        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED) {

            horizontals = ArrayList()
            verticals = ArrayList()
            operadora = Operadora(this)
            fragment.getMapAsync(fragment)
        }


        PhoneDataWorker.setupSelf(
                applicationContext,
                getSharedPreferences("coleta", MODE_PRIVATE)
                        .getLong("minutos", 20L)
        )
        */
    }

    private fun requestPermissions() {
        if(PermissionUtility.hasLocationPermissions(this))
            return
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            EasyPermissions.requestPermissions(
                    this,
            "Permissão de localização é essencial para o funcionamento do app",
            REQUEST_PERMISSIONS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            )
        else
            EasyPermissions.requestPermissions(
                    this,
                    "Permissão de localização é essencial para o funcionamento do app",
                    REQUEST_PERMISSIONS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE
            )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms))
            AppSettingsDialog.Builder(this).build().show()
        else
            requestPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(EXTRA_GPS_DIALOG, isGpsDialogOpened)
    }

    override fun onStart() {
        super.onStart()
        fragment.getMapAsync {
            initUi()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_GPS) {
            isGpsDialogOpened = false
            if (resultCode == Activity.RESULT_OK) {
                loadLastLocation()
            } else {
                Toast.makeText(this, ERROR_GPS_DISABLED, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_PERMISSIONS && permissions.isNotEmpty()) {
//            if (permissions.find { it == Manifest.permission.ACCESS_FINE_LOCATION } != null
//                    && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
//                loadLastLocation()
//            } else {
//                showError(ERROR_PERMISSION_RESULT)
//                finish()
//            }
//        }
//    }

    private fun initUi() {
        loadLastLocation()

        phoneDataViewModel.currentLocationError.observe(this) { error ->
            handleLocationError(error)
        }
    }

    private fun loadLastLocation() {
        if (!hasPermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS
            )
            return
        }
        phoneDataViewModel.requestLocation()
    }

    private fun handleLocationError(error: LocationError?) {
        if (error != null) {
            when (error) {
                is LocationError.ErrorLocationUnavailable -> showError(ERROR_CURRENT_LOCATION)
            }
        }
    }


    private fun hasPermission(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == granted
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun showPlayServicesErrorMessage(errorCode: Int) {
        GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode, REQUEST_ERROR_PLAY_SERVICES).show()
    }

    /*
    override fun onMapReady(mapa: GoogleMap) {
        gmap = mapa
        gmap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        getCurrentLocation()

        gmap?.setOnCameraIdleListener {
            gmap?.clear()
            gmap?.cameraPosition?.zoom?.let { cameraPositionZoom ->
                i = setupZoomScaleIndex(cameraPositionZoom)
            }
            horizontals = DrawAPI.drawLinesX(gmap, scales[i])
            verticals = DrawAPI.drawLinesY(gmap, scales[i])
            handleDrawLines(horizontals)
            handleDrawLines(verticals)

            val latLngs: MutableList<LatLng> = mutableListOf()
            horizontals?.forEach { x ->
                verticals?.forEach { y ->
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

    private fun setupZoomScaleIndex(cameraPositionZoom: Float): Int {
        return when {
            cameraPositionZoom > zooms[0] -> {
                0
            }
            cameraPositionZoom > zooms[1] -> {
                1
            }
            cameraPositionZoom > zooms[2] -> {
                2
            }
            cameraPositionZoom > zooms[3] -> {
                3
            }
            cameraPositionZoom > zooms[4] -> {
                4
            }
            cameraPositionZoom > zooms[5] -> {
                5
            }
            cameraPositionZoom > zooms[6] -> {
                6
            }
            cameraPositionZoom > zooms[7] -> {
                7
            }
            cameraPositionZoom > zooms[8] -> {
                8
            }
            cameraPositionZoom > zooms[9] -> {
                9
            }
            cameraPositionZoom > zooms[10] -> {
                10
            }
            cameraPositionZoom > zooms[11] -> {
                11
            }
            cameraPositionZoom > zooms[12] -> {
                12
            }
            cameraPositionZoom > zooms[13] -> {
                13
            }
            cameraPositionZoom > zooms[14] -> {
                14
            }
            else -> {
                15
            }
        }
    }

    private fun handleDrawLines(po: ArrayList<PolylineOptions>?) {
        for (ln in po!!) {
            if (ln.isVisible) {
                gmap!!.addPolyline(ln)
            }
        }
    }

    private fun fillQuads(d: PhoneData, scale: Double) {
        val quad = LatLng(DrawAPI.getIdCoord(d.latitude, scale),
                DrawAPI.getIdCoord(d.longitude, scale))
        gmap!!.addPolygon(DrawAPI.fillQuad(quad, scale, d.signalStrength))
        Log.i("CMMainActivity", "QUAD ::lat: " + quad.latitude + ", lng: " + quad.longitude)
        Log.i("CMMainActivity", "DadosQuad :: lat: " + d.latitude + ", lng: " + d.longitude)
    }
    */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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

    companion object {
        private const val REQUEST_ERROR_PLAY_SERVICES = 1
        private const val REQUEST_PERMISSIONS = 2
        private const val REQUEST_CHECK_GPS = 3
        private const val ERROR_CURRENT_LOCATION = "Current Location Error"
        private const val ERROR_PERMISSION_RESULT = "Permission Result Error"
        private const val ERROR_GPS_DISABLED = "GPS Disabled Error"
        private const val ERROR_GPS_SETTINGS = "GPS Settings Unavailable Error"
        private const val EXTRA_GPS_DIALOG = "gpsDialogIsOpen"
    }
}