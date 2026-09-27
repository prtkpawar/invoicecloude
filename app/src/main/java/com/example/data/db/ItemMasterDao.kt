package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ItemMaster
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemMasterDao {
    @Query("SELECT * FROM item_master WHERE businessId = :businessId AND isActive = 1 ORDER BY sortOrder ASC")
    fun getActiveItemsFlow(businessId: Long = 1L): Flow<List<ItemMaster>>

    @Query("SELECT * FROM item_master WHERE businessId = :businessId AND isActive = 1 ORDER BY sortOrder ASC")
    suspend fun getActiveItems(businessId: Long = 1L): List<ItemMaster>

    @Query("SELECT * FROM item_master WHERE businessId = :businessId AND category = :category AND isActive = 1 ORDER BY sortOrder ASC")
    fun getItemsByCategoryFlow(category: String, businessId: Long = 1L): Flow<List<ItemMaster>>

    @Query("SELECT * FROM item_master WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Int): ItemMaster?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemMaster): Long

    @Update
    suspend fun update(item: ItemMaster)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemMaster>)

    @Query("DELETE FROM item_master WHERE businessId = :businessId")
    suspend fun deleteAll(businessId: Long = 1L)

    @Delete
    suspend fun delete(item: ItemMaster)
}
