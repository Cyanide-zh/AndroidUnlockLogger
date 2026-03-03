// app/src/main/java/com/example/unlocklogger/service/TtsJobService.kt
package com.example.unlocklogger.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.JobIntentService
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
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
        
        // 1. 初始化锁
        ttsDoneLatch = CountDownLatch(1) 

        // 2. 初始化 TTS 引擎
        tts = TextToSpeech(this, this)
        
        // 3. 阻塞线程，等待播放完成
        try {
            Log.d(TAG, "正在准备播放长文本...")
            // 设置 300 秒（5分钟）超时，足以应对极长文本
            val success = ttsDoneLatch.await(300, TimeUnit.SECONDS) 
            if (success) {
                Log.d(TAG, "TTS 播放任务圆满完成。")
            } else {
                Log.e(TAG, "TTS 播放超时，可能文本过长或引擎卡死。")
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "JobService 线程被中断。", e)
            Thread.currentThread().interrupt()
        } finally {
            // 确保任务完成后释放资源
            releaseTtsResources()
        }
    }
    
    // TTS 引擎初始化回调
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // 自动获取系统当前默认语言，不再硬编码 Locale.CHINA 或 Locale.US
            val currentLocale = tts.defaultVoice?.locale ?: Locale.getDefault()
            val result = tts.setLanguage(currentLocale) 

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "当前设备不支持语言: $currentLocale")
                ttsDoneLatch.countDown() 
            } else {
                Log.d(TAG, "TTS 初始化成功，使用语言: $currentLocale")
                speakText(textToSpeak!!)
            }
        } else {
            Log.e(TAG, "TTS 初始化失败")
            ttsDoneLatch.countDown()
        }
    }

    private fun speakText(text: String) {
        // 使用正则表达式进行分句：匹配半角/全角 [。！？，.!?] 及其后的空格
        // 使用正向后瞻 (?<=...) 确保标点符号保留在分句末尾，使播报有自然停顿
        val regex = Regex("(?<=[。！？，.!?])\\s*|(?<=[,])\\s*")
        val sentences = text.split(regex).filter { it.isNotBlank() }
        
        if (sentences.isEmpty()) {
            ttsDoneLatch.countDown()
            return
        }

        val lastIndex = sentences.size - 1

        // 设置监听器
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) { 
                Log.d(TAG, "正在朗读分段: $utteranceId") 
            }
            
            override fun onError(utteranceId: String) { 
                Log.e(TAG, "朗读错误: $utteranceId") 
                ttsDoneLatch.countDown() 
            }
            
            override fun onDone(utteranceId: String) {
                // 只有当最后一段话朗读完毕时，才释放 CountDownLatch
                if (utteranceId == "part_$lastIndex") {
                    Log.d(TAG, "全部文本播放完毕。")
                    ttsDoneLatch.countDown()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                ttsDoneLatch.countDown()
            }
        })
        
        // 循环播报每一个分句
        sentences.forEachIndexed { index, sentence ->
            val utteranceId = "part_$index"
            // 第一句使用 QUEUE_FLUSH 清空之前的遗留，后续全部用 QUEUE_ADD 排队播放
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(sentence.trim(), queueMode, null, utteranceId)
            } else {
                val params = HashMap<String, String>()
                params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceId
                @Suppress("DEPRECATION")
                tts.speak(sentence.trim(), queueMode, params)
            }
        }
    }
    
    private fun releaseTtsResources() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
            Log.d(TAG, "TTS 资源已释放。")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseTtsResources()
    }
}
