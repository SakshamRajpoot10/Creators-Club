package com.creatorsclub.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.designsystem.theme.KreaviloColors

data class StudioDirectoryItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val cardBg: Color,
    val features: List<String>,
    val tags: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudiosDirectoryScreen(
    onNavigateToStudio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember {
        listOf("All", "Photo & Visual", "Video & Audio", "Design & Art", "AI & Tracing", "Planning")
    }

    val allStudios = remember {
        listOf(
            StudioDirectoryItem(
                id = "photo",
                name = "Photo Studio",
                category = "Photo & Visual",
                description = "Non-destructive photo editing, tone curve grading, HSL sliders, lens filters, and smart vignette.",
                icon = Icons.Outlined.Photo,
                badgeColor = KreaviloColors.PhotoStudioBadge,
                cardBg = KreaviloColors.PhotoStudioCardBg,
                features = listOf("HSL Sliders", "Curve Grading", "Crop & Rotate", "Preset Filters", "Lossless Export"),
                tags = listOf("photo", "picture", "edit", "color", "grade", "filter", "contrast", "crop", "lightroom")
            ),
            StudioDirectoryItem(
                id = "video",
                name = "Video Studio",
                category = "Video & Audio",
                description = "Precision timeline video editor for Reels, Shorts, and clips with trim, split, speed ramps, and transitions.",
                icon = Icons.Outlined.Videocam,
                badgeColor = KreaviloColors.VideoStudioBadge,
                cardBg = KreaviloColors.VideoStudioCardBg,
                features = listOf("Timeline Editor", "Reels 9:16", "Transitions", "Speed Ramp", "Audio Sync"),
                tags = listOf("video", "reel", "short", "cut", "trim", "transition", "timeline", "capcut", "film")
            ),
            StudioDirectoryItem(
                id = "design",
                name = "Design Studio",
                category = "Design & Art",
                description = "Multi-element graphic design for posters, banners, YouTube thumbnails, and social media flyers.",
                icon = Icons.Outlined.DesignServices,
                badgeColor = KreaviloColors.DesignStudioBadge,
                cardBg = KreaviloColors.DesignStudioCardBg,
                features = listOf("Canvas Layers", "Custom Fonts", "Stickers & Badges", "Social Templates", "SVG/PNG Export"),
                tags = listOf("design", "poster", "banner", "thumbnail", "flyer", "canva", "graphic", "text", "shape")
            ),
            StudioDirectoryItem(
                id = "drawing",
                name = "Drawing Studio",
                category = "Design & Art",
                description = "Digital sketchbook with responsive pencil, ink, highlighter brushes, color palettes, and 8x mandala symmetry.",
                icon = Icons.Outlined.Palette,
                badgeColor = KreaviloColors.DrawingStudioBadge,
                cardBg = KreaviloColors.DrawingStudioCardBg,
                features = listOf("8x Mandala Mirror", "Custom Brushes", "Alpha Blending", "Undo/Redo Stack", "Transparent PNG"),
                tags = listOf("draw", "sketch", "paint", "art", "brush", "mandala", "illustration", "doodle", "procreate")
            ),
            StudioDirectoryItem(
                id = "product",
                name = "Product Studio",
                category = "Photo & Visual",
                description = "Automated product photography studio with AI background removal, studio podiums, and advertising banners.",
                icon = Icons.Outlined.ShoppingBag,
                badgeColor = KreaviloColors.ProductStudioBadge,
                cardBg = KreaviloColors.ProductStudioCardBg,
                features = listOf("AI Background Cut", "3D Studio Podiums", "Drop Shadows", "Sale Badges", "E-Commerce Ready"),
                tags = listOf("product", "ecommerce", "background", "remove", "podium", "ad", "commercial", "amazon", "sale")
            ),
            StudioDirectoryItem(
                id = "interior",
                name = "Interior Studio",
                category = "Planning",
                description = "2D room blueprint designer and furniture arrangement planner for homes, shops, and studios.",
                icon = Icons.Outlined.Home,
                badgeColor = KreaviloColors.InteriorStudioBadge,
                cardBg = KreaviloColors.InteriorStudioCardBg,
                features = listOf("2D Floorplan Grid", "Furniture Library", "Snap to Wall", "Metric Scale", "Blueprint Export"),
                tags = listOf("interior", "room", "floorplan", "furniture", "blueprint", "home", "layout", "architecture")
            ),
            StudioDirectoryItem(
                id = "writing",
                name = "Writing Studio",
                category = "Planning",
                description = "Content creator script editor with smooth auto-scrolling teleprompter, hook generators, and video captioning.",
                icon = Icons.Outlined.Edit,
                badgeColor = KreaviloColors.WritingStudioBadge,
                cardBg = KreaviloColors.WritingStudioCardBg,
                features = listOf("Smooth Teleprompter", "Word Counter", "Hook Generator", "Speed Controls", "Markdown Export"),
                tags = listOf("writing", "script", "teleprompter", "speech", "hook", "caption", "notes", "story")
            ),
            StudioDirectoryItem(
                id = "audio",
                name = "Audio Studio",
                category = "Video & Audio",
                description = "Studio-grade 48 kHz voiceover recorder with real-time waveform visualizer, noise gate, and text-to-speech engine.",
                icon = Icons.Outlined.GraphicEq,
                badgeColor = KreaviloColors.AudioStudioBadge,
                cardBg = KreaviloColors.AudioStudioCardBg,
                features = listOf("48 kHz WAV Recording", "Live Oscilloscope", "Offline TTS", "Noise Gate", "Audio Trimmer"),
                tags = listOf("audio", "sound", "voice", "record", "mic", "tts", "narration", "podcast", "speech")
            ),
            StudioDirectoryItem(
                id = "ar_drawing",
                name = "AR Tracing Studio",
                category = "AI & Tracing",
                description = "Learn drawing by placing your phone on a cup or stand and tracing projected high-contrast photo contours on paper.",
                icon = Icons.Outlined.CameraAlt,
                badgeColor = KreaviloColors.DrawingStudioBadge,
                cardBg = KreaviloColors.DrawingStudioCardBg,
                features = listOf("Sobel Edge Filter", "Opacity Slider", "Flashlight Toggle", "Torch Lock", "Paper Tracing"),
                tags = listOf("ar", "trace", "tracing", "camera", "draw", "contour", "sketch", "outline", "learn")
            )
        )
    }

    val filteredStudios = remember(searchQuery, selectedCategory) {
        allStudios.filter { studio ->
            val matchesCategory = (selectedCategory == "All") || (studio.category == selectedCategory)
            val matchesSearch = searchQuery.isBlank() ||
                    studio.name.contains(searchQuery, ignoreCase = true) ||
                    studio.description.contains(searchQuery, ignoreCase = true) ||
                    studio.tags.any { it.contains(searchQuery, ignoreCase = true) } ||
                    studio.features.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Studios Directory",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KreaviloColors.Surface)
            )
        },
        containerColor = KreaviloColors.Background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search tools, studios, features...", color = KreaviloColors.TextTertiary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = "Search", tint = KreaviloColors.TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = KreaviloColors.TextSecondary)
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = KreaviloColors.Surface,
                    unfocusedContainerColor = KreaviloColors.Surface,
                    focusedBorderColor = KreaviloColors.Primary,
                    unfocusedBorderColor = KreaviloColors.Border
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Pills
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KreaviloColors.Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Studios List
            Text(
                text = "${filteredStudios.size} Studios Available",
                fontSize = 12.sp,
                color = KreaviloColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredStudios, key = { it.id }) { studio ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToStudio(studio.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = studio.cardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = studio.icon,
                                        contentDescription = studio.name,
                                        tint = studio.badgeColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = studio.name,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KreaviloColors.TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = studio.category,
                                        fontSize = 11.sp,
                                        color = studio.badgeColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = { onNavigateToStudio(studio.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = studio.badgeColor),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = studio.description,
                                fontSize = 12.sp,
                                color = KreaviloColors.TextSecondary,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Features Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                studio.features.take(3).forEach { feature ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.85f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = feature,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = KreaviloColors.TextPrimary
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
