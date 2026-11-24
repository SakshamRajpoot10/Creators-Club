package com.creatorsclub.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assistant_chats_table",
    indices = [
        Index(value = ["session_id", "timestamp"])
    ]
)
data class AssistantChatEntity(
    @PrimaryKey
    @ColumnInfo(name = "message_id")
    val messageId: String,

    @ColumnInfo(name = "session_id")
    val sessionId: String,

    @ColumnInfo(name = "sender")
    val sender: String, // 'USER' or 'CREATOR_AI'

    @ColumnInfo(name = "text_content")
    val textContent: String,

    @ColumnInfo(name = "suggested_action_id")
    val suggestedActionId: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)
