package com.example.myapplication.view.destinationlist

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.example.myapplication.R
import com.example.myapplication.data.db.entities.DestinationEntity
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.android.synthetic.main.pop_up_menu.*

class AddDestinationSaveDialog(context: Context, var addDialogListener: AddDialogListener, var destinationPoint: Point): AppCompatDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pop_up_menu)

        btnSaveDestionation.setOnClickListener {
            val name = etSaveName.text.toString()

            if(name.isEmpty()) {
                Toast.makeText(context, "Please enter name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val destination = DestinationEntity(name, destinationPoint.longitude(), destinationPoint.latitude())
            addDialogListener.onAddSaveButtonClick(destination)
            dismiss()
        }

        btnCloseDialog.setOnClickListener {
            cancel()
        }
    }
}