package com.creatorsclub.core.database.dao

import androidx.room.*
import com.creatorsclub.core.database.model.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets_table WHERE project_id = :projectId ORDER BY created_at ASC")
    fun getAssetsForProject(projectId: String): Flow<List<AssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("DELETE FROM assets_table WHERE project_id = :projectId")
    suspend fun deleteAllAssetsForProject(projectId: String)
}
