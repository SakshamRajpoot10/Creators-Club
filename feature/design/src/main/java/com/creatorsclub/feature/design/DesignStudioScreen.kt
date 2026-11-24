package com.creatorsclub.feature.design

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors
import com.creatorsclub.engine.canvas.model.DesignTemplate
import com.creatorsclub.engine.canvas.model.ShapeType
import com.creatorsclub.engine.canvas.ui.InteractiveDesignCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignStudioScreen(
    viewModel: DesignStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Design Studio",
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
                    if (uiState.selectedElementId != null) {
                        IconButton(onClick = { viewModel.deleteSelectedElement() }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = KreaviloColors.Error)
                        }
                    }

                    IconButton(onClick = { viewModel.undo() }, enabled = uiState.canUndo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = if (uiState.canUndo) KreaviloColors.TextPrimary else KreaviloColors.TextTertiary)
                    }

                    IconButton(onClick = { viewModel.redo() }, enabled = uiState.canRedo) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = if (uiState.canRedo) KreaviloColors.TextPrimary else KreaviloColors.TextTertiary)
                    }

                    Button(
                        onClick = { viewModel.exportDesign() },
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
                // Secondary Controls Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
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
                            onAddImportedSticker = { filePath -> viewModel.addImageElement(filePath) }
                        )
                        DesignStudioTab.BACKGROUND -> BackgroundColorPickerRow(
                            onSelectColor = { viewModel.setBackgroundColor(it) }
                        )
                    }
                }

                HorizontalDivider(color = KreaviloColors.Border)

                // Main Studio Tabs
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
                        selected = uiState.activeTab == DesignStudioTab.TEMPLATES,
                        onClick = { viewModel.selectTab(DesignStudioTab.TEMPLATES) },
                        text = { Text("Templates", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Outlined.GridView, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = uiState.activeTab == DesignStudioTab.TEXT,
                        onClick = { viewModel.selectTab(DesignStudioTab.TEXT) },
                        text = { Text("Text", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Outlined.TextFields, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = uiState.activeTab == DesignStudioTab.SHAPES,
                        onClick = { viewModel.selectTab(DesignStudioTab.SHAPES) },
                        text = { Text("Elements", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Outlined.Category, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = uiState.activeTab == DesignStudioTab.STICKERS,
                        onClick = { viewModel.selectTab(DesignStudioTab.STICKERS) },
                        text = { Text("Stickers", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = uiState.activeTab == DesignStudioTab.BACKGROUND,
                        onClick = { viewModel.selectTab(DesignStudioTab.BACKGROUND) },
                        text = { Text("Background", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Outlined.ColorLens, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE5E7EB)),
            contentAlignment = Alignment.Center
        ) {
            // Interactive 340x340 dp canvas preview container
            InteractiveDesignCanvas(
                elements = uiState.elements,
                selectedElementId = uiState.selectedElementId,
                backgroundColor = uiState.backgroundColor,
                canvasWidthDp = 340,
                canvasHeightDp = 340,
                onSelectElement = { viewModel.selectElement(it) },
                onUpdateElementTransform = { id, x, y -> viewModel.updateElementPosition(id, x, y) }
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(color = KreaviloColors.Primary)
            }
        }
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
                    .width(140.dp)
                    .height(70.dp)
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
                        color = KreaviloColors.Primary
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
                    .clickable { onSelectColor(color) }
            )
        }
    }
}

@Composable
fun StickersControlsRow(
    importedStickers: List<java.io.File>,
    onAddBadge: (String, Color) -> Unit,
    onAddImportedSticker: (String) -> Unit
) {
    val curatedBadges = listOf(
        Pair("🔥 TRENDING", Color(0xFFEF4444)),
        Pair("⭐ BEST VALUE", Color(0xFFF59E0B)),
        Pair("🏷️ 50% OFF", Color(0xFF10B981)),
        Pair("✨ NEW ARRIVAL", Color(0xFF6366F1)),
        Pair("🚀 CREATOR CLUB", Color(0xFF405BC7)),
        Pair("⚡ LIMITED", Color(0xFF8B5CF6))
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(curatedBadges) { (label, color) ->
            Button(
                onClick = { onAddBadge(label, color) },
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        items(importedStickers) { stickerFile ->
            Card(
                modifier = Modifier
                    .height(38.dp)
                    .clickable { onAddImportedSticker(stickerFile.absolutePath) },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.SurfaceVariant)
            ) {
                Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                    Text("📁 ${stickerFile.name.take(12)}", fontSize = 11.sp, color = KreaviloColors.TextPrimary)
                }
            }
        }
    }
}
