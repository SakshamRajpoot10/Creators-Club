package com.creatorsclub.feature.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.creatorsclub.core.designsystem.theme.KreaviloColors

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val usedFor: String,
    val icon: ImageVector,
    val permissions: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }

    val permissionItems = remember {
        val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        listOf(
            PermissionItem(
                id = "camera",
                title = "Camera",
                description = "Required to capture live photos and videos, and to trace paper sketches in AR Tracing Studio.",
                usedFor = "AR Tracing Studio, Photo Studio, Product Studio, Video Studio",
                icon = Icons.Outlined.PhotoCamera,
                permissions = listOf(Manifest.permission.CAMERA)
            ),
            PermissionItem(
                id = "microphone",
                title = "Microphone",
                description = "Required to record 48 kHz voiceovers, podcasts, and audio clips for your creative projects.",
                usedFor = "Audio Studio, Video Studio Voiceover",
                icon = Icons.Outlined.Mic,
                permissions = listOf(Manifest.permission.RECORD_AUDIO)
            ),
            PermissionItem(
                id = "storage",
                title = "Photos & Media Storage",
                description = "Required to import your photos, videos, custom fonts, and export high-resolution creative artwork.",
                usedFor = "All Studios (Import & Lossless Export)",
                icon = Icons.Outlined.FolderOpen,
                permissions = storagePermissions
            ),
            PermissionItem(
                id = "notifications",
                title = "Notifications (Optional)",
                description = "Receive instant alerts when 4K video exports, audio rendering, or background processing complete.",
                usedFor = "Background Video & Audio Renderers",
                icon = Icons.Outlined.Notifications,
                permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    emptyList()
                }
            )
        )
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                refreshKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshKey++
    }

    fun isPermissionGranted(permissions: List<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "App Permissions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = KreaviloColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreaviloColors.Surface)
            )
        },
        containerColor = KreaviloColors.Background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Privacy Guarantee Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.PrimaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = "Security",
                        tint = KreaviloColors.Primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "100% Private & Local Processing",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.Primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Creators Club processes your photos, videos, and audio directly on your device. We never sell or upload your personal media.",
                            fontSize = 11.sp,
                            color = KreaviloColors.TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Required Permissions",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = KreaviloColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Permission Items List
            permissionItems.forEach { item ->
                key(refreshKey) {
                    val granted = isPermissionGranted(item.permissions)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (granted) Color(0xFFE8F5E9) else KreaviloColors.PrimaryContainer
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (granted) Color(0xFF2E7D32) else KreaviloColors.Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KreaviloColors.TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (granted) "Permission Granted" else "Permission Required",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (granted) Color(0xFF2E7D32) else Color(0xFFE65100)
                                    )
                                }

                                if (!granted) {
                                    Button(
                                        onClick = {
                                            permissionLauncher.launch(item.permissions.toTypedArray())
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Allow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Granted",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = item.description,
                                fontSize = 12.sp,
                                color = KreaviloColors.TextSecondary,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Used in: ${item.usedFor}",
                                fontSize = 10.sp,
                                color = KreaviloColors.TextTertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Open System Settings Action
            OutlinedButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KreaviloColors.Primary)
            ) {
                Icon(Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Android System App Settings", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
