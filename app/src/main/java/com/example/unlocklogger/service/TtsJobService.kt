// app/src/main/java/com/example/unlocklogger/service/TtsJobService.kt

package com.example.unlocklogger.service

import android.content.Context
import android.content.Intent
import android.os.Build // 必须导入
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.JobIntentService
import java.util.concurrent.CountDownLatch // 必须导入：用于同步等待
import java.util.concurrent.TimeUnit      // 必须导入：用于设置等待超时
import java.util.Locale

class TtsJobService : JobIntentService(), TextToSpeech.OnInitListener {

    private val TAG = "TtsJobService"
    private lateinit var tts: TextToSpeech
    private var textToSpeak: String? = null
    
    // 锁机制：用于阻塞 onHandleWork，直到 TTS 播放完成或初始化失败
    private lateinit var ttsDoneLatch: CountDownLatch 

    companion object {
        private const val JOB_ID = 2000 // 确保 ID 唯一
        const val EXTRA_TEXT = "tts_text"

        fun enqueueWork(context: Context, text: String) {
            val intent = Intent(context, TtsJobService::class.java).apply {
                putExtra(EXTRA_TEXT, text)
            }
            enqueueWork(context, TtsJobService::class.java, JOB_ID, intent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        textToSpeak = intent.getStringExtra(EXTRA_TEXT)

        if (textToSpeak.isNullOrEmpty()) {
            Log.e(TAG, "未接收到要朗读的文本。")
            return
        }
        
        // 1. 初始化锁：计数器为 1 (代表 TTS 任务未完成)
        ttsDoneLatch = CountDownLatch(1) 

        // 2. 初始化 TTS 引擎
        tts = TextToSpeech(this, this)
        
        // 3. 阻塞线程，等待 TTS 任务完成或超时 (最长 15 秒)
        try {
            Log.d(TAG, "等待 TTS 初始化和播放完成...")
            val success = ttsDoneLatch.await(15, TimeUnit.SECONDS) 
            if (success) {
                Log.d(TAG, "TTS 播放成功完成。")
            } else {
                Log.e(TAG, "TTS 播放超时或未能开始，释放资源。")
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "JobService 线程被中断。", e)
            Thread.currentThread().interrupt()
        } finally {
            // 确保任务完成后释放资源，无论成功还是失败
            releaseTtsResources()
        }
    }
    
    // TTS 引擎初始化回调
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // 尝试设置语言为中文，如果需要其他语言可修改
            val result = tts.setLanguage(Locale.CHINA) 

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "语言数据丢失或不支持该语言。")
                ttsDoneLatch.countDown() // 立即释放锁
            } else {
                speakText(textToSpeak!!)
            }
        } else {
            Log.e(TAG, "TTS 初始化失败，Status: $status")
            ttsDoneLatch.countDown() // 立即释放锁
        }
    }

    private fun speakText(text: String) {
        // 设置监听器，以便知道语音何时播放完毕
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            
            override fun onStart(utteranceId: String) { 
                Log.d(TAG, "开始朗读: $utteranceId") 
            }
            
            override fun onError(utteranceId: String) { 
                Log.e(TAG, "朗读错误: $utteranceId") 
                ttsDoneLatch.countDown() // 播放错误时释放锁
            }
            
            override fun onDone(utteranceId: String) {
                Log.d(TAG, "朗读完毕: $utteranceId")
                ttsDoneLatch.countDown() // 🚨 关键：播放完毕时释放锁
            }
            
            // 确保覆盖所有抽象方法
            @Deprecated("Deprecated in Java")
            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                Log.d(TAG, "朗读停止: $utteranceId")
                ttsDoneLatch.countDown() // 朗读停止时释放锁
            }
        })
        
        val utteranceId = "TTS_SHELL_REQUEST"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            @Suppress("DEPRECATION")
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }
    
    /** 释放 TTS 引擎资源 */
    private fun releaseTtsResources() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 双重确保在服务销毁时释放资源
        releaseTtsResources()
        Log.d(TAG, "TtsJobService 销毁。")
    }
}