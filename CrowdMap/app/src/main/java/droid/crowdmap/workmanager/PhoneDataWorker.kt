package droid.crowdmap.workmanager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.*
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.work.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import droid.crowdmap.getCurrentDate
import droid.crowdmap.modelos.PhoneData
import droid.crowdmap.repository.PhoneDataRepository
import java.util.concurrent.TimeUnit

class PhoneDataWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val phoneDataRepository = PhoneDataRepository(context)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var myLocation: LatLng? = null

    override suspend fun doWork(): Result {
        return try {
//            setupLocationClient()
//            getCurrentLocation()
//            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//            var signalStrength: Int = -1
//            val phoneStateListener = object : PhoneStateListener() {
//                override fun onSignalStrengthsChanged(ss: SignalStrength) {
//                    super.onSignalStrengthsChanged(ss)
//                    signalStrength = ss.gsmSignalStrength
//                }
//            }
//            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
//            val phoneData = PhoneData(signalStrength.toDouble(), myLocation?.latitude
//                    ?: 0.0, myLocation?.longitude ?: 0.0, "claro", getCurrentDate())
//            phoneDataRepository.updatePhoneData(phoneData)
            Result.success()
        } catch (throwable: Throwable) {
            Log.e(throwable.message, throwable.toString())
            Result.failure()
        }
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnCompleteListener {
                if (it.result != null) {
                    myLocation = LatLng(it.result.latitude, it.result.longitude)
                } else {
                    Toast.makeText(
                            context,
                            "No location found",
                            Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    companion object {
        fun setupSelf(context: Context, interval: Long) {
            val phoneDataWorkRequest = PeriodicWorkRequestBuilder<PhoneDataWorker>(interval, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("PhoneDataWorker", ExistingPeriodicWorkPolicy.REPLACE, phoneDataWorkRequest)
        }
    }
}