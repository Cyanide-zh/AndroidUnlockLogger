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
    private var activeListener: LocationListener? = null
    private var locManager: LocationManager? = null
    private const val TAG = "LocationHelper"

    @SuppressLint("MissingPermission")
    fun startTracking(context: Context, intervalMs: Long) {
        // 1. 防御性处理：如果之前有没关闭的监听，先强制关闭，防止重复叠加导致耗电
        stopTracking()

        locManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        // 2. 检查 GPS 是否开启
        if (locManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
            Log.e(TAG, "GPS 硬件未开启，无法启动持续定位！")
            return
        }

        // 3. 创建持续定位监听器
        activeListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // 每次位置变动都会触发此回调
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val timeString = sdf.format(Date(location.time))
                
                val lat = location.latitude
                val lon = location.longitude
                val alt = String.format(Locale.US, "%.2f", location.altitude)
                val speed = location.speed
                val bearing = location.bearing
                val accuracy = location.accuracy

                val result = "Time: $timeString | Lat: $lat | Lon: $lon | Alt: ${alt}m | Speed: ${speed}m/s | Dir: ${bearing}° | Acc: ${accuracy}m"
                
                // 持续打印到 logcat，标签为 UnlockLoggerLocation 方便过滤
                Log.i("UnlockLoggerLocation", "【实时轨迹】$result")
                
                // 🚨 注意：这里绝对不要写任何 removeUpdates 逻辑，让它一直运行！
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                Log.e(TAG, "GPS 被用户手动关闭，持续定位被迫中断")
            }
        }

        try {
            Log.d(TAG, "唤醒 GPS 硬件，开始长连接持续定位，当前应用间隔: ${intervalMs}ms")
            // 🚨 核心替换：每指定时间更新一次，距离变动 0 米就更新，强制硬件保持通电
            locManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 
                intervalMs, 
                0f, 
                activeListener!!
            )
        } catch (e: Exception) {
            Log.e(TAG, "启动持续定位失败异常: ${e.message}")
        }
    }

    // 🚨 必须配套提供此停止方法，供外界关闭硬件
    fun stopTracking() {
        try {
            activeListener?.let {
                locManager?.removeUpdates(it)
                Log.d(TAG, "持续定位已成功关闭，GPS 硬件断电释放")
            }
        } catch (e: Exception) {
            Log.e(TAG, "关闭定位异常: ${e.message}")
        }
        activeListener = null
        locManager = null
    }
}
