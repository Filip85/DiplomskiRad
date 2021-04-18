package com.example.myapplication.view.destinationlist

import com.example.myapplication.data.db.entities.DestinationEntity

interface AddDialogListener {
    fun onAddSaveButtonClick(destination: DestinationEntity)
}