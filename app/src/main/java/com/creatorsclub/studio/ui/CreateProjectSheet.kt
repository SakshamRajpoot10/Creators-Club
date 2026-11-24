package com.creatorsclub.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors

data class StudioCreateOption(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val cardBg: Color,
    val actions: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectSheet(
    onDismiss: () -> Unit,
    onSelectStudio: (studioId: String, initialAction: String) -> Unit
) {
    val studios = remember {
        listOf(
            StudioCreateOption(
                id = "photo",
                title = "Photo Studio",
                description = "Edit, color grade, and enhance your photos non-destructively.",
                icon = Icons.Outlined.Photo,
                badgeColor = KreaviloColors.PhotoStudioBadge,
                cardBg = KreaviloColors.PhotoStudioCardBg,
                actions = listOf("Import Photo", "Capture Photo", "Blank Canvas")
            ),
            StudioCreateOption(
                id = "video",
                title = "Video Studio",
                description = "Trim clips, add transitions, music, speed ramps & export reels.",
                icon = Icons.Outlined.Videocam,
                badgeColor = KreaviloColors.VideoStudioBadge,
                cardBg = KreaviloColors.VideoStudioCardBg,
                actions = listOf("Import Video Clips", "New Timeline")
            ),
            StudioCreateOption(
                id = "design",
                title = "Design Studio",
                description = "Create posters, social media banners, flyers and YouTube thumbnails.",
                icon = Icons.Outlined.DesignServices,
                badgeColor = KreaviloColors.DesignStudioBadge,
                cardBg = KreaviloColors.DesignStudioCardBg,
                actions = listOf("Blank Canvas", "Choose Template", "Custom Size")
            ),
            StudioCreateOption(
                id = "drawing",
                title = "Drawing Studio",
                description = "Draw and sketch with responsive brushes and 8x mandala symmetry.",
                icon = Icons.Outlined.Palette,
                badgeColor = KreaviloColors.DrawingStudioBadge,
                cardBg = KreaviloColors.DrawingStudioCardBg,
                actions = listOf("Blank Sketchbook", "Mandala Symmetry")
            ),
            StudioCreateOption(
                id = "product",
                title = "Product Studio",
                description = "Remove background, place on studio podiums, and create commercial ads.",
                icon = Icons.Outlined.ShoppingBag,
                badgeColor = KreaviloColors.ProductStudioBadge,
                cardBg = KreaviloColors.ProductStudioCardBg,
                actions = listOf("Import Product Photo", "Capture Photo")
            ),
            StudioCreateOption(
                id = "interior",
                title = "Interior Studio",
                description = "Design 2D room floorplans and arrange furniture intuitively.",
                icon = Icons.Outlined.Home,
                badgeColor = KreaviloColors.InteriorStudioBadge,
                cardBg = KreaviloColors.InteriorStudioCardBg,
                actions = listOf("2D Floorplan", "Room Redesign")
            ),
            StudioCreateOption(
                id = "writing",
                title = "Writing Studio",
                description = "Write scripts, video hooks, captions, and launch teleprompter.",
                icon = Icons.Outlined.Edit,
                badgeColor = KreaviloColors.WritingStudioBadge,
                cardBg = KreaviloColors.WritingStudioCardBg,
                actions = listOf("New Script", "Video Teleprompter", "Reel Hook")
            ),
            StudioCreateOption(
                id = "audio",
                title = "Audio Studio",
                description = "Record 48 kHz voiceovers with live waveform and TTS narration.",
                icon = Icons.Outlined.GraphicEq,
                badgeColor = KreaviloColors.AudioStudioBadge,
                cardBg = KreaviloColors.AudioStudioCardBg,
                actions = listOf("Record Voiceover", "Import Audio", "Text to Speech")
            ),
            StudioCreateOption(
                id = "ar_drawing",
                title = "AR Tracing Studio",
                description = "Convert photos to outline sketches and trace on paper using camera.",
                icon = Icons.Outlined.CameraAlt,
                badgeColor = KreaviloColors.DrawingStudioBadge,
                cardBg = KreaviloColors.DrawingStudioCardBg,
                actions = listOf("Choose Reference", "Camera Tracing")
            ),
            StudioCreateOption(
                id = "banner",
                title = "Banner & Thumbnail Studio",
                description = "Design YouTube thumbnails (1280x720) & channel banners (2560x1440) with safe zones.",
                icon = Icons.Outlined.BurstMode,
                badgeColor = KreaviloColors.DesignStudioBadge,
                cardBg = KreaviloColors.DesignStudioCardBg,
                actions = listOf("YouTube Thumbnail (1280x720)", "Channel Banner (2560x1440)", "Instagram Post", "X Header")
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = KreaviloColors.Surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = KreaviloColors.Border) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Start Creating",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = KreaviloColors.TextPrimary
                    )
                    Text(
                        text = "Choose a studio and bring your ideas to life.",
                        fontSize = 12.sp,
                        color = KreaviloColors.TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = KreaviloColors.TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(studios) { studio ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = studio.cardBg)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = studio.icon,
                                        contentDescription = studio.title,
                                        tint = studio.badgeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = studio.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KreaviloColors.TextPrimary
                                    )
                                    Text(
                                        text = studio.description,
                                        fontSize = 11.sp,
                                        color = KreaviloColors.TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                studio.actions.forEach { action ->
                                    Button(
                                        onClick = { onSelectStudio(studio.id, action) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = action,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = studio.badgeColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
