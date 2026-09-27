package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object ItemCategory {
    const val SOLAR_PANEL = "SOLAR_PANEL"
    const val INVERTER = "INVERTER"
    const val STRUCTURE = "STRUCTURE"
    const val CABLE = "CABLE"
    const val BATTERY = "BATTERY"
    const val ACCESSORY = "ACCESSORY"
    const val LABOR = "LABOR"
    const val CIVIL_WORK = "CIVIL_WORK"
    const val GRAINS_PULSES = "GRAINS_PULSES"
    const val OILS_GHEE = "OILS_GHEE"
    const val PACKAGED_FOOD = "PACKAGED_FOOD"
    const val DAIRY = "DAIRY"
    const val SPICES = "SPICES"
    const val HOUSEHOLD = "HOUSEHOLD"
    const val MEDICINE = "MEDICINE"
    const val HEALTHCARE = "HEALTHCARE"
    const val FIRST_AID = "FIRST_AID"
    const val HAIRCUT = "HAIRCUT"
    const val FACIAL = "FACIAL"
    const val SPA = "SPA"
    const val APPAREL = "APPAREL"
    const val FOOTWEAR = "FOOTWEAR"
    const val STATIONERY = "STATIONERY"
    const val BEVERAGES = "BEVERAGES"
    const val MEALS_THALI = "MEALS_THALI"
    const val FAST_FOOD = "FAST_FOOD"
    const val CEMENT_BUILDING = "CEMENT_BUILDING"
    const val PAINTS = "PAINTS"
    const val PLUMBING = "PLUMBING"
    const val ELECTRICAL = "ELECTRICAL"
    const val TOOLS = "TOOLS"
    const val SWEETS_MITHAI = "SWEETS_MITHAI"
    const val BAKERY = "BAKERY"
    const val CHARGING = "CHARGING"
    const val AUDIO = "AUDIO"
    const val ACCESSORIES = "ACCESSORIES"
    const val CONSULTING = "CONSULTING"
    const val MAINTENANCE = "MAINTENANCE"
    const val MISC = "MISC"

    val ALL = listOf(
        GRAINS_PULSES,
        OILS_GHEE,
        PACKAGED_FOOD,
        MEDICINE,
        HEALTHCARE,
        HAIRCUT,
        FACIAL,
        APPAREL,
        BEVERAGES,
        MEALS_THALI,
        SWEETS_MITHAI,
        BAKERY,
        ELECTRICAL,
        HARDWARE_TOOLS,
        PLUMBING,
        PAINTS,
        CEMENT_BUILDING,
        SOLAR_PANEL,
        INVERTER,
        STRUCTURE,
        CABLE,
        BATTERY,
        ACCESSORY,
        LABOR,
        CIVIL_WORK,
        CHARGING,
        AUDIO,
        CONSULTING,
        MISC
    ).distinct()

    const val HARDWARE_TOOLS = "TOOLS"
}

object ItemUnit {
    const val NOS = "NOS"
    const val PCS = "PCS"
    const val KG = "KG"
    const val GM = "GM"
    const val LTR = "LTR"
    const val ML = "ML"
    const val PKT = "PKT"
    const val BOX = "BOX"
    const val STRIP = "STRIP"
    const val BOTTLE = "BOTTLE"
    const val TUBE = "TUBE"
    const val PLATE = "PLATE"
    const val CUP = "CUP"
    const val BAG = "BAG"
    const val PAIR = "PAIR"
    const val SET = "SET"
    const val ROLL = "ROLL"
    const val BUCKET = "BUCKET"
    const val CASE = "CASE"
    const val CARTON = "CARTON"
    const val TIN = "TIN"
    const val DOZEN = "DOZEN"
    const val SERVICE = "SERVICE"
    const val SITTING = "SITTING"
    const val PKG = "PKG"
    const val HOUR = "HOUR"
    const val DAY = "DAY"
    const val MONTH = "MONTH"
    const val MTR = "MTR"
    const val SQFT = "SQFT"
    const val BRASS = "BRASS"
    const val JOB = "JOB"
    const val TRUCK = "TRUCK"
    const val WATT = "WATT"
    const val KW = "KW"

    val ALL = listOf(
        PCS,
        NOS,
        KG,
        GM,
        LTR,
        ML,
        PKT,
        BOX,
        STRIP,
        BOTTLE,
        TUBE,
        PLATE,
        CUP,
        BAG,
        PAIR,
        SET,
        ROLL,
        BUCKET,
        CASE,
        CARTON,
        TIN,
        DOZEN,
        SERVICE,
        HOUR,
        DAY,
        MONTH,
        MTR,
        SQFT,
        BRASS,
        JOB,
        KW
    ).distinct()
}

@Entity(
    tableName = "item_master",
    indices = [
        Index("businessId")
    ]
)
data class ItemMaster(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessId: Long = 1L,
    val label: String = "",
    val description: String = "",
    val hsn: String = "85414300",
    val category: String = ItemCategory.SOLAR_PANEL,
    val unit: String = ItemUnit.NOS,
    val taxRate: Double = 18.0,
    val rate: Double = 0.0,
    val purchasePrice: Double? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val businessType: String = "",
    val mrp: Double? = null,
    val barcode: String? = null,
    val batchNo: String? = null,
    val expiry: String? = null,
    val warranty: String? = null,
    val brand: String? = null,
    val extraAttributes: String? = null
)
