package com.creatorsclub.feature.audio

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioStudioScreen(
    viewModel: AudioStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Audio Studio",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreaviloColors.Surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(KreaviloColors.Background)
                .padding(16.dp)
        ) {
            // Voice Recorder Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface)
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
                            if (uiState.isRecording) viewModel.stopRecording() else viewModel.startRecording()
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isRecording) KreaviloColors.Error else KreaviloColors.Primary)
                    ) {
                        Icon(
                            imageVector = if (uiState.isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = "Record Toggle",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Text to Speech Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI Voice Narration (TTS)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KreaviloColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.ttsInputText,
                        onValueChange = { viewModel.updateTtsText(it) },
                        placeholder = { Text("Type any script text in English or Hindi...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.speakTts("en-IN") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Speak (English)", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.speakTts("hi-IN") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Accent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Speak (हिंदी)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
