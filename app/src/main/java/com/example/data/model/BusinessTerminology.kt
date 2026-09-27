package com.example.data.model

/**
 * Defines industry-specific terminology, document labels, and invoicing behavior
 * across the application.
 */
data class BusinessTerminology(
    val industryKey: String,
    val templateId: Long,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val partyLabel: String,             // e.g. "Patient", "Customer", "Client", "Guest"
    val partyPluralLabel: String,       // e.g. "Patients", "Customers", "Clients", "Guests"
    val inventoryLabel: String,         // e.g. "Medicines & Stock", "Grocery Stock", "Menu Items", "Materials & Stock"
    val estimateDocLabel: String,       // e.g. "Prescription Quote", "Estimate / Parcha", "KOT Estimate", "Quotation / BOQ"
    val invoiceDocLabel: String,        // e.g. "Rx Tax Bill", "Tax Invoice / Cash Memo", "Dine-in / Takeaway Bill"
    val lineItemTerm: String,           // e.g. "Medicine", "Grocery Item", "Dish / Drink", "Work Item / BOQ"
    val referenceNumberTerm: String,    // e.g. "Doctor / Rx No", "Table / Token No", "Site Location / RA Bill", "PO / Ref"
    val defaultTaxRate: Double,
    val isCapacityBased: Boolean = false, // True ONLY for Solar EPC by default
    val quickStapleItems: List<QuickItemChip> = emptyList(),
    val defaultInvoiceNote: String = "",
    val defaultEstimateNote: String = "",
    val defaultInvoiceTerms: String = "",
    val defaultEstimateTerms: String = ""
)

data class QuickItemChip(
    val label: String,
    val description: String = "",
    val unit: String,
    val defaultRate: Double,
    val taxRate: Double = 0.0,
    val hsn: String = "",
    val batchNo: String? = null,
    val expiry: String? = null,
    val warranty: String? = null,
    val brand: String? = null,
    val extraAttributes: String? = null
)

object BusinessTerminologyRegistry {

    val KIRANA = BusinessTerminology(
        industryKey = "KIRANA_GROCERY",
        templateId = 3L,
        title = "Kirana & Grocery Store",
        subtitle = "General store, packaged goods, grains, oils & FMCG",
        iconEmoji = "🛒",
        partyLabel = "Customer",
        partyPluralLabel = "Customers",
        inventoryLabel = "Grocery Stock",
        estimateDocLabel = "Price Estimate / Parcha",
        invoiceDocLabel = "Tax Invoice / Cash Memo",
        lineItemTerm = "Grocery Item",
        referenceNumberTerm = "Counter / Bill Token",
        defaultTaxRate = 5.0,
        isCapacityBased = false,
        quickStapleItems = listOf(
            QuickItemChip("Basmati Rice (5kg)", "Aged premium long grain", "BAG", 480.0, 5.0, "10063020"),
            QuickItemChip("Chakki Fresh Atta (10kg)", "100% whole wheat stone ground", "BAG", 390.0, 0.0, "11010000"),
            QuickItemChip("Refined Sunflower Oil (1L)", "Fortified with Vit A & D", "LTR", 145.0, 5.0, "15121910"),
            QuickItemChip("Pure Cow Ghee (1L)", "Traditional bilona churned", "LTR", 620.0, 12.0, "04059020"),
            QuickItemChip("Toor Dal (1kg)", "Unpolished protein rich", "KG", 160.0, 0.0, "07136000"),
            QuickItemChip("Madhur Clean Sugar (1kg)", "Sulphur-free clean crystals", "KG", 46.0, 5.0, "17019990")
        ),
        defaultInvoiceNote = "Counter cash sale / Retail grocery delivery",
        defaultEstimateNote = "Monthly grocery estimate / Parcha list",
        defaultInvoiceTerms = "1. Goods sold in good condition.\n2. Exchange accepted within 3 days with bill for unopened packaged goods.\n3. Thank you for your patronage!",
        defaultEstimateTerms = "1. Prices subject to market commodity rate fluctuations.\n2. Delivery free on orders above ₹1,000 within 3km."
    )

    val PHARMACY = BusinessTerminology(
        industryKey = "MEDICAL_PHARMACY",
        templateId = 4L,
        title = "Pharmacy & Healthcare",
        subtitle = "Chemist, prescription drugs, surgicals & wellness",
        iconEmoji = "💊",
        partyLabel = "Patient",
        partyPluralLabel = "Patients",
        inventoryLabel = "Medicines & Drugs",
        estimateDocLabel = "Prescription Quote / Estimate",
        invoiceDocLabel = "Pharmacy Tax Invoice (Rx Bill)",
        lineItemTerm = "Medicine / Formulation",
        referenceNumberTerm = "Doctor / Rx Number",
        defaultTaxRate = 12.0,
        isCapacityBased = false,
        quickStapleItems = listOf(
            QuickItemChip("Paracetamol 650mg", "Dolo 650 (Antipyretic / Pain)", "STRIP", 32.0, 12.0, "30049099", batchNo = "BCH-2401", expiry = "11/26", brand = "Micro Labs"),
            QuickItemChip("Azithromycin 500mg", "Azee 500 (Antibiotic Strip of 5)", "STRIP", 120.0, 12.0, "30049099", batchNo = "AZ-892", expiry = "09/26", brand = "Cipla"),
            QuickItemChip("Pantoprazole DSR", "Pan-D (Antacid / Gas Strip of 10)", "STRIP", 145.0, 12.0, "30049099", batchNo = "PND-402", expiry = "01/27", brand = "Alkem"),
            QuickItemChip("Cetirizine 10mg", "Cetzine (Anti-allergic Strip of 10)", "STRIP", 22.0, 12.0, "30049099", batchNo = "CTZ-119", expiry = "06/27", brand = "Dr. Reddy"),
            QuickItemChip("Cough Syrup 100ml", "Ascoril-D Cough Relief Bottle", "BOTTLE", 128.0, 12.0, "30049099", batchNo = "ASC-551", expiry = "12/26", brand = "Glenmark"),
            QuickItemChip("ORS Electrolyte Pack", "WHO formulation energy powder", "PKT", 22.0, 12.0, "30049099", batchNo = "ORS-332", expiry = "08/26", brand = "Cipla")
        ),
        defaultInvoiceNote = "Prescription medicines dispensed as per Registered Medical Practitioner advice",
        defaultEstimateNote = "Monthly chronic medicine estimate / Treatment estimate",
        defaultInvoiceTerms = "1. Schedule H/X drugs dispensed strictly against valid doctor prescription.\n2. Refrigerated medicines (e.g., Insulin, Vaccines) cannot be returned.\n3. Verify batch number and expiry before accepting delivery.\n4. Drug License & GST compliant invoice.",
        defaultEstimateTerms = "1. Estimate valid for 15 days subject to stock availability.\n2. Home delivery available for chronic patient orders."
    )

    val RESTAURANT = BusinessTerminology(
        industryKey = "HOTEL_RESTAURANT",
        templateId = 8L,
        title = "Cafe, Restaurant & Hotel",
        subtitle = "Dine-in, takeaway, cloud kitchen, food & beverages",
        iconEmoji = "🍽️",
        partyLabel = "Guest",
        partyPluralLabel = "Guests / Tables",
        inventoryLabel = "Menu & Dishes",
        estimateDocLabel = "Table Order / KOT Estimate",
        invoiceDocLabel = "Dining / Takeaway Bill",
        lineItemTerm = "Dish / Beverage",
        referenceNumberTerm = "Table / Token No.",
        defaultTaxRate = 5.0,
        isCapacityBased = false,
        quickStapleItems = listOf(
            QuickItemChip("Masala Chai (Special)", "Cardamom & ginger brew", "CUP", 30.0, 5.0, "21069099", extraAttributes = "🟢 Veg | Regular"),
            QuickItemChip("Cold Coffee with Ice Cream", "Rich creamy shake", "GLASS", 120.0, 5.0, "21069099", extraAttributes = "🟢 Veg | 300ml"),
            QuickItemChip("Paneer Butter Masala", "Cottage cheese rich makhani gravy", "PORTION", 240.0, 5.0, "21069099", extraAttributes = "🟢 Veg | Full"),
            QuickItemChip("Veg Dum Biryani", "Served with mirchi ka salan & raita", "PORTION", 220.0, 5.0, "21069099", extraAttributes = "🟢 Veg | Handi"),
            QuickItemChip("Butter Naan Basket", "Clay oven baked with amul butter", "PCS", 45.0, 5.0, "19059090", extraAttributes = "🟢 Veg | 1 Pc"),
            QuickItemChip("Chicken Tikka Masala", "Charcoal roasted chicken in spicy gravy", "PORTION", 290.0, 5.0, "21069099", extraAttributes = "🔴 Non-Veg | Full")
        ),
        defaultInvoiceNote = "Thank you for dining with us! Hope you enjoyed the meal.",
        defaultEstimateNote = "Banquet / Party catering estimate",
        defaultInvoiceTerms = "1. GST 5% charged as per Restaurant composite scheme (without ITC).\n2. Service charge is voluntary and discretionary.\n3. Please check bill before payment.",
        defaultEstimateTerms = "1. Catering / Banquet booking confirmed against 50% advance deposit.\n2. Final headcount to be confirmed 24 hours prior to event."
    )

    val HARDWARE = BusinessTerminology(
        industryKey = "HARDWARE_SANITARY",
        templateId = 9L,
        title = "Hardware, Paints & Electricals",
        subtitle = "Pipes, sanitaryware, paints, tools, electrical fittings",
        iconEmoji = "🔧",
        partyLabel = "Customer / Contractor",
        partyPluralLabel = "Customers & Contractors",
        inventoryLabel = "Hardware & Material Stock",
        estimateDocLabel = "Quotation / Estimate",
        invoiceDocLabel = "Material Tax Invoice / Challan",
        lineItemTerm = "Material / Hardware Item",
        referenceNumberTerm = "Challan / Gate Pass No.",
        defaultTaxRate = 18.0,
        isCapacityBased = false,
        quickStapleItems = listOf(
            QuickItemChip("PVC Pipe 1\" (6m)", "Astral / Supreme Class 3 Heavy Duty", "PCS", 380.0, 18.0, "39172310", brand = "Astral"),
            QuickItemChip("Asian Paints Royale 20L", "Luxury interior emulsion white", "BUCKET", 6850.0, 18.0, "32091000", brand = "Asian Paints"),
            QuickItemChip("Ultratech Cement (50kg)", "PPC 53 grade building cement", "BAG", 390.0, 28.0, "25232930", brand = "Ultratech"),
            QuickItemChip("Modular Switch 10A", "Anchor Roma 1-way switch", "PCS", 38.0, 18.0, "85365020", brand = "Anchor"),
            QuickItemChip("Brass Ball Valve 1/2\"", "High pressure leak-proof valve", "PCS", 195.0, 18.0, "84818030", brand = "Zoloto"),
            QuickItemChip("SS Screws 2\" (Box)", "Stainless steel 304 screws 100 pcs", "BOX", 140.0, 18.0, "73181500")
        ),
        defaultInvoiceNote = "Building materials and hardware delivered for construction/repair site",
        defaultEstimateNote = "Material supply quotation for project site",
        defaultInvoiceTerms = "1. Goods inspected and taken at buyer's risk.\n2. Cut pipes, mixed tint paints, or damaged hardware cannot be returned.\n3. Manufacturer warranty applicable on electrical fittings.",
        defaultEstimateTerms = "1. Material rates valid for 7 days due to raw material and steel fluctuations.\n2. Transport and unloading charges extra at actuals."
    )

    val CONSTRUCTION = BusinessTerminology(
        industryKey = "CONSTRUCTION",
        templateId = 2L,
        title = "Construction & Civil Contractor",
        subtitle = "Slab casting, masonry, turnkey building & renovations",
        iconEmoji = "🏗️",
        partyLabel = "Client / Owner",
        partyPluralLabel = "Clients & Site Owners",
        inventoryLabel = "Work Scope & BOQ Items",
        estimateDocLabel = "Work Quotation / BOQ Estimate",
        invoiceDocLabel = "Contractor Running Account (RA) Bill",
        lineItemTerm = "Work Item / Scope",
        referenceNumberTerm = "Site Location / RA Bill No.",
        defaultTaxRate = 18.0,
        isCapacityBased = false,
        quickStapleItems = listOf(
            QuickItemChip("RCC Slab Casting M20/M25", "Ready concrete with shuttering, steel binding & vibrator", "SQFT", 185.0, 18.0, "995411"),
            QuickItemChip("Brick Masonry (Red Clay)", "9\" thick external wall with cement mortar 1:6", "SQFT", 75.0, 18.0, "995411"),
            QuickItemChip("Internal Wall Plastering", "12mm smooth cement plaster with sponge finish", "SQFT", 32.0, 18.0, "995411"),
            QuickItemChip("Tile Flooring (Vitrified)", "2x2 double charged tiles with adhesive & grouting", "SQFT", 65.0, 18.0, "995411"),
            QuickItemChip("Earthwork Excavation", "Foundation trench excavation in hard murrum/rock", "BRASS", 1400.0, 18.0, "995411"),
            QuickItemChip("Waterproofing Treatment", "Chemical coating with 5-year anti-leak guarantee", "SQFT", 45.0, 18.0, "995411")
        ),
        defaultInvoiceNote = "Running Account (RA) Bill for site work executed as per structural layout",
        defaultEstimateNote = "Bill of Quantities (BOQ) cost estimate for proposed civil work",
        defaultInvoiceTerms = "1. Payment to be released within 7 days of RA bill certification.\n2. Extra items beyond approved BOQ billed on joint site measurement.\n3. Retention 5% deductible until final handover.",
        defaultEstimateTerms = "1. Quotation based on architectural drawings provided by client.\n2. Water and three-phase electricity at site in client's scope.\n3. Validity: strictly 15 days."
    )

    val SALON = BusinessTerminology(
        industryKey = "SALON_SPA",
        templateId = 5L,
        title = "Salon, Spa & Beauty Care",
        subtitle = "Hair styling, grooming, facial treatments, spa therapies",
        iconEmoji = "💇",
        partyLabel = "Client",
        partyPluralLabel = "Clients",
        inventoryLabel = "Services & Treatments",
        estimateDocLabel = "Service Package Quotation",
        invoiceDocLabel = "Salon & Spa Service Bill",
        lineItemTerm = "Service / Package",
        referenceNumberTerm = "Stylist / Appointment No.",
        defaultTaxRate = 18.0,
        isCapacityBased = false,
        quickStapleItems = listOf(
            QuickItemChip("Haircut & Beard Sculpting", "Style consultation, hair wash, styling & beard trim", "SERVICE", 450.0, 18.0, "999721", extraAttributes = "Men | 45 min"),
            QuickItemChip("Hydra Glow Facial Treatment", "Deep cleansing, blackhead extraction & vitamin mask", "SERVICE", 1600.0, 18.0, "999721", extraAttributes = "Unisex | 60 min"),
            QuickItemChip("L'Oreal Keratin Hair Spa", "Deep conditioning treatment with ozone steam", "SERVICE", 950.0, 18.0, "999721", extraAttributes = "Unisex | 50 min"),
            QuickItemChip("Bridal / Pre-Groom Package", "Complete glow ritual, waxing, threading & styling", "PACKAGE", 5500.0, 18.0, "999721", extraAttributes = "Bridal | 3 hours"),
            QuickItemChip("Organic Pedicure & Manicure", "Herbal scrub, nail shaping & massage", "SERVICE", 750.0, 18.0, "999721", extraAttributes = "Unisex | 45 min")
        ),
        defaultInvoiceNote = "Thank you for visiting! We hope you loved your treatment & styling.",
        defaultEstimateNote = "Bridal / Event grooming package proposal",
        defaultInvoiceTerms = "1. Services rendered once confirmed are non-refundable.\n2. Pre-booked packages valid for 90 days from date of issue.\n3. Inform stylist in advance about allergies or sensitive skin conditions.",
        defaultEstimateTerms = "1. Package bookings confirmed upon 30% advance deposit.\n2. Rescheduling requires 24 hours prior intimation."
    )

    val SERVICES = BusinessTerminology(
        industryKey = "SERVICES_CONSULTING",
        templateId = 10L,
        title = "Services & Professional Consulting",
        subtitle = "IT solutions, legal, accounting, design & advisory",
        iconEmoji = "💼",
        partyLabel = "Client",
        partyPluralLabel = "Clients & Accounts",
        inventoryLabel = "Services & Retainers",
        estimateDocLabel = "Proposal & Fee Quote",
        invoiceDocLabel = "Professional Tax Invoice",
        lineItemTerm = "Deliverable / Scope",
        referenceNumberTerm = "PO / Engagement Code",
        defaultTaxRate = 18.0,
        isCapacityBased = false,
        quickStapleItems = listOf(
            QuickItemChip("Monthly Retainer Fee", "Ongoing advisory, maintenance & support services", "MONTH", 25000.0, 18.0, "998311"),
            QuickItemChip("Software / Web Development Milestone", "Phase 1: Architecture, UI design & core backend API", "MILESTONE", 45000.0, 18.0, "998314"),
            QuickItemChip("GST & Income Tax Filing", "Quarterly reconciliation, 3B/GSTR-1 filing & audit check", "QUARTER", 7500.0, 18.0, "998231"),
            QuickItemChip("Digital Marketing & Ads Campaign", "Meta & Google Ads setup, creative design & optimization", "CAMPAIGN", 18000.0, 18.0, "998361"),
            QuickItemChip("Hourly Technical Consultation", "Senior specialist architecture review & guidance", "HOURS", 2500.0, 18.0, "998313")
        ),
        defaultInvoiceNote = "Professional consulting services rendered in accordance with service agreement",
        defaultEstimateNote = "Project scope & professional fee proposal",
        defaultInvoiceTerms = "1. Payment due within 15 days of invoice date.\n2. Remit payment via NEFT/RTGS to bank coordinates above.\n3. Intellectual property transfers upon complete fee settlement.",
        defaultEstimateTerms = "1. Proposal valid for 30 calendar days.\n2. Project kick-off scheduled post receipt of initial advance milestone."
    )

    val ELECTRONICS = BusinessTerminology(
        industryKey = "ELECTRONICS_MOBILE",
        templateId = 11L,
        title = "Electronics, Mobile & Appliances",
        subtitle = "Smartphones, TVs, accessories, repairs & gadgets",
        iconEmoji = "📱",
        partyLabel = "Customer",
        partyPluralLabel = "Customers",
        inventoryLabel = "Devices & Gadgets",
        estimateDocLabel = "Price Quotation / Repair Estimate",
        invoiceDocLabel = "Electronics Tax Invoice",
        lineItemTerm = "Model / Device Item",
        referenceNumberTerm = "IMEI / Serial / Job Card No.",
        defaultTaxRate = 18.0,
        isCapacityBased = false,
        quickStapleItems = listOf(
            QuickItemChip("Android Smartphone (8GB/128GB)", "5G AMOLED 50MP camera with fast charger", "NOS", 16999.0, 18.0, "85171300", warranty = "1 Year Brand Warranty", brand = "Samsung"),
            QuickItemChip("Smart LED TV 43\" 4K", "Ultra HD HDR Google TV with Dolby Audio", "NOS", 24990.0, 18.0, "85287200", warranty = "2 Years Warranty", brand = "LG"),
            QuickItemChip("Fast Charger 65W GaN", "Type-C dual port fast charger with cable", "NOS", 1499.0, 18.0, "85044030", warranty = "6 Months Warranty"),
            QuickItemChip("TWS Bluetooth Earbuds", "Active Noise Cancellation 30hr battery", "NOS", 1999.0, 18.0, "85183000", warranty = "1 Year Warranty", brand = "boAt"),
            QuickItemChip("Display Screen Replacement", "Original OLED screen assembly with fitting", "SERVICE", 3200.0, 18.0, "998713", warranty = "90 Days Touch Warranty")
        ),
        defaultInvoiceNote = "Consumer electronics & accessories tax invoice with serial tracking",
        defaultEstimateNote = "Device purchase / Repair service quotation",
        defaultInvoiceTerms = "1. Warranty serviced directly through manufacturer authorized service centers.\n2. Physical damage, water ingress, and burn-outs void brand warranty.\n3. Keep tax invoice copy for warranty claims.",
        defaultEstimateTerms = "1. Repair estimate valid for 7 days.\n2. Internal diagnostics fee non-refundable if repair is declined."
    )

    val RETAIL = BusinessTerminology(
        industryKey = "RETAILER",
        templateId = 6L,
        title = "Retail & Fashion Boutique",
        subtitle = "Apparel, footwear, cosmetics, lifestyle & accessories",
        iconEmoji = "🛍️",
        partyLabel = "Customer",
        partyPluralLabel = "Customers",
        inventoryLabel = "Garments & Retail Stock",
        estimateDocLabel = "Price Estimate / Quote",
        invoiceDocLabel = "Retail Tax Invoice / Bill",
        lineItemTerm = "Article / Garment",
        referenceNumberTerm = "Barcode / SKU Code",
        defaultTaxRate = 5.0,
        isCapacityBased = false,
        quickStapleItems = listOf(
            QuickItemChip("Men's Cotton Formal Shirt", "Slim fit 100% breathable Egyptian cotton", "PCS", 999.0, 5.0, "62052000", brand = "Arrow"),
            QuickItemChip("Denim Jeans (Stretchable)", "Mid-rise dark indigo washed denim", "PCS", 1499.0, 12.0, "62034200", brand = "Levi's"),
            QuickItemChip("Women's Cotton Kurti Set", "Printed kurti with palazzo & dupatta", "SET", 1250.0, 5.0, "62044220"),
            QuickItemChip("Leather Formal Shoes", "Genuine leather cushioned derby shoes", "PAIR", 2299.0, 18.0, "64039990"),
            QuickItemChip("Casual Polo T-Shirt", "Pique cotton bio-washed regular fit", "PCS", 599.0, 5.0, "61091000")
        ),
        defaultInvoiceNote = "Thank you for shopping with us! Looking forward to your next visit.",
        defaultEstimateNote = "Bulk purchase / Corporate clothing estimate",
        defaultInvoiceTerms = "1. Exchange allowed within 7 days with price tag and original bill intact.\n2. Innerwear and altered garments cannot be exchanged.\n3. No cash refund.",
        defaultEstimateTerms = "1. Bulk discount applicable on quantity orders.\n2. Delivery timeline: 5-7 business days."
    )

    val SWEETS = BusinessTerminology(
        industryKey = "SWEET_BAKERY",
        templateId = 12L,
        title = "Sweets, Bakery & Confectionery",
        subtitle = "Mithai, cakes, pastries, namkeen & party snacks",
        iconEmoji = "🍬",
        partyLabel = "Customer",
        partyPluralLabel = "Customers",
        inventoryLabel = "Sweets & Bakery Stock",
        estimateDocLabel = "Party Order / Sweet Estimate",
        invoiceDocLabel = "Tax Invoice / Cash Bill",
        lineItemTerm = "Sweet / Cake Item",
        referenceNumberTerm = "Token / Order No.",
        defaultTaxRate = 5.0,
        isCapacityBased = false,
        quickStapleItems = listOf(
            QuickItemChip("Kaju Katli (Pure Cashew)", "Thin silver foil diamond cut royal sweet", "KG", 880.0, 5.0, "21069099", extraAttributes = "Desi Ghee | Shelf Life: 15 Days"),
            QuickItemChip("Motichoor Ladoo (Desi Ghee)", "Fine boondi made with 100% pure cow ghee", "KG", 480.0, 5.0, "21069099", extraAttributes = "Desi Ghee | Shelf Life: 7 Days"),
            QuickItemChip("Dutch Chocolate Truffle Cake", "Rich dark chocolate sponge with ganache", "KG", 650.0, 18.0, "19059010", extraAttributes = "Eggless | Fresh Daily"),
            QuickItemChip("Butter Croissant / Puff", "Golden flaky layered bakery snack", "PCS", 40.0, 18.0, "19059090", extraAttributes = "Fresh Daily"),
            QuickItemChip("Gulab Jamun (Hot)", "Soft milk mawa dumplings in rose saffron syrup", "KG", 380.0, 5.0, "21069099")
        ),
        defaultInvoiceNote = "Freshly prepared sweets & baked delicacies. Keep refrigerated after purchase.",
        defaultEstimateNote = "Wedding / Festival bulk sweet boxes quotation",
        defaultInvoiceTerms = "1. Consume sweets within stated freshness shelf life.\n2. Perishable food items cannot be exchanged once taken outside counter.\n3. Made with pure ingredients & utmost hygiene.",
        defaultEstimateTerms = "1. Custom gift box packaging orders confirmed on 50% advance.\n2. Festival pre-bookings closed 48 hours prior."
    )

    val DISTRIBUTOR = BusinessTerminology(
        industryKey = "DISTRIBUTOR",
        templateId = 7L,
        title = "Wholesale & Distribution",
        subtitle = "Bulk FMCG, trade supply, stockist & B2B delivery",
        iconEmoji = "📦",
        partyLabel = "Dealer / Retailer",
        partyPluralLabel = "Dealers & Retailers",
        inventoryLabel = "Bulk Inventory & Warehousing",
        estimateDocLabel = "Wholesale Price Quotation",
        invoiceDocLabel = "Commercial Tax Invoice & E-Way Bill",
        lineItemTerm = "Case / Master Carton",
        referenceNumberTerm = "Purchase Order (PO) / Delivery Challan",
        defaultTaxRate = 18.0,
        isCapacityBased = false,
        quickStapleItems = listOf(
            QuickItemChip("Detergent Powder (Case of 24)", "1kg pack x 24 pcs master carton", "CASE", 2400.0, 18.0, "34029011"),
            QuickItemChip("Refined Oil Tin (15kg Bulk)", "Commercial kitchen packing tin", "TIN", 1950.0, 5.0, "15121910"),
            QuickItemChip("Tea Powder Premium (Case 40)", "250g x 40 retail pouches master box", "CASE", 3800.0, 5.0, "09024020"),
            QuickItemChip("Biscuits Assorted Carton", "100 packs x 50g variety carton", "CASE", 950.0, 18.0, "19053100")
        ),
        defaultInvoiceNote = "Wholesale distribution supply against authorized purchase order",
        defaultEstimateNote = "Trade price list & tiered volume quotation",
        defaultInvoiceTerms = "1. Interest @18% p.a. charged on bills delayed beyond credit period (15 days).\n2. E-way bill generated for consignments above ₹50,000.\n3. Goods transit insurance is under consignee scope.",
        defaultEstimateTerms = "1. Trade discounts applicable strictly as per MOQ slab.\n2. Freight extra as per transporter docket."
    )

    val SOLAR = BusinessTerminology(
        industryKey = "SOLAR_ENERGY",
        templateId = 1L,
        title = "Solar EPC & Renewable Energy",
        subtitle = "Rooftop solar, grid-tie inverters, subsidies & solar pumps",
        iconEmoji = "☀️",
        partyLabel = "Customer / Site Owner",
        partyPluralLabel = "Customers",
        inventoryLabel = "Solar Equipment & Inventory",
        estimateDocLabel = "Solar Proposal & Estimate",
        invoiceDocLabel = "Solar Plant Tax Invoice",
        lineItemTerm = "Equipment / Component",
        referenceNumberTerm = "Consumer / Sanctioned Load No.",
        defaultTaxRate = 5.0,
        isCapacityBased = true, // Solar retains the Capacity (kW x Rate/kW) calculation
        quickStapleItems = listOf(
            QuickItemChip("3kW Grid-Tie Solar Package", "Waaree 540W Mono PERC + 3.3kW Inverter + Structure", "KW", 52000.0, 5.0, "85414300", warranty = "25 Yrs Panel / 5 Yrs Inverter"),
            QuickItemChip("5kW Three Phase Solar Plant", "Tier-1 Mono Half-cut Modules + 5kW Grid Inverter", "KW", 49000.0, 5.0, "85414300", warranty = "25 Yrs Panel / 5 Yrs Inverter"),
            QuickItemChip("10kW Commercial Solar System", "Heavy Duty GI Structure, AC/DC Protections & Net Metering", "KW", 46000.0, 5.0, "85414300", warranty = "25 Yrs Panel / 5 Yrs Inverter")
        ),
        defaultInvoiceNote = "Rooftop on-grid solar photovoltaic power plant with PM Surya Ghar subsidy compliance",
        defaultEstimateNote = "Bank loan proposal & solar feasibility quotation",
        defaultInvoiceTerms = DEFAULT_ESTIMATE_TERMS,
        defaultEstimateTerms = DEFAULT_ESTIMATE_TERMS
    )

    val ALL: List<BusinessTerminology> = listOf(
        KIRANA,
        PHARMACY,
        RESTAURANT,
        HARDWARE,
        CONSTRUCTION,
        SALON,
        SERVICES,
        ELECTRONICS,
        RETAIL,
        SWEETS,
        DISTRIBUTOR,
        SOLAR
    )

    fun getForCategory(category: String?): BusinessTerminology {
        val cat = category?.uppercase()?.trim() ?: "KIRANA_GROCERY"
        return when {
            cat.contains("KIRANA") || cat.contains("GROCERY") -> KIRANA
            cat.contains("PHARMACY") || cat.contains("MEDICAL") || cat.contains("HEALTH") -> PHARMACY
            cat.contains("RESTAURANT") || cat.contains("HOTEL") || cat.contains("CAFE") || cat.contains("FOOD") -> RESTAURANT
            cat.contains("HARDWARE") || cat.contains("SANITARY") || cat.contains("PAINT") -> HARDWARE
            cat.contains("CONSTRUCTION") || cat.contains("BUILD") || cat.contains("CIVIL") -> CONSTRUCTION
            cat.contains("SALON") || cat.contains("SPA") || cat.contains("BEAUTY") -> SALON
            cat.contains("SERVICES") || cat.contains("CONSULT") || cat.contains("TECH") -> SERVICES
            cat.contains("ELECTRONIC") || cat.contains("MOBILE") -> ELECTRONICS
            cat.contains("RETAIL") || cat.contains("FASHION") || cat.contains("CLOTH") -> RETAIL
            cat.contains("SWEET") || cat.contains("BAKERY") -> SWEETS
            cat.contains("DISTRIBUT") || cat.contains("WHOLESALE") -> DISTRIBUTOR
            cat.contains("SOLAR") -> SOLAR
            else -> KIRANA
        }
    }

    fun getForTemplateId(templateId: Long): BusinessTerminology {
        return when (templateId) {
            1L -> SOLAR
            2L -> CONSTRUCTION
            3L -> KIRANA
            4L -> PHARMACY
            5L -> SALON
            6L -> RETAIL
            7L -> DISTRIBUTOR
            8L -> RESTAURANT
            9L -> HARDWARE
            10L -> SERVICES
            11L -> ELECTRONICS
            12L -> SWEETS
            else -> KIRANA
        }
    }

    fun getForProfile(profile: com.example.data.entity.BusinessProfile?): BusinessTerminology {
        if (profile == null) return SOLAR
        if (!profile.category.isNullOrBlank()) {
            val matched = getForCategory(profile.category)
            if (matched != KIRANA || profile.category.contains("KIRANA", ignoreCase = true) || profile.category.contains("GROCERY", ignoreCase = true)) {
                return matched
            }
        }
        return getForTemplateId(profile.businessTemplateId)
    }
}
