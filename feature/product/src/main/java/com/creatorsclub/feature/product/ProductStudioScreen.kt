package com.creatorsclub.feature.product

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.style.TextOverflow
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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadImage(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Product Studio",
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
                    // Import product photo
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = "Import Product Photo",
                            tint = KreaviloColors.Primary
                        )
                    }

                    // Export button
                    Button(
                        onClick = { viewModel.exportProductImage() },
                        enabled = (uiState.cutoutBitmap != null || uiState.originalBitmap != null) && !uiState.isLoading,
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
            if (uiState.originalBitmap != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KreaviloColors.Surface)
                        .navigationBarsPadding()
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
                                containerColor = if (uiState.hasRemovedBackground) Color(0xFF2E7D32) else KreaviloColors.Accent
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
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE5E7EB)),
            contentAlignment = Alignment.Center
        ) {
            val activeBitmap = uiState.cutoutBitmap ?: uiState.originalBitmap

            if (activeBitmap != null) {
                // Live 320x320 Product Display Canvas
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
                }
            } else {
                // Interactive Empty State Card
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
                                .background(KreaviloColors.ProductStudioCardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = null,
                                tint = KreaviloColors.ProductStudioBadge,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Product Photography Studio",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Isolate items with automatic background removal, place them on commercial studio podiums, and cast realistic shadows.",
                            fontSize = 12.sp,
                            color = KreaviloColors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Product Photo", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { viewModel.loadSampleProduct() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp), tint = KreaviloColors.Primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Try Sample Product", color = KreaviloColors.Primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Export Success Feedback
            uiState.exportSuccessUri?.let {
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
                            text = "Product Ad exported to Gallery!",
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
