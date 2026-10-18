package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings

sealed class DeviceActionResult {
    data class AppOpened(val appName: String, val speechMessage: String) : DeviceActionResult()
    data class AppNotFound(val appName: String, val speechMessage: String) : DeviceActionResult()
    data class ActionExecuted(val description: String, val speechMessage: String) : DeviceActionResult()
    object NotAnAction : DeviceActionResult()
}

class DeviceActionManager(private val context: Context) {

    fun tryExecuteVoiceCommand(rawPrompt: String): DeviceActionResult {
        val trimmed = rawPrompt.trim().lowercase()

        // 1. Detect App Launch Commands
        val targetApp = extractAppNameToOpen(trimmed)
        if (targetApp != null) {
            return launchAppByName(targetApp)
        }

        // 2. Detect System Device Actions
        if (isCameraCommand(trimmed)) {
            return openCamera()
        }

        if (isSettingsCommand(trimmed)) {
            return openSettings()
        }

        if (isAssistantSettingsCommand(trimmed)) {
            return openDefaultAssistantSettings()
        }

        if (isAlarmCommand(trimmed)) {
            return openAlarm()
        }

        return DeviceActionResult.NotAnAction
    }

    private fun extractAppNameToOpen(text: String): String? {
        val patterns = listOf(
            Regex("^(?:open|launch|start)\\s+(.+)$", RegexOption.IGNORE_CASE),
            Regex("^(.+?)\\s+(?:खोलो|ओपन करो|चालू करो|चलाओ|start करो)$", RegexOption.IGNORE_CASE),
            Regex("^(?:खोलो|ओपन करो)\\s+(.+)$", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val candidate = match.groupValues[1].trim()
                if (candidate.isNotBlank() && candidate.length > 1) {
                    return candidate
                }
            }
        }
        return null
    }

    fun launchAppByName(query: String): DeviceActionResult {
        val normalized = query.lowercase().trim()
        val pm = context.packageManager

        // Known direct mappings
        val directMapping = mapOf(
            "youtube" to "com.google.android.youtube",
            "यूट्यूब" to "com.google.android.youtube",
            "whatsapp" to "com.whatsapp",
            "व्हाट्सएप" to "com.whatsapp",
            "chrome" to "com.android.chrome",
            "क्रोम" to "com.android.chrome",
            "browser" to "com.android.chrome",
            "ब्राउज़र" to "com.android.chrome",
            "maps" to "com.google.android.apps.maps",
            "गूगल मैप्स" to "com.google.android.apps.maps",
            "मैप्स" to "com.google.android.apps.maps",
            "spotify" to "com.spotify.music",
            "स्पॉटिफ़ाई" to "com.spotify.music",
            "instagram" to "com.instagram.android",
            "इंस्टाग्राम" to "com.instagram.android",
            "facebook" to "com.facebook.katana",
            "फेसबुक" to "com.facebook.katana",
            "telegram" to "org.telegram.messenger",
            "टेलीग्राम" to "org.telegram.messenger",
            "gmail" to "com.google.android.gm",
            "जीमेल" to "com.google.android.gm",
            "ईमेल" to "com.google.android.gm",
            "camera" to "camera_intent",
            "कैमरा" to "camera_intent",
            "settings" to "settings_intent",
            "सेटिंग्स" to "settings_intent",
            "calculator" to "calculator_intent",
            "कैलकुलेटर" to "calculator_intent",
            "clock" to "clock_intent",
            "घड़ी" to "clock_intent",
            "alarm" to "alarm_intent",
            "अलार्म" to "alarm_intent"
        )

        val directTarget = directMapping[normalized]
        if (directTarget != null) {
            when (directTarget) {
                "camera_intent" -> return openCamera()
                "settings_intent" -> return openSettings()
                "alarm_intent", "clock_intent" -> return openAlarm()
                "calculator_intent" -> return openCalculator()
                else -> {
                    val launchIntent = pm.getLaunchIntentForPackage(directTarget)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        return DeviceActionResult.AppOpened(
                            appName = query.replaceFirstChar { it.uppercase() },
                            speechMessage = "Opening $query... $query खोला जा रहा है।"
                        )
                    }
                }
            }
        }

        // Search through all installed applications
        try {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installedApps) {
                val label = pm.getApplicationLabel(app).toString().lowercase()
                if (label.contains(normalized) || normalized.contains(label)) {
                    val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        val appDisplayName = pm.getApplicationLabel(app).toString()
                        return DeviceActionResult.AppOpened(
                            appName = appDisplayName,
                            speechMessage = "Opening $appDisplayName... $appDisplayName खोला जा रहा है।"
                        )
                    }
                }
            }
        } catch (_: Exception) {}

        return DeviceActionResult.AppNotFound(
            appName = query,
            speechMessage = "Could not find $query on your device. आपके डिवाइस पर $query नहीं मिला।"
        )
    }

    private fun isCameraCommand(text: String): Boolean {
        return text.contains("open camera") || text.contains("start camera") ||
                text.contains("कैमरा खोलो") || text.contains("कैमरा चालू करो") ||
                text.contains("फोटो खींचो") || text.contains("take a photo")
    }

    private fun isSettingsCommand(text: String): Boolean {
        return text == "settings" || text == "open settings" ||
                text == "सेटिंग्स" || text == "सेटिंग्स खोलो"
    }

    private fun isAssistantSettingsCommand(text: String): Boolean {
        return text.contains("default assistant") || text.contains("डिफ़ॉल्ट असिस्टेंट") ||
                text.contains("set default ai") || text.contains("डिफ़ॉल्ट एआई")
    }

    private fun isAlarmCommand(text: String): Boolean {
        return text.contains("alarm") || text.contains("अलार्म") ||
                text.contains("clock") || text.contains("घड़ी")
    }

    fun openCamera(): DeviceActionResult {
        return try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            DeviceActionResult.ActionExecuted(
                description = "Camera opened",
                speechMessage = "Camera is opening. कैमरा खोला जा रहा है।"
            )
        } catch (e: Exception) {
            DeviceActionResult.AppNotFound("Camera", "Unable to open camera.")
        }
    }

    fun openSettings(): DeviceActionResult {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            DeviceActionResult.ActionExecuted(
                description = "Settings opened",
                speechMessage = "Opening device settings. सेटिंग्स खोली जा रही हैं।"
            )
        } catch (e: Exception) {
            DeviceActionResult.AppNotFound("Settings", "Unable to open settings.")
        }
    }

    fun openCalculator(): DeviceActionResult {
        val calcPackages = listOf(
            "com.google.android.calculator",
            "com.android.calculator2",
            "com.sec.android.app.popupcalculator",
            "com.miui.calculator"
        )
        for (pkg in calcPackages) {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return DeviceActionResult.ActionExecuted(
                    description = "Calculator opened",
                    speechMessage = "Calculator is opening. कैलकुलेटर खोला जा रहा है।"
                )
            }
        }
        return DeviceActionResult.AppNotFound("Calculator", "Calculator app not found.")
    }

    fun openAlarm(): DeviceActionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            DeviceActionResult.ActionExecuted(
                description = "Alarms opened",
                speechMessage = "Opening clock and alarms. अलार्म और घड़ी खोली जा रही है।"
            )
        } catch (e: Exception) {
            DeviceActionResult.AppNotFound("Clock/Alarm", "Unable to open alarms.")
        }
    }

    fun openDefaultAssistantSettings(): DeviceActionResult {
        val intentList = listOf(
            Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        )

        for (intent in intentList) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return DeviceActionResult.ActionExecuted(
                    description = "Default Assistant Settings opened",
                    speechMessage = "Opening Assistant settings to select E-DEN as default. डिफ़ॉल्ट असिस्टेंट सेटिंग्स खोली जा रही हैं।"
                )
            } catch (_: Exception) {}
        }
        return DeviceActionResult.AppNotFound("Settings", "Could not open default assistant settings.")
    }

    fun openAppDetailsSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
