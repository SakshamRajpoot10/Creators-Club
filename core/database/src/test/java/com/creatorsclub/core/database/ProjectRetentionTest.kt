package com.creatorsclub.core.database

import com.creatorsclub.core.database.model.ProjectEntity
import com.creatorsclub.core.database.retention.ProjectRetentionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectRetentionTest {

    private val oneDayMs = 24L * 60 * 60 * 1000L

    @Test
    fun testActiveProjectHardRetention15Days() {
        val now = 1_700_000_000_000L
        val project = ProjectEntity(
            projectId = "p1",
            name = "Test",
            studioType = "PHOTO",
            createdAt = now - (10 * oneDayMs), // 10 days old
            modifiedAt = now - (10 * oneDayMs),
            canvasWidth = 1080,
            canvasHeight = 1080,
            aspectRatio = "1:1",
            thumbnailPath = "",
            projectFilePath = "",
            isFavorite = false,
            isDeleted = false,
            deletedAt = null
        )

        val daysRemaining = ProjectRetentionManager.calculateDaysRemaining(project, now)
        assertEquals(5, daysRemaining)
    }

    @Test
    fun testTrashRetention5DaysVs15DaysHardMax() {
        val now = 1_700_000_000_000L
        // Project created 14 days ago and soft-deleted today (14 days old).
        // 15-day hard max is in 1 day; trash retention 5 days is in 5 days.
        // Rule: effectivePurgeAt = min(createdAt + 15d, deletedAt + 5d) => 1 day remaining!
        val projectOld = ProjectEntity(
            projectId = "p2",
            name = "Test Old",
            studioType = "PHOTO",
            createdAt = now - (14 * oneDayMs),
            modifiedAt = now,
            canvasWidth = 1080,
            canvasHeight = 1080,
            aspectRatio = "1:1",
            thumbnailPath = "",
            projectFilePath = "",
            isFavorite = false,
            isDeleted = true,
            deletedAt = now // deleted today
        )

        val daysRemainingOld = ProjectRetentionManager.calculateDaysRemaining(projectOld, now)
        assertEquals(1, daysRemainingOld)

        // Project created 2 days ago and soft-deleted today.
        // 15-day hard max is in 13 days; trash retention 5 days is in 5 days.
        // effectivePurgeAt = min(13 days, 5 days) => 5 days remaining!
        val projectNew = ProjectEntity(
            projectId = "p3",
            name = "Test New",
            studioType = "PHOTO",
            createdAt = now - (2 * oneDayMs),
            modifiedAt = now,
            canvasWidth = 1080,
            canvasHeight = 1080,
            aspectRatio = "1:1",
            thumbnailPath = "",
            projectFilePath = "",
            isFavorite = false,
            isDeleted = true,
            deletedAt = now
        )

        val daysRemainingNew = ProjectRetentionManager.calculateDaysRemaining(projectNew, now)
        assertEquals(5, daysRemainingNew)
    }
}
