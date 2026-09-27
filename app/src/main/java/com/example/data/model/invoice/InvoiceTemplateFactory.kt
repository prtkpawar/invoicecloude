package com.example.data.model.invoice

import android.graphics.Paint
import com.example.data.entity.BusinessProfile
import com.example.data.model.Company
import com.example.data.model.Customer
import com.example.data.model.Doc
import com.example.data.model.DocWithDetails
import com.example.data.model.FirmBranding
import com.example.utils.Formatters
import com.example.utils.NumWords

/**
 * Factory that dynamically generates adaptive invoice structures, table columns,
 * and metadata fields matching the firm's industry (Medical, Kirana, Solar, Construction, etc.).
 */
object InvoiceTemplateFactory {

    fun getColumnsForType(type: InvoiceBusinessType, isInvoice: Boolean = true): List<InvoiceColumnDef> {
        return when (type) {
            InvoiceBusinessType.MEDICAL -> listOf(
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

            InvoiceBusinessType.KIRANA -> listOf(
                InvoiceColumnDef("sr", "SR", 0.05f, Paint.Align.CENTER),
                InvoiceColumnDef("desc", "GROCERY ITEM / COMMODITY", 0.32f, Paint.Align.LEFT),
                InvoiceColumnDef("unit_wt", "UNIT / WT", 0.12f, Paint.Align.CENTER),
                InvoiceColumnDef("qty", "QTY", 0.08f, Paint.Align.CENTER),
                InvoiceColumnDef("mrp", "MRP (₹)", 0.11f, Paint.Align.RIGHT),
                InvoiceColumnDef("rate", "RATE (₹)", 0.11f, Paint.Align.RIGHT),
                InvoiceColumnDef("disc", "DISC", 0.07f, Paint.Align.RIGHT),
                InvoiceColumnDef("amount", "AMOUNT (₹)", 0.14f, Paint.Align.RIGHT)
            )

            InvoiceBusinessType.CONSTRUCTION -> listOf(
                InvoiceColumnDef("sr", "SR", 0.05f, Paint.Align.CENTER),
                InvoiceColumnDef("desc", "WORK SPECIFICATION / BOQ SCHEDULE", 0.44f, Paint.Align.LEFT),
                InvoiceColumnDef("qty", "QTY", 0.10f, Paint.Align.CENTER),
                InvoiceColumnDef("unit", "UNIT", 0.09f, Paint.Align.CENTER),
                InvoiceColumnDef("rate", "RATE (₹)", 0.14f, Paint.Align.RIGHT),
                InvoiceColumnDef("amount", "AMOUNT (₹)", 0.18f, Paint.Align.RIGHT)
            )

            InvoiceBusinessType.ELECTRONICS_RETAIL -> listOf(
                InvoiceColumnDef("sr", "SR", 0.05f, Paint.Align.CENTER),
                InvoiceColumnDef("desc", "PRODUCT / MODEL / SPECIFICATION", 0.33f, Paint.Align.LEFT),
                InvoiceColumnDef("brand", "BRAND / MODEL", 0.13f, Paint.Align.LEFT),
                InvoiceColumnDef("warranty", "WARRANTY", 0.11f, Paint.Align.CENTER),
                InvoiceColumnDef("qty", "QTY", 0.08f, Paint.Align.CENTER),
                InvoiceColumnDef("rate", "RATE (₹)", 0.14f, Paint.Align.RIGHT),
                InvoiceColumnDef("amount", "AMOUNT (₹)", 0.16f, Paint.Align.RIGHT)
            )

            InvoiceBusinessType.SOLAR -> listOf(
                InvoiceColumnDef("sr", "SR", 0.05f, Paint.Align.CENTER),
                InvoiceColumnDef("desc", "SOLAR EQUIPMENT & TURNKEY WORKS", 0.35f, Paint.Align.LEFT),
                InvoiceColumnDef("hsn", "HSN/SAC", 0.10f, Paint.Align.CENTER),
                InvoiceColumnDef("qty", "CAPACITY", 0.09f, Paint.Align.CENTER),
                InvoiceColumnDef("rate", "RATE (₹)", 0.11f, Paint.Align.RIGHT),
                InvoiceColumnDef("cgst", "CGST", 0.08f, Paint.Align.RIGHT),
                InvoiceColumnDef("sgst", "SGST", 0.08f, Paint.Align.RIGHT),
                InvoiceColumnDef("amount", "AMOUNT (₹)", 0.14f, Paint.Align.RIGHT)
            )

            else -> listOf(
                InvoiceColumnDef("sr", "SR", 0.05f, Paint.Align.CENTER),
                InvoiceColumnDef("desc", "ITEM DESCRIPTION / SERVICE", 0.37f, Paint.Align.LEFT),
                InvoiceColumnDef("hsn", "HSN/SAC", 0.11f, Paint.Align.CENTER),
                InvoiceColumnDef("qty", "QTY", 0.08f, Paint.Align.CENTER),
                InvoiceColumnDef("rate", "RATE (₹)", 0.11f, Paint.Align.RIGHT),
                InvoiceColumnDef("cgst", "CGST", 0.07f, Paint.Align.RIGHT),
                InvoiceColumnDef("sgst", "SGST", 0.07f, Paint.Align.RIGHT),
                InvoiceColumnDef("amount", "AMOUNT (₹)", 0.14f, Paint.Align.RIGHT)
            )
        }
    }

    fun buildDynamicInvoice(
        detail: DocWithDetails,
        business: BusinessProfile?,
        company: Company? = null,
        themeStr: String? = null
    ): DynamicInvoiceData {
        val doc = detail.doc
        val customer = detail.customer ?: Customer(name = "Valued Customer")
        val firm = FirmBranding.from(business, company, themeStr)
        val businessType = InvoiceBusinessType.fromTemplateId(firm.businessTemplateId)
        val isInvoice = doc.isInvoice

        val docTitle = when {
            doc.isConstructionEstimate -> "CIVIL ESTIMATE & BOQ"
            doc.isConstructionInvoice -> "CIVIL TAX INVOICE"
            isInvoice -> "TAX INVOICE"
            else -> "PRICE ESTIMATE / QUOTATION"
        }

        val docSubtitle = when {
            doc.isConstruction -> "Bill of Quantities / Work Schedule"
            businessType == InvoiceBusinessType.MEDICAL -> "Retail Chemist & Druggist Bill"
            businessType == InvoiceBusinessType.KIRANA -> "Retail Cash / Credit Memo"
            isInvoice -> "Original For Recipient"
            else -> "Commercial Proposal"
        }

        val columns = getColumnsForType(businessType, isInvoice)

        // Convert DocItem rows into rich DynamicLineItem models
        val items = if (detail.items.isNotEmpty()) {
            detail.items.mapIndexed { index, item ->
                val lineCgst = if (doc.gstPercent > 0) item.amount * (doc.gstPercent / 200.0) else 0.0
                val lineSgst = if (doc.gstPercent > 0) item.amount * (doc.gstPercent / 200.0) else 0.0

                // Parse batch / expiry if stored in description or extra attributes
                val desc = item.description.ifBlank { item.label }
                val (batch, exp) = parseBatchAndExpiry(desc)
                val weight = parseWeightOrUnit(item.unit, item.qty)

                DynamicLineItem(
                    srNo = index + 1,
                    label = item.label,
                    description = desc,
                    hsn = item.hsn.ifBlank { businessType.defaultHsn },
                    batchNo = batch,
                    expiryDate = exp,
                    weightOrMeasure = weight,
                    unit = item.unit.ifBlank { "Nos" },
                    qty = item.qty,
                    mrp = (item.rate * 1.15).coerceAtLeast(item.rate),
                    rate = item.rate,
                    discount = 0.0,
                    taxableAmount = item.amount,
                    gstRate = doc.gstPercent,
                    cgst = lineCgst,
                    sgst = lineSgst,
                    totalAmount = item.amount,
                    warranty = "1 Year",
                    brand = item.label.takeIf { it.isNotBlank() && it != desc }
                )
            }
        } else {
            // Single line fallback (e.g. Solar Turnkey Package)
            val kwStr = if (doc.kw > 0) "${doc.kw} kW" else ""
            val pkgName = "${kwStr} On-Grid Solar PV Power Plant System (Turnkey Execution)".trim()
            val effectiveRate = if (doc.ratePerKw > 0) doc.ratePerKw else doc.taxable
            val effectiveQty = if (doc.kw > 0) doc.kw else 1.0

            listOf(
                DynamicLineItem(
                    srNo = 1,
                    label = "SOLAR_SYSTEM",
                    description = pkgName,
                    hsn = "85414300",
                    batchNo = null,
                    expiryDate = null,
                    weightOrMeasure = "$kwStr System",
                    unit = if (doc.kw > 0) "kW" else "set",
                    qty = effectiveQty,
                    mrp = doc.taxable * 1.1,
                    rate = effectiveRate,
                    discount = doc.discount,
                    taxableAmount = doc.taxable,
                    gstRate = doc.gstPercent,
                    cgst = doc.cgst,
                    sgst = doc.sgst,
                    totalAmount = doc.taxable,
                    warranty = "5 Years",
                    brand = firm.brandName
                )
            )
        }

        val defaultTermsList = when (businessType) {
            InvoiceBusinessType.MEDICAL -> listOf(
                "1. Goods once sold will not be taken back without original bill & valid batch.",
                "2. Please check expiry date and tamper-evident seal before consumption.",
                "3. Medicines requiring refrigeration (2-8°C) are non-returnable.",
                "4. Subject to ${firm.city} jurisdiction only."
            )
            InvoiceBusinessType.KIRANA -> listOf(
                "1. Goods once sold will not be exchanged or returned after 24 hours.",
                "2. Check weight, seal and count before leaving the cash counter.",
                "3. Prices inclusive of all applicable taxes unless specified.",
                "4. Thank you for shopping with ${firm.brandName}!"
            )
            InvoiceBusinessType.CONSTRUCTION -> listOf(
                "1. Measurements will be based strictly on actual work executed at site.",
                "2. Water and electricity to be arranged by the client at site.",
                "3. Stage-wise progress billing submitted as per structural completion.",
                "4. Subject to ${firm.city} jurisdiction only."
            )
            InvoiceBusinessType.SOLAR -> listOf(
                "1. Quotation validity strictly 7 days from issue date.",
                "2. DISCOM load extension & demand note in consumer scope.",
                "3. Power yield contingent on solar irradiance and local grid uptime.",
                "4. 5 Years comprehensive on-site warranty on solar inverter & structure."
            )
            else -> listOf(
                "1. Goods once sold will not be taken back or exchanged.",
                "2. Subject to local ${firm.city} jurisdiction only.",
                "3. Full payment required as per agreed payment terms.",
                "4. Direct manufacturer warranty applies on all branded items."
            )
        }

        val terms = if (doc.terms.isNotBlank()) {
            doc.terms.split("\n").filter { it.isNotBlank() }
        } else {
            defaultTermsList
        }

        val customerAddress = listOf(customer.village, customer.address)
            .filter { it.isNotBlank() }
            .joinToString(", ")

        val headerMeta = InvoiceHeaderMeta(
            doctorName = if (businessType == InvoiceBusinessType.MEDICAL) "Dr. Self / Registered Practitioner" else null,
            rxNumber = if (businessType == InvoiceBusinessType.MEDICAL) "RX-${doc.docNo}" else null,
            drugLicenseNo = if (businessType == InvoiceBusinessType.MEDICAL) "20B/21B-MH-DHL-19829" else null,
            siteLocation = if (businessType == InvoiceBusinessType.CONSTRUCTION || businessType == InvoiceBusinessType.SOLAR) customerAddress.ifBlank { "${firm.city} Site" } else null,
            boqCode = if (businessType == InvoiceBusinessType.CONSTRUCTION) "BOQ-${doc.docNo}" else null,
            customerGstin = customer.gstin
        )

        val words = if (doc.amountWords.isNotBlank()) doc.amountWords else NumWords.rupees(doc.total)

        return DynamicInvoiceData(
            firm = firm,
            businessType = businessType,
            docTitle = docTitle,
            docSubtitle = docSubtitle,
            docNo = doc.docNo,
            docDate = doc.docDate,
            dueDateOrValidTill = if (isInvoice) doc.dueDate else doc.validTill,
            isInvoice = isInvoice,
            customerName = customer.name.ifBlank { "Valued Customer" },
            customerMobile = customer.mobile,
            customerAddress = customerAddress,
            customerGstin = customer.gstin,
            headerMeta = headerMeta,
            columns = columns,
            items = items,
            taxableSubtotal = doc.taxable,
            discountTotal = doc.discount,
            cgstTotal = doc.cgst,
            sgstTotal = doc.sgst,
            grandTotal = doc.total,
            totalPaid = detail.totalPaid,
            balanceDue = detail.pending,
            amountInWords = words,
            note = doc.note,
            terms = terms
        )
    }

    private fun parseBatchAndExpiry(text: String): Pair<String?, String?> {
        var batch: String? = null
        var exp: String? = null

        val batchRegex = Regex("""(?i)(?:batch|b\.no|bno)[\s:#]+([A-Z0-9\-]+)""")
        val expRegex = Regex("""(?i)(?:exp|expiry)[\s:#]+([A-Z0-9\/\-]+)""")

        batchRegex.find(text)?.let {
            batch = it.groupValues.getOrNull(1)
        }
        expRegex.find(text)?.let {
            exp = it.groupValues.getOrNull(1)
        }

        return Pair(batch ?: "BT-2026", exp ?: "08/2028")
    }

    private fun parseWeightOrUnit(unit: String, qty: Double): String {
        val u = unit.uppercase().trim()
        val qtyStr = if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
        return when (u) {
            "KG" -> "$qtyStr KG"
            "GM", "GMS" -> "$qtyStr GM"
            "LTR", "LITER" -> "$qtyStr LTR"
            "ML" -> "$qtyStr ML"
            "PKT", "PACK" -> "$qtyStr Pack"
            "STRIP" -> "$qtyStr Strip"
            "BOTTLE" -> "$qtyStr Bot"
            "BOX" -> "$qtyStr Box"
            "SQFT" -> "$qtyStr Sq.Ft"
            "BRASS" -> "$qtyStr Brass"
            else -> "$qtyStr $unit"
        }
    }
}
