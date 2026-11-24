package com.creatorsclub.feature.product

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductStudioScreen(
    viewModel: ProductStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Product Studio",
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
                    Button(
                        onClick = { viewModel.exportProductImage() },
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Background removal CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Studio Backdrop",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KreaviloColors.TextPrimary
                    )

                    Button(
                        onClick = { viewModel.removeBackground() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.hasRemovedBackground) KreaviloColors.Success else KreaviloColors.Accent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (uiState.hasRemovedBackground) "Cutout Active" else "Remove Background", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Backdrop selector pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(StudioBackdrop.entries) { backdrop ->
                        val isSelected = backdrop == uiState.selectedBackdrop
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setBackdrop(backdrop) },
                            label = { Text(backdrop.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KreaviloColors.Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scale & Shadow sliders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scale", fontSize = 11.sp, color = KreaviloColors.TextSecondary, modifier = Modifier.width(45.dp))
                    Slider(
                        value = uiState.productScale,
                        onValueChange = { viewModel.updateScale(it) },
                        valueRange = 0.5f..1.8f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Shadow", fontSize = 11.sp, color = KreaviloColors.TextSecondary, modifier = Modifier.width(45.dp))
                    Slider(
                        value = uiState.shadowIntensity,
                        onValueChange = { viewModel.updateShadowIntensity(it) },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = KreaviloColors.Primary, activeTrackColor = KreaviloColors.Primary)
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
            // Live 320x320 Product Display Canvas
            val activeBitmap = uiState.cutoutBitmap ?: uiState.originalBitmap

            Box(
                modifier = Modifier
                    .size(320.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (uiState.selectedBackdrop == StudioBackdrop.TRANSPARENT) Color.Transparent
                        else runCatching { Color(android.graphics.Color.parseColor(uiState.selectedBackdrop.primaryColorHex)) }.getOrDefault(Color.White)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (activeBitmap != null) {
                    // Contact shadow
                    if (uiState.shadowIntensity > 0f && uiState.selectedBackdrop != StudioBackdrop.TRANSPARENT) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = (-40 + uiState.productOffsetY).dp)
                                .width(180.dp)
                                .height(28.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = uiState.shadowIntensity * 0.45f))
                        )
                    }

                    Image(
                        bitmap = activeBitmap.asImageBitmap(),
                        contentDescription = "Product Showcase",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize(0.75f * uiState.productScale)
                            .offset(y = uiState.productOffsetY.dp)
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Import product photo to create studio showcase",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(color = KreaviloColors.Primary)
            }
        }
    }
}
