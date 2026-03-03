package com.example.unlocklogger.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.unlocklogger.R
import com.example.unlocklogger.core.Config

class SettingsFragment : PreferenceFragmentCompat() {
    
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)
        
        // 由于 Preference XML 中使用的是资源ID作为key，这里需要手动同步到Config常量中
        val timestampPref = findPreference<androidx.preference.EditTextPreference>(getString(R.string.pref_key_timestamp_path))
        timestampPref?.key = Config.KEY_TIMESTAMP_PATH
        
        val logRecordPref = findPreference<androidx.preference.EditTextPreference>(getString(R.string.pref_key_log_record_path))
        logRecordPref?.key = Config.KEY_LOG_RECORD_PATH
        
        val loggingEnabledPref = findPreference<androidx.preference.SwitchPreferenceCompat>(getString(R.string.pref_key_logging_enabled))
        loggingEnabledPref?.key = Config.KEY_LOGGING_ENABLED
    }
}