package com.creatorsclub.studio.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.database.auth.AuthSessionManager
import com.creatorsclub.core.designsystem.theme.KreaviloColors

data class PermissionFeatureItem(
    val title: String,
    val description: String,
    val studiosUsed: String,
    val icon: ImageVector,
    val isGranted: Boolean
)

@Composable
fun PermissionOnboardingScreen(
    onPermissionsCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authManager = remember(context) { AuthSessionManager.getInstance(context) }

    var cameraGranted by remember { mutableStateOf(false) }
    var audioGranted by remember { mutableStateOf(false) }
    var mediaGranted by remember { mutableStateOf(false) }
    var notificationGranted by remember { mutableStateOf(false) }
    var hasRequestedPermissions by remember { mutableStateOf(false) }

    // Android runtime permissions launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasRequestedPermissions = true
        cameraGranted = results[Manifest.permission.CAMERA] == true
        audioGranted = results[Manifest.permission.RECORD_AUDIO] == true

        mediaGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            results[Manifest.permission.READ_MEDIA_IMAGES] == true ||
                    results[Manifest.permission.READ_MEDIA_VIDEO] == true
        } else {
            results[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationGranted = results[Manifest.permission.POST_NOTIFICATIONS] == true
        }
    }

    val permissionsToRequest = remember {
        val list = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.READ_MEDIA_IMAGES)
            list.add(Manifest.permission.READ_MEDIA_VIDEO)
            list.add(Manifest.permission.READ_MEDIA_AUDIO)
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        list.toTypedArray()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KreaviloColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Device Permissions",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KreaviloColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Creators Club processes all your content 100% on-device. Enable permissions to unlock your creative studios.",
                fontSize = 13.sp,
                color = KreaviloColors.TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Permission Card 1: Media & Files
            PermissionItemCard(
                title = "Photos & Media Files",
                description = "Import reference photos, sound tracks, and videos, and export finished high-resolution creative designs.",
                studios = "Photo, Video, Design, Audio, Banner & Thumbnail Studios",
                icon = Icons.Outlined.PhotoLibrary,
                isGranted = mediaGranted
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Permission Card 2: Camera
            PermissionItemCard(
                title = "Camera Access",
                description = "Live camera projection for paper tracing, product photography, and real-time capture.",
                studios = "AR Tracing Studio, Product Studio, Photo Studio",
                icon = Icons.Outlined.CameraAlt,
                isGranted = cameraGranted
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Permission Card 3: Microphone
            PermissionItemCard(
                title = "Microphone",
                description = "Record 48 kHz studio voiceovers, narration, and video audio dubbing directly into your projects.",
                studios = "Audio Studio, Video Studio, Teleprompter",
                icon = Icons.Outlined.Mic,
                isGranted = audioGranted
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Permission Card 4: Notifications
            PermissionItemCard(
                title = "Notifications (Optional)",
                description = "Receive instant alerts when 4K video exports or multi-image rendering batches finish in the background.",
                studios = "Background Exporters & Project Renderers",
                icon = Icons.Outlined.Notifications,
                isGranted = notificationGranted
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action Buttons at Bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Button(
                onClick = {
                    permissionsLauncher.launch(permissionsToRequest)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary)
            ) {
                Text(
                    text = if (hasRequestedPermissions) "Re-check / Grant Permissions" else "Grant Permissions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    authManager.setPermissionsCompleted(true)
                    onPermissionsCompleted()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (hasRequestedPermissions) "Continue to Studios" else "Continue with Selective Access",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KreaviloColors.Primary
                )
            }
        }
    }
}

@Composable
private fun PermissionItemCard(
    title: String,
    description: String,
    studios: String,
    icon: ImageVector,
    isGranted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) Color(0xFFE8F5E9) else KreaviloColors.PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Filled.Check else icon,
                    contentDescription = null,
                    tint = if (isGranted) Color(0xFF2E7D32) else KreaviloColors.Primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KreaviloColors.TextPrimary
                    )
                    if (isGranted) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "Granted",
                                color = Color(0xFF2E7D32),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = KreaviloColors.TextSecondary,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Used in: $studios",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = KreaviloColors.Accent
                )
            }
        }
    }
}
