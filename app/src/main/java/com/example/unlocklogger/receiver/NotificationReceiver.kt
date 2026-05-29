package com.example.unlocklogger.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.unlocklogger.R // 🚨 确保引入了正确的 R 类
import com.example.unlocklogger.service.LocationService // 🚨 引入刚才创建的服务
import com.example.unlocklogger.ui.MainActivity

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "NotificationReceiver"
        const val ACTION_SHELL_NOTIFICATION = "com.example.unlocklogger.ACTION_SHELL_NOTIFICATION"  
        const val EXTRA_MESSAGE = "extra_message"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Receiver 被唤醒，Action: $action")

        when (action) {
            "com.example.unlocklogger.ACTION_START_LOCATION" -> {
                Log.d(TAG, "指令接收：准备启动持续定位服务")
                // 通过广播启动 Service
                val serviceIntent = Intent(context, LocationService::class.java)
                context.startService(serviceIntent)
            }
            "com.example.unlocklogger.ACTION_STOP_LOCATION" -> {
                Log.d(TAG, "指令接收：准备关闭持续定位服务")
                // 通过广播停止 Service
                val serviceIntent = Intent(context, LocationService::class.java)
                context.stopService(serviceIntent)
            }
            ACTION_SHELL_NOTIFICATION -> {
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "未指定通知内容"
                Log.d(TAG, "接收到 Shell 通知请求: $message")
                showNotification(context, message)
            }
        }
    }
    
    private fun showNotification(context: Context, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val uniqueNotificationId = (System.currentTimeMillis() % 1000000000).toInt()
        val channelId = MainActivity.CHANNEL_ID 

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) 
            .setContentTitle("ADB Shell 通知")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) 
            .build()
        
        notificationManager.notify(uniqueNotificationId, notification)
        Log.i(TAG, "成功显示 ADB Shell 通知 (ID: $uniqueNotificationId): $message")
    }
}
