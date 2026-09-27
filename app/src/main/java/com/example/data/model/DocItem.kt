package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "doc_items",
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
data class DocItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessId: Long = 1L,
    val docId: Int = 0,
    val label: String = "",
    val description: String = "",
    val hsn: String = "85414300",
    val qty: Double = 1.0,
    val unit: String = "set",
    val rate: Double = 0.0,
    val amount: Double = 0.0,
    val sortOrder: Int = 0
)
