// app/src/main/java/com/example/unlocklogger/receiver/TtsReceiver.kt

package com.example.unlocklogger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.Build
import com.example.unlocklogger.service.TtsJobService

class TtsReceiver : BroadcastReceiver() {

    private val TAG = "TtsReceiver"
    companion object {
        const val ACTION_TTS_SPEAK = "com.example.unlocklogger.ACTION_TTS_SPEAK"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TTS_SPEAK) {
            val text = intent.getStringExtra(TtsJobService.EXTRA_TEXT)
            
            if (!text.isNullOrEmpty()) {
                Log.d(TAG, "收到 TTS 广播，准备朗读: $text")
                // 将朗读任务委托给 JobService
                TtsJobService.enqueueWork(context, text)
            } else {
                Log.e(TAG, "收到的 TTS 广播中没有有效文本。")
            }
        }
    }
}