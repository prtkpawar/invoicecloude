package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Doc::class,
            parentColumns = ["id"],
            childColumns = ["docId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("docId"),
        Index("businessId")
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessId: Long = 1L,
    val docId: Int = 0,
    val payDate: String = "",
    val amount: Double = 0.0,
    val mode: String = "CASH", // CASH, UPI, BANK, CHEQUE
    val reference: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
