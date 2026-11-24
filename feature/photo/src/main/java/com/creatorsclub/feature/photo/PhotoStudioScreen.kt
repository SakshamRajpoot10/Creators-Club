package com.creatorsclub.feature.photo

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
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
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Photo Studio",
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
                    // Compare button
                    IconButton(
                        onClick = { /* Tap toggle */ },
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KreaviloColors.Surface)
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddPhotoAlternate,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Select a photo to begin editing",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyLarge
                    )
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
    onSelectFilter: (FilterPreset) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(FilterPreset.entries) { preset ->
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
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) KreaviloColors.PrimaryContainer else KreaviloColors.SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterFrames,
                        contentDescription = preset.displayName,
                        tint = if (isSelected) KreaviloColors.Primary else KreaviloColors.TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = preset.displayName,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) KreaviloColors.Primary else KreaviloColors.TextPrimary
                )
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = KreaviloColors.TextPrimary,
            modifier = Modifier.width(80.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = KreaviloColors.Primary,
                activeTrackColor = KreaviloColors.Primary
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = String.format("%.2f", value),
            fontSize = 11.sp,
            color = KreaviloColors.TextSecondary,
            modifier = Modifier.width(42.dp)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onRotate,
                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.SurfaceVariant),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, tint = KreaviloColors.TextPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rotate 90°", color = KreaviloColors.TextPrimary, fontSize = 12.sp)
            }

            Button(
                onClick = onFlip,
                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.SurfaceVariant),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Flip, contentDescription = null, tint = KreaviloColors.TextPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Flip H", color = KreaviloColors.TextPrimary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
