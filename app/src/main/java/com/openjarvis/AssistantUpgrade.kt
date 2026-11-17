package com.openjarvis

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

class AssistantUpgrade(context: Context) {
    data class Persona(
        val id: String,
        val displayName: String,
        val wakeWords: List<String>,
        val systemPrompt: String,
        val pitch: Float,
        val speechRate: Float,
        val isBuiltIn: Boolean
    )

    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    val activePersona: Persona
        get() {
            val allPersonas = getPersonas()
            return allPersonas.firstOrNull { it.id == preferences.getString(KEY_ACTIVE_ID, DEFAULT_ID) }
                ?: allPersonas.first()
        }

    fun getPersonas(): List<Persona> {
        val defaults = builtInPersonas()
        val stored = preferences.getString(KEY_PERSONAS, null) ?: return defaults
        return try {
            val savedPersonas = JSONArray(stored).let { array ->
                (0 until array.length()).mapNotNull { index ->
                    array.optJSONObject(index)?.toPersona()
                }
            }
            if (savedPersonas.isEmpty()) defaults else savedPersonas
        } catch (error: Exception) {
            Log.w(TAG, "Could not read saved personas; using built-in personas", error)
            defaults
        }
    }

    fun switchPersona(idOrName: String): Persona? {
        val target = idOrName.trim()
        val selected = getPersonas().firstOrNull {
            it.id.equals(target, ignoreCase = true) ||
                it.displayName.equals(target, ignoreCase = true) ||
                it.wakeWords.any { word -> word.equals(target, ignoreCase = true) }
        } ?: return null
        preferences.edit().putString(KEY_ACTIVE_ID, selected.id).apply()
        return selected
    }

    fun renameActive(newName: String): Persona? {
        val cleanedName = newName.trim().trimEnd('.', '!', '?', ',')
        if (cleanedName.isBlank()) return null
        val current = activePersona
        val renamed = current.copy(
            displayName = cleanedName,
            wakeWords = current.wakeWords
                .filterNot { it.equals(current.displayName, ignoreCase = true) }
                .plus(cleanedName)
                .distinctBy { it.lowercase(Locale.ROOT) }
        )
        savePersona(renamed)
        return renamed
    }

    fun addCustomPersona(
        displayName: String,
        systemPrompt: String,
        pitch: Float = 1.0f,
        speechRate: Float = 1.0f
    ): Persona? {
        val cleanedName = displayName.trim()
        if (cleanedName.isBlank()) return null
        val persona = Persona(
            id = "custom-${UUID.randomUUID()}",
            displayName = cleanedName,
            wakeWords = listOf(cleanedName),
            systemPrompt = systemPrompt.trim().ifBlank { DEFAULT_CUSTOM_PROMPT },
            pitch = pitch.coerceIn(0.5f, 2.0f),
            speechRate = speechRate.coerceIn(0.5f, 2.0f),
            isBuiltIn = false
        )
        persistPersonas(getPersonas() + persona)
        preferences.edit().putString(KEY_ACTIVE_ID, persona.id).apply()
        return persona
    }

    fun savePersona(persona: Persona): Boolean {
        if (persona.displayName.isBlank()) return false
        val personas = getPersonas()
        if (personas.none { it.id == persona.id }) return false
        val updated = persona.copy(
            displayName = persona.displayName.trim(),
            wakeWords = (persona.wakeWords + persona.displayName.trim())
                .filter(String::isNotBlank)
                .distinctBy { it.lowercase(Locale.ROOT) },
            pitch = persona.pitch.coerceIn(0.5f, 2.0f),
            speechRate = persona.speechRate.coerceIn(0.5f, 2.0f)
        )
        persistPersonas(personas.map { if (it.id == updated.id) updated else it })
        return true
    }

    fun deleteCustomPersona(id: String): Boolean {
        val personas = getPersonas()
        val target = personas.firstOrNull { it.id == id } ?: return false
        if (target.isBuiltIn) return false
        persistPersonas(personas.filterNot { it.id == id })
        if (preferences.getString(KEY_ACTIVE_ID, DEFAULT_ID) == id) {
            preferences.edit().putString(KEY_ACTIVE_ID, DEFAULT_ID).apply()
        }
        return true
    }

    fun isWakeWord(text: String): Boolean {
        val normalized = normalize(text)
        if (normalized.isBlank()) return false
        val acceptedWords = (activePersona.wakeWords + activePersona.displayName + LEGACY_WAKE_WORDS)
            .map(::normalize)
            .filter(String::isNotBlank)
        return acceptedWords.any { word ->
            Regex("(^|\\b)${Regex.escape(word)}(\\b|$)").containsMatchIn(normalized)
        }
    }

    fun handleCommand(command: String): String? {
        val text = command.trim()
        if (text.isBlank()) return null

        SWITCH_COMMAND.matchEntire(text)?.let { match ->
            val character = switchPersona(match.groupValues[1])
            return if (character != null) {
                "Understood. I am now ${character.displayName}."
            } else {
                "I could not find that character. Available characters: ${getPersonas().joinToString { it.displayName }}."
            }
        }
        if (LIST_COMMAND.matches(text)) {
            return "Available characters: ${getPersonas().joinToString { it.displayName }}."
        }
        RENAME_COMMAND.matchEntire(text)?.let { match ->
            val persona = renameActive(match.groupValues[1])
            return if (persona != null) "Understood. You can call me ${persona.displayName}." else "Please provide a name."
        }

        return executeDeviceCommand(text)
    }

    private fun executeDeviceCommand(command: String): String? {
        val normalized = normalize(command)
        when {
            FLASHLIGHT_ON.matches(normalized) -> return setFlashlight(true)
            FLASHLIGHT_OFF.matches(normalized) -> return setFlashlight(false)
            normalized == "mute volume" || normalized == "mute" -> {
                audioManager().setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                return "Volume muted."
            }
            normalized == "max volume" || normalized == "maximum volume" -> {
                audioManager().let { audio ->
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
                }
                return "Volume set to maximum."
            }
            normalized == "volume up" || normalized == "turn volume up" -> {
                audioManager().adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
                return "Volume increased."
            }
            normalized == "volume down" || normalized == "turn volume down" -> {
                audioManager().adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)
                return "Volume decreased."
            }
        }

        ALARM_COMMAND.matchEntire(command.lowercase(Locale.ROOT).trim().trimEnd('.', '!', '?'))?.let { match ->
            val hour = match.groupValues[1].toIntOrNull()
            val minute = match.groupValues[2].toIntOrNull()
            if (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) {
                return "Please specify an alarm time in 24-hour HH:MM format."
            }
            return launchIntent(
                Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_HOUR, hour)
                    putExtra(AlarmClock.EXTRA_MINUTES, minute)
                    putExtra(AlarmClock.EXTRA_MESSAGE, "${activePersona.displayName} alarm")
                    putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                },
                "Opening the alarm setup for %02d:%02d.".format(Locale.ROOT, hour, minute)
            )
        }

        TIMER_COMMAND.matchEntire(normalized)?.let { match ->
            val amount = match.groupValues[1].toLongOrNull()
                ?: return "Please specify a valid timer duration."
            val unit = match.groupValues[2]
            val multiplier = when {
                unit.startsWith("hour") -> 3600L
                unit.startsWith("minute") -> 60L
                else -> 1L
            }
            if (amount <= 0 || amount > Int.MAX_VALUE.toLong() / multiplier) {
                return "Please specify a timer duration between one second and about 68 years."
            }
            val seconds = amount * multiplier
            return launchIntent(
                Intent(AlarmClock.ACTION_SET_TIMER).apply {
                    putExtra(AlarmClock.EXTRA_LENGTH, seconds.toInt())
                    putExtra(AlarmClock.EXTRA_MESSAGE, "${activePersona.displayName} timer")
                    putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                },
                "Opening the timer for $amount $unit."
            )
        }

        if (normalized == "open camera" || normalized == "launch camera" || normalized == "camera") {
            return launchIntent(Intent(MediaStore.ACTION_IMAGE_CAPTURE), "Opening the camera.")
        }

        when {
            normalized.contains("battery status") || normalized.contains("battery percentage") ||
                normalized == "battery level" -> return batteryStatus()
        }

        GOOGLE_COMMAND.matchEntire(command)?.let { match ->
            val query = match.groupValues[1].trim()
            if (query.isBlank()) return "What would you like me to search for?"
            return launchIntent(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")),
                "Searching Google for $query."
            )
        }

        if (normalized.contains("wifi") || normalized.contains("wi fi")) {
            return launchIntent(Intent(Settings.ACTION_WIFI_SETTINGS), "Opening Wi-Fi settings.")
        }
        if (normalized.contains("bluetooth")) {
            return launchIntent(Intent(Settings.ACTION_BLUETOOTH_SETTINGS), "Opening Bluetooth settings.")
        }
        if (normalized.contains("display settings") || normalized == "display" || normalized == "open display") {
            return launchIntent(Intent(Settings.ACTION_DISPLAY_SETTINGS), "Opening display settings.")
        }
        if (normalized.contains("battery settings") || normalized == "battery" || normalized == "open battery") {
            return launchIntent(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS), "Opening battery settings.")
        }

        OPEN_APP_COMMAND.matchEntire(command)?.let { match ->
            val appName = match.groupValues[1].trim()
            if (appName.equals("camera", ignoreCase = true)) {
                return launchIntent(Intent(MediaStore.ACTION_IMAGE_CAPTURE), "Opening the camera.")
            }
            return openApp(appName)
        }
        return null
    }

    private fun setFlashlight(enabled: Boolean): String {
        if (appContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return "Camera permission is required for flashlight control. Grant it in Android app permissions."
        }
        if (!appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            return "This device does not have a flashlight."
        }
        return try {
            val cameraManager = appContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return "No flashlight is available."
            cameraManager.setTorchMode(cameraId, enabled)
            if (enabled) "Flashlight on." else "Flashlight off."
        } catch (error: SecurityException) {
            "Camera permission is required for flashlight control."
        } catch (error: CameraAccessException) {
            Log.w(TAG, "Could not change flashlight state", error)
            "The flashlight is unavailable right now."
        }
    }

    private fun audioManager(): AudioManager =
        appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private fun openApp(label: String): String {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val matches = appContext.packageManager.queryIntentActivities(launcherIntent, 0)
        val match = matches
            .map { it.activityInfo.packageName to it.loadLabel(appContext.packageManager).toString() }
            .firstOrNull { (_, appLabel) -> appLabel.equals(label, ignoreCase = true) }
            ?: matches
                .map { it.activityInfo.packageName to it.loadLabel(appContext.packageManager).toString() }
                .firstOrNull { (_, appLabel) -> appLabel.contains(label, ignoreCase = true) }
        val selected = match ?: return "I could not find an app named $label."
        val packageName = selected.first
        val intent = appContext.packageManager.getLaunchIntentForPackage(packageName)
            ?: return "I could not launch $label."
        return launchIntent(intent, "Opening ${selected.second}.")
    }

    private fun batteryStatus(): String {
        val batteryManager = appContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            -1
        }
        val batteryIntent = appContext.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val chargeState = if (charging) "charging" else "not charging"
        return if (level in 0..100) "Battery is at $level% and is $chargeState." else "Battery status: $chargeState."
    }

    private fun launchIntent(intent: Intent, successMessage: String): String {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            appContext.startActivity(intent)
            successMessage
        } catch (error: ActivityNotFoundException) {
            "This device does not have an app to handle that action."
        } catch (error: SecurityException) {
            Log.w(TAG, "Permission denied while starting device action", error)
            "Android denied that device action."
        }
    }

    private fun persistPersonas(personas: List<Persona>) {
        val array = JSONArray()
        personas.forEach { persona ->
            array.put(JSONObject().apply {
                put("id", persona.id)
                put("displayName", persona.displayName)
                put("wakeWords", JSONArray(persona.wakeWords))
                put("systemPrompt", persona.systemPrompt)
                put("pitch", persona.pitch.toDouble())
                put("speechRate", persona.speechRate.toDouble())
                put("isBuiltIn", persona.isBuiltIn)
            })
        }
        preferences.edit().putString(KEY_PERSONAS, array.toString()).apply()
    }

    private fun JSONObject.toPersona(): Persona? {
        val id = optString("id").takeIf(String::isNotBlank) ?: return null
        val name = optString("displayName").takeIf(String::isNotBlank) ?: return null
        val wakeWordsArray = optJSONArray("wakeWords")
        val wakeWords = wakeWordsArray?.let { words ->
            (0 until words.length()).map { words.optString(it) }.filter(String::isNotBlank)
        }.orEmpty()
        return Persona(
            id = id,
            displayName = name,
            wakeWords = (wakeWords + name).distinctBy { it.lowercase(Locale.ROOT) },
            systemPrompt = optString("systemPrompt", DEFAULT_CUSTOM_PROMPT),
            pitch = optDouble("pitch", 1.0).toFloat().coerceIn(0.5f, 2.0f),
            speechRate = optDouble("speechRate", 1.0).toFloat().coerceIn(0.5f, 2.0f),
            isBuiltIn = optBoolean("isBuiltIn", false)
        )
    }

    private fun normalize(value: String): String =
        value.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim()

    private fun builtInPersonas() = listOf(
        Persona(
            id = "jarvis",
            displayName = "Jarvis",
            wakeWords = listOf("Jarvis", "Hey Jarvis"),
            systemPrompt = "Speak with the polished, polite confidence of a British household butler.",
            pitch = 0.92f,
            speechRate = 0.96f,
            isBuiltIn = true
        ),
        Persona(
            id = "friday",
            displayName = "Friday",
            wakeWords = listOf("Friday", "Hey Friday"),
            systemPrompt = "Speak as a sharp, tactical AI: decisive, observant, and concise.",
            pitch = 1.08f,
            speechRate = 1.12f,
            isBuiltIn = true
        ),
        Persona(
            id = "nova",
            displayName = "Nova",
            wakeWords = listOf("Nova", "Hey Nova"),
            systemPrompt = "Speak with energetic, playful cyberpunk flair and inventive optimism.",
            pitch = 1.2f,
            speechRate = 1.18f,
            isBuiltIn = true
        ),
        Persona(
            id = "ghost",
            displayName = "Ghost",
            wakeWords = listOf("Ghost", "Hey Ghost"),
            systemPrompt = "Speak in concise military-tactical language. Prioritize direct status and action.",
            pitch = 0.82f,
            speechRate = 0.9f,
            isBuiltIn = true
        )
    )

    companion object {
        private const val TAG = "AssistantUpgrade"
        private const val PREFERENCES_NAME = "assistant_upgrade"
        private const val KEY_PERSONAS = "personas"
        private const val KEY_ACTIVE_ID = "active_persona_id"
        private const val DEFAULT_ID = "jarvis"
        private const val DEFAULT_CUSTOM_PROMPT = "Be helpful, clear, and adapt your tone to the user's needs."
        private val LEGACY_WAKE_WORDS = listOf("OpenDroid", "Open Droid")

        private val SWITCH_COMMAND = Regex("^switch to\\s+(.+?)\\s*[.!?]?$", RegexOption.IGNORE_CASE)
        private val LIST_COMMAND = Regex("^(list|show)\\s+(characters|personas)(?:\\s+please)?[.!?]?$", RegexOption.IGNORE_CASE)
        private val RENAME_COMMAND = Regex(
            "^(?:change your name to|call yourself)\\s+(.+?)\\s*[.!?]?$",
            RegexOption.IGNORE_CASE
        )
        private val FLASHLIGHT_ON = Regex(
            "^(?:(?:turn|switch) on (?:the )?(?:flashlight|torch)|(?:turn|switch) (?:the )?(?:flashlight|torch) on|(?:the )?(?:flashlight|torch) on)$"
        )
        private val FLASHLIGHT_OFF = Regex(
            "^(?:(?:turn|switch) off (?:the )?(?:flashlight|torch)|(?:turn|switch) (?:the )?(?:flashlight|torch) off|(?:the )?(?:flashlight|torch) off)$"
        )
        private val ALARM_COMMAND = Regex("^set (?:an? )?alarm for (\\d{1,2}):(\\d{2})$")
        private val TIMER_COMMAND = Regex("^set (?:a )?timer(?: for)? (\\d+) (seconds?|minutes?|hours?)$")
        private val GOOGLE_COMMAND = Regex("^(?:google|search for)\\s+(.+)$", RegexOption.IGNORE_CASE)
        private val OPEN_APP_COMMAND = Regex("^open\\s+(.+)$", RegexOption.IGNORE_CASE)
    }
}
