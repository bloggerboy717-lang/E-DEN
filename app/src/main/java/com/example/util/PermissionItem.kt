package com.example.util

import android.Manifest
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.graphics.vector.ImageVector

data class AssistantPermission(
    val id: String,
    val permission: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isMandatory: Boolean
)

object PermissionRepository {
    fun getAppPermissions(): List<AssistantPermission> {
        val list = mutableListOf(
            AssistantPermission(
                id = "mic",
                permission = Manifest.permission.RECORD_AUDIO,
                title = "माइक्रोफ़ोन (Voice Mic)",
                description = "वॉइस असिस्टेंट से बात करने और वॉइस कमांड देने के लिए।",
                icon = Icons.Default.Mic,
                isMandatory = true
            ),
            AssistantPermission(
                id = "camera",
                permission = Manifest.permission.CAMERA,
                title = "कैमरा (Camera)",
                description = "वॉइस कमांड से कैमरा खोलने और फोटो लेने के लिए।",
                icon = Icons.Default.CameraAlt,
                isMandatory = false
            ),
            AssistantPermission(
                id = "phone",
                permission = Manifest.permission.CALL_PHONE,
                title = "फोन कॉल (Phone Call)",
                description = "वॉइस असिस्टेंट द्वारा सीधे कॉल लगाने के लिए।",
                icon = Icons.Default.Phone,
                isMandatory = false
            ),
            AssistantPermission(
                id = "location",
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                title = "लोकेशन (Location)",
                description = "आस-पास का मौसम, मैप्स और लोकल जानकारी के लिए।",
                icon = Icons.Default.LocationOn,
                isMandatory = false
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(
                AssistantPermission(
                    id = "notification",
                    permission = Manifest.permission.POST_NOTIFICATIONS,
                    title = "नोटिफिकेशन (Notifications)",
                    description = "असिस्टेंट रिमाइंडर और जरूरी अलर्ट्स के लिए।",
                    icon = Icons.Default.Notifications,
                    isMandatory = false
                )
            )
        }

        return list
    }
}
