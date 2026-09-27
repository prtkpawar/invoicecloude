package com.example.data.model

/**
 * Predefined kit that auto-populates equipment/items when selected.
 * 
 * Example: "3 kW Solar Kit" → adds 7 equipment items with quantities
 * computed from the kit's capacity.
 *
 * @param name Display name ("3 kW Solar Kit")
 * @param description Short subtitle ("Ideal for 2-3 BHK homes")
 * @param iconEmoji Visual identifier
 * @param capacityKw For capacity-based firms (Solar), the kW value. 0 for others.
 * @param defaultRatePerUnit Default rate (e.g., ₹50,000 per kW for Solar)
 * @param items Template items that get resolved with dynamic values
 */
data class PredefinedKit(
    val name: String,
    val description: String,
    val iconEmoji: String = "",
    val capacityKw: Double = 0.0,
    val defaultRatePerUnit: Double = 0.0,
    val items: List<KitItem> = emptyList()
)

/**
 * A template item within a kit.
 *
 * Description and qty support template variables:
 * - `{KW}` → capacity in kW (e.g., "3")
 * - `{KW_FMT}` → formatted kW (e.g., "3" or "3.5")
 * - `{PANEL_WATT}` → panel wattage (e.g., "540")
 * - `{PANEL_COUNT}` → number of panels (e.g., "6")
 * - `{INVERTER_KW}` → inverter capacity (e.g., "3.3")
 *
 * These are resolved at runtime by [resolveTemplates].
 */
data class KitItem(
    val description: String,
    val hsn: String = "",
    val unit: String = "NOS",
    val qtyFormula: String = "1",
    val sortOrder: Int = 0
)

/**
 * GST preset chip for quick selection.
 *
 * @param label Display text ("5% GST")
 * @param detail Breakdown text ("CGST 2.5% + SGST 2.5%")
 * @param rate The total GST rate (e.g., 5.0)
 * @param isDefault True if this should be pre-selected for new documents
 */
data class GstPreset(
    val label: String,
    val detail: String = "",
    val rate: Double,
    val isDefault: Boolean = false
)

/**
 * A single editable term/condition with index tracking.
 */
data class TermItem(
    val index: Int,
    val text: String,
    val isEditable: Boolean = true
)

/**
 * Category group for organizing predefined catalog items.
 * Used in construction (Earthwork, RCC, Masonry) and retail (Grains, Dairy, etc.)
 */
data class CatalogCategory(
    val id: String,
    val label: String,
    val iconEmoji: String = "",
    val items: List<CatalogItem> = emptyList()
)

/**
 * A predefined catalog item within a category.
 */
data class CatalogItem(
    val label: String,
    val description: String = "",
    val unit: String = "NOS",
    val defaultRate: Double = 0.0,
    val hsn: String = "",
    val taxRate: Double = 0.0
)

/**
 * Complete form configuration for a specific business/firm type.
 *
 * This drives the entire form UI dynamically:
 * - Which sections to show (capacity? kit selector? catalog?)
 * - What defaults to pre-populate
 * - Which GST presets to offer
 * - What terms & conditions to start with
 *
 * Usage:
 * ```kotlin
 * val config = FirmFormConfigRegistry.getConfig("SOLAR")
 * // Form reads config.kits to show kit selector
 * // Form reads config.gstPresets for GST chips
 * // Form reads config.defaultTerms for T&C list
 * ```
 */
data class FirmFormConfig(
    val firmType: String,

    // ── Capacity Section (Solar-specific) ──
    val capacityBased: Boolean = false,
    val capacityLabel: String = "Capacity",
    val capacityUnit: String = "kW",
    val rateLabel: String = "Rate",
    val rateUnit: String = "per kW",

    // ── Kit Selection ──
    val kits: List<PredefinedKit> = emptyList(),
    val showKitSelector: Boolean = false,

    // ── Catalog Categories (for non-kit item selection) ──
    val catalogCategories: List<CatalogCategory> = emptyList(),
    val showCatalogPicker: Boolean = false,

    // ── GST Presets ──
    val gstPresets: List<GstPreset> = listOf(
        GstPreset("No GST", "Tax exempt", 0.0),
        GstPreset("5%", "CGST 2.5% + SGST 2.5%", 5.0),
        GstPreset("12%", "CGST 6% + SGST 6%", 12.0),
        GstPreset("18%", "CGST 9% + SGST 9%", 18.0),
        GstPreset("Custom", "Enter rate", -1.0)
    ),

    // ── Default Terms & Conditions ──
    val defaultTerms: List<String> = emptyList(),
    val termsEditable: Boolean = true,
    val termsResettable: Boolean = true,

    // ── Default Note ──
    val defaultEstimateNote: String = "",
    val defaultInvoiceNote: String = "",

    // ── Item Units ──
    val unitOptions: List<String> = listOf("NOS", "SET", "PCS", "KG", "LTR", "LOT"),

    // ── Pricing Modes ──
    val pricingModes: List<String> = listOf("FORWARD"),

    // ── Section Visibility ──
    val showCapacitySection: Boolean = false,
    val showEquipmentBoq: Boolean = false,
    val showQuickChips: Boolean = false,
    val showRatePerUnit: Boolean = false
)

/**
 * Resolves template variables in a [KitItem] description and qty formula.
 *
 * @param kw System capacity in kW
 * @param panelWatt Panel wattage (default 540W)
 * @return Pair of (resolved description, resolved qty as Double)
 */
fun KitItem.resolveTemplates(kw: Double, panelWatt: Int = 540): Pair<String, Double> {
    val panelCount = ((kw * 1000) / panelWatt).toInt().coerceAtLeast(1)
    val kwFmt = if (kw % 1.0 == 0.0) kw.toInt().toString()
                else String.format(java.util.Locale.US, "%.1f", kw)
    val inverterKw = String.format(java.util.Locale.US, "%.1f", kw * 1.1)

    val vars = mapOf(
        "{KW}" to kwFmt,
        "{KW_FMT}" to kwFmt,
        "{PANEL_WATT}" to panelWatt.toString(),
        "{PANEL_COUNT}" to panelCount.toString(),
        "{INVERTER_KW}" to inverterKw
    )

    var desc = description
    var qtyStr = qtyFormula
    for ((key, value) in vars) {
        desc = desc.replace(key, value)
        qtyStr = qtyStr.replace(key, value)
    }

    val qty = qtyStr.toDoubleOrNull() ?: 1.0
    return Pair(desc, qty)
}
