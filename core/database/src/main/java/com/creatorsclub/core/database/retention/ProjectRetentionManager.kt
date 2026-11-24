package com.creatorsclub.core.database.retention

import android.content.Context
import com.creatorsclub.core.database.KreaviloDatabase
import com.creatorsclub.core.database.model.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ProjectRetentionManager {

    const val MAX_ACTIVE_RETENTION_MS = 15L * 24 * 60 * 60 * 1000L // 15 Days Hard Maximum
    const val TRASH_RETENTION_MS = 5L * 24 * 60 * 60 * 1000L        // 5 Days Trash Retention

    /**
     * Calculates the effective purge timestamp for any project according to the specification:
     * effectivePurgeAt = min(createdAt + 15 days, deletedAt + 5 days)
     */
    fun calculateEffectivePurgeAt(project: ProjectEntity): Long {
        val hardExpiry = project.createdAt + MAX_ACTIVE_RETENTION_MS
        val trashExpiry = if (project.isDeleted && project.deletedAt != null) {
            project.deletedAt + TRASH_RETENTION_MS
        } else {
            Long.MAX_VALUE
        }
        return minOf(hardExpiry, trashExpiry)
    }

    /**
     * Calculates days remaining before permanent purge.
     */
    fun calculateDaysRemaining(project: ProjectEntity, currentTime: Long = System.currentTimeMillis()): Int {
        val purgeAt = calculateEffectivePurgeAt(project)
        val diffMs = purgeAt - currentTime
        if (diffMs <= 0) return 0
        val oneDay = 24.0 * 60 * 60 * 1000
        return kotlin.math.ceil(diffMs.toDouble() / oneDay).toInt()
    }

    /**
     * Executes retention purge across Room database.
     * Deletes only app-owned private project files (recipes, thumbnails).
     * NEVER deletes external gallery photos or user-exported photos!
     */
    suspend fun performRetentionCleanup(context: Context, currentTime: Long = System.currentTimeMillis()): Int = withContext(Dispatchers.IO) {
        val db = KreaviloDatabase.getInstance(context)
        val projectDao = db.projectDao()

        val allProjects = projectDao.getAllProjectsSync()
        var purgedCount = 0

        for (project in allProjects) {
            val purgeAt = calculateEffectivePurgeAt(project)
            if (currentTime >= purgeAt) {
                // Safely delete app-owned project cache file if within internal storage
                runCatching {
                    val appFilesDir = context.getExternalFilesDir(null)
                    val projectFile = File(project.projectFilePath)
                    if (appFilesDir != null && projectFile.exists() && projectFile.absolutePath.startsWith(appFilesDir.absolutePath)) {
                        projectFile.delete()
                    }
                    val thumbFile = File(project.thumbnailPath)
                    if (appFilesDir != null && thumbFile.exists() && thumbFile.absolutePath.startsWith(appFilesDir.absolutePath)) {
                        thumbFile.delete()
                    }
                }

                // Hard delete row from SQLite
                projectDao.hardDeleteProject(project.projectId)
                purgedCount++
            }
        }

        purgedCount
    }
}
