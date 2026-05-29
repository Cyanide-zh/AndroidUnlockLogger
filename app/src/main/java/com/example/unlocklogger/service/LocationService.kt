package com.example.unlocklogger.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.unlocklogger.utils.LocationHelper

class LocationService : Service() {

    companion object {
        private const val CHANNEL_ID = "location_foreground_channel"
        private const val NOTIFICATION_ID = 99
    }

    override fun onCreate() {
        super.onCreate()
        // 创建前台服务所需的通知渠道（Android 8.0+ 必须）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GPS 实时追踪服务",
                NotificationManager.IMPORTANCE_LOW // 设置为 LOW，避免通知发出刺耳的提示音
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationService", "前台定位服务已启动，准备提升权限并唤醒硬件")

        // 1. 构建常驻通知
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS 实时定位运行中")
            .setContentText("正在通过 ADB Shell 持续同步卫星数据...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) // 使用系统自带图标
            .setOngoing(true) // 设置为无法被用户滑动清除
            .build()

        // 2. 🚨 关键：将服务提升为前台服务（强制锁定硬件权限）
        startForeground(NOTIFICATION_ID, notification)

        // 3. 启动持续定位
        LocationHelper.startTracking(this)

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("LocationService", "前台定位服务正在关闭，释放硬件并降级")
        LocationHelper.stopTracking()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
