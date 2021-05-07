package droid.crowdmap

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.compat.Place
import com.google.android.libraries.places.compat.ui.PlaceSelectionListener
import droid.crowdmap.fabricas.DrawAPI
import droid.crowdmap.modelos.PhoneData
import droid.crowdmap.viewmodel.MapState
import droid.crowdmap.viewmodel.PhoneDataViewModel

class CrowdMapFragment : SupportMapFragment(), OnMapReadyCallback, PlaceSelectionListener {
    private val phoneDataViewModel: PhoneDataViewModel by lazy {
        (requireActivity() as MainActivity).phoneDataViewModel
    }
    private var _googleMap: GoogleMap? = null
    private lateinit var horizontals: ArrayList<PolylineOptions>
    private lateinit var verticals: ArrayList<PolylineOptions>
    private var isOnLocation = false
    private lateinit var currentLocation: LatLng


    override fun onPlaceSelected(place: Place?) {
        val placeLatLng = place!!.latLng
        _googleMap!!.addMarker(MarkerOptions().position(placeLatLng))
        _googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, ZOOM))
    }

    override fun onError(p0: Status?) {}

    override fun getMapAsync(callback: OnMapReadyCallback?) {
        super.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    private fun setupMap(googleMap: GoogleMap) {
        googleMap.run {
            mapType = MAP_TYPE_NORMAL
            isMyLocationEnabled = true
            uiSettings.isMapToolbarEnabled = true
            uiSettings.isZoomControlsEnabled = false
            uiSettings.isMyLocationButtonEnabled = true
            setMaxZoomPreference(ZOOM)
            setMinZoomPreference(ZOOM)
        }
        _googleMap = googleMap

        phoneDataViewModel.mapState.observe(viewLifecycleOwner) { mapState ->
            if (mapState != null && !isOnLocation) {
                updateMap(mapState, googleMap)
                isOnLocation = true
            }
        }
    }

    private fun updateMap(mapState: MapState, googleMap: GoogleMap) {
        googleMap.run {
            val origin = mapState.origin
            if (origin != null && !isOnLocation) {
                currentLocation = origin
                animateCamera(CameraUpdateFactory.newLatLngZoom(origin, ZOOM))
            }
        }
    }

    override fun onMapReady(gmap: GoogleMap) {
        gmap.mapType = GoogleMap.MAP_TYPE_NORMAL
        gmap.setOnCameraIdleListener {
            setupMap(gmap)
            setupGrid(gmap)
        }
    }

    private fun setupGrid(gmap: GoogleMap) {
        horizontals = DrawAPI.drawLinesX(gmap, SCALE)
        verticals = DrawAPI.drawLinesY(gmap, SCALE)
        handleDrawLines(gmap, horizontals)
        handleDrawLines(gmap, verticals)

        val latLngs: MutableList<LatLng> = mutableListOf()
        horizontals.forEach { x ->
            verticals.forEach { y ->
                latLngs.add(LatLng(x.points[0].latitude, y.points[0].longitude))
            }
        }

        val firstCoord = latLngs.first()
        val lastCoord = latLngs.last()

        phoneDataViewModel.getVisiblePhoneData(firstCoord, lastCoord, "claro").observe(this) { phoneDatas ->
            phoneDatas.map { phoneData ->
                fillQuad(gmap, phoneData, SCALE)
            }
        }
    }

    private fun handleDrawLines(gmap: GoogleMap, po: ArrayList<PolylineOptions>) {
        po.forEach { line ->
            if (line.isVisible)
                _googleMap?.addPolyline(line)
        }
    }

    private fun fillQuad(gmap: GoogleMap, phoneData: PhoneData, scale: Double) {
        val quad = LatLng(DrawAPI.getIdCoord(phoneData.latitude, scale),
                DrawAPI.getIdCoord(phoneData.longitude, scale))
        gmap.addPolygon(DrawAPI.fillQuad(quad, scale, phoneData.signalStrength))
        Log.i(LOG_TAG, "QUAD ::lat: ${quad.latitude}, lng: ${quad.longitude}")
        Log.i(LOG_TAG, "DadosQuad :: lat: ${phoneData.latitude}, lng: ${phoneData.longitude}")
    }

    companion object {
        private const val SCALE = 0.002
        private const val ZOOM = 16.35f
        private const val LOG_TAG = "CMFragment"
        private const val WAITING_LOCATION = "Esperando por localização"
        private const val networkOperator = "claro"
    }
}