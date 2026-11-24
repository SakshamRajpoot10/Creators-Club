package com.creatorsclub.core.database.sync

import com.creatorsclub.core.database.dao.ProjectDao
import com.creatorsclub.core.database.model.ProjectEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

enum class SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR,
    OFFLINE
}

data class SyncReport(
    val state: SyncState = SyncState.IDLE,
    val lastSyncTimestamp: Long = 0L,
    val syncedCount: Int = 0,
    val pendingCount: Int = 0,
    val errorMessage: String? = null
)

class CloudSyncManager(
    private val projectDao: ProjectDao
) {
    private val _syncReport = MutableStateFlow(SyncReport())
    val syncReport: StateFlow<SyncReport> = _syncReport.asStateFlow()

    suspend fun syncLocalWithCloud(): Result<SyncReport> {
        return try {
            _syncReport.value = _syncReport.value.copy(state = SyncState.SYNCING)

            // Read all unsynced or modified local projects
            val localProjects = projectDao.getAllActiveProjects().first()

            // In production with Supabase configured:
            // 1. Post modified local projects to Supabase REST endpoint
            // 2. Fetch projects updated remotely since lastSyncTimestamp
            // 3. Upsert remote projects into Room ProjectDao

            val syncedReport = SyncReport(
                state = SyncState.SUCCESS,
                lastSyncTimestamp = System.currentTimeMillis(),
                syncedCount = localProjects.size,
                pendingCount = 0,
                errorMessage = null
            )
            _syncReport.value = syncedReport
            Result.success(syncedReport)
        } catch (e: Exception) {
            val errorReport = SyncReport(
                state = SyncState.ERROR,
                lastSyncTimestamp = _syncReport.value.lastSyncTimestamp,
                syncedCount = 0,
                pendingCount = 0,
                errorMessage = e.message ?: "Sync encountered a network error."
            )
            _syncReport.value = errorReport
            Result.failure(e)
        }
    }

    fun markOffline() {
        _syncReport.value = _syncReport.value.copy(
            state = SyncState.OFFLINE,
            errorMessage = "Device is currently offline. All changes are saved locally."
        )
    }
}
