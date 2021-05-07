package droid.crowdmap.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import droid.crowdmap.basededados.PhoneDataEntity
import droid.crowdmap.modelos.PhoneData
import droid.crowdmap.repository.PhoneDataRepository
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PhoneDataViewModel(val context: Context) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + viewModelJob
    private val viewModelJob = Job()

    private val phoneDataRepo by lazy {
        PhoneDataRepository(context)
    }

    private val locationLiveData by lazy {
        LocationLiveData(context)
    }

    private val locationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context.applicationContext)
    }

    private val _currentLocationError = MutableLiveData<LocationError>()
    val currentLocationError: LiveData<LocationError>
        get() = _currentLocationError

    private val _mapState = MutableLiveData<MapState>().apply { value = MapState() }
    val mapState: LiveData<MapState>
        get() = _mapState

    private val _phoneData: MutableLiveData<List<PhoneDataEntity>> = MutableLiveData()
    val phoneDataLiveData: LiveData<List<PhoneDataEntity>> get() = _phoneData

    fun getVisiblePhoneData(firstCoord: LatLng, lastCoord: LatLng, networkOperator: String): LiveData<List<PhoneData>> {
        return phoneDataRepo.getVisiblePhoneDatas(firstCoord, lastCoord, networkOperator)
    }

    @SuppressLint("MissingPermission")
    private suspend fun loadLastLocation(): Boolean = suspendCoroutine { continuation ->
//        locationLiveData.map {  location ->
//            if(location != null) {
//                _mapState.value = _mapState.value?.copy(origin = LatLng(location.latitude, location.longitude))
//                continuation.resume(true)
//            } else {
//                _currentLocationError.value = LocationError.ErrorLocationUnavailable
//                continuation.resume(false)
//            }
//        }
        locationClient.lastLocation.addOnCompleteListener { task ->
            val location = task.result
            if (location != null) {
                _mapState.value = _mapState.value?.copy(origin = LatLng(location.latitude, location.longitude))
                continuation.resume(true)
            } else {
                _currentLocationError.value = LocationError.ErrorLocationUnavailable
                continuation.resume(false)
            }
        }
    }

    fun requestLocation() {
        launch {
            _currentLocationError.value = try {
                val success = withContext(Dispatchers.Default) { loadLastLocation() }
                if(success)
                    null
                else
                    LocationError.ErrorLocationUnavailable
            } catch (e: Exception) {
                LocationError.ErrorLocationUnavailable
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}

class PhoneDataViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhoneDataViewModel::class.java)) {
            return PhoneDataViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class MapState(
        val origin: LatLng? = null
)


sealed class LocationError {
    object ErrorLocationUnavailable : LocationError()
}