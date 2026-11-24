package com.creatorsclub.feature.photo.removebg

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveBgScreen(
    viewModel: RemoveBgViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadImageFromUri(it) }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { viewModel.loadImageFromBitmap(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Remove Background",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = KreaviloColors.TextPrimary)
                    }
                },
                actions = {
                    // Capture with Camera
                    IconButton(onClick = { takePictureLauncher.launch(null) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = "Camera", tint = KreaviloColors.Primary)
                    }

                    // Import from Gallery
                    IconButton(onClick = { photoPickerLauncher.launch("image/*") }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = "Gallery", tint = KreaviloColors.Primary)
                    }

                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.canUndo,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (uiState.canUndo) KreaviloColors.TextPrimary else KreaviloColors.TextTertiary
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = uiState.canRedo,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (uiState.canRedo) KreaviloColors.TextPrimary else KreaviloColors.TextTertiary
                        )
                    }

                    // Export Lossless PNG Button
                    Button(
                        onClick = { viewModel.exportTransparentPng() },
                        enabled = uiState.compositeBitmap != null && !uiState.isLoading && !uiState.isExporting,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .padding(end = 8.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export PNG", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreaviloColors.Surface)
            )
        },
        bottomBar = {
            if (uiState.compositeBitmap != null) {
                Surface(
                    color = KreaviloColors.Surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        // Tool Mode Chips (Auto, Erase, Restore, Feather)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.activeTool == RemoveBgTool.AUTO,
                                onClick = { viewModel.selectTool(RemoveBgTool.AUTO) },
                                label = { Text("Auto Cutout", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Outlined.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                            FilterChip(
                                selected = uiState.activeTool == RemoveBgTool.ERASE,
                                onClick = { viewModel.selectTool(RemoveBgTool.ERASE) },
                                label = { Text("Erase Brush", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Outlined.AutoFixNormal, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                            FilterChip(
                                selected = uiState.activeTool == RemoveBgTool.RESTORE,
                                onClick = { viewModel.selectTool(RemoveBgTool.RESTORE) },
                                label = { Text("Restore Brush", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Outlined.Brush, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Brush Size Slider (when brush active)
                        if (uiState.activeTool == RemoveBgTool.ERASE || uiState.activeTool == RemoveBgTool.RESTORE) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Brush Size", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
                                Text("${uiState.brushSize.roundToInt()} px", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
                            }
                            Slider(
                                value = uiState.brushSize,
                                onValueChange = { viewModel.updateBrushSize(it) },
                                valueRange = 10f..80f,
                                colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
                            )
                        } else {
                            // Feather Radius Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Edge Feathering", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
                                Text("${uiState.featherRadius.roundToInt()} px", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
                            }
                            Slider(
                                value = uiState.featherRadius,
                                onValueChange = { viewModel.updateFeather(it) },
                                valueRange = 0f..20f,
                                colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
                            )
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
                .background(Color(0xFF1E212B)),
            contentAlignment = Alignment.Center
        ) {
            val composite = uiState.compositeBitmap
            if (composite != null) {
                var zoomScale by remember { mutableFloatStateOf(1f) }
                var panOffset by remember { mutableStateOf(Offset.Zero) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(uiState.activeTool) {
                            if (uiState.activeTool == RemoveBgTool.ERASE || uiState.activeTool == RemoveBgTool.RESTORE) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val normX = (change.position.x / size.width).coerceIn(0f, 1f)
                                    val normY = (change.position.y / size.height).coerceIn(0f, 1f)
                                    viewModel.onBrushStroke(normX, normY)
                                }
                            } else {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    zoomScale = (zoomScale * zoom).coerceIn(1f, 5f)
                                    panOffset = if (zoomScale > 1f) {
                                        Offset(panOffset.x + pan.x, panOffset.y + pan.y)
                                    } else {
                                        Offset.Zero
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Checkerboard Canvas Pattern (Transparency Indicator)
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val squareSize = 24f
                        val cols = (size.width / squareSize).toInt() + 1
                        val rows = (size.height / squareSize).toInt() + 1
                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                val isEven = (r + c) % 2 == 0
                                drawRect(
                                    color = if (isEven) Color(0xFF2C303E) else Color(0xFF202330),
                                    topLeft = Offset(c * squareSize, r * squareSize),
                                    size = Size(squareSize, squareSize)
                                )
                            }
                        }
                    }

                    // Transparent Cutout Image
                    Image(
                        bitmap = composite.asImageBitmap(),
                        contentDescription = "Cutout Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = panOffset.x
                                translationY = panOffset.y
                            }
                    )
                }
            } else {
                // Empty State Card
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
                                .background(KreaviloColors.ProductStudioCardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoFixHigh,
                                contentDescription = null,
                                tint = KreaviloColors.ProductStudioBadge,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "On-Device Background Removal",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Automatic foreground extraction, feathering, and manual erase/restore brushes. 100% offline.",
                            fontSize = 12.sp,
                            color = KreaviloColors.TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Photo from Gallery", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { takePictureLauncher.launch(null) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp), tint = KreaviloColors.Primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Take Photo with Camera", color = KreaviloColors.Primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Export Success Feedback
            uiState.exportSuccessUri?.let { uri ->
                Surface(
                    color = Color(0xFF1B5E20),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Transparent PNG saved to Gallery!",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.dismissExportMessage() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Loading indicator
            if (uiState.isLoading || uiState.isExporting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = KreaviloColors.Primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (uiState.isExporting) "Saving transparent PNG..." else "Extracting foreground subject...",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
