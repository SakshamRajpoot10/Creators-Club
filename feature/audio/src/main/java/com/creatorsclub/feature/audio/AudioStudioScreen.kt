package com.creatorsclub.feature.audio

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.creatorsclub.core.designsystem.theme.KreaviloColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioStudioScreen(
    viewModel: AudioStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importAudioFile(it) }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Audio Studio",
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
                    IconButton(onClick = { audioPickerLauncher.launch("audio/*") }) {
                        Icon(
                            imageVector = Icons.Outlined.AudioFile,
                            contentDescription = "Import Audio Track",
                            tint = KreaviloColors.Primary
                        )
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
                .padding(16.dp)
        ) {
            // Voice Recorder Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (uiState.isRecording) "Recording Voiceover..." else "High-Fidelity Voice Recorder",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = KreaviloColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Waveform Visualizer Bars
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.amplitudeWaveforms.isNotEmpty()) {
                            uiState.amplitudeWaveforms.forEach { amp ->
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .height((amp * 45).dp.coerceAtLeast(6.dp))
                                        .padding(horizontal = 1.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (uiState.isRecording) KreaviloColors.Error else KreaviloColors.Primary)
                                )
                            }
                        } else {
                            Text("Waveform will pulse during active recording", fontSize = 11.sp, color = KreaviloColors.TextTertiary)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Big Mic Button
                    IconButton(
                        onClick = {
                            if (uiState.isRecording) {
                                viewModel.stopRecording()
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    viewModel.startRecording()
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isRecording) KreaviloColors.Error else KreaviloColors.Primary)
                    ) {
                        Icon(
                            imageVector = if (uiState.isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = "Record",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (uiState.isRecording) "${uiState.recordingDurationSec / 10}s • Tap to finish" else "Tap to record (48 kHz Lossless)",
                        fontSize = 11.sp,
                        color = if (uiState.isRecording) KreaviloColors.Error else KreaviloColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TTS & Narration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Text-to-Speech Voice Narration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KreaviloColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.ttsInputText,
                        onValueChange = { viewModel.updateTtsText(it) },
                        placeholder = { Text("Type text for Indian English or Hindi narration...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.speakTts("en-IN") },
                                enabled = uiState.ttsInputText.isNotBlank() && !uiState.isSpeakingTts,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary)
                            ) {
                                Text("Speak (EN)", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.speakTts("hi-IN") },
                                enabled = uiState.ttsInputText.isNotBlank() && !uiState.isSpeakingTts,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Accent)
                            ) {
                                Text("Speak (HI)", fontSize = 11.sp)
                            }
                        }

                        if (uiState.isSpeakingTts) {
                            IconButton(onClick = { viewModel.stopTts() }) {
                                Icon(Icons.Filled.Stop, contentDescription = "Stop", tint = KreaviloColors.Error)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Saved Recordings List Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Audio Clips (${uiState.savedRecordings.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = KreaviloColors.TextPrimary
                )
                TextButton(onClick = { audioPickerLauncher.launch("audio/*") }) {
                    Text("+ Import Audio", fontSize = 11.sp, color = KreaviloColors.Primary)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.savedRecordings) { file ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Outlined.GraphicEq, contentDescription = null, tint = KreaviloColors.Primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = file.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = KreaviloColors.TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${file.length() / 1024} KB",
                                        fontSize = 10.sp,
                                        color = KreaviloColors.TextSecondary
                                    )
                                }
                            }

                            IconButton(onClick = { viewModel.deleteRecording(file) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = KreaviloColors.Error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
