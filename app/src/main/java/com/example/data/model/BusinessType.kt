package com.example.data.model

data class BusinessTypePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val defaultUnits: List<String>,
    val defaultCategories: List<String>,
    val starterItems: List<StarterItem>
)

data class StarterItem(
    val label: String,
    val description: String,
    val category: String,
    val unit: String,
    val rate: Double,
    val purchasePrice: Double,
    val taxRate: Double,
    val hsn: String,
    val mrp: Double? = null,
    val barcode: String? = null,
    val batchNo: String? = null,
    val expiry: String? = null,
    val warranty: String? = null,
    val brand: String? = null,
    val extraAttributes: String? = null
)

object BusinessCatalogPresets {

    const val KIRANA = "KIRANA_GROCERY"
    const val MEDICAL = "MEDICAL_PHARMACY"
    const val SALON = "SALON_SPA"
    const val RETAIL = "RETAILER"
    const val DISTRIBUTOR = "DISTRIBUTOR"
    const val RESTAURANT = "HOTEL_RESTAURANT"
    const val HARDWARE = "HARDWARE_SANITARY"
    const val SWEET_SHOP = "SWEET_BAKERY"
    const val SOLAR = "SOLAR_ENERGY"
    const val CONSTRUCTION = "CONSTRUCTION"
    const val ELECTRONICS = "ELECTRONICS_MOBILE"
    const val SERVICES = "SERVICES_CONSULTING"

    val ALL_PRESETS: List<BusinessTypePreset> = listOf(
        BusinessTypePreset(
            id = KIRANA,
            title = "Kirana & Grocery Store",
            subtitle = "General store, grains, FMCG, oils, packaged goods",
            iconEmoji = "🛒",
            defaultUnits = listOf("KG", "GM", "LTR", "ML", "PKT", "BOX", "PCS", "BAG"),
            defaultCategories = listOf("GRAINS_PULSES", "OILS_GHEE", "SPICES", "PACKAGED_FOOD", "DAIRY", "HOUSEHOLD"),
            starterItems = listOf(
                StarterItem("Basmati Rice (Premium)", "Long grain aged basmati rice", "GRAINS_PULSES", "KG", 95.0, 80.0, 5.0, "10063020"),
                StarterItem("Chakki Fresh Atta", "100% whole wheat stone ground", "GRAINS_PULSES", "KG", 42.0, 36.0, 0.0, "11010000"),
                StarterItem("Refined Sunflower Oil 1L", "Fortified with Vitamin A & D", "OILS_GHEE", "LTR", 145.0, 128.0, 5.0, "15121910"),
                StarterItem("Pure Desi Ghee 1L", "Cow ghee traditional churned", "OILS_GHEE", "LTR", 620.0, 540.0, 12.0, "04059020"),
                StarterItem("Toor Dal (Unpolished)", "Premium unpolished protein dal", "GRAINS_PULSES", "KG", 160.0, 138.0, 0.0, "07136000"),
                StarterItem("Sugar / Madhur Shakkar", "Clean sulphur-free sugar crystals", "PACKAGED_FOOD", "KG", 45.0, 39.0, 5.0, "17019990"),
                StarterItem("Tata Salt 1kg", "Vacuum evaporated iodized salt", "PACKAGED_FOOD", "PKT", 28.0, 24.0, 0.0, "25010010"),
                StarterItem("Tea Powder 500g", "Kadak premium blend leaf tea", "PACKAGED_FOOD", "PKT", 230.0, 195.0, 5.0, "09024020"),
                StarterItem("Turmeric / Haldi Powder 200g", "Agmark pure ground turmeric", "SPICES", "PKT", 65.0, 52.0, 5.0, "09103020"),
                StarterItem("Washing Detergent Powder 1kg", "Active enzyme stain remover", "HOUSEHOLD", "PKT", 125.0, 102.0, 18.0, "34022010"),
                StarterItem("Good Day Butter Biscuits 100g", "Rich cashew & butter cookies", "PACKAGED_FOOD", "PKT", 30.0, 25.0, 18.0, "19053100"),
                StarterItem("Amul Butter 500g", "Pasteurized salted table butter", "DAIRY", "PKT", 275.0, 250.0, 12.0, "04051000")
            )
        ),
        BusinessTypePreset(
            id = MEDICAL,
            title = "Medical & Pharmacy",
            subtitle = "Chemist shop, tablets, syrups, first-aid, healthcare",
            iconEmoji = "💊",
            defaultUnits = listOf("STRIP", "BOTTLE", "BOX", "PCS", "TUBE", "PACK"),
            defaultCategories = listOf("MEDICINE", "HEALTHCARE", "FIRST_AID", "SYRUP", "BABY_CARE"),
            starterItems = listOf(
                StarterItem("Paracetamol 650mg Tablets", "Strip of 15 tablets - Antipyretic", "MEDICINE", "STRIP", 32.0, 24.0, 12.0, "30049060"),
                StarterItem("Amoxicillin 500mg Capsules", "Strip of 10 capsules antibiotic", "MEDICINE", "STRIP", 88.0, 68.0, 12.0, "30041010"),
                StarterItem("Cough & Cold Syrup 100ml", "Non-drowsy bronchial relief", "SYRUP", "BOTTLE", 115.0, 85.0, 12.0, "30049011"),
                StarterItem("Vitamin C 500mg Chewable", "Strip of 15 immune booster tabs", "MEDICINE", "STRIP", 45.0, 32.0, 12.0, "29362700"),
                StarterItem("Antiseptic Liquid 200ml", "Chlorhexidine disinfectant lotion", "FIRST_AID", "BOTTLE", 95.0, 75.0, 18.0, "30049099"),
                StarterItem("Sterile Gauze Bandage Roll", "10cm x 4m sterile medical cotton", "FIRST_AID", "PCS", 25.0, 18.0, 12.0, "30059010"),
                StarterItem("ORS Electrolyte Drink Sachet", "WHO-formula oral rehydration salt", "HEALTHCARE", "PKT", 22.0, 17.0, 12.0, "21069099"),
                StarterItem("Digital Thermometer", "Fast 10-sec oral/armpit reading", "HEALTHCARE", "PCS", 220.0, 160.0, 18.0, "90251910"),
                StarterItem("Pain Relief Gel / Ointment 30g", "Diclofenac fast action gel", "MEDICINE", "TUBE", 78.0, 58.0, 12.0, "30049099"),
                StarterItem("Surgical 3-Ply Face Mask (Box 50)", "BFE > 99% meltblown filter", "HEALTHCARE", "BOX", 150.0, 105.0, 12.0, "63079090")
            )
        ),
        BusinessTypePreset(
            id = SALON,
            title = "Salon, Spa & Beauty",
            subtitle = "Haircut, styling, facial, spa, makeup, beauty parlor",
            iconEmoji = "💇",
            defaultUnits = listOf("SERVICE", "SITTING", "PKG", "HOUR", "NOS"),
            defaultCategories = listOf("HAIRCUT", "TREATMENT", "FACIAL", "SPA", "BRIDAL"),
            starterItems = listOf(
                StarterItem("Men's Haircut & Styling", "Precision cut with wash & blow dry", "HAIRCUT", "SERVICE", 150.0, 20.0, 18.0, "999721"),
                StarterItem("Beard Trim & Razor Lineup", "Warm towel shaping and beard oil", "HAIRCUT", "SERVICE", 80.0, 10.0, 18.0, "999721"),
                StarterItem("De-Tan & Organic Facial", "Skin lightening fruit & herbal scrub", "FACIAL", "SERVICE", 650.0, 120.0, 18.0, "999722"),
                StarterItem("L'Oreal Hair Spa & Steam", "Deep conditioning nutritive mask", "SPA", "SERVICE", 550.0, 110.0, 18.0, "999721"),
                StarterItem("Women's Layer / Step Haircut", "Wash, style cut and blast dry", "HAIRCUT", "SERVICE", 380.0, 30.0, 18.0, "999721"),
                StarterItem("Global Hair Color / Highlights", "Ammonia-free radiant coloration", "TREATMENT", "SERVICE", 1200.0, 350.0, 18.0, "999721"),
                StarterItem("Head Massage with Ayurvedic Oil", "20 mins pressure point relaxation", "SPA", "SERVICE", 200.0, 25.0, 18.0, "999722"),
                StarterItem("Bridal Makeup Package", "HD bridal makeup with saree draping", "BRIDAL", "PKG", 6500.0, 1200.0, 18.0, "999722"),
                StarterItem("Full Body Waxing & Bleach", "Rica wax painless hair removal", "TREATMENT", "SERVICE", 850.0, 140.0, 18.0, "999722")
            )
        ),
        BusinessTypePreset(
            id = RETAIL,
            title = "Retailer & General Store",
            subtitle = "Clothing, footwear, stationery, gifts, accessories",
            iconEmoji = "🛍️",
            defaultUnits = listOf("PCS", "PAIR", "SET", "BOX", "DOZEN", "MTR"),
            defaultCategories = listOf("APPAREL", "FOOTWEAR", "STATIONERY", "BAGS", "ACCESSORIES"),
            starterItems = listOf(
                StarterItem("Men's Cotton Casual Shirt", "100% breathable pure cotton shirt", "APPAREL", "PCS", 699.0, 480.0, 5.0, "62052000"),
                StarterItem("Formal Denim Jeans Trousers", "Stretch fabric comfort fit", "APPAREL", "PCS", 999.0, 680.0, 5.0, "62034200"),
                StarterItem("Cotton Round-Neck T-Shirt", "Bio-washed combed cotton tee", "APPAREL", "PCS", 350.0, 220.0, 5.0, "61091000"),
                StarterItem("Running Shoes / Sneakers", "Cushioned sole athletic footwear", "FOOTWEAR", "PAIR", 899.0, 620.0, 12.0, "64041190"),
                StarterItem("School / Laptop Backpack 30L", "Waterproof polyester multi-pocket", "BAGS", "PCS", 650.0, 420.0, 18.0, "42021290"),
                StarterItem("Genuine Leather Wallet / Belt", "Handcrafted formal accessory", "ACCESSORIES", "PCS", 280.0, 160.0, 18.0, "42033000"),
                StarterItem("Classmate A4 Long Notebook", "200 ruled pages spiral bound", "STATIONERY", "PCS", 65.0, 45.0, 12.0, "48201090"),
                StarterItem("Stainless Steel Water Bottle 1L", "Double wall insulated thermal flask", "ACCESSORIES", "PCS", 320.0, 210.0, 18.0, "73239390")
            )
        ),
        BusinessTypePreset(
            id = DISTRIBUTOR,
            title = "Distributor & Wholesaler",
            subtitle = "FMCG trading, cartons, cases, bulk supplies, agency",
            iconEmoji = "🚚",
            defaultUnits = listOf("CASE", "CARTON", "BOX", "TIN", "BAG", "DOZEN", "PCS"),
            defaultCategories = listOf("BEVERAGES", "SNACKS", "EDIBLE_OIL", "DETERGENT", "CONFECTIONERY"),
            starterItems = listOf(
                StarterItem("Cold Drinks 250ml (Case 24)", "Assorted aerated soft drink bottles", "BEVERAGES", "CASE", 840.0, 720.0, 28.0, "22021010"),
                StarterItem("Instant Noodles (Carton of 48)", "70g packs masala noodles", "SNACKS", "CARTON", 620.0, 530.0, 18.0, "19023010"),
                StarterItem("Edible Refined Oil 15L Tin", "Soyabean / Sunflower bulk cooking oil", "EDIBLE_OIL", "TIN", 1950.0, 1820.0, 5.0, "15079010"),
                StarterItem("Packaged Namkeen Carton (60 pkts)", "Assorted bhujia & snacks 50g", "SNACKS", "CARTON", 540.0, 440.0, 12.0, "21069099"),
                StarterItem("Laundry Detergent Bar (Box 50)", "200g stain removing wash bars", "DETERGENT", "BOX", 750.0, 630.0, 18.0, "34011941"),
                StarterItem("Toothpaste 150g (Box of 24)", "Herbal anti-cavity dental cream", "CONFECTIONERY", "BOX", 1120.0, 940.0, 18.0, "33061020"),
                StarterItem("Tea Master Carton 10kg", "CTC hotel blend dust tea", "BEVERAGES", "CARTON", 3200.0, 2850.0, 5.0, "09024020"),
                StarterItem("Wafer Biscuits (Box of 36)", "Chocolate & vanilla cream wafers", "CONFECTIONERY", "BOX", 480.0, 390.0, 18.0, "19053219")
            )
        ),
        BusinessTypePreset(
            id = RESTAURANT,
            title = "Hotel, Restaurant & Cafe",
            subtitle = "Meals, thali, beverages, snacks, bakery, food court",
            iconEmoji = "🍽️",
            defaultUnits = listOf("PLATE", "CUP", "BOTTLE", "PCS", "KG", "PORTION"),
            defaultCategories = listOf("MEALS_THALI", "CURRIES", "BREADS", "BEVERAGES", "FAST_FOOD", "DESSERT"),
            starterItems = listOf(
                StarterItem("Special Veg Thali (Full Meal)", "2 Sabzi, 4 Roti, Dal, Rice, Sweet, Papad", "MEALS_THALI", "PLATE", 140.0, 60.0, 5.0, "996331"),
                StarterItem("Paneer Butter Masala", "Cottage cheese in rich butter gravy", "CURRIES", "PLATE", 180.0, 75.0, 5.0, "996331"),
                StarterItem("Butter Roti / Tandoori Naan", "Fresh hot tandoor wheat flatbread", "BREADS", "PCS", 20.0, 6.0, 5.0, "996331"),
                StarterItem("Veg Dum Biryani with Raita", "Basmati rice layered with veggies & spices", "MEALS_THALI", "PLATE", 160.0, 65.0, 5.0, "996331"),
                StarterItem("Special Masala Chai", "Ginger cardamom brewed milk tea", "BEVERAGES", "CUP", 20.0, 6.0, 5.0, "996331"),
                StarterItem("Masala Dosa with Sambhar", "Crispy fermented crepe with potato masala", "FAST_FOOD", "PLATE", 75.0, 25.0, 5.0, "996331"),
                StarterItem("Dal Tadka & Steamed Jeera Rice", "Yellow lentils tempered with ghee & cumin", "CURRIES", "PLATE", 150.0, 50.0, 5.0, "996331"),
                StarterItem("Mineral Water Bottle 1L", "Packaged purified drinking water", "BEVERAGES", "BOTTLE", 20.0, 12.0, 18.0, "22011010"),
                StarterItem("Hot Gulab Jamun (2 Pcs)", "Khoya dumplings in rose sugar syrup", "DESSERT", "PLATE", 45.0, 15.0, 5.0, "996331")
            )
        ),
        BusinessTypePreset(
            id = HARDWARE,
            title = "Hardware, Electrical & Sanitary",
            subtitle = "Pipes, paints, tools, wires, cement, bathroom fixtures",
            iconEmoji = "🔧",
            defaultUnits = listOf("PCS", "BAG", "MTR", "ROLL", "BUCKET", "BOX", "SET", "KG"),
            defaultCategories = listOf("CEMENT_BUILDING", "PAINTS", "PLUMBING", "ELECTRICAL", "TOOLS", "FASTENERS"),
            starterItems = listOf(
                StarterItem("UltraTech Cement 50kg Bag", "PPC Grade high-strength building cement", "CEMENT_BUILDING", "BAG", 380.0, 350.0, 28.0, "25232930"),
                StarterItem("Asian Paints Exterior 20L Bucket", "Weatherproof anti-fungal exterior paint", "PAINTS", "BUCKET", 3800.0, 3200.0, 18.0, "32091090"),
                StarterItem("PVC Conduit Pipe 25mm 3m", "Heavy duty fire retardant pipe", "PLUMBING", "PCS", 75.0, 58.0, 18.0, "39172310"),
                StarterItem("Copper Wire 1.5 sq mm (90m Roll)", "FR PVC insulated electrical wire", "ELECTRICAL", "ROLL", 1450.0, 1220.0, 18.0, "85444999"),
                StarterItem("Modular 6A Switch (Anchor/Havells)", "Polycarbonate smooth rocker switch", "ELECTRICAL", "PCS", 38.0, 26.0, 18.0, "85365020"),
                StarterItem("LED Bulb 9W (Warm / Cool White)", "B22 high-efficiency 900 lumens bulb", "ELECTRICAL", "PCS", 85.0, 55.0, 18.0, "85395000"),
                StarterItem("Heavy Duty Brass Bib Tap 1/2\"", "Chrome plated anti-drip water tap", "PLUMBING", "PCS", 260.0, 190.0, 18.0, "84818020"),
                StarterItem("Rotary Drill Machine 13mm 550W", "Impact hammer drill for masonry", "TOOLS", "PCS", 1650.0, 1300.0, 18.0, "84672100"),
                StarterItem("SS Screws Box 1.5\" (100 Pcs)", "Anti-rust stainless steel wood screws", "FASTENERS", "BOX", 120.0, 85.0, 18.0, "73181500")
            )
        ),
        BusinessTypePreset(
            id = SWEET_SHOP,
            title = "Sweet Shop, Bakery & Dairy",
            subtitle = "Mithai, namkeen, farsan, cakes, peda, sweets",
            iconEmoji = "🍬",
            defaultUnits = listOf("KG", "GM", "BOX", "PCS", "PKT", "PLATE"),
            defaultCategories = listOf("SWEETS_MITHAI", "NAMKEEN_FARSAN", "BAKERY", "DAIRY", "SNACKS"),
            starterItems = listOf(
                StarterItem("Special Kaju Katli", "Diamond cut pure cashew fudge", "SWEETS_MITHAI", "KG", 850.0, 620.0, 5.0, "21069099"),
                StarterItem("Pure Ghee Gulab Jamun", "Mawa dumplings steeped in saffron syrup", "SWEETS_MITHAI", "KG", 340.0, 230.0, 5.0, "21069099"),
                StarterItem("Motichoor / Besan Laddu", "Gram flour fine pearls in desi ghee", "SWEETS_MITHAI", "KG", 320.0, 220.0, 5.0, "21069099"),
                StarterItem("Fresh Bengali Rasgulla 1kg", "Spongy cottage cheese balls in syrup", "SWEETS_MITHAI", "BOX", 240.0, 160.0, 5.0, "21069099"),
                StarterItem("Hot Samosa with Mint Chutney", "Crispy spiced potato pastry snack", "SNACKS", "PCS", 18.0, 8.0, 5.0, "21069099"),
                StarterItem("Khaman Dhokla", "Soft steamed savory gram cake", "SNACKS", "KG", 160.0, 80.0, 5.0, "21069099"),
                StarterItem("Fresh Milk Peda", "Cardamom flavored sweet milk fudge", "SWEETS_MITHAI", "KG", 440.0, 310.0, 5.0, "04049000"),
                StarterItem("Black Forest Birthday Cake 500g", "Fresh cream chocolate sponge cake", "BAKERY", "PCS", 380.0, 210.0, 18.0, "19059010"),
                StarterItem("Bakery White Bread (400g)", "Soft sliced sandwich bread", "BAKERY", "PKT", 40.0, 28.0, 0.0, "19059020"),
                StarterItem("Butter Nankhatai / Cookies 400g", "Traditional cardamom baked cookies", "BAKERY", "BOX", 220.0, 140.0, 18.0, "19053100")
            )
        ),
        BusinessTypePreset(
            id = SOLAR,
            title = "Solar & Electrical Systems",
            subtitle = "Solar panels, inverters, rooftop systems, subsidies",
            iconEmoji = "☀️",
            defaultUnits = listOf("NOS", "KW", "SET", "MTR", "JOB", "WATT"),
            defaultCategories = listOf("SOLAR_PANEL", "INVERTER", "STRUCTURE", "CABLE", "BATTERY", "ACCESSORY"),
            starterItems = listOf(
                StarterItem("540W Mono PERC Solar Panel", "Half-cut bifacial high efficiency module", "SOLAR_PANEL", "NOS", 18500.0, 15500.0, 12.0, "85414300"),
                StarterItem("3.3 kW Single Phase Inverter", "On-Grid solar inverter with Wi-Fi dongle", "INVERTER", "NOS", 34000.0, 28500.0, 12.0, "85044090"),
                StarterItem("5 kW 3-Phase Solar Inverter", "Dual MPPT on-grid solar PCU", "INVERTER", "NOS", 46000.0, 39000.0, 12.0, "85044090"),
                StarterItem("Elevated HDGI Mounting Structure 3kW", "Hot dip galvanized iron structure 80 micron", "STRUCTURE", "SET", 16500.0, 13200.0, 18.0, "73089090"),
                StarterItem("Solar DC Cable 4 sq mm (Copper)", "Tinned copper UV resistant wire", "CABLE", "MTR", 45.0, 34.0, 18.0, "85444999"),
                StarterItem("AC/DC Distribution Box (SPD+MCB)", "IP65 weatherproof protection box", "ACCESSORY", "SET", 4200.0, 3200.0, 18.0, "85371000"),
                StarterItem("Chemical Earthing Kit with Compound", "Copper bonded rod 2m + 25kg compound", "ACCESSORY", "SET", 2800.0, 2100.0, 18.0, "85389000")
            )
        ),
        BusinessTypePreset(
            id = CONSTRUCTION,
            title = "Civil Construction & Contractors",
            subtitle = "Slab casting, masonry, plaster, tiling, foundation",
            iconEmoji = "🏗️",
            defaultUnits = listOf("SQFT", "BRASS", "BAG", "DAY", "TRUCK", "JOB"),
            defaultCategories = listOf("CIVIL_WORK", "LABOR", "STRUCTURE", "CEMENT_BUILDING", "MISC"),
            starterItems = listOf(
                StarterItem("RCC Slab Casting Work", "Complete shuttering, steel binding & pouring", "CIVIL_WORK", "SQFT", 260.0, 210.0, 18.0, "995411"),
                StarterItem("Red Brick Masonry Work (9\" wall)", "Sand-cement mortar joint construction", "CIVIL_WORK", "SQFT", 48.0, 36.0, 18.0, "995411"),
                StarterItem("Internal Plaster Work (12mm)", "Smooth sponge finish cement plaster", "CIVIL_WORK", "SQFT", 22.0, 15.0, 18.0, "995411"),
                StarterItem("External Double-Coat Plaster", "Weatherproof sand-faced river plaster", "CIVIL_WORK", "SQFT", 30.0, 21.0, 18.0, "995411"),
                StarterItem("Vitrified Floor Tiling Laying", "Leveling, adhesive & tile laying work", "CIVIL_WORK", "SQFT", 35.0, 24.0, 18.0, "995412"),
                StarterItem("Foundation Excavation Work", "Earthwork excavation in soil/soft rock", "CIVIL_WORK", "BRASS", 140.0, 100.0, 18.0, "995411"),
                StarterItem("Skilled Mason Labor Daily Wage", "Experienced head mason daily rate", "LABOR", "DAY", 950.0, 800.0, 18.0, "995411")
            )
        ),
        BusinessTypePreset(
            id = ELECTRONICS,
            title = "Mobile & Electronics Shop",
            subtitle = "Smartphones, chargers, earphones, accessories, repair",
            iconEmoji = "📱",
            defaultUnits = listOf("PCS", "SET", "BOX", "SERVICE", "NOS"),
            defaultCategories = listOf("SMARTPHONE", "CHARGING", "AUDIO", "ACCESSORIES", "REPAIR"),
            starterItems = listOf(
                StarterItem("Fast Type-C Charging Cable 65W", "Braided high-speed data & power wire", "CHARGING", "PCS", 250.0, 130.0, 18.0, "85444299"),
                StarterItem("Fast Mobile Charger Adapter 33W", "GaN dual port PD + QC 3.0 wall plug", "CHARGING", "PCS", 499.0, 290.0, 18.0, "85044090"),
                StarterItem("Bluetooth Wireless Earbuds (TWS)", "Noise cancelling earbuds with mic", "AUDIO", "PCS", 1299.0, 850.0, 18.0, "85183000"),
                StarterItem("Tempered Glass Screen Protector", "11D curved edge edge-to-edge guard", "ACCESSORIES", "PCS", 120.0, 35.0, 18.0, "70071900"),
                StarterItem("Shockproof Silicone Mobile Cover", "Matte finish camera bump protection", "ACCESSORIES", "PCS", 150.0, 60.0, 18.0, "39269099"),
                StarterItem("Power Bank 10000mAh Dual USB", "Lithium polymer fast charge bank", "CHARGING", "PCS", 999.0, 680.0, 18.0, "85044090"),
                StarterItem("Mobile Screen / Display Replacement", "OEM original LCD screen fitting", "REPAIR", "SERVICE", 1850.0, 1200.0, 18.0, "998713")
            )
        ),
        BusinessTypePreset(
            id = SERVICES,
            title = "Professional Services & Consulting",
            subtitle = "Consultancy, maintenance, agency, freelance, IT",
            iconEmoji = "💼",
            defaultUnits = listOf("SERVICE", "HOUR", "DAY", "MONTH", "PROJECT"),
            defaultCategories = listOf("CONSULTING", "MAINTENANCE", "IT_SERVICES", "LEGAL", "MARKETING"),
            starterItems = listOf(
                StarterItem("Business Consulting Consultation", "Strategic financial & legal advisory", "CONSULTING", "HOUR", 1500.0, 200.0, 18.0, "998311"),
                StarterItem("Annual Maintenance Contract (AMC)", "Scheduled quarterly preventive checkups", "MAINTENANCE", "MONTH", 2500.0, 800.0, 18.0, "998719"),
                StarterItem("Website & Digital Catalog Setup", "Custom e-commerce storefront development", "IT_SERVICES", "PROJECT", 12000.0, 3500.0, 18.0, "998314"),
                StarterItem("GST Filing & Return Compliance", "Monthly GSTR-1 & 3B verification", "LEGAL", "MONTH", 1200.0, 300.0, 18.0, "998231")
            )
        )
    )

    fun getPreset(id: String): BusinessTypePreset {
        return ALL_PRESETS.firstOrNull { it.id == id } ?: ALL_PRESETS.first()
    }

    fun getPresetByTemplateId(templateId: Long): BusinessTypePreset {
        val idx = (templateId.toInt() - 1).coerceIn(0, ALL_PRESETS.size - 1)
        return ALL_PRESETS[idx]
    }

    fun getPresetByBusinessType(typeKey: String?): BusinessTypePreset {
        if (typeKey.isNullOrBlank()) return ALL_PRESETS.first()
        val normalized = typeKey.uppercase().trim()
        return ALL_PRESETS.firstOrNull {
            it.id.equals(normalized, ignoreCase = true) ||
            it.title.contains(normalized, ignoreCase = true)
        } ?: when {
            normalized.contains("KIRANA") || normalized.contains("GROCERY") -> getPreset(KIRANA)
            normalized.contains("PHARM") || normalized.contains("MEDIC") -> getPreset(MEDICAL)
            normalized.contains("SALON") || normalized.contains("BEAUTY") || normalized.contains("SPA") -> getPreset(SALON)
            normalized.contains("RETAIL") || normalized.contains("CLOTH") -> getPreset(RETAIL)
            normalized.contains("DISTRIB") || normalized.contains("WHOLE") -> getPreset(DISTRIBUTOR)
            normalized.contains("REST") || normalized.contains("HOTEL") || normalized.contains("CAFE") -> getPreset(RESTAURANT)
            normalized.contains("HARDWARE") || normalized.contains("PLUMB") || normalized.contains("ELECTRICAL") -> getPreset(HARDWARE)
            normalized.contains("SWEET") || normalized.contains("BAKERY") || normalized.contains("DAIRY") -> getPreset(SWEET_SHOP)
            normalized.contains("SOLAR") -> getPreset(SOLAR)
            normalized.contains("CONST") || normalized.contains("CIVIL") -> getPreset(CONSTRUCTION)
            normalized.contains("ELEC") || normalized.contains("MOBILE") -> getPreset(ELECTRONICS)
            normalized.contains("SERV") || normalized.contains("CONSULT") -> getPreset(SERVICES)
            else -> ALL_PRESETS.first()
        }
    }
}
