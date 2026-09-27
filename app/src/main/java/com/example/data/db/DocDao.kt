package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Doc
import kotlinx.coroutines.flow.Flow

@Dao
interface DocDao {
    @Query("SELECT * FROM documents WHERE businessId = :businessId ORDER BY docDate DESC, id DESC")
    fun getAllDocsFlow(businessId: Long = 1L): Flow<List<Doc>>

    @Query("SELECT * FROM documents WHERE businessId = :businessId AND docType = :docType ORDER BY docDate DESC, id DESC")
    fun getDocsByTypeFlow(docType: String, businessId: Long = 1L): Flow<List<Doc>>

    @Query("SELECT * FROM documents WHERE businessId = :businessId AND docType = 'ESTIMATE' ORDER BY docDate DESC, id DESC")
    fun getSolarEstimatesFlow(businessId: Long = 1L): Flow<List<Doc>>

    @Query("SELECT * FROM documents WHERE businessId = :businessId AND docType = 'INVOICE' ORDER BY docDate DESC, id DESC")
    fun getSolarInvoicesFlow(businessId: Long = 1L): Flow<List<Doc>>

    @Query("SELECT * FROM documents WHERE businessId = :businessId AND docType IN ('CONST_ESTIMATE', 'CONST_INVOICE') ORDER BY docDate DESC, id DESC")
    fun getConstructionDocsFlow(businessId: Long = 1L): Flow<List<Doc>>

    @Query("SELECT * FROM documents WHERE businessId = :businessId AND docType = 'ESTIMATE' ORDER BY docDate DESC, id DESC")
    fun getAllEstimatesFlow(businessId: Long = 1L): Flow<List<Doc>>

    @Query("SELECT * FROM documents WHERE businessId = :businessId AND docType = 'ESTIMATE' ORDER BY docDate DESC, id DESC")
    suspend fun getAllEstimates(businessId: Long = 1L): List<Doc>

    @Query("SELECT * FROM documents WHERE businessId = :businessId AND customerId = :customerId ORDER BY docDate DESC, id DESC")
    fun getDocsByCustomerFlow(customerId: Int, businessId: Long = 1L): Flow<List<Doc>>

    @Query("SELECT * FROM documents WHERE businessId = :businessId AND docType = 'ESTIMATE' AND customerId = :customerId ORDER BY docDate DESC, id DESC")
    suspend fun getEstimatesByCustomer(customerId: Int, businessId: Long = 1L): List<Doc>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocById(id: Int): Doc?

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun getDocByIdFlow(id: Int): Flow<Doc?>

    @Query("SELECT * FROM documents WHERE parentEstimateId = :estimateId LIMIT 1")
    suspend fun getChildInvoiceForEstimate(estimateId: Int): Doc?

    @Query("SELECT COUNT(*) FROM documents WHERE businessId = :businessId AND docNo = :docNo")
    suspend fun countByDocNo(docNo: String, businessId: Long = 1L): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(doc: Doc): Long

    @Update
    suspend fun update(doc: Doc)

    @Delete
    suspend fun delete(doc: Doc)

    @Query("UPDATE documents SET status = :status, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET dueDate = :dueDate, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateDueDate(id: Int, dueDate: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET pdfPath = :pdfPath, updatedAt = :timestamp WHERE id = :id")
    suspend fun updatePdfPath(id: Int, pdfPath: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET terms = :terms, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateTerms(id: Int, terms: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM documents ORDER BY docDate DESC, id DESC")
    fun getAllDocsGlobalFlow(): Flow<List<Doc>>

    @Query("UPDATE documents SET whatsappShareCount = whatsappShareCount + 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun incrementWhatsAppShareCount(id: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET businessId = :businessId, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateDocBusinessId(id: Int, businessId: Long, timestamp: Long = System.currentTimeMillis())
}
