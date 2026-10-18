package com.example.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.preferences.OrbSettings
import com.example.data.preferences.SettingsRepository
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.VoiceSettings
import com.example.data.remote.GeminiApiService
import com.example.data.repository.ChatRepository
import com.example.model.ChatMessage
import com.example.ui.orb.OrbState
import com.example.ui.orb.PlanetTheme
import com.example.util.VoiceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class KeyTestState {
    object Idle : KeyTestState()
    object Testing : KeyTestState()
    data class Success(val latencyMs: Long) : KeyTestState()
    data class Error(val message: String) : KeyTestState()
}

class EdenViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val apiService = GeminiApiService()
    private val settingsRepository = SettingsRepository(application)
    private val chatRepository = ChatRepository(database.chatDao(), apiService, settingsRepository)

    val currentSessionId = "default"

    val userPreferences: StateFlow<UserPreferences> = settingsRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val chatMessages: StateFlow<List<ChatMessage>> = chatRepository.getMessagesForSession(currentSessionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _orbState = MutableStateFlow(OrbState.Idle)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _keyTestState = MutableStateFlow<KeyTestState>(KeyTestState.Idle)
    val keyTestState: StateFlow<KeyTestState> = _keyTestState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Real-time Spoken Voice Subtitles
    private val _lastSpokenUserText = MutableStateFlow("")
    val lastSpokenUserText: StateFlow<String> = _lastSpokenUserText.asStateFlow()

    private val _lastSpokenAiText = MutableStateFlow("")
    val lastSpokenAiText: StateFlow<String> = _lastSpokenAiText.asStateFlow()

    // Hands-free continuous loop option
    private val _continuousMode = MutableStateFlow(false)
    val continuousMode: StateFlow<Boolean> = _continuousMode.asStateFlow()

    val voiceManager = VoiceManager(application) { isSpeaking ->
        if (isSpeaking) {
            _orbState.value = OrbState.Speaking
        } else if (_orbState.value == OrbState.Speaking) {
            _orbState.value = OrbState.Idle
            // In continuous conversation mode, automatically reopen mic after speech concludes
            if (_continuousMode.value) {
                viewModelScope.launch {
                    delay(400)
                    startListening()
                }
            }
        }
    }

    val isMicOn: StateFlow<Boolean> = voiceManager.isListening
    val isSpeaking: StateFlow<Boolean> = voiceManager.isSpeaking
    val liveRecognizedText: StateFlow<String> = voiceManager.recognizedText
    val audioRms: StateFlow<Float> = voiceManager.audioRms

    init {
        viewModelScope.launch {
            voiceManager.isListening.collect { listening ->
                if (listening) {
                    _orbState.value = OrbState.Listening
                } else if (_orbState.value == OrbState.Listening) {
                    _orbState.value = OrbState.Idle
                }
            }
        }
    }

    // API Key Missing Dialog state
    private val _showMissingApiKeyDialog = MutableStateFlow(false)
    val showMissingApiKeyDialog: StateFlow<Boolean> = _showMissingApiKeyDialog.asStateFlow()

    fun dismissMissingApiKeyDialog() {
        _showMissingApiKeyDialog.value = false
    }

    fun openMissingApiKeyDialog() {
        _showMissingApiKeyDialog.value = true
    }

    fun toggleMic() {
        if (userPreferences.value.effectiveApiKey.isBlank()) {
            _showMissingApiKeyDialog.value = true
            _errorMessage.value = "एपीआई की नहीं डाला गया है! कृपया सेटिंग्स में जाएं।"
            return
        }
        if (isMicOn.value) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun toggleMute() {
        val currentTts = userPreferences.value.ttsEnabled
        setTtsEnabled(!currentTts)
        if (currentTts) {
            voiceManager.stopSpeaking()
            _orbState.value = OrbState.Idle
        }
    }

    fun toggleContinuousMode() {
        if (userPreferences.value.effectiveApiKey.isBlank()) {
            _showMissingApiKeyDialog.value = true
            return
        }
        _continuousMode.value = !_continuousMode.value
    }

    fun processVoicePrompt(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) return

        if (userPreferences.value.effectiveApiKey.isBlank()) {
            _showMissingApiKeyDialog.value = true
            _errorMessage.value = "एपीआई की नहीं डाला गया है! कृपया सेटिंग्स में जाएं।"
            return
        }

        _lastSpokenUserText.value = trimmed
        _lastSpokenAiText.value = ""

        voiceManager.stopSpeaking()
        voiceManager.stopListening()

        _orbState.value = OrbState.Thinking
        _errorMessage.value = null

        viewModelScope.launch {
            val result = chatRepository.sendMessage(
                sessionId = currentSessionId,
                userPrompt = trimmed
            ) { chunkText ->
                _lastSpokenAiText.value = chunkText
            }

            result.onSuccess { output ->
                _lastSpokenAiText.value = output
                val prefs = userPreferences.value
                if (prefs.ttsEnabled && output.isNotBlank()) {
                    _orbState.value = OrbState.Speaking
                    voiceManager.speak(
                        text = output,
                        enabled = true,
                        pitch = prefs.voiceSettings.pitch,
                        speechRate = prefs.voiceSettings.speechRate
                    )
                } else {
                    _orbState.value = OrbState.Idle
                }
            }.onFailure { err ->
                _orbState.value = OrbState.Idle
                _lastSpokenAiText.value = "एरर: ${err.message}"
                _errorMessage.value = err.message ?: "Failed to generate response"
            }
        }
    }

    fun startListening() {
        if (userPreferences.value.effectiveApiKey.isBlank()) {
            _showMissingApiKeyDialog.value = true
            _errorMessage.value = "एपीआई की नहीं डाला गया है! कृपया सेटिंग्स में जाएं।"
            return
        }
        voiceManager.stopSpeaking()
        voiceManager.startListening { recognizedText ->
            if (recognizedText.isNotBlank()) {
                processVoicePrompt(recognizedText)
            }
        }
    }

    fun stopListening() {
        voiceManager.stopListening()
        _orbState.value = OrbState.Idle
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
        _orbState.value = OrbState.Idle
    }

    fun previewVoice(sampleText: String? = null) {
        val prefs = userPreferences.value
        val textToSpeak = sampleText ?: "नमस्ते, मैं ई-डन हूँ। आपकी डीप न्यूट्रल जेमिनी प्रो वॉइस असिस्टेंट।"
        voiceManager.speak(
            text = textToSpeak,
            enabled = true,
            pitch = prefs.voiceSettings.pitch,
            speechRate = prefs.voiceSettings.speechRate
        )
    }

    fun updateVoiceSettings(pitch: Float, rate: Float, persona: String) {
        viewModelScope.launch {
            settingsRepository.updateVoiceSettings(pitch, rate, persona)
        }
    }

    fun testApiKey(apiKey: String, model: String) {
        viewModelScope.launch {
            _keyTestState.value = KeyTestState.Testing
            val result = apiService.testApiKey(apiKey, model)
            result.onSuccess { latency ->
                _keyTestState.value = KeyTestState.Success(latency)
            }.onFailure { err ->
                _keyTestState.value = KeyTestState.Error(err.message ?: "Test failed")
            }
        }
    }

    fun resetKeyTestState() {
        _keyTestState.value = KeyTestState.Idle
    }

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            settingsRepository.saveApiKey(apiKey)
            _keyTestState.value = KeyTestState.Idle
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            settingsRepository.clearApiKey()
            _keyTestState.value = KeyTestState.Idle
        }
    }

    fun selectModel(model: String) {
        viewModelScope.launch {
            settingsRepository.setModel(model)
        }
    }

    fun setTtsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTtsEnabled(enabled)
            if (!enabled) {
                voiceManager.stopSpeaking()
            }
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticsEnabled(enabled)
        }
    }

    fun applyPlanetPreset(preset: PlanetTheme) {
        viewModelScope.launch {
            settingsRepository.applyPlanetPreset(preset)
        }
    }

    fun updateOrbColors(primary: Color, secondary: Color, glow: Color) {
        viewModelScope.launch {
            settingsRepository.updateOrbColors(primary, secondary, glow)
        }
    }

    fun updateOrbSliders(size: Float, glowIntensity: Float, speed: Float, showRings: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateOrbSliders(size, glowIntensity, speed, showRings)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            voiceManager.stopSpeaking()
            chatRepository.clearSession(currentSessionId)
            _lastSpokenUserText.value = ""
            _lastSpokenAiText.value = ""
            _orbState.value = OrbState.Idle
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
