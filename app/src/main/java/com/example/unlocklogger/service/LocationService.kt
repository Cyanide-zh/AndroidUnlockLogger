package com.example.unlocklogger.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.unlocklogger.utils.LocationHelper

class LocationService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationService", "定位服务已启动，准备唤醒硬件")
        // 调用 Helper 开启持续定位
        LocationHelper.startTracking(this)
        // START_STICKY 表示如果服务被系统意外杀死，尝试自动重启服务
        return START_STICKY 
    }

    override fun onDestroy() {
        Log.d("LocationService", "定位服务正在关闭，准备释放硬件")
        // 当服务被停止（例如 stopservice 指令）时，确保关掉 GPS 硬件
        LocationHelper.stopTracking()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
