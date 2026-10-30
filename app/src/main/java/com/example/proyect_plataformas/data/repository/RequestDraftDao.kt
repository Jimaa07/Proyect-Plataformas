package com.example.proyect_plataformas.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RequestDraftDao {

    @Query("SELECT * FROM request_drafts WHERE providerId = :providerId LIMIT 1")
    suspend fun getDraft(providerId: String): RequestDraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: RequestDraftEntity)

    @Query("DELETE FROM request_drafts WHERE providerId = :providerId")
    suspend fun deleteDraft(providerId: String)
}
