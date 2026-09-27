package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.AppSetting
import com.example.data.model.Company
import com.example.data.model.Customer
import com.example.data.model.Party
import com.example.data.model.PartyCategory
import com.example.data.model.Doc
import com.example.data.model.DocItem
import com.example.data.model.DocStatus
import com.example.data.model.DocType
import com.example.data.model.DocWithDetails
import com.example.data.model.ItemMaster
import com.example.data.model.Payment
import com.example.data.model.StarterItem
import com.example.utils.Formatters
import kotlinx.coroutines.Dispatchers
import com.example.data.model.BusinessCatalogPresets
import com.example.data.model.LedgerEntry
import com.example.data.model.LedgerTransactionType
import com.example.data.model.PartyStatementData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.room.withTransaction

/**
 * Dashboard statistics summary.
 *
 * NOTE: [totalPending], [receivedThisMonth], and [totalInvoiced] use Double for
 * legacy compatibility. New code should convert to [com.example.core.money.Money]
 * via `Money.fromLegacyDouble()` before any calculation or display.
 * TODO: Migrate to Money type after the DAO layer is refactored.
 */
data class DashboardStats(
    val totalPending: Double = 0.0,
    val receivedThisMonth: Double = 0.0,
    val openEstimatesCount: Int = 0,
    val totalInvoiced: Double = 0.0,
    val dueInvoices: List<DocWithDetails> = emptyList(),
    val pendingEstimates: List<DocWithDetails> = emptyList()
)

class BillingRepository(val db: AppDatabase) {

    companion object {
        /** Sentinel value returned by recordQuickPaymentForParty when no invoice exists.
         *  The UI should check for this and ask the user to confirm before creating one. */
        const val QUICK_PAYMENT_NO_INVOICE = -1L
    }

    suspend fun getItemsForDoc(docId: Int): List<DocItem> = withContext(Dispatchers.IO) {
        db.docItemDao().getItemsForDoc(docId)
    }

    // -------------------------------- Company --------------------------------
    fun getCompanyFlow(): Flow<Company?> = db.companyDao().getCompanyFlow()

    suspend fun getCompany(): Company = withContext(Dispatchers.IO) {
        db.companyDao().getCompany() ?: Company()
    }

    suspend fun updateCompany(company: Company) = withContext(Dispatchers.IO) {
        db.companyDao().insert(company)
    }

    // -------------------------------- Parties / Customers --------------------------------
    fun getAllPartiesFlow(businessId: Long = 1L): Flow<List<Party>> = db.partyDao().getAllPartiesFlow(businessId)

    fun getPartiesByCategoryFlow(category: PartyCategory, businessId: Long = 1L): Flow<List<Party>> =
        db.partyDao().getPartiesByCategoryFlow(category, businessId)

    fun searchPartiesFlow(query: String, businessId: Long = 1L): Flow<List<Party>> =
        if (query.isBlank()) db.partyDao().getAllPartiesFlow(businessId)
        else db.partyDao().searchPartiesFlow(query, businessId)

    suspend fun getPartyById(id: Int): Party? = withContext(Dispatchers.IO) {
        db.partyDao().getPartyById(id)
    }

    suspend fun saveParty(party: Party): Long = withContext(Dispatchers.IO) {
        db.partyDao().insert(party)
    }

    suspend fun deleteParty(party: Party, businessId: Long = 1L): Boolean = withContext(Dispatchers.IO) {
        val count = db.partyDao().getDocumentCountForParty(party.id, businessId)
        if (count > 0) return@withContext false
        db.partyDao().delete(party)
        true
    }

    // Customer aliases for backwards compatibility
    fun getAllCustomersFlow(businessId: Long = 1L): Flow<List<Party>> = getAllPartiesFlow(businessId)
    fun searchCustomersFlow(query: String, businessId: Long = 1L): Flow<List<Party>> = searchPartiesFlow(query, businessId)
    suspend fun getCustomerById(id: Int): Party? = getPartyById(id)
    suspend fun saveCustomer(customer: Party): Long = saveParty(customer)
    suspend fun deleteCustomer(customer: Party, businessId: Long = 1L): Boolean = deleteParty(customer, businessId)

    // -------------------------------- Settings --------------------------------
    fun getSettingFlow(key: String, businessId: Long = 1L): Flow<String?> = db.settingDao().getSettingFlow(key, businessId)

    suspend fun getSetting(key: String, fallback: String = "", businessId: Long = 1L): String = withContext(Dispatchers.IO) {
        db.settingDao().getSetting(key, businessId) ?: fallback
    }

    suspend fun setSetting(key: String, value: String, businessId: Long = 1L) = withContext(Dispatchers.IO) {
        db.settingDao().setSetting(AppSetting(key, value, businessId))
    }

    // -------------------------------- Item Master --------------------------------
    fun getItemMasterFlow(businessId: Long = 1L): Flow<List<ItemMaster>> = db.itemMasterDao().getActiveItemsFlow(businessId)

    fun getItemsByCategoryFlow(category: String, businessId: Long = 1L): Flow<List<ItemMaster>> =
        db.itemMasterDao().getItemsByCategoryFlow(category, businessId)

    suspend fun getItemMaster(businessId: Long = 1L): List<ItemMaster> = withContext(Dispatchers.IO) {
        db.itemMasterDao().getActiveItems(businessId)
    }

    suspend fun saveItem(item: ItemMaster): Long = withContext(Dispatchers.IO) {
        if (item.id == 0) {
            db.itemMasterDao().insert(item)
        } else {
            db.itemMasterDao().update(item)
            item.id.toLong()
        }
    }

    suspend fun deleteItem(item: ItemMaster) = withContext(Dispatchers.IO) {
        db.itemMasterDao().delete(item)
    }

    suspend fun saveItemMaster(items: List<ItemMaster>, businessId: Long = 1L) = withContext(Dispatchers.IO) {
        db.itemMasterDao().deleteAll(businessId)
        db.itemMasterDao().insertAll(items.mapIndexed { i, it -> it.copy(sortOrder = i, businessId = businessId) })
    }

    suspend fun importStarterCatalogForBusiness(businessId: Long, businessTypeKey: String, replaceExisting: Boolean = false) = withContext(Dispatchers.IO) {
        val preset = BusinessCatalogPresets.getPreset(businessTypeKey)
        val starterList = preset.starterItems.mapIndexed { idx, item ->
            ItemMaster(
                businessId = businessId,
                businessType = preset.id,
                label = item.label,
                description = item.description,
                category = item.category,
                unit = item.unit,
                rate = item.rate,
                purchasePrice = item.purchasePrice,
                taxRate = item.taxRate,
                hsn = item.hsn,
                mrp = item.mrp,
                barcode = item.barcode,
                batchNo = item.batchNo,
                expiry = item.expiry,
                warranty = item.warranty,
                brand = item.brand,
                extraAttributes = item.extraAttributes,
                sortOrder = idx,
                isActive = true
            )
        }
        if (replaceExisting) {
            db.itemMasterDao().deleteAll(businessId)
        }
        db.itemMasterDao().insertAll(starterList)
    }

    suspend fun importStarterItem(businessId: Long, starterItem: StarterItem, presetId: String) = withContext(Dispatchers.IO) {
        val newItem = ItemMaster(
            businessId = businessId,
            businessType = presetId,
            label = starterItem.label,
            description = starterItem.description,
            category = starterItem.category,
            unit = starterItem.unit,
            rate = starterItem.rate,
            purchasePrice = starterItem.purchasePrice,
            taxRate = starterItem.taxRate,
            hsn = starterItem.hsn,
            mrp = starterItem.mrp,
            barcode = starterItem.barcode,
            batchNo = starterItem.batchNo,
            expiry = starterItem.expiry,
            warranty = starterItem.warranty,
            brand = starterItem.brand,
            extraAttributes = starterItem.extraAttributes,
            isActive = true
        )
        db.itemMasterDao().insert(newItem)
    }

    // -------------------------------- Documents --------------------------------
    suspend fun getNextDocNo(docType: String, businessId: Long = 1L): String = withContext(Dispatchers.IO) {
        val (key, prefixKey, defaultPrefix) = when (docType) {
            DocType.INVOICE -> Triple("next_inv_no", "inv_prefix", "INV-")
            DocType.CONST_ESTIMATE -> Triple("next_const_est_no", "const_est_prefix", "SCE-")
            DocType.CONST_INVOICE -> Triple("next_const_inv_no", "const_inv_prefix", "SCI-")
            else -> Triple("next_est_no", "est_prefix", "EST-")
        }

        val prefix = db.settingDao().getSetting(prefixKey, businessId) ?: defaultPrefix
        var num = (db.settingDao().getSetting(key, businessId) ?: "1").toIntOrNull() ?: 1

        var finalCandidate = ""
        while (true) {
            val candidate = "$prefix${num.toString().padStart(5, '0')}"
            val exists = db.docDao().countByDocNo(candidate, businessId)
            if (exists == 0) {
                finalCandidate = candidate
                break
            }
            num++
        }
        finalCandidate
    }

    suspend fun saveDoc(doc: Doc, items: List<DocItem>, businessId: Long? = null): Int = withContext(Dispatchers.IO) {
        db.withTransaction {
            val targetBusinessId = businessId ?: (if (doc.businessId > 0L) doc.businessId else 1L)
            var docToSave = doc.copy(businessId = targetBusinessId)
            if (docToSave.docNo.isBlank()) {
                val nextNo = getNextDocNo(docToSave.docType, targetBusinessId)
                docToSave = docToSave.copy(docNo = nextNo)
            }

            val docId = if (docToSave.id == 0) {
                val id = db.docDao().insert(docToSave).toInt()
                // Increment the counter setting
                val key = when (docToSave.docType) {
                    DocType.INVOICE -> "next_inv_no"
                    DocType.CONST_ESTIMATE -> "next_const_est_no"
                    DocType.CONST_INVOICE -> "next_const_inv_no"
                    else -> "next_est_no"
                }
                val lastNumPart = docToSave.docNo.filter { it.isDigit() }.toIntOrNull() ?: 1
                db.settingDao().setSetting(AppSetting(key, (lastNumPart + 1).toString(), targetBusinessId))
                id
            } else {
                db.docDao().update(docToSave)
                docToSave.id
            }

            // Replace items
            db.docItemDao().deleteItemsForDoc(docId)
            val itemsWithDocId = items.mapIndexed { idx, it ->
                it.copy(docId = docId, sortOrder = idx, businessId = targetBusinessId)
            }
            db.docItemDao().insertAll(itemsWithDocId)

            docId
        }
    }

    suspend fun deleteDoc(doc: Doc) = withContext(Dispatchers.IO) {
        db.docDao().delete(doc)
    }

    suspend fun updateDocStatus(docId: Int, status: String) = withContext(Dispatchers.IO) {
        db.docDao().updateStatus(docId, status)
    }

    suspend fun updateDueDate(docId: Int, dueDate: String) = withContext(Dispatchers.IO) {
        db.docDao().updateDueDate(docId, dueDate)
    }

    suspend fun updatePdfPath(docId: Int, pdfPath: String) = withContext(Dispatchers.IO) {
        db.docDao().updatePdfPath(docId, pdfPath)
    }

    suspend fun updateDocTerms(docId: Int, terms: String) = withContext(Dispatchers.IO) {
        db.docDao().updateTerms(docId, terms)
    }

    suspend fun updateDocBusinessId(docId: Int, businessId: Long) = withContext(Dispatchers.IO) {
        db.docDao().updateDocBusinessId(docId, businessId)
    }

    suspend fun getDocById(docId: Int): Doc? = withContext(Dispatchers.IO) {
        db.docDao().getDocById(docId)
    }

    suspend fun getChildInvoiceForEstimate(estimateId: Int): Doc? = withContext(Dispatchers.IO) {
        db.docDao().getChildInvoiceForEstimate(estimateId)
    }

    suspend fun convertToInvoice(estimateId: Int, businessId: Long? = null): Int = withContext(Dispatchers.IO) {
        val est = db.docDao().getDocById(estimateId) ?: error("Estimate not found")
        val targetBusinessId = businessId ?: (if (est.businessId > 0L) est.businessId else 1L)
        val items = db.docItemDao().getItemsForDoc(estimateId)

        val targetType = if (est.isConstruction) DocType.CONST_INVOICE else DocType.INVOICE
        val invDocNo = getNextDocNo(targetType, targetBusinessId)
        val today = Formatters.todayIso()
        val dueDate = Formatters.datePlusDaysIso(15)

        val invoice = Doc(
            businessId = targetBusinessId,
            docType = targetType,
            docNo = invDocNo,
            docDate = today,
            validTill = "",
            dueDate = dueDate,
            customerId = est.customerId,
            kw = est.kw,
            ratePerKw = est.ratePerKw,
            pricingMode = est.pricingMode,
            gstMode = est.gstMode,
            gstPercent = est.gstPercent,
            discount = est.discount,
            taxable = est.taxable,
            cgst = est.cgst,
            sgst = est.sgst,
            total = est.total,
            amountWords = est.amountWords,
            note = est.note,
            terms = est.terms,
            status = DocStatus.UNPAID,
            parentEstimateId = estimateId
        )

        val newInvoiceId = saveDoc(invoice, items, targetBusinessId)
        db.docDao().updateStatus(estimateId, DocStatus.CONVERTED)
        newInvoiceId
    }

    suspend fun getAllEstimates(businessId: Long = 1L): List<Doc> = withContext(Dispatchers.IO) {
        db.docDao().getAllEstimates(businessId)
    }

    suspend fun getEstimatesByCustomer(customerId: Int, businessId: Long = 1L): List<Doc> = withContext(Dispatchers.IO) {
        db.docDao().getEstimatesByCustomer(customerId, businessId)
    }

    fun getAllDocsGlobalFlow(): Flow<List<Doc>> = db.docDao().getAllDocsGlobalFlow()

    suspend fun trackWhatsAppShare(docId: Int) = withContext(Dispatchers.IO) {
        db.docDao().incrementWhatsAppShareCount(docId)
    }

    // -------------------------------- Combined Flow for Documents --------------------------------
    fun getDocsWithDetailsFlow(docType: String? = null, businessId: Long = 1L): Flow<List<DocWithDetails>> {
        val docsFlow = when (docType) {
            null -> db.docDao().getAllDocsFlow(businessId)
            DocType.ESTIMATE -> db.docDao().getSolarEstimatesFlow(businessId)
            DocType.INVOICE -> db.docDao().getSolarInvoicesFlow(businessId)
            DocType.CONST_ESTIMATE, DocType.CONST_INVOICE, "CONSTRUCTION" -> db.docDao().getConstructionDocsFlow(businessId)
            else -> db.docDao().getDocsByTypeFlow(docType, businessId)
        }
        val allDocsFlow = db.docDao().getAllDocsFlow(businessId)
        val customersFlow = db.partyDao().getAllPartiesFlow(businessId)
        val paymentsFlow = db.paymentDao().getAllPaymentsFlow(businessId)

        return combine(docsFlow, allDocsFlow, customersFlow, paymentsFlow) { docs, allDocs, customers, payments ->
            val customerMap = customers.associateBy { it.id }
            val paymentMap = payments.groupBy { it.docId }
            val docMap = allDocs.associateBy { it.id }

            docs.map { doc ->
                val customer = customerMap[doc.customerId]
                val docPayments = paymentMap[doc.id] ?: emptyList()
                val totalPaid = docPayments.sumOf { it.amount }
                val parentEstNo = doc.parentEstimateId?.let { docMap[it]?.docNo }
                DocWithDetails(
                    doc = doc,
                    customer = customer,
                    payments = docPayments,
                    totalPaid = totalPaid,
                    parentEstimateNo = parentEstNo
                )
            }
        }
    }

    fun getDocWithDetailsFlow(docId: Int, businessId: Long = 1L): Flow<DocWithDetails?> {
        val docFlow = db.docDao().getDocByIdFlow(docId)
        val allDocsFlow = db.docDao().getAllDocsGlobalFlow()
        val customersFlow = db.partyDao().getAllPartiesFlow()
        val itemsFlow = db.docItemDao().getItemsForDocFlow(docId)
        val paymentsFlow = db.paymentDao().getPaymentsForDocFlow(docId)

        return combine(docFlow, allDocsFlow, customersFlow, itemsFlow, paymentsFlow) { doc, allDocs, customers, items, payments ->
            if (doc == null) return@combine null
            val customer = customers.find { it.id == doc.customerId }
            val totalPaid = payments.sumOf { it.amount }
            val parentEstNo = doc.parentEstimateId?.let { pId -> allDocs.find { it.id == pId }?.docNo }
            DocWithDetails(
                doc = doc,
                customer = customer,
                items = items,
                payments = payments,
                totalPaid = totalPaid,
                parentEstimateNo = parentEstNo
            )
        }
    }

    // -------------------------------- Payments --------------------------------
    fun getPaymentsForDocFlow(docId: Int, businessId: Long = 1L): Flow<List<Payment>> =
        db.paymentDao().getPaymentsForDocFlow(docId, businessId)

    suspend fun getPaymentsForDoc(docId: Int, businessId: Long = 1L): List<Payment> = withContext(Dispatchers.IO) {
        db.paymentDao().getPaymentsForDoc(docId, businessId)
    }

    suspend fun addPayment(payment: Payment, businessId: Long = 1L) = withContext(Dispatchers.IO) {
        db.paymentDao().insert(payment.copy(businessId = businessId))
        refreshInvoiceStatus(payment.docId)
    }

    suspend fun deletePayment(payment: Payment) = withContext(Dispatchers.IO) {
        db.paymentDao().delete(payment)
        refreshInvoiceStatus(payment.docId)
    }

    private suspend fun refreshInvoiceStatus(docId: Int) {
        val doc = db.docDao().getDocById(docId) ?: return
        if (!doc.isInvoice || doc.status == DocStatus.CANCELLED) return
        val totalPaid = db.paymentDao().getTotalPaidForDoc(docId)
        val computed = doc.computeStatus(totalPaid)
        db.docDao().updateStatus(docId, computed)
    }

    // -------------------------------- Dashboard Stats Flow --------------------------------
    fun getDashboardStatsFlow(businessId: Long = 1L): Flow<DashboardStats> {
        val invoicesFlow = db.docDao().getDocsByTypeFlow(DocType.INVOICE, businessId)
        val estimatesFlow = db.docDao().getDocsByTypeFlow(DocType.ESTIMATE, businessId)
        val customersFlow = db.partyDao().getAllPartiesFlow(businessId)
        val paymentsFlow = db.paymentDao().getAllPaymentsFlow(businessId)

        return combine(invoicesFlow, estimatesFlow, customersFlow, paymentsFlow) { invoices, estimates, customers, payments ->
            val customerMap = customers.associateBy { it.id }
            val paymentMap = payments.groupBy { it.docId }

            var totalPending = 0.0
            var totalInvoiced = 0.0

            val invoiceDetailsList = invoices.map { inv ->
                val cust = customerMap[inv.customerId]
                val pays = paymentMap[inv.id] ?: emptyList()
                val paid = pays.sumOf { it.amount }
                val detail = DocWithDetails(doc = inv, customer = cust, payments = pays, totalPaid = paid)

                if (inv.status != DocStatus.CANCELLED) {
                    totalInvoiced += inv.total
                    if (detail.pending > 0.01) {
                        totalPending += detail.pending
                    }
                }
                detail
            }

            // Month start ISO
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val monthStart = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
            val receivedMonth = payments
                .filter { it.payDate >= monthStart }
                .sumOf { it.amount }

            val openEstimates = estimates.count { it.status == DocStatus.ESTIMATE }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val dueInvoices = invoiceDetailsList
                .filter { it.doc.isInvoice && it.pending > 0.01 && it.doc.dueDate.isNotBlank() && it.doc.dueDate <= today }
                .sortedBy { it.doc.dueDate }

            val pendingEstimatesList = estimates
                .filter { it.status == DocStatus.ESTIMATE }
                .map { est -> DocWithDetails(doc = est, customer = customerMap[est.customerId]) }
                .sortedByDescending { it.doc.createdAt }

            DashboardStats(
                totalPending = totalPending,
                receivedThisMonth = receivedMonth,
                openEstimatesCount = openEstimates,
                totalInvoiced = totalInvoiced,
                dueInvoices = dueInvoices,
                pendingEstimates = pendingEstimatesList
            )
        }
    }

    // -------------------------------- Business Profiles --------------------------------
    fun getAllBusinessProfilesFlow(): Flow<List<com.example.data.entity.BusinessProfile>> =
        db.businessProfileDao().getAllActive()

    suspend fun getAllBusinessProfilesDirect(): List<com.example.data.entity.BusinessProfile> = withContext(Dispatchers.IO) {
        db.businessProfileDao().getAllActiveDirect()
    }

    fun getBusinessProfileFlow(id: Long): Flow<com.example.data.entity.BusinessProfile?> =
        db.businessProfileDao().getById(id)

    suspend fun getBusinessProfileDirect(id: Long): com.example.data.entity.BusinessProfile? = withContext(Dispatchers.IO) {
        db.businessProfileDao().getByIdDirect(id)
    }

    suspend fun saveBusinessProfile(profile: com.example.data.entity.BusinessProfile): Long = withContext(Dispatchers.IO) {
        db.businessProfileDao().insert(profile)
    }

    suspend fun updateBusinessProfile(profile: com.example.data.entity.BusinessProfile) = withContext(Dispatchers.IO) {
        db.businessProfileDao().update(profile)
    }

    suspend fun deleteBusinessProfile(id: Long) = withContext(Dispatchers.IO) {
        db.businessProfileDao().softDelete(id)
    }

    // -------------------------------- Party Statement / Ledger --------------------------------
    fun getPartyStatementFlow(partyId: Int, businessId: Long = 1L): Flow<PartyStatementData?> {
        return combine(
            db.partyDao().getPartyByIdFlow(partyId),
            db.docDao().getDocsByCustomerFlow(partyId, businessId),
            db.paymentDao().getPaymentsForCustomerFlow(partyId, businessId)
        ) { party, docs, payments ->
            if (party == null) return@combine null

            // Separate invoices vs estimates
            val invoices = docs.filter { it.isInvoice && it.status != DocStatus.CANCELLED }
            val estimates = docs.filter { !it.isInvoice && it.status != DocStatus.CANCELLED }

            // Build chronological entries
            // Event = Pair<Date, LedgerEntryDraft>
            data class EventDraft(
                val timestamp: Long,
                val date: String,
                val type: LedgerTransactionType,
                val docType: String?,
                val docId: Int?,
                val paymentId: Int?,
                val voucherNo: String,
                val particulars: String,
                val paymentMode: String?,
                val debit: Double,
                val credit: Double
            )

            val drafts = mutableListOf<EventDraft>()

            // 1. Invoices -> Debits
            for (inv in invoices) {
                val parsedTime = try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(inv.docDate)?.time ?: inv.createdAt
                } catch (_: Exception) {
                    inv.createdAt
                }
                drafts.add(
                    EventDraft(
                        timestamp = parsedTime,
                        date = inv.docDate.ifBlank { Formatters.todayIso() },
                        type = LedgerTransactionType.INVOICE,
                        docType = inv.docType,
                        docId = inv.id,
                        paymentId = null,
                        voucherNo = inv.docNo,
                        particulars = "${if (inv.isConstruction) "Civil Bill" else "Sales Invoice"} (${inv.status})",
                        paymentMode = null,
                        debit = inv.total,
                        credit = 0.0
                    )
                )
            }

            // 2. Payments -> Credits
            for (pay in payments) {
                val parsedTime = try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(pay.payDate)?.time ?: pay.createdAt
                } catch (_: Exception) {
                    pay.createdAt
                }
                val linkedDoc = docs.firstOrNull { it.id == pay.docId }
                val refPart = if (pay.reference.isNotBlank()) " [Ref: ${pay.reference}]" else ""
                val docPart = if (linkedDoc != null) " for ${linkedDoc.docNo}" else ""
                drafts.add(
                    EventDraft(
                        timestamp = parsedTime,
                        date = pay.payDate.ifBlank { Formatters.todayIso() },
                        type = LedgerTransactionType.PAYMENT_RECEIVED,
                        docType = null,
                        docId = pay.docId,
                        paymentId = pay.id,
                        voucherNo = "PAY-${pay.id.toString().padStart(4, '0')}",
                        particulars = "Payment Received (${pay.mode})$docPart$refPart",
                        paymentMode = pay.mode,
                        debit = 0.0,
                        credit = pay.amount
                    )
                )
            }

            // Sort ascending by date/time for running balance calculation
            val sortedDrafts = drafts.sortedWith(compareBy({ it.date }, { it.timestamp }))

            var running = 0.0
            var totalDebit = 0.0
            var totalCredit = 0.0

            val entries = sortedDrafts.mapIndexed { idx, d ->
                totalDebit += d.debit
                totalCredit += d.credit
                running += (d.debit - d.credit)
                val balanceType = if (running >= 0) "Dr" else "Cr"
                LedgerEntry(
                    id = "${d.type}_${d.docId ?: d.paymentId ?: idx}",
                    date = d.date,
                    type = d.type,
                    docType = d.docType,
                    docId = d.docId,
                    paymentId = d.paymentId,
                    voucherNo = d.voucherNo,
                    particulars = d.particulars,
                    paymentMode = d.paymentMode,
                    debit = d.debit,
                    credit = d.credit,
                    runningBalance = kotlin.math.abs(running),
                    balanceType = balanceType
                )
            }

            PartyStatementData(
                party = party,
                openingBalance = 0.0,
                totalDebit = totalDebit,
                totalCredit = totalCredit,
                netBalance = running,
                entries = entries.reversed(), // Latest first for display
                invoicesCount = invoices.size,
                paymentsCount = payments.size,
                fromDate = sortedDrafts.firstOrNull()?.date,
                toDate = sortedDrafts.lastOrNull()?.date
            )
        }
    }

    suspend fun recordQuickPaymentForParty(
        partyId: Int,
        amount: Double,
        mode: String,
        date: String,
        ref: String,
        note: String,
        businessId: Long = 1L
    ): Long = withContext(Dispatchers.IO) {
        // Find latest invoice with pending balance or fallback to any invoice for party
        val docs = db.docDao().getDocsByCustomerFlow(partyId, businessId).firstOrNull() ?: emptyList()
        val invoices = docs.filter { it.isInvoice && it.status != DocStatus.CANCELLED }

        // Find invoice with unpaid balance
        var targetDoc: Doc? = null
        for (inv in invoices) {
            val paid = db.paymentDao().getTotalPaidForDoc(inv.id, businessId)
            if (inv.total - paid > 0.01) {
                targetDoc = inv
                break
            }
        }
        val docId = targetDoc?.id ?: invoices.firstOrNull()?.id ?: run {
            // SAFETY: Return sentinel value instead of silently creating phantom invoices.
            // The caller (UI layer) should check for QUICK_PAYMENT_NO_INVOICE and
            // show a confirmation dialog before calling createQuickInvoiceAndPay().
            return@withContext QUICK_PAYMENT_NO_INVOICE
        }

        val payment = Payment(
            businessId = businessId,
            docId = docId,
            payDate = if (date.isNotBlank()) date else Formatters.todayIso(),
            amount = amount,
            mode = mode,
            reference = ref,
            note = note
        )
        val payId = db.paymentDao().insert(payment)

        // Check and update doc status
        val totalPaid = db.paymentDao().getTotalPaidForDoc(docId, businessId)
        val doc = db.docDao().getDocById(docId)
        if (doc != null) {
            val newStatus = when {
                totalPaid >= doc.total - 0.01 -> DocStatus.PAID
                totalPaid > 0.01 -> DocStatus.PARTIAL
                else -> doc.status
            }
            if (newStatus != doc.status) {
                db.docDao().updateStatus(docId, newStatus)
            }
        }

        payId
    }

    /**
     * Explicitly creates a quick invoice and records the payment.
     * Should ONLY be called after the user confirms via dialog.
     */
    suspend fun createQuickInvoiceAndPay(
        partyId: Int,
        amount: Double,
        mode: String,
        date: String,
        ref: String,
        note: String,
        businessId: Long = 1L
    ): Long = withContext(Dispatchers.IO) {
        val today = if (date.isNotBlank()) date else Formatters.todayIso()
        val quickDocNo = getNextDocNo(DocType.INVOICE, businessId)
        val newDoc = Doc(
            businessId = businessId,
            customerId = partyId,
            docType = DocType.INVOICE,
            docNo = quickDocNo,
            docDate = today,
            dueDate = today,
            taxable = amount,
            total = amount,
            status = DocStatus.PAID,
            note = "Quick payment invoice: $note"
        )
        val docId = db.docDao().insert(newDoc).toInt()

        val payment = Payment(
            businessId = businessId,
            docId = docId,
            payDate = today,
            amount = amount,
            mode = mode,
            reference = ref,
            note = note
        )
        db.paymentDao().insert(payment)
    }
}
