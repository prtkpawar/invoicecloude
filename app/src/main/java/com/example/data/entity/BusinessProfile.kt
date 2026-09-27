package com.example.data.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val legalName: String = "",
    val brandName: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "Maharashtra",
    val pincode: String = "",
    val gstin: String? = null,
    val pan: String? = null,
    val bankAccountNo: String? = null,
    val ifsc: String? = null,
    val upiVpa: String? = null,
    val signatoryName: String? = null,
    val logoPath: String? = null,
    val signaturePath: String? = null,
    val businessTemplateId: Long = 3L,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    @get:Ignore
    val category: String
        get() = when (businessTemplateId) {
            1L -> "SOLAR"
            2L -> "CONSTRUCTION"
            3L -> "KIRANA"
            4L -> "PHARMACY"
            5L -> "SALON"
            6L -> "RETAIL"
            7L -> "DISTRIBUTOR"
            8L -> "RESTAURANT"
            9L -> "HARDWARE"
            10L -> "SERVICES"
            11L -> "ELECTRONICS"
            12L -> "SWEET_SHOP"
            else -> "KIRANA"
        }

    /**
     * Mobile number placeholder — BusinessProfile currently has no phone column.
     * Returns empty until the DB is migrated to include a phone field.
     * TODO: Add phone column in MIGRATION_9_10
     */
    @get:Ignore
    val mobile: String
        get() = ""

    @get:Ignore
    val placeOfSupply: String
        get() = "$state (${stateToGstCode(state)})"

    companion object {
        /** Maps Indian state names to their GST state codes */
        fun stateToGstCode(state: String): String = when (state.trim().lowercase()) {
            "jammu and kashmir", "jammu & kashmir" -> "01"
            "himachal pradesh" -> "02"
            "punjab" -> "03"
            "chandigarh" -> "04"
            "uttarakhand" -> "05"
            "haryana" -> "06"
            "delhi" -> "07"
            "rajasthan" -> "08"
            "uttar pradesh" -> "09"
            "bihar" -> "10"
            "sikkim" -> "11"
            "arunachal pradesh" -> "12"
            "nagaland" -> "13"
            "manipur" -> "14"
            "mizoram" -> "15"
            "tripura" -> "16"
            "meghalaya" -> "17"
            "assam" -> "18"
            "west bengal" -> "19"
            "jharkhand" -> "20"
            "odisha" -> "21"
            "chhattisgarh" -> "22"
            "madhya pradesh" -> "23"
            "gujarat" -> "24"
            "dadra and nagar haveli and daman and diu" -> "26"
            "maharashtra" -> "27"
            "andhra pradesh" -> "37"
            "karnataka" -> "29"
            "goa" -> "30"
            "lakshadweep" -> "31"
            "kerala" -> "32"
            "tamil nadu" -> "33"
            "puducherry" -> "34"
            "andaman and nicobar islands" -> "35"
            "telangana" -> "36"
            "ladakh" -> "38"
            else -> "27" // Default to Maharashtra
        }
    }
}
