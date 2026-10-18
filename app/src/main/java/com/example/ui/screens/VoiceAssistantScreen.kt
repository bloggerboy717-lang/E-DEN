package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.EdenViewModel
import com.example.ui.components.CosmicBackground
import com.example.ui.orb.CosmicOrbView
import com.example.ui.orb.OrbState
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.CosmicMuted
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.RadiantAqua
import com.example.ui.theme.StarlightWhite
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.SurfaceGlassBorder
import kotlinx.coroutines.launch

@Composable
fun VoiceAssistantScreen(
    viewModel: EdenViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val userPreferences by viewModel.userPreferences.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()
    val isMicOn by viewModel.isMicOn.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val liveRecognizedText by viewModel.liveRecognizedText.collectAsState()
    val lastSpokenUserText by viewModel.lastSpokenUserText.collectAsState()
    val lastSpokenAiText by viewModel.lastSpokenAiText.collectAsState()
    val continuousMode by viewModel.continuousMode.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showMissingApiKeyDialog by viewModel.showMissingApiKeyDialog.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Microphone permission is required for voice assistant.")
            }
        }
    }

    fun performHaptic() {
        if (userPreferences.hapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    val voiceSuggestions = listOf(
        "नमस्ते, आप क्या कर सकती हैं?",
        "What are black holes?",
        "आज का विचार क्या है?",
        "Tell me a cosmic science fact",
        "जीवन का अर्थ क्या है?"
    )

    CosmicBackground(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "E-DEN",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = StarlightWhite
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceGlass,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGlassBorder.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "GEMINI PRO VOICE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RadiantAqua,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Deep Neutral Female Voice AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicMuted
                        )
                    }

                    // Continuous Hands-Free Mode Toggle Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (continuousMode) ElectricIndigo.copy(alpha = 0.35f) else SurfaceGlass,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (continuousMode) RadiantAqua else SurfaceGlassBorder
                        ),
                        modifier = Modifier
                            .clickable {
                                performHaptic()
                                if (userPreferences.effectiveApiKey.isBlank()) {
                                    viewModel.openMissingApiKeyDialog()
                                } else {
                                    viewModel.toggleContinuousMode()
                                }
                            }
                            .testTag("continuous_mode_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = null,
                                tint = if (continuousMode) RadiantAqua else CosmicMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (continuousMode) "Auto Loop: ON" else "Auto Loop: OFF",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (continuousMode) StarlightWhite else CosmicMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // API Key missing prompt banner
                AnimatedVisibility(
                    visible = userPreferences.effectiveApiKey.isBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .clickable {
                                performHaptic()
                                viewModel.openMissingApiKeyDialog()
                            }
                            .testTag("api_key_banner"),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.90f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("एपीआई की नहीं डाला गया है!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5))
                                Text("टैप करें और सेटिंग्स में जाकर अपना Gemini API Key दर्ज करें।", style = MaterialTheme.typography.bodyMedium, color = CosmicMuted)
                            }
                        }
                    }
                }

                // 3D Glowing Planet Orb Centerpiece
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .testTag("planet_orb_view"),
                    contentAlignment = Alignment.Center
                ) {
                    CosmicOrbView(
                        state = orbState,
                        settings = userPreferences.orbSettings,
                        size = 270.dp,
                        audioRms = audioRms
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Orb Reactive Voice Status Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceGlass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val dotColor = when (orbState) {
                            OrbState.Listening -> Color(0xFF10B981)
                            OrbState.Thinking -> NebulaViolet
                            OrbState.Speaking -> ElectricIndigo
                            OrbState.Idle -> if (userPreferences.effectiveApiKey.isBlank()) Color(0xFFEF4444) else RadiantAqua
                        }
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (orbState) {
                                OrbState.Listening -> "MIC ON • LISTENING TO YOUR VOICE..."
                                OrbState.Thinking -> "THINKING • PROCESSING WITH GEMINI PRO..."
                                OrbState.Speaking -> "SPEAKING • DEEP NEUTRAL VOICE..."
                                OrbState.Idle -> if (userPreferences.effectiveApiKey.isBlank()) "KEY MISSING • API KEY नहीं डाला गया है" else if (isMicOn) "MIC ACTIVE" else "MIC OFF • TAP MIC TO TALK"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (userPreferences.effectiveApiKey.isBlank() && orbState == OrbState.Idle) Color(0xFFFCA5A5) else StarlightWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Spoken Subtitle & Dialogue Display Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, SurfaceGlassBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.85f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val currentPrompt = if (isMicOn && liveRecognizedText.isNotBlank()) {
                            liveRecognizedText
                        } else if (lastSpokenUserText.isNotBlank()) {
                            lastSpokenUserText
                        } else {
                            ""
                        }

                        if (currentPrompt.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "YOU: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RadiantAqua
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentPrompt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StarlightWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (lastSpokenAiText.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "E-DEN: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CelestialCyan
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = lastSpokenAiText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (lastSpokenAiText.startsWith("एरर") || lastSpokenAiText.startsWith("Error")) Color(0xFFFCA5A5) else Color(0xFFE2E8F0)
                                )
                            }
                        } else if (currentPrompt.isBlank()) {
                            Text(
                                text = if (userPreferences.effectiveApiKey.isBlank())
                                    "चेतावनी: एपीआई की नहीं डाला गया है। पहले सेटिंग्स में जाकर एपीआई की दर्ज करें।"
                                else
                                    "माइक ऑन करें और पूछें — E-DEN डीप न्यूट्रल फीमेल वॉइस में तुरंत उत्तर देगी।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (userPreferences.effectiveApiKey.isBlank()) Color(0xFFFCA5A5) else CosmicMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Spoken Suggestion Chips
                Text(
                    text = "SPEAK OR TAP TO ASK",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp,
                    color = RadiantAqua,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    voiceSuggestions.forEach { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceGlass,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGlassBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable {
                                performHaptic()
                                if (userPreferences.effectiveApiKey.isBlank()) {
                                    viewModel.openMissingApiKeyDialog()
                                } else {
                                    viewModel.processVoicePrompt(suggestion)
                                }
                            }
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = RadiantAqua,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Main Voice Controls Deck: [Mute/Unmute] [Big Mic On/Off] [Stop Speaking]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // MUTE / UNMUTE BUTTON (म्यूट / अनम्यूट)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        IconButton(
                            onClick = {
                                performHaptic()
                                viewModel.toggleMute()
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (userPreferences.ttsEnabled) "Voice Output Unmuted (आवाज चालू)" else "Voice Output Muted (आवाज म्यूट)"
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    if (userPreferences.ttsEnabled) SurfaceGlass else Color(0xFFEF4444).copy(alpha = 0.25f)
                                )
                                .border(
                                    1.dp,
                                    if (userPreferences.ttsEnabled) SurfaceGlassBorder else Color(0xFFEF4444),
                                    CircleShape
                                )
                                .testTag("mute_unmute_button")
                        ) {
                            Icon(
                                imageVector = if (userPreferences.ttsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Mute/Unmute",
                                tint = if (userPreferences.ttsEnabled) RadiantAqua else Color(0xFFEF4444),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (userPreferences.ttsEnabled) "Unmuted" else "Muted",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (userPreferences.ttsEnabled) RadiantAqua else Color(0xFFEF4444)
                        )
                    }

                    // PRIMARY BIG GLOWING MIC BUTTON (माइक ऑन / माइक ऑफ)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .clip(CircleShape)
                                .background(
                                    if (userPreferences.effectiveApiKey.isBlank()) Color(0xFF7F1D1D).copy(alpha = 0.8f)
                                    else if (isMicOn) Color(0xFF10B981)
                                    else ElectricIndigo
                                )
                                .border(
                                    3.dp,
                                    if (userPreferences.effectiveApiKey.isBlank()) Color(0xFFEF4444)
                                    else if (isMicOn) Color(0xFF6EE7B7)
                                    else RadiantAqua,
                                    CircleShape
                                )
                                .clickable {
                                    performHaptic()
                                    if (userPreferences.effectiveApiKey.isBlank()) {
                                        viewModel.openMissingApiKeyDialog()
                                        return@clickable
                                    }

                                    if (isMicOn) {
                                        viewModel.stopListening()
                                    } else {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (hasPermission) {
                                            viewModel.startListening()
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                                .testTag("primary_mic_toggle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = if (isMicOn) "Mic On" else "Mic Off",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (userPreferences.effectiveApiKey.isBlank()) "KEY MISSING"
                            else if (isMicOn) "MIC ON (बोलिए)"
                            else "MIC OFF (माइक चालू करें)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (userPreferences.effectiveApiKey.isBlank()) Color(0xFFFCA5A5)
                            else if (isMicOn) Color(0xFF34D399)
                            else StarlightWhite
                        )
                    }

                    // STOP SPEAKING / INTERRUPT BUTTON (रोकें)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        IconButton(
                            onClick = {
                                performHaptic()
                                viewModel.stopSpeaking()
                            },
                            enabled = isSpeaking,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSpeaking) Color(0xFFDC2626).copy(alpha = 0.45f) else SurfaceGlass.copy(alpha = 0.5f)
                                )
                                .border(
                                    1.dp,
                                    if (isSpeaking) Color(0xFFEF4444) else SurfaceGlassBorder.copy(alpha = 0.3f),
                                    CircleShape
                                )
                                .testTag("stop_speaking_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop speaking",
                                tint = if (isSpeaking) Color(0xFFF87171) else CosmicMuted,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isSpeaking) "Stop Speech" else "Idle",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSpeaking) Color(0xFFF87171) else CosmicMuted
                        )
                    }
                }
            }

            // POPUP: एपीआई की नहीं डालने पर दिखने वाला पॉपअप (Missing API Key Modal Dialog)
            if (showMissingApiKeyDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissMissingApiKeyDialog() },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7F1D1D).copy(alpha = 0.5f))
                                .border(1.5.dp, Color(0xFFEF4444), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Missing Key",
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "एपीआई की नहीं डाला गया है!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = StarlightWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "आपने अभी तक अपना Google Gemini API Key दर्ज नहीं किया है। वॉइस असिस्टेंट केवल तभी कार्य करेगा जब आप अपनी वैध एपीआई की दर्ज करेंगे।",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFE2E8F0),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF450A0A),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "एरर: API Key अनुपलब्ध है (No API Key Provided)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFCA5A5),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                performHaptic()
                                viewModel.dismissMissingApiKeyDialog()
                                onNavigateToSettings()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                            modifier = Modifier.testTag("popup_go_to_settings_button")
                        ) {
                            Text("सेटिंग्स में जाएं (Settings)", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                performHaptic()
                                viewModel.dismissMissingApiKeyDialog()
                            },
                            modifier = Modifier.testTag("popup_dismiss_button")
                        ) {
                            Text("रद्द करें (Cancel)", color = CosmicMuted)
                        }
                    },
                    containerColor = SurfaceDark,
                    shape = RoundedCornerShape(24.dp)
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )
        }
    }
}
