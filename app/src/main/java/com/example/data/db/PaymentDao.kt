package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE docId = :docId ORDER BY payDate DESC, id DESC")
    fun getPaymentsForDocFlow(docId: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE docId = :docId ORDER BY payDate DESC, id DESC")
    suspend fun getPaymentsForDoc(docId: Int): List<Payment>

    @Query("SELECT * FROM payments WHERE docId = :docId AND businessId = :businessId ORDER BY payDate DESC, id DESC")
    fun getPaymentsForDocFlow(docId: Int, businessId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE docId = :docId AND businessId = :businessId ORDER BY payDate DESC, id DESC")
    suspend fun getPaymentsForDoc(docId: Int, businessId: Long): List<Payment>

    @Query("""
        SELECT p.* FROM payments p
        INNER JOIN documents d ON p.docId = d.id
        WHERE d.customerId = :customerId AND p.businessId = :businessId
        ORDER BY p.payDate ASC, p.id ASC
    """)
    fun getPaymentsForCustomerFlow(customerId: Int, businessId: Long = 1L): Flow<List<Payment>>

    @Query("""
        SELECT p.* FROM payments p
        INNER JOIN documents d ON p.docId = d.id
        WHERE d.customerId = :customerId AND p.businessId = :businessId
        ORDER BY p.payDate ASC, p.id ASC
    """)
    suspend fun getPaymentsForCustomer(customerId: Int, businessId: Long = 1L): List<Payment>

    @Query("SELECT * FROM payments WHERE businessId = :businessId ORDER BY payDate DESC, id DESC")
    fun getAllPaymentsFlow(businessId: Long = 1L): Flow<List<Payment>>

    @Query("SELECT IFNULL(SUM(amount), 0.0) FROM payments WHERE docId = :docId AND businessId = :businessId")
    fun getTotalPaidForDocFlow(docId: Int, businessId: Long = 1L): Flow<Double>

    @Query("SELECT IFNULL(SUM(amount), 0.0) FROM payments WHERE docId = :docId AND businessId = :businessId")
    suspend fun getTotalPaidForDoc(docId: Int, businessId: Long = 1L): Double

    @Query("SELECT IFNULL(SUM(amount), 0.0) FROM payments WHERE businessId = :businessId AND payDate >= :sinceDate")
    fun getPaymentsSinceFlow(sinceDate: String, businessId: Long = 1L): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment): Long

    @Update
    suspend fun update(payment: Payment)

    @Delete
    suspend fun delete(payment: Payment)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deleteById(id: Int)
}
