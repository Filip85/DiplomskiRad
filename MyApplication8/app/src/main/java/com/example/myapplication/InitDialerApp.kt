package com.example.myapplication;

import android.Manifest.permission.*
import android.app.Activity
import android.car.Car
import android.car.Car.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.example.myapplication.view.MainActivity

var ALL_PERMISSIONS = 10001

class InitDialerApp : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.init_dialer_app)

        arePermissionsGranted()
    }

    private fun arePermissionsGranted() {
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                !((checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
                                && (checkSelfPermission(WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED)
                                && (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                                && (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                                && (checkSelfPermission(READ_SYNC_SETTINGS) == PackageManager.PERMISSION_GRANTED)
                                && (checkSelfPermission(WRITE_SYNC_SETTINGS) == PackageManager.PERMISSION_GRANTED)
                                && (checkSelfPermission(PERMISSION_CONTROL_CAR_DOORS) == PackageManager.PERMISSION_GRANTED)
                                && (checkSelfPermission(PERMISSION_MILEAGE) == PackageManager.PERMISSION_GRANTED)
                                && (checkSelfPermission(PERMISSION_CAR_DYNAMICS_STATE) == PackageManager.PERMISSION_GRANTED)
                                && (checkSelfPermission(PERMISSION_CAR_ENGINE_DETAILED) == PackageManager.PERMISSION_GRANTED))
            } else {
                TODO("VERSION.SDK_INT < M")
            }
        )
        {
            val permissions = arrayOf(
                    READ_CONTACTS,
                    WRITE_CONTACTS,
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE,
                    READ_SYNC_SETTINGS,
                    WRITE_SYNC_SETTINGS,
                    PERMISSION_CONTROL_CAR_DOORS,
                    PERMISSION_MILEAGE,
                    PERMISSION_CAR_DYNAMICS_STATE,
                    PERMISSION_CAR_ENGINE_DETAILED,
                    ACCESS_FINE_LOCATION,
                    INTERNET
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions (permissions, ALL_PERMISSIONS)
            }

        } else {
            val mainActivity = Intent(this, MainActivity::class.java)
            startActivity(mainActivity)
        }

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String?>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("PERMISSIONSSS", grantResults.size.toString())
        Log.d("PERMISSIONSSS", permissions.toString())
        grantResults[6] = 0
        when (requestCode) {
            ALL_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && permissions.size == grantResults.size) {
                    var i = 0
                    while (i < permissions.size) {

                        //Log.d("456789101112", grantResults[i].toString())
                        grantResults[i] = 0
                        Log.d("456789101112", grantResults[i].toString())
                        Log.d("12345678910", permissions[i].toString())
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            Log.d("PERMISSIONS DENIED", permissions[i].toString())

                            finish()
                        }
                        i++
                    }
                }

                val mainActivity = Intent(this, MainActivity::class.java)
                startActivity(mainActivity)
            }
        }
    }

    /*override fun requestPermissions(permissions: Array<(out) String!>, code: Int) {
        super.requestPermissions(permissions, code)
    }*/

    /*override fun requestPermissions(permissions: Array<String?>, requestCode: Int) {
        super.requestPermissions(permissions, requestCode)
    }*/


}
