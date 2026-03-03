package com.example.unlocklogger.core

// 存储配置用的键
object Config {
    // SharedPreferences 键
    const val KEY_LOGGING_ENABLED = "logging_enabled"
    const val KEY_TIMESTAMP_PATH = "timestamp_file_path"
    const val KEY_LOG_RECORD_PATH = "log_record_file_path"
    
    // 默认路径（使用您要求的系统路径作为默认值）
    const val DEFAULT_TIMESTAMP_PATH = "/data/last_unlock_time.txt" 
    const val DEFAULT_LOG_RECORD_PATH = "/data/local/tmp/unlock_records.txt"
}