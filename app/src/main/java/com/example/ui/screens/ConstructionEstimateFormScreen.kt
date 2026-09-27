package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.CustomerDialog
import com.example.ui.components.GstPresetChips
import com.example.ui.components.TermsEditor
import com.example.data.model.FirmFormConfig
import com.example.data.model.FirmFormConfigRegistry
import com.example.ui.theme.SwiggyBorder
import com.example.ui.theme.SwiggyDark
import com.example.ui.theme.SwiggyGray
import com.example.ui.theme.SwiggyOrange
import com.example.ui.theme.SwamiGold
import com.example.ui.theme.SwamiGreen
import com.example.ui.theme.SwamiNavy
import com.example.ui.theme.SwamiRed
import com.example.ui.viewmodel.BillingViewModel
import com.example.utils.Formatters
import com.example.utils.NumWords
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Custom Construction Item Row state.
 *
 * NOTE: [rate] and [qty] use toDoubleOrNull() for parsing which is lax.
 * [taxable] computes via BigDecimal (good) but converts back to Double (legacy).
 * TODO: Migrate to [com.example.core.money.Money] and [MoneyTextField] for input.
 */
data class ConstructionItemRow(
    val id: Int = 0,
    val description: String = "",
    val rateText: String = "",
    val qtyText: String = "1",
    val unit: String = "Sq.Ft"
) {
    val rate: Double get() = rateText.toDoubleOrNull() ?: 0.0
    val qty: Double get() = qtyText.toDoubleOrNull() ?: 0.0
    val taxable: Double
        get() = BigDecimal.valueOf(rate)
            .multiply(BigDecimal.valueOf(qty))
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
}

/**
 * Standard Civil Item Definition for Quick Catalog
 */
data class StandardCivilWork(
    val category: String,
    val title: String,
    val description: String,
    val defaultUnit: String,
    val defaultRate: Double
)

val STANDARD_CIVIL_CATALOG = listOf(
    // Foundation & Earthwork
    StandardCivilWork("Earthwork", "Excavation in Earth / Soft Rock", "Excavation in earth, soft rock & murrum for foundation trench/pit including disposal & refilling.", "Cu.Ft", 18.0),
    StandardCivilWork("Earthwork", "Murrum / Sand Filling in Plinth", "Filling in plinth with quarry murrum / sand in 15cm layers including watering, compaction & ramming.", "Brass", 3200.0),
    StandardCivilWork("Earthwork", "Anti-Termite Treatment", "Chemical anti-termite treatment for soil, foundation trenches and plinth area as per IS standards.", "Sq.Ft", 14.0),

    // Concrete & RCC
    StandardCivilWork("Concrete & RCC", "PCC 1:4:8 Bedding", "Providing & laying Plain Cement Concrete (PCC) 1:4:8 in foundation and floor base with curing.", "Cu.Ft", 120.0),
    StandardCivilWork("Concrete & RCC", "RCC Footings & Columns (M20/M25)", "Providing and casting RCC M20/M25 for foundation footings and columns including steel & formwork.", "Cu.Ft", 380.0),
    StandardCivilWork("Concrete & RCC", "RCC Plinth & Tie Beams", "Providing and casting RCC Plinth beams with M20 concrete, centering, shuttering & reinforcement steel.", "Cu.Ft", 390.0),
    StandardCivilWork("Concrete & RCC", "RCC Roof Slab & Beams Casting", "Providing & casting RCC M20/M25 roof slab and beams including centering, steel binding & curing complete.", "Cu.Ft", 410.0),
    StandardCivilWork("Concrete & RCC", "RCC Staircase Waist Slab & Steps", "RCC Staircase with steps, waist slab, reinforcement binding and smooth finish.", "Cu.Ft", 420.0),

    // Masonry & Brickwork
    StandardCivilWork("Masonry", "6\" Red Clay / Fly Ash Brickwork", "Providing and constructing 6 inch thick brick masonry in cement mortar 1:6 with proper racking of joints.", "Sq.Ft", 95.0),
    StandardCivilWork("Masonry", "9\" Main Load Bearing Brickwork", "Providing and constructing 9 inch thick brick masonry in cement mortar 1:6 with curing complete.", "Sq.Ft", 145.0),
    StandardCivilWork("Masonry", "4\" Partition Brickwork", "Providing and constructing 4 inch thick partition brick wall with RCC patli & cement mortar 1:4.", "Sq.Ft", 65.0),
    StandardCivilWork("Masonry", "AAC Lightweight Block Masonry", "AAC Lightweight Block masonry 6\" thick using thin-bed polymer adhesive jointing mortar.", "Sq.Ft", 88.0),

    // Plaster & POP
    StandardCivilWork("Plaster", "Internal Smooth Cement Plaster (12mm)", "Internal cement plaster 12mm thick in cement mortar 1:4 with smooth neeru / lime finish and 7 days curing.", "Sq.Ft", 42.0),
    StandardCivilWork("Plaster", "External Sand-Faced Plaster (20mm)", "External double coat sand-faced cement plaster 20mm thick with waterproofing compound and scaffolding.", "Sq.Ft", 55.0),
    StandardCivilWork("Plaster", "POP Punning / False Ceiling", "Gypsum / POP smooth punning on walls & designer false ceiling with perimeter channels.", "Sq.Ft", 75.0),

    // Flooring & Tiles
    StandardCivilWork("Flooring", "Vitrified Tiles Flooring 2x2 / 4x2", "Providing and laying premium 2x2 / 4x2 vitrified tiles on 1:4 cement bed with 4\" skirting & epoxy grouting.", "Sq.Ft", 85.0),
    StandardCivilWork("Flooring", "Bathroom / Toilet Anti-Skid & Wall Tiles", "Anti-skid ceramic floor tiles and 7ft glazed wall tiles with waterproof spacer grouting.", "Sq.Ft", 78.0),
    StandardCivilWork("Flooring", "Granite Kitchen Platform & Sink", "18mm black granite kitchen platform with stainless steel sink (SS 304), facia & granite support vertical brackets.", "Rft", 1100.0),
    StandardCivilWork("Flooring", "Granite Staircase Treads & Risers", "Providing and fixing polished granite treads & risers with full moulding, chamfering and grooving.", "Sq.Ft", 165.0),

    // Doors & Windows
    StandardCivilWork("Doors & Windows", "Granite Door & Window Framing", "Granite door and window frame (Chowkat) 3-side / 4-side with polishing and silicone seal.", "Rft", 195.0),
    StandardCivilWork("Doors & Windows", "Aluminium Sliding Windows (3-Track)", "Aluminium powder-coated 3-track sliding window with mosquito net & 5mm float glass.", "Sq.Ft", 225.0),
    StandardCivilWork("Doors & Windows", "Flush Door with Laminate & SS Hardware", "32mm waterproof solid core flush door with 1mm mica laminate, SS hinges, mortise lock & handles.", "Nos", 5800.0),

    // Plumbing & Electrical
    StandardCivilWork("Plumbing & Electrical", "Concealed CPVC/UPVC Plumbing Complete", "Concealed CPVC/UPVC water supply & SWR drainage piping with Jaguar/equivalent brass fittings complete.", "L.S.", 65000.0),
    StandardCivilWork("Plumbing & Electrical", "Concealed Electrical Wiring & DB", "Concealed PVC conduit wiring (Polycab/Finolex FR), modular switch plates, MCB Distribution Board & earthing.", "L.S.", 55000.0),
    StandardCivilWork("Plumbing & Electrical", "Overhead / Underground Water Tank", "Underground RCC sumptank / 1000L Triple Layer Overhead Water Storage Tank with inlet-outlet fittings.", "L.S.", 35000.0),

    // Painting & Waterproofing
    StandardCivilWork("Finishing", "Wall Putty (2 Coats) + Primer", "Applying 2 coats of Birla / JK white cement wall putty with 1 coat interior primer and sanding smooth.", "Sq.Ft", 16.0),
    StandardCivilWork("Finishing", "Interior Acrylic Emulsion / Royale Paint", "Applying 2 coats of Asian Paints Royale / Premium interior acrylic emulsion paint complete.", "Sq.Ft", 24.0),
    StandardCivilWork("Finishing", "Exterior Weather-Proof Apex Paint", "Applying 2 coats of Asian Paints Apex weather-proof exterior acrylic emulsion over 1 coat exterior primer.", "Sq.Ft", 22.0),
    StandardCivilWork("Finishing", "Terrace Waterproofing (Brickbat Coba)", "Terrace waterproofing with brickbat coba 115mm thick with chemical coating & pond testing.", "Sq.Ft", 75.0),

    // Fabrication
    StandardCivilWork("Fabrication", "MS Safety Window Grills", "Mild steel heavy decorative safety window box grills with red oxide primer and enamel paint.", "Sq.Ft", 160.0),
    StandardCivilWork("Fabrication", "SS 304 Staircase / Balcony Railing", "Stainless Steel 304 grade mirror finish staircase and balcony railing with toughened glass brackets.", "Rft", 1250.0),
    StandardCivilWork("Fabrication", "Main Designer Safety Gate", "Heavy duty MS designer main sliding/swing gate with sheet, hinges, lock & automotive PU paint.", "L.S.", 32000.0)
)

val COMMON_UNITS = listOf("Sq.Ft", "Cu.Ft", "Cu.M", "Brass", "Rft", "Nos", "Kg", "Bags", "L.S.", "Ton")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstructionEstimateFormScreen(
    editDocId: Int? = null,
    viewModel: BillingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isEdit = editDocId != null

    val customers by viewModel.customersState.collectAsState()
    val estimates by viewModel.estimatesState.collectAsState()

    var showCustomerPicker by remember { mutableStateOf(false) }
    var showNewCustomerDialog by remember { mutableStateOf(false) }
    var showQuickCatalogSheet by remember { mutableStateOf(false) }
    var selectedCatalogCategory by remember { mutableStateOf("All") }

    val formConfig = remember { FirmFormConfigRegistry.getConfig("CONSTRUCTION") }

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var docNo by rememberSaveable { mutableStateOf("") }
    var docDate by rememberSaveable { mutableStateOf(Formatters.todayIso()) }
    var validTillDate by rememberSaveable { mutableStateOf(Formatters.datePlusDaysIso(30)) }

    // PDF Table Layout Option: CONST_BOQ (Standard 6-col separate Qty & Unit) or CONST_TAX_BOQ (7-col itemized tax)
    var pdfLayoutMode by rememberSaveable { mutableStateOf("CONST_BOQ") }

    // GST: 18% standard for construction, or 12%, 5%, 0%
    var gstRate by remember { mutableStateOf(18.0) }
    var customGstText by rememberSaveable { mutableStateOf("18") }
    var isCustomGst by rememberSaveable { mutableStateOf(false) }
    var discountText by rememberSaveable { mutableStateOf("") }

    val itemsList = remember {
        mutableStateListOf(
            ConstructionItemRow(description = "", rateText = "", qtyText = "1", unit = "Sq.Ft")
        )
    }

    var noteText by rememberSaveable { mutableStateOf("Construction work estimate as per site measurements & specifications") }
    var termsText by rememberSaveable {
        mutableStateOf(
            """1. Quotation valid for 30 days from date of estimate.
2. Water and electricity to be provided at site by the client.
3. Any extra work beyond specified scope will be charged additional.
4. Payment milestone: 30% advance, balance as per progress of work.
5. All disputes subject to Dhule (Maharashtra) jurisdiction."""
        )
    }

    var isLoading by remember { mutableStateOf(true) }

    // Load initial data
    LaunchedEffect(editDocId) {
        if (isEdit) {
            val existing = viewModel.repository.getDocById(editDocId!!)
            if (existing != null) {
                docNo = existing.docNo
                selectedCustomer = viewModel.repository.getCustomerById(existing.customerId)
                docDate = existing.docDate
                validTillDate = existing.validTill
                pdfLayoutMode = if (existing.pricingMode == "CONST_TAX_BOQ") "CONST_TAX_BOQ" else "CONST_BOQ"
                gstRate = existing.gstPercent
                customGstText = existing.gstPercent.toString()
                isCustomGst = gstRate !in listOf(0.0, 5.0, 12.0, 18.0)
                discountText = if (existing.discount > 0.0) (if (existing.discount % 1.0 == 0.0) existing.discount.toInt().toString() else existing.discount.toString()) else ""
                noteText = existing.note
                termsText = existing.terms

                val existingItems = viewModel.repository.getItemsForDoc(existing.id)
                itemsList.clear()
                if (existingItems.isNotEmpty()) {
                    existingItems.forEach { itm ->
                        val rStr = if (itm.rate % 1.0 == 0.0) itm.rate.toInt().toString() else itm.rate.toString()
                        val qStr = if (itm.qty % 1.0 == 0.0) itm.qty.toInt().toString() else itm.qty.toString()
                        itemsList.add(
                            ConstructionItemRow(
                                id = itm.id,
                                description = itm.description,
                                rateText = rStr,
                                qtyText = qStr,
                                unit = itm.unit.ifBlank { "Sq.Ft" }
                            )
                        )
                    }
                } else {
                    itemsList.add(ConstructionItemRow(description = "", rateText = "", qtyText = "1", unit = "Sq.Ft"))
                }
            }
        } else {
            docNo = viewModel.repository.getNextDocNo(DocType.CONST_ESTIMATE)
        }
        isLoading = false
    }

    // Calculations - totally reactive on itemsList changes, discount, and GST
    val grossTaxable = itemsList.sumOf { it.taxable }
    val activeDiscount = discountText.toDoubleOrNull() ?: 0.0
    val totalTaxable = maxOf(0.0, BigDecimal.valueOf(grossTaxable - activeDiscount).setScale(2, RoundingMode.HALF_UP).toDouble())
    val activeGstPercent = if (isCustomGst) (customGstText.toDoubleOrNull() ?: 0.0) else gstRate
    val cgstAmount = BigDecimal.valueOf(totalTaxable)
        .multiply(BigDecimal.valueOf(activeGstPercent / 200.0))
        .setScale(2, RoundingMode.HALF_UP)
        .toDouble()
    val sgstAmount = BigDecimal.valueOf(totalTaxable)
        .multiply(BigDecimal.valueOf(activeGstPercent / 200.0))
        .setScale(2, RoundingMode.HALF_UP)
        .toDouble()
    val grandTotal = BigDecimal.valueOf(totalTaxable + cgstAmount + sgstAmount)
        .setScale(2, RoundingMode.HALF_UP)
        .toDouble()
    val amountInWords = NumWords.rupees(grandTotal)

    fun pickDate(currentIso: String, onDatePicked: (String) -> Unit) {
        val cal = Calendar.getInstance()
        try {
            val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(currentIso)
            if (d != null) cal.time = d
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosen = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                onDatePicked(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(chosen.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Smart Preset Loader Function
    fun loadSmartPlan(planType: String) {
        itemsList.clear()
        when (planType) {
            "TURNKEY" -> {
                itemsList.addAll(
                    listOf(
                        ConstructionItemRow(description = "Excavation in earth, soft rock & murrum for foundation trench/pit including disposal & refilling.", rateText = "18", qtyText = "1500", unit = "Cu.Ft"),
                        ConstructionItemRow(description = "Providing & laying Plain Cement Concrete (PCC 1:4:8) under footings & flooring bed with curing.", rateText = "120", qtyText = "450", unit = "Cu.Ft"),
                        ConstructionItemRow(description = "Providing and casting RCC M20/M25 for footings, plinth beam, columns, beams & roof slab casting with steel & shuttering.", rateText = "390", qtyText = "1800", unit = "Cu.Ft"),
                        ConstructionItemRow(description = "Providing and constructing 6 inch thick brick masonry in cement mortar 1:6 with proper joint racking.", rateText = "95", qtyText = "1200", unit = "Sq.Ft"),
                        ConstructionItemRow(description = "Internal smooth cement plaster 12mm & external double-coat sand-faced plaster 20mm complete.", rateText = "45", qtyText = "2800", unit = "Sq.Ft"),
                        ConstructionItemRow(description = "Providing and laying premium 2x2 / 4x2 vitrified floor tiles with 4\" skirting & epoxy grouting.", rateText = "85", qtyText = "1100", unit = "Sq.Ft"),
                        ConstructionItemRow(description = "Granite door/window chowkats & powder-coated aluminium 3-track sliding windows.", rateText = "220", qtyText = "240", unit = "Sq.Ft"),
                        ConstructionItemRow(description = "2 Coats Birla wall putty, primer & 2 coats Royale / Apex weather-proof painting complete.", rateText = "38", qtyText = "2800", unit = "Sq.Ft"),
                        ConstructionItemRow(description = "Concealed CPVC/UPVC plumbing, sanitary pipework, Jaguar/equivalent brass fittings & tank complete.", rateText = "65000", qtyText = "1", unit = "L.S."),
                        ConstructionItemRow(description = "Concealed FR copper wiring, modular switch plates, MCB Distribution Board & earthing.", rateText = "55000", qtyText = "1", unit = "L.S.")
                    )
                )
                Toast.makeText(context, "Loaded Residential Turnkey Package", Toast.LENGTH_SHORT).show()
            }
            "STRUCTURE" -> {
                itemsList.addAll(
                    listOf(
                        ConstructionItemRow(description = "Earthwork excavation & leveling for foundation footings.", rateText = "18", qtyText = "1200", unit = "Cu.Ft"),
                        ConstructionItemRow(description = "PCC 1:4:8 bed leveling in foundation.", rateText = "120", qtyText = "380", unit = "Cu.Ft"),
                        ConstructionItemRow(description = "RCC Foundation footings, plinth beam, columns, lintels & roof slab casting (M20 grade with steel & shuttering).", rateText = "390", qtyText = "1600", unit = "Cu.Ft"),
                        ConstructionItemRow(description = "6 inch brick masonry superstructure in cement mortar 1:6.", rateText = "95", qtyText = "1000", unit = "Sq.Ft"),
                        ConstructionItemRow(description = "Parapet wall construction with coping & waterproofing.", rateText = "140", qtyText = "180", unit = "Rft")
                    )
                )
                Toast.makeText(context, "Loaded Civil Structure Package", Toast.LENGTH_SHORT).show()
            }
            "RENOVATION" -> {
                itemsList.addAll(
                    listOf(
                        ConstructionItemRow(description = "Dismantling old flooring, wall hacking, debris removal & transport.", rateText = "18000", qtyText = "1", unit = "L.S."),
                        ConstructionItemRow(description = "Wall hacking and 12mm internal cement plastering.", rateText = "42", qtyText = "1400", unit = "Sq.Ft"),
                        ConstructionItemRow(description = "Vitrified floor tiles 4x2 & bathroom wall tile fitting.", rateText = "88", qtyText = "1100", unit = "Sq.Ft"),
                        ConstructionItemRow(description = "Toilet & bathroom waterproofing with brickbat coba and chemical coat.", rateText = "75", qtyText = "150", unit = "Sq.Ft"),
                        ConstructionItemRow(description = "2 Coat Birla wall putty + Asian Paints Royale luxury emulsion painting.", rateText = "40", qtyText = "2200", unit = "Sq.Ft")
                    )
                )
                Toast.makeText(context, "Loaded Renovation & Finishing Package", Toast.LENGTH_SHORT).show()
            }
            "FABRICATION" -> {
                itemsList.addAll(
                    listOf(
                        ConstructionItemRow(description = "MS heavy decorative safety window box grills with anti-rust primer and enamel paint.", rateText = "165", qtyText = "220", unit = "Sq.Ft"),
                        ConstructionItemRow(description = "SS 304 grade mirror finish staircase railing with 50mm pipe & toughened glass brackets.", rateText = "1250", qtyText = "45", unit = "Rft"),
                        ConstructionItemRow(description = "Heavy duty MS designer main safety gate with sheet, hinges, lock & paint.", rateText = "32000", qtyText = "1", unit = "L.S.")
                    )
                )
                Toast.makeText(context, "Loaded Fabrication & Steel Work Package", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEdit) "Edit Construction Estimate" else "New Construction Estimate",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "SWAMI CONSTRUCTION • $docNo",
                            fontSize = 11.5.sp,
                            color = SwamiGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwamiNavy,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Button(
                        onClick = {
                            if (selectedCustomer == null) {
                                Toast.makeText(context, "Please select or add a client", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (itemsList.isEmpty() || itemsList.all { it.description.isBlank() }) {
                                Toast.makeText(context, "Please add at least one work item description", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val docItems = itemsList.mapIndexed { idx, row ->
                                DocItem(
                                    id = row.id,
                                    label = (idx + 1).toString(),
                                    description = row.description.trim(),
                                    hsn = "9954",
                                    qty = row.qty,
                                    unit = row.unit.trim().ifBlank { "Sq.Ft" },
                                    rate = row.rate,
                                    amount = row.taxable,
                                    sortOrder = idx
                                )
                            }

                            val docToSave = Doc(
                                id = editDocId ?: 0,
                                docType = DocType.CONST_ESTIMATE,
                                docNo = docNo,
                                docDate = docDate,
                                validTill = validTillDate,
                                dueDate = "",
                                customerId = selectedCustomer!!.id,
                                kw = 0.0,
                                ratePerKw = 0.0,
                                pricingMode = pdfLayoutMode, // CONST_BOQ or CONST_TAX_BOQ
                                gstMode = "GST_${activeGstPercent.toInt()}",
                                gstPercent = activeGstPercent,
                                discount = activeDiscount,
                                taxable = totalTaxable,
                                cgst = cgstAmount,
                                sgst = sgstAmount,
                                total = grandTotal,
                                amountWords = amountInWords,
                                note = noteText.trim(),
                                terms = termsText.trim(),
                                status = DocStatus.ESTIMATE,
                                parentEstimateId = null
                            )

                            viewModel.saveDoc(docToSave, docItems) { savedId ->
                                Toast.makeText(context, "Saved $docNo successfully", Toast.LENGTH_SHORT).show()
                                onNavigateToDetail(savedId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SwamiGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SAVE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Smart Plan Presets Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SwamiNavy.copy(alpha = 0.05f)),
                    border = BorderStroke(1.5.dp, SwamiNavy.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(SwamiGold.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Smart Construction Plans & Presets",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SwamiNavy
                                    )
                                    Text(
                                        text = "1-Tap to auto-load industry standard BOQ work items",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                OutlinedButton(
                                    onClick = { loadSmartPlan("TURNKEY") },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, SwamiNavy.copy(alpha = 0.3f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("🏠 Full Turnkey House", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SwamiNavy)
                                }
                            }
                            item {
                                OutlinedButton(
                                    onClick = { loadSmartPlan("STRUCTURE") },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, SwamiNavy.copy(alpha = 0.3f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("🧱 RCC Structure & Slab", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SwamiNavy)
                                }
                            }
                            item {
                                OutlinedButton(
                                    onClick = { loadSmartPlan("RENOVATION") },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, SwamiNavy.copy(alpha = 0.3f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("🎨 Renovation & Finishing", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SwamiNavy)
                                }
                            }
                            item {
                                OutlinedButton(
                                    onClick = { loadSmartPlan("FABRICATION") },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, SwamiNavy.copy(alpha = 0.3f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("🔨 MS/SS Fabrication", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SwamiNavy)
                                }
                            }
                        }
                    }
                }
            }

            // Section 1: Client Selection
            item {
                SectionHeader(title = "1. Client Details", icon = Icons.Default.Person)
                Spacer(modifier = Modifier.height(8.dp))

                if (selectedCustomer != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SwamiNavy.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SwamiNavy.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedCustomer!!.name.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = SwamiNavy
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedCustomer!!.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (selectedCustomer!!.mobile.isNotBlank()) {
                                    Text(
                                        text = "Mobile: ${selectedCustomer!!.mobile}",
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                val loc = listOf(selectedCustomer!!.village, selectedCustomer!!.address).filter { it.isNotBlank() }.joinToString(", ")
                                if (loc.isNotBlank()) {
                                    Text(
                                        text = "Site: $loc",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { showCustomerPicker = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Change Client", tint = SwamiNavy)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCustomerPicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SwiggyBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiggyDark)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = SwiggyOrange)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Client")
                        }
                        Button(
                            onClick = { showNewCustomerDialog = true },
                            modifier = Modifier.weight(1.1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SwiggyOrange, contentColor = MaterialTheme.colorScheme.onPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add New Client", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }

            // Section 2: Document Info & PDF Layout Option
            item {
                SectionHeader(title = "2. Estimate Details & PDF Format Option", icon = Icons.Default.ReceiptLong)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = docNo,
                        onValueChange = { docNo = it },
                        label = { Text("Estimate No.") },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = SwamiNavy,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = SwamiNavy,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = SwamiNavy
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = docDate,
                        onValueChange = { docDate = it },
                        label = { Text("Estimate Date") },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = SwamiNavy,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = SwamiNavy,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = SwamiNavy
                        ),
                        trailingIcon = {
                            IconButton(onClick = { pickDate(docDate) { docDate = it } }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = SwamiNavy)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = validTillDate,
                    onValueChange = { validTillDate = it },
                    label = { Text("Quotation Valid Till") },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = SwamiNavy,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = SwamiNavy,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = SwamiNavy
                    ),
                    trailingIcon = {
                        IconButton(onClick = { pickDate(validTillDate) { validTillDate = it } }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Valid Till", tint = SwamiNavy)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // PDF Layout Option Selector Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "PDF Table Column Layout Format:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = SwamiNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = pdfLayoutMode == "CONST_BOQ",
                                onClick = { pdfLayoutMode = "CONST_BOQ" },
                                label = { Text("Standard Civil BOQ (# | Desc | Qty | Unit | Rate | Amt)", fontSize = 11.5.sp, color = if (pdfLayoutMode == "CONST_BOQ") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SwamiNavy,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = pdfLayoutMode == "CONST_TAX_BOQ",
                                onClick = { pdfLayoutMode = "CONST_TAX_BOQ" },
                                label = { Text("Itemized Tax BOQ (# | Desc | Qty | Unit | Rate | GST | Amt)", fontSize = 11.5.sp, color = if (pdfLayoutMode == "CONST_TAX_BOQ") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SwamiNavy,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Section 3: Multi-Item Custom Table & Quick Library
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = "3. Civil Work Items & Schedule of Rates", icon = Icons.Default.ListAlt)
                    Surface(
                        color = SwamiNavy.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${itemsList.size} item(s)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SwamiNavy,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Distinct Quantity & Unit columns with live calculations.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = { showQuickCatalogSheet = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.LibraryAdd, contentDescription = null, tint = SwamiGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("⚡ Work Library", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = SwamiGreen)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            itemsIndexed(itemsList) { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Header row of item with Item # and controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = SwamiNavy.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Item #${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SwamiNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Duplicate button
                                IconButton(
                                    onClick = {
                                        itemsList.add(index + 1, item.copy(id = 0))
                                        Toast.makeText(context, "Item duplicated", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }

                                if (itemsList.size > 1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { itemsList.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Remove Item",
                                            tint = SwamiRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Description Field
                        OutlinedTextField(
                            value = item.description,
                            onValueChange = { newDesc ->
                                itemsList[index] = item.copy(description = newDesc)
                            },
                            label = { Text("Work & Specification Description *") },
                            placeholder = { Text("e.g. RCC Column & Beam M20 grade work, Brickwork 6 inch, Plaster, Tile fitting...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = SwamiNavy,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedLabelColor = SwamiNavy,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = SwamiNavy
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Clear inputs for Quantity, Unit and Rate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = item.qtyText,
                                onValueChange = { newQty ->
                                    itemsList[index] = item.copy(qtyText = newQty)
                                },
                                label = { Text("Quantity") },
                                placeholder = { Text("1", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedBorderColor = SwamiNavy,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedLabelColor = SwamiNavy,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    cursorColor = SwamiNavy
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = item.unit,
                                onValueChange = { newUnit ->
                                    itemsList[index] = item.copy(unit = newUnit)
                                },
                                label = { Text("Unit") },
                                placeholder = { Text("Sq.Ft", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedBorderColor = SwamiNavy,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedLabelColor = SwamiNavy,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    cursorColor = SwamiNavy
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = item.rateText,
                                onValueChange = { newRate ->
                                    itemsList[index] = item.copy(rateText = newRate)
                                },
                                label = { Text("Rate (₹)") },
                                placeholder = { Text("0.00", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedBorderColor = SwamiNavy,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedLabelColor = SwamiNavy,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    cursorColor = SwamiNavy
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Unit Selector Chips
                        Text(
                            text = "Quick Unit Picker:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(COMMON_UNITS) { u ->
                                FilterChip(
                                    selected = item.unit.equals(u, ignoreCase = true),
                                    onClick = { itemsList[index] = item.copy(unit = u) },
                                    label = { Text(u, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SwamiNavy,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Row Live Calculation Badge
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val qDisplay = item.qtyText.ifBlank { "0" }
                                val uDisplay = item.unit.ifBlank { "Unit" }
                                val rDisplay = item.rateText.ifBlank { "0" }
                                Text(
                                    text = "$qDisplay $uDisplay × ₹$rDisplay",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Amount: ${Formatters.money(item.taxable)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = SwamiNavy
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Add Item Row Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            itemsList.add(
                                ConstructionItemRow(description = "", rateText = "", qtyText = "1", unit = "Sq.Ft")
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Add Blank Item", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { showQuickCatalogSheet = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SwamiGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LibraryAdd, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick From Library", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            // Section 4: Discount & GST Calculation
            item {
                SectionHeader(title = "4. Discount & GST Calculation", icon = Icons.Default.Percent)
                Spacer(modifier = Modifier.height(8.dp))

                // Optional Discount Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (activeDiscount > 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, if (activeDiscount > 0) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = if (activeDiscount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Special Discount (Optional)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (activeDiscount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (activeDiscount > 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "-₹${Formatters.plain(activeDiscount)} Off",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = discountText,
                            onValueChange = { discountText = it },
                            label = { Text("Discount Amount (₹)") },
                            prefix = { Text("₹ ") },
                            trailingIcon = if (discountText.isNotBlank()) {
                                {
                                    IconButton(onClick = { discountText = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear Discount", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. 5000 (Optional discount)") }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Discount Preset Chips
                        Text("Quick Discount Presets:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("2000", "5000", "10000", "15000", "20000", "25000", "50000").forEach { quickDisc ->
                                FilterChip(
                                    selected = discountText == quickDisc,
                                    onClick = {
                                        discountText = if (discountText == quickDisc) "" else quickDisc
                                    },
                                    label = { Text("₹${quickDisc.toInt() / 1000}k Off", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Select Applicable GST Rate:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = SwamiNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            formConfig.gstPresets.forEach { preset ->
                                if (preset.rate >= 0) {
                                    val isSelected = !isCustomGst && gstRate == preset.rate
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            isCustomGst = false
                                            gstRate = preset.rate
                                        },
                                        label = { Text(preset.label + " GST", fontSize = 11.5.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SwamiNavy,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                } else {
                                    FilterChip(
                                        selected = isCustomGst,
                                        onClick = { isCustomGst = true },
                                        label = { Text("Custom %", fontSize = 11.5.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SwamiNavy,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }

                        if (isCustomGst) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customGstText,
                                onValueChange = { customGstText = it },
                                label = { Text("GST %") },
                                textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = SwamiNavy,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedLabelColor = SwamiNavy,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    cursorColor = SwamiNavy
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }
            }

            // Section 5: Real-time Calculation Summary
            item {
                SectionHeader(title = "5. Calculation Summary & Verification", icon = Icons.Default.Calculate)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.5.dp, SwamiNavy.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (activeDiscount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Gross Works Value:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = Formatters.money(grossTaxable),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Special Discount:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "- ₹ ${Formatters.plain(activeDiscount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (activeDiscount > 0) "Net Taxable Value:" else "Total Taxable Value (Sum of Works):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = Formatters.money(totalTaxable),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = SwamiNavy
                            )
                        }

                        if (activeGstPercent > 0.0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CGST (${activeGstPercent / 2}%):", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Formatters.money(cgstAmount), fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SGST (${activeGstPercent / 2}%):", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Formatters.money(sgstAmount), fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("GST: Exempt / Nil (0%)", fontSize = 12.sp, color = SwamiGreen)
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ESTIMATE TOTAL:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SwamiNavy
                                )
                                Text(
                                    text = "All inclusive",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = Formatters.money(grandTotal),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = SwamiNavy
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "In words: Rupees $amountInWords",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section 6: Notes & Terms
            item {
                SectionHeader(title = "6. Notes & Terms & Conditions", icon = Icons.Default.Description)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note / Remark") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = SwamiNavy,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = SwamiNavy,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = SwamiNavy
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                val termList = termsText.split("\n").filter { it.isNotBlank() }
                TermsEditor(
                    terms = termList,
                    onTermsChanged = { updated -> termsText = updated.joinToString("\n") },
                    defaultTerms = formConfig.defaultTerms,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Quick Work Library Bottom Sheet / Dialog
    if (showQuickCatalogSheet) {
        val categories = listOf("All") + STANDARD_CIVIL_CATALOG.map { it.category }.distinct()
        val filteredCatalog = if (selectedCatalogCategory == "All") {
            STANDARD_CIVIL_CATALOG
        } else {
            STANDARD_CIVIL_CATALOG.filter { it.category == selectedCatalogCategory }
        }

        AlertDialog(
            onDismissRequest = { showQuickCatalogSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LibraryBooks, contentDescription = null, tint = SwamiNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Civil Work Library", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
                    Text(
                        "Tap any standard civil specification to add it immediately:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCatalogCategory == cat,
                                onClick = { selectedCatalogCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SwamiNavy,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCatalog) { workItem ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // If the first row is empty, replace it, otherwise add new row
                                        val rateFormatted = if (workItem.defaultRate % 1.0 == 0.0) workItem.defaultRate.toInt().toString() else workItem.defaultRate.toString()
                                        if (itemsList.size == 1 && itemsList[0].description.isBlank() && itemsList[0].rateText.isBlank()) {
                                            itemsList[0] = ConstructionItemRow(
                                                description = workItem.description,
                                                rateText = rateFormatted,
                                                qtyText = "1",
                                                unit = workItem.defaultUnit
                                            )
                                        } else {
                                            itemsList.add(
                                                ConstructionItemRow(
                                                    description = workItem.description,
                                                    rateText = rateFormatted,
                                                    qtyText = "1",
                                                    unit = workItem.defaultUnit
                                                )
                                            )
                                        }
                                        Toast.makeText(context, "Added: ${workItem.title}", Toast.LENGTH_SHORT).show()
                                        showQuickCatalogSheet = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = workItem.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SwamiNavy,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            color = SwamiGreen.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "₹${Formatters.plain(workItem.defaultRate)} / ${workItem.defaultUnit}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SwamiGreen,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = workItem.description,
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuickCatalogSheet = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Customer Picker Modal
    if (showCustomerPicker) {
        CustomerPickerModal(
            customers = customers,
            estimates = estimates,
            isInvoice = false,
            onDismiss = { showCustomerPicker = false },
            onSelectCustomer = { cust ->
                showCustomerPicker = false
                selectedCustomer = cust
            },
            onSelectEstimate = { estDetail ->
                showCustomerPicker = false
                selectedCustomer = estDetail.customer
            },
            onAddNew = {
                showCustomerPicker = false
                showNewCustomerDialog = true
            }
        )
    }

    // New Customer Dialog
    if (showNewCustomerDialog) {
        CustomerDialog(
            existing = null,
            onDismiss = { showNewCustomerDialog = false },
            onSave = { newCust ->
                coroutineScope.launch {
                    val newId = viewModel.repository.saveCustomer(newCust)
                    val inserted = viewModel.repository.getCustomerById(newId.toInt())
                    selectedCustomer = inserted
                    showNewCustomerDialog = false
                    Toast.makeText(context, "Added client ${newCust.name}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = SwamiNavy, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SwamiNavy)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerPickerModal(
    customers: List<Customer>,
    estimates: List<DocWithDetails>,
    isInvoice: Boolean,
    onDismiss: () -> Unit,
    onSelectCustomer: (Customer) -> Unit,
    onSelectEstimate: (DocWithDetails) -> Unit,
    onAddNew: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers
        else customers.filter { it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery) || it.city.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Client / Customer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onAddNew) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New Client")
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search client by name, phone or city...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No clients found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(filtered) { cust ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCustomer(cust) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SwamiNavy.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    cust.name.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = SwamiNavy
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (cust.phone.isNotBlank()) {
                                    Text(cust.phone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (cust.address.isNotBlank() || cust.city.isNotBlank()) {
                                    Text(listOf(cust.address, cust.city).filter { it.isNotBlank() }.joinToString(", "), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
