package com.creatorsclub.studio.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.creatorsclub.core.database.auth.AuthSessionManager
import com.creatorsclub.core.designsystem.theme.KreaviloColors
import com.creatorsclub.studio.R

@Composable
fun OnboardingScreen(
    onOnboardingCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authManager = remember(context) { AuthSessionManager.getInstance(context) }
    var currentPage by remember { mutableIntStateOf(1) }
    var refreshPermissionsKey by remember { mutableIntStateOf(0) }

    // Recheck permissions on app resume
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                refreshPermissionsKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KreaviloColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally(animationSpec = tween(350)) { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween(350)) { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally(animationSpec = tween(350)) { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween(350)) { width -> width } + fadeOut()
                }
            },
            label = "OnboardingPageTransition"
        ) { page ->
            when (page) {
                1 -> WelcomeScreenContent(
                    onGetStarted = { currentPage = 2 }
                )
                2 -> PermissionSetupScreenContent(
                    refreshKey = refreshPermissionsKey,
                    onPermissionStateChanged = { refreshPermissionsKey++ },
                    onContinue = {
                        authManager.setOnboardingCompleted(true)
                        authManager.setPermissionsCompleted(true)
                        onOnboardingCompleted()
                    },
                    onSkip = {
                        authManager.setOnboardingCompleted(true)
                        authManager.setPermissionsCompleted(true)
                        onOnboardingCompleted()
                    }
                )
            }
        }
    }
}

/**
 * Screen 1: Welcome Screen (1 of 2)
 * Matches exact UI layout: App squircle logo, title, tagline, welcome card with 3 features,
 * "Get started" button, and "1 of 2" indicator.
 */
@Composable
private fun WelcomeScreenContent(
    onGetStarted: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // App Icon in Squircle Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(KreaviloColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.creators_club_icon),
                    contentDescription = "Kreavilo Studio Logo",
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Kreavilo Studio",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = KreaviloColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Create without limits.",
                fontSize = 13.sp,
                color = KreaviloColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Welcome Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(KreaviloColors.BorderLight)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Welcome, Creator!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KreaviloColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Your complete creative workspace for photography, video, design, audio and more.",
                        fontSize = 13.sp,
                        color = KreaviloColors.TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Feature 1: Create professional videos
                    WelcomeFeatureRow(
                        icon = Icons.Outlined.Movie,
                        title = "Create professional videos"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Feature 2: Design anything
                    WelcomeFeatureRow(
                        icon = Icons.Outlined.Palette,
                        title = "Design anything"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Feature 3: Studio-grade creative tools
                    WelcomeFeatureRow(
                        icon = Icons.Outlined.AutoAwesome,
                        title = "Create with studio tools"
                    )
                }
            }
        }

        // Bottom Navigation & Actions
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary)
            ) {
                Text(
                    text = "Get started",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "1 of 2",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = KreaviloColors.TextTertiary
            )
        }
    }
}

@Composable
private fun WelcomeFeatureRow(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEEF2FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = KreaviloColors.Primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = KreaviloColors.TextPrimary
        )
    }
}

/**
 * Screen 2: Permission Setup Screen (2 of 2)
 * Matches exact UI layout: "Set up your workspace", blue accent bar, subtitle,
 * 4 permission cards (Camera, Microphone, Photos & videos, Notifications) with Enable buttons,
 * "Continue" primary button, "Skip for now" secondary option, and "2 of 2" indicator.
 */
@Composable
private fun PermissionSetupScreenContent(
    refreshKey: Int,
    onPermissionStateChanged: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current

    val isCameraGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    val isMicrophoneGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    val isPhotosGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    val isNotificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    // Individual Permission Launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { onPermissionStateChanged() }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { onPermissionStateChanged() }

    val mediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { onPermissionStateChanged() }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { onPermissionStateChanged() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Set up your workspace",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = KreaviloColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Blue Accent Bar
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(KreaviloColors.Primary)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enable access to the tools you want to use.",
                fontSize = 13.sp,
                color = KreaviloColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card 1: Camera
            PermissionSetupRow(
                icon = Icons.Outlined.PhotoCamera,
                title = "Camera",
                description = "Capture photos and videos",
                isGranted = isCameraGranted,
                onEnable = { cameraLauncher.launch(Manifest.permission.CAMERA) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card 2: Microphone
            PermissionSetupRow(
                icon = Icons.Outlined.Mic,
                title = "Microphone",
                description = "Record audio and voiceovers",
                isGranted = isMicrophoneGranted,
                onEnable = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card 3: Photos & videos
            PermissionSetupRow(
                icon = Icons.Outlined.PhotoLibrary,
                title = "Photos & videos",
                description = "Import your creative media",
                isGranted = isPhotosGranted,
                onEnable = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mediaLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO))
                    } else {
                        mediaLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card 4: Notifications
            PermissionSetupRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                description = "Receive export updates",
                isGranted = isNotificationsGranted,
                onEnable = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )
        }

        // Bottom Actions
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip for now",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KreaviloColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "2 of 2",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = KreaviloColors.TextTertiary
            )
        }
    }
}

@Composable
private fun PermissionSetupRow(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onEnable: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(KreaviloColors.BorderLight)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isGranted) Color(0xFFE8F5E9) else Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Filled.Check else icon,
                        contentDescription = null,
                        tint = if (isGranted) Color(0xFF2E7D32) else KreaviloColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KreaviloColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = KreaviloColors.TextSecondary
                    )
                }
            }

            if (isGranted) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "Enabled",
                        color = Color(0xFF2E7D32),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            } else {
                TextButton(
                    onClick = onEnable,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Enable",
                        color = KreaviloColors.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
