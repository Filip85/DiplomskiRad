package com.example.myapplication

import android.car.Car
import android.car.hardware.CarSensorEvent
import android.car.hardware.CarSensorManager
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var car: Car

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        car = Car.createCar(this, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val carSensorManager = car.getCarManager(Car.SENSOR_SERVICE) as CarSensorManager
                carSensorManager.registerListener(
                    { onCarSensorChanged(it) },
                    CarSensorManager.SENSOR_TYPE_CAR_SPEED,
                    CarSensorManager.SENSOR_RATE_NORMAL
                )
                Log.d("MainActivity", carSensorManager.toString())
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                TODO("Not yet implemented")
            }

        })
        car.connect()
    }

    fun onCarSensorChanged(carEvent: CarSensorEvent?) {
        if(carEvent?.sensorType == CarSensorManager.SENSOR_TYPE_CAR_SPEED) {
            speed.text = (carEvent.floatValues[0] * 3.6f).toString()   //array holding float type of sensor data. If the sensor has single value, only floatValues[0] should be used.
        }
    }
}

