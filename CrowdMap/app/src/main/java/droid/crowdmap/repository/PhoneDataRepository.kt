package droid.crowdmap.repository

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import droid.crowdmap.basededados.CrowdMapDatabase

class PhoneDataRepository(context: Context){
    val dao = CrowdMapDatabase.getDatabase(context).phoneDataDao()

    fun getPhoneDatas() = dao.getAll()

    fun getVisiblePhoneDatas(
            firstCoord: LatLng,
            lastCoord: LatLng,
            networkOperator: String) =
            dao.getVisiblePhoneDatas(firstCoord.latitude, firstCoord.longitude,
                    lastCoord.latitude, lastCoord.longitude, networkOperator)

    fun getPhoneData(latitude: Double, longitude: Double, networkOperator: String) =
            dao.getOne(latitude, longitude, networkOperator)
}