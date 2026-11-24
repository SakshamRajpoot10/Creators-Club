package com.creatorsclub.feature.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Activity Result Launchers for Creator Asset Imports
    val fontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "font_${System.currentTimeMillis()}.ttf"
            viewModel.importAsset(it, "fonts", fileName)
        }
    }

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "audio_${System.currentTimeMillis()}.mp3"
            viewModel.importAsset(it, "audio", fileName)
        }
    }

    val stickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "sticker_${System.currentTimeMillis()}.png"
            viewModel.importAsset(it, "stickers", fileName)
        }
    }

    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "banner_${System.currentTimeMillis()}.jpg"
            viewModel.importAsset(it, "banners", fileName)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Creator Hub",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        )
                    )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Status Feedback Card
            uiState.statusMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = KreaviloColors.PrimaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KreaviloColors.Primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.dismissStatusMessage() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = KreaviloColors.Primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // CREATOR ASSET HUB (Fonts, Audio, Stickers, Banners)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Creator Asset Import Hub", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.TextPrimary)
                            Text("Import fonts, audio, stickers & templates permanently", fontSize = 11.sp, color = KreaviloColors.TextSecondary)
                        }
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = KreaviloColors.Primary)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Custom Typography & Fonts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Custom Fonts (.ttf, .otf)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
                            Text("${uiState.assetStats.fontsCount} imported fonts ready in Design Studio", fontSize = 11.sp, color = KreaviloColors.TextTertiary)
                        }
                        OutlinedButton(
                            onClick = { fontLauncher.launch("*/*") },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import Font", fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = KreaviloColors.BorderLight)

                    // 2. Custom Audio & Music
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Audio & Music Tracks (.mp3, .wav)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
                            Text("${uiState.assetStats.audioCount} user tracks available offline", fontSize = 11.sp, color = KreaviloColors.TextTertiary)
                        }
                        OutlinedButton(
                            onClick = { audioLauncher.launch("audio/*") },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import Audio", fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = KreaviloColors.BorderLight)

                    // 3. Custom Stickers & Overlays
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Stickers & Overlays (.png, .webp)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
                            Text("${uiState.assetStats.stickersCount} imported transparent badges", fontSize = 11.sp, color = KreaviloColors.TextTertiary)
                        }
                        OutlinedButton(
                            onClick = { stickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import Sticker", fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = KreaviloColors.BorderLight)

                    // 4. Custom Banners & Templates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Banners & Design Backdrops", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
                            Text("${uiState.assetStats.bannersCount} custom background templates", fontSize = 11.sp, color = KreaviloColors.TextTertiary)
                        }
                        OutlinedButton(
                            onClick = { bannerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import Banner", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Management Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Storage & Cache", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Temporary render frames: ${String.format("%.1f", uiState.cacheSizeMb)} MB",
                        fontSize = 12.sp,
                        color = KreaviloColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.clearCache() },
                        colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Outlined.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (uiState.isCacheCleared) "Cache Cleared!" else "Clear Cache", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Model Manager Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("On-Device AI Model Manager", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.TextPrimary)
                        Text("100% Offline", fontSize = 10.sp, color = KreaviloColors.Success, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    uiState.models.forEach { model ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(model.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
                                Text(model.purpose, fontSize = 10.sp, color = KreaviloColors.TextSecondary)
                            }
                            Text(model.sizeMb, fontSize = 10.sp, color = KreaviloColors.Primary, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = KreaviloColors.BorderLight)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About & Creator Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About Creators Club (Kreavilo Studio)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Lead Architect & Developer: Saksham Rajpoot", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.Primary)
                    Text("Taglines: Create without limits. | Design. Edit. Imagine. Create.", fontSize = 11.sp, color = KreaviloColors.TextSecondary)
                    Text("Version: ${uiState.appVersion}", fontSize = 11.sp, color = KreaviloColors.TextTertiary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "100% Free Forever  •  Zero Watermarks  •  Zero Cloud Lock-in",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KreaviloColors.Success
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
