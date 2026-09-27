package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Party
import com.example.data.model.PartyCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties WHERE businessId = :businessId ORDER BY name COLLATE NOCASE ASC")
    fun getAllPartiesFlow(businessId: Long = 1L): Flow<List<Party>>

    @Query("SELECT * FROM parties WHERE businessId = :businessId AND category = :category ORDER BY name COLLATE NOCASE ASC")
    fun getPartiesByCategoryFlow(category: PartyCategory, businessId: Long = 1L): Flow<List<Party>>

    @Query("SELECT * FROM parties WHERE businessId = :businessId AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR village LIKE '%' || :query || '%') ORDER BY name COLLATE NOCASE ASC")
    fun searchPartiesFlow(query: String, businessId: Long = 1L): Flow<List<Party>>

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    suspend fun getPartyById(id: Int): Party?

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    fun getPartyByIdFlow(id: Int): Flow<Party?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(party: Party): Long

    @Update
    suspend fun update(party: Party)

    @Delete
    suspend fun delete(party: Party)

    @Query("SELECT COUNT(*) FROM documents WHERE customerId = :partyId AND businessId = :businessId")
    suspend fun getDocumentCountForParty(partyId: Int, businessId: Long = 1L): Int

    // Backwards-compatible aliases for legacy CustomerDao queries
    @Query("SELECT * FROM parties WHERE businessId = :businessId ORDER BY name COLLATE NOCASE ASC")
    fun getAllCustomersFlow(businessId: Long = 1L): Flow<List<Party>>

    @Query("SELECT * FROM parties WHERE businessId = :businessId AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR village LIKE '%' || :query || '%') ORDER BY name COLLATE NOCASE ASC")
    fun searchCustomersFlow(query: String, businessId: Long = 1L): Flow<List<Party>>

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Int): Party?

    @Query("SELECT COUNT(*) FROM documents WHERE customerId = :customerId AND businessId = :businessId")
    suspend fun getDocumentCountForCustomer(customerId: Int, businessId: Long = 1L): Int
}

typealias CustomerDao = PartyDao
