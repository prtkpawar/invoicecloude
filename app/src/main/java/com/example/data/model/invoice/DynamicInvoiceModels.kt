package com.example.data.model.invoice

import android.graphics.Paint
import com.example.data.entity.BusinessProfile
import com.example.data.model.FirmBranding

enum class InvoiceBusinessType(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val defaultHsn: String,
    val description: String
) {
    SOLAR("SOLAR_ENERGY", "Solar & Renewable EPC", "☀️", "85414300", "Rooftop, Net-Metering, kW Capacity & Panel Makes"),
    MEDICAL("MEDICAL_PHARMACY", "Medical Store & Pharmacy", "💊", "30049099", "Rx Drugs, Batch No, Expiry MM/YY & Doctor Details"),
    KIRANA("KIRANA_GROCERY", "Kirana, Grocery & Supermarket", "🛒", "21069099", "FMCG Commodities, Weights (kg/g/L), MRP & Loose Items"),
    CONSTRUCTION("CONSTRUCTION", "Civil Construction & Contracting", "🏗️", "995411", "BOQ Work Schedules, Site Locations, Brass & Sq.Ft"),
    ELECTRONICS_RETAIL("ELECTRONICS_MOBILE", "Electronics & Retail", "📱", "85044090", "IMEI / Serial Numbers, Brands, Models & Warranty"),
    RESTAURANT("HOTEL_RESTAURANT", "Hotel, Cafe & Restaurant", "🍽️", "996331", "Table / Token Billing, KOT, Dine-In / Takeaway & FSSAI"),
    HARDWARE("HARDWARE_SANITARY", "Hardware & Sanitary", "🔧", "25232930", "Building Materials, Pipes, Grades, Gauges & Paints"),
    SALON("SALON_SPA", "Salon, Spa & Wellness", "💇", "999721", "Services, Stylists, Treatment Durations & Packages"),
    SERVICES("SERVICES_CONSULTING", "Professional Services", "💼", "998311", "Consultancy, AMC, SAC Codes & Milestone Billing"),
    GENERAL("GENERAL", "General Trading & Commercial", "🏢", "998719", "General Wholesale, Retail, Tax Invoices & Trade");

    companion object {
        fun fromTemplateId(templateId: Long): InvoiceBusinessType {
            return when (templateId) {
                1L -> SOLAR
                2L -> CONSTRUCTION
                3L -> KIRANA
                4L -> MEDICAL
                5L -> SALON
                6L, 7L -> GENERAL
                8L -> RESTAURANT
                9L -> HARDWARE
                10L -> SERVICES
                11L -> ELECTRONICS_RETAIL
                12L -> KIRANA
                else -> GENERAL
            }
        }

        fun fromCategory(category: String?): InvoiceBusinessType {
            if (category == null) return GENERAL
            val u = category.uppercase()
            return when {
                u.contains("MEDIC") || u.contains("PHARM") -> MEDICAL
                u.contains("KIRANA") || u.contains("GROCERY") -> KIRANA
                u.contains("SOLAR") -> SOLAR
                u.contains("CONST") || u.contains("CIVIL") -> CONSTRUCTION
                u.contains("ELEC") || u.contains("MOBILE") -> ELECTRONICS_RETAIL
                u.contains("REST") || u.contains("HOTEL") || u.contains("CAFE") -> RESTAURANT
                u.contains("HARDWARE") -> HARDWARE
                u.contains("SALON") || u.contains("SPA") -> SALON
                u.contains("SERV") -> SERVICES
                else -> GENERAL
            }
        }
    }
}

enum class DynamicFieldType {
    TEXT,
    NUMBER,
    DATE,
    DROPDOWN,
    CHIP_SELECTOR,
    STEPPER,
    TOGGLE
}

data class DynamicFieldDef(
    val key: String,
    val label: String,
    val placeholder: String = "",
    val fieldType: DynamicFieldType = DynamicFieldType.TEXT,
    val options: List<String> = emptyList(),
    val weight: Float = 1.0f,
    val isRequired: Boolean = false,
    val iconEmoji: String = ""
)

data class InvoiceColumnDef(
    val key: String,
    val header: String,
    val weight: Float,
    val align: Paint.Align = Paint.Align.LEFT
)

data class DynamicLineItem(
    val srNo: Int = 1,
    val label: String = "",
    val description: String = "",
    val hsn: String = "",
    val batchNo: String? = null,
    val expiryDate: String? = null,
    val weightOrMeasure: String? = null,
    val unit: String = "Nos",
    val qty: Double = 1.0,
    val mrp: Double? = null,
    val rate: Double = 0.0,
    val discount: Double = 0.0,
    val taxableAmount: Double = 0.0,
    val gstRate: Double = 0.0,
    val cgst: Double = 0.0,
    val sgst: Double = 0.0,
    val totalAmount: Double = 0.0,
    val warranty: String? = null,
    val brand: String? = null
)

data class InvoiceHeaderMeta(
    val doctorName: String? = null,
    val rxNumber: String? = null,
    val drugLicenseNo: String? = null,
    val siteLocation: String? = null,
    val boqCode: String? = null,
    val vehicleNo: String? = null,
    val customerGstin: String? = null,
    val tableOrTokenNo: String? = null,
    val consumerNumber: String? = null,
    val discomName: String? = null
)

data class DynamicInvoiceData(
    val firm: FirmBranding,
    val businessType: InvoiceBusinessType,
    val docTitle: String,
    val docSubtitle: String,
    val docNo: String,
    val docDate: String,
    val dueDateOrValidTill: String = "",
    val isInvoice: Boolean = true,
    val customerName: String,
    val customerMobile: String = "",
    val customerAddress: String = "",
    val customerGstin: String? = null,
    val headerMeta: InvoiceHeaderMeta = InvoiceHeaderMeta(),
    val columns: List<InvoiceColumnDef> = emptyList(),
    val items: List<DynamicLineItem> = emptyList(),
    val taxableSubtotal: Double = 0.0,
    val discountTotal: Double = 0.0,
    val cgstTotal: Double = 0.0,
    val sgstTotal: Double = 0.0,
    val grandTotal: Double = 0.0,
    val totalPaid: Double = 0.0,
    val balanceDue: Double = 0.0,
    val amountInWords: String = "",
    val note: String = "",
    val terms: List<String> = emptyList()
)
