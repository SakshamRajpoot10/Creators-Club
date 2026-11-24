package com.creatorsclub.core.database.dao

import androidx.room.*
import com.creatorsclub.core.database.model.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects_table WHERE is_deleted = 0 ORDER BY modified_at DESC")
    fun getAllActiveProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects_table WHERE is_deleted = 0 AND studio_type = :studioType ORDER BY modified_at DESC")
    fun getProjectsByStudio(studioType: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects_table WHERE is_deleted = 0 AND is_favorite = 1 ORDER BY modified_at DESC")
    fun getFavoriteProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects_table WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    fun getDeletedProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects_table WHERE project_id = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("UPDATE projects_table SET is_favorite = :isFavorite WHERE project_id = :projectId")
    suspend fun setFavorite(projectId: String, isFavorite: Boolean)

    @Query("UPDATE projects_table SET is_deleted = 1, deleted_at = :deletedAt WHERE project_id = :projectId")
    suspend fun softDeleteProject(projectId: String, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE projects_table SET is_deleted = 0, deleted_at = NULL WHERE project_id = :projectId")
    suspend fun restoreProject(projectId: String)

    @Query("DELETE FROM projects_table WHERE project_id = :projectId")
    suspend fun hardDeleteProject(projectId: String)

    @Query("SELECT * FROM projects_table WHERE is_deleted = 0 AND name LIKE '%' || :query || '%' ORDER BY modified_at DESC")
    fun searchProjects(query: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects_table WHERE is_deleted = 1 AND (:currentTime - deleted_at) > :trashRetentionMs")
    suspend fun getProjectsDueForTrashPurge(currentTime: Long, trashRetentionMs: Long): List<ProjectEntity>

    @Query("SELECT * FROM projects_table WHERE (:currentTime - created_at) > :maxActiveRetentionMs")
    suspend fun getProjectsDueForHardExpiry(currentTime: Long, maxActiveRetentionMs: Long): List<ProjectEntity>

    @Query("SELECT * FROM projects_table")
    suspend fun getAllProjectsSync(): List<ProjectEntity>
}
