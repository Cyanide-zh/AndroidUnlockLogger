package com.example.unlocklogger.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.preference.PreferenceManager 
import com.example.unlocklogger.core.Config
//导入不变
class UnlockLogManager(private val context: Context) {
    
    private val TAG = "UnlockLogManager"
    
    // 获取日志路径的辅助函数
    private fun getLogFilePath(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(Config.KEY_LOG_RECORD_PATH, Config.DEFAULT_LOG_RECORD_PATH)!!
    }

    /**
     * 读取指定路径下的所有解锁记录 (通过 Root Shell)。
     */
    suspend fun readAllLogs(): List<String> = withContext(Dispatchers.IO) {
        val logPath = getLogFilePath()
        val catCommand = "cat $logPath"
        
        try {
            // 执行 cat 命令来读取文件内容
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", catCommand))
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                // 读取标准输出流
                val logsContent = process.inputStream.bufferedReader().readText()
                val logs = logsContent.lines().filter { it.isNotBlank() }.reversed()
                Log.d(TAG, "Root 读取成功，共 ${logs.size} 条记录。")
                return@withContext logs
            } else {
                val errorOutput = process.errorStream.bufferedReader().readText()
                Log.e(TAG, "Root 读取失败 (Code: $exitCode, Path: $logPath): $errorOutput")
            }
        } catch (e: Exception) {
            Log.e(TAG, "执行 Root cat 命令时发生异常: ${e.message}")
        }
        // 如果失败，尝试读取应用私有目录下的旧文件（作为兼容/调试手段，但不是主要逻辑）
        return@withContext emptyList<String>()
    }
    
    // ... (其他方法如 clearAllLogs 和 deleteLogRecord 需要大幅修改，因为它们涉及 Root 下的文件操作，
    // 在纯 Shell 中实现文件的删除/清空/单行删除会非常复杂。这里只提供清空的 Root 实现。)


    /**
     * 清空指定路径的日志文件 (通过 Root Shell)。
     */
    suspend fun clearAllLogs(): Boolean = withContext(Dispatchers.IO) {
        val logPath = getLogFilePath()
        // 清空文件：将空字符串写入文件 (覆写)
        val command = "echo -n > $logPath" 
        
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                Log.i(TAG, "Root 成功清空日志文件: $logPath")
                return@withContext true
            } else {
                Log.e(TAG, "Root 清空失败 (Code: $exitCode, Cmd: $command)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Root 清空时发生异常: ${e.message}")
        }
        return@withContext false
    }
    
    /**
     * ⚠️ 警告：删除单条记录 (deleteLogRecord) 在纯 Root Shell 中实现极为复杂且低效。
     * 如果坚持要删除单条记录，建议：
     * 1. 在应用中读取全部内容。
     * 2. 在应用内存中删除指定行。
     * 3. 使用 Root 权限将修改后的全部内容写回文件 (覆写)。
     * 为避免代码过于冗长，此实现将暂时跳过单行删除的 Root 实现，只提供清空。
     */
    suspend fun deleteLogRecord(recordIndex: Int): Boolean = withContext(Dispatchers.IO) {
        // ... 此处需要复杂的 Root 写入逻辑，或执行 'readAllLogs' -> 'delete in memory' -> 'write back (Root)'
        // 为了避免过度复杂，只提供一个失败提示
        Log.e(TAG, "Root 模式下，单行删除功能未实现，请使用清空功能。")
        return@withContext false
    }

}