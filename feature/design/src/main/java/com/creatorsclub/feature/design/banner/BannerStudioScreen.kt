package com.creatorsclub.feature.design.banner

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors
import com.creatorsclub.engine.canvas.ui.InteractiveDesignCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BannerStudioScreen(
    viewModel: BannerStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addImage(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Banner & Thumbnail Studio",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = KreaviloColors.TextPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${uiState.activePreset.title} (${uiState.activePreset.width}x${uiState.activePreset.height})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = KreaviloColors.TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = KreaviloColors.TextPrimary)
                    }
                },
                actions = {
                    // Safe Zone Toggle Button
                    IconButton(onClick = { viewModel.toggleSafeZoneGuides() }) {
                        Icon(
                            imageVector = if (uiState.showSafeZoneGuides) Icons.Filled.GridOn else Icons.Outlined.GridOff,
                            contentDescription = "Toggle Safe Zone Guides",
                            tint = if (uiState.showSafeZoneGuides) KreaviloColors.Primary else KreaviloColors.TextSecondary
                        )
                    }

                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.canUndo
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (uiState.canUndo) KreaviloColors.TextPrimary else KreaviloColors.Border
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = uiState.canRedo
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (uiState.canRedo) KreaviloColors.TextPrimary else KreaviloColors.Border
                        )
                    }

                    // Export Button
                    Button(
                        onClick = { viewModel.exportBanner() },
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreaviloColors.Surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(KreaviloColors.Background)
                .navigationBarsPadding()
        ) {
            // Feedback Card
            uiState.statusMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = KreaviloColors.PrimaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = msg, fontSize = 12.sp, color = KreaviloColors.Primary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.dismissFeedback() }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = KreaviloColors.Primary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Canvas Area with Aspect-Ratio-Preserving Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Compute DP dimensions based on 320dp max preview width
                val previewWidthDp = 330
                val ratio = uiState.activePreset.height.toFloat() / uiState.activePreset.width.toFloat()
                val previewHeightDp = (previewWidthDp * ratio).toInt().coerceIn(110, 360)

                Box(
                    modifier = Modifier
                        .width(previewWidthDp.dp)
                        .height(previewHeightDp.dp)
                ) {
                    InteractiveDesignCanvas(
                        elements = uiState.elements,
                        selectedElementId = uiState.selectedElementId,
                        backgroundColor = uiState.backgroundColor,
                        documentWidth = uiState.activePreset.width,
                        documentHeight = uiState.activePreset.height,
                        onSelectElement = { viewModel.selectElement(it) },
                        onUpdateElementTransform = { id, x, y -> viewModel.updateElementPosition(id, x, y) },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Safe Zone Overlay Grid
                    if (uiState.showSafeZoneGuides) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )

                            when (uiState.activePreset) {
                                BannerPreset.YOUTUBE_BANNER -> {
                                    // 1235x338 safe zone centered inside 2560x1440
                                    val safeW = size.width * (1235f / 2560f)
                                    val safeH = size.height * (338f / 1440f)
                                    val left = (size.width - safeW) / 2f
                                    val top = (size.height - safeH) / 2f
                                    drawRect(
                                        color = Color(0xFFF59E0B),
                                        topLeft = Offset(left, top),
                                        size = Size(safeW, safeH),
                                        style = stroke
                                    )
                                }
                                BannerPreset.YOUTUBE_THUMBNAIL -> {
                                    // Timestamp warning safe box at bottom right
                                    val badgeW = size.width * 0.22f
                                    val badgeH = size.height * 0.18f
                                    drawRect(
                                        color = Color(0xFFEF4444),
                                        topLeft = Offset(size.width - badgeW - 8f, size.height - badgeH - 8f),
                                        size = Size(badgeW, badgeH),
                                        style = stroke
                                    )
                                }
                                else -> {
                                    // 8% margin margin guide
                                    val pad = size.width * 0.06f
                                    drawRect(
                                        color = Color(0xFF3B82F6),
                                        topLeft = Offset(pad, pad),
                                        size = Size(size.width - (pad * 2), size.height - (pad * 2)),
                                        style = stroke
                                    )
                                }
                            }
                        }

                        // Guide Label
                        if (uiState.activePreset == BannerPreset.YOUTUBE_BANNER) {
                            Text(
                                text = "Safe Zone (Mobile/Desktop)",
                                color = Color(0xFFF59E0B),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 4.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        } else if (uiState.activePreset == BannerPreset.YOUTUBE_THUMBNAIL) {
                            Text(
                                text = "Timestamp Safe Zone",
                                color = Color(0xFFEF4444),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 6.dp, bottom = 4.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Quick Delete Toolbar when Element is Selected
            uiState.selectedElementId?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.deleteSelectedElement() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Selected", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bottom Navigation Tabs
            Surface(
                color = KreaviloColors.Surface,
                tonalElevation = 4.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column {
                    ScrollableTabRow(
                        selectedTabIndex = uiState.activeTab.ordinal,
                        containerColor = KreaviloColors.Surface,
                        contentColor = KreaviloColors.Primary,
                        edgePadding = 12.dp
                    ) {
                        BannerTab.values().forEach { tab ->
                            Tab(
                                selected = uiState.activeTab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                text = { Text(tab.name, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    // Tab Content Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(12.dp)
                    ) {
                        when (uiState.activeTab) {
                            BannerTab.PRESETS -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BannerPreset.values().forEach { preset ->
                                        val isSelected = preset == uiState.activePreset
                                        Card(
                                            modifier = Modifier
                                                .width(135.dp)
                                                .clickable { viewModel.selectPreset(preset) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) KreaviloColors.PrimaryContainer else KreaviloColors.Background
                                            ),
                                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(KreaviloColors.Primary)) else null
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = preset.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) KreaviloColors.Primary else KreaviloColors.TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "${preset.width}x${preset.height}",
                                                    fontSize = 10.sp,
                                                    color = KreaviloColors.TextSecondary
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = preset.aspectRatioLabel,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = KreaviloColors.Accent
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            BannerTab.TEXT -> {
                                var inputText by remember { mutableStateOf("") }
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = inputText,
                                            onValueChange = { inputText = it },
                                            placeholder = { Text("Catchy Title or Hook...", fontSize = 12.sp) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                if (inputText.isNotBlank()) {
                                                    viewModel.addText(inputText, Color(0xFFFACC15), true)
                                                    inputText = ""
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary)
                                        ) {
                                            Text("Add", fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            "VIRAL HOOK" to Color(0xFFFACC15),
                                            "EPISODE 01" to Color.White,
                                            "MUST WATCH" to Color(0xFFEF4444),
                                            "100% FREE" to Color(0xFF10B981),
                                            "HOW TO" to Color(0xFF38BDF8)
                                        ).forEach { (presetTxt, color) ->
                                            AssistChip(
                                                onClick = { viewModel.addText(presetTxt, color, true) },
                                                label = { Text(presetTxt, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color) }
                                            )
                                        }
                                    }
                                }
                            }

                            BannerTab.BADGES -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    listOf(
                                        "NEW VIDEO" to Color(0xFFEF4444),
                                        "4K ULTRA" to Color(0xFF8B5CF6),
                                        "WATCH NOW" to Color(0xFFDC2626),
                                        "FREE" to Color(0xFF10B981),
                                        "SALE 50%" to Color(0xFFF59E0B),
                                        "OFFICIAL" to Color(0xFF2563EB)
                                    ).forEach { (badge, bg) ->
                                        Button(
                                            onClick = { viewModel.addCtaBadge(badge, bg) },
                                            colors = ButtonDefaults.buttonColors(containerColor = bg),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(badge, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }

                            BannerTab.BACKGROUND -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Custom Photo background button
                                    OutlinedButton(
                                        onClick = { photoPickerLauncher.launch("image/*") },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Import Photo", fontSize = 12.sp)
                                    }

                                    listOf(
                                        Color(0xFF0F172A),
                                        Color(0xFF000000),
                                        Color(0xFFDC2626),
                                        Color(0xFF1E3A8A),
                                        Color(0xFF4C1D95),
                                        Color(0xFF065F46),
                                        Color(0xFFFFFFFF)
                                    ).forEach { col ->
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(col)
                                                .border(2.dp, if (uiState.backgroundColor == col) KreaviloColors.Primary else Color.Gray.copy(alpha = 0.3f), CircleShape)
                                                .clickable { viewModel.setBackgroundColor(col) }
                                        )
                                    }
                                }
                            }

                            BannerTab.LAYERS -> {
                                if (uiState.elements.isEmpty()) {
                                    Text("No elements on canvas. Add text, badges, or photos.", fontSize = 12.sp, color = KreaviloColors.TextSecondary)
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        uiState.elements.forEach { el ->
                                            val isSel = el.id == uiState.selectedElementId
                                            FilterChip(
                                                selected = isSel,
                                                onClick = { viewModel.selectElement(el.id) },
                                                label = { Text(el.id.take(12), fontSize = 11.sp) },
                                                trailingIcon = {
                                                    if (isSel) {
                                                        IconButton(onClick = { viewModel.deleteSelectedElement() }, modifier = Modifier.size(18.dp)) {
                                                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Red)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
