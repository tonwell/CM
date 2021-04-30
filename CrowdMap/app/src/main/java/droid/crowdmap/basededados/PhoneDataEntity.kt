package droid.crowdmap.basededados

import android.provider.ContactsContract
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import droid.crowdmap.basededados.PhoneDataEntity.Companion.TABLE_NAME
import droid.crowdmap.modelos.PhoneData

@Entity(tableName = TABLE_NAME)
data class PhoneDataEntity(
        @PrimaryKey(autoGenerate = true) var id: Long,
        @ColumnInfo(name = COLUMN_SIGNAL_STRENGTH) var signalStrength: Double,
        var latitude: Double,
        var longitude: Double,
        @ColumnInfo(name = COLUMN_NETWORK_OPERATOR) var networkOperator: String,
        var date: String
) {
    companion object {
        const val TABLE_NAME="phone_data"
        const val COLUMN_SIGNAL_STRENGTH="signal_strength"
        const val COLUMN_NETWORK_OPERATOR="network_operator"
    }
}

fun PhoneDataEntity.asDomainModel() = PhoneData(
        signalStrength = signalStrength,
        latitude = latitude,
        longitude = longitude,
        networkOperator = networkOperator,
        date = date
)

fun List<PhoneDataEntity>.asDomainModel() = map { it.asDomainModel() }