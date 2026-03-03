// app/src/main/java/com/example/unlocklogger/ui/MainActivity.kt

package com.example.unlocklogger.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
// 权限请求所需导入
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
// 导入正确的 Activity 基类
import androidx.appcompat.app.AppCompatActivity 
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unlocklogger.R // 确保导入了 R 类
import com.example.unlocklogger.core.Config // 导入 Config 常量文件
import com.example.unlocklogger.data.UnlockLogManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    companion object {
        // 通知相关常量
        const val CHANNEL_ID = "shell_notification_channel"
        const val CHANNEL_NAME = "Shell Notifications"
        const val KEY_LOGGING_ENABLED = Config.KEY_LOGGING_ENABLED 
        
        // 权限请求代码
        private const val REQUEST_NOTIFICATION_PERMISSION = 100
    }

    private lateinit var logManager: UnlockLogManager
    private lateinit var logAdapter: LogAdapter
    
    // Coroutine Scope 用于管理后台任务
    private val uiScope = CoroutineScope(Dispatchers.Main + Job()) 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 加载 XML 布局文件
        setContentView(R.layout.activity_main) 

        // 实例化日志管理器
        logManager = UnlockLogManager(applicationContext)

        // 🚨 新增：处理 Android 13+ 的通知权限请求
        requestNotificationPermission()
        
        // 创建通知通道 (无论是 ADB 还是应用内都需确保存在)
        createNotificationChannel()
        
        // 1. 加载 SettingsFragment 到 FragmentContainerView 
        // R.id.settings_fragment_container 必须在 activity_main.xml 中存在
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_fragment_container, SettingsFragment())
            .commit()
            
        // 2. 设置日志列表 UI
        setupLogListUi()
        // 3. 加载并显示日志
        loadLogs()
    }
    
    /**
     * 初始化日志列表 (RecyclerView) 和清空按钮。
     */
    private fun setupLogListUi() {
        val clearButton: Button = findViewById(R.id.button_clear_logs)
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view_logs)
        
        // 1. 初始化 RecyclerView 和 Adapter
        logAdapter = LogAdapter(mutableListOf()) { index -> 
            Toast.makeText(this, "Root 模式下暂不支持单条删除，请使用清空按钮。", Toast.LENGTH_SHORT).show()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = logAdapter

        // 2. 设置清空按钮监听器
        clearButton.setOnClickListener {
            clearAllLogs()
        }
    }

    /** 异步加载日志记录并更新 UI */
    private fun loadLogs() {
        uiScope.launch {
            // 通过 UnlockLogManager (使用 Root Shell cat) 读取日志
            val logs = logManager.readAllLogs()
            
            withContext(Dispatchers.Main) {
                logAdapter.updateLogs(logs)
                Toast.makeText(this@MainActivity, "已加载 ${logs.size} 条记录", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /** 异步清空所有日志 (使用 Root Shell echo -n >) */
    private fun clearAllLogs() {
        uiScope.launch {
            val success = logManager.clearAllLogs()
            if (success) {
                withContext(Dispatchers.Main) {
                    logAdapter.updateLogs(emptyList()) // 清空 Adapter 数据
                    Toast.makeText(this@MainActivity, "日志已清空 (Root)", Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "清空失败 (Root 权限不足或路径错误)", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * 🚨 新增：在 Android 13 (API 33) 及以上版本运行时请求 POST_NOTIFICATIONS 权限。
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) { 
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                
                ActivityCompat.requestPermissions(
                    this, 
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    /**
     * 创建通知渠道。
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = "由 ADB Shell 命令触发的通知"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}