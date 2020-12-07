package droid.crowdmap.basededados

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhoneDataDao {
    @Query("SELECT * FROM phone_data")
    fun getAll(): LiveData<List<PhoneData>>

    @Query("""
        SELECT * 
        FROM phone_data 
        WHERE latitude = :latitude AND longitude = :longitude AND network_operator = :networkOperator
        LIMIT 1
        """)
    fun getOne(latitude: Double, longitude: Double, networkOperator: String): LiveData<PhoneData>

    @Query("""
        SELECT * 
        FROM phone_data 
        WHERE
        latitude BETWEEN :firstLatitude AND :lastLatitude AND
        longitude BETWEEN :firstLongitude AND :lastLongitude AND
        network_operator = :networkOperator
        """)
    fun getVisiblePhoneDatas(firstLatitude: Double, firstLongitude: Double, lastLatitude: Double, lastLongitude: Double, networkOperator: String): LiveData<List<PhoneData>>

    @Query("SELECT * FROM phone_data WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): LiveData<List<PhoneData>>

    @Insert
    fun insertAll(vararg users: PhoneData)

    @Delete
    fun delete(phone_data: PhoneData)
}