package com.creatorsclub.feature.video

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Video Studio",
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
                    // Import Video Icon Button
                    IconButton(onClick = { videoPickerLauncher.launch("video/*") }) {
                        Icon(
                            imageVector = Icons.Outlined.VideoCall,
                            contentDescription = "Import Video Clip",
                            tint = KreaviloColors.Primary
                        )
                    }

                    // Import Audio / Music Icon Button
                    IconButton(onClick = { audioPickerLauncher.launch("audio/*") }) {
                        Icon(
                            imageVector = Icons.Outlined.Audiotrack,
                            contentDescription = "Import Audio / Music",
                            tint = if (uiState.backgroundAudioUri != null) KreaviloColors.Primary else KreaviloColors.TextSecondary
                        )
                    }

                    // Delete Selected Clip
                    if (uiState.selectedClipId != null) {
                        IconButton(onClick = { viewModel.deleteSelectedClip() }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete Clip", tint = KreaviloColors.Error)
                        }
                    }

                    // Export Button
                    Button(
                        onClick = { viewModel.exportVideo() },
                        enabled = uiState.clips.isNotEmpty() && !uiState.isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export MP4", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreaviloColors.Surface)
            )
        },
        bottomBar = {
            if (uiState.clips.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KreaviloColors.Surface)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Audio Track Indicator Chip
                    if (uiState.backgroundAudioUri != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KreaviloColors.PrimaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Outlined.Audiotrack,
                                        contentDescription = null,
                                        tint = KreaviloColors.Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Audio: ${uiState.backgroundAudioName ?: "Imported Track"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = KreaviloColors.Primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.setBackgroundAudio(null) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove Audio",
                                        tint = KreaviloColors.Primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Playback control row
                    val selectedClip = uiState.clips.find { it.id == uiState.selectedClipId }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = KreaviloColors.PrimaryContainer)
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = KreaviloColors.Primary
                            )
                        }

                        Text(
                            text = "${(uiState.totalDurationMs / 1000f).coerceAtLeast(0f)}s Total Duration",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KreaviloColors.TextSecondary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0.5f, 1.0f, 2.0f).forEach { spd ->
                                val isSpeedSelected = (selectedClip?.speed ?: 1.0f) == spd
                                FilterChip(
                                    selected = isSpeedSelected,
                                    onClick = { viewModel.setClipSpeed(spd) },
                                    label = { Text("${spd}x", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KreaviloColors.Primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Timeline Clips Track
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Clips Timeline", fontSize = 11.sp, color = KreaviloColors.TextSecondary, fontWeight = FontWeight.SemiBold)
                        TextButton(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = KreaviloColors.Primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Clip", fontSize = 11.sp, color = KreaviloColors.Primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.clips) { clip ->
                            val isSelected = clip.id == uiState.selectedClipId
                            Card(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(64.dp)
                                    .clickable { viewModel.selectClip(clip.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) KreaviloColors.PrimaryContainer else KreaviloColors.SurfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = clip.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) KreaviloColors.Primary else KreaviloColors.TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${(clip.effectiveDurationMs / 1000f).coerceAtLeast(0.1f)}s • ${clip.speed}x",
                                        fontSize = 10.sp,
                                        color = KreaviloColors.TextSecondary
                                    )
                                }
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
                // Interactive Empty State
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
                                .background(KreaviloColors.VideoStudioCardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Videocam,
                                contentDescription = null,
                                tint = KreaviloColors.VideoStudioBadge,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Start Video Project",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Import video clips to edit reels, cut scenes, adjust playback speed, and export HD MP4 videos.",
                            fontSize = 12.sp,
                            color = KreaviloColors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Video Clips from Gallery", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Export Success Feedback
            uiState.exportSuccessFile?.let { file ->
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
                            text = "Video exported successfully (${file.name})!",
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
