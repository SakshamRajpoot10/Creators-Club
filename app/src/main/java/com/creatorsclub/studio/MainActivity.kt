package com.creatorsclub.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.creatorsclub.core.designsystem.theme.KreaviloColors
import com.creatorsclub.core.designsystem.theme.KreaviloTheme
import com.creatorsclub.feature.ar_drawing.ArDrawingScreen
import com.creatorsclub.feature.ar_drawing.ArDrawingViewModel
import com.creatorsclub.feature.audio.AudioStudioScreen
import com.creatorsclub.feature.audio.AudioStudioViewModel
import com.creatorsclub.feature.design.DesignStudioScreen
import com.creatorsclub.feature.design.DesignStudioViewModel
import com.creatorsclub.feature.design.banner.BannerStudioScreen
import com.creatorsclub.feature.design.banner.BannerStudioViewModel
import com.creatorsclub.feature.drawing.DrawingStudioScreen
import com.creatorsclub.feature.drawing.DrawingStudioViewModel
import com.creatorsclub.feature.interior.InteriorStudioScreen
import com.creatorsclub.feature.interior.InteriorStudioViewModel
import com.creatorsclub.feature.photo.PhotoStudioScreen
import com.creatorsclub.feature.photo.PhotoStudioViewModel
import com.creatorsclub.feature.product.ProductStudioScreen
import com.creatorsclub.feature.product.ProductStudioViewModel
import com.creatorsclub.feature.projects.ProjectsScreen
import com.creatorsclub.feature.projects.ProjectsViewModel
import com.creatorsclub.feature.settings.PermissionsScreen
import com.creatorsclub.feature.settings.SettingsScreen
import com.creatorsclub.feature.settings.SettingsViewModel
import com.creatorsclub.feature.video.VideoStudioScreen
import com.creatorsclub.feature.video.VideoStudioViewModel
import com.creatorsclub.feature.writing.WritingStudioScreen
import com.creatorsclub.feature.writing.WritingStudioViewModel
import com.creatorsclub.studio.ui.CreateProjectSheet
import com.creatorsclub.studio.ui.StudiosDirectoryScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KreaviloTheme {
                val navController = rememberNavController()

                fun navigateToStudio(studioId: String) {
                    when (studioId.lowercase()) {
                        "photo" -> navController.navigate("photo_studio")
                        "design" -> navController.navigate("design_studio")
                        "product" -> navController.navigate("product_studio")
                        "drawing" -> navController.navigate("drawing_studio")
                        "ar_drawing" -> navController.navigate("ar_drawing_studio")
                        "video" -> navController.navigate("video_studio")
                        "audio" -> navController.navigate("audio_studio")
                        "writing" -> navController.navigate("writing_studio")
                        "interior" -> navController.navigate("interior_studio")
                        "banner", "thumbnail", "banner_thumbnail", "banner_studio" -> navController.navigate("banner_studio")
                        else -> navController.navigate("photo_studio")
                    }
                }

                NavHost(navController = navController, startDestination = "main_root") {
                    // Unified Main App Root (Hosts Home, Studios Directory, Projects, Settings)
                    composable("main_root") {
                        MainAppScreen(
                            onNavigateToStudio = ::navigateToStudio,
                            onNavigateToPermissions = { navController.navigate("permissions") }
                        )
                    }

                    composable("permissions") {
                        PermissionsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 1. Photo Studio
                    composable("photo_studio") {
                        val viewModel: PhotoStudioViewModel = hiltViewModel()
                        PhotoStudioScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 2. Design Studio
                    composable("design_studio") {
                        val viewModel: DesignStudioViewModel = hiltViewModel()
                        DesignStudioScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 3. Product Photography Studio
                    composable("product_studio") {
                        val viewModel: ProductStudioViewModel = hiltViewModel()
                        ProductStudioScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 4. Drawing & Painting Studio
                    composable("drawing_studio") {
                        val viewModel: DrawingStudioViewModel = hiltViewModel()
                        DrawingStudioScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 5. Camera AR Tracing Studio
                    composable("ar_drawing_studio") {
                        val viewModel: ArDrawingViewModel = hiltViewModel()
                        ArDrawingScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 6. Video Studio
                    composable("video_studio") {
                        val viewModel: VideoStudioViewModel = hiltViewModel()
                        VideoStudioScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 7. Audio Studio
                    composable("audio_studio") {
                        val viewModel: AudioStudioViewModel = hiltViewModel()
                        AudioStudioScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 8. Writing Studio & Script Teleprompter
                    composable("writing_studio") {
                        val viewModel: WritingStudioViewModel = hiltViewModel()
                        WritingStudioScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 9. Interior Floorplan Studio
                    composable("interior_studio") {
                        val viewModel: InteriorStudioViewModel = hiltViewModel()
                        InteriorStudioScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // 10. Banner & Thumbnail Studio
                    composable("banner_studio") {
                        val viewModel: BannerStudioViewModel = hiltViewModel()
                        BannerStudioScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
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

data class QuickToolItem(
    val title: String,
    val studioId: String,
    val initialAction: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    onNavigateToStudio: (String) -> Unit,
    onNavigateToPermissions: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateSheet by remember { mutableStateOf(false) }

    val projectsViewModel: ProjectsViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    Scaffold(
        containerColor = KreaviloColors.Background,
        bottomBar = {
            NavigationBar(
                containerColor = KreaviloColors.Surface,
                tonalElevation = 6.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, maxLines = 1) },
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
                    label = { Text("Studios", fontSize = 11.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KreaviloColors.Primary,
                        selectedTextColor = KreaviloColors.Primary,
                        indicatorColor = KreaviloColors.PrimaryContainer
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { showCreateSheet = true },
                    icon = {
                        Icon(
                            Icons.Filled.AddCircle,
                            contentDescription = "Create",
                            tint = KreaviloColors.Primary,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text("Create", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
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
                    label = { Text("Projects", fontSize = 11.sp, maxLines = 1) },
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
                    label = { Text("Settings", fontSize = 11.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KreaviloColors.Primary,
                        selectedTextColor = KreaviloColors.Primary,
                        indicatorColor = KreaviloColors.PrimaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeDashboardScreen(
                    onNavigateToStudio = onNavigateToStudio,
                    onOpenCreateSheet = { showCreateSheet = true },
                    onNavigateToStudiosTab = { selectedTab = 1 },
                    onNavigateToProjectsTab = { selectedTab = 3 }
                )
                1 -> StudiosDirectoryScreen(
                    onNavigateToStudio = onNavigateToStudio
                )
                3 -> ProjectsScreen(
                    viewModel = projectsViewModel,
                    onOpenProject = { project -> onNavigateToStudio(project.studioType) }
                )
                4 -> SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToPermissions = onNavigateToPermissions
                )
            }

            if (showCreateSheet) {
                CreateProjectSheet(
                    onDismiss = { showCreateSheet = false },
                    onSelectStudio = { studioId, _ ->
                        showCreateSheet = false
                        onNavigateToStudio(studioId)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    onNavigateToStudio: (String) -> Unit,
    onOpenCreateSheet: () -> Unit,
    onNavigateToStudiosTab: () -> Unit,
    onNavigateToProjectsTab: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val quickTools = remember {
        listOf(
            QuickToolItem("Edit Photo", "photo", "Import Photo", Icons.Outlined.PhotoCamera, KreaviloColors.PhotoStudioBadge),
            QuickToolItem("Remove BG", "product", "Import Product Photo", Icons.Outlined.AutoFixHigh, KreaviloColors.ProductStudioBadge),
            QuickToolItem("Create Poster", "design", "Choose Template", Icons.Outlined.DesignServices, KreaviloColors.DesignStudioBadge),
            QuickToolItem("Start Drawing", "drawing", "Blank Sketchbook", Icons.Outlined.Draw, KreaviloColors.DrawingStudioBadge),
            QuickToolItem("Record Voice", "audio", "Record Voiceover", Icons.Outlined.Mic, KreaviloColors.AudioStudioBadge),
            QuickToolItem("Video Reel", "video", "New Timeline", Icons.Outlined.MovieFilter, KreaviloColors.VideoStudioBadge),
            QuickToolItem("AR Tracing", "ar_drawing", "Camera Tracing", Icons.Outlined.CameraAlt, KreaviloColors.DrawingStudioBadge),
            QuickToolItem("Teleprompter", "writing", "Video Teleprompter", Icons.Outlined.Subtitles, KreaviloColors.WritingStudioBadge),
            QuickToolItem("2D Floorplan", "interior", "2D Floorplan", Icons.Outlined.MeetingRoom, KreaviloColors.InteriorStudioBadge),
            QuickToolItem("Thumbnails", "banner", "YouTube Thumbnail", Icons.Outlined.BurstMode, KreaviloColors.DesignStudioBadge)
        )
    }

    val studios = remember {
        listOf(
            StudioItem("photo", "Photo Studio", Icons.Outlined.Photo, KreaviloColors.PhotoStudioBadge, KreaviloColors.PhotoStudioCardBg),
            StudioItem("video", "Video Studio", Icons.Outlined.Videocam, KreaviloColors.VideoStudioBadge, KreaviloColors.VideoStudioCardBg),
            StudioItem("design", "Design Studio", Icons.Outlined.DesignServices, KreaviloColors.DesignStudioBadge, KreaviloColors.DesignStudioCardBg),
            StudioItem("drawing", "Drawing Studio", Icons.Outlined.Palette, KreaviloColors.DrawingStudioBadge, KreaviloColors.DrawingStudioCardBg),
            StudioItem("product", "Product Studio", Icons.Outlined.ShoppingBag, KreaviloColors.ProductStudioBadge, KreaviloColors.ProductStudioCardBg),
            StudioItem("interior", "Interior Studio", Icons.Outlined.Home, KreaviloColors.InteriorStudioBadge, KreaviloColors.InteriorStudioCardBg),
            StudioItem("writing", "Writing Studio", Icons.Outlined.Edit, KreaviloColors.WritingStudioBadge, KreaviloColors.WritingStudioCardBg),
            StudioItem("audio", "Audio Studio", Icons.Outlined.GraphicEq, KreaviloColors.AudioStudioBadge, KreaviloColors.AudioStudioCardBg),
            StudioItem("ar_drawing", "AR Tracing", Icons.Outlined.CameraAlt, KreaviloColors.DrawingStudioBadge, KreaviloColors.DrawingStudioCardBg),
            StudioItem("banner", "Banner Studio", Icons.Outlined.BurstMode, KreaviloColors.DesignStudioBadge, KreaviloColors.DesignStudioCardBg)
        )
    }

    val filteredStudios = remember(searchQuery) {
        if (searchQuery.isBlank()) studios
        else studios.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.creators_club_icon),
                    contentDescription = "Creators Club Logo",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Creators Club",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary,
                            fontSize = 20.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Create without limits",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = KreaviloColors.TextSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onNavigateToProjectsTab) {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = "Projects & Assets",
                    tint = KreaviloColors.Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hero Greeting Card with Landscape Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.promo_landscape_banner),
                    contentDescription = "Creators Club Studio Banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "What will you create today?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose a studio and bring your ideas to life.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = KreaviloColors.TextSecondary,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onOpenCreateSheet,
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Creative Tools Section
        Text(
            text = "Quick Creative Tools",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = KreaviloColors.TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            quickTools.forEach { tool ->
                Card(
                    modifier = Modifier
                        .width(108.dp)
                        .clickable { onNavigateToStudio(tool.studioId) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(KreaviloColors.Border))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(tool.color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = tool.title,
                                tint = tool.color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tool.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KreaviloColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Find any creative tool or studio", color = KreaviloColors.TextTertiary, fontSize = 14.sp) },
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

        Spacer(modifier = Modifier.height(20.dp))

        // Studios Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your creative studios",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = KreaviloColors.TextPrimary
                )
            )
            TextButton(onClick = onNavigateToStudiosTab) {
                Text(
                    text = "See all (${filteredStudios.size})",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = KreaviloColors.Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Studios Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            filteredStudios.chunked(2).forEach { rowStudios ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowStudios.forEach { studio ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(88.dp)
                                .clickable { onNavigateToStudio(studio.id) },
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
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = studio.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = KreaviloColors.TextPrimary,
                                        fontSize = 13.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (rowStudios.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
