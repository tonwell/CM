package droid.crowdmap.modelos

import droid.crowdmap.basededados.PhoneDataEntity

data class PhoneData(
        var signalStrength: Double,
        var latitude: Double,
        var longitude: Double,
        var networkOperator: String,
        var date: String
)

fun PhoneData.asDatabaseModel() = PhoneDataEntity(
        id = 0,
        signalStrength = signalStrength,
        latitude = latitude,
        longitude = longitude,
        networkOperator = networkOperator,
        date = date
)