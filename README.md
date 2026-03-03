# Android Unlock Logger

A lightweight Android utility designed to log device unlock events using Root privileges and provide seamless interaction via ADB shell commands. code with Google Gemini.

### Whats supported now
- **Silent Process Wake-up**: Includes a specialized InvisibleActivity (Theme.NoDisplay) to wake up the app process from a "Force Stopped" state without interrupting the user.
- **ADB Integration**: Trigger system notifications and Text-to-Speech (TTS) directly from the command line.
- **Root-Level Logging**: Accesses system logs to track precise unlock timestamps using UnlockLogManager.

### Requirements
- **Android OS**: 8.0 (Oreo) or higher.

- **Root**: Required for reading protected system log files. (Notifications and TTS will work without Root)

- **Target API**: 34 (Android 14).

### Installation
1. Clone the repository:
Bash
git clone https://github.com/Cyanide-zh/AndroidUnlockLogger.git
2. Build: Open the project in Android Studio and build the APK.
3. Permissions: Ensure the app is granted Root Access and Notification Permissions (Android 13+).

### Usage via ADB
- **Wake up the App (Even if Force-Stopped)**: `am start -n com.example.unlocklogger/.ui.InvisibleActivity`
- **Trigger a System Notification**: `am broadcast -a com.example.unlocklogger.ACTION_SHELL_NOTIFICATION -n com.example.unlocklogger/com.example.unlocklogger.receiver.NotificationReceiver -f 0x00000020 --es notification_message  "The time is ""$(date "+%Y-%m-%d_%H-%M-%S_%3N")" ; `
- **Text-to-Speech (TTS)**: `tts='Welcome to the H.E.V. Mark 4 protective system. For use in hazardous environment conditions. High-impact reactive armor activated. Atmospheric contaminant sensors activated. Vital sign monitoring activated. Automatic medical systems engaged. Defensive weapon selection system activated. Munition level monitoring activated. Communications interface online. Have a very safe day!' ; am broadcast -a com.example.unlocklogger.ACTION_TTS_SPEAK -n com.example.unlocklogger/com.example.unlocklogger.receiver.TtsReceiver  -f 0x00000020 --es tts_text "$tts" ;  `
