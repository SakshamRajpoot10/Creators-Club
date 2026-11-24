package com.creatorsclub.feature.design

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors
import com.creatorsclub.engine.canvas.model.DesignTemplate
import com.creatorsclub.engine.canvas.model.ShapeType
import com.creatorsclub.engine.canvas.ui.InteractiveDesignCanvas
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignStudioScreen(
    viewModel: DesignStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
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
                                text = "Design Studio",
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

                            // Delete selected element OR move project to Trash
                            if (uiState.selectedElementId != null) {
                                IconButton(onClick = { viewModel.deleteSelectedElement() }, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Delete Element", tint = KreaviloColors.Error)
                                }
                            } else {
                                IconButton(onClick = { viewModel.setShowTrashConfirmDialog(true) }, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Move to Trash", tint = KreaviloColors.Error)
                                }
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
                                onClick = { viewModel.exportDesign() },
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
                // Secondary Controls Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    when (uiState.activeTab) {
                        DesignStudioTab.TEMPLATES -> TemplatesCarousel(
                            templates = uiState.availableTemplates,
                            onSelectTemplate = { viewModel.loadTemplate(it) }
                        )
                        DesignStudioTab.TEXT -> TextControlsRow(
                            onAddHeading = { viewModel.addText("Add Heading", isHeading = true) },
                            onAddSubheading = { viewModel.addText("Add Subheading", isHeading = false) },
                            onAddBody = { viewModel.addText("Add body text here...", isHeading = false) }
                        )
                        DesignStudioTab.SHAPES -> ShapeControlsRow(
                            onAddShape = { shapeType, color -> viewModel.addShape(shapeType, color) }
                        )
                        DesignStudioTab.STICKERS -> StickersControlsRow(
                            importedStickers = uiState.importedStickers,
                            onAddBadge = { label, color -> viewModel.addSticker(label, color) },
                            onAddImportedSticker = { filePath -> viewModel.addImageElement(filePath) },
                            onImportNewSticker = { uri -> viewModel.addImageFromUri(uri) }
                        )
                        DesignStudioTab.BACKGROUND -> BackgroundColorPickerRow(
                            onSelectColor = { viewModel.setBackgroundColor(it) }
                        )
                    }
                }

                HorizontalDivider(color = KreaviloColors.Border)

                // Main Studio Tabs: Replaced broken TabRow with ScrollableTabRow to prevent Templ/ates and Eleme/nts word breaking
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
                    Tab(
                        selected = uiState.activeTab == DesignStudioTab.TEMPLATES,
                        onClick = { viewModel.selectTab(DesignStudioTab.TEMPLATES) },
                        text = { Text("Templates", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                        icon = { Icon(Icons.Outlined.GridView, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = uiState.activeTab == DesignStudioTab.TEXT,
                        onClick = { viewModel.selectTab(DesignStudioTab.TEXT) },
                        text = { Text("Text", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                        icon = { Icon(Icons.Outlined.TextFields, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = uiState.activeTab == DesignStudioTab.SHAPES,
                        onClick = { viewModel.selectTab(DesignStudioTab.SHAPES) },
                        text = { Text("Elements", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                        icon = { Icon(Icons.Outlined.Category, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = uiState.activeTab == DesignStudioTab.STICKERS,
                        onClick = { viewModel.selectTab(DesignStudioTab.STICKERS) },
                        text = { Text("Stickers", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                        icon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = uiState.activeTab == DesignStudioTab.BACKGROUND,
                        onClick = { viewModel.selectTab(DesignStudioTab.BACKGROUND) },
                        text = { Text("Background", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                        icon = { Icon(Icons.Outlined.ColorLens, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F121C)), // Dark backdrop for visual canvas focus
            contentAlignment = Alignment.Center
        ) {
            // Interactive canvas with true document coordinate system (fixes (0,0) pile-up)
            InteractiveDesignCanvas(
                elements = uiState.elements,
                selectedElementId = uiState.selectedElementId,
                backgroundColor = uiState.backgroundColor,
                documentWidth = uiState.canvasWidth,
                documentHeight = uiState.canvasHeight,
                onSelectElement = { viewModel.selectElement(it) },
                onDoubleTapElement = { viewModel.startEditingText(it) },
                onUpdateElementTransform = { id, x, y -> viewModel.updateElementPosition(id, x, y) }
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(color = KreaviloColors.Primary)
            }
        }
    }

    // --- Inline Text Editor Dialog ---
    uiState.editingTextElement?.let { textElement ->
        var currentText by remember(textElement.id) { mutableStateOf(textElement.text) }
        var currentSize by remember(textElement.id) { mutableFloatStateOf(textElement.fontSizeSp) }
        var isBold by remember(textElement.id) { mutableStateOf(textElement.isBold) }
        var currentColor by remember(textElement.id) { mutableStateOf(textElement.color) }

        AlertDialog(
            onDismissRequest = { viewModel.dismissTextEditing() },
            title = { Text("Edit Text", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentText,
                        onValueChange = { currentText = it },
                        label = { Text("Text Content") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Font Size: ${currentSize.toInt()} sp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = currentSize,
                        onValueChange = { currentSize = it },
                        valueRange = 14f..96f,
                        colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bold Font", fontSize = 13.sp)
                        Switch(
                            checked = isBold,
                            onCheckedChange = { isBold = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = KreaviloColors.Primary)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateEditingText(currentText, currentSize, isBold, currentColor)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary)
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissTextEditing() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Trash Confirmation Dialog ---
    if (uiState.showTrashConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowTrashConfirmDialog(false) },
            title = { Text("Move Project to Trash?", fontWeight = FontWeight.Bold) },
            text = { Text("This design will be moved to Projects → Trash and retained for 5 days before permanent cleanup.") },
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
}

@Composable
fun TemplatesCarousel(
    templates: List<DesignTemplate>,
    onSelectTemplate: (DesignTemplate) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(templates) { tmpl ->
            Card(
                modifier = Modifier
                    .width(150.dp)
                    .height(72.dp)
                    .clickable { onSelectTemplate(tmpl) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.SurfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = tmpl.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KreaviloColors.TextPrimary,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tmpl.category.displayName,
                        fontSize = 9.sp,
                        color = KreaviloColors.Primary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun TextControlsRow(
    onAddHeading: () -> Unit,
    onAddSubheading: () -> Unit,
    onAddBody: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onAddHeading,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.PrimaryContainer),
            modifier = Modifier.weight(1f)
        ) {
            Text("Heading", fontSize = 12.sp, color = KreaviloColors.Primary, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onAddSubheading,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.SurfaceVariant),
            modifier = Modifier.weight(1f)
        ) {
            Text("Subheading", fontSize = 11.sp, color = KreaviloColors.TextPrimary, fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onAddBody,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.SurfaceVariant),
            modifier = Modifier.weight(1f)
        ) {
            Text("Body text", fontSize = 11.sp, color = KreaviloColors.TextPrimary)
        }
    }
}

@Composable
fun ShapeControlsRow(
    onAddShape: (ShapeType, Color) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onAddShape(ShapeType.ROUNDED_RECTANGLE, KreaviloColors.Primary) },
            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Filled.CropSquare, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Card", fontSize = 12.sp)
        }

        Button(
            onClick = { onAddShape(ShapeType.CIRCLE, KreaviloColors.Accent) },
            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Accent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Filled.Circle, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Circle", fontSize = 12.sp)
        }

        Button(
            onClick = { onAddShape(ShapeType.BADGE_STAR, Color(0xFFF59E0B)) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Badge", fontSize = 12.sp)
        }
    }
}

@Composable
fun StickersControlsRow(
    importedStickers: List<File>,
    onAddBadge: (String, Color) -> Unit,
    onAddImportedSticker: (String) -> Unit,
    onImportNewSticker: (android.net.Uri) -> Unit
) {
    val stickerPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { onImportNewSticker(it) }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            OutlinedButton(
                onClick = { stickerPicker.launch("image/*") },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Import Asset", fontSize = 11.sp)
            }
        }
        item {
            Button(
                onClick = { onAddBadge("BESTSELLER", Color(0xFFB83C49)) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB83C49)),
                modifier = Modifier.height(34.dp)
            ) {
                Text("Bestseller", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Button(
                onClick = { onAddBadge("NEW ARRIVAL", Color(0xFF237A57)) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF237A57)),
                modifier = Modifier.height(34.dp)
            ) {
                Text("New Arrival", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        items(importedStickers) { file ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = KreaviloColors.SurfaceVariant,
                modifier = Modifier
                    .height(34.dp)
                    .clickable { onAddImportedSticker(file.absolutePath) }
            ) {
                Text(
                    text = file.nameWithoutExtension.take(10),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun BackgroundColorPickerRow(
    onSelectColor: (Color) -> Unit
) {
    val colors = listOf(
        Color.White,
        Color(0xFFF7F8FC),
        Color(0xFF405BC7),
        Color(0xFF7664C7),
        Color(0xFF182033),
        Color(0xFF237A57),
        Color(0xFFB83C49),
        Color(0xFFFEF3C7)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(colors) { color ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color(0xFFD1D5DB), CircleShape)
                    .clickable { onSelectColor(color) }
            )
        }
    }
}
