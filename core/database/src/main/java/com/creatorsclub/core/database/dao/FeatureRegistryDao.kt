package com.creatorsclub.core.database.dao

import androidx.room.*
import com.creatorsclub.core.database.model.FeatureRegistryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeatureRegistryDao {
    @Query("SELECT * FROM feature_registry_table ORDER BY display_name ASC")
    fun getAllFeatures(): Flow<List<FeatureRegistryEntity>>

    @Query("SELECT * FROM feature_registry_table WHERE studio_category = :category ORDER BY display_name ASC")
    fun getFeaturesByCategory(category: String): Flow<List<FeatureRegistryEntity>>

    @Query("SELECT * FROM feature_registry_table WHERE feature_id = :featureId LIMIT 1")
    suspend fun getFeatureById(featureId: String): FeatureRegistryEntity?

    @Query("""
        SELECT * FROM feature_registry_table 
        WHERE display_name LIKE '%' || :query || '%' 
           OR display_name_hi LIKE '%' || :query || '%' 
           OR aliases_csv LIKE '%' || :query || '%'
        ORDER BY display_name ASC
    """)
    fun searchFeatures(query: String): Flow<List<FeatureRegistryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeatures(features: List<FeatureRegistryEntity>)
}
