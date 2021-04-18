package com.example.myapplication.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.data.db.entities.DestinationEntity

@Dao
interface DestinationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(destination: DestinationEntity)

    @Delete
    suspend fun delete(destination: DestinationEntity)

    @Query("SELECT * FROM detination_entity")
    fun getAllDestinations(): LiveData<List<DestinationEntity>>

    @Query("SELECT destination_lat FROM detination_entity WHERE destination_name = :name")
    suspend fun getDestinationLat(name: String): Double

    @Query("SELECT destination_lng FROM detination_entity WHERE destination_name = :name")
    suspend fun getDestinationLng(name: String): Double
}