package com.creatorsclub.feature.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorsclub.core.database.model.ProjectEntity
import com.creatorsclub.core.designsystem.theme.KreaviloColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel,
    onOpenProject: (ProjectEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Projects Library",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KreaviloColors.TextPrimary
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Filter Pills (All, Favorites, Trash)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedFilter == ProjectFilterTab.ALL,
                    onClick = { viewModel.selectFilter(ProjectFilterTab.ALL) },
                    label = { Text("All Projects", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = KreaviloColors.Primary, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = uiState.selectedFilter == ProjectFilterTab.FAVORITES,
                    onClick = { viewModel.selectFilter(ProjectFilterTab.FAVORITES) },
                    label = { Text("Favorites", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = KreaviloColors.Accent, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = uiState.selectedFilter == ProjectFilterTab.TRASH,
                    onClick = { viewModel.selectFilter(ProjectFilterTab.TRASH) },
                    label = { Text("Trash", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Projects List
            if (uiState.projects.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.projects) { project ->
                        ProjectCardItem(
                            project = project,
                            isTrashMode = uiState.selectedFilter == ProjectFilterTab.TRASH,
                            onClick = { onOpenProject(project) },
                            onToggleFavorite = { viewModel.toggleFavorite(project.projectId, project.isFavorite) },
                            onDelete = { viewModel.softDelete(project.projectId) },
                            onRestore = { viewModel.restore(project.projectId) },
                            onHardDelete = { viewModel.hardDelete(project.projectId) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (uiState.selectedFilter == ProjectFilterTab.TRASH) "Trash is empty" else "No projects created yet",
                            color = KreaviloColors.TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCardItem(
    project: ProjectEntity,
    isTrashMode: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit,
    onHardDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = KreaviloColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(KreaviloColors.PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DesignServices,
                        contentDescription = null,
                        tint = KreaviloColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = project.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KreaviloColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${project.studioType} Studio  •  ${project.aspectRatio}",
                        fontSize = 11.sp,
                        color = KreaviloColors.TextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isTrashMode) {
                    IconButton(onClick = onRestore) {
                        Icon(Icons.Default.Restore, contentDescription = "Restore", tint = KreaviloColors.Success)
                    }
                    IconButton(onClick = onHardDelete) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Permanent Delete", tint = KreaviloColors.Error)
                    }
                } else {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (project.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (project.isFavorite) Color(0xFFF59E0B) else KreaviloColors.TextTertiary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = KreaviloColors.TextSecondary)
                    }
                }
            }
        }
    }
}
