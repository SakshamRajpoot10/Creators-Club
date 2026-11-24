package com.creatorsclub.feature.drawing

import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingStudioScreen(
    viewModel: DrawingStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var canvasSize by remember { mutableStateOf(IntSize(1080, 1080)) }

    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Auto-navigate back if project was soft-deleted to Trash
    LaunchedEffect(uiState.isProjectTrashed) {
        if (uiState.isProjectTrashed) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = KreaviloColors.Surface,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
                    // Row A: Back, Title, Favorite, Trash
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNavigateBack, modifier = Modifier.size(44.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = KreaviloColors.TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Drawing Studio",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = KreaviloColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Favorite Star
                            IconButton(onClick = { viewModel.toggleFavorite() }, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Favorite",
                                    tint = if (uiState.isFavorite) Color(0xFFFFB300) else KreaviloColors.TextSecondary
                                )
                            }

                            // Move to Trash
                            IconButton(onClick = { viewModel.setShowTrashConfirmDialog(true) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Move to Trash", tint = KreaviloColors.Error)
                            }

                            // Undo
                            IconButton(onClick = { viewModel.undo() }, enabled = uiState.canUndo, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = "Undo",
                                    tint = if (uiState.canUndo) KreaviloColors.TextPrimary else KreaviloColors.TextTertiary
                                )
                            }

                            // Redo
                            IconButton(onClick = { viewModel.redo() }, enabled = uiState.canRedo, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Redo,
                                    contentDescription = "Redo",
                                    tint = if (uiState.canRedo) KreaviloColors.TextPrimary else KreaviloColors.TextTertiary
                                )
                            }

                            // Export Button
                            Button(
                                onClick = { viewModel.exportDrawing() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KreaviloColors.Surface)
                    .navigationBarsPadding()
            ) {
                // Secondary Controls Area based on selected Tab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    when (uiState.activeTab) {
                        DrawingToolTab.BRUSHES -> {
                            BrushControlsPanel(
                                selectedBrush = uiState.selectedBrush,
                                strokeWidth = uiState.strokeWidth,
                                strokeAlpha = uiState.strokeAlpha,
                                onSelectBrush = { viewModel.selectBrush(it) },
                                onWidthChange = { viewModel.setStrokeWidth(it) },
                                onAlphaChange = { viewModel.setStrokeAlpha(it) }
                            )
                        }
                        DrawingToolTab.SYMMETRY -> {
                            SymmetryControlsPanel(
                                currentMode = uiState.symmetryMode,
                                onSelectMode = { viewModel.setSymmetryMode(it) }
                            )
                        }
                        DrawingToolTab.SHAPES -> {
                            ShapesControlsPanel(
                                selectedShape = uiState.selectedShapeType,
                                onSelectShape = { viewModel.selectShapeType(it) },
                                onAddSampleShape = {
                                    viewModel.addShape(
                                        uiState.selectedShapeType,
                                        canvasSize.width * 0.25f,
                                        canvasSize.height * 0.25f,
                                        canvasSize.width * 0.75f,
                                        canvasSize.height * 0.75f
                                    )
                                }
                            )
                        }
                        DrawingToolTab.LAYERS -> {
                            LayersControlsPanel(
                                layers = uiState.layers,
                                activeLayerId = uiState.activeLayerId,
                                onAddLayer = { viewModel.addLayer() },
                                onSelectLayer = { viewModel.selectLayer(it) },
                                onToggleVisibility = { viewModel.toggleLayerVisibility(it) },
                                onToggleLock = { viewModel.toggleLayerLock(it) },
                                onOpacityChange = { id, op -> viewModel.setLayerOpacity(id, op) },
                                onDeleteLayer = { viewModel.deleteLayer(it) }
                            )
                        }
                        DrawingToolTab.COLOR -> {
                            ColorAndCanvasControlsPanel(
                                currentColor = uiState.brushColor,
                                isTransparent = uiState.isTransparentBackground,
                                onSelectColor = { viewModel.setBrushColor(it) },
                                onToggleTransparent = { viewModel.setTransparentBackground(it) },
                                onClearCanvas = { viewModel.setShowClearCanvasDialog(true) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = KreaviloColors.Border)

                // Main Drawing Studio Tab Navigation (Single-line, horizontally scrollable)
                ScrollableTabRow(
                    selectedTabIndex = uiState.activeTab.ordinal,
                    containerColor = KreaviloColors.Surface,
                    contentColor = KreaviloColors.Primary,
                    edgePadding = 12.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.activeTab.ordinal]),
                            color = KreaviloColors.Primary
                        )
                    }
                ) {
                    DrawingToolTab.entries.forEach { tab ->
                        val isSelected = uiState.activeTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            },
                            icon = {
                                val icon = when (tab) {
                                    DrawingToolTab.BRUSHES -> Icons.Outlined.Brush
                                    DrawingToolTab.SYMMETRY -> Icons.Outlined.Grain
                                    DrawingToolTab.SHAPES -> Icons.Outlined.Category
                                    DrawingToolTab.LAYERS -> Icons.Outlined.Layers
                                    DrawingToolTab.COLOR -> Icons.Outlined.Palette
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
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
                .background(Color(0xFF0F121C)), // Dark neutral canvas backdrop
            contentAlignment = Alignment.Center
        ) {
            // Visibly bounded dominant canvas
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .onSizeChanged { canvasSize = it },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isTransparentBackground) Color.Transparent else Color.White
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF333A4D))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (uiState.isTransparentBackground) {
                                Modifier.background(Color.White) // subtle clean back
                            } else Modifier
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                zoomScale = (zoomScale * zoom).coerceIn(1f, 4f)
                                panOffset = if (zoomScale > 1f) {
                                    Offset(panOffset.x + pan.x, panOffset.y + pan.y)
                                } else Offset.Zero
                            }
                        }
                        .pointerInput(uiState.selectedBrush, uiState.symmetryMode) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val docX = (offset.x - panOffset.x) / zoomScale
                                    val docY = (offset.y - panOffset.y) / zoomScale
                                    viewModel.startStroke(docX, docY)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val docX = (change.position.x - panOffset.x) / zoomScale
                                    val docY = (change.position.y - panOffset.y) / zoomScale
                                    viewModel.appendPoint(docX, docY)
                                },
                                onDragEnd = {
                                    viewModel.endStroke(canvasSize.width.toFloat(), canvasSize.height.toFloat())
                                }
                            )
                        }
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = panOffset.x
                                translationY = panOffset.y
                            }
                    ) {
                        // Render layers in sequence (bottom to top)
                        for (layer in uiState.layers) {
                            if (!layer.isVisible) continue
                            val layerAlpha = layer.opacity

                            // Render layer shapes
                            for (shape in layer.shapes) {
                                if (shape.points.size < 2) continue
                                val p1 = shape.points[0]
                                val p2 = shape.points[1]

                                val left = minOf(p1.x, p2.x)
                                val top = minOf(p1.y, p2.y)
                                val width = maxOf(1f, kotlin.math.abs(p2.x - p1.x))
                                val height = maxOf(1f, kotlin.math.abs(p2.y - p1.y))

                                when (shape.shapeType) {
                                    DrawingShapeType.LINE -> {
                                        drawLine(
                                            color = shape.strokeColor,
                                            start = Offset(p1.x, p1.y),
                                            end = Offset(p2.x, p2.y),
                                            strokeWidth = shape.strokeWidth,
                                            cap = StrokeCap.Round,
                                            alpha = shape.alpha * layerAlpha
                                        )
                                    }
                                    DrawingShapeType.RECTANGLE, DrawingShapeType.SQUARE -> {
                                        drawRect(
                                            color = shape.strokeColor,
                                            topLeft = Offset(left, top),
                                            size = Size(width, height),
                                            style = Stroke(width = shape.strokeWidth),
                                            alpha = shape.alpha * layerAlpha
                                        )
                                    }
                                    DrawingShapeType.ROUNDED_RECT -> {
                                        drawRoundRect(
                                            color = shape.strokeColor,
                                            topLeft = Offset(left, top),
                                            size = Size(width, height),
                                            cornerRadius = CornerRadius(shape.cornerRadius, shape.cornerRadius),
                                            style = Stroke(width = shape.strokeWidth),
                                            alpha = shape.alpha * layerAlpha
                                        )
                                    }
                                    DrawingShapeType.CIRCLE, DrawingShapeType.ELLIPSE -> {
                                        drawOval(
                                            color = shape.strokeColor,
                                            topLeft = Offset(left, top),
                                            size = Size(width, height),
                                            style = Stroke(width = shape.strokeWidth),
                                            alpha = shape.alpha * layerAlpha
                                        )
                                    }
                                    else -> {
                                        drawLine(
                                            color = shape.strokeColor,
                                            start = Offset(p1.x, p1.y),
                                            end = Offset(p2.x, p2.y),
                                            strokeWidth = shape.strokeWidth,
                                            cap = StrokeCap.Round,
                                            alpha = shape.alpha * layerAlpha
                                        )
                                    }
                                }
                            }

                            // Render layer strokes
                            for (stroke in layer.strokes) {
                                renderSingleStroke(stroke, layerAlpha)
                            }
                        }

                        // Render active/in-progress stroke
                        uiState.currentStroke?.let { stroke ->
                            renderSingleStroke(stroke, 1f)
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    if (uiState.showTrashConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowTrashConfirmDialog(false) },
            title = { Text("Move Project to Trash?", fontWeight = FontWeight.Bold) },
            text = { Text("This drawing will be moved to Projects → Trash and retained for 5 days before permanent cleanup.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.trashProject() },
                    colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Error)
                ) {
                    Text("Move to Trash")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowTrashConfirmDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showClearCanvasDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowClearCanvasDialog(false) },
            title = { Text("Clear Canvas?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to erase all strokes and shapes on the canvas? This action can still be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearCanvas() },
                    colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowClearCanvasDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.renderSingleStroke(
    stroke: DrawingStroke,
    layerAlpha: Float
) {
    if (stroke.points.size < 2) return

    val path = Path()
    path.moveTo(stroke.points[0].x, stroke.points[0].y)
    for (i in 1 until stroke.points.size) {
        path.lineTo(stroke.points[i].x, stroke.points[i].y)
    }

    val cap = when (stroke.brushType) {
        BrushType.PENCIL -> StrokeCap.Round
        BrushType.CALLIGRAPHY -> StrokeCap.Square
        BrushType.HIGHLIGHTER -> StrokeCap.Square
        else -> StrokeCap.Round
    }

    drawPath(
        path = path,
        color = stroke.color,
        alpha = stroke.alpha * layerAlpha,
        style = Stroke(
            width = stroke.strokeWidth,
            cap = cap,
            join = StrokeJoin.Round
        )
    )
}

// --- Tool Panels ---

@Composable
private fun BrushControlsPanel(
    selectedBrush: BrushType,
    strokeWidth: Float,
    strokeAlpha: Float,
    onSelectBrush: (BrushType) -> Unit,
    onWidthChange: (Float) -> Unit,
    onAlphaChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(BrushType.entries) { brush ->
                val isSelected = brush == selectedBrush
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectBrush(brush) },
                    label = {
                        Text(
                            text = brush.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KreaviloColors.Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Size: ${strokeWidth.toInt()} px", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
                Slider(
                    value = strokeWidth,
                    onValueChange = onWidthChange,
                    valueRange = 2f..70f,
                    colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Opacity: ${(strokeAlpha * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
                Slider(
                    value = strokeAlpha,
                    onValueChange = onAlphaChange,
                    valueRange = 0.05f..1f,
                    colors = SliderDefaults.colors(thumbColor = KreaviloColors.Accent, activeTrackColor = KreaviloColors.Accent)
                )
            }
        }
    }
}

/**
 * Solves the P0 letter-by-letter wrapping 8x Mandala bug!
 * Strictly horizontal scrolling row with [Off] [Vertical] [Horizontal] [4x] [6x] [8x] [12x].
 */
@Composable
private fun SymmetryControlsPanel(
    currentMode: SymmetryMode,
    onSelectMode: (SymmetryMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Symmetry: ${currentMode.displayName}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = KreaviloColors.Primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(SymmetryMode.entries) { mode ->
                val isSelected = mode == currentMode
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectMode(mode) },
                    label = {
                        Text(
                            text = mode.chipLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (mode.isRadial) KreaviloColors.Accent else KreaviloColors.Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun ShapesControlsPanel(
    selectedShape: DrawingShapeType,
    onSelectShape: (DrawingShapeType) -> Unit,
    onAddSampleShape: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Shapes (${selectedShape.displayName})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.TextPrimary)
            OutlinedButton(
                onClick = onAddSampleShape,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Place on Canvas", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(DrawingShapeType.entries) { shape ->
                val isSelected = shape == selectedShape
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectShape(shape) },
                    label = {
                        Text(
                            text = shape.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    },
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
private fun LayersControlsPanel(
    layers: List<DrawingLayer>,
    activeLayerId: String,
    onAddLayer: () -> Unit,
    onSelectLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onOpacityChange: (String, Float) -> Unit,
    onDeleteLayer: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Layers (${layers.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.TextPrimary)
            Button(
                onClick = onAddLayer,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Layer", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(layers) { layer ->
                val isSelected = layer.id == activeLayerId
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) KreaviloColors.PrimaryContainer else KreaviloColors.SurfaceVariant,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, KreaviloColors.Primary) else null,
                    modifier = Modifier.clickable { onSelectLayer(layer.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = layer.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) KreaviloColors.Primary else KreaviloColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Eye icon
                        IconButton(onClick = { onToggleVisibility(layer.id) }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = if (layer.isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = if (layer.isVisible) KreaviloColors.TextPrimary else KreaviloColors.TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Delete layer (only if > 1 layer)
                        if (layers.size > 1) {
                            IconButton(onClick = { onDeleteLayer(layer.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Delete",
                                    tint = KreaviloColors.Error,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorAndCanvasControlsPanel(
    currentColor: Color,
    isTransparent: Boolean,
    onSelectColor: (Color) -> Unit,
    onToggleTransparent: (Boolean) -> Unit,
    onClearCanvas: () -> Unit
) {
    val palette = listOf(
        Color(0xFF182033),
        Color(0xFF405BC7),
        Color(0xFF7664C7),
        Color(0xFF237A57),
        Color(0xFFB83C49),
        Color(0xFFF59E0B),
        Color(0xFFEC4899),
        Color(0xFF06B6D4),
        Color(0xFF8B5CF6),
        Color(0xFFE11D48),
        Color(0xFFFFFFFF)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(palette) { color ->
                    val isSelected = currentColor == color
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) KreaviloColors.Primary else Color(0xFFD1D5DB),
                                shape = CircleShape
                            )
                            .clickable { onSelectColor(color) }
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Clear Canvas button
            OutlinedButton(
                onClick = onClearCanvas,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KreaviloColors.Error),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Outlined.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Transparent PNG Canvas",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = KreaviloColors.TextPrimary
            )
            Switch(
                checked = isTransparent,
                onCheckedChange = onToggleTransparent,
                colors = SwitchDefaults.colors(checkedThumbColor = KreaviloColors.Primary)
            )
        }
    }
}
