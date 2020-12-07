package droid.crowdmap

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import java.util.ArrayList

object PermissionUtils {
    fun validate(activity: Activity, requestCode: Int, vararg permissions: String) : Boolean {
        // var list = ArrayList<String>()
//        for( permission in permissions) {
//            val permissionGranted = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
//            if(!permissionGranted)
//                list.add(permission)
//        }
        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        } as ArrayList<String>

        if(permissionsNeeded.isEmpty())
            return true
        
        val newPermissions = arrayOfNulls<String>(permissionsNeeded.size)
        permissionsNeeded.toArray(newPermissions)
        ActivityCompat.requestPermissions(activity, newPermissions, 1)
        return false
    }
}