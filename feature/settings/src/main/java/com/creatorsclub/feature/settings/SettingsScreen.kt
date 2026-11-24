package com.creatorsclub.feature.settings

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                    Text("About Kreavilo Studio", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KreaviloColors.TextPrimary)
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
        }
    }
}
