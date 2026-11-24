package com.creatorsclub.feature.video

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.creatorsclub.core.designsystem.theme.KreaviloColors
import com.creatorsclub.engine.video.model.VideoTransition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoStudioScreen(
    viewModel: VideoStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addClip(it) }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setBackgroundAudio(it) }
    }

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
                                text = "Video Studio",
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

                            // Delete selected clip OR move project to Trash
                            IconButton(onClick = { viewModel.setShowTrashConfirmDialog(true) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Move to Trash", tint = KreaviloColors.Error)
                            }

                            // Export Button
                            Button(
                                onClick = { viewModel.exportVideo() },
                                enabled = uiState.clips.isNotEmpty() && !uiState.isLoading,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export MP4", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (uiState.clips.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KreaviloColors.Surface)
                        .navigationBarsPadding()
                ) {
                    // Contextual Tool Panels based on active tab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        when (uiState.activeTab) {
                            VideoToolTab.TIMELINE -> {
                                TimelineActionPanel(
                                    selectedClipId = uiState.selectedClipId,
                                    onAddClip = { videoPickerLauncher.launch("video/*") },
                                    onSplit = { viewModel.splitClipAtCurrentPosition() },
                                    onMoveLeft = { id -> viewModel.moveClipLeft(id) },
                                    onMoveRight = { id -> viewModel.moveClipRight(id) },
                                    onDeleteClip = { viewModel.deleteSelectedClip() }
                                )
                            }
                            VideoToolTab.SPEED -> {
                                val selectedClip = uiState.clips.find { it.id == uiState.selectedClipId }
                                SpeedActionPanel(
                                    currentSpeed = selectedClip?.speed ?: 1.0f,
                                    onSelectSpeed = { viewModel.setClipSpeed(it) }
                                )
                            }
                            VideoToolTab.AUDIO -> {
                                AudioActionPanel(
                                    audioName = uiState.backgroundAudioName,
                                    onImportAudio = { audioPickerLauncher.launch("audio/*") },
                                    onRemoveAudio = { viewModel.setBackgroundAudio(null) }
                                )
                            }
                            VideoToolTab.TRANSITIONS -> {
                                val selectedClip = uiState.clips.find { it.id == uiState.selectedClipId }
                                TransitionsActionPanel(
                                    currentTransition = selectedClip?.transition ?: VideoTransition.NONE,
                                    onSelectTransition = { viewModel.setClipTransition(it) }
                                )
                            }
                            VideoToolTab.EXPORT -> {
                                ExportSettingsPanel(
                                    currentRes = uiState.exportResolution,
                                    onExport = { viewModel.exportVideo() }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = KreaviloColors.Border)

                    // Compact Contextual Navigation Tabs (Single line, horizontally scrollable)
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
                        VideoToolTab.entries.forEach { tab ->
                            val isSelected = uiState.activeTab == tab
                            Tab(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(tab) },
                                text = {
                                    Text(
                                        text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                },
                                icon = {
                                    val icon = when (tab) {
                                        VideoToolTab.TIMELINE -> Icons.Outlined.Movie
                                        VideoToolTab.SPEED -> Icons.Outlined.Speed
                                        VideoToolTab.AUDIO -> Icons.Outlined.Audiotrack
                                        VideoToolTab.TRANSITIONS -> Icons.Outlined.Transform
                                        VideoToolTab.EXPORT -> Icons.Outlined.Download
                                    }
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0C0F17))
        ) {
            // 1. Dominant Video Preview Area (Takes 45-50% height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.clips.isNotEmpty()) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = viewModel.player
                                useController = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Clean Empty State
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VideoLibrary,
                                contentDescription = null,
                                tint = KreaviloColors.Primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Professional Video Studio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Multi-track NLE editing, trim, split, speed control, audio mix and MP4 export.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                            modifier = Modifier.fillMaxWidth(0.75f)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Video Clip", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(color = KreaviloColors.Primary)
                }
            }

            if (uiState.clips.isNotEmpty()) {
                // 2. Playback Transport Bar
                Surface(
                    color = Color(0xFF161B26),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Current timecode
                            Text(
                                text = formatTimecode(uiState.currentPositionMs),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Transport buttons: -5s | Play/Pause | +5s
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(onClick = { viewModel.seekRelative(-5000L) }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Filled.Replay5, contentDescription = "-5s", tint = Color.White, modifier = Modifier.size(20.dp))
                                }

                                IconButton(
                                    onClick = { viewModel.togglePlayPause() },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = KreaviloColors.Primary),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                IconButton(onClick = { viewModel.seekRelative(5000L) }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Filled.Forward5, contentDescription = "+5s", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }

                            // Total duration timecode
                            Text(
                                text = formatTimecode(uiState.totalDurationMs),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // Playhead scrubbing slider
                        Slider(
                            value = uiState.currentPositionMs.toFloat(),
                            onValueChange = { viewModel.seekTo(it.toLong()) },
                            valueRange = 0f..uiState.totalDurationMs.toFloat().coerceAtLeast(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = KreaviloColors.Primary,
                                activeTrackColor = KreaviloColors.Primary,
                                inactiveTrackColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }

                // 3. Multi-Track NLE Timeline
                Surface(
                    color = Color(0xFF0F131D),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 6.dp)
                    ) {
                        // Track V1: Video Sequence
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("V1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(24.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(uiState.clips) { clip ->
                                    val isSelected = clip.id == uiState.selectedClipId
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color.White) else null,
                                        modifier = Modifier
                                            .width(110.dp)
                                            .height(48.dp)
                                            .clickable { viewModel.selectClip(clip.id) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = clip.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${(clip.effectiveDurationMs / 1000f).coerceAtLeast(0f)}s",
                                                    fontSize = 9.sp,
                                                    color = Color(0xFFCBD5E1)
                                                )
                                                if (clip.speed != 1.0f) {
                                                    Text(
                                                        text = "${clip.speed}x",
                                                        fontSize = 9.sp,
                                                        color = Color(0xFFFBBF24),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Track A1: Audio Track
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("A1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(24.dp))

                            if (uiState.backgroundAudioUri != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF065F46),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(34.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Outlined.Audiotrack, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = uiState.backgroundAudioName ?: "Audio Track",
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text("♫ Waveform Active", fontSize = 9.sp, color = Color(0xFF6EE7B7))
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(34.dp)
                                        .clickable { audioPickerLauncher.launch("audio/*") }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tap to add background music / voiceover", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Trash Confirmation Dialog
    if (uiState.showTrashConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowTrashConfirmDialog(false) },
            title = { Text("Move Project to Trash?", fontWeight = FontWeight.Bold) },
            text = { Text("This video project will be moved to Projects → Trash and retained for 5 days before permanent cleanup.") },
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

private fun formatTimecode(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val millis = (ms % 1000) / 10
    return String.format("%02d:%02d:%02d", minutes, seconds, millis)
}

// --- Action Panels ---

@Composable
private fun TimelineActionPanel(
    selectedClipId: String?,
    onAddClip: () -> Unit,
    onSplit: () -> Unit,
    onMoveLeft: (String) -> Unit,
    onMoveRight: (String) -> Unit,
    onDeleteClip: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onAddClip,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Clip", fontSize = 11.sp)
        }

        if (selectedClipId != null) {
            Button(
                onClick = onSplit,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Outlined.ContentCut, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Split", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = { onMoveLeft(selectedClipId) }, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Move Left", tint = KreaviloColors.TextPrimary)
            }

            IconButton(onClick = { onMoveRight(selectedClipId) }, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "Move Right", tint = KreaviloColors.TextPrimary)
            }

            IconButton(onClick = onDeleteClip, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = KreaviloColors.Error)
            }
        }
    }
}

@Composable
private fun SpeedActionPanel(
    currentSpeed: Float,
    onSelectSpeed: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Playback Speed", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.TextPrimary)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                val isSelected = currentSpeed == spd
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectSpeed(spd) },
                    label = { Text("${spd}x", fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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
private fun AudioActionPanel(
    audioName: String?,
    onImportAudio: () -> Unit,
    onRemoveAudio: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Audio & Music", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.TextPrimary)
            Text(audioName ?: "No external audio loaded", fontSize = 10.sp, color = KreaviloColors.TextSecondary)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onImportAudio,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Outlined.Audiotrack, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (audioName != null) "Replace" else "Import Music", fontSize = 11.sp)
            }

            if (audioName != null) {
                OutlinedButton(
                    onClick = onRemoveAudio,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Remove", fontSize = 11.sp, color = KreaviloColors.Error)
                }
            }
        }
    }
}

@Composable
private fun TransitionsActionPanel(
    currentTransition: VideoTransition,
    onSelectTransition: (VideoTransition) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Transition: ${currentTransition.displayName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(VideoTransition.entries) { tr ->
                val isSelected = tr == currentTransition
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectTransition(tr) },
                    label = { Text(tr.displayName, fontSize = 10.sp) },
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
private fun ExportSettingsPanel(
    currentRes: String,
    onExport: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Export Resolution", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.TextPrimary)
            Text("Hardware-Accelerated H.264 MP4", fontSize = 10.sp, color = KreaviloColors.TextSecondary)
        }

        Button(
            onClick = onExport,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Export Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
