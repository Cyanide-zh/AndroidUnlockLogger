package com.example.unlocklogger.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LocationHelper {
    @SuppressLint("MissingPermission")
    fun requestSingleLocation(context: Context, callback: (String) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e("LocationHelper", "GPS 未开启")
            callback("Error: GPS disabled")
            return
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // 1. 获取时间 (格式化为 yyyy-MM-dd HH:mm:ss)
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val timeString = sdf.format(Date(location.time))

                // 2. 格式化数据
                // 经纬度：原始精度 (不限制小数位)
                val lat = location.latitude
                val lon = location.longitude
                // 海拔：2位小数
                val alt = String.format(Locale.US, "%.2f", location.altitude)
                // 速度：m/s
                val speed = location.speed
                // 方向 (Bearing)：度数
                val bearing = location.bearing
                // 精确度 (Accuracy)：米
                val accuracy = location.accuracy

                val result = "Time: $timeString | Lat: $lat | Lon: $lon | Alt: ${alt}m | Speed: ${speed}m/s | Dir: ${bearing}° | Acc: ${accuracy}m"
                
                Log.i("LocationHelper", "定位成功: $result")
                callback(result)
                locationManager.removeUpdates(this) 
            }
            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
            override fun onProviderEnabled(p0: String) {}
            override fun onProviderDisabled(p0: String) {}
        }

        try {
            // 注意：requestSingleUpdate 可能在室内获取不到 GPS，
            // 首次测试建议在户外进行，以获取 GPS 卫星原始数据
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null)
        } catch (e: Exception) {
            Log.e("LocationHelper", "定位请求异常: ${e.message}")
        }
    }
}
