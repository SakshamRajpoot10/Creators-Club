package com.creatorsclub.feature.ar_drawing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArDrawingScreen(
    viewModel: ArDrawingViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Camera AR Tracing",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = KreaviloColors.TextPrimary)
                    }
                },
                actions = {
                    // Lock button to freeze touch so phone can sit above paper
                    IconButton(
                        onClick = { viewModel.toggleScreenLock() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (uiState.isScreenLocked) KreaviloColors.PrimaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (uiState.isScreenLocked) Icons.Filled.Lock else Icons.Outlined.LockOpen,
                            contentDescription = "Lock Screen",
                            tint = if (uiState.isScreenLocked) KreaviloColors.Primary else KreaviloColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreaviloColors.Surface)
            )
        },
        bottomBar = {
            if (!uiState.isScreenLocked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KreaviloColors.Surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Opacity Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Opacity",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KreaviloColors.TextPrimary,
                            modifier = Modifier.width(60.dp)
                        )
                        Slider(
                            value = uiState.overlayOpacity,
                            onValueChange = { viewModel.setOverlayOpacity(it) },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(uiState.overlayOpacity * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = KreaviloColors.TextSecondary,
                            modifier = Modifier.width(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mode toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = uiState.isOutlineMode,
                            onClick = { viewModel.toggleOutlineMode() },
                            label = { Text("Sketch Outline", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KreaviloColors.Primary,
                                selectedLabelColor = Color.White
                            )
                        )

                        Text(
                            text = "Tip: Place phone on cup/stand above paper",
                            fontSize = 11.sp,
                            color = KreaviloColors.TextSecondary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F172A))
                .pointerInput(uiState.isScreenLocked) {
                    if (!uiState.isScreenLocked) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            viewModel.updateTransform(
                                scale = (uiState.scale * zoom).coerceIn(0.4f, 4.0f),
                                offsetX = uiState.offsetX + pan.x,
                                offsetY = uiState.offsetY + pan.y,
                                rotation = uiState.rotation + rotation
                            )
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val displayBitmap = if (uiState.isOutlineMode) {
                uiState.sketchOutlineBitmap ?: uiState.referenceBitmap
            } else {
                uiState.referenceBitmap
            }

            if (displayBitmap != null) {
                Image(
                    bitmap = displayBitmap.asImageBitmap(),
                    contentDescription = "Tracing Guide Overlay",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = uiState.scale
                            scaleY = uiState.scale
                            translationX = uiState.offsetX
                            translationY = uiState.offsetY
                            rotationZ = uiState.rotation
                            alpha = uiState.overlayOpacity
                        }
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Draw,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Choose any photo or sketch to begin tracing",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
            }

            // Locked watermark indicator
            if (uiState.isScreenLocked) {
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Screen Locked for Tracing — Tap Lock icon to unlock", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
