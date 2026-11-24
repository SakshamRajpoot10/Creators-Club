package com.creatorsclub.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feature_registry_table")
data class FeatureRegistryEntity(
    @PrimaryKey
    @ColumnInfo(name = "feature_id")
    val featureId: String,

    @ColumnInfo(name = "display_name")
    val displayName: String,

    @ColumnInfo(name = "display_name_hi")
    val displayNameHi: String,

    @ColumnInfo(name = "aliases_csv")
    val aliasesCsv: String,

    @ColumnInfo(name = "studio_category")
    val studioCategory: String,

    @ColumnInfo(name = "navigation_route")
    val navigationRoute: String,

    @ColumnInfo(name = "required_inputs_json")
    val requiredInputsJson: String,

    @ColumnInfo(name = "action_type")
    val actionType: String,

    @ColumnInfo(name = "help_steps_json")
    val helpStepsJson: String
)
