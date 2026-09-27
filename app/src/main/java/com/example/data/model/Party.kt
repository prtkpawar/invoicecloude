package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PartyCategory {
    CUSTOMER,
    VENDOR,
    BOTH
}

@Entity(
    tableName = "parties",
    indices = [
        Index("businessId")
    ]
)
data class Party(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessId: Long = 1L,
    val name: String = "",
    val phone: String = "",
    val mobile: String = phone,
    val email: String = "",
    val address: String = "",
    val village: String = "",
    val city: String = "",
    val state: String = "Maharashtra",
    val pincode: String = "",
    val gstin: String = "",
    val consumerNumber: String = "",
    val sanctionLoad: String = "",
    val notes: String = "",
    val category: PartyCategory = PartyCategory.CUSTOMER,
    val photoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

typealias Customer = Party
