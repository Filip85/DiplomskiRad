package com.example.myapplication.view.destinationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repositories.DestinationRepository

@Suppress("UNCHECKED_CAST")
class DestinationViewModelFactory(
    private val repository: DestinationRepository
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DestinationViewModel(repository) as T
    }
}