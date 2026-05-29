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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LocationHelper {
    // 设置超时时间，单位：毫秒 (例如 40秒)
    private const val TIMEOUT_MS = 40000L

    @SuppressLint("MissingPermission")
    fun requestSingleLocation(context: Context, callback: (String) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val handler = Handler(Looper.getMainLooper())

        // 检查是否有可用提供者
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        
        if (!isGpsEnabled) {
            Log.e("LocationHelper", "所有定位服务均未开启")
            callback("Error: All location providers disabled")
            return
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // 收到位置后，立即移除定时器，避免触发超时
                handler.removeCallbacksAndMessages(null)

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val timeString = sdf.format(Date(location.time))

                val lat = location.latitude
                val lon = location.longitude
                val alt = String.format(Locale.US, "%.2f", location.altitude)
                val speed = location.speed
                val bearing = location.bearing
                val accuracy = location.accuracy

                val result = "Time: $timeString | Lat: $lat | Lon: $lon | Alt: ${alt}m | Speed: ${speed}m/s | Dir: ${bearing}° | Acc: ${accuracy}m"
                
                Log.i("LocationHelper", "定位成功: $result")
                callback(result)
                
                // 停止所有监听，这会促使系统关闭相关硬件
                locationManager.removeUpdates(this)
            }
            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
            override fun onProviderEnabled(p0: String) {}
            override fun onProviderDisabled(p0: String) {}
        }

        // 定义超时逻辑
        val timeoutRunnable = Runnable {
            Log.e("LocationHelper", "定位超时，强制停止")
            locationManager.removeUpdates(listener)
            callback("Error: Location request timed out")
        }
        handler.postDelayed(timeoutRunnable, TIMEOUT_MS)

        try {
            // 请求 GPS 定位
            if (isGpsEnabled) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null)
            }
        } catch (e: Exception) {
            Log.e("LocationHelper", "定位请求异常: ${e.message}")
            handler.removeCallbacksAndMessages(null)
        }
    }
}
