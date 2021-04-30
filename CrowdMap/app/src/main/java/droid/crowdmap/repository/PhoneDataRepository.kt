package droid.crowdmap.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.android.gms.maps.model.LatLng
import droid.crowdmap.basededados.CrowdMapDatabase
import droid.crowdmap.basededados.asDomainModel
import droid.crowdmap.modelos.PhoneData
import droid.crowdmap.modelos.asDatabaseModel

class PhoneDataRepository(val context: Context) {
    private val dao = CrowdMapDatabase.getDatabase(context).phoneDataDao()

    fun getVisiblePhoneDatas(firstCoord: LatLng, lastCoord: LatLng, networkOperator: String): LiveData<List<PhoneData>> {
        return dao.getVisiblePhoneDatas(firstCoord.latitude, firstCoord.longitude,
                lastCoord.latitude, lastCoord.longitude, networkOperator).map { it.asDomainModel() }
    }

    suspend fun updatePhoneData(currentPhoneData: PhoneData) {
        dao.updatePhoneData(currentPhoneData.asDatabaseModel())
    }
}