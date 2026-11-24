package com.creatorsclub.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.creatorsclub.core.designsystem.theme.KreaviloTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KreaviloTheme {
                MainAppScreen()
            }
        }
    }
}

data class StudioItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val cardBg: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val studios = remember {
        listOf(
            StudioItem("photo", "Photo Studio", Icons.Outlined.Photo, KreaviloColors.PhotoStudioBadge, KreaviloColors.PhotoStudioCardBg),
            StudioItem("video", "Video Studio", Icons.Outlined.Videocam, KreaviloColors.VideoStudioBadge, KreaviloColors.VideoStudioCardBg),
            StudioItem("design", "Design Studio", Icons.Outlined.DesignServices, KreaviloColors.DesignStudioBadge, KreaviloColors.DesignStudioCardBg),
            StudioItem("drawing", "Drawing Studio", Icons.Outlined.Palette, KreaviloColors.DrawingStudioBadge, KreaviloColors.DrawingStudioCardBg),
            StudioItem("product", "Product Studio", Icons.Outlined.ShoppingBag, KreaviloColors.ProductStudioBadge, KreaviloColors.ProductStudioCardBg),
            StudioItem("interior", "Interior Studio", Icons.Outlined.Home, KreaviloColors.InteriorStudioBadge, KreaviloColors.InteriorStudioCardBg),
            StudioItem("writing", "Writing Studio", Icons.Outlined.Edit, KreaviloColors.WritingStudioBadge, KreaviloColors.WritingStudioCardBg),
            StudioItem("audio", "Audio Studio", Icons.Outlined.GraphicEq, KreaviloColors.AudioStudioBadge, KreaviloColors.AudioStudioCardBg)
        )
    }

    Scaffold(
        containerColor = KreaviloColors.Background,
        bottomBar = {
            NavigationBar(
                containerColor = KreaviloColors.Surface,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KreaviloColors.Primary,
                        selectedTextColor = KreaviloColors.Primary,
                        indicatorColor = KreaviloColors.PrimaryContainer
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(if (selectedTab == 1) Icons.Filled.GridView else Icons.Outlined.GridView, contentDescription = "Studios") },
                    label = { Text("Studios", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KreaviloColors.Primary,
                        selectedTextColor = KreaviloColors.Primary,
                        indicatorColor = KreaviloColors.PrimaryContainer
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Create", tint = KreaviloColors.Primary, modifier = Modifier.size(28.dp)) },
                    label = { Text("Create", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KreaviloColors.Primary,
                        selectedTextColor = KreaviloColors.Primary,
                        indicatorColor = KreaviloColors.PrimaryContainer
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(if (selectedTab == 3) Icons.Filled.Folder else Icons.Outlined.Folder, contentDescription = "Projects") },
                    label = { Text("Projects", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KreaviloColors.Primary,
                        selectedTextColor = KreaviloColors.Primary,
                        indicatorColor = KreaviloColors.PrimaryContainer
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(if (selectedTab == 4) Icons.Filled.Settings else Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KreaviloColors.Primary,
                        selectedTextColor = KreaviloColors.Primary,
                        indicatorColor = KreaviloColors.PrimaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(KreaviloColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Kreavilo Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Kreavilo",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        )
                    )
                }

                IconButton(onClick = { /* Notifications */ }) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = KreaviloColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hero Greeting Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.PrimaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "What will you create today?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.Primary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your imagination starts here.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = KreaviloColors.TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { /* New Project */ },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KreaviloColors.Primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Create project", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Find any creative tool", color = KreaviloColors.TextTertiary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = "Search", tint = KreaviloColors.TextSecondary)
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

            Spacer(modifier = Modifier.height(20.dp))

            // Studios Section Header
            Text(
                text = "Your creative studios",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = KreaviloColors.TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Studios 2x4 Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                studios.chunked(2).forEach { rowStudios ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowStudios.forEach { studio ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(84.dp)
                                    .clickable { /* Navigate to Studio */ },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = studio.cardBg)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = studio.icon,
                                        contentDescription = studio.name,
                                        tint = studio.badgeColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = studio.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = KreaviloColors.TextPrimary,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Creator AI Companion Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Open Creator AI Assistant */ },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(KreaviloColors.Border))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(KreaviloColors.AccentContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Creator AI",
                                tint = KreaviloColors.Accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Creator AI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KreaviloColors.TextPrimary
                                )
                            )
                            Text(
                                text = "Ask me anything",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = KreaviloColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = { /* Voice Command */ },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(KreaviloColors.PrimaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Voice Input",
                            tint = KreaviloColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
