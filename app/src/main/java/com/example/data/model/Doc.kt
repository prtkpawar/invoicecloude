package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.math.RoundingMode

object DocType {
    const val ESTIMATE = "ESTIMATE"
    const val INVOICE = "INVOICE"
    const val CONST_ESTIMATE = "CONST_ESTIMATE"
    const val CONST_INVOICE = "CONST_INVOICE"
}

const val DEFAULT_ESTIMATE_TERMS = """1. Quotation validity: strictly 7 days.
2. DISCOM load extension / Demand Note: consumer scope.
3. Power yield: contingent on solar irradiance and weather.
4. PV modules generate DC current exclusively under sunlight.
5. Sprinkler system: charged extra.
6. Civil and masonry works: consumer scope.
7. Elevated / customized mounting structure: charged extra.
8. Maintenance walkway platform: charged extra.
9. With good sunlight, 3 kW of solar gives you 400+ units a month, saving over ₹45,000+ every year."""

object DocStatus {
    const val ESTIMATE = "ESTIMATE"
    const val CONVERTED = "CONVERTED"
    const val UNPAID = "UNPAID"
    const val PARTIAL = "PARTIAL"
    const val PAID = "PAID"
    const val CANCELLED = "CANCELLED"
}

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = Party::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("docNo", unique = true),
        Index("customerId"),
        Index("docType"),
        Index("businessId")
    ]
)
data class Doc(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessId: Long = 1L,
    val docType: String = DocType.ESTIMATE,
    val docNo: String = "",
    val docDate: String = "",
    val validTill: String = "",
    val dueDate: String = "",
    val customerId: Int = 0,
    val kw: Double = 0.0,
    val ratePerKw: Double = 0.0,
    val pricingMode: String = "ROUND_TOTAL",
    val gstMode: String = "GST_5",
    val gstPercent: Double = 5.0,
    val discount: Double = 0.0,
    val taxable: Double = 0.0,
    val cgst: Double = 0.0,
    val sgst: Double = 0.0,
    val total: Double = 0.0,
    val amountWords: String = "",
    val note: String = "",
    val terms: String = "",
    val status: String = DocStatus.ESTIMATE,
    val parentEstimateId: Int? = null,
    val pdfPath: String? = null,
    val whatsappShareCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isInvoice: Boolean get() = docType == DocType.INVOICE || docType == DocType.CONST_INVOICE
    val isSolarInvoice: Boolean get() = docType == DocType.INVOICE
    val isSolarEstimate: Boolean get() = docType == DocType.ESTIMATE
    val isConstruction: Boolean get() = docType == DocType.CONST_ESTIMATE || docType == DocType.CONST_INVOICE
    val isConstructionEstimate: Boolean get() = docType == DocType.CONST_ESTIMATE
    val isConstructionInvoice: Boolean get() = docType == DocType.CONST_INVOICE

    fun computePending(totalPaid: Double): Double {
        val diff = total - totalPaid
        return BigDecimal.valueOf(diff).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    fun computeStatus(totalPaid: Double): String {
        if (status == DocStatus.CANCELLED) return DocStatus.CANCELLED
        if (!isInvoice) {
            return if (status == DocStatus.CONVERTED) DocStatus.CONVERTED else DocStatus.ESTIMATE
        }
        val pending = computePending(totalPaid)
        return when {
            pending <= 0.01 -> DocStatus.PAID
            totalPaid > 0.0 -> DocStatus.PARTIAL
            else -> DocStatus.UNPAID
        }
    }
}
