package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Company
import com.example.data.model.Customer
import com.example.data.model.Party
import com.example.data.model.Doc
import com.example.data.model.DocItem
import com.example.data.model.DocType
import com.example.data.model.DocWithDetails
import com.example.data.model.ItemMaster
import com.example.data.model.PartyStatementData
import com.example.data.model.Payment
import com.example.data.model.StarterItem
import com.example.data.repository.BillingRepository
import com.example.data.repository.DashboardStats
import com.example.utils.BackupHelper
import com.example.utils.PdfGenerator
import com.example.utils.ShareHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

import com.example.data.entity.BusinessProfile
import com.example.data.model.BusinessTypePreset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * GO-LIVE EDITION — firm scoping hardened:
 *  1. Every saved document is stamped with the CURRENTLY selected firm.
 *  2. Converting an estimate -> invoice keeps the ESTIMATE's firm
 *     (no more "created under the wrong firm").
 *  3. Linking an estimate in the invoice form auto-switches to that
 *     estimate's firm so totals/letterhead stay consistent.
 */
class BillingViewModel(application: Application) : AndroidViewModel(application) {
    val database = AppDatabase.getInstance(application)
    val repository = BillingRepository(database)

    private val prefs = application.getSharedPreferences("swami_business_prefs", Context.MODE_PRIVATE)
    private val _activeBusinessId = MutableStateFlow(prefs.getLong("active_business_id", 1L))
    val activeBusinessIdState: StateFlow<Long> = _activeBusinessId.asStateFlow()
    val activeBusinessId: Long get() = _activeBusinessId.value

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", true))
    val isOnboardingCompletedState: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllBusinessProfilesFlow().collect { list ->
                if (list.isNotEmpty()) {
                    val currentId = _activeBusinessId.value
                    if (list.none { it.id == currentId }) {
                        val fallbackId = list.first().id
                        _activeBusinessId.value = fallbackId
                        prefs.edit().putLong("active_business_id", fallbackId).apply()
                    }
                }
            }
        }
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        _isOnboardingCompleted.value = true
    }

    fun resetOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", false).apply()
        _isOnboardingCompleted.value = false
    }

    val companyState: StateFlow<Company?> = repository.getCompanyFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val businessProfilesState: StateFlow<List<BusinessProfile>> = repository.getAllBusinessProfilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeBusinessProfileState: StateFlow<BusinessProfile?> = _activeBusinessId
        .flatMapLatest { id -> repository.getBusinessProfileFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val partiesState: StateFlow<List<Party>> = _activeBusinessId
        .flatMapLatest { id -> repository.getAllPartiesFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customersState: StateFlow<List<Customer>> = partiesState

    @OptIn(ExperimentalCoroutinesApi::class)
    val estimatesState: StateFlow<List<DocWithDetails>> = _activeBusinessId
        .flatMapLatest { id -> repository.getDocsWithDetailsFlow(DocType.ESTIMATE, id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val invoicesState: StateFlow<List<DocWithDetails>> = _activeBusinessId
        .flatMapLatest { id -> repository.getDocsWithDetailsFlow(DocType.INVOICE, id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val constructionDocsState: StateFlow<List<DocWithDetails>> = _activeBusinessId
        .flatMapLatest { id -> repository.getDocsWithDetailsFlow("CONSTRUCTION", id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dashboardStatsState: StateFlow<DashboardStats> = _activeBusinessId
        .flatMapLatest { id -> repository.getDashboardStatsFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    @OptIn(ExperimentalCoroutinesApi::class)
    val itemMasterState: StateFlow<List<ItemMaster>> = _activeBusinessId
        .flatMapLatest { id -> repository.getItemMasterFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocsGlobalState: StateFlow<List<Doc>> = repository.getAllDocsGlobalFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun trackWhatsAppShare(docId: Int) {
        viewModelScope.launch { repository.trackWhatsAppShare(docId) }
    }

    fun switchBusiness(id: Long) {
        _activeBusinessId.value = id
        prefs.edit().putLong("active_business_id", id).apply()
        viewModelScope.launch {
            val profile = repository.getBusinessProfileDirect(id)
            if (profile != null) {
                val currentComp = repository.getCompany()
                val updatedComp = currentComp.copy(
                    name = profile.legalName.ifBlank { profile.brandName },
                    address = profile.address,
                    gstin = profile.gstin ?: "",
                    placeOfSupply = "${profile.state} (Code: 27)",
                    accountName = profile.legalName.ifBlank { profile.brandName },
                    accountNumber = profile.bankAccountNo ?: "",
                    ifsc = profile.ifsc ?: "",
                    signatoryName = profile.signatoryName ?: ""
                )
                repository.updateCompany(updatedComp)
            }
        }
    }

    fun saveBusinessProfile(profile: BusinessProfile, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveBusinessProfile(profile)
            _activeBusinessId.value = id
            prefs.edit().putLong("active_business_id", id).apply()

            val currentComp = repository.getCompany()
            val updatedComp = currentComp.copy(
                name = profile.legalName.ifBlank { profile.brandName },
                address = profile.address,
                gstin = profile.gstin ?: "",
                placeOfSupply = "${profile.state} (Code: 27)",
                accountName = profile.legalName.ifBlank { profile.brandName },
                accountNumber = profile.bankAccountNo ?: "",
                ifsc = profile.ifsc ?: "",
                signatoryName = profile.signatoryName ?: ""
            )
            repository.updateCompany(updatedComp)

            onResult(id)
            triggerBackgroundAutoBackup()
        }
    }

    fun updateDocBusiness(docId: Int, businessId: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateDocBusinessId(docId, businessId)
            onDone()
        }
    }

    fun deleteBusinessProfile(id: Long, onResult: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteBusinessProfile(id)
            val allActive = repository.getAllBusinessProfilesDirect()
            if (activeBusinessId == id) {
                val nextId = allActive.firstOrNull()?.id ?: 1L
                _activeBusinessId.value = nextId
                prefs.edit().putLong("active_business_id", nextId).apply()
            }
            onResult()
            triggerBackgroundAutoBackup()
        }
    }

    fun saveItem(item: ItemMaster, onResult: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveItem(item.copy(businessId = activeBusinessId))
            onResult()
            triggerBackgroundAutoBackup()
        }
    }

    fun deleteItem(item: ItemMaster, onResult: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteItem(item)
            onResult()
            triggerBackgroundAutoBackup()
        }
    }

    private val _selectedDocId = MutableStateFlow<Int?>(null)
    val selectedDocId: StateFlow<Int?> = _selectedDocId.asStateFlow()

    private val _currentDocDetails = MutableStateFlow<DocWithDetails?>(null)
    val currentDocDetails: StateFlow<DocWithDetails?> = _currentDocDetails.asStateFlow()

    private var selectDocJob: kotlinx.coroutines.Job? = null

    fun getDocDetailFlow(docId: Int): kotlinx.coroutines.flow.Flow<DocWithDetails?> {
        return repository.getDocWithDetailsFlow(docId)
    }

    fun selectDoc(docId: Int) {
        _selectedDocId.value = docId
        selectDocJob?.cancel()
        selectDocJob = viewModelScope.launch {
            repository.getDocWithDetailsFlow(docId).collect { details ->
                _currentDocDetails.value = details
            }
        }
    }

    private fun triggerBackgroundAutoBackup() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try { BackupHelper.triggerAutoBackup(getApplication(), database) } catch (_: Exception) {}
        }
    }

    fun saveParty(party: Party, onResult: (Long) -> Unit) {
        viewModelScope.launch {
            val targetBizId = if (party.businessId > 0L) party.businessId else activeBusinessId
            val id = repository.saveParty(party.copy(businessId = targetBizId))
            onResult(id)
            triggerBackgroundAutoBackup()
        }
    }

    fun deleteParty(party: Party, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val deleted = repository.deleteParty(party, activeBusinessId)
            onResult(deleted)
            triggerBackgroundAutoBackup()
        }
    }

    fun saveCustomer(customer: Customer, onResult: (Long) -> Unit) = saveParty(customer, onResult)
    fun deleteCustomer(customer: Customer, onResult: (Boolean) -> Unit) = deleteParty(customer, onResult)

    /**
     * FIX: stamp every saved document with the currently active firm,
     * regardless of what the form passed in.
     */
    fun saveDoc(doc: Doc, items: List<DocItem>, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val stamped = if (doc.businessId > 0L) doc else doc.copy(businessId = activeBusinessId)
            val id = repository.saveDoc(stamped, items)
            onResult(id)
            triggerBackgroundAutoBackup()
        }
    }

    fun deleteDoc(doc: Doc, onResult: () -> Unit) {
        viewModelScope.launch {
            repository.deleteDoc(doc)
            onResult()
            triggerBackgroundAutoBackup()
        }
    }

    fun updateDocStatus(docId: Int, status: String) {
        viewModelScope.launch {
            repository.updateDocStatus(docId, status)
            triggerBackgroundAutoBackup()
        }
    }

    fun updateDueDate(docId: Int, dueDate: String) {
        viewModelScope.launch {
            repository.updateDueDate(docId, dueDate)
            triggerBackgroundAutoBackup()
        }
    }

    fun updateDocTerms(docId: Int, terms: String, onResult: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateDocTerms(docId, terms)
            onResult()
            triggerBackgroundAutoBackup()
        }
    }

    /**
     * FIX: converted invoices inherit the ESTIMATE's firm, not whatever
     * firm happens to be active at tap time.
     */
    fun convertToInvoice(estimateId: Int, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val estimate = repository.getDocById(estimateId)
            val newInvoiceId = repository.convertToInvoice(estimateId)
            val targetFirm = estimate?.businessId?.takeIf { it > 0L } ?: activeBusinessId
            repository.updateDocBusinessId(newInvoiceId, targetFirm)
            onResult(newInvoiceId)
            triggerBackgroundAutoBackup()
        }
    }

    fun addPayment(payment: Payment, onResult: () -> Unit) {
        viewModelScope.launch {
            repository.addPayment(payment)
            onResult()
            triggerBackgroundAutoBackup()
        }
    }

    fun deletePayment(payment: Payment, onResult: () -> Unit) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            onResult()
            triggerBackgroundAutoBackup()
        }
    }

    fun saveItemMaster(items: List<ItemMaster>, onResult: () -> Unit) {
        viewModelScope.launch {
            repository.saveItemMaster(items)
            onResult()
            triggerBackgroundAutoBackup()
        }
    }

    fun setSetting(key: String, value: String) {
        viewModelScope.launch { repository.setSetting(key, value) }
    }

    fun generateAndSharePdf(
        context: Context,
        detail: DocWithDetails,
        language: ShareHelper.MessageLanguage = ShareHelper.MessageLanguage.ENGLISH,
        businessOverride: BusinessProfile? = null
    ) {
        viewModelScope.launch {
            val profile = businessOverride
                ?: repository.getBusinessProfileDirect(detail.doc.businessId)
                ?: repository.getBusinessProfileDirect(activeBusinessId)
            val comp = repository.getCompany()
            val targetBusinessId = profile?.id ?: activeBusinessId
            val themeStr = repository.getSetting("pdf_theme", "MODERN_COLOR", targetBusinessId)
            val pdfFile = PdfGenerator.generatePdf(context, detail, comp, business = profile, themeStr = themeStr)
            repository.updatePdfPath(detail.doc.id, pdfFile.absolutePath)

            val type = if (detail.doc.isInvoice) ShareHelper.TemplateType.INVOICE else ShareHelper.TemplateType.ESTIMATE
            val settingKey = ShareHelper.getSettingKey(type, language)
            val fallback = ShareHelper.getDefaultTemplate(type, language)
            val template = repository.getSetting(settingKey, fallback, targetBusinessId)
            val shareMsg = ShareHelper.formatTemplateMessage(template, detail)

            trackWhatsAppShare(detail.doc.id)
            ShareHelper.sharePdf(context, pdfFile, shareMsg)
        }
    }

    fun previewPdf(context: Context, detail: DocWithDetails, businessOverride: BusinessProfile? = null) {
        viewModelScope.launch {
            val profile = businessOverride
                ?: repository.getBusinessProfileDirect(detail.doc.businessId)
                ?: repository.getBusinessProfileDirect(activeBusinessId)
            val comp = repository.getCompany()
            val targetBusinessId = profile?.id ?: activeBusinessId
            val themeStr = repository.getSetting("pdf_theme", "MODERN_COLOR", targetBusinessId)
            val pdfFile = PdfGenerator.generatePdf(context, detail, comp, business = profile, themeStr = themeStr)
            repository.updatePdfPath(detail.doc.id, pdfFile.absolutePath)
            ShareHelper.viewPdf(context, pdfFile)
        }
    }

    fun previewReceiptPdf(context: Context, detail: DocWithDetails, payment: Payment, businessOverride: BusinessProfile? = null) {
        viewModelScope.launch {
            val profile = businessOverride
                ?: repository.getBusinessProfileDirect(detail.doc.businessId)
                ?: repository.getBusinessProfileDirect(activeBusinessId)
            val comp = repository.getCompany()
            val targetBusinessId = profile?.id ?: activeBusinessId
            val themeStr = repository.getSetting("pdf_theme", "MODERN_COLOR", targetBusinessId)
            val pdfFile = PdfGenerator.generateReceiptPdf(context, detail, payment, comp, business = profile, themeStr = themeStr)
            ShareHelper.viewPdf(context, pdfFile)
        }
    }

    fun shareReceiptPdf(context: Context, detail: DocWithDetails, payment: Payment, viaWhatsApp: Boolean = false, businessOverride: BusinessProfile? = null) {
        viewModelScope.launch {
            val profile = businessOverride
                ?: repository.getBusinessProfileDirect(detail.doc.businessId)
                ?: repository.getBusinessProfileDirect(activeBusinessId)
            val comp = repository.getCompany()
            val targetBusinessId = profile?.id ?: activeBusinessId
            val themeStr = repository.getSetting("pdf_theme", "MODERN_COLOR", targetBusinessId)
            val pdfFile = PdfGenerator.generateReceiptPdf(context, detail, payment, comp, business = profile, themeStr = themeStr)
            val msg = ShareHelper.formatTemplateMessage(ShareHelper.DEFAULT_MSG_RECEIPT, detail, payment.amount)
            ShareHelper.sharePdf(context, pdfFile, msg, targetWhatsAppDirectly = viaWhatsApp)
        }
    }

    fun importStarterCatalog(businessTypeKey: String, replaceExisting: Boolean = false, onResult: () -> Unit = {}) {
        viewModelScope.launch {
            repository.importStarterCatalogForBusiness(activeBusinessId, businessTypeKey, replaceExisting)
            onResult()
            triggerBackgroundAutoBackup()
        }
    }

    fun importCatalogPreset(preset: BusinessTypePreset, onResult: (Int) -> Unit = {}) {
        viewModelScope.launch {
            repository.importStarterCatalogForBusiness(activeBusinessId, preset.id, false)
            onResult(preset.starterItems.size)
            triggerBackgroundAutoBackup()
        }
    }

    fun importSinglePresetItem(item: StarterItem, presetId: String, onResult: () -> Unit = {}) {
        viewModelScope.launch {
            repository.importStarterItem(activeBusinessId, item, presetId)
            onResult()
            triggerBackgroundAutoBackup()
        }
    }

    fun getPartyStatementFlow(partyId: Int): Flow<PartyStatementData?> {
        return repository.getPartyStatementFlow(partyId, activeBusinessId)
    }

    fun recordQuickPaymentForParty(
        partyId: Int, amount: Double, mode: String, date: String, ref: String, note: String,
        onResult: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val payId = repository.recordQuickPaymentForParty(partyId, amount, mode, date, ref, note, activeBusinessId)
            onResult(payId)
            triggerBackgroundAutoBackup()
        }
    }

    fun generateAndSharePartyStatementPdf(context: Context, statement: PartyStatementData) {
        viewModelScope.launch {
            val comp = repository.getCompany()
            val activeProfile = repository.getBusinessProfileDirect(activeBusinessId)
            val themeStr = repository.getSetting("pdf_theme", "MODERN_COLOR", activeBusinessId)
            val pdfFile = PdfGenerator.generatePartyStatementPdf(context, statement, activeProfile, comp, themeStr)
            val shareMsg = buildString {
                appendLine("📄 *PARTY STATEMENT / ACCOUNT LEDGER*")
                appendLine("Firm: *${activeProfile?.brandName ?: comp.name}*")
                appendLine("Party: *${statement.party.name}*")
                appendLine("Total Billed: *₹ ${com.example.utils.Formatters.formatInr(statement.totalDebit)}*")
                appendLine("Total Received: *₹ ${com.example.utils.Formatters.formatInr(statement.totalCredit)}*")
                if (statement.isReceivable) {
                    appendLine("Net Balance Due: *₹ ${com.example.utils.Formatters.formatInr(statement.netBalance)} Dr (Pending)*")
                } else if (statement.isPayable) {
                    appendLine("Net Balance: *₹ ${com.example.utils.Formatters.formatInr(kotlin.math.abs(statement.netBalance))} Cr (Advance)*")
                } else {
                    appendLine("Net Balance: *₹ 0.00 (All Settled)*")
                }
                appendLine("\nPlease find attached detailed statement PDF.")
            }
            ShareHelper.sharePdf(context, pdfFile, shareMsg)
        }
    }

    fun previewPartyStatementPdf(context: Context, statement: PartyStatementData) {
        viewModelScope.launch {
            val comp = repository.getCompany()
            val activeProfile = repository.getBusinessProfileDirect(activeBusinessId)
            val themeStr = repository.getSetting("pdf_theme", "MODERN_COLOR", activeBusinessId)
            val pdfFile = PdfGenerator.generatePartyStatementPdf(context, statement, activeProfile, comp, themeStr)
            ShareHelper.viewPdf(context, pdfFile)
        }
    }

    fun createBackup(context: Context, onResult: (File?) -> Unit) {
        viewModelScope.launch {
            try { onResult(BackupHelper.createBackup(context, database)) }
            catch (e: Exception) { e.printStackTrace(); onResult(null) }
        }
    }

    fun triggerAutoBackup(onResult: (File?) -> Unit = {}) {
        viewModelScope.launch { onResult(BackupHelper.triggerAutoBackup(getApplication(), database)) }
    }

    fun listBackupSnapshots(): List<com.example.utils.BackupSnapshot> = BackupHelper.listSnapshots(getApplication())

    fun restoreSnapshot(file: File, onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(BackupHelper.restoreFromFile(database, file)) }
    }

    fun savePdfPath(docId: Int, path: String) {
        viewModelScope.launch { repository.updatePdfPath(docId, path) }
    }

    fun restoreFromFile(file: File, onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(BackupHelper.restoreFromFile(database, file)) }
    }

    fun restoreBackup(jsonString: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try { onResult(BackupHelper.restoreBackup(database, jsonString)) }
            catch (e: Exception) { onResult("Restore failed: ${e.localizedMessage}") }
        }
    }

    fun exportLedgerToJson(context: Context, onResult: (File?) -> Unit) {
        viewModelScope.launch {
            try { onResult(BackupHelper.exportLedgerToJson(context, database, activeBusinessId)) }
            catch (e: Exception) { e.printStackTrace(); onResult(null) }
        }
    }

    fun exportLedgerToCsv(context: Context, onResult: (File?) -> Unit) {
        viewModelScope.launch {
            try { onResult(BackupHelper.exportLedgerToCsv(context, database, activeBusinessId)) }
            catch (e: Exception) { e.printStackTrace(); onResult(null) }
        }
    }

    fun exportInvoicesToCsv(context: Context, onResult: (File?) -> Unit) {
        viewModelScope.launch {
            try { onResult(BackupHelper.exportInvoicesToCsv(context, database, activeBusinessId)) }
            catch (e: Exception) { e.printStackTrace(); onResult(null) }
        }
    }

    fun exportCustomersToCsv(context: Context, onResult: (File?) -> Unit) {
        viewModelScope.launch {
            try { onResult(BackupHelper.exportCustomersToCsv(context, database, activeBusinessId)) }
            catch (e: Exception) { e.printStackTrace(); onResult(null) }
        }
    }

    fun exportTallyXml(context: Context, monthYear: String = "", onResult: (java.io.File?) -> Unit) {
        viewModelScope.launch {
            try {
                val xml = com.example.utils.TallyExportHelper.generateTallyXml(database, activeBusinessId, monthYear)
                val exportDir = java.io.File(context.filesDir, "Exports").apply { mkdirs() }
                val stamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                val xmlFile = java.io.File(exportDir, "Tally_Sales_$stamp.xml")
                java.io.FileOutputStream(xmlFile).use { out ->
                    out.write(xml.toByteArray(Charsets.UTF_8))
                }
                onResult(xmlFile)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }
}
