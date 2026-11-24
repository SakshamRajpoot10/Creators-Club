package com.creatorsclub.feature.interior

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteriorStudioScreen(
    viewModel: InteriorStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Interior Design Studio",
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
                    if (uiState.selectedFurnitureId != null) {
                        IconButton(onClick = { viewModel.rotateSelected() }) {
                            Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate", tint = KreaviloColors.TextPrimary)
                        }
                        IconButton(onClick = { viewModel.deleteSelected() }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = KreaviloColors.Error)
                        }
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
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text("Add Furniture to 2D Plan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KreaviloColors.TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(FurnitureType.entries) { type ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.addFurniture(type) },
                            label = { Text(type.displayName, fontSize = 11.sp) }
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
            // 2D Room Floorplan Canvas (320x320 dp)
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(uiState.floorColor)
                    .border(4.dp, uiState.wallColor, RoundedCornerShape(8.dp))
                    .pointerInput(uiState.placedFurnitureList) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val hit = uiState.placedFurnitureList.find { f ->
                                    val dx = offset.x - f.x
                                    val dy = offset.y - f.y
                                    dx in (-f.type.widthDp / 2)..(f.type.widthDp / 2) &&
                                    dy in (-f.type.heightDp / 2)..(f.type.heightDp / 2)
                                }
                                viewModel.selectFurniture(hit?.id)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                uiState.selectedFurnitureId?.let { id ->
                                    val current = uiState.placedFurnitureList.find { it.id == id } ?: return@let
                                    viewModel.updatePosition(id, current.x + dragAmount.x, current.y + dragAmount.y)
                                }
                            }
                        )
                    }
            ) {
                // Dimension labels
                Text(
                    text = "${uiState.roomWidthMeters}m",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                )

                Text(
                    text = "${uiState.roomHeightMeters}m",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                )

                // Render placed furniture blocks
                for (furniture in uiState.placedFurnitureList) {
                    val isSelected = furniture.id == uiState.selectedFurnitureId
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (furniture.x - furniture.type.widthDp / 2).roundToInt(),
                                    (furniture.y - furniture.type.heightDp / 2).roundToInt()
                                )
                            }
                            .size(furniture.type.widthDp.dp, furniture.type.heightDp.dp)
                            .rotate(furniture.rotation)
                            .clip(RoundedCornerShape(6.dp))
                            .background(furniture.type.color)
                            .then(
                                if (isSelected) Modifier.border(2.dp, Color.Yellow, RoundedCornerShape(6.dp)) else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = furniture.type.displayName,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
