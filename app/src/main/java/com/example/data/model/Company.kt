package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company")
data class Company(
    @PrimaryKey val id: Int = 1,
    val name: String = Firm.LEGAL_NAME,
    val address: String = Firm.ADDRESS_ONE_LINE,
    val email: String = Firm.EMAIL,
    val gstin: String = Firm.GSTIN,
    val placeOfSupply: String = Firm.PLACE_OF_SUPPLY,
    val bankName: String = Firm.BANK_NAME,
    val accountName: String = Firm.ACCOUNT_NAME,
    val accountNumber: String = Firm.ACCOUNT_NUMBER,
    val ifsc: String = Firm.IFSC,
    val signatoryName: String = Firm.SIGNED_BY_PERSON
)
