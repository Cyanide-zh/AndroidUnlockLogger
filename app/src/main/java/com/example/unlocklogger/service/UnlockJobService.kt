package com.example.unlocklogger.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.preference.PreferenceManager
import com.example.unlocklogger.core.Config
import com.example.unlocklogger.utils.RootShell
import java.text.SimpleDateFormat
import java.util.*

class UnlockJobService : JobIntentService() {
    private val TAG = "UnlockJobService"

    companion object {
        private const val JOB_ID = 1000
        
        fun enqueueWork(context: Context) {
            val intent = Intent(context, UnlockJobService::class.java)
            enqueueWork(context, UnlockJobService::class.java, JOB_ID, intent)
        }
    }

    // 在 JobIntentService 的工作线程中执行任务
    override fun onHandleWork(intent: Intent) {
        
        // 1. 获取配置的路径
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val timestampPath = prefs.getString(Config.KEY_TIMESTAMP_PATH, Config.DEFAULT_TIMESTAMP_PATH)!!
        val logRecordPath = prefs.getString(Config.KEY_LOG_RECORD_PATH, Config.DEFAULT_LOG_RECORD_PATH)!!
        val loggingEnabled = prefs.getBoolean(Config.KEY_LOGGING_ENABLED, true)

        // 2. 准备数据
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val currentMillis = System.currentTimeMillis()
		val currentSeconds = currentMillis / 1000L
        val formattedTime = dateFormat.format(Date(currentMillis))

        // 3. 执行 Root 写入任务 (直接在 onHandleWork 的后台线程中执行)
        
        // A. 写入最新解锁时间戳 (覆写)
		val timestampCmd = "echo $currentSeconds > $timestampPath"
        RootShell.execute(timestampCmd)

        // B. 记录日志 (追加)
        if (loggingEnabled) {
            val logCmd = "echo $formattedTime >> $logRecordPath"
            RootShell.execute(logCmd) // ⚠️ 直接调用 RootShell.execute()
        }
        
        // ❌ 注意：已删除顶部的 kotlinx.coroutines.* 导入，因为已不再需要
    }
}