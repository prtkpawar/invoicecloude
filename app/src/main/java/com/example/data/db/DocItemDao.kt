package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DocItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DocItemDao {
    @Query("SELECT * FROM doc_items WHERE docId = :docId ORDER BY sortOrder ASC")
    fun getItemsForDocFlow(docId: Int): Flow<List<DocItem>>

    @Query("SELECT * FROM doc_items WHERE docId = :docId ORDER BY sortOrder ASC")
    suspend fun getItemsForDoc(docId: Int): List<DocItem>

    @Query("SELECT * FROM doc_items WHERE docId = :docId AND businessId = :businessId ORDER BY sortOrder ASC")
    fun getItemsForDocFlow(docId: Int, businessId: Long): Flow<List<DocItem>>

    @Query("SELECT * FROM doc_items WHERE docId = :docId AND businessId = :businessId ORDER BY sortOrder ASC")
    suspend fun getItemsForDoc(docId: Int, businessId: Long): List<DocItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DocItem>)

    @Query("DELETE FROM doc_items WHERE docId = :docId")
    suspend fun deleteItemsForDoc(docId: Int)
}
