package com.creatorsclub.feature.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creatorsclub.core.database.KreaviloDatabase
import com.creatorsclub.core.database.model.ProjectEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProjectFilterTab {
    ALL,
    FAVORITES,
    TRASH
}

data class ProjectsUiState(
    val selectedFilter: ProjectFilterTab = ProjectFilterTab.ALL,
    val searchQuery: String = "",
    val projects: List<ProjectEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val db = KreaviloDatabase.getInstance(application)
    private val projectDao = db.projectDao()

    private val _selectedFilter = MutableStateFlow(ProjectFilterTab.ALL)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<ProjectsUiState> = combine(
        _selectedFilter,
        _searchQuery,
        _selectedFilter.flatMapLatest { filter ->
            when (filter) {
                ProjectFilterTab.ALL -> projectDao.getAllActiveProjects()
                ProjectFilterTab.FAVORITES -> projectDao.getFavoriteProjects()
                ProjectFilterTab.TRASH -> projectDao.getDeletedProjects()
            }
        }
    ) { filter, query, projectList ->
        val filtered = if (query.isBlank()) {
            projectList
        } else {
            projectList.filter { it.name.contains(query, ignoreCase = true) }
        }
        ProjectsUiState(
            selectedFilter = filter,
            searchQuery = query,
            projects = filtered
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProjectsUiState())

    fun selectFilter(filter: ProjectFilterTab) {
        _selectedFilter.value = filter
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(projectId: String, currentFavorite: Boolean) {
        viewModelScope.launch {
            projectDao.setFavorite(projectId, !currentFavorite)
        }
    }

    fun softDelete(projectId: String) {
        viewModelScope.launch {
            projectDao.softDeleteProject(projectId)
        }
    }

    fun restore(projectId: String) {
        viewModelScope.launch {
            projectDao.restoreProject(projectId)
        }
    }

    fun hardDelete(projectId: String) {
        viewModelScope.launch {
            projectDao.hardDeleteProject(projectId)
        }
    }
}
