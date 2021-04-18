package com.example.myapplication.data.db.entities

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mapbox.mapboxsdk.geometry.LatLng

@Entity(tableName = "detination_entity")
data class DestinationEntity(
    @ColumnInfo(name = "destination_name")
    var destinationName: String,
    @ColumnInfo(name = "destination_lng")
    var destinationLng: Double,
    @ColumnInfo(name = "destination_lat")
    var destinationLat: Double
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}