package com.example.data.model

/**
 * Central registry for firm-specific form configurations.
 *
 * Each firm type (Solar, Construction, Kirana, etc.) has a tailored config
 * that drives the form UI: which sections appear, what items are predefined,
 * which GST rates are offered, and what default terms to show.
 *
 * Usage:
 * ```kotlin
 * val config = FirmFormConfigRegistry.getConfig(businessProfile.category)
 * ```
 */
object FirmFormConfigRegistry {

    fun getConfig(category: String): FirmFormConfig = when (category.uppercase().trim()) {
        "SOLAR" -> solarConfig()
        "CONSTRUCTION" -> constructionConfig()
        "KIRANA" -> kiranaConfig()
        "PHARMACY" -> pharmacyConfig()
        "SALON" -> salonConfig()
        "RESTAURANT" -> restaurantConfig()
        "HARDWARE" -> hardwareConfig()
        "ELECTRONICS" -> electronicsConfig()
        "RETAIL" -> retailConfig()
        "DISTRIBUTOR" -> distributorConfig()
        "SWEET_SHOP" -> sweetShopConfig()
        "SERVICES" -> servicesConfig()
        else -> genericConfig()
    }

    // ═══════════════════════════════════════════════════
    //  ☀️  SOLAR EPC
    // ═══════════════════════════════════════════════════
    private fun solarConfig() = FirmFormConfig(
        firmType = "SOLAR",
        capacityBased = true,
        capacityLabel = "System Capacity",
        capacityUnit = "kW",
        rateLabel = "Rate",
        rateUnit = "per kW",
        showCapacitySection = true,
        showEquipmentBoq = true,
        showKitSelector = true,
        showRatePerUnit = true,
        pricingModes = listOf("FORWARD", "FIXED_TOTAL"),
        unitOptions = listOf("NOS", "SET", "LOT", "MTR", "JOB", "KW"),

        kits = listOf(
            PredefinedKit(
                name = "3 kW Kit",
                description = "Ideal for 2-3 BHK homes • Single Phase",
                iconEmoji = "🏠",
                capacityKw = 3.0,
                defaultRatePerUnit = 50000.0,
                items = solarBoqTemplate()
            ),
            PredefinedKit(
                name = "5 kW Kit",
                description = "Medium homes & shops • Single/Three Phase",
                iconEmoji = "🏢",
                capacityKw = 5.0,
                defaultRatePerUnit = 48000.0,
                items = solarBoqTemplate()
            ),
            PredefinedKit(
                name = "10 kW Kit",
                description = "Commercial & large rooftops • Three Phase",
                iconEmoji = "🏭",
                capacityKw = 10.0,
                defaultRatePerUnit = 45000.0,
                items = solarBoqTemplate()
            )
        ),

        gstPresets = listOf(
            GstPreset("No GST", "Tax exempt", 0.0),
            GstPreset("5%", "CGST 2.5% + SGST 2.5%", 5.0, isDefault = true),
            GstPreset("Custom", "Enter custom rate", -1.0)
        ),

        defaultTerms = solarDefaultTerms(),

        defaultEstimateNote = "Rooftop on-grid solar photovoltaic power plant with PM Surya Ghar subsidy compliance",
        defaultInvoiceNote = "Supply, installation & commissioning of grid-connected solar power plant"
    )

    /**
     * Standard 7-item Solar BoQ template.
     * Template variables are resolved at runtime by [KitItem.resolveTemplates].
     */
    private fun solarBoqTemplate() = listOf(
        KitItem(
            description = "Solar PV Modules ({PANEL_WATT}W Mono PERC Half-Cut DCR, ALMM Approved)",
            hsn = "85414011",
            unit = "NOS",
            qtyFormula = "{PANEL_COUNT}",
            sortOrder = 0
        ),
        KitItem(
            description = "Solar Grid-Tie Inverter ({KW_FMT} kW On-Grid with WiFi Monitoring)",
            hsn = "85044090",
            unit = "SET",
            qtyFormula = "1",
            sortOrder = 1
        ),
        KitItem(
            description = "Module Mounting Structure (Hot Dip Galvanized Iron - High Wind Tolerant)",
            hsn = "73089090",
            unit = "SET",
            qtyFormula = "1",
            sortOrder = 2
        ),
        KitItem(
            description = "ACDB & DCDB Distribution Protection Enclosures with Type II SPD & MCBs",
            hsn = "85371000",
            unit = "SET",
            qtyFormula = "1",
            sortOrder = 3
        ),
        KitItem(
            description = "Chemical Earthing Electrodes (3 Sets: AC, DC, LA) & Lightning Arrester",
            hsn = "85359090",
            unit = "SET",
            qtyFormula = "3",
            sortOrder = 4
        ),
        KitItem(
            description = "Solar DC UV-Resistant Copper Cables (4/6 sq.mm) & AC Armoured Cables",
            hsn = "85444999",
            unit = "LOT",
            qtyFormula = "1",
            sortOrder = 5
        ),
        KitItem(
            description = "Discom Net-Metering Liaisoning, Inspection, Testing & Grid Commissioning",
            hsn = "998719",
            unit = "JOB",
            qtyFormula = "1",
            sortOrder = 6
        )
    )

    /**
     * 9 standard Solar terms & conditions.
     * User can edit/remove/reset to these defaults.
     */
    private fun solarDefaultTerms() = listOf(
        "All equipment supplied will be new, branded, and carry manufacturer warranty.",
        "Solar PV Modules: 25-year linear performance warranty. Inverter: 5-year standard warranty.",
        "Installation will be completed within 15-20 working days from the date of advance payment.",
        "Payment Terms: 50% advance with order, 40% on material delivery, 10% on commissioning.",
        "Net-metering application, DISCOM inspection, and grid synchronization are included.",
        "Civil/structural modifications to the roof (if required) are not included in this scope.",
        "Subsidy under PM Surya Ghar Muft Bijli Yojana will be facilitated; approval is subject to DISCOM/MNRE.",
        "This quotation is valid for 30 days from the date of issue.",
        "Any additional cabling beyond 30 meters or conduit work will be charged extra at actuals."
    )

    // ═══════════════════════════════════════════════════
    //  🏗️  CONSTRUCTION
    // ═══════════════════════════════════════════════════
    private fun constructionConfig() = FirmFormConfig(
        firmType = "CONSTRUCTION",
        showCatalogPicker = true,
        showEquipmentBoq = true,
        pricingModes = listOf("FORWARD", "FIXED_TOTAL"),
        unitOptions = listOf("Sq.Ft", "Cu.Ft", "Rft", "Brass", "Nos", "Bag", "MT", "LOT", "LS"),

        catalogCategories = listOf(
            CatalogCategory("EARTHWORK", "Earthwork", "⛏️", listOf(
                CatalogItem("Soil Excavation", "Manual excavation in all types of soil", "Cu.Ft", 12.0, "99543", 18.0),
                CatalogItem("Hard Rock Cutting", "Machine cutting in hard rock", "Cu.Ft", 55.0, "99543", 18.0),
                CatalogItem("Earth Backfilling", "Backfilling with excavated earth", "Cu.Ft", 6.0, "99543", 18.0),
                CatalogItem("Anti-Termite Treatment", "Chemical treatment for termite protection", "Sq.Ft", 8.0, "99543", 18.0)
            )),
            CatalogCategory("RCC", "RCC Work", "🏗️", listOf(
                CatalogItem("PCC (1:3:6)", "Plain cement concrete foundation", "Cu.Ft", 45.0, "99543", 18.0),
                CatalogItem("RCC (M20 Grade)", "Reinforced cement concrete M20", "Cu.Ft", 75.0, "99543", 18.0),
                CatalogItem("RCC (M25 Grade)", "Reinforced cement concrete M25", "Cu.Ft", 85.0, "99543", 18.0),
                CatalogItem("Steel Reinforcement", "TMT Fe-500D grade bars", "KG", 72.0, "72142000", 18.0)
            )),
            CatalogCategory("MASONRY", "Masonry", "🧱", listOf(
                CatalogItem("Brick Masonry (9 inch)", "First class brick work in CM 1:6", "Cu.Ft", 65.0, "99543", 18.0),
                CatalogItem("Brick Masonry (4.5 inch)", "Half brick partition wall in CM 1:4", "Sq.Ft", 55.0, "99543", 18.0),
                CatalogItem("AAC Block Masonry", "Autoclaved aerated concrete blocks", "Sq.Ft", 70.0, "99543", 18.0)
            )),
            CatalogCategory("PLASTER", "Plaster & Finish", "🖌️", listOf(
                CatalogItem("Internal Plaster (12mm)", "Cement plaster 1:4 single coat", "Sq.Ft", 22.0, "99543", 18.0),
                CatalogItem("External Plaster (20mm)", "Cement plaster 1:4 double coat", "Sq.Ft", 32.0, "99543", 18.0),
                CatalogItem("POP Punning", "Plaster of Paris finish on walls", "Sq.Ft", 18.0, "99543", 18.0)
            )),
            CatalogCategory("FLOORING", "Flooring", "🏠", listOf(
                CatalogItem("Vitrified Tiles (2x2)", "Standard vitrified floor tiles with fixing", "Sq.Ft", 85.0, "99543", 18.0),
                CatalogItem("Granite Flooring", "Polished granite with grouting", "Sq.Ft", 145.0, "99543", 18.0),
                CatalogItem("Kota Stone Flooring", "Natural Kota stone with polishing", "Sq.Ft", 65.0, "99543", 18.0)
            )),
            CatalogCategory("FABRICATION", "Fabrication & Steel", "⚙️", listOf(
                CatalogItem("MS Railing", "Mild steel railing with primer paint", "Rft", 350.0, "73089090", 18.0),
                CatalogItem("MS Gate (Standard)", "Fabricated MS main gate", "Sq.Ft", 280.0, "73089090", 18.0),
                CatalogItem("SS Railing", "Stainless steel 304 grade railing", "Rft", 850.0, "73089090", 18.0)
            ))
        ),

        gstPresets = listOf(
            GstPreset("No GST", "Unregistered works", 0.0),
            GstPreset("12%", "CGST 6% + SGST 6% (Works contract)", 12.0, isDefault = true),
            GstPreset("18%", "CGST 9% + SGST 9% (Pure service)", 18.0),
            GstPreset("Custom", "Enter custom rate", -1.0)
        ),

        defaultTerms = listOf(
            "This quotation is valid for 30 days from the date of issue.",
            "Payment Terms: 30% advance, 60% in progress milestones, 10% on completion.",
            "All materials will be ISI/BIS marked and of approved quality.",
            "Work completion timeline: As mutually agreed, subject to weather and site conditions.",
            "Any additional work not included in this BOQ will be charged at actuals with prior approval.",
            "Rates are inclusive of labour and material unless specified otherwise.",
            "Scaffolding, water, and electricity at site to be provided by the client.",
            "All dimensions are approximate and may vary ±5% based on site conditions.",
            "Warranty: 1 year on workmanship defects from the date of handover."
        ),

        defaultEstimateNote = "Detailed construction cost estimate with BOQ",
        defaultInvoiceNote = "Tax invoice for civil construction work"
    )

    // ═══════════════════════════════════════════════════
    //  🛒  KIRANA & GROCERY
    // ═══════════════════════════════════════════════════
    private fun kiranaConfig() = FirmFormConfig(
        firmType = "KIRANA",
        showQuickChips = true,
        showCatalogPicker = true,
        unitOptions = listOf("KG", "GM", "LTR", "ML", "PKT", "BOX", "PCS", "BAG"),

        catalogCategories = listOf(
            CatalogCategory("GRAINS", "Grains & Pulses", "🌾", listOf(
                CatalogItem("Basmati Rice", "Aged premium long grain basmati", "KG", 95.0, "10063020", 5.0),
                CatalogItem("Chakki Atta", "Whole wheat stone ground flour", "KG", 42.0, "11010000", 0.0),
                CatalogItem("Toor Dal", "Unpolished protein rich", "KG", 160.0, "07136000", 0.0),
                CatalogItem("Moong Dal", "Split washed green gram", "KG", 140.0, "07132900", 0.0)
            )),
            CatalogCategory("OILS", "Oils & Ghee", "🫒", listOf(
                CatalogItem("Sunflower Oil 1L", "Fortified with Vitamin A & D", "LTR", 145.0, "15121910", 5.0),
                CatalogItem("Mustard Oil 1L", "Kachi Ghani cold pressed", "LTR", 190.0, "15141100", 5.0),
                CatalogItem("Pure Cow Ghee 1L", "Traditional bilona churned", "LTR", 620.0, "04059020", 12.0)
            )),
            CatalogCategory("SPICES", "Spices & Masala", "🌶️", listOf(
                CatalogItem("Turmeric Powder 200g", "Agmark pure ground turmeric", "PKT", 65.0, "09103020", 5.0),
                CatalogItem("Red Chilli Powder 200g", "Kashmiri Mirchi powder", "PKT", 85.0, "09042190", 5.0),
                CatalogItem("Garam Masala 100g", "Blended whole spice mix", "PKT", 75.0, "09109100", 5.0)
            )),
            CatalogCategory("PACKAGED", "Packaged & FMCG", "📦", listOf(
                CatalogItem("Sugar 1kg", "Sulphur-free clean crystals", "KG", 45.0, "17019990", 5.0),
                CatalogItem("Tata Salt 1kg", "Vacuum evaporated iodized salt", "PKT", 28.0, "25010010", 0.0),
                CatalogItem("Tea Powder 500g", "Kadak premium blend leaf tea", "PKT", 230.0, "09024020", 5.0)
            ))
        ),

        gstPresets = listOf(
            GstPreset("No GST", "Unpackaged essential goods", 0.0),
            GstPreset("5%", "CGST 2.5% + SGST 2.5%", 5.0, isDefault = true),
            GstPreset("12%", "CGST 6% + SGST 6%", 12.0),
            GstPreset("18%", "CGST 9% + SGST 9%", 18.0),
            GstPreset("Custom", "Enter custom rate", -1.0)
        ),

        defaultTerms = listOf(
            "Goods sold in good condition.",
            "Exchange accepted within 3 days with bill for unopened packaged goods.",
            "Prices subject to market commodity rate fluctuations.",
            "Thank you for your patronage!"
        ),

        defaultEstimateNote = "Monthly grocery estimate / Parcha list",
        defaultInvoiceNote = "Counter cash sale / Retail grocery delivery"
    )

    // ═══════════════════════════════════════════════════
    //  💊  PHARMACY
    // ═══════════════════════════════════════════════════
    private fun pharmacyConfig() = FirmFormConfig(
        firmType = "PHARMACY",
        showQuickChips = true,
        showCatalogPicker = true,
        unitOptions = listOf("STRIP", "BOTTLE", "BOX", "PCS", "TUBE", "PACK"),

        catalogCategories = listOf(
            CatalogCategory("MEDICINE", "Medicines", "💊", listOf(
                CatalogItem("Paracetamol 650mg", "Strip of 15 tablets - Antipyretic", "STRIP", 32.0, "30049060", 12.0),
                CatalogItem("Amoxicillin 500mg", "Strip of 10 capsules antibiotic", "STRIP", 88.0, "30041010", 12.0),
                CatalogItem("Vitamin C 500mg", "Strip of 15 immune booster tabs", "STRIP", 45.0, "29362700", 12.0)
            )),
            CatalogCategory("HEALTHCARE", "Healthcare", "🩺", listOf(
                CatalogItem("Cough & Cold Syrup 100ml", "Non-drowsy bronchial relief", "BOTTLE", 115.0, "30049011", 12.0),
                CatalogItem("ORS Sachets (Pack of 10)", "Electrolyte rehydration salts", "BOX", 35.0, "30049090", 5.0),
                CatalogItem("Digital Thermometer", "Instant read medical thermometer", "PCS", 180.0, "90251990", 12.0)
            ))
        ),

        gstPresets = listOf(
            GstPreset("No GST", "Essential medicines", 0.0),
            GstPreset("5%", "CGST 2.5% + SGST 2.5%", 5.0),
            GstPreset("12%", "CGST 6% + SGST 6%", 12.0, isDefault = true),
            GstPreset("Custom", "Enter custom rate", -1.0)
        ),

        defaultTerms = listOf(
            "Medicines dispensed as per prescription.",
            "Returns accepted only for sealed packs within 7 days with bill.",
            "Schedule H / H1 drugs require valid prescription.",
            "Check expiry date before use."
        ),

        defaultEstimateNote = "Prescription quote for patient reference",
        defaultInvoiceNote = "Pharmacy retail sale"
    )

    // ═══════════════════════════════════════════════════
    //  💇  SALON & SPA
    // ═══════════════════════════════════════════════════
    private fun salonConfig() = FirmFormConfig(
        firmType = "SALON",
        showQuickChips = true,
        unitOptions = listOf("SERVICE", "SESSION", "PCS", "ML", "PACK"),

        catalogCategories = listOf(
            CatalogCategory("HAIRCUT", "Hair Services", "✂️", listOf(
                CatalogItem("Men's Haircut", "Standard precision haircut", "SERVICE", 200.0, "998971", 18.0),
                CatalogItem("Women's Haircut", "Styling haircut with blow dry", "SERVICE", 500.0, "998971", 18.0),
                CatalogItem("Hair Colour (Global)", "Full head global colouring", "SERVICE", 1500.0, "998971", 18.0)
            )),
            CatalogCategory("SKIN", "Skin & Facial", "🧴", listOf(
                CatalogItem("Basic Facial", "Deep cleansing facial treatment", "SERVICE", 600.0, "998971", 18.0),
                CatalogItem("De-Tan Pack", "Skin brightening de-tan treatment", "SERVICE", 400.0, "998971", 18.0),
                CatalogItem("Threading (Full Face)", "Precise eyebrow & face threading", "SERVICE", 100.0, "998971", 18.0)
            ))
        ),

        gstPresets = listOf(
            GstPreset("No GST", "Below threshold", 0.0),
            GstPreset("18%", "CGST 9% + SGST 9%", 18.0, isDefault = true),
            GstPreset("Custom", "Enter custom rate", -1.0)
        ),

        defaultTerms = listOf(
            "Patch test recommended for chemical treatments.",
            "Cancellation charges apply for no-shows after 30 minutes.",
            "Product recommendations are for home care guidance only."
        ),

        defaultEstimateNote = "Service estimate for salon / spa treatments",
        defaultInvoiceNote = "Salon & beauty service bill"
    )

    // ═══════════════════════════════════════════════════
    //  🍽️  RESTAURANT
    // ═══════════════════════════════════════════════════
    private fun restaurantConfig() = FirmFormConfig(
        firmType = "RESTAURANT",
        showQuickChips = true,
        unitOptions = listOf("PLATE", "HALF", "FULL", "PCS", "GLASS", "LTR"),

        catalogCategories = listOf(
            CatalogCategory("VEG", "Veg Main Course", "🥗", listOf(
                CatalogItem("Paneer Butter Masala", "Rich tomato gravy with paneer", "PLATE", 220.0, "996331", 5.0),
                CatalogItem("Dal Makhani", "Slow cooked black lentils", "PLATE", 180.0, "996331", 5.0),
                CatalogItem("Jeera Rice", "Cumin tempered basmati rice", "PLATE", 120.0, "996331", 5.0)
            )),
            CatalogCategory("NONVEG", "Non-Veg", "🍗", listOf(
                CatalogItem("Chicken Biryani", "Dum cooked Hyderabadi biryani", "PLATE", 280.0, "996331", 5.0),
                CatalogItem("Butter Chicken", "Tandoori chicken in makhani", "PLATE", 320.0, "996331", 5.0),
                CatalogItem("Mutton Rogan Josh", "Kashmiri style lamb curry", "PLATE", 380.0, "996331", 5.0)
            )),
            CatalogCategory("BREAD", "Breads & Roti", "🫓", listOf(
                CatalogItem("Butter Naan", "Tandoor baked with butter", "PCS", 50.0, "996331", 5.0),
                CatalogItem("Garlic Naan", "Tandoor baked with garlic", "PCS", 60.0, "996331", 5.0),
                CatalogItem("Tandoori Roti", "Whole wheat tandoor roti", "PCS", 30.0, "996331", 5.0)
            ))
        ),

        gstPresets = listOf(
            GstPreset("No GST", "Below threshold", 0.0),
            GstPreset("5%", "CGST 2.5% + SGST 2.5%", 5.0, isDefault = true),
            GstPreset("18%", "CGST 9% + SGST 9% (AC restaurant)", 18.0),
            GstPreset("Custom", "Enter custom rate", -1.0)
        ),

        defaultTerms = listOf(
            "Food prepared fresh to order.",
            "Packed food is non-returnable and non-refundable.",
            "Prices inclusive of all taxes unless mentioned otherwise."
        ),

        defaultEstimateNote = "Catering / party order estimate",
        defaultInvoiceNote = "Restaurant dine-in / takeaway bill"
    )

    // ═══════════════════════════════════════════════════
    //  🔧  HARDWARE & SANITARY
    // ═══════════════════════════════════════════════════
    private fun hardwareConfig() = FirmFormConfig(
        firmType = "HARDWARE",
        showQuickChips = true,
        showCatalogPicker = true,
        unitOptions = listOf("PCS", "KG", "BAG", "BOX", "SET", "FT", "MTR", "BUNDLE"),

        gstPresets = listOf(
            GstPreset("No GST", "Unregistered", 0.0),
            GstPreset("12%", "CGST 6% + SGST 6%", 12.0),
            GstPreset("18%", "CGST 9% + SGST 9%", 18.0, isDefault = true),
            GstPreset("28%", "CGST 14% + SGST 14%", 28.0),
            GstPreset("Custom", "Enter custom rate", -1.0)
        ),

        defaultTerms = listOf(
            "Material as per standard specifications.",
            "Delivery charges extra for orders below ₹5,000.",
            "Returns accepted for defective items within 7 days with bill.",
            "Rates subject to change without prior notice."
        ),

        defaultEstimateNote = "Hardware & building material quotation",
        defaultInvoiceNote = "Hardware supply tax invoice"
    )

    // ═══════════════════════════════════════════════════
    //  📱  ELECTRONICS & MOBILE
    // ═══════════════════════════════════════════════════
    private fun electronicsConfig() = FirmFormConfig(
        firmType = "ELECTRONICS",
        showQuickChips = true,
        unitOptions = listOf("PCS", "SET", "UNIT", "NOS", "BOX"),

        gstPresets = listOf(
            GstPreset("12%", "CGST 6% + SGST 6%", 12.0),
            GstPreset("18%", "CGST 9% + SGST 9%", 18.0, isDefault = true),
            GstPreset("28%", "CGST 14% + SGST 14% (Luxury)", 28.0),
            GstPreset("Custom", "Enter custom rate", -1.0)
        ),

        defaultTerms = listOf(
            "Warranty as per manufacturer policy. Keep warranty card safe.",
            "No returns on opened software/accessories.",
            "Service & repair charges are separate from warranty.",
            "Screen damage is not covered under standard warranty."
        ),

        defaultEstimateNote = "Electronics / mobile quotation",
        defaultInvoiceNote = "Electronics retail bill"
    )

    // ═══════════════════════════════════════════════════
    //  👔  RETAIL & FASHION
    // ═══════════════════════════════════════════════════
    private fun retailConfig() = FirmFormConfig(
        firmType = "RETAIL",
        showQuickChips = true,
        unitOptions = listOf("PCS", "SET", "PAIR", "MTR", "DOZ", "BOX"),

        gstPresets = listOf(
            GstPreset("5%", "CGST 2.5% + SGST 2.5%", 5.0, isDefault = true),
            GstPreset("12%", "CGST 6% + SGST 6%", 12.0),
            GstPreset("18%", "CGST 9% + SGST 9%", 18.0),
            GstPreset("Custom", "Enter custom rate", -1.0)
        ),

        defaultTerms = listOf(
            "Exchange within 7 days with original bill and tags.",
            "No refunds — exchange or store credit only.",
            "Altered garments cannot be returned."
        ),

        defaultEstimateNote = "Retail quotation / Price list",
        defaultInvoiceNote = "Retail sales bill"
    )

    // ═══════════════════════════════════════════════════
    //  📦  DISTRIBUTOR / WHOLESALE
    // ═══════════════════════════════════════════════════
    private fun distributorConfig() = FirmFormConfig(
        firmType = "DISTRIBUTOR",
        showCatalogPicker = true,
        unitOptions = listOf("BOX", "CTN", "BAG", "KG", "LTR", "PCS", "DOZ", "CASE"),

        gstPresets = listOf(
            GstPreset("5%", "CGST 2.5% + SGST 2.5%", 5.0),
            GstPreset("12%", "CGST 6% + SGST 6%", 12.0, isDefault = true),
            GstPreset("18%", "CGST 9% + SGST 9%", 18.0),
            GstPreset("28%", "CGST 14% + SGST 14%", 28.0),
            GstPreset("Custom", "Enter custom rate", -1.0)
        ),

        defaultTerms = listOf(
            "Minimum order quantity applies as per rate list.",
            "Delivery within 3-5 working days from order confirmation.",
            "Payment: 100% advance or credit as per agreed terms.",
            "Damaged goods must be reported within 24 hours of receipt with photos."
        ),

        defaultEstimateNote = "Wholesale / bulk order quotation",
        defaultInvoiceNote = "Distributor supply tax invoice"
    )

    // ═══════════════════════════════════════════════════
    //  🍰  SWEET SHOP & BAKERY
    // ═══════════════════════════════════════════════════
    private fun sweetShopConfig() = FirmFormConfig(
        firmType = "SWEET_SHOP",
        showQuickChips = true,
        unitOptions = listOf("KG", "GM", "PCS", "BOX", "PKT", "PLATE"),

        gstPresets = listOf(
            GstPreset("No GST", "Fresh sweets (unbranded)", 0.0),
            GstPreset("5%", "CGST 2.5% + SGST 2.5%", 5.0, isDefault = true),
            GstPreset("12%", "CGST 6% + SGST 6% (Branded)", 12.0),
            GstPreset("Custom", "Enter custom rate", -1.0)
        ),

        defaultTerms = listOf(
            "Sweets are freshly prepared daily.",
            "Advance booking required for bulk / party orders (48 hours).",
            "Products to be consumed within mentioned shelf life."
        ),

        defaultEstimateNote = "Sweet box / party order estimate",
        defaultInvoiceNote = "Sweet shop / bakery bill"
    )

    // ═══════════════════════════════════════════════════
    //  🔧  SERVICES & CONSULTING
    // ═══════════════════════════════════════════════════
    private fun servicesConfig() = FirmFormConfig(
        firmType = "SERVICES",
        showQuickChips = false,
        unitOptions = listOf("HRS", "DAY", "JOB", "MONTH", "PROJECT", "PCS"),

        gstPresets = listOf(
            GstPreset("No GST", "Below threshold", 0.0),
            GstPreset("18%", "CGST 9% + SGST 9%", 18.0, isDefault = true),
            GstPreset("Custom", "Enter custom rate", -1.0)
        ),

        defaultTerms = listOf(
            "Services to be rendered as per agreed scope of work.",
            "Payment: 50% advance, 50% on completion.",
            "Additional work beyond scope will be charged separately.",
            "Project timeline as mutually discussed."
        ),

        defaultEstimateNote = "Service / consulting quotation",
        defaultInvoiceNote = "Professional service tax invoice"
    )

    // ═══════════════════════════════════════════════════
    //  📋  GENERIC (Fallback)
    // ═══════════════════════════════════════════════════
    private fun genericConfig() = FirmFormConfig(
        firmType = "GENERIC",
        showQuickChips = false,
        unitOptions = listOf("NOS", "PCS", "SET", "KG", "LTR", "BOX", "LOT"),

        gstPresets = listOf(
            GstPreset("No GST", "Tax exempt", 0.0),
            GstPreset("5%", "CGST 2.5% + SGST 2.5%", 5.0),
            GstPreset("12%", "CGST 6% + SGST 6%", 12.0),
            GstPreset("18%", "CGST 9% + SGST 9%", 18.0, isDefault = true),
            GstPreset("28%", "CGST 14% + SGST 14%", 28.0),
            GstPreset("Custom", "Enter custom rate", -1.0)
        ),

        defaultTerms = listOf(
            "Prices valid for 15 days from the date of quotation.",
            "Payment terms as mutually agreed.",
            "Goods once sold will not be taken back."
        ),

        defaultEstimateNote = "",
        defaultInvoiceNote = ""
    )
}
