package com.creatorsclub.feature.photo

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterFrames
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors
import com.creatorsclub.engine.image.model.*
import com.creatorsclub.engine.image.processor.ColorMatrixProcessor
import com.creatorsclub.engine.image.processor.HistogramData
import kotlin.math.roundToInt

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
    ) { bitmap: Bitmap? ->
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
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = KreaviloColors.TextPrimary
                        )
                    }
                },
                actions = {
                    // Capture Live Photo
                    IconButton(
                        onClick = {
                            val hasCamera = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                            if (hasCamera) {
                                takePictureLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = "Capture Photo",
                            tint = KreaviloColors.Primary
                        )
                    }

                    // Import Photo
                    IconButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = "Import Photo",
                            tint = KreaviloColors.Primary
                        )
                    }

                    // Compare Button
                    IconButton(
                        onClick = { viewModel.setComparingOriginal(!uiState.isComparingOriginal) },
                        enabled = uiState.sourceBitmap != null,
                        modifier = Modifier.size(48.dp)
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
                        enabled = uiState.canUndo,
                        modifier = Modifier.size(48.dp)
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
                        enabled = uiState.canRedo,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (uiState.canRedo) KreaviloColors.TextPrimary else KreaviloColors.TextTertiary
                        )
                    }

                    // Export Button CTA
                    Button(
                        onClick = { viewModel.showExportSheet() },
                        enabled = uiState.sourceBitmap != null && !uiState.isLoading && !uiState.isExporting,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .padding(end = 8.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    // Secondary Area depending on active tab
                    AnimatedVisibility(visible = uiState.activeTool == ActivePhotoTool.NONE) {
                        when (uiState.navTab) {
                            PhotoNavTab.LOOKS -> LooksDrawer(
                                selectedFilter = uiState.adjustments.filterPreset,
                                filterIntensity = uiState.adjustments.filterIntensity,
                                thumbnailBitmap = uiState.previewThumbnail,
                                onSelectFilter = { viewModel.applyFilter(it) },
                                onIntensityChange = { viewModel.setFilterIntensity(it) }
                            )
                            PhotoNavTab.TOOLS -> ToolsGridDrawer(
                                onSelectTool = { viewModel.openTool(it) }
                            )
                            PhotoNavTab.EXPORT -> Unit
                        }
                    }

                    HorizontalDivider(color = KreaviloColors.Border)

                    // Primary 3-Tab Bar: LOOKS | TOOLS | EXPORT
                    TabRow(
                        selectedTabIndex = uiState.navTab.ordinal,
                        containerColor = KreaviloColors.Surface,
                        contentColor = KreaviloColors.Primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.navTab.ordinal]),
                                color = KreaviloColors.Primary
                            )
                        }
                    ) {
                        PhotoNavTab.entries.forEach { tab ->
                            val isSelected = uiState.navTab == tab
                            Tab(
                                selected = isSelected,
                                onClick = { viewModel.selectNavTab(tab) },
                                text = {
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = when (tab) {
                                            PhotoNavTab.LOOKS -> Icons.Outlined.AutoFixHigh
                                            PhotoNavTab.TOOLS -> Icons.Outlined.Tune
                                            PhotoNavTab.EXPORT -> Icons.Outlined.Download
                                        },
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
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
                .background(Color(0xFF12141C)), // Dark canvas for photo color fidelity
            contentAlignment = Alignment.Center
        ) {
            val bitmap = uiState.sourceBitmap
            if (bitmap != null) {
                var zoomScale by remember { mutableFloatStateOf(1f) }
                var panOffset by remember { mutableStateOf(Offset.Zero) }

                val colorFilter = if (uiState.isComparingOriginal) {
                    null
                } else {
                    val colorMatrix = ColorMatrixProcessor.createCombinedColorMatrix(uiState.adjustments)
                    ColorFilter.colorMatrix(colorMatrix)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                zoomScale = (zoomScale * zoom).coerceIn(1f, 5f)
                                panOffset = if (zoomScale > 1f) {
                                    Offset(panOffset.x + pan.x, panOffset.y + pan.y)
                                } else {
                                    Offset.Zero
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    zoomScale = 1f
                                    panOffset = Offset.Zero
                                },
                                onPress = {
                                    // Touch and hold to compare
                                    viewModel.setComparingOriginal(true)
                                    tryAwaitRelease()
                                    viewModel.setComparingOriginal(false)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Active Image Preview",
                        colorFilter = colorFilter,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .graphicsLayer {
                                scaleX = zoomScale * (if (uiState.cropTransform.isFlippedHorizontal) -1f else 1f)
                                scaleY = zoomScale * (if (uiState.cropTransform.isFlippedVertical) -1f else 1f)
                                translationX = panOffset.x
                                translationY = panOffset.y
                                rotationZ = uiState.cropTransform.rotationDegrees + uiState.cropTransform.straightenDegrees
                            }
                    )
                }

                // Press and Hold Compare Banner
                if (uiState.isComparingOriginal) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = "ORIGINAL (HOLD TO COMPARE)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                // Live Floating Mini-Histogram (if enabled)
                if (uiState.isHistogramVisible && uiState.histogramData != null) {
                    FloatingHistogramWidget(
                        histogram = uiState.histogramData!!,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    )
                }
            } else {
                // Empty State
                PhotoStudioEmptyState(
                    onImport = { photoPickerLauncher.launch("image/*") },
                    onSample = { viewModel.loadSampleImage() },
                    onBlank = { viewModel.createBlankCanvas() }
                )
            }

            // Contextual Tool Panels (Tune, Curves, HSL, Crop, Details, Creative, Histogram)
            AnimatedVisibility(
                visible = uiState.activeTool != ActivePhotoTool.NONE,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    color = KreaviloColors.Surface,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(bottom = 12.dp)
                    ) {
                        // Tool Header with Done / Cancel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.closeTool(applyChanges = false) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Outlined.Close, contentDescription = "Cancel", tint = KreaviloColors.TextSecondary)
                            }

                            Text(
                                text = uiState.activeTool.displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = KreaviloColors.TextPrimary
                            )

                            IconButton(
                                onClick = { viewModel.closeTool(applyChanges = true) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Outlined.Check, contentDescription = "Apply", tint = KreaviloColors.Primary)
                            }
                        }

                        HorizontalDivider(color = KreaviloColors.Border)

                        // Tool Content Body
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            when (uiState.activeTool) {
                                ActivePhotoTool.TUNE -> TuneToolPanel(uiState = uiState, viewModel = viewModel)
                                ActivePhotoTool.CURVES -> CurvesToolPanel(uiState = uiState, viewModel = viewModel)
                                ActivePhotoTool.HSL -> HslToolPanel(uiState = uiState, viewModel = viewModel)
                                ActivePhotoTool.CROP -> CropToolPanel(uiState = uiState, viewModel = viewModel)
                                ActivePhotoTool.WHITE_BALANCE -> WhiteBalanceToolPanel(uiState = uiState, viewModel = viewModel)
                                ActivePhotoTool.DETAILS -> DetailsToolPanel(uiState = uiState, viewModel = viewModel)
                                ActivePhotoTool.CREATIVE -> CreativeToolPanel(uiState = uiState, viewModel = viewModel)
                                ActivePhotoTool.HISTOGRAM -> HistogramToolPanel(uiState = uiState, viewModel = viewModel)
                                ActivePhotoTool.NONE -> Unit
                            }
                        }
                    }
                }
            }

            // Export Sheet
            if (uiState.isExportSheetVisible) {
                ExportStudioSheet(
                    uiState = uiState,
                    onUpdateConfig = { viewModel.updateExportConfig(it) },
                    onDismiss = { viewModel.hideExportSheet() },
                    onConfirmExport = { viewModel.exportImage() }
                )
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
                            text = "Photo saved to Gallery at full resolution!",
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

            // Error feedback
            uiState.errorMessage?.let { msg ->
                Surface(
                    color = Color(0xFFB71C1C),
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
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.dismissErrorMessage() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

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
                            text = if (uiState.isExporting) "Rendering full-resolution photo..." else "Loading photo...",
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

// =========================================================================
// LOOKS DRAWER (34 PRESETS + INTENSITY SLIDER)
// =========================================================================

@Composable
fun LooksDrawer(
    selectedFilter: FilterPreset,
    filterIntensity: Float,
    thumbnailBitmap: Bitmap?,
    onSelectFilter: (FilterPreset) -> Unit,
    onIntensityChange: (Float) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = remember { listOf("All", "Essentials", "Cinema", "Classic", "Monochrome", "Creative") }

    val filteredPresets = remember(selectedCategory) {
        if (selectedCategory == "All") FilterPreset.entries
        else FilterPreset.entries.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Preset Intensity Slider (visible when a filter preset != NONE is active)
        if (selectedFilter != FilterPreset.NONE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Strength",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KreaviloColors.TextSecondary,
                    modifier = Modifier.width(56.dp)
                )
                Slider(
                    value = filterIntensity,
                    onValueChange = onIntensityChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = KreaviloColors.Primary,
                        activeTrackColor = KreaviloColors.Primary
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(filterIntensity * 100).roundToInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = KreaviloColors.Primary,
                    modifier = Modifier.width(36.dp)
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
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
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredPresets) { preset ->
                val isSelected = preset == selectedFilter
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectFilter(preset) }
                        .padding(2.dp)
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

// =========================================================================
// TOOLS GRID DRAWER
// =========================================================================

@Composable
fun ToolsGridDrawer(
    onSelectTool: (ActivePhotoTool) -> Unit
) {
    val tools = remember {
        listOf(
            ActivePhotoTool.TUNE,
            ActivePhotoTool.CURVES,
            ActivePhotoTool.HSL,
            ActivePhotoTool.CROP,
            ActivePhotoTool.WHITE_BALANCE,
            ActivePhotoTool.DETAILS,
            ActivePhotoTool.CREATIVE,
            ActivePhotoTool.HISTOGRAM
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tools) { tool ->
            ToolGridItem(tool = tool, onClick = { onSelectTool(tool) })
        }
    }
}

@Composable
fun ToolGridItem(
    tool: ActivePhotoTool,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(KreaviloColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (tool) {
                    ActivePhotoTool.TUNE -> Icons.Outlined.Tune
                    ActivePhotoTool.CURVES -> Icons.AutoMirrored.Outlined.ShowChart
                    ActivePhotoTool.HSL -> Icons.Outlined.Palette
                    ActivePhotoTool.CROP -> Icons.Outlined.Crop
                    ActivePhotoTool.WHITE_BALANCE -> Icons.Outlined.WbSunny
                    ActivePhotoTool.DETAILS -> Icons.Outlined.Details
                    ActivePhotoTool.CREATIVE -> Icons.Outlined.AutoAwesome
                    ActivePhotoTool.HISTOGRAM -> Icons.Outlined.BarChart
                    ActivePhotoTool.NONE -> Icons.Outlined.Tune
                },
                contentDescription = tool.displayName,
                tint = KreaviloColors.Primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tool.displayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = KreaviloColors.TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =========================================================================
// CONTEXTUAL TOOL PANELS
// =========================================================================

@Composable
fun TuneToolPanel(
    uiState: PhotoStudioUiState,
    viewModel: PhotoStudioViewModel
) {
    val currentProp = uiState.activeTuneProperty
    val adj = uiState.adjustments
    val currentValue = when (currentProp) {
        ActiveTuneProperty.BRIGHTNESS -> adj.brightness
        ActiveTuneProperty.CONTRAST -> adj.contrast
        ActiveTuneProperty.SATURATION -> adj.saturation
        ActiveTuneProperty.EXPOSURE -> adj.exposure
        ActiveTuneProperty.WARMTH -> adj.warmth
        ActiveTuneProperty.TINT -> adj.tint
        ActiveTuneProperty.HIGHLIGHTS -> adj.highlights
        ActiveTuneProperty.SHADOWS -> adj.shadows
        ActiveTuneProperty.VIGNETTE -> adj.vignette
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Property Chips Carousel
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ActiveTuneProperty.entries) { prop ->
                val isSelected = prop == currentProp
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectTuneProperty(prop) },
                    label = { Text(prop.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KreaviloColors.Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Property Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(currentProp.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.TextPrimary)
            Text(String.format("%.2f", currentValue), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
        }

        Slider(
            value = currentValue,
            onValueChange = { viewModel.updateTuneValue(currentProp, it) },
            valueRange = currentProp.minVal..currentProp.maxVal,
            colors = SliderDefaults.colors(
                thumbColor = KreaviloColors.Primary,
                activeTrackColor = KreaviloColors.Primary
            )
        )
    }
}

@Composable
fun CurvesToolPanel(
    uiState: PhotoStudioUiState,
    viewModel: PhotoStudioViewModel
) {
    val channel = uiState.activeCurveChannel
    val curves = uiState.adjustments.curves
    val points = curves.getPointsForChannel(channel)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Channel Selector (RGB, Red, Green, Blue)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CurveChannel.entries.forEach { ch ->
                    val isSelected = ch == channel
                    val chColor = when (ch) {
                        CurveChannel.RGB -> Color.DarkGray
                        CurveChannel.RED -> Color(0xFFE53935)
                        CurveChannel.GREEN -> Color(0xFF43A047)
                        CurveChannel.BLUE -> Color(0xFF1E88E5)
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCurveChannel(ch) },
                        label = { Text(ch.name, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            IconButton(
                onClick = { viewModel.resetCurveChannel(channel) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = "Reset Channel", tint = KreaviloColors.TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive Curves Canvas
        val strokeColor = when (channel) {
            CurveChannel.RGB -> Color(0xFF374151)
            CurveChannel.RED -> Color(0xFFE53935)
            CurveChannel.GREEN -> Color(0xFF43A047)
            CurveChannel.BLUE -> Color(0xFF1E88E5)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1F2937))
                .pointerInput(channel) {
                    detectTapGestures { offset ->
                        val normX = (offset.x / size.width).coerceIn(0f, 1f)
                        val normY = (1f - offset.y / size.height).coerceIn(0f, 1f)
                        // Add or adjust control point
                        val mutable = points.toMutableList()
                        // Replace nearest or add
                        val nearestIdx = mutable.indexOfFirst { kotlin.math.abs(it.x - normX) < 0.15f }
                        if (nearestIdx in 1 until mutable.size - 1) {
                            mutable[nearestIdx] = CurvePoint(normX, normY)
                        } else if (nearestIdx == -1 && mutable.size < 5) {
                            mutable.add(CurvePoint(normX, normY))
                            mutable.sortBy { it.x }
                        }
                        viewModel.updateCurvePoints(channel, mutable)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw 3x3 Grid
                for (step in 1..3) {
                    val lineX = w * (step / 4f)
                    val lineY = h * (step / 4f)
                    drawLine(Color.White.copy(alpha = 0.15f), Offset(lineX, 0f), Offset(lineX, h), 1f)
                    drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, lineY), Offset(w, lineY), 1f)
                }

                // Draw Spline Curve
                val sorted = points.sortedBy { it.x }
                if (sorted.isNotEmpty()) {
                    val path = Path()
                    path.moveTo(sorted.first().x * w, (1f - sorted.first().y) * h)
                    for (i in 0 until sorted.size - 1) {
                        val p0 = sorted[i]
                        val p1 = sorted[i + 1]
                        val x0 = p0.x * w
                        val y0 = (1f - p0.y) * h
                        val x1 = p1.x * w
                        val y1 = (1f - p1.y) * h
                        val cx = (x0 + x1) / 2f
                        path.cubicTo(cx, y0, cx, y1, x1, y1)
                    }
                    drawPath(path, strokeColor, style = Stroke(width = 4f))

                    // Draw Control Points
                    for (pt in sorted) {
                        drawCircle(Color.White, radius = 8f, center = Offset(pt.x * w, (1f - pt.y) * h))
                        drawCircle(strokeColor, radius = 5f, center = Offset(pt.x * w, (1f - pt.y) * h))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Tap on grid to add or adjust curve points.", fontSize = 10.sp, color = KreaviloColors.TextTertiary)
    }
}

@Composable
fun HslToolPanel(
    uiState: PhotoStudioUiState,
    viewModel: PhotoStudioViewModel
) {
    val activeBand = uiState.activeHslBand
    val adj = uiState.adjustments.hslAdjustments[activeBand] ?: HslBandAdjustment()

    Column(modifier = Modifier.fillMaxWidth()) {
        // 8 Color Band Circles
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(HslColorBand.entries) { band ->
                val isSelected = band == activeBand
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(band.colorHex))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.White else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { viewModel.selectHslBand(band) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hue Slider
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Hue Shift", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Text("${(adj.hue * 180).roundToInt()}°", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
        }
        Slider(
            value = adj.hue,
            onValueChange = { viewModel.updateHslBand(activeBand, hue = it) },
            valueRange = -1f..1f,
            colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
        )

        // Saturation Slider
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Saturation", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Text("${(adj.saturation * 100).roundToInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
        }
        Slider(
            value = adj.saturation,
            onValueChange = { viewModel.updateHslBand(activeBand, saturation = it) },
            valueRange = -1f..1f,
            colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
        )

        // Luminance Slider
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Luminance", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Text("${(adj.luminance * 100).roundToInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
        }
        Slider(
            value = adj.luminance,
            onValueChange = { viewModel.updateHslBand(activeBand, luminance = it) },
            valueRange = -1f..1f,
            colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
        )
    }
}

@Composable
fun CropToolPanel(
    uiState: PhotoStudioUiState,
    viewModel: PhotoStudioViewModel
) {
    val crop = uiState.cropTransform

    Column(modifier = Modifier.fillMaxWidth()) {
        // Transform Action Buttons (Rotate 90, Flip H, Flip V)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.rotate90() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rotate 90°", fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = { viewModel.flipHorizontal() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Outlined.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Flip H", fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = { viewModel.flipVertical() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Outlined.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Flip V", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Straighten Slider (-45° to +45°)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Straighten", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Text("${crop.straightenDegrees.roundToInt()}°", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
        }
        Slider(
            value = crop.straightenDegrees,
            onValueChange = { viewModel.updateStraighten(it) },
            valueRange = -45f..45f,
            colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Aspect Ratio Selector Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(CropAspectRatio.entries) { ratio ->
                val isSelected = ratio == crop.aspectRatio
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setAspectRatio(ratio) },
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

@Composable
fun WhiteBalanceToolPanel(
    uiState: PhotoStudioUiState,
    viewModel: PhotoStudioViewModel
) {
    val adj = uiState.adjustments
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Temperature (Warmth)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Text(String.format("%.2f", adj.warmth), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
        }
        Slider(
            value = adj.warmth,
            onValueChange = { viewModel.updateTuneValue(ActiveTuneProperty.WARMTH, it) },
            valueRange = -1f..1f,
            colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tint (Green / Magenta)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Text(String.format("%.2f", adj.tint), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
        }
        Slider(
            value = adj.tint,
            onValueChange = { viewModel.updateTuneValue(ActiveTuneProperty.TINT, it) },
            valueRange = -1f..1f,
            colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
        )
    }
}

@Composable
fun DetailsToolPanel(
    uiState: PhotoStudioUiState,
    viewModel: PhotoStudioViewModel
) {
    val details = uiState.adjustments.details
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Sharpening", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Text("${(details.sharpening * 100).roundToInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
        }
        Slider(
            value = details.sharpening,
            onValueChange = { viewModel.updateDetails(sharpening = it, structure = details.structure) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Structure / Clarity", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Text("${(details.structure * 100).roundToInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
        }
        Slider(
            value = details.structure,
            onValueChange = { viewModel.updateDetails(sharpening = details.sharpening, structure = it) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
        )
    }
}

@Composable
fun CreativeToolPanel(
    uiState: PhotoStudioUiState,
    viewModel: PhotoStudioViewModel
) {
    val creative = uiState.adjustments.creative
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Dehaze", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Text("${(creative.dehaze * 100).roundToInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
        }
        Slider(
            value = creative.dehaze,
            onValueChange = { viewModel.updateCreative(dehaze = it) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Film Grain", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Text("${(creative.grain * 100).roundToInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
        }
        Slider(
            value = creative.grain,
            onValueChange = { viewModel.updateCreative(grain = it) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
        )
    }
}

@Composable
fun HistogramToolPanel(
    uiState: PhotoStudioUiState,
    viewModel: PhotoStudioViewModel
) {
    val hist = uiState.histogramData
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Live Overlay", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.TextPrimary)
            Switch(
                checked = uiState.isHistogramVisible,
                onCheckedChange = { viewModel.toggleHistogram() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (hist != null) {
            FloatingHistogramWidget(
                histogram = hist,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        } else {
            Text("Histogram data calculating...", fontSize = 11.sp, color = KreaviloColors.TextSecondary)
        }
    }
}

@Composable
fun FloatingHistogramWidget(
    histogram: HistogramData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(160.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.75f))
            .padding(6.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val max = histogram.maxCount.coerceAtLeast(1)

            fun drawBins(bins: IntArray, color: Color) {
                val step = 256f / w
                val path = Path()
                path.moveTo(0f, h)
                for (x in 0 until w.toInt()) {
                    val binIdx = (x * step).toInt().coerceIn(0, 255)
                    val count = bins[binIdx]
                    val y = h - (count.toFloat() / max * h).coerceIn(0f, h)
                    path.lineTo(x.toFloat(), y)
                }
                path.lineTo(w, h)
                drawPath(path, color.copy(alpha = 0.35f))
            }

            drawBins(histogram.redBins, Color(0xFFEF4444))
            drawBins(histogram.greenBins, Color(0xFF10B981))
            drawBins(histogram.blueBins, Color(0xFF3B82F6))
            drawBins(histogram.lumaBins, Color.White)
        }
    }
}

// =========================================================================
// EXPORT STUDIO SHEET
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportStudioSheet(
    uiState: PhotoStudioUiState,
    onUpdateConfig: (ExportConfig) -> Unit,
    onDismiss: () -> Unit,
    onConfirmExport: () -> Unit
) {
    val config = uiState.exportConfig
    var filename by remember { mutableStateOf(config.filename) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = KreaviloColors.Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Export Image",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KreaviloColors.TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = KreaviloColors.PrimaryContainer
                ) {
                    Text(
                        text = "~${uiState.estimatedFileSizeKb / 1024f} MB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KreaviloColors.Primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filename Input
            OutlinedTextField(
                value = filename,
                onValueChange = {
                    filename = it
                    onUpdateConfig(config.copy(filename = it))
                },
                label = { Text("Filename") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Format Selection (JPEG, PNG, WEBP)
            Text("File Format", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExportFormat.entries.forEach { fmt ->
                    val isSelected = fmt == config.format
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateConfig(config.copy(format = fmt)) },
                        label = { Text(fmt.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KreaviloColors.Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quality Slider (Only for lossy formats JPEG and WEBP)
            if (config.format != ExportFormat.PNG) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Compression Quality", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
                    Text("${config.quality}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.Primary)
                }
                Slider(
                    value = config.quality.toFloat(),
                    onValueChange = { onUpdateConfig(config.copy(quality = it.roundToInt())) },
                    valueRange = 60f..100f,
                    steps = 8,
                    colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Explicit Pixel Resolution Selector
            Text("Target Dimensions (px)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ExportDimension.entries.forEach { dim ->
                    val isSelected = dim == config.dimension
                    val dimensionSubtitle = when (dim) {
                        ExportDimension.ORIGINAL -> "${uiState.originalWidth} × ${uiState.originalHeight} px (Original)"
                        ExportDimension.UHD_4K -> "3840 px Max Width/Height (Ultra HD)"
                        ExportDimension.QHD_1440P -> "2560 px Max Width/Height (Quad HD)"
                        ExportDimension.FHD_1080P -> "1920 px Max Width/Height (Full HD)"
                        ExportDimension.SQUARE_1080 -> "1080 × 1080 px (1:1 Instagram Post)"
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) KreaviloColors.PrimaryContainer else KreaviloColors.SurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUpdateConfig(config.copy(dimension = dim)) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = dim.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) KreaviloColors.Primary else KreaviloColors.TextPrimary
                                )
                                Text(
                                    text = dimensionSubtitle,
                                    fontSize = 11.sp,
                                    color = KreaviloColors.TextSecondary
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { onUpdateConfig(config.copy(dimension = dim)) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy & Metadata Options
            Text("Privacy & Metadata", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Strip GPS Location", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = KreaviloColors.TextPrimary)
                    Text("Removes geolocation tags for privacy", fontSize = 10.sp, color = KreaviloColors.TextSecondary)
                }
                Switch(
                    checked = config.stripLocation,
                    onCheckedChange = { onUpdateConfig(config.copy(stripLocation = it)) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Save CTA Button
            Button(
                onClick = onConfirmExport,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save to Device Gallery (Lossless)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// =========================================================================
// EMPTY STATE
// =========================================================================

@Composable
fun PhotoStudioEmptyState(
    onImport: () -> Unit,
    onSample: () -> Unit,
    onBlank: () -> Unit
) {
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
                text = "Professional Photo Studio",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = KreaviloColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Full-resolution non-destructive image editing with 34 looks, curves, HSL, and direct gallery export.",
                fontSize = 12.sp,
                color = KreaviloColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onImport,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Photo from Gallery", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onSample,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp), tint = KreaviloColors.Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Sample Artwork", color = KreaviloColors.Primary, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = onBlank,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Blank Canvas (1080×1080)", fontSize = 12.sp, color = KreaviloColors.TextSecondary)
            }
        }
    }
}
