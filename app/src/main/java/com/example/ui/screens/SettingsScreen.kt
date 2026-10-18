package com.example.ui.screens

import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EdenViewModel
import com.example.ui.KeyTestState
import com.example.ui.components.CosmicBackground
import com.example.ui.orb.CosmicOrbView
import com.example.ui.orb.OrbState
import com.example.ui.orb.PlanetPresets
import com.example.ui.orb.PlanetTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: EdenViewModel,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val userPreferences by viewModel.userPreferences.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val keyTestState by viewModel.keyTestState.collectAsState()

    var keyInput by remember(userPreferences.customApiKey) {
        mutableStateOf(userPreferences.customApiKey)
    }
    var keyVisible by remember { mutableStateOf(false) }

    fun performHaptic() {
        if (userPreferences.hapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    val availableModels = listOf(
        "gemini-2.5-flash",
        "gemini-2.5-pro",
        "gemini-1.5-flash",
        "gemini-1.5-pro",
        "gemini-2.0-flash"
    )

    // Palette choices for color customization
    val primaryPalette = listOf(
        Color(0xFFE2B771), Color(0xFF2563EB), Color(0xFFDC2626), Color(0xFFD97706),
        Color(0xFF0284C7), Color(0xFF9333EA), Color(0xFFF59E0B), Color(0xFF64748B)
    )

    val secondaryPalette = listOf(
        Color(0xFF8C6239), Color(0xFF10B981), Color(0xFF9A3412), Color(0xFFFDE68A),
        Color(0xFF06B6D4), Color(0xFFEC4899), Color(0xFFEA580C), Color(0xFFCBD5E1)
    )

    val glowPalette = listOf(
        Color(0xFFFFD580), Color(0xFF60A5FA), Color(0xFFF87171), Color(0xFFFBBF24),
        Color(0xFF38BDF8), Color(0xFFC084FC), Color(0xFFFDE047), Color(0xFFE2E8F0)
    )

    CosmicBackground(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 28.dp, bottom = 120.dp)
            ) {
                // Section 1: Live Interactive Orb Preview
                Text(
                    text = "CELESTIAL PREVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    color = RadiantAqua,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, SurfaceGlassBorder, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.90f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CosmicOrbView(
                            state = OrbState.Idle,
                            settings = userPreferences.orbSettings,
                            size = 190.dp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${userPreferences.orbSettings.presetName} Planet Simulation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = StarlightWhite
                        )
                        Text(
                            text = "Live preview — adjustments apply in real time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CosmicMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: Gemini API Key & Diagnostics
                Text(
                    text = "GEMINI API CONFIGURATION",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    color = RadiantAqua,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, SurfaceGlassBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.85f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = RadiantAqua,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "API Key",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = StarlightWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = {
                                keyInput = it
                                viewModel.resetKeyTestState()
                            },
                            placeholder = { Text("Paste your Gemini API key...", color = CosmicMuted) },
                            visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { keyVisible = !keyVisible }) {
                                    Icon(
                                        imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle visibility",
                                        tint = CosmicMuted
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("api_key_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DeepNavy,
                                unfocusedContainerColor = DeepNavy,
                                focusedBorderColor = ElectricIndigo,
                                unfocusedBorderColor = SurfaceGlassBorder,
                                focusedTextColor = StarlightWhite,
                                unfocusedTextColor = StarlightWhite
                            ),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Key Action Buttons: Test, Save, Clear
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    performHaptic()
                                    viewModel.testApiKey(keyInput, userPreferences.selectedModel)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_key_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlass),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGlassBorder)
                            ) {
                                if (keyTestState is KeyTestState.Testing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = RadiantAqua,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text("Test Key", color = RadiantAqua)
                            }

                            Button(
                                onClick = {
                                    performHaptic()
                                    viewModel.saveApiKey(keyInput)
                                    scope.launch { snackbarHostState.showSnackbar("API key saved securely") }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_key_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Key")
                            }

                            if (userPreferences.customApiKey.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        performHaptic()
                                        viewModel.clearApiKey()
                                        keyInput = ""
                                        scope.launch { snackbarHostState.showSnackbar("API key deleted") }
                                    },
                                    modifier = Modifier.testTag("delete_key_button"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete key", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Test Status Result Box
                        AnimatedVisibility(
                            visible = keyTestState !is KeyTestState.Idle,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Spacer(modifier = Modifier.height(10.dp))
                            when (val state = keyTestState) {
                                is KeyTestState.Testing -> {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DeepNavy,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Testing connection to Google Gemini API...", style = MaterialTheme.typography.bodyMedium, color = CosmicMuted)
                                        }
                                    }
                                }

                                is KeyTestState.Success -> {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF064E3B).copy(alpha = 0.6f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Operational: Verified in ${state.latencyMs} ms",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFFD1FAE5),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                is KeyTestState.Error -> {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF7F1D1D).copy(alpha = 0.6f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = state.message,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFFFEE2E2)
                                            )
                                        }
                                    }
                                }

                                else -> Unit
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 3: Model Selector
                Text(
                    text = "AI MODEL ENGINE",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    color = RadiantAqua,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableModels.forEach { model ->
                        val isSelected = model == userPreferences.selectedModel
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                performHaptic()
                                viewModel.selectModel(model)
                            },
                            label = { Text(model) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricIndigo,
                                selectedLabelColor = Color.White,
                                containerColor = SurfaceDark,
                                labelColor = CosmicMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) RadiantAqua else SurfaceGlassBorder
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 4: Planet Presets (Earth, Mars, Jupiter, Saturn, Neptune, Nebula, Sun, Moon)
                Text(
                    text = "PLANET STYLE PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    color = RadiantAqua,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PlanetPresets.all.forEach { preset ->
                        val isSelected = preset.name.equals(userPreferences.orbSettings.presetName, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) ElectricIndigo.copy(alpha = 0.35f) else SurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) RadiantAqua else SurfaceGlassBorder.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .width(120.dp)
                                .clickable {
                                    performHaptic()
                                    viewModel.applyPlanetPreset(preset)
                                }
                                .testTag("planet_preset_${preset.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(preset.primaryColor)
                                        .border(2.dp, preset.glowColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StarlightWhite
                                )
                                Text(
                                    text = if (preset.hasRings) "With Rings" else "Smooth",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CosmicMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 5: Fine-Tuning Sliders & Ring Toggle
                Text(
                    text = "ORB DYNAMICS & SLIDERS",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    color = RadiantAqua,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, SurfaceGlassBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.85f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Planetary Rings Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Planetary Rings", style = MaterialTheme.typography.titleMedium, color = StarlightWhite, fontWeight = FontWeight.SemiBold)
                                Text("Render 3D orbital rings around the sphere", style = MaterialTheme.typography.bodyMedium, color = CosmicMuted)
                            }
                            Switch(
                                checked = userPreferences.orbSettings.showRings,
                                onCheckedChange = { showRings ->
                                    performHaptic()
                                    viewModel.updateOrbSliders(
                                        size = userPreferences.orbSettings.sizeFactor,
                                        glowIntensity = userPreferences.orbSettings.glowIntensity,
                                        speed = userPreferences.orbSettings.rotationSpeed,
                                        showRings = showRings
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StarlightWhite,
                                    checkedTrackColor = ElectricIndigo
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Orb Size Slider
                        Text("Orb Scale: ${(userPreferences.orbSettings.sizeFactor * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = StarlightWhite)
                        Slider(
                            value = userPreferences.orbSettings.sizeFactor,
                            onValueChange = { newSize ->
                                viewModel.updateOrbSliders(
                                    size = newSize,
                                    glowIntensity = userPreferences.orbSettings.glowIntensity,
                                    speed = userPreferences.orbSettings.rotationSpeed,
                                    showRings = userPreferences.orbSettings.showRings
                                )
                            },
                            valueRange = 0.7f..1.3f,
                            colors = SliderDefaults.colors(thumbColor = RadiantAqua, activeTrackColor = ElectricIndigo)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Glow Intensity Slider
                        Text("Glow Corona Intensity: ${(userPreferences.orbSettings.glowIntensity * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = StarlightWhite)
                        Slider(
                            value = userPreferences.orbSettings.glowIntensity,
                            onValueChange = { newGlow ->
                                viewModel.updateOrbSliders(
                                    size = userPreferences.orbSettings.sizeFactor,
                                    glowIntensity = newGlow,
                                    speed = userPreferences.orbSettings.rotationSpeed,
                                    showRings = userPreferences.orbSettings.showRings
                                )
                            },
                            valueRange = 0.2f..1.5f,
                            colors = SliderDefaults.colors(thumbColor = RadiantAqua, activeTrackColor = ElectricIndigo)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Rotation Speed Slider
                        Text("Rotation Speed: ${(userPreferences.orbSettings.rotationSpeed * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = StarlightWhite)
                        Slider(
                            value = userPreferences.orbSettings.rotationSpeed,
                            onValueChange = { newSpeed ->
                                viewModel.updateOrbSliders(
                                    size = userPreferences.orbSettings.sizeFactor,
                                    glowIntensity = userPreferences.orbSettings.glowIntensity,
                                    speed = newSpeed,
                                    showRings = userPreferences.orbSettings.showRings
                                )
                            },
                            valueRange = 0.2f..2.5f,
                            colors = SliderDefaults.colors(thumbColor = RadiantAqua, activeTrackColor = ElectricIndigo)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 6: Color Pickers
                Text(
                    text = "CUSTOM COLOR PALETTE",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    color = RadiantAqua,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, SurfaceGlassBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.85f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Primary Planet Color", style = MaterialTheme.typography.titleMedium, color = StarlightWhite)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            primaryPalette.forEach { color ->
                                val isChosen = color.toArgb() == userPreferences.orbSettings.primaryColor.toArgb()
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(2.dp, if (isChosen) StarlightWhite else Color.Transparent, CircleShape)
                                        .clickable {
                                            performHaptic()
                                            viewModel.updateOrbColors(
                                                primary = color,
                                                secondary = userPreferences.orbSettings.secondaryColor,
                                                glow = userPreferences.orbSettings.glowColor
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChosen) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = StarlightWhite, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Atmospheric Stripe Color", style = MaterialTheme.typography.titleMedium, color = StarlightWhite)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            secondaryPalette.forEach { color ->
                                val isChosen = color.toArgb() == userPreferences.orbSettings.secondaryColor.toArgb()
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(2.dp, if (isChosen) StarlightWhite else Color.Transparent, CircleShape)
                                        .clickable {
                                            performHaptic()
                                            viewModel.updateOrbColors(
                                                primary = userPreferences.orbSettings.primaryColor,
                                                secondary = color,
                                                glow = userPreferences.orbSettings.glowColor
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChosen) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = StarlightWhite, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Corona & Ring Glow Color", style = MaterialTheme.typography.titleMedium, color = StarlightWhite)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            glowPalette.forEach { color ->
                                val isChosen = color.toArgb() == userPreferences.orbSettings.glowColor.toArgb()
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(2.dp, if (isChosen) StarlightWhite else Color.Transparent, CircleShape)
                                        .clickable {
                                            performHaptic()
                                            viewModel.updateOrbColors(
                                                primary = userPreferences.orbSettings.primaryColor,
                                                secondary = userPreferences.orbSettings.secondaryColor,
                                                glow = color
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChosen) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = StarlightWhite, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 7: Voice Persona & Acoustics (डीप न्यूट्रल फीमेल वॉइस)
                Text(
                    text = "VOICE ACOUSTICS & PERSONA (जेमिनी प्रो वॉइस)",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    color = RadiantAqua,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, SurfaceGlassBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.85f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = RadiantAqua,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Pro Neutral Female Voice",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = StarlightWhite
                            )
                        }
                        Text(
                            text = "डीप न्यूट्रल वूमेन टोन — शांत, गंभीर, और संतुलित वॉइस",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CosmicMuted,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        // Voice Persona Presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val currentPersona = userPreferences.voiceSettings.persona
                            val presets = listOf(
                                Triple("deep_neutral_female", "Gemini Pro Deep (Recommended)", Pair(0.80f, 0.94f)),
                                Triple("ultra_deep_female", "Deep Resonant (गहरा)", Pair(0.72f, 0.90f)),
                                Triple("balanced_female", "Balanced Neutral (संतुलित)", Pair(0.88f, 0.98f)),
                                Triple("standard_female", "Standard (सामान्य)", Pair(1.0f, 1.0f))
                            )

                            presets.forEach { (id, label, params) ->
                                val isSelected = currentPersona == id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        performHaptic()
                                        viewModel.updateVoiceSettings(params.first, params.second, id)
                                    },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricIndigo,
                                        selectedLabelColor = Color.White,
                                        containerColor = DeepNavy,
                                        labelColor = CosmicMuted
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) RadiantAqua else SurfaceGlassBorder
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Voice Depth / Pitch Slider
                        val pitchVal = userPreferences.voiceSettings.pitch
                        val depthDesc = when {
                            pitchVal < 0.75f -> "Ultra Deep (अति गहरा)"
                            pitchVal <= 0.85f -> "Deep Neutral Pro (गहरा व शांत)"
                            else -> "Standard (सामान्य)"
                        }
                        Text(
                            text = "Voice Depth (गहराई / Pitch): ${(pitchVal * 100).toInt()}% • $depthDesc",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarlightWhite
                        )
                        Slider(
                            value = pitchVal,
                            onValueChange = { newPitch ->
                                viewModel.updateVoiceSettings(
                                    pitch = newPitch,
                                    rate = userPreferences.voiceSettings.speechRate,
                                    persona = "custom"
                                )
                            },
                            valueRange = 0.60f..1.15f,
                            colors = SliderDefaults.colors(thumbColor = RadiantAqua, activeTrackColor = ElectricIndigo)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Speech Pace / Rate Slider
                        val rateVal = userPreferences.voiceSettings.speechRate
                        val paceDesc = when {
                            rateVal < 0.85f -> "Deliberate & Slow"
                            rateVal <= 0.98f -> "Poised & Neutral (शांत)"
                            else -> "Energetic"
                        }
                        Text(
                            text = "Speech Pace (गति / Rate): ${(rateVal * 100).toInt()}% • $paceDesc",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarlightWhite
                        )
                        Slider(
                            value = rateVal,
                            onValueChange = { newRate ->
                                viewModel.updateVoiceSettings(
                                    pitch = userPreferences.voiceSettings.pitch,
                                    rate = newRate,
                                    persona = "custom"
                                )
                            },
                            valueRange = 0.70f..1.30f,
                            colors = SliderDefaults.colors(thumbColor = RadiantAqua, activeTrackColor = ElectricIndigo)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Voice Preview Action Buttons (Hindi & English)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    performHaptic()
                                    viewModel.previewVoice("नमस्ते, मैं ई-डन हूँ। आपकी डीप न्यूट्रल जेमिनी प्रो वॉइस असिस्टेंट।")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("preview_voice_hi_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlass),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGlassBorder)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = RadiantAqua)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("आवाज सुनें (हिंदी)", color = RadiantAqua, fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    performHaptic()
                                    viewModel.previewVoice("Hello, I am E-DEN. Your deep neutral Gemini voice assistant.")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("preview_voice_en_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlass),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGlassBorder)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = RadiantAqua)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Listen (English)", color = RadiantAqua, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Auto TTS Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto Read Responses (स्वचालित वाचन)", style = MaterialTheme.typography.titleMedium, color = StarlightWhite)
                                Text("Speak AI responses aloud immediately", style = MaterialTheme.typography.bodyMedium, color = CosmicMuted)
                            }
                            Switch(
                                checked = userPreferences.ttsEnabled,
                                onCheckedChange = {
                                    performHaptic()
                                    viewModel.setTtsEnabled(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = StarlightWhite, checkedTrackColor = ElectricIndigo)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Haptic Feedback Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Haptic Tactile Feedback", style = MaterialTheme.typography.titleMedium, color = StarlightWhite)
                                Text("Vibrate gently on voice interactions & taps", style = MaterialTheme.typography.bodyMedium, color = CosmicMuted)
                            }
                            Switch(
                                checked = userPreferences.hapticsEnabled,
                                onCheckedChange = {
                                    performHaptic()
                                    viewModel.setHapticsEnabled(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = StarlightWhite, checkedTrackColor = ElectricIndigo)
                            )
                        }
                    }
                }
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
