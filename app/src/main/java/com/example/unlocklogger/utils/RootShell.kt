package com.example.unlocklogger.utils

import android.util.Log
import java.io.DataOutputStream

/**
 * 用于在后台执行 Root (su) 命令的工具类。
 */
object RootShell {

    private const val TAG = "RootShell"

    /**
     * 执行一个或多个 Root 命令。
     * @param command 要执行的 shell 命令字符串，例如 "echo 1 > /data/file.txt"
     * @return 成功执行返回 true，否则返回 false。
     */
    fun execute(command: String): Boolean {
        var process: Process? = null
        var outputStream: DataOutputStream? = null
        try {
            // 请求 Root 权限
            process = Runtime.getRuntime().exec("su")
            outputStream = DataOutputStream(process.outputStream)

            // 写入并执行命令
            outputStream.writeBytes(command + "\n")
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            
            // 等待命令执行完毕
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                Log.i(TAG, "Root 命令执行成功: $command")
                return true
            } else {
                Log.e(TAG, "Root 命令执行失败 (Code: $exitCode, Cmd: $command)")
                // 尝试打印错误流，以帮助调试权限问题或路径问题
                val errorStream = process.errorStream.bufferedReader().readText()
                Log.e(TAG, "Root Shell 错误输出: $errorStream")
                return false
            }

        } catch (e: Exception) {
            Log.e(TAG, "执行 Root 命令时发生异常 (设备可能未 Root): " + e.message)
            return false
        } finally {
            try {
                outputStream?.close()
                process?.destroy()
            } catch (e: Exception) {
                // 忽略关闭异常
            }
        }
    }
}