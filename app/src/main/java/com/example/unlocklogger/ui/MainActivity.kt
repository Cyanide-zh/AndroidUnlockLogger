package com.example.unlocklogger.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log

/**
 * 隐形 Activity：仅用于冷启动进程，不显示任何界面
 */
class InvisibleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("UnlockLogger", "InvisibleActivity: 进程已唤醒")

        // 如果你希望在启动应用时顺便执行某些操作（比如发个通知）
        val msg = intent.getStringExtra("notification_message")
        if (msg != null) {
            // 这里可以直接调用你之前在 NotificationReceiver 里的通知逻辑
            Log.i("UnlockLogger", "收到随启动附带的消息: $msg")
        }

        // 🚨 极其重要：必须立即调用 finish()
        // 配合 Theme.NoDisplay，系统会认为这个 Activity 从未出现过
        finish()
    }
}