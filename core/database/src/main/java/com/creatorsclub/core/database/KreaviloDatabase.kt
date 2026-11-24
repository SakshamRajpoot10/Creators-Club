package com.creatorsclub.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.creatorsclub.core.database.dao.AssistantChatDao
import com.creatorsclub.core.database.dao.AssetDao
import com.creatorsclub.core.database.dao.FeatureRegistryDao
import com.creatorsclub.core.database.dao.ProjectDao
import com.creatorsclub.core.database.model.AssistantChatEntity
import com.creatorsclub.core.database.model.AssetEntity
import com.creatorsclub.core.database.model.FeatureRegistryEntity
import com.creatorsclub.core.database.model.ProjectEntity

@Database(
    entities = [
        ProjectEntity::class,
        AssetEntity::class,
        FeatureRegistryEntity::class,
        AssistantChatEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KreaviloDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun assetDao(): AssetDao
    abstract fun featureRegistryDao(): FeatureRegistryDao
    abstract fun assistantChatDao(): AssistantChatDao

    companion object {
        private const val DATABASE_NAME = "kreavilo_studio.db"

        @Volatile
        private var instance: KreaviloDatabase? = null

        fun getInstance(context: Context): KreaviloDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KreaviloDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
            }
        }
    }
}
