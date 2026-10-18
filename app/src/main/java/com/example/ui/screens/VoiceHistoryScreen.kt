package com.example.ui.screens

import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.ui.EdenViewModel
import com.example.ui.components.CosmicBackground
import com.example.ui.components.MarkdownText
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.CosmicMuted
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.RadiantAqua
import com.example.ui.theme.StarlightWhite
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.SurfaceGlassBorder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceHistoryScreen(
    viewModel: EdenViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val messages by viewModel.chatMessages.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    fun performHaptic() {
        if (userPreferences.hapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    CosmicBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Voice Logs & History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = StarlightWhite
                        )
                        Text(
                            text = "Transcripts of voice conversations",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicMuted
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            performHaptic()
                            if (messages.isNotEmpty()) {
                                val transcript = messages.joinToString("\n\n") { msg ->
                                    val sender = if (msg.role == "user") "User (Voice)" else "E-DEN (Voice)"
                                    "[$sender]: ${msg.content}"
                                }
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "E-DEN Voice Transcripts")
                                    putExtra(Intent.EXTRA_TEXT, transcript)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share voice transcripts"))
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("No voice logs to share.") }
                            }
                        },
                        modifier = Modifier.testTag("share_voice_logs_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = CosmicMuted)
                    }

                    IconButton(
                        onClick = {
                            performHaptic()
                            showClearDialog = true
                        },
                        modifier = Modifier.testTag("clear_voice_logs_button")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear logs", tint = CosmicMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy.copy(alpha = 0.85f),
                    titleContentColor = StarlightWhite
                )
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceGlass)
                                    .border(1.dp, SurfaceGlassBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = RadiantAqua,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Voice Logs Yet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = StarlightWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Turn on the Mic and speak with E-DEN to see voice transcripts here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CosmicMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            VoiceLogItem(
                                message = message,
                                onCopy = {
                                    performHaptic()
                                    clipboardManager.setText(AnnotatedString(message.content))
                                    scope.launch { snackbarHostState.showSnackbar("Transcript copied") }
                                },
                                onReplayVoice = {
                                    performHaptic()
                                    viewModel.voiceManager.speak(
                                        text = message.content,
                                        enabled = true,
                                        pitch = userPreferences.voiceSettings.pitch,
                                        speechRate = userPreferences.voiceSettings.speechRate
                                    )
                                }
                            )
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text(text = "Clear Voice History?", color = StarlightWhite) },
                text = { Text(text = "This will remove all saved spoken voice transcripts from your device.", color = CosmicMuted) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearChat()
                            showClearDialog = false
                            scope.launch { snackbarHostState.showSnackbar("Voice history cleared") }
                        }
                    ) {
                        Text("Clear", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel", color = RadiantAqua)
                    }
                },
                containerColor = SurfaceDark,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun VoiceLogItem(
    message: ChatMessage,
    onCopy: () -> Unit,
    onReplayVoice: () -> Unit
) {
    val isUser = message.role == "user"
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isUser) ElectricIndigo.copy(alpha = 0.4f) else SurfaceGlassBorder.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isUser) DeepNavy.copy(alpha = 0.90f) else SurfaceDark.copy(alpha = 0.95f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUser) Icons.Default.Mic else Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = if (isUser) RadiantAqua else CelestialCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isUser) "You (Voice)" else "E-DEN (Deep Voice)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) RadiantAqua else CelestialCyan
                    )
                }
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = CosmicMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isUser) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = StarlightWhite
                )
            } else {
                MarkdownText(
                    content = message.content,
                    textColor = if (message.isError) Color(0xFFFCA5A5) else Color(0xFFE2E8F0)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isUser && message.content.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .clickable { onReplayVoice() }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Replay",
                            tint = RadiantAqua,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Replay Voice", style = MaterialTheme.typography.labelSmall, color = RadiantAqua)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Row(
                    modifier = Modifier
                        .clickable { onCopy() }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = CosmicMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", style = MaterialTheme.typography.labelSmall, color = CosmicMuted)
                }
            }
        }
    }
}
