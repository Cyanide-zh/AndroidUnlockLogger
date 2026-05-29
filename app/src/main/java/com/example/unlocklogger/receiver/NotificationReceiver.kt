package com.example.unlocklogger.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.unlocklogger.R
import com.example.unlocklogger.ui.MainActivity
import com.example.unlocklogger.utils.LocationHelper // 必须添加这一行
/**
 * 接收来自 ADB Shell 命令的特定广播，并在通知栏显示消息。
 */
class NotificationReceiver : BroadcastReceiver() {

    private val TAG = "NotificationReceiver"
    
    companion object {
        const val ACTION_SHELL_NOTIFICATION = "com.example.unlocklogger.ACTION_SHELL_NOTIFICATION" 
        private const val EXTRA_MESSAGE = "notification_message"
    }
    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.e("TEST_DEBUG", "Receiver 被唤醒了！Action: " + intent.action)
        val action = intent.action
        Log.d("UnlockLogger", "onReceive 触发, Action: ${intent.action}")
        if (intent.action == "com.example.unlocklogger.ACTION_GET_LOCATION") {
            // 使用一个异步方法或者直接启动 Service/Worker 来处理定位
            // 因为 GPS 定位请求是异步的，Receiver 随时可能被系统销毁
            Log.d("UnlockLogger", "准备请求位置...")
            LocationHelper.requestSingleLocation(context) { locationString ->
                Log.i("UnlockLoggerLocation", "LOCATION_RESULT: $locationString")
            }
        } else if (intent.action == ACTION_SHELL_NOTIFICATION) {
            val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "未指定通知内容"
            Log.d(TAG, "接收到 Shell 通知请求: $message")
            // 构建并显示通知
            showNotification(context, message)
        }
    }
    /**
     * 构建并显示通知
     */
    private fun showNotification(context: Context, message: String) {
        val notificationManager = 
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 🚨 关键修改点：在方法内部生成 ID
        // 每次调用时获取当前系统时间的毫秒数并取模，确保生成一个独一无二的 Int ID
        val uniqueNotificationId = (System.currentTimeMillis() % 1000000000).toInt()

        val channelId = MainActivity.CHANNEL_ID 

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // 请确保此图标在 res/drawable 中存在
            .setContentTitle("ADB Shell 通知")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) 
            .build()
        
        // 🚨 使用生成的 uniqueNotificationId
        notificationManager.notify(uniqueNotificationId, notification)
        
        Log.i(TAG, "成功显示 ADB Shell 通知 (ID: $uniqueNotificationId): $message")
    }
}
