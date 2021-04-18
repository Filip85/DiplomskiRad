package com.example.myapplication.data.repositories

import com.example.myapplication.data.db.DestinationDatabase
import com.example.myapplication.data.db.entities.DestinationEntity

class DestinationRepository(
    private val db: DestinationDatabase
) {
    suspend fun upsert(destination: DestinationEntity) = db.getDestinationDao().upsert(destination)

    suspend fun delete(destination: DestinationEntity) = db.getDestinationDao().delete(destination)

    fun getAllDestinations() = db.getDestinationDao().getAllDestinations()

    suspend fun getDestinationLat(name: String) = db.getDestinationDao().getDestinationLat(name)

    suspend fun getDestinationLng(name: String) = db.getDestinationDao().getDestinationLng(name)
}