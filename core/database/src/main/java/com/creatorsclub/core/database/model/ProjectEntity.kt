package com.creatorsclub.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "projects_table",
    indices = [
        Index(value = ["modified_at"], orders = [Index.Order.DESC]),
        Index(value = ["studio_type"]),
        Index(value = ["is_deleted"])
    ]
)
data class ProjectEntity(
    @PrimaryKey
    @ColumnInfo(name = "project_id")
    val projectId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "studio_type")
    val studioType: String, // 'PHOTO', 'DESIGN', 'VIDEO', 'DRAWING', 'PRODUCT', etc.

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long,

    @ColumnInfo(name = "canvas_width")
    val canvasWidth: Int,

    @ColumnInfo(name = "canvas_height")
    val canvasHeight: Int,

    @ColumnInfo(name = "aspect_ratio")
    val aspectRatio: String,

    @ColumnInfo(name = "thumbnail_path")
    val thumbnailPath: String,

    @ColumnInfo(name = "project_file_path")
    val projectFilePath: String,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null
)
