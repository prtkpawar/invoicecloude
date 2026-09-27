package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BusinessProfile
import com.example.data.model.BusinessTerminologyRegistry
import com.example.data.model.Customer
import com.example.data.model.Doc
import com.example.data.model.DocItem
import com.example.data.model.DocStatus
import com.example.data.model.DocType
import com.example.data.model.DocWithDetails
import com.example.data.model.Payment
import com.example.ui.components.BusinessSwitcherDialog
import com.example.ui.components.CollapsibleSection
import com.example.ui.components.CompactFieldRow
import com.example.ui.components.CompactItemRow
import androidx.compose.material3.ModalBottomSheet
import com.example.ui.components.CatalogPicker
import com.example.ui.components.CompactTextField
import com.example.ui.components.CustomerDialog
import com.example.ui.components.ItemCatalogBottomSheet
import com.example.ui.components.KitSelectorRow
import com.example.ui.components.TermsEditor
import com.example.ui.components.WizardBottomBar
import com.example.ui.components.WizardStepIndicator
import com.example.data.model.FirmFormConfig
import com.example.data.model.FirmFormConfigRegistry
import com.example.data.model.resolveTemplates
import com.example.ui.theme.pro.Brand100
import com.example.ui.theme.pro.Brand600
import com.example.ui.theme.pro.Canvas
import com.example.ui.theme.pro.Danger600
import com.example.ui.theme.pro.Ink400
import com.example.ui.theme.pro.Ink600
import com.example.ui.theme.pro.Ink900
import com.example.ui.theme.pro.Line
import com.example.ui.theme.pro.Spacing
import com.example.ui.theme.pro.Success600
import com.example.ui.theme.pro.Surface
import com.example.ui.theme.pro.SurfaceSoft
import com.example.ui.viewmodel.BillingViewModel
import com.example.utils.Calc
import com.example.utils.Formatters
import com.example.utils.NumWords
import com.example.utils.Totals
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 3-Step Wizard for creating and editing Tax Invoices, Estimates & BOQ Quotes.
 * Replaces monolithic 3,000+ line form with a clean, compact 3-step workflow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocFormScreen(
    docType: String,
    editDocId: Int? = null,
    viewModel: BillingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isInvoice = docType == DocType.INVOICE
    val isEdit = editDocId != null

    // Multi-firm state
    val businessProfiles by viewModel.businessProfilesState.collectAsState()
    val activeGlobalBusinessId by viewModel.activeBusinessIdState.collectAsState()
    val activeProfile by viewModel.activeBusinessProfileState.collectAsState()

    var formBusinessId by remember { mutableLongStateOf(activeGlobalBusinessId) }
    val currentFirm = remember(formBusinessId, businessProfiles, activeProfile) {
        businessProfiles.find { it.id == formBusinessId } ?: activeProfile
    }
    val terminology = remember(currentFirm) {
        BusinessTerminologyRegistry.getForProfile(currentFirm)
    }

    // ── Firm-specific form configuration ──
    val formConfig: FirmFormConfig = remember(currentFirm) {
        FirmFormConfigRegistry.getConfig(currentFirm?.category ?: "KIRANA")
    }
    // Selected kit index for Solar and similar capacity-based firms (-1 = custom/none)
    var selectedKitIndex by rememberSaveable { mutableIntStateOf(0) }

    // Step state (0: Customer, 1: Items, 2: Review)
    var currentStep by rememberSaveable { mutableIntStateOf(0) }

    // Dialog flags
    var showBusinessSwitcherInDoc by remember { mutableStateOf(false) }
    var showCatalogPicker by remember { mutableStateOf(false) }
    var showNewCustomerDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Customer & Header Data
    val customers by viewModel.customersState.collectAsState()
    val estimates by viewModel.estimatesState.collectAsState()
    val itemMasters by viewModel.itemMasterState.collectAsState()

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerSearchQuery by rememberSaveable { mutableStateOf("") }
    var docNo by rememberSaveable { mutableStateOf("") }
    var docDate by rememberSaveable { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var dueDate by rememberSaveable { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() + 15L * 86400000L))) }
    var validTillDate by rememberSaveable { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() + 30L * 86400000L))) }

    // Line items & Taxes
    val itemsList = remember { mutableStateListOf<DocItem>() }
    var gstMode by rememberSaveable { mutableStateOf("GST_5") } // NONE, GST_5, GST_12, GST_18, GST_28, CUSTOM
    var customGstText by rememberSaveable { mutableStateOf("5") }
    var discountText by rememberSaveable { mutableStateOf("0") }
    var installationText by rememberSaveable { mutableStateOf("0") }

    // Optional Collapsible Details
    var isTransportExpanded by rememberSaveable { mutableStateOf(false) }
    var vehicleNo by rememberSaveable { mutableStateOf("") }
    var transporterName by rememberSaveable { mutableStateOf("") }
    var ewayBillNo by rememberSaveable { mutableStateOf("") }
    var deliveryAddress by rememberSaveable { mutableStateOf("") }

    var isBankExpanded by rememberSaveable { mutableStateOf(false) }
    var bankName by rememberSaveable { mutableStateOf("") }
    var accountNo by rememberSaveable { mutableStateOf("") }
    var ifscCode by rememberSaveable { mutableStateOf("") }

    var isTermsExpanded by rememberSaveable { mutableStateOf(false) }
    var termsText by rememberSaveable { mutableStateOf("") }

    var isNotesExpanded by rememberSaveable { mutableStateOf(false) }
    var noteText by rememberSaveable { mutableStateOf("") }

    // Payment collection (for invoice)
    var paidAmountText by rememberSaveable { mutableStateOf("") }
    var paymentMode by rememberSaveable { mutableStateOf("UPI") }

    val isDirty = selectedCustomer != null || 
        itemsList.isNotEmpty() || 
        noteText.isNotEmpty() || 
        termsText.isNotEmpty() ||
        (docNo.isNotEmpty() && !isEdit)

    BackHandler(enabled = isDirty && !showDiscardDialog) {
        showDiscardDialog = true
    }

    // Capacity & Pricing fields (Dedicated Swami Solar engine)
    val isFirmSolar = remember(currentFirm, terminology) {
        terminology.isCapacityBased ||
        currentFirm?.category == "SOLAR" ||
        currentFirm?.legalName?.contains("Solar", ignoreCase = true) == true ||
        currentFirm?.brandName?.contains("Solar", ignoreCase = true) == true
    }
    var isSolarModeActive by rememberSaveable { mutableStateOf(isFirmSolar) }
    var pricingMode by rememberSaveable { mutableStateOf(Calc.MODE_FORWARD) }
    var kwText by rememberSaveable { mutableStateOf("3") }
    var rateText by rememberSaveable { mutableStateOf("50000") }
    var roundTotalText by rememberSaveable { mutableStateOf("150000") }
    var isSolarBoqExpanded by rememberSaveable { mutableStateOf(false) }

    // Auto-generates standard Solar equipment specifications (BoQ) for given capacity
    fun generateStandardSolarBoq(kw: Double) {
        val panelWatt = 540
        val panelCount = ((kw * 1000) / panelWatt).toInt().coerceAtLeast(1)
        val kwFmt = if (kw % 1.0 == 0.0) kw.toInt().toString() else String.format(Locale.US, "%.1f", kw)
        itemsList.clear()
        itemsList.add(
            DocItem(
                label = "1",
                description = "Solar PV Modules ($panelWatt W Mono PERC Half-Cut DCR, ALMM Approved)",
                hsn = "85414011",
                qty = panelCount.toDouble(),
                unit = "NOS",
                rate = 0.0,
                amount = 0.0,
                sortOrder = 0
            )
        )
        itemsList.add(
            DocItem(
                label = "2",
                description = "Solar Grid-Tie Inverter ($kwFmt kW On-Grid with WiFi Monitoring)",
                hsn = "85044090",
                qty = 1.0,
                unit = "SET",
                rate = 0.0,
                amount = 0.0,
                sortOrder = 1
            )
        )
        itemsList.add(
            DocItem(
                label = "3",
                description = "Module Mounting Structure (Hot Dip Galvanized Iron - High Wind Tolerant)",
                hsn = "73089090",
                qty = 1.0,
                unit = "SET",
                rate = 0.0,
                amount = 0.0,
                sortOrder = 2
            )
        )
        itemsList.add(
            DocItem(
                label = "4",
                description = "ACDB & DCDB Distribution Protection Enclosures with Type II SPD & MCBs",
                hsn = "85371000",
                qty = 1.0,
                unit = "SET",
                rate = 0.0,
                amount = 0.0,
                sortOrder = 3
            )
        )
        itemsList.add(
            DocItem(
                label = "5",
                description = "Chemical Earthing Electrodes (3 Sets: AC, DC, LA) & Lightning Arrester",
                hsn = "85359090",
                qty = 3.0,
                unit = "SET",
                rate = 0.0,
                amount = 0.0,
                sortOrder = 4
            )
        )
        itemsList.add(
            DocItem(
                label = "6",
                description = "Solar DC UV-Resistant Copper Cables (4/6 sq.mm) & AC Armoured Cables",
                hsn = "85444999",
                qty = 1.0,
                unit = "LOT",
                rate = 0.0,
                amount = 0.0,
                sortOrder = 5
            )
        )
        itemsList.add(
            DocItem(
                label = "7",
                description = "Discom Net-Metering Liaisoning, Inspection, Testing & Grid Commissioning",
                hsn = "998719",
                qty = 1.0,
                unit = "JOB",
                rate = 0.0,
                amount = 0.0,
                sortOrder = 6
            )
        )
    }

    // Helper: configure form defaults for given firm
    fun applyFirmDefaults(profile: BusinessProfile) {
        formBusinessId = profile.id
        viewModel.switchBusiness(profile.id)
        val term = BusinessTerminologyRegistry.getForProfile(profile)
        val config = FirmFormConfigRegistry.getConfig(profile.category)
        val isSolar = config.capacityBased
        isSolarModeActive = isSolar

        if (!isSolar) {
            kwText = "0"
            rateText = "0"
            selectedKitIndex = -1
            val hasSolar = itemsList.any {
                it.description.contains("Module", ignoreCase = true) ||
                it.description.contains("Inverter", ignoreCase = true) ||
                it.description.contains("Panel", ignoreCase = true)
            }
            if (itemsList.isEmpty() || hasSolar) {
                itemsList.clear()
            }
        } else {
            // Use the first kit from config as default
            selectedKitIndex = 0
            val defaultKit = config.kits.firstOrNull()
            if (defaultKit != null) {
                kwText = if (defaultKit.capacityKw % 1.0 == 0.0) 
                    defaultKit.capacityKw.toInt().toString() 
                    else defaultKit.capacityKw.toString()
                rateText = defaultKit.defaultRatePerUnit.toInt().toString()
                val total = defaultKit.capacityKw * defaultKit.defaultRatePerUnit
                roundTotalText = total.toInt().toString()
            } else {
                kwText = "3"
                rateText = "50000"
                roundTotalText = "150000"
            }
            pricingMode = Calc.MODE_FORWARD
            // Generate items from kit template
            generateStandardSolarBoq(defaultKit?.capacityKw ?: 3.0)
        }

        // GST from config default
        val defaultGst = config.gstPresets.find { it.isDefault }
        if (defaultGst != null) {
            when (defaultGst.rate) {
                0.0 -> { gstMode = "NONE"; customGstText = "0" }
                5.0 -> { gstMode = "GST_5"; customGstText = "5" }
                12.0 -> { gstMode = "GST_12"; customGstText = "12" }
                18.0 -> { gstMode = "GST_18"; customGstText = "18" }
                28.0 -> { gstMode = "GST_28"; customGstText = "28" }
                else -> { gstMode = "CUSTOM"; customGstText = defaultGst.rate.toInt().toString() }
            }
        } else {
            when (term.defaultTaxRate) {
                0.0 -> { gstMode = "NONE"; customGstText = "0" }
                5.0 -> { gstMode = "GST_5"; customGstText = "5" }
                12.0 -> { gstMode = "GST_12"; customGstText = "12" }
                18.0 -> { gstMode = "GST_18"; customGstText = "18" }
                28.0 -> { gstMode = "GST_28"; customGstText = "28" }
                else -> { gstMode = "CUSTOM"; customGstText = term.defaultTaxRate.toInt().toString() }
            }
        }

        // Terms & Notes from config (firm-specific)
        noteText = if (isInvoice) config.defaultInvoiceNote else config.defaultEstimateNote
        termsText = if (config.defaultTerms.isNotEmpty()) {
            config.defaultTerms.mapIndexed { i, t -> "${i + 1}. $t" }.joinToString("\n")
        } else {
            if (isInvoice) term.defaultInvoiceTerms else term.defaultEstimateTerms
        }

        // Fill bank fields from profile
        bankName = profile.legalName
        accountNo = profile.bankAccountNo ?: ""
        ifscCode = profile.ifsc ?: ""
    }

    // Load initial document data
    LaunchedEffect(editDocId) {
        if (isEdit) {
            val existing = viewModel.repository.getDocById(editDocId!!)
            if (existing != null) {
                if (existing.businessId > 0L) {
                    formBusinessId = existing.businessId
                    viewModel.switchBusiness(existing.businessId)
                }
                docNo = existing.docNo
                docDate = existing.docDate
                dueDate = existing.dueDate
                validTillDate = existing.validTill
                selectedCustomer = viewModel.repository.getCustomerById(existing.customerId)
                gstMode = existing.gstMode
                if (existing.gstMode == "CUSTOM") customGstText = existing.gstPercent.toString()
                discountText = if (existing.discount > 0) existing.discount.toInt().toString() else "0"
                noteText = existing.note
                termsText = existing.terms

                val existingItems = viewModel.repository.getItemsForDoc(existing.id)
                itemsList.clear()
                itemsList.addAll(existingItems)

                if (existing.kw > 0.0) {
                    kwText = if (existing.kw % 1.0 == 0.0) existing.kw.toInt().toString() else existing.kw.toString()
                    isSolarModeActive = true
                }
                if (existing.ratePerKw > 0.0) {
                    rateText = existing.ratePerKw.toInt().toString()
                }
                if (existing.pricingMode.isNotBlank()) {
                    pricingMode = existing.pricingMode
                }
                roundTotalText = existing.total.toInt().toString()

                val existingPayments = viewModel.repository.getPaymentsForDoc(existing.id)
                if (existingPayments.isNotEmpty()) {
                    val p = existingPayments.first()
                    paidAmountText = p.amount.toInt().toString()
                    paymentMode = p.mode
                }
            }
        } else {
            val initialFirm = businessProfiles.find { it.id == formBusinessId } ?: activeProfile
            if (initialFirm != null) {
                applyFirmDefaults(initialFirm)
            } else if (isFirmSolar) {
                isSolarModeActive = true
                kwText = "3"
                rateText = "50000"
                roundTotalText = "150000"
                pricingMode = Calc.MODE_FORWARD
                gstMode = "GST_5"
                customGstText = "5"
                if (itemsList.isEmpty()) {
                    generateStandardSolarBoq(3.0)
                }
            }
            docNo = viewModel.repository.getNextDocNo(docType, formBusinessId)
        }
    }

    // Tax & Totals calculation
    val gstPercent = when (gstMode) {
        "NONE" -> 0.0
        "GST_5" -> 5.0
        "GST_12" -> 12.0
        "GST_18" -> 18.0
        "GST_28" -> 28.0
        else -> customGstText.toDoubleOrNull() ?: 0.0
    }
    val discountVal = discountText.toDoubleOrNull() ?: 0.0
    val installationVal = installationText.toDoubleOrNull() ?: 0.0
    val kwVal = kwText.toDoubleOrNull() ?: 0.0
    LaunchedEffect(kwVal) {
        if (kwVal > 0) {
            generateStandardSolarBoq(kwVal)
        }
    }
    val rateVal = rateText.toDoubleOrNull() ?: 0.0
    val roundTotalVal = roundTotalText.toDoubleOrNull() ?: 0.0

    val itemsSubtotal = remember(itemsList.toList()) {
        itemsList.sumOf { it.qty * it.rate }
    }

    val totals: Totals = remember(isSolarModeActive, pricingMode, gstPercent, kwVal, rateVal, roundTotalVal, discountVal, itemsSubtotal) {
        if (isSolarModeActive && kwVal > 0) {
            Calc.compute(
                pricingMode = pricingMode,
                gstPercent = gstPercent,
                kw = kwVal,
                ratePerKw = rateVal,
                roundTotal = roundTotalVal,
                discount = discountVal,
                installationCharges = installationVal
            )
        } else {
            Calc.computeFromItems(
                itemsTotal = itemsSubtotal,
                gstPercent = gstPercent,
                discount = discountVal
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEdit) "Edit ${if (isInvoice) "Invoice" else "Estimate"}" else "New ${if (isInvoice) "Tax Invoice" else "Estimate / Quote"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Ink900
                        )
                        Text(
                            text = "${currentFirm?.brandName ?: "Active Firm"} • Step ${currentStep + 1} of 3",
                            fontSize = 11.5.sp,
                            color = Ink600
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { if (isDirty) showDiscardDialog = true else onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink900)
                    }
                },
                actions = {
                    Button(
                        onClick = { showBusinessSwitcherInDoc = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand600, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .height(32.dp)
                    ) {
                        Text("Firm ▾", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Canvas)
            )
        },
        bottomBar = {
            val onSaveAction = {
                if (selectedCustomer == null) {
                    currentStep = 0
                    Toast.makeText(context, "Please select a customer first", Toast.LENGTH_SHORT).show()
                } else if (totals.total <= 0.0) {
                    currentStep = 1
                    Toast.makeText(context, if (isSolarModeActive) "Please enter valid kW capacity and rate" else "Please add items to bill", Toast.LENGTH_SHORT).show()
                } else {
                    val fullNote = listOfNotNull(
                        vehicleNo.takeIf { it.isNotBlank() }?.let { "Vehicle: $it" },
                        ewayBillNo.takeIf { it.isNotBlank() }?.let { "E-Way: $it" },
                        deliveryAddress.takeIf { it.isNotBlank() }?.let { "Ship to: $it" },
                        noteText.trim().takeIf { it.isNotBlank() }
                    ).joinToString(" | ")

                    val effectiveRatePerKw = if (isSolarModeActive) {
                        if (pricingMode == Calc.MODE_FORWARD) rateVal else (if (kwVal > 0) roundTotalVal / kwVal else 0.0)
                    } else itemsSubtotal

                    val docToSave = Doc(
                        id = editDocId ?: 0,
                        businessId = formBusinessId,
                        docType = docType,
                        docNo = docNo.ifBlank { "DOC-${System.currentTimeMillis().toString().takeLast(6)}" },
                        docDate = docDate,
                        validTill = if (isInvoice) "" else validTillDate,
                        dueDate = if (isInvoice) dueDate else "",
                        customerId = selectedCustomer!!.id,
                        kw = if (isSolarModeActive) kwVal else 0.0,
                        ratePerKw = effectiveRatePerKw,
                        pricingMode = if (isSolarModeActive) pricingMode else "ITEMIZED",
                        gstMode = gstMode,
                        gstPercent = gstPercent,
                        discount = discountVal,
                        taxable = totals.taxable,
                        cgst = totals.cgst,
                        sgst = totals.sgst,
                        total = totals.total,
                        amountWords = NumWords.rupees(totals.total),
                        note = fullNote,
                        terms = termsText.trim(),
                        status = if (isEdit) DocStatus.ESTIMATE else if (isInvoice) DocStatus.UNPAID else DocStatus.ESTIMATE
                    )

                    viewModel.saveDoc(docToSave, itemsList) { savedId ->
                        val paidVal = paidAmountText.toDoubleOrNull() ?: 0.0
                        if (isInvoice && paidVal > 0.0) {
                            val initialPayment = Payment(
                                docId = savedId,
                                businessId = formBusinessId,
                                amount = paidVal,
                                payDate = docDate,
                                mode = paymentMode,
                                reference = "Initial Payment"
                            )
                            viewModel.addPayment(initialPayment) {}
                        }
                        Toast.makeText(context, "Document Saved Successfully!", Toast.LENGTH_SHORT).show()
                        onNavigateToDetail(savedId)
                    }
                }
            }

            WizardBottomBar(
                currentStep = currentStep,
                totalSteps = 3,
                onBack = { if (currentStep > 0) currentStep-- else if (isDirty) showDiscardDialog = true else onNavigateBack() },
                onNext = {
                    if (currentStep == 0 && selectedCustomer == null) {
                        Toast.makeText(context, "Please select or add a ${terminology.partyLabel.lowercase()}", Toast.LENGTH_SHORT).show()
                    } else if (currentStep == 1 && totals.total <= 0.0) {
                        Toast.makeText(context, if (isSolarModeActive) "Please enter valid kW capacity and rate" else "Please add items to calculate amount", Toast.LENGTH_SHORT).show()
                    } else {
                        currentStep++
                    }
                },
                onSave = onSaveAction,
                nextLabel = if (currentStep == 0) "Next: Items (${if (isSolarModeActive) "Capacity" else itemsList.size.toString()}) →" else "Next: Review Summary →"
            )
        },
        containerColor = Canvas
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // Top Billing Firm Letterhead Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Line),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showBusinessSwitcherInDoc = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Brand600,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = terminology.iconEmoji.ifBlank { "🏢" },
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = currentFirm?.brandName?.ifBlank { currentFirm?.legalName } ?: "Active Firm",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Ink900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${terminology.title} • GST: ${currentFirm?.gstin ?: "Unregistered"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ink600,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    Text(
                        "Change ▾",
                        color = Brand600,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            // Step Indicator Header
            WizardStepIndicator(
                currentStep = currentStep,
                stepTitles = listOf("1. ${terminology.partyLabel}", "2. ${terminology.inventoryLabel}", "3. Review & Save"),
                onStepClick = { currentStep = it }
            )

            // Step Content
            when (currentStep) {
                // ==================== STEP 0: CUSTOMER SELECTION ====================
                0 -> {
                    val filteredCustomers = remember(customers, customerSearchQuery) {
                        if (customerSearchQuery.isBlank()) customers.take(6)
                        else customers.filter {
                            it.name.contains(customerSearchQuery, ignoreCase = true) ||
                            it.phone.contains(customerSearchQuery)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        item {
                            // Smart customer search bar
                            OutlinedTextField(
                                value = customerSearchQuery,
                                onValueChange = { customerSearchQuery = it },
                                placeholder = { Text("Search ${terminology.partyLabel.lowercase()} by name or phone...", color = Ink400) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Ink600) },
                                trailingIcon = {
                                    IconButton(onClick = { showNewCustomerDialog = true }) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = "Add New", tint = Brand600)
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Brand600,
                                    unfocusedBorderColor = Line,
                                    focusedContainerColor = Surface,
                                    unfocusedContainerColor = Surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Selected Customer Card
                        item {
                            if (selectedCustomer != null) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.5.dp, Success600),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success600, modifier = Modifier.size(28.dp))
                                            Spacer(Modifier.width(10.dp))
                                            Column {
                                                Text(selectedCustomer!!.name, fontWeight = FontWeight.Bold, color = Ink900, fontSize = 15.sp)
                                                Text("📱 ${selectedCustomer!!.phone} • ${selectedCustomer!!.city.ifBlank { "Location N/A" }}", color = Ink600, fontSize = 12.sp)
                                                if (selectedCustomer!!.gstin.isNotBlank()) {
                                                    Text("GSTIN: ${selectedCustomer!!.gstin}", color = Ink600, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { selectedCustomer = null },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Line),
                                            modifier = Modifier.height(30.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Change", fontSize = 11.sp, color = Ink600)
                                        }
                                    }
                                }
                            }
                        }

                        // Recent / Suggested Customer Chips
                        item {
                            Text("Quick Select Recent ${terminology.partyPluralLabel}", style = MaterialTheme.typography.labelMedium, color = Ink600)
                        }

                        item {
                            if (customers.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("No customers registered yet", color = Ink400, fontSize = 13.sp)
                                        Spacer(Modifier.height(8.dp))
                                        Button(
                                            onClick = { showNewCustomerDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Brand600, contentColor = Color.White)
                                        ) {
                                            Text("+ Add First Customer")
                                        }
                                    }
                                }
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(customers.take(8)) { cust ->
                                        SuggestionChip(
                                            onClick = {
                                                selectedCustomer = cust
                                                if (cust.businessId > 0L && cust.businessId != formBusinessId) {
                                                    val custFirm = businessProfiles.find { it.id == cust.businessId }
                                                    if (custFirm != null) {
                                                        applyFirmDefaults(custFirm)
                                                    }
                                                }
                                            },
                                            label = { Text(cust.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (selectedCustomer?.id == cust.id) Brand100 else Surface,
                                                labelColor = if (selectedCustomer?.id == cust.id) Brand600 else Ink900
                                            ),
                                            border = SuggestionChipDefaults.suggestionChipBorder(
                                                enabled = true,
                                                borderColor = if (selectedCustomer?.id == cust.id) Brand600 else Line
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Document Metadata Row (Doc No & Date)
                        item {
                            Spacer(Modifier.height(Spacing.xs))
                            Text("Document Details", style = MaterialTheme.typography.labelMedium, color = Ink600)
                            Spacer(Modifier.height(4.dp))
                            CompactFieldRow {
                                CompactTextField(
                                    value = docNo,
                                    onValueChange = { docNo = it },
                                    label = if (isInvoice) "Invoice No" else "Estimate No",
                                    modifier = Modifier.weight(1f)
                                )
                                CompactTextField(
                                    value = docDate,
                                    onValueChange = { docDate = it },
                                    label = "Doc Date",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // ==================== STEP 1: LINE ITEMS & PRICING ====================
                1 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        // Mode Switcher Header: Solar Plant vs Itemized List
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, Line),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(3.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSolarModeActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        modifier = Modifier.weight(1f).clickable {
                                            isSolarModeActive = true
                                            if (itemsList.isEmpty() && kwVal > 0) generateStandardSolarBoq(kwVal)
                                        }
                                    ) {
                                        Text(
                                            text = "⚡ Solar Plant (kW × Rate)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSolarModeActive) MaterialTheme.colorScheme.onPrimary else Ink600,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (!isSolarModeActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        modifier = Modifier.weight(1f).clickable { isSolarModeActive = false }
                                    ) {
                                        Text(
                                            text = "📦 Itemized Bill of Materials",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!isSolarModeActive) MaterialTheme.colorScheme.onPrimary else Ink600,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (isSolarModeActive) {
                            // ------------------- SWAMI SOLAR CAPACITY ENGINE -------------------
                            // 1. Plant Capacity in kW
                            item {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("☀️", fontSize = 18.sp)
                                                Spacer(Modifier.width(6.dp))
                                                Text("Solar Rooftop Capacity", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Text(
                                                    text = "${(kwVal * 1000).toInt()} Watts Peak",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = kwText,
                                                onValueChange = { kwText = it },
                                                label = { Text("Capacity (kW)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f)
                                            )
                                            OutlinedTextField(
                                                value = if (pricingMode == Calc.MODE_FORWARD) rateText else roundTotalText,
                                                onValueChange = { if (pricingMode == Calc.MODE_FORWARD) rateText = it else roundTotalText = it },
                                                label = { Text(if (pricingMode == Calc.MODE_FORWARD) "Rate (₹/kW)" else "Total (₹)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = installationText,
                                            onValueChange = { installationText = it },
                                            label = { Text("Installation Charges (₹)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        KitSelectorRow(
                                            kits = formConfig.kits,
                                            selectedIndex = selectedKitIndex,
                                            onKitSelected = { index, kit ->
                                                selectedKitIndex = index
                                                kwText = if (kit.capacityKw % 1.0 == 0.0) kit.capacityKw.toInt().toString() else kit.capacityKw.toString()
                                                rateText = kit.defaultRatePerUnit.toInt().toString()
                                                val total = kit.capacityKw * kit.defaultRatePerUnit
                                                roundTotalText = total.toInt().toString()
                                                // Generate BoQ items from the kit template
                                                itemsList.clear()
                                                kit.items.forEach { kitItem ->
                                                    val (desc, qty) = kitItem.resolveTemplates(kit.capacityKw)
                                                    itemsList.add(
                                                        DocItem(
                                                            label = "${itemsList.size + 1}",
                                                            description = desc,
                                                            hsn = kitItem.hsn,
                                                            qty = qty,
                                                            unit = kitItem.unit,
                                                            rate = 0.0,
                                                            amount = 0.0,
                                                            sortOrder = kitItem.sortOrder
                                                        )
                                                    )
                                                }
                                            },
                                            onCustomSelected = {
                                                selectedKitIndex = -1
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            // 2. Solar Pricing: Rate per kW / Fixed Total
                            item {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Line),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("💰 Solar System Pricing", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (pricingMode == Calc.MODE_FORWARD) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                    border = BorderStroke(0.8.dp, if (pricingMode == Calc.MODE_FORWARD) MaterialTheme.colorScheme.outline else Line),
                                                    modifier = Modifier.clickable { pricingMode = Calc.MODE_FORWARD }
                                                ) {
                                                    Text("Rate/kW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (pricingMode == Calc.MODE_FORWARD) MaterialTheme.colorScheme.primary else Ink600, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (pricingMode == Calc.MODE_ROUND) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                    border = BorderStroke(0.8.dp, if (pricingMode == Calc.MODE_ROUND) MaterialTheme.colorScheme.outline else Line),
                                                    modifier = Modifier.clickable { pricingMode = Calc.MODE_ROUND }
                                                ) {
                                                    Text("Fixed Total", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (pricingMode == Calc.MODE_ROUND) MaterialTheme.colorScheme.primary else Ink600, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        if (pricingMode == Calc.MODE_FORWARD) {
                                            OutlinedTextField(
                                                value = rateText,
                                                onValueChange = { rateText = it },
                                                label = { Text("Rate per kW (₹)") },
                                                leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Ink600) },
                                                trailingIcon = { Text("/ kW", fontWeight = FontWeight.Medium, color = Ink600, modifier = Modifier.padding(end = 12.dp)) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text("Quick Rate Presets:", fontSize = 11.5.sp, color = Ink600)
                                            Spacer(Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                listOf(42000, 45000, 48000, 50000, 52000, 55000).forEach { r ->
                                                    val isSelected = rateVal == r.toDouble()
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = if (isSelected) Success600 else MaterialTheme.colorScheme.surfaceVariant,
                                                        border = BorderStroke(1.dp, if (isSelected) Success600 else Line),
                                                        modifier = Modifier.clickable { rateText = r.toString() }
                                                    ) {
                                                        Text(
                                                            text = "₹${r / 1000}k/kW",
                                                            fontSize = 11.5.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Ink900,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "⚡ Base Cost: $kwVal kW × ₹${Formatters.plain(rateVal)}/kW = ${Formatters.money(kwVal * rateVal)}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        } else {
                                            OutlinedTextField(
                                                value = roundTotalText,
                                                onValueChange = { roundTotalText = it },
                                                label = { Text("Total Package Amount (₹)") },
                                                leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Ink600) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            val effective = if (kwVal > 0) roundTotalVal / kwVal else 0.0
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Derived Rate: ₹${Formatters.plain(effective)} / kW for $kwVal kW System",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. PM Surya Ghar Govt. Subsidy / Discount
                            item {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Line),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("🏛️", fontSize = 16.sp)
                                                Spacer(Modifier.width(6.dp))
                                                Text("PM Surya Ghar Subsidy", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Ink900)
                                            }
                                            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(0.8.dp, Success600)) {
                                                Text("Govt CFA", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Success600, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text("1-Tap MNRE Subsidy Presets:", fontSize = 11.5.sp, color = Ink600)
                                        Spacer(Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(
                                                "78000" to "3 kW+ (₹78,000)",
                                                "60000" to "2 kW (₹60,000)",
                                                "30000" to "1 kW (₹30,000)",
                                                "0" to "No Subsidy (₹0)"
                                            ).forEach { (amount, lbl) ->
                                                val isSelected = discountText == amount
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) Success600 else MaterialTheme.colorScheme.surfaceVariant,
                                                    border = BorderStroke(1.dp, if (isSelected) Success600 else Line),
                                                    modifier = Modifier.clickable { discountText = amount }
                                                ) {
                                                    Text(
                                                        text = lbl,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Ink900,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = discountText,
                                            onValueChange = { discountText = it },
                                            label = { Text("Subsidy / Discount Amount (₹)") },
                                            leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Danger600) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        if (discountVal > 0) {
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                text = "Consumer Base: ${Formatters.money(totals.grossSubtotal)} - ${Formatters.money(discountVal)} = ${Formatters.money(totals.taxable)}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Success600
                                            )
                                        }
                                    }
                                }
                            }

                            // 4. GST Rate Selector
                            item {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Line),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("GST & Taxes", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Ink900)
                                        Spacer(Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            formConfig.gstPresets.forEach { preset ->
                                                if (preset.rate >= 0) {
                                                    val mode = when (preset.rate) {
                                                        0.0 -> "NONE"
                                                        5.0 -> "GST_5"
                                                        12.0 -> "GST_12"
                                                        18.0 -> "GST_18"
                                                        28.0 -> "GST_28"
                                                        else -> "CUSTOM"
                                                    }
                                                    val isSelected = gstMode == mode
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = { gstMode = mode; if (mode == "CUSTOM") customGstText = preset.rate.toInt().toString() },
                                                        label = { Text("${preset.label} GST", fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                            containerColor = Surface,
                                                            labelColor = Ink900
                                                        )
                                                    )
                                                } else {
                                                    val isSelected = gstMode == "CUSTOM"
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = { gstMode = "CUSTOM" },
                                                        label = { Text("Custom", fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                            containerColor = Surface,
                                                            labelColor = Ink900
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. System Components (BoQ) Accordion
                            item {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Line),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clickable { isSolarBoqExpanded = !isSolarBoqExpanded },
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("📋 System Components (${itemsList.size} Specs)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Ink900)
                                                Text("Standard rooftop equipment included in quote", fontSize = 11.sp, color = Ink600)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(if (isSolarBoqExpanded) "Hide ▲" else "View / Edit ▼", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        if (isSolarBoqExpanded) {
                                            Spacer(Modifier.height(10.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                                OutlinedButton(
                                                    onClick = { generateStandardSolarBoq(kwVal) },
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("🔄 Reset Standard BoQ", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary)
                                                }
                                                Spacer(Modifier.width(6.dp))
                                                Button(
                                                    onClick = {
                                                        itemsList.add(DocItem(label = "${itemsList.size + 1}", description = "", hsn = "", qty = 1.0, unit = "NOS", rate = 0.0, amount = 0.0, sortOrder = itemsList.size))
                                                    },
                                                    shape = RoundedCornerShape(6.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("+ Add Component", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onPrimary)
                                                }
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            itemsList.forEachIndexed { idx, itm ->
                                                CompactItemRow(
                                                    item = itm,
                                                    index = idx,
                                                    onUpdate = { updated -> itemsList[idx] = updated },
                                                    onDelete = { itemsList.removeAt(idx) }
                                                )
                                                Spacer(Modifier.height(4.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // 6. Real-time Solar Totals Summary Card
                            item {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            val rateDisplay = if (pricingMode == Calc.MODE_FORWARD) rateVal else (if (kwVal > 0) roundTotalVal / kwVal else 0.0)
                                            Text("Solar System ($kwVal kW @ ${Formatters.money(rateDisplay)}/kW)", color = Ink600, fontSize = 13.sp)
                                            Text(Formatters.money(totals.grossSubtotal), fontWeight = FontWeight.SemiBold, color = Ink900)
                                        }
                                        if (discountVal > 0) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("PM Surya Ghar Subsidy / Discount", color = Success600, fontSize = 12.sp)
                                                Text("- ${Formatters.money(discountVal)}", fontWeight = FontWeight.SemiBold, color = Success600)
                                            }
                                        }
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Taxable Base", color = Ink600, fontSize = 12.sp)
                                            Text(Formatters.money(totals.taxable), color = Ink900)
                                        }
                                        if (totals.cgst > 0 || totals.sgst > 0) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("GST ($gstPercent%)", color = Ink600, fontSize = 12.sp)
                                                Text("+ ${Formatters.money(totals.cgst + totals.sgst)}", color = Ink900)
                                            }
                                        }
                                        HorizontalDivider(color = Line, modifier = Modifier.padding(vertical = 8.dp))
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column {
                                                Text("NET PAYABLE AMOUNT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink900)
                                                Text("Customer Share", fontSize = 11.sp, color = Ink600)
                                            }
                                            Text(Formatters.money(totals.total), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        } else {
                            // ------------------- STANDARD ITEMIZED ENGINE -------------------
                            // Action bar: Add Item & Item Catalog
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Line Items (${itemsList.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink900
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { showCatalogPicker = true },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Line),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("📖 Catalog", fontSize = 12.sp, color = Ink900, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                itemsList.add(
                                                    DocItem(
                                                        label = "${itemsList.size + 1}",
                                                        description = "",
                                                        hsn = "",
                                                        qty = 1.0,
                                                        unit = "NOS",
                                                        rate = 0.0,
                                                        amount = 0.0,
                                                        sortOrder = itemsList.size
                                                    )
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Brand600, contentColor = Color.White),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("+ Custom Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Spreadsheet Items List
                            if (itemsList.isEmpty()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = SurfaceSoft,
                                        border = BorderStroke(1.dp, Line),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("No items added yet", color = Ink600, fontSize = 14.sp)
                                            Spacer(Modifier.height(8.dp))
                                            Button(
                                                onClick = { showCatalogPicker = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = Brand600, contentColor = Color.White)
                                            ) {
                                                Text("Pick from ${terminology.inventoryLabel}")
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(itemsList.size) { index ->
                                    val itm = itemsList[index]
                                    CompactItemRow(
                                        item = itm,
                                        index = index,
                                        onUpdate = { updated -> itemsList[index] = updated },
                                        onDelete = { itemsList.removeAt(index) }
                                    )
                                }
                            }

                            // GST Rate & Discount Selector
                            item {
                                Spacer(Modifier.height(Spacing.xs))
                                Text("GST & Taxes", style = MaterialTheme.typography.labelMedium, color = Ink600)
                                Spacer(Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    formConfig.gstPresets.forEach { preset ->
                                        if (preset.rate >= 0) {
                                            val mode = when (preset.rate) {
                                                0.0 -> "NONE"
                                                5.0 -> "GST_5"
                                                12.0 -> "GST_12"
                                                18.0 -> "GST_18"
                                                28.0 -> "GST_28"
                                                else -> "CUSTOM"
                                            }
                                            val isSelected = gstMode == mode
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { gstMode = mode; if (mode == "CUSTOM") customGstText = preset.rate.toInt().toString() },
                                                label = { Text("${preset.label} GST", fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Brand600,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                    containerColor = Surface,
                                                    labelColor = Ink900
                                                )
                                            )
                                        } else {
                                            val isSelected = gstMode == "CUSTOM"
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { gstMode = "CUSTOM" },
                                                label = { Text("Custom", fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Brand600,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                    containerColor = Surface,
                                                    labelColor = Ink900
                                                )
                                            )
                                        }
                                    }
                                }

                                CompactFieldRow(modifier = Modifier.padding(top = 6.dp)) {
                                    CompactTextField(
                                        value = discountText,
                                        onValueChange = { discountText = it },
                                        label = "Discount (₹)",
                                        keyboardType = KeyboardType.Number,
                                        modifier = Modifier.weight(1f)
                                    )
                                    CompactTextField(
                                        value = dueDate,
                                        onValueChange = { dueDate = it },
                                        label = "Payment Due Date",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Live Running Totals Card
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Line),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = Spacing.xs)
                                    ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Subtotal", color = Ink600, fontSize = 13.sp)
                                            Text(Formatters.money(totals.taxable), fontWeight = FontWeight.SemiBold, color = Ink900)
                                        }
                                        if (totals.cgst > 0 || totals.sgst > 0) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("CGST + SGST ($gstPercent%)", color = Ink600, fontSize = 12.sp)
                                                Text("+ ${Formatters.money(totals.cgst + totals.sgst)}", color = Ink900)
                                            }
                                        }
                                        if (discountVal > 0) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Discount", color = Danger600, fontSize = 12.sp)
                                                Text("- ${Formatters.money(discountVal)}", color = Danger600)
                                            }
                                        }
                                        HorizontalDivider(color = Line, modifier = Modifier.padding(vertical = 6.dp))
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("TOTAL AMOUNT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
                                            Text(Formatters.money(totals.total), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink900)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==================== STEP 2: REVIEW & OPTIONAL DETAILS ====================
                2 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        // Summary Card
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.5.dp, Brand600),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Document Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
                                        Text(if (isInvoice) "TAX INVOICE" else "ESTIMATE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Brand600)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("Customer: ${selectedCustomer?.name ?: "N/A"}", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Ink900)
                                    Text("No: $docNo • Date: $docDate", fontSize = 12.sp, color = Ink600)
                                    if (isSolarModeActive && kwVal > 0) {
                                        val rDisplay = if (pricingMode == Calc.MODE_FORWARD) rateVal else (if (kwVal > 0) roundTotalVal / kwVal else 0.0)
                                        Text("⚡ System: $kwVal kW Solar Rooftop Plant @ ${Formatters.money(rDisplay)}/kW", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.primary)
                                        if (discountVal > 0) {
                                            Text("🏛️ PM Surya Ghar Subsidy: -${Formatters.money(discountVal)}", fontSize = 12.sp, color = Success600, fontWeight = FontWeight.SemiBold)
                                        }
                                        Text("📋 BoQ Equipment: ${itemsList.size} specs included", fontSize = 12.sp, color = Ink600)
                                    } else {
                                        Text("Items: ${itemsList.size} line items", fontSize = 12.sp, color = Ink600)
                                    }
                                    HorizontalDivider(color = Line, modifier = Modifier.padding(vertical = 8.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Net Payable Total", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink900)
                                        Text(Formatters.money(totals.total), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink900)
                                    }
                                }
                            }
                        }

                        // Payment recording (Invoices only)
                        if (isInvoice) {
                            item {
                                CollapsibleSection(
                                    title = "Record Payment Receipt",
                                    icon = Icons.Default.Receipt,
                                    isExpanded = paidAmountText.isNotBlank(),
                                    onToggle = {
                                        paidAmountText = if (paidAmountText.isBlank()) totals.total.toInt().toString() else ""
                                    },
                                    badge = "Optional"
                                ) {
                                    CompactFieldRow {
                                        CompactTextField(
                                            value = paidAmountText,
                                            onValueChange = { paidAmountText = it },
                                            label = "Received Amount (₹)",
                                            keyboardType = KeyboardType.Number,
                                            modifier = Modifier.weight(1f)
                                        )
                                        CompactTextField(
                                            value = paymentMode,
                                            onValueChange = { paymentMode = it },
                                            label = "Payment Mode",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // Collapsible: Transport Details
                        item {
                            CollapsibleSection(
                                title = "Transport & Logistics",
                                icon = Icons.Default.LocalShipping,
                                isExpanded = isTransportExpanded,
                                onToggle = { isTransportExpanded = !isTransportExpanded },
                                badge = "Optional"
                            ) {
                                CompactFieldRow {
                                    CompactTextField(
                                        value = vehicleNo,
                                        onValueChange = { vehicleNo = it.uppercase() },
                                        label = "Vehicle No",
                                        modifier = Modifier.weight(1f)
                                    )
                                    CompactTextField(
                                        value = transporterName,
                                        onValueChange = { transporterName = it },
                                        label = "Transporter",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                CompactFieldRow {
                                    CompactTextField(
                                        value = ewayBillNo,
                                        onValueChange = { ewayBillNo = it },
                                        label = "E-Way Bill No",
                                        modifier = Modifier.weight(1f)
                                    )
                                    CompactTextField(
                                        value = deliveryAddress,
                                        onValueChange = { deliveryAddress = it },
                                        label = "Ship to Address",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Collapsible: Bank Coordinates
                        item {
                            CollapsibleSection(
                                title = "Bank & UPI Details",
                                icon = Icons.Default.AccountBalance,
                                isExpanded = isBankExpanded,
                                onToggle = { isBankExpanded = !isBankExpanded },
                                badge = "Letterhead"
                            ) {
                                CompactFieldRow {
                                    CompactTextField(
                                        value = bankName,
                                        onValueChange = { bankName = it },
                                        label = "Bank Name",
                                        modifier = Modifier.weight(1f)
                                    )
                                    CompactTextField(
                                        value = accountNo,
                                        onValueChange = { accountNo = it },
                                        label = "Account No",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                CompactTextField(
                                    value = ifscCode,
                                    onValueChange = { ifscCode = it.uppercase() },
                                    label = "IFSC Code",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Collapsible: Terms & Conditions
                        item {
                            CollapsibleSection(
                                title = "Terms & Conditions",
                                icon = Icons.Default.Description,
                                isExpanded = isTermsExpanded,
                                onToggle = { isTermsExpanded = !isTermsExpanded },
                                badge = "Legal"
                            ) {
                                val termList = termsText.split("\n").filter { it.isNotBlank() }
                                TermsEditor(
                                    terms = termList,
                                    onTermsChanged = { updated -> termsText = updated.joinToString("\n") },
                                    defaultTerms = formConfig.defaultTerms,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Collapsible: Notes
                        item {
                            CollapsibleSection(
                                title = "Private / Public Notes",
                                icon = Icons.Default.Note,
                                isExpanded = isNotesExpanded,
                                onToggle = { isNotesExpanded = !isNotesExpanded },
                                badge = "Optional"
                            ) {
                                OutlinedTextField(
                                    value = noteText,
                                    onValueChange = { noteText = it },
                                    label = { Text("Notes / Remarks") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Brand600,
                                        unfocusedBorderColor = Line,
                                        focusedContainerColor = Surface,
                                        unfocusedContainerColor = Surface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Item Catalog Bottom Sheet
    if (showCatalogPicker) {
        if (formConfig.catalogCategories.isNotEmpty()) {
            ModalBottomSheet(
                onDismissRequest = { showCatalogPicker = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                CatalogPicker(
                    categories = formConfig.catalogCategories,
                    onItemAdded = { catalogItem ->
                        itemsList.add(
                            DocItem(
                                label = "${itemsList.size + 1}",
                                description = catalogItem.label,
                                hsn = "",
                                qty = 1.0,
                                unit = catalogItem.unit,
                                rate = catalogItem.defaultRate,
                                amount = catalogItem.defaultRate,
                                sortOrder = itemsList.size
                            )
                        )
                        Toast.makeText(context, "Added '${catalogItem.label.take(20)}...'", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } else {
            ItemCatalogBottomSheet(
                items = itemMasters,
                onSelect = { m ->
                    itemsList.add(
                        DocItem(
                            label = "${itemsList.size + 1}",
                            description = m.description,
                            hsn = m.hsn,
                            qty = 1.0,
                            unit = m.unit,
                            rate = m.rate,
                            amount = m.rate,
                            sortOrder = itemsList.size
                        )
                    )
                    Toast.makeText(context, "Added '${m.description.take(20)}...'", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showCatalogPicker = false }
            )
        }
    }

    // Modal: New Customer Dialog
    if (showNewCustomerDialog) {
        CustomerDialog(
            onDismiss = { showNewCustomerDialog = false },
            onSave = { newCust ->
                showNewCustomerDialog = false
                viewModel.saveCustomer(newCust.copy(businessId = formBusinessId)) { newId ->
                    selectedCustomer = newCust.copy(id = newId.toInt(), businessId = formBusinessId)
                    Toast.makeText(context, "Customer '${newCust.name}' Added!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Modal: Business Switcher Dialog
    if (showBusinessSwitcherInDoc) {
        BusinessSwitcherDialog(
            businesses = businessProfiles,
            activeBusinessId = formBusinessId,
            onSelectBusiness = { selectedId ->
                val chosen = businessProfiles.find { it.id == selectedId }
                if (chosen != null) {
                    applyFirmDefaults(chosen)
                }
                showBusinessSwitcherInDoc = false
                Toast.makeText(context, "Switched firm to '${chosen?.brandName}'", Toast.LENGTH_SHORT).show()
            },
            onAddNewBusiness = { showBusinessSwitcherInDoc = false },
            onEditBusiness = { showBusinessSwitcherInDoc = false },
            onDismiss = { showBusinessSwitcherInDoc = false }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to go back?") },
            confirmButton = {
                TextButton(onClick = { 
                    showDiscardDialog = false
                    onNavigateBack() 
                }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }
}

