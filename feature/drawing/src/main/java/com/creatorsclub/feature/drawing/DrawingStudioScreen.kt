package com.creatorsclub.feature.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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
    var canvasSize by remember { mutableStateOf(IntSize(1, 1)) }
    var showBrushSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Drawing Studio",
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
                    // Brush settings toggle
                    IconButton(onClick = { showBrushSettings = !showBrushSettings }) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Brush Size & Opacity",
                            tint = if (showBrushSettings) KreaviloColors.Primary else KreaviloColors.TextSecondary
                        )
                    }

                    // Background Transparency Toggle
                    IconButton(onClick = { viewModel.setTransparentBackground(!uiState.isTransparentBackground) }) {
                        Icon(
                            imageVector = if (uiState.isTransparentBackground) Icons.Filled.GridOn else Icons.Outlined.CropSquare,
                            contentDescription = "Transparent PNG Mode",
                            tint = if (uiState.isTransparentBackground) KreaviloColors.Primary else KreaviloColors.TextSecondary
                        )
                    }

                    // Clear Canvas
                    IconButton(onClick = { viewModel.clearCanvas() }) {
                        Icon(Icons.Outlined.DeleteSweep, contentDescription = "Clear Canvas", tint = KreaviloColors.Error)
                    }

                    // Undo
                    IconButton(onClick = { viewModel.undo() }, enabled = uiState.canUndo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = if (uiState.canUndo) KreaviloColors.TextPrimary else KreaviloColors.TextTertiary)
                    }

                    // Redo
                    IconButton(onClick = { viewModel.redo() }, enabled = uiState.canRedo) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = if (uiState.canRedo) KreaviloColors.TextPrimary else KreaviloColors.TextTertiary)
                    }

                    // Export
                    Button(
                        onClick = { viewModel.exportDrawing() },
                        enabled = !uiState.isLoading && uiState.strokes.isNotEmpty(),
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
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Brush Size & Opacity Panel (when toggled)
                if (showBrushSettings) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Brush Size: ${uiState.strokeWidth.toInt()} px", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
                            Text("Opacity: ${(uiState.strokeAlpha * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Slider(
                                value = uiState.strokeWidth,
                                onValueChange = { viewModel.setStrokeWidth(it) },
                                valueRange = 2f..70f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
                            )
                            Slider(
                                value = uiState.strokeAlpha,
                                onValueChange = { viewModel.setStrokeAlpha(it) },
                                valueRange = 0.05f..1f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = KreaviloColors.Accent, activeTrackColor = KreaviloColors.Accent)
                            )
                        }
                        HorizontalDivider(color = KreaviloColors.BorderLight, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                // Symmetry Mode Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Symmetry: ${uiState.symmetryMode.displayName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KreaviloColors.TextSecondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = uiState.symmetryMode == SymmetryMode.NONE,
                            onClick = { viewModel.setSymmetryMode(SymmetryMode.NONE) },
                            label = { Text("Off", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = uiState.symmetryMode == SymmetryMode.VERTICAL_MIRROR,
                            onClick = { viewModel.setSymmetryMode(SymmetryMode.VERTICAL_MIRROR) },
                            label = { Text("Mirror", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = uiState.symmetryMode == SymmetryMode.RADIAL_MANDALA,
                            onClick = { viewModel.setSymmetryMode(SymmetryMode.RADIAL_MANDALA) },
                            label = { Text("8x Mandala", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KreaviloColors.Accent,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Brush selection row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(BrushType.entries) { brush ->
                        val isSelected = brush == uiState.selectedBrush
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectBrush(brush) },
                            label = { Text(brush.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KreaviloColors.Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Color picker palette
                val colors = listOf(
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

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colors) { color ->
                        val isSelected = uiState.brushColor == color
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { viewModel.setBrushColor(color) }
                                .then(
                                    if (isSelected) Modifier.clip(CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = if (color == Color.White) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
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
                .background(if (uiState.isTransparentBackground) Color(0xFFF0F2F5) else Color.White)
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            viewModel.startStroke(offset.x, offset.y)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            viewModel.appendPoint(change.position.x, change.position.y)
                        },
                        onDragEnd = {
                            viewModel.endStroke(canvasSize.width.toFloat(), canvasSize.height.toFloat())
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw completed strokes
                for (stroke in uiState.strokes) {
                    if (stroke.points.size < 2) continue
                    val path = Path().apply {
                        moveTo(stroke.points[0].x, stroke.points[0].y)
                        for (i in 1 until stroke.points.size) {
                            lineTo(stroke.points[i].x, stroke.points[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = stroke.color.copy(alpha = stroke.alpha),
                        style = Stroke(
                            width = stroke.strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Draw active stroke currently being drawn
                uiState.currentStroke?.let { active ->
                    if (active.points.size >= 2) {
                        val activePath = Path().apply {
                            moveTo(active.points[0].x, active.points[0].y)
                            for (i in 1 until active.points.size) {
                                lineTo(active.points[i].x, active.points[i].y)
                            }
                        }
                        drawPath(
                            path = activePath,
                            color = active.color.copy(alpha = active.alpha),
                            style = Stroke(
                                width = active.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // Export Success Feedback
            uiState.exportSuccessUri?.let {
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
                            text = "Artwork saved to Gallery (Lossless PNG)!",
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
                CircularProgressIndicator(
                    color = KreaviloColors.Primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
