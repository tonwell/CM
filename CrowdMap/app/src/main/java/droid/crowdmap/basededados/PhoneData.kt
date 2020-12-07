package droid.crowdmap.basededados

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import droid.crowdmap.basededados.PhoneData.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class PhoneData(
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