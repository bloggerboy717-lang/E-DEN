package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.BuildConfig
import com.example.ui.orb.PlanetPresets
import com.example.ui.orb.PlanetTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "eden_settings")

data class OrbSettings(
    val presetName: String = "Saturn",
    val primaryColor: Color = PlanetPresets.Saturn.primaryColor,
    val secondaryColor: Color = PlanetPresets.Saturn.secondaryColor,
    val glowColor: Color = PlanetPresets.Saturn.glowColor,
    val sizeFactor: Float = 1.0f,
    val glowIntensity: Float = 1.0f,
    val rotationSpeed: Float = 1.0f,
    val showRings: Boolean = true
)

data class VoiceSettings(
    val persona: String = "deep_neutral_female",
    val pitch: Float = 0.80f, // Deep resonant female pitch
    val speechRate: Float = 0.94f // Measured, sophisticated cadence
)

data class UserPreferences(
    val customApiKey: String = "",
    val effectiveApiKey: String = "",
    val selectedModel: String = "gemini-2.5-flash",
    val ttsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val voiceSettings: VoiceSettings = VoiceSettings(),
    val orbSettings: OrbSettings = OrbSettings()
)

class SettingsRepository(private val context: Context) {

    private val KEY_API_KEY = stringPreferencesKey("gemini_api_key")
    private val KEY_MODEL = stringPreferencesKey("selected_model")
    private val KEY_TTS = booleanPreferencesKey("tts_enabled")
    private val KEY_HAPTICS = booleanPreferencesKey("haptics_enabled")

    // Voice customisation keys
    private val KEY_VOICE_PERSONA = stringPreferencesKey("voice_persona")
    private val KEY_VOICE_PITCH = floatPreferencesKey("voice_pitch")
    private val KEY_VOICE_RATE = floatPreferencesKey("voice_rate")

    // Orb customization keys
    private val KEY_ORB_PRESET = stringPreferencesKey("orb_preset")
    private val KEY_ORB_PRIMARY_COLOR = longPreferencesKey("orb_primary_color")
    private val KEY_ORB_SECONDARY_COLOR = longPreferencesKey("orb_secondary_color")
    private val KEY_ORB_GLOW_COLOR = longPreferencesKey("orb_glow_color")
    private val KEY_ORB_SIZE = floatPreferencesKey("orb_size")
    private val KEY_ORB_GLOW_INTENSITY = floatPreferencesKey("orb_glow_intensity")
    private val KEY_ORB_ROTATION_SPEED = floatPreferencesKey("orb_rotation_speed")
    private val KEY_ORB_SHOW_RINGS = booleanPreferencesKey("orb_show_rings")

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val savedKey = prefs[KEY_API_KEY] ?: ""
        val effectiveKey = savedKey.trim()

        val presetName = prefs[KEY_ORB_PRESET] ?: "Saturn"
        val defaultPreset = PlanetPresets.getByName(presetName)

        val primaryLong = prefs[KEY_ORB_PRIMARY_COLOR]
        val secondaryLong = prefs[KEY_ORB_SECONDARY_COLOR]
        val glowLong = prefs[KEY_ORB_GLOW_COLOR]

        val primaryColor = if (primaryLong != null) Color(primaryLong.toULong()) else defaultPreset.primaryColor
        val secondaryColor = if (secondaryLong != null) Color(secondaryLong.toULong()) else defaultPreset.secondaryColor
        val glowColor = if (glowLong != null) Color(glowLong.toULong()) else defaultPreset.glowColor

        val size = prefs[KEY_ORB_SIZE] ?: 1.0f
        val glowIntensity = prefs[KEY_ORB_GLOW_INTENSITY] ?: 1.0f
        val rotationSpeed = prefs[KEY_ORB_ROTATION_SPEED] ?: 1.0f
        val showRings = prefs[KEY_ORB_SHOW_RINGS] ?: defaultPreset.hasRings

        val voicePersona = prefs[KEY_VOICE_PERSONA] ?: "deep_neutral_female"
        val voicePitch = prefs[KEY_VOICE_PITCH] ?: 0.80f
        val voiceRate = prefs[KEY_VOICE_RATE] ?: 0.94f

        UserPreferences(
            customApiKey = savedKey,
            effectiveApiKey = effectiveKey,
            selectedModel = prefs[KEY_MODEL] ?: "gemini-2.5-flash",
            ttsEnabled = prefs[KEY_TTS] ?: true,
            hapticsEnabled = prefs[KEY_HAPTICS] ?: true,
            voiceSettings = VoiceSettings(
                persona = voicePersona,
                pitch = voicePitch,
                speechRate = voiceRate
            ),
            orbSettings = OrbSettings(
                presetName = presetName,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                glowColor = glowColor,
                sizeFactor = size,
                glowIntensity = glowIntensity,
                rotationSpeed = rotationSpeed,
                showRings = showRings
            )
        )
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_API_KEY] = apiKey.trim()
        }
    }

    suspend fun clearApiKey() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_API_KEY)
        }
    }

    suspend fun setModel(model: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MODEL] = model
        }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TTS] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAPTICS] = enabled
        }
    }

    suspend fun updateVoiceSettings(pitch: Float, rate: Float, persona: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VOICE_PITCH] = pitch
            prefs[KEY_VOICE_RATE] = rate
            prefs[KEY_VOICE_PERSONA] = persona
        }
    }

    suspend fun applyPlanetPreset(preset: PlanetTheme) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ORB_PRESET] = preset.name
            prefs[KEY_ORB_PRIMARY_COLOR] = preset.primaryColor.value.toLong()
            prefs[KEY_ORB_SECONDARY_COLOR] = preset.secondaryColor.value.toLong()
            prefs[KEY_ORB_GLOW_COLOR] = preset.glowColor.value.toLong()
            prefs[KEY_ORB_SHOW_RINGS] = preset.hasRings
        }
    }

    suspend fun updateOrbColors(primary: Color, secondary: Color, glow: Color) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ORB_PRIMARY_COLOR] = primary.value.toLong()
            prefs[KEY_ORB_SECONDARY_COLOR] = secondary.value.toLong()
            prefs[KEY_ORB_GLOW_COLOR] = glow.value.toLong()
        }
    }

    suspend fun updateOrbSliders(size: Float, glowIntensity: Float, rotationSpeed: Float, showRings: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ORB_SIZE] = size
            prefs[KEY_ORB_GLOW_INTENSITY] = glowIntensity
            prefs[KEY_ORB_ROTATION_SPEED] = rotationSpeed
            prefs[KEY_ORB_SHOW_RINGS] = showRings
        }
    }
}
