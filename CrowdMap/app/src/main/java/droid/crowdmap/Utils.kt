package droid.crowdmap

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import androidx.core.content.edit
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = Calendar.getInstance().time
    return sdf.format(date)
}

fun exponentialMovingAverage(oldValue: Double, newValue: Double): Double {
    val alpha = 0.3
    return oldValue * alpha + newValue * (1 - alpha)
}

object PermissionUtility {
    fun hasLocationPermissions(context: Context) =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                EasyPermissions.hasPermissions(
                        context,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )
            else
                EasyPermissions.hasPermissions(
                        context,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.FOREGROUND_SERVICE
                )
}

fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}

internal object SharedPreferenceUtil {

    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"

    fun getLocationTrackingPref(context: Context): Boolean =
            context.getSharedPreferences(
                    "CrowdMapPref", Context.MODE_PRIVATE)
                    .getBoolean(KEY_FOREGROUND_ENABLED, false)

    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
            context.getSharedPreferences(
                    "CrowdMapPref",
                    Context.MODE_PRIVATE).edit {
                putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
            }
}

