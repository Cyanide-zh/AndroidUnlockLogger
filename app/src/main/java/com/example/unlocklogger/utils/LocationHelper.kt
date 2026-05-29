package com.example.unlocklogger.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log

object LocationHelper {
    private const val TIMEOUT_MS = 40000L //单位毫秒

    @SuppressLint("MissingPermission")
    fun requestSingleLocation(context: Context, callback: (String) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val handler = Handler(Looper.getMainLooper())

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                handler.removeCallbacksAndMessages(null)
                val result = "Lat=${location.latitude}, Lon=${location.longitude}, Acc=${location.accuracy}"
                Log.i("LocationHelper", "GPS 定位成功: $result")
                callback(result)
                locationManager.removeUpdates(this)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                Log.e("LocationHelper", "GPS 被用户关闭")
                callback("Error: GPS disabled")
            }
        }

        val timeoutRunnable = Runnable {
            Log.e("LocationHelper", "GPS 超时触发")
            try {
                locationManager.removeUpdates(listener)
            } catch (e: Exception) {
                Log.e("LocationHelper", "移除监听异常: ${e.message}")
            }
            callback("Error: GPS Timeout")
        }
        
        handler.postDelayed(timeoutRunnable, TIMEOUT_MS)
        
        try {
            Log.d("LocationHelper", "开始请求纯 GPS 定位...")
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null)
        } catch (e: Exception) {
            Log.e("LocationHelper", "请求异常: ${e.message}")
            handler.removeCallbacksAndMessages(null)
        }
    }
}
