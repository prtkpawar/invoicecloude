package com.example.data.model

import android.graphics.Color
import com.example.data.entity.BusinessProfile

enum class PdfTheme(
    val id: String,
    val displayName: String,
    val subtitle: String
) {
    MODERN_COLOR(
        "MODERN_COLOR",
        "Modern Vibrant (Recommended)",
        "Industry-tailored brand colors, custom/vector logo emblem & integrated QR payment box"
    ),
    CLASSIC_GST(
        "CLASSIC_GST",
        "Classic GST / CA Formal",
        "Traditional bordered accounting layout with formal uppercase header and Tally-style boxes"
    ),
    MINIMAL_CLEAN(
        "MINIMAL_CLEAN",
        "Minimalist Clean",
        "Contemporary typography, elegant high-whitespace headers, and sleek dividers"
    ),
    THERMAL_POS(
        "THERMAL_POS",
        "Compact POS Receipt",
        "Condensed slip layout optimized for retail, pharmacy, and cafe counter bills"
    );

    companion object {
        fun fromString(value: String?): PdfTheme {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.id.equals(value, ignoreCase = true) }
                ?: MODERN_COLOR
        }
    }
}

data class FirmBranding(
    val businessId: Long = 1L,
    val brandName: String = Firm.BRAND_NAME,
    val legalName: String = Firm.LEGAL_NAME,
    val tagline: String = "",
    val address: String = Firm.ADDRESS_ONE_LINE,
    val city: String = "Dhule",
    val state: String = Firm.STATE,
    val pincode: String = "424306",
    val gstin: String = Firm.GSTIN,
    val pan: String = "",
    val email: String = Firm.EMAIL,
    val phone: String = "",
    val placeOfSupply: String = Firm.PLACE_OF_SUPPLY,
    val bankName: String = Firm.BANK_NAME,
    val accountName: String = Firm.ACCOUNT_NAME,
    val accountNumber: String = Firm.ACCOUNT_NUMBER,
    val ifsc: String = Firm.IFSC,
    val upiVpa: String = "",
    val signatoryName: String = Firm.SIGNED_BY_PERSON,
    val signatoryTitle: String = "Authorized Signatory",
    val logoPath: String? = null,
    val signaturePath: String? = null,
    val businessTemplateId: Long = 1L,
    val category: String = "SOLAR",
    val theme: PdfTheme = PdfTheme.MODERN_COLOR,
    val primaryColor: Int = Color.parseColor("#0F172A"),
    val accentColor: Int = Color.parseColor("#EA580C"),
    val bgTint: Int = Color.parseColor("#F8FAFC"),
    val isCapacityBased: Boolean = true
) {
    companion object {
        fun from(
            business: BusinessProfile?,
            company: Company? = null,
            themeOverride: String? = null,
            bankNameOverride: String? = null,
            signatoryTitleOverride: String? = null,
            emailOverride: String? = null,
            phoneOverride: String? = null,
            taglineOverride: String? = null
        ): FirmBranding {
            val comp = company ?: Company()
            val bId = business?.id ?: 1L
            val bTempId = business?.businessTemplateId ?: 1L
            val terminology = BusinessTerminologyRegistry.getForTemplateId(bTempId)

            val rawBrand = business?.brandName?.trim()?.ifBlank { null }
            val rawLegal = business?.legalName?.trim()?.ifBlank { null }

            val effectiveBrand = when {
                rawBrand != null -> rawBrand
                rawLegal != null -> rawLegal
                company != null && company.name.isNotBlank() -> company.name
                else -> terminology.title
            }

            val effectiveLegal = when {
                rawLegal != null -> rawLegal
                rawBrand != null -> rawBrand
                company != null && company.name.isNotBlank() -> company.name
                else -> effectiveBrand
            }

            val effectiveAddress = business?.address?.trim()?.ifBlank { null }
                ?: company?.address?.trim()?.ifBlank { null }
                ?: ""
            val effectiveCity = business?.city?.trim()?.ifBlank { null }
                ?: ""
            val effectiveState = business?.state?.trim()?.ifBlank { null }
                ?: "Maharashtra"
            val effectivePin = business?.pincode?.trim()?.ifBlank { null }
                ?: ""
            val effectiveGstin = business?.gstin?.trim()?.ifBlank { null }
                ?: company?.gstin?.trim()?.ifBlank { null }
                ?: ""
            val effectivePan = business?.pan?.trim()?.ifBlank { null } ?: ""
            val effectiveEmail = emailOverride?.trim()?.ifBlank { null }
                ?: company?.email?.trim()?.ifBlank { null }
                ?: ""
            val effectivePhone = phoneOverride?.trim()?.ifBlank { null }
                ?: business?.mobile?.trim()?.ifBlank { null }
                ?: ""
            val effectivePos = company?.placeOfSupply?.trim()?.ifBlank { null }
                ?: "$effectiveState (Code: 27)"

            val effectiveAccNo = business?.bankAccountNo?.trim()?.ifBlank { null }
                ?: company?.accountNumber?.trim()?.ifBlank { null }
                ?: ""
            val effectiveIfsc = business?.ifsc?.trim()?.ifBlank { null }
                ?: company?.ifsc?.trim()?.ifBlank { null }
                ?: ""
            val effectiveBankName = bankNameOverride?.trim()?.ifBlank { null }
                ?: company?.bankName?.trim()?.ifBlank { null }
                ?: (if (effectiveAccNo.isNotBlank()) "Primary Bank" else "")
            val effectiveAccName = business?.legalName?.trim()?.ifBlank { null }
                ?: company?.accountName?.trim()?.ifBlank { null }
                ?: effectiveLegal
            val effectiveUpi = business?.upiVpa?.trim()?.ifBlank { null }
                ?: ""

            val effectiveSignatory = business?.signatoryName?.trim()?.ifBlank { null }
                ?: company?.signatoryName?.trim()?.ifBlank { null }
                ?: "Authorized Signatory"
            val effectiveSigTitle = signatoryTitleOverride?.trim()?.ifBlank { null } ?: "Proprietor / Authorized"

            val category = terminology.industryKey
            val theme = PdfTheme.fromString(themeOverride)

            // Category color palettes
            val (primary, accent, tint) = when (bTempId) {
                1L -> Triple(Color.parseColor("#0F172A"), Color.parseColor("#EA580C"), Color.parseColor("#FFF7ED")) // Solar
                2L -> Triple(Color.parseColor("#1E293B"), Color.parseColor("#2563EB"), Color.parseColor("#EFF6FF")) // Construction
                3L, 6L -> Triple(Color.parseColor("#0F766E"), Color.parseColor("#16A34A"), Color.parseColor("#F0FDF4")) // Kirana / Retail
                4L -> Triple(Color.parseColor("#0369A1"), Color.parseColor("#0D9488"), Color.parseColor("#F0FDFA")) // Pharmacy
                5L -> Triple(Color.parseColor("#831843"), Color.parseColor("#DB2777"), Color.parseColor("#FDF2F8")) // Salon
                8L -> Triple(Color.parseColor("#78350F"), Color.parseColor("#D97706"), Color.parseColor("#FEF3C7")) // Restaurant
                9L -> Triple(Color.parseColor("#334155"), Color.parseColor("#EA580C"), Color.parseColor("#FFFBEB")) // Hardware
                10L -> Triple(Color.parseColor("#312E81"), Color.parseColor("#4F46E5"), Color.parseColor("#EEF2FF")) // Services
                11L -> Triple(Color.parseColor("#0F172A"), Color.parseColor("#0284C7"), Color.parseColor("#F0F9FF")) // Electronics
                12L -> Triple(Color.parseColor("#713F12"), Color.parseColor("#EAB308"), Color.parseColor("#FEFCE8")) // Sweets
                else -> Triple(Color.parseColor("#0F172A"), Color.parseColor("#EA580C"), Color.parseColor("#FFF7ED"))
            }

            val defaultTagline = when (bTempId) {
                1L -> "SOLAR ENERGY SYSTEMS, ROOFTOP PV & BACKUP POWER"
                2L -> "CIVIL ENGINEERS, BUILDERS & CONTRACTORS"
                3L -> "SUPERMARKET, PROVISIONS & DAILY GROCERIES"
                4L -> "CHEMISTS, DRUGGISTS & HEALTHCARE ESSENTIALS"
                5L -> "HAIR, BEAUTY, SPA & WELLNESS LOUNGE"
                6L -> "GENERAL RETAIL & CONSUMER GOODS"
                7L -> "WHOLESALE TRADING & FMCG DISTRIBUTION"
                8L -> "CAFE, RESTAURANT & CATERING SERVICES"
                9L -> "HARDWARE, PAINTS, TOOLS & SANITARYWARE"
                10L -> "PROFESSIONAL IT, CONSULTING & TECHNICAL SERVICES"
                11L -> "HOME APPLIANCES, MOBILE & ELECTRONICS HUB"
                12L -> "AUTHENTIC SWEETS, NAMKEEN & BAKERY DELIGHTS"
                else -> "QUALITY PRODUCTS & COMMITTED SERVICES"
            }

            return FirmBranding(
                businessId = bId,
                brandName = effectiveBrand,
                legalName = effectiveLegal,
                tagline = taglineOverride?.ifBlank { null } ?: defaultTagline,
                address = effectiveAddress,
                city = effectiveCity,
                state = effectiveState,
                pincode = effectivePin,
                gstin = effectiveGstin,
                pan = effectivePan,
                email = effectiveEmail,
                phone = effectivePhone,
                placeOfSupply = effectivePos,
                bankName = effectiveBankName,
                accountName = effectiveAccName,
                accountNumber = effectiveAccNo,
                ifsc = effectiveIfsc,
                upiVpa = effectiveUpi,
                signatoryName = effectiveSignatory,
                signatoryTitle = effectiveSigTitle,
                logoPath = business?.logoPath,
                signaturePath = business?.signaturePath,
                businessTemplateId = bTempId,
                category = category,
                theme = theme,
                primaryColor = primary,
                accentColor = accent,
                bgTint = tint,
                isCapacityBased = terminology.isCapacityBased
            )
        }
    }
}
