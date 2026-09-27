package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.DraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftDao {

    /** Observe the latest draft for a specific document (or new doc when docId is null) */
    @Query("SELECT * FROM drafts WHERE docType = :docType AND (docId = :docId OR (docId IS NULL AND :docId IS NULL)) AND businessId = :businessId ORDER BY lastModified DESC LIMIT 1")
    fun observeLatest(docType: String, docId: Int?, businessId: Long): Flow<DraftEntity?>

    /** Get all drafts for a business (for draft list/cleanup) */
    @Query("SELECT * FROM drafts WHERE businessId = :businessId ORDER BY lastModified DESC")
    fun getAllDraftsFlow(businessId: Long): Flow<List<DraftEntity>>

    /** Upsert a draft (insert or replace) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(draft: DraftEntity)

    /** Delete a specific draft by ID */
    @Query("DELETE FROM drafts WHERE draftId = :draftId")
    suspend fun delete(draftId: Long)

    /** Delete all drafts for a specific document */
    @Query("DELETE FROM drafts WHERE docId = :docId AND docType = :docType")
    suspend fun deleteForDoc(docId: Int, docType: String)

    /** Delete all drafts older than a given timestamp */
    @Query("DELETE FROM drafts WHERE lastModified < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)

    /** Count drafts (for dashboard badge) */
    @Query("SELECT COUNT(*) FROM drafts WHERE businessId = :businessId")
    suspend fun countDrafts(businessId: Long): Int
}
