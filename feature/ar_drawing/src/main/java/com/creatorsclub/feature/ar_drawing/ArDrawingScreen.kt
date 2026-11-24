package com.creatorsclub.feature.ar_drawing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadReferenceFromUri(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Camera AR Tracing",
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = KreaviloColors.TextPrimary)
                    }
                },
                actions = {
                    // Camera Viewfinder Permission Toggle
                    IconButton(onClick = {
                        if (!hasCameraPermission) {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }) {
                        Icon(
                            imageVector = if (hasCameraPermission) Icons.Filled.Videocam else Icons.Outlined.VideocamOff,
                            contentDescription = if (hasCameraPermission) "Camera Active" else "Enable Camera Viewfinder",
                            tint = if (hasCameraPermission) KreaviloColors.Primary else KreaviloColors.Error
                        )
                    }

                    // Import Reference Image
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = "Import Reference Image",
                            tint = KreaviloColors.Primary
                        )
                    }

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
            if (!uiState.isScreenLocked && (uiState.sketchOutlineBitmap != null || uiState.referenceBitmap != null)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KreaviloColors.Surface)
                        .navigationBarsPadding()
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
                            modifier = Modifier.width(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Controls Row (Edge Filter toggle, Reset)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = uiState.isOutlineMode,
                            onClick = { viewModel.toggleOutlineMode() },
                            label = { Text("Contour Sketch Mode", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KreaviloColors.Primary,
                                selectedLabelColor = Color.White
                            )
                        )

                        TextButton(
                            onClick = { viewModel.updateTransform(1.0f, 0f, 0f, 0f) }
                        ) {
                            Text("Reset Scale", fontSize = 11.sp, color = KreaviloColors.Primary)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF1E222D))
                .pointerInput(uiState.isScreenLocked) {
                    if (!uiState.isScreenLocked) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            viewModel.updateTransform(
                                scale = (uiState.scale * zoom).coerceIn(0.4f, 4f),
                                offsetX = uiState.offsetX + pan.x,
                                offsetY = uiState.offsetY + pan.y,
                                rotation = uiState.rotation + rotation
                            )
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val bitmapToDraw = if (uiState.isOutlineMode && uiState.sketchOutlineBitmap != null) {
                uiState.sketchOutlineBitmap
            } else {
                uiState.referenceBitmap
            }

            if (bitmapToDraw != null) {
                Image(
                    bitmap = bitmapToDraw.asImageBitmap(),
                    contentDescription = "AR Tracing Reference",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .graphicsLayer {
                            scaleX = uiState.scale
                            scaleY = uiState.scale
                            translationX = uiState.offsetX
                            translationY = uiState.offsetY
                            rotationZ = uiState.rotation
                            alpha = uiState.overlayOpacity
                        }
                )

                // Lock instruction pill
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
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Screen Locked for Paper Tracing", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                // Interactive Empty State Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(KreaviloColors.DrawingStudioCardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                tint = KreaviloColors.DrawingStudioBadge,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Learn to Draw on Paper",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Import any photo to extract high-contrast outline contours. Prop your phone on a stand or mug above your paper, lock the screen, and trace the lines directly!",
                            fontSize = 12.sp,
                            color = KreaviloColors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose Reference Photo", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { viewModel.loadSampleSketch() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp), tint = KreaviloColors.Primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Try Sample Contour", color = KreaviloColors.Primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(color = KreaviloColors.Primary)
            }
        }
    }
}
