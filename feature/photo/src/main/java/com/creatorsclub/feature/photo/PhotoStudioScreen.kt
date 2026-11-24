package com.creatorsclub.feature.photo

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors
import com.creatorsclub.engine.image.model.CropAspectRatio
import com.creatorsclub.engine.image.model.FilterPreset
import com.creatorsclub.engine.image.processor.ColorMatrixProcessor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoStudioScreen(
    viewModel: PhotoStudioViewModel,
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
    ) { bitmap: android.graphics.Bitmap? ->
        bitmap?.let { viewModel.loadImageFromBitmap(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Photo Studio",
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
                    // Capture Live Photo with Camera
                    IconButton(onClick = {
                        val hasCamera = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.CAMERA
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (hasCamera) {
                            takePictureLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = "Capture Photo",
                            tint = KreaviloColors.Primary
                        )
                    }

                    // Import Photo Icon
                    IconButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = "Import Photo",
                            tint = KreaviloColors.Primary
                        )
                    }

                    // Compare button
                    IconButton(
                        onClick = { viewModel.setComparingOriginal(!uiState.isComparingOriginal) },
                        enabled = uiState.sourceBitmap != null
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Compare,
                            contentDescription = "Compare with Original",
                            tint = if (uiState.isComparingOriginal) KreaviloColors.Primary else KreaviloColors.TextSecondary
                        )
                    }

                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.canUndo
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (uiState.canUndo) KreaviloColors.TextPrimary else KreaviloColors.TextTertiary
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = uiState.canRedo
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (uiState.canRedo) KreaviloColors.TextPrimary else KreaviloColors.TextTertiary
                        )
                    }

                    // Export Button
                    Button(
                        onClick = { viewModel.exportImage() },
                        enabled = uiState.sourceBitmap != null && !uiState.isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreaviloColors.Surface)
            )
        },
        bottomBar = {
            if (uiState.sourceBitmap != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KreaviloColors.Surface)
                        .navigationBarsPadding()
                ) {
                    // Secondary Controls Area (Sliders / Filter gallery / Crop pills)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        when (uiState.activeTab) {
                            PhotoEditorTab.FILTERS -> FilterGalleryRow(
                                selectedFilter = uiState.adjustments.filterPreset,
                                thumbnailBitmap = uiState.previewThumbnail,
                                onSelectFilter = { viewModel.applyFilter(it) }
                            )
                            PhotoEditorTab.ADJUSTMENTS -> AdjustmentSlidersRow(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                            PhotoEditorTab.CROP -> CropControlsRow(
                                selectedRatio = uiState.cropTransform.aspectRatio,
                                onSelectRatio = { viewModel.setAspectRatio(it) },
                                onRotate = { viewModel.rotate90() },
                                onFlip = { viewModel.flipHorizontal() }
                            )
                        }
                    }

                    HorizontalDivider(color = KreaviloColors.Border)

                    // Primary Tool Tabs (Filters, Adjustments, Crop)
                    TabRow(
                        selectedTabIndex = uiState.activeTab.ordinal,
                        containerColor = KreaviloColors.Surface,
                        contentColor = KreaviloColors.Primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.activeTab.ordinal]),
                                color = KreaviloColors.Primary
                            )
                        }
                    ) {
                        Tab(
                            selected = uiState.activeTab == PhotoEditorTab.FILTERS,
                            onClick = { viewModel.selectTab(PhotoEditorTab.FILTERS) },
                            text = { Text("Filters", fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(Icons.Outlined.AutoFixHigh, contentDescription = null) }
                        )
                        Tab(
                            selected = uiState.activeTab == PhotoEditorTab.ADJUSTMENTS,
                            onClick = { viewModel.selectTab(PhotoEditorTab.ADJUSTMENTS) },
                            text = { Text("Adjust", fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(Icons.Outlined.Tune, contentDescription = null) }
                        )
                        Tab(
                            selected = uiState.activeTab == PhotoEditorTab.CROP,
                            onClick = { viewModel.selectTab(PhotoEditorTab.CROP) },
                            text = { Text("Crop & Rotate", fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(Icons.Outlined.Crop, contentDescription = null) }
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
                .background(Color(0xFF12141C)), // Dark canvas container for professional image viewing
            contentAlignment = Alignment.Center
        ) {
            val bitmap = uiState.sourceBitmap
            if (bitmap != null) {
                val colorFilter = if (uiState.isComparingOriginal) {
                    null
                } else {
                    val colorMatrix = ColorMatrixProcessor.createCombinedColorMatrix(uiState.adjustments)
                    ColorFilter.colorMatrix(colorMatrix)
                }

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Active Image Preview",
                    colorFilter = colorFilter,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .graphicsLayer {
                            rotationZ = uiState.cropTransform.rotationDegrees
                            scaleX = if (uiState.cropTransform.isFlippedHorizontal) -1f else 1f
                            scaleY = if (uiState.cropTransform.isFlippedVertical) -1f else 1f
                        }
                )

                if (uiState.isComparingOriginal) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = "ORIGINAL",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
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
                                .background(KreaviloColors.PhotoStudioCardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = null,
                                tint = KreaviloColors.PhotoStudioBadge,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Start Editing in Photo Studio",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Import a high-resolution photo from your phone or begin on a blank canvas.",
                            fontSize = 12.sp,
                            color = KreaviloColors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Button 1: Import from Gallery
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Photo from Gallery", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Button 2: Try Sample Photo
                        OutlinedButton(
                            onClick = { viewModel.loadSampleImage() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp), tint = KreaviloColors.Primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Try Sample Artwork", color = KreaviloColors.Primary, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Button 3: Blank White Canvas (1:1)
                        TextButton(
                            onClick = { viewModel.createBlankCanvas() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create Blank Canvas (1080x1080)", fontSize = 12.sp, color = KreaviloColors.TextSecondary)
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
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Photo saved in device Gallery (Lossless)!",
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

            if (uiState.isLoading) {
                CircularProgressIndicator(color = KreaviloColors.Primary)
            }
        }
    }
}

@Composable
fun FilterGalleryRow(
    selectedFilter: FilterPreset,
    thumbnailBitmap: Bitmap?,
    onSelectFilter: (FilterPreset) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = remember { listOf("All", "Essentials", "Cinema", "Classic", "Monochrome", "Creative") }

    val filteredPresets = remember(selectedCategory) {
        if (selectedCategory == "All") FilterPreset.entries
        else FilterPreset.entries.filter { it.category == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            items(categories) { cat ->
                val isCatSelected = cat == selectedCategory
                FilterChip(
                    selected = isCatSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp, fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KreaviloColors.PrimaryContainer,
                        selectedLabelColor = KreaviloColors.Primary
                    )
                )
            }
        }

        // Horizontal Filters Carousel with Live Thumbnail Previews
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredPresets) { preset ->
                val isSelected = preset == selectedFilter
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectFilter(preset) }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) KreaviloColors.PrimaryContainer else KreaviloColors.SurfaceVariant)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) KreaviloColors.Primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (thumbnailBitmap != null) {
                            val matrix = ColorMatrixProcessor.getPresetColorMatrix(preset)
                            Image(
                                bitmap = thumbnailBitmap.asImageBitmap(),
                                contentDescription = preset.displayName,
                                colorFilter = ColorFilter.colorMatrix(matrix),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.FilterFrames,
                                contentDescription = preset.displayName,
                                tint = if (isSelected) KreaviloColors.Primary else KreaviloColors.TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = preset.displayName,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) KreaviloColors.Primary else KreaviloColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun AdjustmentSlidersRow(
    uiState: PhotoStudioUiState,
    viewModel: PhotoStudioViewModel
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Tool Selector Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ActiveAdjustmentTool.entries) { tool ->
                val isSelected = tool == uiState.activeAdjustmentTool
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectAdjustmentTool(tool) },
                    label = { Text(tool.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KreaviloColors.Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active Slider
        when (uiState.activeAdjustmentTool) {
            ActiveAdjustmentTool.BRIGHTNESS -> SliderWithLabel(
                label = "Brightness",
                value = uiState.adjustments.brightness,
                valueRange = -1f..1f,
                onValueChange = { viewModel.updateBrightness(it) }
            )
            ActiveAdjustmentTool.CONTRAST -> SliderWithLabel(
                label = "Contrast",
                value = uiState.adjustments.contrast,
                valueRange = 0f..2f,
                onValueChange = { viewModel.updateContrast(it) }
            )
            ActiveAdjustmentTool.SATURATION -> SliderWithLabel(
                label = "Saturation",
                value = uiState.adjustments.saturation,
                valueRange = 0f..2f,
                onValueChange = { viewModel.updateSaturation(it) }
            )
            ActiveAdjustmentTool.EXPOSURE -> SliderWithLabel(
                label = "Exposure",
                value = uiState.adjustments.exposure,
                valueRange = -1f..1f,
                onValueChange = { viewModel.updateExposure(it) }
            )
            ActiveAdjustmentTool.WARMTH -> SliderWithLabel(
                label = "Warmth",
                value = uiState.adjustments.warmth,
                valueRange = -1f..1f,
                onValueChange = { viewModel.updateWarmth(it) }
            )
            ActiveAdjustmentTool.VIGNETTE -> SliderWithLabel(
                label = "Vignette",
                value = uiState.adjustments.vignette,
                valueRange = 0f..1f,
                onValueChange = { viewModel.updateVignette(it) }
            )
        }
    }
}

@Composable
fun SliderWithLabel(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Text(String.format("%.2f", value), fontSize = 12.sp, color = KreaviloColors.Primary, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = KreaviloColors.Primary,
                activeTrackColor = KreaviloColors.Primary,
                inactiveTrackColor = KreaviloColors.BorderLight
            )
        )
    }
}

@Composable
fun CropControlsRow(
    selectedRatio: CropAspectRatio,
    onSelectRatio: (CropAspectRatio) -> Unit,
    onRotate: () -> Unit,
    onFlip: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Transform Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRotate,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Rotate 90°", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = onFlip,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Outlined.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Flip Horiz", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Aspect Ratio Selector Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(CropAspectRatio.entries) { ratio ->
                val isSelected = ratio == selectedRatio
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectRatio(ratio) },
                    label = { Text(ratio.displayName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KreaviloColors.Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}
