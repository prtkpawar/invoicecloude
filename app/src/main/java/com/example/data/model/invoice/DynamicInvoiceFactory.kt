package com.example.data.model.invoice

import android.graphics.Paint
import com.example.data.model.DocItem

/**
 * Strategy interface defining field requirements, input dynamics, column definitions,
 * and parsing logic for various industry types.
 */
interface InvoiceFieldStrategy {
    val businessType: InvoiceBusinessType
    val isCapacityBased: Boolean get() = false
    val hasBatchAndExpiry: Boolean get() = false
    val hasDoctorAndRx: Boolean get() = false
    val hasWeightAndUnits: Boolean get() = false
    val hasMrpDiscount: Boolean get() = false
    val hasBrandAndWarranty: Boolean get() = false
    val hasSerialOrImei: Boolean get() = false
    val hasDimensionsOrArea: Boolean get() = false
    val hasTableToken: Boolean get() = false

    val partyLabel: String
    val partyHeaderTitle: String
    val itemNoun: String
    val defaultHsn: String
    val defaultTaxRate: Double
    val unitOptions: List<String>
    val quickCatalogItems: List<QuickItemPreset>

    val qtyStepSize: Double get() = 1.0

    val headerExtraFields: List<DynamicFieldDef> get() = emptyList()
    val itemExtraFields: List<DynamicFieldDef> get() = emptyList()

    fun getColumns(isInvoice: Boolean = true): List<InvoiceColumnDef>

    fun formatItemDescription(
        name: String,
        batch: String? = null,
        expiry: String? = null,
        mrp: Double? = null,
        brand: String? = null,
        warranty: String? = null,
        weightOrMeasure: String? = null
    ): String

    fun parseItemMetadata(item: DocItem): ParsedItemAttributes
}

data class QuickItemPreset(
    val label: String,
    val description: String,
    val hsn: String,
    val defaultUnit: String,
    val defaultRate: Double,
    val defaultGst: Double = 5.0,
    val defaultBatch: String? = null,
    val defaultExpiry: String? = null,
    val defaultMrp: Double? = null,
    val defaultBrand: String? = null
)

data class ParsedItemAttributes(
    val cleanName: String,
    val batchNo: String? = null,
    val expiryDate: String? = null,
    val mrp: Double? = null,
    val brand: String? = null,
    val warranty: String? = null,
    val weightOrMeasure: String? = null
)

// ============================================================================================
// 1. MEDICAL / PHARMACY STRATEGY
// ============================================================================================
class MedicalPharmacyStrategy : InvoiceFieldStrategy {
    override val businessType: InvoiceBusinessType = InvoiceBusinessType.MEDICAL
    override val hasBatchAndExpiry: Boolean = true
    override val hasDoctorAndRx: Boolean = true
    override val hasMrpDiscount: Boolean = true
    override val partyLabel: String = "Patient / Customer"
    override val partyHeaderTitle: String = "PATIENT / CUSTOMER DETAILS"
    override val itemNoun: String = "Medicine / Pharmaceutical Drug"
    override val defaultHsn: String = "30049099"
    override val defaultTaxRate: Double = 12.0
    override val unitOptions: List<String> = listOf("Strip", "Tablet", "Bottle", "Box", "Vial", "Tube", "Capsule", "Pcs", "Syrup")

    override val qtyStepSize: Double = 1.0

    override val headerExtraFields: List<DynamicFieldDef> = listOf(
        DynamicFieldDef("doctorName", "Consulting Doctor / Prescriber", "e.g. Dr. Rajesh Sharma, MBBS", DynamicFieldType.TEXT, iconEmoji = "🩺"),
        DynamicFieldDef("rxNumber", "Prescription / Rx No", "e.g. RX-2026/891", DynamicFieldType.TEXT, iconEmoji = "📝"),
        DynamicFieldDef("drugLicenseNo", "Patient / Retail D.L. No", "e.g. MH-TZ-20B-10928", DynamicFieldType.TEXT, iconEmoji = "💊")
    )

    override val itemExtraFields: List<DynamicFieldDef> = listOf(
        DynamicFieldDef("batchNo", "Batch No", "e.g. BT-9810", DynamicFieldType.TEXT, weight = 1.0f, isRequired = true),
        DynamicFieldDef("expiryDate", "Expiry (MM/YY)", "e.g. 12/28", DynamicFieldType.TEXT, weight = 1.0f, isRequired = true),
        DynamicFieldDef("mrp", "MRP (₹)", "e.g. 150", DynamicFieldType.NUMBER, weight = 0.9f)
    )

    override val quickCatalogItems: List<QuickItemPreset> = listOf(
        QuickItemPreset("Paracetamol 650mg", "Dolo / Calpol 650mg Tab (Strip of 15)", "30049099", "Strip", 34.0, 12.0, defaultBatch = "BT-8201", defaultExpiry = "12/28", defaultMrp = 38.0),
        QuickItemPreset("Amoxicillin 500mg", "Moxclav 625mg Tab (Strip of 10)", "30041010", "Strip", 145.0, 12.0, defaultBatch = "MC-4491", defaultExpiry = "08/27", defaultMrp = 168.0),
        QuickItemPreset("Pantoprazole 40mg", "Pan-40 Gastro-Resistant Tab (15s)", "30049099", "Strip", 110.0, 12.0, defaultBatch = "PN-1029", defaultExpiry = "10/27", defaultMrp = 132.0),
        QuickItemPreset("Cough Syrup 100ml", "Ascoril-D Cough Relief Syrup 100ml", "30049099", "Bottle", 85.0, 12.0, defaultBatch = "AS-9921", defaultExpiry = "05/27", defaultMrp = 98.0),
        QuickItemPreset("Digital Thermometer", "Dr. Morepen Clinical Digital Thermometer", "90251920", "Pcs", 180.0, 18.0, defaultBatch = "DM-2026", defaultExpiry = "N/A", defaultMrp = 225.0)
    )

    override fun getColumns(isInvoice: Boolean): List<InvoiceColumnDef> = listOf(
        InvoiceColumnDef("sr", "SR", 0.05f, Paint.Align.CENTER),
        InvoiceColumnDef("desc", "MEDICINE / PHARMACEUTICAL DRUG", 0.28f, Paint.Align.LEFT),
        InvoiceColumnDef("batch", "BATCH NO", 0.11f, Paint.Align.CENTER),
        InvoiceColumnDef("exp", "EXPIRY", 0.09f, Paint.Align.CENTER),
        InvoiceColumnDef("hsn", "HSN", 0.09f, Paint.Align.CENTER),
        InvoiceColumnDef("qty", "QTY", 0.07f, Paint.Align.CENTER),
        InvoiceColumnDef("mrp", "MRP (₹)", 0.09f, Paint.Align.RIGHT),
        InvoiceColumnDef("rate", "RATE (₹)", 0.09f, Paint.Align.RIGHT),
        InvoiceColumnDef("amount", "AMOUNT (₹)", 0.13f, Paint.Align.RIGHT)
    )

    override fun formatItemDescription(
        name: String,
        batch: String?,
        expiry: String?,
        mrp: Double?,
        brand: String?,
        warranty: String?,
        weightOrMeasure: String?
    ): String {
        val parts = mutableListOf<String>()
        if (!batch.isNullOrBlank()) parts.add("Batch: ${batch.trim()}")
        if (!expiry.isNullOrBlank()) parts.add("Exp: ${expiry.trim()}")
        if (mrp != null && mrp > 0) parts.add("MRP: ₹${String.format("%.2f", mrp)}")
        return if (parts.isNotEmpty()) "${name.trim()} [${parts.joinToString(" | ")}]" else name.trim()
    }

    override fun parseItemMetadata(item: DocItem): ParsedItemAttributes {
        val raw = item.description.ifBlank { item.label }
        var cleanName = raw
        var batch: String? = null
        var exp: String? = null
        var mrp: Double? = null

        val bracketIndex = raw.indexOf('[')
        if (bracketIndex != -1 && raw.endsWith(']')) {
            cleanName = raw.substring(0, bracketIndex).trim()
            val metaStr = raw.substring(bracketIndex + 1, raw.length - 1)
            val tokens = metaStr.split("|").map { it.trim() }
            for (t in tokens) {
                when {
                    t.startsWith("Batch:", ignoreCase = true) -> batch = t.substringAfter(":").trim()
                    t.startsWith("Exp:", ignoreCase = true) -> exp = t.substringAfter(":").trim()
                    t.startsWith("MRP:", ignoreCase = true) -> {
                        val num = t.substringAfter(":").replace("₹", "").trim()
                        mrp = num.toDoubleOrNull()
                    }
                }
            }
        }
        return ParsedItemAttributes(cleanName, batch, exp, mrp)
    }
}

// ============================================================================================
// 2. KIRANA / GROCERY STRATEGY
// ============================================================================================
class KiranaGroceryStrategy : InvoiceFieldStrategy {
    override val businessType: InvoiceBusinessType = InvoiceBusinessType.KIRANA
    override val hasWeightAndUnits: Boolean = true
    override val hasMrpDiscount: Boolean = true
    override val partyLabel: String = "Customer / Grahak"
    override val partyHeaderTitle: String = "CUSTOMER / BUYER DETAILS"
    override val itemNoun: String = "Grocery Commodity / FMCG Item"
    override val defaultHsn: String = "21069099"
    override val defaultTaxRate: Double = 0.0
    override val unitOptions: List<String> = listOf("kg", "g", "L", "ml", "Pcs", "Pack", "Dozen", "Bag", "Box", "Quintal")

    override val qtyStepSize: Double = 0.25

    override val headerExtraFields: List<DynamicFieldDef> = listOf(
        DynamicFieldDef("deliveryType", "Delivery / Counter Type", "Counter Cash / Home Delivery", DynamicFieldType.DROPDOWN, listOf("Counter Sale", "Home Delivery", "Wholesale Dispatch"), iconEmoji = "🛍️"),
        DynamicFieldDef("fssaiNo", "Customer FSSAI / Reg No (Optional)", "e.g. 11522999000123", DynamicFieldType.TEXT, iconEmoji = "🏷️")
    )

    override val itemExtraFields: List<DynamicFieldDef> = listOf(
        DynamicFieldDef("weightOrMeasure", "Weight / Pack Size", "e.g. 500g / 1kg / 5L", DynamicFieldType.TEXT, weight = 1.0f),
        DynamicFieldDef("mrp", "MRP (₹)", "e.g. 120", DynamicFieldType.NUMBER, weight = 0.9f)
    )

    override val quickCatalogItems: List<QuickItemPreset> = listOf(
        QuickItemPreset("Basmati Rice (Premium)", "Fortune Rozana Basmati Rice (5 kg)", "10063020", "kg", 85.0, 0.0, defaultMrp = 95.0),
        QuickItemPreset("Chana Dal (Desi)", "Unpolished Desi Chana Dal Grade-A", "07132000", "kg", 92.0, 0.0, defaultMrp = 98.0),
        QuickItemPreset("Sunflower Oil 1L", "Gemini Pure Sunflower Oil Pouch (1 L)", "15121910", "L", 128.0, 5.0, defaultMrp = 140.0),
        QuickItemPreset("Wheat Flour (Atta)", "Aashirvaad Shudh Chakki Atta (10 kg)", "11010000", "kg", 42.0, 0.0, defaultMrp = 46.0),
        QuickItemPreset("Sugar (Madhu)", "Pure Refined Sulphurless Sugar (1 kg)", "17019990", "kg", 40.0, 0.0, defaultMrp = 44.0),
        QuickItemPreset("Turmeric Powder", "Everest Pure Haldi Powder (500 g)", "09103030", "g", 115.0, 5.0, defaultMrp = 125.0)
    )

    override fun getColumns(isInvoice: Boolean): List<InvoiceColumnDef> = listOf(
        InvoiceColumnDef("sr", "SR", 0.05f, Paint.Align.CENTER),
        InvoiceColumnDef("desc", "GROCERY ITEM / COMMODITY", 0.32f, Paint.Align.LEFT),
        InvoiceColumnDef("unit_wt", "UNIT / WT", 0.12f, Paint.Align.CENTER),
        InvoiceColumnDef("qty", "QTY", 0.08f, Paint.Align.CENTER),
        InvoiceColumnDef("mrp", "MRP (₹)", 0.11f, Paint.Align.RIGHT),
        InvoiceColumnDef("rate", "RATE (₹)", 0.11f, Paint.Align.RIGHT),
        InvoiceColumnDef("disc", "DISC", 0.07f, Paint.Align.RIGHT),
        InvoiceColumnDef("amount", "AMOUNT (₹)", 0.14f, Paint.Align.RIGHT)
    )

    override fun formatItemDescription(
        name: String,
        batch: String?,
        expiry: String?,
        mrp: Double?,
        brand: String?,
        warranty: String?,
        weightOrMeasure: String?
    ): String {
        val parts = mutableListOf<String>()
        if (!weightOrMeasure.isNullOrBlank()) parts.add("Pack: ${weightOrMeasure.trim()}")
        if (mrp != null && mrp > 0) parts.add("MRP: ₹${String.format("%.2f", mrp)}")
        return if (parts.isNotEmpty()) "${name.trim()} [${parts.joinToString(" | ")}]" else name.trim()
    }

    override fun parseItemMetadata(item: DocItem): ParsedItemAttributes {
        val raw = item.description.ifBlank { item.label }
        var cleanName = raw
        var wt: String? = null
        var mrp: Double? = null

        val bracketIndex = raw.indexOf('[')
        if (bracketIndex != -1 && raw.endsWith(']')) {
            cleanName = raw.substring(0, bracketIndex).trim()
            val metaStr = raw.substring(bracketIndex + 1, raw.length - 1)
            val tokens = metaStr.split("|").map { it.trim() }
            for (t in tokens) {
                when {
                    t.startsWith("Pack:", ignoreCase = true) || t.startsWith("Wt:", ignoreCase = true) -> {
                        wt = t.substringAfter(":").trim()
                    }
                    t.startsWith("MRP:", ignoreCase = true) -> {
                        val num = t.substringAfter(":").replace("₹", "").trim()
                        mrp = num.toDoubleOrNull()
                    }
                }
            }
        }
        return ParsedItemAttributes(cleanName, weightOrMeasure = wt ?: item.unit, mrp = mrp)
    }
}

// ============================================================================================
// 3. SOLAR ENERGY & RENEWABLE EPC STRATEGY
// ============================================================================================
class SolarEPCStrategy : InvoiceFieldStrategy {
    override val businessType: InvoiceBusinessType = InvoiceBusinessType.SOLAR
    override val isCapacityBased: Boolean = true
    override val hasBrandAndWarranty: Boolean = true
    override val partyLabel: String = "Beneficiary / Client"
    override val partyHeaderTitle: String = "PROJECT BENEFICIARY / CLIENT DETAILS"
    override val itemNoun: String = "Solar PV System / Module / Inverter"
    override val defaultHsn: String = "85414300"
    override val defaultTaxRate: Double = 5.0
    override val unitOptions: List<String> = listOf("kW", "Nos", "Set", "Job", "Lot")

    override val qtyStepSize: Double = 1.0

    override val headerExtraFields: List<DynamicFieldDef> = listOf(
        DynamicFieldDef("consumerNumber", "Electricity Consumer No / CA No", "e.g. 015280918231", DynamicFieldType.TEXT, iconEmoji = "⚡"),
        DynamicFieldDef("discomName", "Discom / Utility Circle", "e.g. MSEDCL Dhule Urban", DynamicFieldType.TEXT, iconEmoji = "🏢"),
        DynamicFieldDef("sanctionLoad", "Sanctioned Load (kW)", "e.g. 5.0 kW", DynamicFieldType.NUMBER, iconEmoji = "🔌")
    )

    override val itemExtraFields: List<DynamicFieldDef> = listOf(
        DynamicFieldDef("brand", "Make / Module Brand", "e.g. Waaree / Tata DCR", DynamicFieldType.TEXT, weight = 1.0f),
        DynamicFieldDef("warranty", "Warranty Period", "e.g. 25 Years Performance", DynamicFieldType.TEXT, weight = 1.0f)
    )

    override val quickCatalogItems: List<QuickItemPreset> = listOf(
        QuickItemPreset("3 kW On-Grid Solar Plant", "3.3 kWp DCR Mono PERC Bifacial Solar Rooftop PV System", "85414300", "kW", 52000.0, 5.0, defaultBrand = "Tata / Waaree", defaultExpiry = "25 Yrs"),
        QuickItemPreset("5 kW On-Grid Solar Plant", "5.4 kWp High-Efficiency On-Grid Rooftop Power Plant", "85414300", "kW", 49000.0, 5.0, defaultBrand = "Adani / Sungrow", defaultExpiry = "25 Yrs"),
        QuickItemPreset("Solar Hybrid Inverter 5kVA", "5kVA 48V MPPT Hybrid Solar Inverter with Wi-Fi Monitoring", "85044090", "Nos", 46000.0, 12.0, defaultBrand = "Growatt / Deye", defaultExpiry = "5 Yrs"),
        QuickItemPreset("Hot Dip GI Solar Structure", "High-Wind Speed Elevated GI Structure (35mm)", "73089010", "Set", 18500.0, 18.0, defaultBrand = "Heavy GI", defaultExpiry = "10 Yrs")
    )

    override fun getColumns(isInvoice: Boolean): List<InvoiceColumnDef> = listOf(
        InvoiceColumnDef("sr", "SR", 0.05f, Paint.Align.CENTER),
        InvoiceColumnDef("desc", "SOLAR EQUIPMENT & TURNKEY WORKS", 0.35f, Paint.Align.LEFT),
        InvoiceColumnDef("hsn", "HSN/SAC", 0.10f, Paint.Align.CENTER),
        InvoiceColumnDef("qty", "CAPACITY", 0.09f, Paint.Align.CENTER),
        InvoiceColumnDef("rate", "RATE (₹)", 0.11f, Paint.Align.RIGHT),
        InvoiceColumnDef("cgst", "CGST", 0.08f, Paint.Align.RIGHT),
        InvoiceColumnDef("sgst", "SGST", 0.08f, Paint.Align.RIGHT),
        InvoiceColumnDef("amount", "AMOUNT (₹)", 0.14f, Paint.Align.RIGHT)
    )

    override fun formatItemDescription(
        name: String,
        batch: String?,
        expiry: String?,
        mrp: Double?,
        brand: String?,
        warranty: String?,
        weightOrMeasure: String?
    ): String {
        val parts = mutableListOf<String>()
        if (!brand.isNullOrBlank()) parts.add("Make: ${brand.trim()}")
        if (!warranty.isNullOrBlank()) parts.add("Warranty: ${warranty.trim()}")
        return if (parts.isNotEmpty()) "${name.trim()} [${parts.joinToString(" | ")}]" else name.trim()
    }

    override fun parseItemMetadata(item: DocItem): ParsedItemAttributes {
        val raw = item.description.ifBlank { item.label }
        var cleanName = raw
        var make: String? = null
        var war: String? = "25 Years"

        val bracketIndex = raw.indexOf('[')
        if (bracketIndex != -1 && raw.endsWith(']')) {
            cleanName = raw.substring(0, bracketIndex).trim()
            val metaStr = raw.substring(bracketIndex + 1, raw.length - 1)
            val tokens = metaStr.split("|").map { it.trim() }
            for (t in tokens) {
                when {
                    t.startsWith("Make:", ignoreCase = true) || t.startsWith("Brand:", ignoreCase = true) -> make = t.substringAfter(":").trim()
                    t.startsWith("Warranty:", ignoreCase = true) -> war = t.substringAfter(":").trim()
                }
            }
        }
        return ParsedItemAttributes(cleanName, brand = make, warranty = war)
    }
}

// ============================================================================================
// 4. CIVIL CONSTRUCTION & BOQ STRATEGY
// ============================================================================================
class CivilConstructionStrategy : InvoiceFieldStrategy {
    override val businessType: InvoiceBusinessType = InvoiceBusinessType.CONSTRUCTION
    override val hasDimensionsOrArea: Boolean = true
    override val hasWeightAndUnits: Boolean = true
    override val partyLabel: String = "Client / Site Owner"
    override val partyHeaderTitle: String = "CLIENT / CONSTRUCTION SITE DETAILS"
    override val itemNoun: String = "BOQ Work Schedule / Material"
    override val defaultHsn: String = "995411"
    override val defaultTaxRate: Double = 18.0
    override val unitOptions: List<String> = listOf("Sq.Ft", "Brass", "Cu.M", "Rmt", "Bags", "Tons", "Nos", "L.S.", "Days")

    override val qtyStepSize: Double = 0.5

    override val headerExtraFields: List<DynamicFieldDef> = listOf(
        DynamicFieldDef("siteLocation", "Construction Site / Plot Location", "e.g. Plot No 44, Devpur, Dhule", DynamicFieldType.TEXT, iconEmoji = "🏗️"),
        DynamicFieldDef("boqCode", "Work Order / RA Bill No", "e.g. WO-CIVIL-2026/04", DynamicFieldType.TEXT, iconEmoji = "📐")
    )

    override val quickCatalogItems: List<QuickItemPreset> = listOf(
        QuickItemPreset("RCC Slab Concreting M20", "Ready-Mix / Machine Concrete casting M20 with Vibrator", "995411", "Sq.Ft", 240.0, 18.0),
        QuickItemPreset("Red Brick Masonry 9-inch", "Fly-Ash / Red Clay Brickwork in cement mortar 1:6", "995411", "Brass", 18500.0, 18.0),
        QuickItemPreset("Internal Cement Plaster", "Smooth finish 12mm thick cement sand plastering", "995411", "Sq.Ft", 38.0, 18.0),
        QuickItemPreset("Vitrified Flooring 2x2", "Double-Charged Vitrified Tile Laying with adhesive", "995411", "Sq.Ft", 75.0, 18.0),
        QuickItemPreset("TMT Fe-550D Steel Rebars", "Primary Tata / Jindal TMT Steel Supply & Binding", "72142090", "Tons", 64000.0, 18.0)
    )

    override fun getColumns(isInvoice: Boolean): List<InvoiceColumnDef> = listOf(
        InvoiceColumnDef("sr", "SR", 0.05f, Paint.Align.CENTER),
        InvoiceColumnDef("desc", "WORK SPECIFICATION / BOQ SCHEDULE", 0.44f, Paint.Align.LEFT),
        InvoiceColumnDef("qty", "QTY", 0.10f, Paint.Align.CENTER),
        InvoiceColumnDef("unit", "UNIT", 0.09f, Paint.Align.CENTER),
        InvoiceColumnDef("rate", "RATE (₹)", 0.14f, Paint.Align.RIGHT),
        InvoiceColumnDef("amount", "AMOUNT (₹)", 0.18f, Paint.Align.RIGHT)
    )

    override fun formatItemDescription(
        name: String,
        batch: String?,
        expiry: String?,
        mrp: Double?,
        brand: String?,
        warranty: String?,
        weightOrMeasure: String?
    ): String = name.trim()

    override fun parseItemMetadata(item: DocItem): ParsedItemAttributes =
        ParsedItemAttributes(item.description.ifBlank { item.label })
}

// ============================================================================================
// 5. ELECTRONICS & MOBILE STRATEGY
// ============================================================================================
class ElectronicsMobileStrategy : InvoiceFieldStrategy {
    override val businessType: InvoiceBusinessType = InvoiceBusinessType.ELECTRONICS_RETAIL
    override val hasSerialOrImei: Boolean = true
    override val hasBrandAndWarranty: Boolean = true
    override val hasMrpDiscount: Boolean = true
    override val partyLabel: String = "Customer / Buyer"
    override val partyHeaderTitle: String = "CUSTOMER / BUYER DETAILS"
    override val itemNoun: String = "Electronic Gadget / Appliance"
    override val defaultHsn: String = "85044090"
    override val defaultTaxRate: Double = 18.0
    override val unitOptions: List<String> = listOf("Pcs", "Nos", "Set", "Box")

    override val qtyStepSize: Double = 1.0

    override val itemExtraFields: List<DynamicFieldDef> = listOf(
        DynamicFieldDef("brand", "Brand / Model", "e.g. Samsung Galaxy S24", DynamicFieldType.TEXT, weight = 1.0f),
        DynamicFieldDef("batch", "IMEI / Serial No", "e.g. 864902049182901", DynamicFieldType.TEXT, weight = 1.0f),
        DynamicFieldDef("warranty", "Warranty Period", "e.g. 1 Year Brand Warranty", DynamicFieldType.TEXT, weight = 0.9f),
        DynamicFieldDef("mrp", "MRP (₹)", "e.g. 21999", DynamicFieldType.NUMBER, weight = 0.8f)
    )

    override val quickCatalogItems: List<QuickItemPreset> = listOf(
        QuickItemPreset("Smartphone 5G 128GB", "5G AMOLED 8GB/128GB (IMEI: 864902049182901)", "85171300", "Pcs", 18999.0, 18.0, defaultBrand = "Samsung", defaultExpiry = "1 Year", defaultMrp = 21999.0),
        QuickItemPreset("Smart LED TV 43-inch", "4K Ultra HD Dolby Vision Smart Google TV", "85287200", "Nos", 24500.0, 18.0, defaultBrand = "Sony / LG", defaultExpiry = "2 Years", defaultMrp = 31990.0),
        QuickItemPreset("Inverter AC 1.5 Ton", "5-Star Dual Inverter Copper Split AC", "84151010", "Set", 36500.0, 28.0, defaultBrand = "Daikin", defaultExpiry = "5 Years (PCB)", defaultMrp = 44500.0)
    )

    override fun getColumns(isInvoice: Boolean): List<InvoiceColumnDef> = listOf(
        InvoiceColumnDef("sr", "SR", 0.05f, Paint.Align.CENTER),
        InvoiceColumnDef("desc", "PRODUCT / MODEL / SPECIFICATION", 0.33f, Paint.Align.LEFT),
        InvoiceColumnDef("brand", "BRAND / MODEL", 0.13f, Paint.Align.LEFT),
        InvoiceColumnDef("warranty", "WARRANTY", 0.11f, Paint.Align.CENTER),
        InvoiceColumnDef("qty", "QTY", 0.08f, Paint.Align.CENTER),
        InvoiceColumnDef("rate", "RATE (₹)", 0.14f, Paint.Align.RIGHT),
        InvoiceColumnDef("amount", "AMOUNT (₹)", 0.16f, Paint.Align.RIGHT)
    )

    override fun formatItemDescription(
        name: String,
        batch: String?,
        expiry: String?,
        mrp: Double?,
        brand: String?,
        warranty: String?,
        weightOrMeasure: String?
    ): String {
        val parts = mutableListOf<String>()
        if (!brand.isNullOrBlank()) parts.add("Brand: ${brand.trim()}")
        if (!batch.isNullOrBlank()) parts.add("IMEI/SN: ${batch.trim()}")
        if (!warranty.isNullOrBlank()) parts.add("Warranty: ${warranty.trim()}")
        if (mrp != null && mrp > 0) parts.add("MRP: ₹${String.format("%.2f", mrp)}")
        return if (parts.isNotEmpty()) "${name.trim()} [${parts.joinToString(" | ")}]" else name.trim()
    }

    override fun parseItemMetadata(item: DocItem): ParsedItemAttributes {
        val raw = item.description.ifBlank { item.label }
        var cleanName = raw
        var brand: String? = null
        var warranty: String? = "1 Year"
        var imei: String? = null
        var mrp: Double? = null

        val bracketIndex = raw.indexOf('[')
        if (bracketIndex != -1 && raw.endsWith(']')) {
            cleanName = raw.substring(0, bracketIndex).trim()
            val metaStr = raw.substring(bracketIndex + 1, raw.length - 1)
            val tokens = metaStr.split("|").map { it.trim() }
            for (t in tokens) {
                when {
                    t.startsWith("Brand:", ignoreCase = true) || t.startsWith("Make:", ignoreCase = true) -> brand = t.substringAfter(":").trim()
                    t.startsWith("IMEI/SN:", ignoreCase = true) || t.startsWith("SN:", ignoreCase = true) -> imei = t.substringAfter(":").trim()
                    t.startsWith("Warranty:", ignoreCase = true) -> warranty = t.substringAfter(":").trim()
                    t.startsWith("MRP:", ignoreCase = true) -> {
                        val num = t.substringAfter(":").replace("₹", "").trim()
                        mrp = num.toDoubleOrNull()
                    }
                }
            }
        }
        return ParsedItemAttributes(cleanName, batchNo = imei, brand = brand, warranty = warranty, mrp = mrp)
    }
}

// ============================================================================================
// 6. RESTAURANT & CAFE (F&B) STRATEGY
// ============================================================================================
class RestaurantCafeStrategy : InvoiceFieldStrategy {
    override val businessType: InvoiceBusinessType = InvoiceBusinessType.RESTAURANT
    override val hasTableToken: Boolean = true
    override val partyLabel: String = "Guest / Customer"
    override val partyHeaderTitle: String = "GUEST / TABLE DETAILS"
    override val itemNoun: String = "Food & Beverage / Menu Dish"
    override val defaultHsn: String = "996331"
    override val defaultTaxRate: Double = 5.0
    override val unitOptions: List<String> = listOf("Plate", "Portion", "Pcs", "Cup", "Glass", "Bowl", "Bottle")

    override val qtyStepSize: Double = 1.0

    override val headerExtraFields: List<DynamicFieldDef> = listOf(
        DynamicFieldDef("tableOrTokenNo", "Table / Token / KOT No", "e.g. Table T-04 / Token #28", DynamicFieldType.TEXT, isRequired = true, iconEmoji = "🍽️"),
        DynamicFieldDef("dineInTakeaway", "Order Type", "Dine-In", DynamicFieldType.DROPDOWN, listOf("Dine-In", "Takeaway / Parcel", "Online Delivery"), iconEmoji = "🛵"),
        DynamicFieldDef("stewardName", "Captain / Steward", "e.g. Rohit K.", DynamicFieldType.TEXT, iconEmoji = "👨‍🍳")
    )

    override val quickCatalogItems: List<QuickItemPreset> = listOf(
        QuickItemPreset("Paneer Butter Masala", "Cottage cheese simmered in rich butter tomato gravy", "996331", "Plate", 240.0, 5.0),
        QuickItemPreset("Butter Naan / Roti", "Crisp clay oven tandoori bread with pure butter", "996331", "Pcs", 45.0, 5.0),
        QuickItemPreset("Special Veg Biryani", "Aromatic basmati rice cooked with whole spices & raita", "996331", "Portion", 220.0, 5.0),
        QuickItemPreset("Cold Coffee with Ice Cream", "Thick blended cold coffee topped with vanilla scoop", "996331", "Glass", 120.0, 5.0)
    )

    override fun getColumns(isInvoice: Boolean): List<InvoiceColumnDef> = listOf(
        InvoiceColumnDef("sr", "SR", 0.06f, Paint.Align.CENTER),
        InvoiceColumnDef("desc", "MENU DISH / ITEM", 0.44f, Paint.Align.LEFT),
        InvoiceColumnDef("qty", "QTY", 0.10f, Paint.Align.CENTER),
        InvoiceColumnDef("rate", "RATE (₹)", 0.16f, Paint.Align.RIGHT),
        InvoiceColumnDef("amount", "AMOUNT (₹)", 0.24f, Paint.Align.RIGHT)
    )

    override fun formatItemDescription(
        name: String,
        batch: String?,
        expiry: String?,
        mrp: Double?,
        brand: String?,
        warranty: String?,
        weightOrMeasure: String?
    ): String = name.trim()

    override fun parseItemMetadata(item: DocItem): ParsedItemAttributes =
        ParsedItemAttributes(item.description.ifBlank { item.label })
}

// ============================================================================================
// 7. HARDWARE & SANITARY STRATEGY
// ============================================================================================
class HardwareSanitaryStrategy : InvoiceFieldStrategy {
    override val businessType: InvoiceBusinessType = InvoiceBusinessType.HARDWARE
    override val hasWeightAndUnits: Boolean = true
    override val hasBrandAndWarranty: Boolean = true
    override val partyLabel: String = "Contractor / Customer"
    override val partyHeaderTitle: String = "PARTY / CONTRACTOR DETAILS"
    override val itemNoun: String = "Hardware / Tools / Sanitary Item"
    override val defaultHsn: String = "25232930"
    override val defaultTaxRate: Double = 18.0
    override val unitOptions: List<String> = listOf("Kg", "Meter", "Pcs", "Box", "Bag", "Nos", "Bundle", "Ltr", "Set")

    override val qtyStepSize: Double = 1.0

    override val quickCatalogItems: List<QuickItemPreset> = listOf(
        QuickItemPreset("OPC 53 Grade Cement", "UltraTech / ACC 53 Grade Cement Bag (50 kg)", "25232930", "Bag", 370.0, 28.0, defaultBrand = "UltraTech"),
        QuickItemPreset("CPVC Pipe 1-inch (3m)", "Astral High-Pressure CPVC Plumbing Pipe", "39172310", "Pcs", 420.0, 18.0, defaultBrand = "Astral"),
        QuickItemPreset("Apex Weatherproof Emulsion", "Asian Paints Apex Exterior White (20 Ltr)", "32091000", "Ltr", 4650.0, 18.0, defaultBrand = "Asian Paints")
    )

    override fun getColumns(isInvoice: Boolean): List<InvoiceColumnDef> = listOf(
        InvoiceColumnDef("sr", "SR", 0.05f, Paint.Align.CENTER),
        InvoiceColumnDef("desc", "HARDWARE / SANITARY / PAINTS ITEM", 0.38f, Paint.Align.LEFT),
        InvoiceColumnDef("unit", "UNIT", 0.09f, Paint.Align.CENTER),
        InvoiceColumnDef("qty", "QTY", 0.08f, Paint.Align.CENTER),
        InvoiceColumnDef("rate", "RATE (₹)", 0.12f, Paint.Align.RIGHT),
        InvoiceColumnDef("cgst", "CGST", 0.07f, Paint.Align.RIGHT),
        InvoiceColumnDef("sgst", "SGST", 0.07f, Paint.Align.RIGHT),
        InvoiceColumnDef("amount", "AMOUNT (₹)", 0.14f, Paint.Align.RIGHT)
    )

    override fun formatItemDescription(
        name: String,
        batch: String?,
        expiry: String?,
        mrp: Double?,
        brand: String?,
        warranty: String?,
        weightOrMeasure: String?
    ): String {
        val parts = mutableListOf<String>()
        if (!brand.isNullOrBlank()) parts.add("Brand: ${brand.trim()}")
        return if (parts.isNotEmpty()) "${name.trim()} [${parts.joinToString(" | ")}]" else name.trim()
    }

    override fun parseItemMetadata(item: DocItem): ParsedItemAttributes =
        ParsedItemAttributes(item.description.ifBlank { item.label })
}

// ============================================================================================
// 8. SALON & WELLNESS STRATEGY
// ============================================================================================
class SalonWellnessStrategy : InvoiceFieldStrategy {
    override val businessType: InvoiceBusinessType = InvoiceBusinessType.SALON
    override val partyLabel: String = "Client / Guest"
    override val partyHeaderTitle: String = "CLIENT / APPOINTMENT DETAILS"
    override val itemNoun: String = "Salon Service / Treatment"
    override val defaultHsn: String = "999721"
    override val defaultTaxRate: Double = 18.0
    override val unitOptions: List<String> = listOf("Service", "Session", "Package", "Hour", "Pcs")

    override val headerExtraFields: List<DynamicFieldDef> = listOf(
        DynamicFieldDef("therapistName", "Stylist / Therapist Name", "e.g. Priya M.", DynamicFieldType.TEXT, iconEmoji = "💇"),
        DynamicFieldDef("appointmentSlot", "Appointment Time", "e.g. 04:30 PM", DynamicFieldType.TEXT, iconEmoji = "⏰")
    )

    override val quickCatalogItems: List<QuickItemPreset> = listOf(
        QuickItemPreset("Premium Hair Cut & Styling", "Precision cut with hair wash & blow dry styling", "999721", "Service", 450.0, 18.0),
        QuickItemPreset("Hydra Facial Glow Therapy", "Deep pore cleansing with botanical serum hydration", "999721", "Session", 1800.0, 18.0),
        QuickItemPreset("Moroccan Hair Spa", "Deep conditioning treatment with essential oils", "999721", "Service", 1200.0, 18.0)
    )

    override fun getColumns(isInvoice: Boolean): List<InvoiceColumnDef> = listOf(
        InvoiceColumnDef("sr", "SR", 0.06f, Paint.Align.CENTER),
        InvoiceColumnDef("desc", "SALON & WELLNESS SERVICE DESCRIPTION", 0.46f, Paint.Align.LEFT),
        InvoiceColumnDef("qty", "QTY", 0.10f, Paint.Align.CENTER),
        InvoiceColumnDef("rate", "RATE (₹)", 0.16f, Paint.Align.RIGHT),
        InvoiceColumnDef("amount", "AMOUNT (₹)", 0.22f, Paint.Align.RIGHT)
    )

    override fun formatItemDescription(
        name: String,
        batch: String?,
        expiry: String?,
        mrp: Double?,
        brand: String?,
        warranty: String?,
        weightOrMeasure: String?
    ): String = name.trim()

    override fun parseItemMetadata(item: DocItem): ParsedItemAttributes =
        ParsedItemAttributes(item.description.ifBlank { item.label })
}

// ============================================================================================
// 9. PROFESSIONAL SERVICES & CONSULTING STRATEGY
// ============================================================================================
class ProfessionalServicesStrategy : InvoiceFieldStrategy {
    override val businessType: InvoiceBusinessType = InvoiceBusinessType.SERVICES
    override val partyLabel: String = "Client / Organization"
    override val partyHeaderTitle: String = "CLIENT / CORPORATE DETAILS"
    override val itemNoun: String = "Professional Service / Deliverable"
    override val defaultHsn: String = "998311"
    override val defaultTaxRate: Double = 18.0
    override val unitOptions: List<String> = listOf("Job", "Hours", "Days", "Month", "Milestone", "Nos")

    override val headerExtraFields: List<DynamicFieldDef> = listOf(
        DynamicFieldDef("projectMilestone", "Project Code / Milestone", "e.g. PRJ-2026/MILESTONE-1", DynamicFieldType.TEXT, iconEmoji = "💼"),
        DynamicFieldDef("poNumber", "Client Purchase Order (PO) Ref", "e.g. PO-CORP-9921", DynamicFieldType.TEXT, iconEmoji = "📄")
    )

    override val quickCatalogItems: List<QuickItemPreset> = listOf(
        QuickItemPreset("Business Advisory Retainer", "Monthly Tax & Regulatory Compliance Advisory", "998311", "Month", 15000.0, 18.0),
        QuickItemPreset("Custom Software Development", "Sprint Phase 1 Deliverables & Architecture Setup", "998313", "Milestone", 45000.0, 18.0),
        QuickItemPreset("Annual Maintenance Contract", "Comprehensive System Support & Infrastructure AMC", "998719", "Job", 24000.0, 18.0)
    )

    override fun getColumns(isInvoice: Boolean): List<InvoiceColumnDef> = listOf(
        InvoiceColumnDef("sr", "SR", 0.05f, Paint.Align.CENTER),
        InvoiceColumnDef("desc", "SERVICE DELIVERABLE & SPECIFICATION", 0.43f, Paint.Align.LEFT),
        InvoiceColumnDef("sac", "SAC CODE", 0.12f, Paint.Align.CENTER),
        InvoiceColumnDef("qty", "UNIT", 0.08f, Paint.Align.CENTER),
        InvoiceColumnDef("rate", "RATE (₹)", 0.14f, Paint.Align.RIGHT),
        InvoiceColumnDef("amount", "AMOUNT (₹)", 0.18f, Paint.Align.RIGHT)
    )

    override fun formatItemDescription(
        name: String,
        batch: String?,
        expiry: String?,
        mrp: Double?,
        brand: String?,
        warranty: String?,
        weightOrMeasure: String?
    ): String = name.trim()

    override fun parseItemMetadata(item: DocItem): ParsedItemAttributes =
        ParsedItemAttributes(item.description.ifBlank { item.label })
}

// ============================================================================================
// 10. GENERAL / RETAIL STRATEGY (DEFAULT)
// ============================================================================================
class GeneralRetailStrategy : InvoiceFieldStrategy {
    override val businessType: InvoiceBusinessType = InvoiceBusinessType.GENERAL
    override val hasMrpDiscount: Boolean = true
    override val partyLabel: String = "Billed To / Customer"
    override val partyHeaderTitle: String = "BILLED TO / CUSTOMER DETAILS"
    override val itemNoun: String = "Product Item / Service"
    override val defaultHsn: String = "998719"
    override val defaultTaxRate: Double = 18.0
    override val unitOptions: List<String> = listOf("Nos", "Pcs", "Kg", "Hours", "Days", "Units", "Set", "Box")

    override val quickCatalogItems: List<QuickItemPreset> = listOf(
        QuickItemPreset("Standard Trading Item A", "General Consumer Goods Item", "998719", "Nos", 500.0, 18.0),
        QuickItemPreset("Commercial Service Fee", "Professional Consulting & Maintenance", "998311", "Job", 2500.0, 18.0)
    )

    override fun getColumns(isInvoice: Boolean): List<InvoiceColumnDef> = listOf(
        InvoiceColumnDef("sr", "SR", 0.05f, Paint.Align.CENTER),
        InvoiceColumnDef("desc", "ITEM DESCRIPTION / SERVICE", 0.37f, Paint.Align.LEFT),
        InvoiceColumnDef("hsn", "HSN/SAC", 0.11f, Paint.Align.CENTER),
        InvoiceColumnDef("qty", "QTY", 0.08f, Paint.Align.CENTER),
        InvoiceColumnDef("rate", "RATE (₹)", 0.11f, Paint.Align.RIGHT),
        InvoiceColumnDef("cgst", "CGST", 0.07f, Paint.Align.RIGHT),
        InvoiceColumnDef("sgst", "SGST", 0.07f, Paint.Align.RIGHT),
        InvoiceColumnDef("amount", "AMOUNT (₹)", 0.14f, Paint.Align.RIGHT)
    )

    override fun formatItemDescription(
        name: String,
        batch: String?,
        expiry: String?,
        mrp: Double?,
        brand: String?,
        warranty: String?,
        weightOrMeasure: String?
    ): String = name.trim()

    override fun parseItemMetadata(item: DocItem): ParsedItemAttributes =
        ParsedItemAttributes(item.description.ifBlank { item.label })
}

/**
 * DynamicInvoiceFactory: Factory that orchestrates strategy lookup based on
 * template IDs or industry categories.
 */
object DynamicInvoiceFactory {
    private val pharmacyStrategy = MedicalPharmacyStrategy()
    private val kiranaStrategy = KiranaGroceryStrategy()
    private val solarStrategy = SolarEPCStrategy()
    private val constructionStrategy = CivilConstructionStrategy()
    private val electronicsStrategy = ElectronicsMobileStrategy()
    private val restaurantStrategy = RestaurantCafeStrategy()
    private val hardwareStrategy = HardwareSanitaryStrategy()
    private val salonStrategy = SalonWellnessStrategy()
    private val servicesStrategy = ProfessionalServicesStrategy()
    private val generalStrategy = GeneralRetailStrategy()

    fun getStrategy(templateId: Long?, category: String? = null): InvoiceFieldStrategy {
        if (templateId != null) {
            when (templateId) {
                1L -> return solarStrategy
                2L -> return constructionStrategy
                3L, 12L -> return kiranaStrategy
                4L -> return pharmacyStrategy
                5L -> return salonStrategy
                6L, 7L -> return generalStrategy
                8L -> return restaurantStrategy
                9L -> return hardwareStrategy
                10L -> return servicesStrategy
                11L -> return electronicsStrategy
            }
        }
        val catType = InvoiceBusinessType.fromCategory(category)
        return when (catType) {
            InvoiceBusinessType.SOLAR -> solarStrategy
            InvoiceBusinessType.MEDICAL -> pharmacyStrategy
            InvoiceBusinessType.KIRANA -> kiranaStrategy
            InvoiceBusinessType.CONSTRUCTION -> constructionStrategy
            InvoiceBusinessType.ELECTRONICS_RETAIL -> electronicsStrategy
            InvoiceBusinessType.RESTAURANT -> restaurantStrategy
            InvoiceBusinessType.HARDWARE -> hardwareStrategy
            InvoiceBusinessType.SALON -> salonStrategy
            InvoiceBusinessType.SERVICES -> servicesStrategy
            else -> generalStrategy
        }
    }

    fun getStrategyForBusinessType(type: InvoiceBusinessType): InvoiceFieldStrategy {
        return when (type) {
            InvoiceBusinessType.SOLAR -> solarStrategy
            InvoiceBusinessType.MEDICAL -> pharmacyStrategy
            InvoiceBusinessType.KIRANA -> kiranaStrategy
            InvoiceBusinessType.CONSTRUCTION -> constructionStrategy
            InvoiceBusinessType.ELECTRONICS_RETAIL -> electronicsStrategy
            InvoiceBusinessType.RESTAURANT -> restaurantStrategy
            InvoiceBusinessType.HARDWARE -> hardwareStrategy
            InvoiceBusinessType.SALON -> salonStrategy
            InvoiceBusinessType.SERVICES -> servicesStrategy
            else -> generalStrategy
        }
    }
}
