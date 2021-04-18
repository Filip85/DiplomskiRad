package com.example.myapplication.view.destinationlist

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.db.entities.DestinationEntity
import com.example.myapplication.data.repositories.DestinationRepository
import kotlinx.coroutines.*

class DestinationViewModel(
    private val repository: DestinationRepository
): ViewModel() {

    fun upsert(destination: DestinationEntity) = CoroutineScope(Dispatchers.Main).launch {
        repository.upsert(destination)
    }

    fun delete(destination: DestinationEntity) = CoroutineScope(Dispatchers.Main).launch {
        repository.delete(destination)
    }

    fun getAllDestinations() = repository.getAllDestinations()

    fun getDestinationLat(name: String) = runBlocking {
        repository.getDestinationLat(name)
    }

    fun getDestinationLng(name: String) = runBlocking {
        repository.getDestinationLng(name)
    }
}