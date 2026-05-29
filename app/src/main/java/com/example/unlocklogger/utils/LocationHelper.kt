package com.example.unlocklogger.utils

// LocationHelper.kt 简化版
import android.location.LocationManager
import android.location.LocationListener
import android.annotation.SuppressLint

@SuppressLint("MissingPermission")
fun requestSingleLocation(context: Context, callback: (String) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    // 检查是否有 GPS 提供者
    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        Log.e("UnlockLogger", "GPS 未开启")
        return
    }

    val listener = object : LocationListener {
        override fun onLocationChanged(location: android.location.Location) {
            val result = "Lat=${location.latitude}, Lon=${location.longitude}"
            callback(result)
            locationManager.removeUpdates(this) // 获取一次后立即移除
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: android.os.Bundle?) {}
    }

    // 主动申请一次更新
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, listener)
}
