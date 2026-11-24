package com.creatorsclub.feature.writing

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingStudioScreen(
    viewModel: WritingStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Writing Studio",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = KreaviloColors.TextPrimary)
                    }
                },
                actions = {
                    // Teleprompter Launcher button
                    Button(
                        onClick = { viewModel.toggleTeleprompter(true) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Accent),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Teleprompter", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreaviloColors.Surface)
            )
        },
        bottomBar = {
            // Stats bar (Words, Chars, Read time)
            Surface(
                color = KreaviloColors.Surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.wordCount} words  •  ${uiState.characterCount} chars",
                        fontSize = 12.sp,
                        color = KreaviloColors.TextSecondary
                    )

                    Text(
                        text = "Est. Read: ${String.format("%.1f", uiState.estimatedReadingTimeMinutes)} min",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KreaviloColors.Primary
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(KreaviloColors.Background)
                .padding(16.dp)
        ) {
            // Quick Script Templates
            Text("Script Templates", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = { viewModel.applyTemplate("YOUTUBE_HOOK") },
                    label = { Text("YouTube Script", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = false,
                    onClick = { viewModel.applyTemplate("REEL_SCRIPT") },
                    label = { Text("30s Reel Hook", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = false,
                    onClick = { viewModel.applyTemplate("PRODUCT_PITCH") },
                    label = { Text("Product Pitch", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Editor Field
            OutlinedTextField(
                value = uiState.scriptContent,
                onValueChange = { viewModel.updateContent(it) },
                placeholder = {
                    Text(
                        text = "Start writing your video script, story, or social caption here...\nSupports Markdown formatting (# headings, - lists).",
                        color = KreaviloColors.TextTertiary,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = KreaviloColors.Surface,
                    unfocusedContainerColor = KreaviloColors.Surface,
                    focusedBorderColor = KreaviloColors.Primary,
                    unfocusedBorderColor = KreaviloColors.Border
                )
            )
        }

        // Full-screen Teleprompter Dialog
        if (uiState.isTeleprompterActive) {
            TeleprompterOverlay(
                content = uiState.scriptContent,
                scrollSpeed = uiState.teleprompterScrollSpeed,
                fontSizeSp = uiState.teleprompterFontSizeSp,
                isScrolling = uiState.isTeleprompterScrolling,
                onToggleScrolling = { viewModel.toggleTeleprompterScrolling() },
                onClose = { viewModel.toggleTeleprompter(false) }
            )
        }
    }
}

@Composable
fun TeleprompterOverlay(
    content: String,
    scrollSpeed: Int,
    fontSizeSp: Float,
    isScrolling: Boolean,
    onToggleScrolling: () -> Unit,
    onClose: () -> Unit
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(isScrolling, scrollSpeed) {
        if (isScrolling) {
            while (isScrolling) {
                delay(40)
                scrollState.scrollBy(scrollSpeed.toFloat() * 1.5f)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Teleprompter Control Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                Button(
                    onClick = onToggleScrolling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScrolling) KreaviloColors.Error else KreaviloColors.Success
                    )
                ) {
                    Icon(
                        imageVector = if (isScrolling) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isScrolling) "Pause" else "Start Scrolling")
                }
            }

            // Scrolling Script Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 40.dp)
            ) {
                Text(
                    text = if (content.isNotBlank()) content else "No script written yet. Add script text to use teleprompter.",
                    color = Color.White,
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * 1.4f).sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
