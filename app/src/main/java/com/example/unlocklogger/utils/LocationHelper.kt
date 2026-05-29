package com.example.unlocklogger.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

object LocationHelper {
    @SuppressLint("MissingPermission")
    fun requestSingleLocation(context: Context, callback: (String) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        // 使用高精度请求
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1 // 只请求一次
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    val format = "last location={Latitude=${location?.latitude}, Longitude=${location?.longitude}, Accuracy=${location?.accuracy}}"
                    callback(format)
                }
            },
            Looper.getMainLooper()
        )
    }
}
