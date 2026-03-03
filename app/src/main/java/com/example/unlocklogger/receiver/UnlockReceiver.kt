package com.example.unlocklogger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_USER_PRESENT
import android.util.Log
import com.example.unlocklogger.service.UnlockJobService // 导入我们新的 JobService

/**
 * 负责接收屏幕解锁事件 (ACTION_USER_PRESENT)。
 * 由于 Android O+ 限制，它不执行耗时操作，而是将任务委托给 UnlockJobService。
 */
class UnlockReceiver : BroadcastReceiver() {

    private val TAG = "UnlockReceiver"
    
    override fun onReceive(context: Context, intent: Intent) {
        // 检查 Intent Action 是否为屏幕解锁
        if (intent.action == ACTION_USER_PRESENT) {
            
            // 记录日志，确认广播被接收
            Log.d(TAG, "接收到 ACTION_USER_PRESENT 广播，准备启动 JobService。")

            // 🚨 核心逻辑：将 Root 写入任务委托给 JobIntentService
            // JobIntentService 可以在应用的后台安全地执行耗时操作，绕过隐式广播限制。
            UnlockJobService.enqueueWork(context)
        }
    }
}