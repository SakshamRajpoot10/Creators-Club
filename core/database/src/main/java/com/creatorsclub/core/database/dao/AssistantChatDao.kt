package com.creatorsclub.core.database.dao

import androidx.room.*
import com.creatorsclub.core.database.model.AssistantChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantChatDao {
    @Query("SELECT * FROM assistant_chats_table WHERE session_id = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<AssistantChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AssistantChatEntity)

    @Query("DELETE FROM assistant_chats_table WHERE session_id = :sessionId")
    suspend fun clearSession(sessionId: String)
}
