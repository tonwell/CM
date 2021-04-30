package droid.crowdmap.basededados

import androidx.lifecycle.LiveData
import androidx.room.*
import droid.crowdmap.exponentialMovingAverage
import droid.crowdmap.getCurrentDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect



@Dao
abstract class PhoneDataDao {
    @Query(GET_ONE_QUERY)
    abstract fun getOne(latitude: Double, longitude: Double, networkOperator: String): Flow<PhoneDataEntity?>

    @Query(GET_VISIBLE_QUERY)
    abstract fun getVisiblePhoneDatas(firstLatitude: Double, firstLongitude: Double, lastLatitude: Double, lastLongitude: Double, networkOperator: String): LiveData<List<PhoneDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(phoneDataEntity: PhoneDataEntity)

    @Transaction
    open suspend fun updatePhoneData(phoneDataEntity: PhoneDataEntity) {
        val latestPhoneDataFlow = getOne(phoneDataEntity.latitude, phoneDataEntity.longitude, phoneDataEntity.networkOperator)
       latestPhoneDataFlow.collect { latestPhoneData ->
           if(latestPhoneData != null){
               val currentSignalStrength = exponentialMovingAverage(latestPhoneData.signalStrength, phoneDataEntity.signalStrength)
               val currentPhoneData = phoneDataEntity.copy(signalStrength = currentSignalStrength, date = getCurrentDate())
               insert(currentPhoneData)
           }
        }
    }

    @Delete
    abstract suspend fun delete(phoneDataEntity: PhoneDataEntity)
}

const val GET_ONE_QUERY = """
    SELECT *
    FROM phone_data
    WHERE latitude = :latitude AND longitude = :longitude AND network_operator = :networkOperator
    LIMIT 1
"""

const val GET_VISIBLE_QUERY = """
        SELECT * 
        FROM phone_data 
        WHERE
        latitude BETWEEN :firstLatitude AND :lastLatitude AND
        longitude BETWEEN :firstLongitude AND :lastLongitude AND
        network_operator = :networkOperator
"""