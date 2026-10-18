package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onSpeakingStateChanged: (Boolean) -> Unit = {}
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                applyOptimalFemaleVoice(Locale.getDefault())

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        onSpeakingStateChanged(true)
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        onSpeakingStateChanged(false)
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        onSpeakingStateChanged(false)
                    }
                })
            }
        }
    }

    /**
     * Finds and applies the deepest, smoothest neutral female neural voice available
     * in the system TTS engine for the target language.
     */
    private fun applyOptimalFemaleVoice(targetLocale: Locale) {
        val tts = textToSpeech ?: return
        try {
            tts.language = targetLocale
            val voices = tts.voices
            if (!voices.isNullOrEmpty()) {
                // Filter for female voices matching target locale
                val matchingFemaleVoice = voices.firstOrNull { voice ->
                    val isLangMatch = voice.locale.language == targetLocale.language
                    val isFemale = voice.name.contains("female", ignoreCase = true) ||
                            voice.name.contains("sfg", ignoreCase = true) || // Google EN female
                            voice.name.contains("iob", ignoreCase = true) ||
                            voice.name.contains("hie", ignoreCase = true) || // Google HI female
                            voice.name.contains("hid", ignoreCase = true) ||
                            voice.name.contains("cfl", ignoreCase = true)
                    isLangMatch && isFemale && !voice.isNetworkConnectionRequired
                } ?: voices.firstOrNull { voice ->
                    val isLangMatch = voice.locale.language == targetLocale.language
                    val isFemale = voice.name.contains("female", ignoreCase = true) ||
                            voice.name.contains("sfg", ignoreCase = true) ||
                            voice.name.contains("hie", ignoreCase = true)
                    isLangMatch && isFemale
                } ?: voices.firstOrNull { voice ->
                    voice.locale.language == targetLocale.language
                }

                matchingFemaleVoice?.let { voice ->
                    tts.voice = voice
                }
            }
        } catch (_: Exception) {
            // Fallback gracefully to default engine settings
        }
    }

    fun startListening(onResult: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _speechError.value = "Speech recognition is not available on this device."
            return
        }

        stopSpeaking()
        stopListening()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-US", "en-IN"))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _speechError.value = null
                    _recognizedText.value = ""
                }

                override fun onBeginningOfSpeech() {
                    _isListening.value = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    _audioRms.value = (rmsdB / 10f).coerceIn(0f, 1f)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission required"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                        else -> "Speech recognition error ($error)"
                    }
                    _speechError.value = errorMsg
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        _recognizedText.value = text
                        onResult(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        _recognizedText.value = text
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            startListening(intent)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        _isListening.value = false
        _audioRms.value = 0f
    }

    fun speak(
        text: String,
        enabled: Boolean,
        pitch: Float = 0.80f,
        speechRate: Float = 0.94f
    ) {
        if (!enabled || !isTtsInitialized) return
        stopListening()

        // Clean markdown characters for spoken reading
        val cleanedText = text
            .replace(Regex("```[\\s\\S]*?```"), "Code omitted.")
            .replace(Regex("[*#_`~>]"), "")
            .trim()

        if (cleanedText.isBlank()) return

        val tts = textToSpeech ?: return

        // Detect if text contains Hindi / Devanagari script
        val isHindi = cleanedText.any { it in '\u0900'..'\u097F' }
        val targetLocale = if (isHindi) Locale("hi", "IN") else Locale.getDefault()

        applyOptimalFemaleVoice(targetLocale)

        // Set deep resonant pitch (0.75f - 0.82f) and calm neutral cadence (0.94f)
        tts.setPitch(pitch)
        tts.setSpeechRate(speechRate)

        tts.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, "EDEN_TTS_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        if (isTtsInitialized) {
            textToSpeech?.stop()
            _isSpeaking.value = false
            onSpeakingStateChanged(false)
        }
    }

    fun destroy() {
        stopListening()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
