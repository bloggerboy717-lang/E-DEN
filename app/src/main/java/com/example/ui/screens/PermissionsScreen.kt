package com.example.ui.screens

import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.EdenViewModel
import com.example.ui.components.CosmicBackground
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.CosmicMuted
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.RadiantAqua
import com.example.ui.theme.StarlightWhite
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.SurfaceGlassBorder
import com.example.util.PermissionRepository

@Composable
fun PermissionsScreen(
    viewModel: EdenViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val permissions = remember { PermissionRepository.getAppPermissions() }

    // Trigger state to re-evaluate permissions upon result
    var checkTrigger by remember { mutableIntStateOf(0) }

    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    val singleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        checkTrigger++
    }

    val multipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        checkTrigger++
    }

    CosmicBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 28.dp, bottom = 120.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(ElectricIndigo.copy(alpha = 0.25f))
                        .border(1.dp, RadiantAqua.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = RadiantAqua,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PERMISSIONS & DEFAULT ASSISTANT",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.2.sp,
                        color = RadiantAqua,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "परमिशन और डिफ़ॉल्ट एआई सेटअप",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = StarlightWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Default Assistant Setting Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, RadiantAqua.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                    .testTag("default_assistant_card"),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.92f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RadiantAqua.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = RadiantAqua,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Set as Default Assistant / डिफ़ॉल्ट एआई",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = StarlightWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "E-DEN को अपने फोन का डिफ़ॉल्ट वॉइस असिस्टेंट बनाएं ताकि पावर बटन या होम बटन दबाते ही यह तुरंत खुल सके।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CosmicMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.openDefaultAssistantSettings()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("set_default_assistant_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("डिफ़ॉल्ट असिस्टेंट सेटिंग्स खोलें (Set Default)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Grant All Permissions Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "अनुमतियां (APP PERMISSIONS)",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    color = RadiantAqua,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        val needed = permissions
                            .map { it.permission }
                            .filter { !isPermissionGranted(it) }
                            .toTypedArray()
                        if (needed.isNotEmpty()) {
                            multipleLauncher.launch(needed)
                        } else {
                            viewModel.openAppSettings()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RadiantAqua),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RadiantAqua.copy(alpha = 0.6f))
                ) {
                    Text("सभी परमिशन दें (Grant All)")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Permission List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Read checkTrigger so Compose recomposes when permissions change
                val unused = checkTrigger

                permissions.forEach { perm ->
                    val granted = isPermissionGranted(perm.permission)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .border(
                                1.dp,
                                if (granted) Color(0xFF10B981).copy(alpha = 0.5f) else SurfaceGlassBorder,
                                RoundedCornerShape(18.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.85f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (granted) Color(0xFF064E3B).copy(alpha = 0.6f) else SurfaceGlass
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = perm.icon,
                                    contentDescription = null,
                                    tint = if (granted) Color(0xFF34D399) else RadiantAqua,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = perm.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StarlightWhite
                                    )
                                    if (perm.isMandatory) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFEF4444).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "आवश्यक",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFFCA5A5),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = perm.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CosmicMuted,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (granted) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF064E3B).copy(alpha = 0.6f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Granted",
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "अनुमति मिली",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFD1FAE5),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        singleLauncher.launch(perm.permission)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("grant_permission_${perm.id}")
                                ) {
                                    Text("परमिशन दें")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Settings button fallback
            OutlinedButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    viewModel.openAppSettings()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicMuted),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGlassBorder)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("फ़ोन ऐप सेटिंग्स खोलें (Manage In System Settings)")
            }
        }
    }
}
