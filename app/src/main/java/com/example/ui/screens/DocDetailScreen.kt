package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import com.example.utils.DynamicUpiQrGenerator
import com.example.utils.ThermalPrinterHelper
import com.example.data.entity.BusinessProfile
import com.example.ui.theme.SwiggyDark
import com.example.ui.theme.SwiggyGray
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Company
import com.example.data.model.Doc
import com.example.data.model.DocStatus
import com.example.data.model.DocType
import com.example.data.model.DocWithDetails
import com.example.data.model.Payment
import com.example.ui.components.AddPaymentDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SwamiGold
import com.example.ui.theme.SwamiGreen
import com.example.ui.theme.SwamiNavy
import com.example.ui.theme.SwamiRed
import com.example.ui.viewmodel.BillingViewModel
import com.example.utils.Formatters
import com.example.utils.PdfGenerator
import com.example.utils.ShareHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocDetailScreen(
    docId: Int,
    viewModel: BillingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int, String) -> Unit,
    onNavigateToLinkedDoc: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val detail by remember(docId) { viewModel.getDocDetailFlow(docId) }.collectAsState(initial = null)
    val company by viewModel.companyState.collectAsState()
    val businessProfiles by viewModel.businessProfilesState.collectAsState()
    val activeBusinessProfile by viewModel.activeBusinessProfileState.collectAsState()
    val activeBusinessId by viewModel.activeBusinessIdState.collectAsState()

    var selectedPdfTheme by remember { mutableStateOf("MODERN_COLOR") }
    LaunchedEffect(detail?.doc?.businessId, activeBusinessId) {
        val bId = detail?.doc?.businessId?.takeIf { it > 0 } ?: activeBusinessId
        selectedPdfTheme = viewModel.repository.getSetting("pdf_theme", "MODERN_COLOR", bId)
    }

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showFirmSwitchDialog by remember { mutableStateOf(false) }
    var showFirmEditDialog by remember { mutableStateOf(false) }
    var showPdfPreviewDialog by remember { mutableStateOf(false) }
    var whatsAppDialogType by remember { mutableStateOf<com.example.utils.ShareHelper.TemplateType?>(null) }
    var selectedReceiptPayment by remember { mutableStateOf<Payment?>(null) }
    var childInvoice by remember { mutableStateOf<Doc?>(null) }
    var showEditTermsDialog by remember { mutableStateOf(false) }
    var editingTermsText by remember { mutableStateOf("") }

    LaunchedEffect(detail?.doc?.id) {
        val current = detail
        if (current != null && !current.doc.isInvoice) {
            childInvoice = viewModel.repository.getChildInvoiceForEstimate(current.doc.id)
        }
    }

    if (detail == null) {
        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading document details...")
            }
        }
        return
    }

    val currentDetail = detail!!
    val doc = currentDetail.doc
    val isInvoice = doc.isInvoice

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = doc.docNo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (doc.isConstruction) "CIVIL ESTIMATE & BOQ" else if (isInvoice) "TAX INVOICE" else "QUOTATION / ESTIMATE",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.ui.theme.BizNavy,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { onNavigateToEdit(doc.id, doc.docType) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("📋 Duplicate Invoice") },
                                onClick = {
                                    showMenu = false
                                    scope.launch {
                                        val nextDocNo = if (doc.isInvoice) {
                                            val prefix = viewModel.repository.getSetting("inv_prefix", "INV-", doc.businessId)
                                            val nextNum = viewModel.repository.getSetting("next_inv_no", "1", doc.businessId)
                                            prefix + nextNum.padStart(5, '0')
                                        } else {
                                            val prefix = viewModel.repository.getSetting("est_prefix", "EST-", doc.businessId)
                                            val nextNum = viewModel.repository.getSetting("next_est_no", "1", doc.businessId)
                                            prefix + nextNum.padStart(5, '0')
                                        }
                                        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                                        val newDoc = doc.copy(
                                            id = 0,
                                            docNo = nextDocNo,
                                            docDate = today,
                                            dueDate = "",
                                            status = if (doc.isInvoice) com.example.data.model.DocStatus.UNPAID else com.example.data.model.DocStatus.ESTIMATE,
                                            parentEstimateId = null,
                                            pdfPath = null,
                                            whatsappShareCount = 0,
                                            createdAt = System.currentTimeMillis(),
                                            updatedAt = System.currentTimeMillis()
                                        )
                                        val newDocId = viewModel.repository.db.docDao().insert(newDoc).toInt()
                                        val copiedItems = currentDetail.items.map { item -> item.copy(id = 0, docId = newDocId) }
                                        viewModel.repository.db.docItemDao().insertAll(copiedItems)
                                        
                                        if (doc.isInvoice) {
                                            val nextNum = (viewModel.repository.getSetting("next_inv_no", "1", doc.businessId).toIntOrNull() ?: 1) + 1
                                            viewModel.repository.setSetting("next_inv_no", nextNum.toString(), doc.businessId)
                                        } else {
                                            val nextNum = (viewModel.repository.getSetting("next_est_no", "1", doc.businessId).toIntOrNull() ?: 1) + 1
                                            viewModel.repository.setSetting("next_est_no", nextNum.toString(), doc.businessId)
                                        }
                                        Toast.makeText(context, "Invoice duplicated as $nextDocNo", Toast.LENGTH_SHORT).show()
                                        onNavigateToLinkedDoc(newDocId)
                                    }
                                }
                            )
                            if (isInvoice && doc.status != DocStatus.CANCELLED) {
                                DropdownMenuItem(
                                    text = { Text("Mark Cancelled") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.updateDocStatus(doc.id, DocStatus.CANCELLED)
                                        Toast.makeText(context, "Invoice marked cancelled", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Delete Document", color = SwamiRed) },
                                onClick = {
                                    showMenu = false
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Document Lifecycle Visualizer
            item {
                DocumentLifecycleTracker(
                    doc = doc,
                    currentDetail = currentDetail,
                    childInvoice = childInvoice,
                    onNavigateToLinkedDoc = onNavigateToLinkedDoc,
                    onAddPaymentClick = { showPaymentDialog = true },
                    onConvertClick = {
                        viewModel.convertToInvoice(doc.id) { newInvoiceId ->
                            Toast.makeText(context, "Invoice created successfully!", Toast.LENGTH_SHORT).show()
                            onNavigateToLinkedDoc(newInvoiceId)
                        }
                    }
                )
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentDetail.customerName.ifEmpty { "(No Customer)" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            StatusBadge(status = currentDetail.computedStatus)
                        }

                        if (currentDetail.customer != null) {
                            val custNo = "CUST-#${currentDetail.customer.id.toString().padStart(4, '0')}"
                            DetailRow(label = "Customer Number", value = custNo)

                            val addressStr = listOfNotNull(
                                currentDetail.customer.village.takeIf { it.isNotBlank() },
                                currentDetail.customer.address.takeIf { it.isNotBlank() }
                            ).joinToString(", ")
                            if (addressStr.isNotBlank()) {
                                DetailRow(label = "Address", value = addressStr)
                            }
                        }

                        if (currentDetail.customerMobile.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = SwamiNavy,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentDetail.customerMobile,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Call",
                                    color = SwamiNavy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.clickable {
                                        ShareHelper.dialPhone(context, currentDetail.customerMobile)
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "WhatsApp",
                                    color = Color(0xFF25D366),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.clickable {
                                        ShareHelper.openWhatsAppChat(context, currentDetail.customerMobile, "")
                                    }
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        DetailRow(
                            label = if (isInvoice) "Invoice Date" else "Estimate Date",
                            value = Formatters.fmtDate(doc.docDate)
                        )
                        if (!isInvoice && doc.validTill.isNotBlank()) {
                            DetailRow(label = "Valid Till", value = Formatters.fmtDate(doc.validTill))
                        }
                        if (isInvoice) {
                            DetailRow(
                                label = "Due Date",
                                value = if (doc.dueDate.isBlank()) "Not set" else Formatters.fmtDate(doc.dueDate)
                            )
                            if (!currentDetail.parentEstimateNo.isNullOrBlank()) {
                                DetailRow(
                                    label = "Linked Estimate",
                                    value = currentDetail.parentEstimateNo,
                                    color = SwamiNavy,
                                    isBold = true
                                )
                            }
                        }
                        if (doc.kw > 0.0) {
                            val kwFormatted = if (doc.kw % 1.0 == 0.0) doc.kw.toInt().toString() else doc.kw.toString()
                            DetailRow(
                                label = "Total System [KW & Rate]",
                                value = "$kwFormatted kW @ ${Formatters.money(doc.ratePerKw)}/kW",
                                isBold = true
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        if (doc.discount > 0.0) {
                            val grossVal = doc.taxable + doc.discount
                            DetailRow(label = "Gross Subtotal", value = Formatters.plain(grossVal))
                            DetailRow(
                                label = "Special Discount",
                                value = "- ₹ ${Formatters.plain(doc.discount)}",
                                color = SwamiGreen,
                                isBold = true
                            )
                            DetailRow(label = "Net Taxable", value = Formatters.plain(doc.taxable))
                        } else {
                            DetailRow(label = "Taxable", value = Formatters.plain(doc.taxable))
                        }
                        if (doc.gstPercent > 0) {
                            DetailRow(label = "CGST (${doc.gstPercent / 2}%)", value = Formatters.plain(doc.cgst))
                            DetailRow(label = "SGST (${doc.gstPercent / 2}%)", value = Formatters.plain(doc.sgst))
                        }
                        DetailRow(
                            label = "Total Amount",
                            value = Formatters.money(doc.total),
                            isBold = true
                        )

                        if (isInvoice) {
                            Spacer(modifier = Modifier.height(4.dp))
                            DetailRow(
                                label = "Amount Paid",
                                value = Formatters.money(currentDetail.totalPaid),
                                color = SwamiGreen
                            )
                            DetailRow(
                                label = "PENDING BALANCE",
                                value = Formatters.money(currentDetail.pending),
                                isBold = true,
                                color = if (currentDetail.pending > 0.01) SwamiRed else SwamiGreen
                            )
                        }
                    }
                }
            }

            // Letterhead & Issuing Firm Card
            item {
                val assignedFirm = businessProfiles.firstOrNull { it.id == doc.businessId } ?: activeBusinessProfile
                val firmName = assignedFirm?.brandName?.takeIf { it.isNotBlank() } ?: assignedFirm?.legalName?.takeIf { it.isNotBlank() } ?: "Default Firm"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Apartment,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Printing Letterhead / Firm", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                                    Text(firmName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { showFirmSwitchDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Change", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                    }
                                }
                                Surface(
                                    color = Color(0xFFFFF7ED),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { showFirmEditDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                                    }
                                }
                            }
                        }

                        if (!assignedFirm?.address.isNullOrBlank() || !assignedFirm?.gstin.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            val subText = listOfNotNull(
                                assignedFirm?.address?.takeIf { it.isNotBlank() },
                                assignedFirm?.gstin?.takeIf { it.isNotBlank() }?.let { "GSTIN: $it" },
                                assignedFirm?.bankAccountNo?.takeIf { it.isNotBlank() }?.let { "A/c: $it" }
                            ).joinToString(" • ")
                            Text(
                                text = subText,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Primary Action Buttons (PDF Preview & WhatsApp Share)
            item {
                val docFirm = businessProfiles.firstOrNull { it.id == currentDetail.doc.businessId } ?: activeBusinessProfile
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showPdfPreviewDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BizNavy, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Preview PDF", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color.White)
                        }
                        Button(
                            onClick = {
                                whatsAppDialogType = if (isInvoice) com.example.utils.ShareHelper.TemplateType.INVOICE else com.example.utils.ShareHelper.TemplateType.ESTIMATE
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WhatsApp PDF", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color.White)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val firmProfile = businessProfiles.firstOrNull { it.id == currentDetail.doc.businessId } ?: activeBusinessProfile
                                val printed = ThermalPrinterHelper.printReceipt(context, currentDetail, currentDetail.items, firmProfile)
                                if (printed) {
                                    Toast.makeText(context, "🖨️ Receipt printed via Bluetooth thermal printer!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Bluetooth thermal printer not connected. Please pair in Settings.", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Thermal Print", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }

                        if (isInvoice) {
                            OutlinedButton(
                                onClick = { showDatePickerDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (doc.dueDate.isBlank()) "Due Date" else "Due: ${Formatters.fmtDate(doc.dueDate)}", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            }
                        }
                    }

                    if (isInvoice && currentDetail.pending > 0.01 && currentDetail.customerMobile.isNotBlank()) {
                        Button(
                            onClick = {
                                // ⚡ 1-Tap Quick WhatsApp Reminder (no dialog)
                                val docFirm = businessProfiles.firstOrNull { it.id == currentDetail.doc.businessId } ?: activeBusinessProfile
                                val firmName = docFirm?.brandName?.takeIf { it.isNotBlank() } ?: docFirm?.legalName?.takeIf { it.isNotBlank() } ?: "Swami Solar"
                                val success = ShareHelper.sendQuickPaymentReminder(
                                    context = context,
                                    customerName = currentDetail.customerName,
                                    customerMobile = currentDetail.customerMobile,
                                    docNo = doc.docNo,
                                    totalAmount = doc.total,
                                    pendingAmount = currentDetail.pending,
                                    dueDate = if (doc.dueDate.isBlank()) "" else Formatters.fmtDate(doc.dueDate),
                                    upiVpa = docFirm?.upiVpa,
                                    firmName = firmName
                                )
                                if (success) {
                                    Toast.makeText(context, "⚡ WhatsApp Payment Reminder sent!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Unable to open WhatsApp for ${currentDetail.customerMobile}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            border = BorderStroke(1.5.dp, SwamiRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SwamiRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("⚡ Quick WhatsApp Reminder (1-Tap)", color = SwamiRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Dynamic UPI QR Code Card (per-invoice, NPCI-compliant)
            item {
                val docFirm = businessProfiles.firstOrNull { it.id == currentDetail.doc.businessId } ?: activeBusinessProfile
                val upiVpa = docFirm?.upiVpa?.takeIf { it.isNotBlank() }
                if (isInvoice && !upiVpa.isNullOrBlank() && currentDetail.pending > 0.01) {
                    val firmName = docFirm?.brandName?.takeIf { it.isNotBlank() } ?: docFirm?.legalName ?: "Swami Solar"
                    val qrBitmap = remember(upiVpa, doc.docNo, currentDetail.pending) {
                        DynamicUpiQrGenerator.generateInvoicePaymentQr(
                            upiVpa = upiVpa,
                            firmName = firmName,
                            invoiceNo = doc.docNo,
                            invoiceTotal = currentDetail.pending
                        )
                    }
                    if (qrBitmap != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Dynamic UPI Payment QR", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = com.example.ui.theme.BizNavy)
                                        Text("Scan using PhonePe / GPay / Paytm", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                                    ) {
                                        Text("NPCI Compliant", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "NPCI UPI Payment QR Code",
                                    modifier = Modifier.size(160.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Amount: ₹${Formatters.plain(currentDetail.pending)} • Ref: ${doc.docNo}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "UPI VPA: $upiVpa",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Convert to Invoice Banner (for Estimates) - Ultra Eye-Catchy Button
            if (!isInvoice && doc.status != DocStatus.CONVERTED) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.convertToInvoice(doc.id) { newInvoiceId ->
                                    Toast.makeText(context, "Invoice created successfully!", Toast.LENGTH_SHORT).show()
                                    onNavigateToLinkedDoc(newInvoiceId)
                                }
                            }
                            .shadow(4.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SwamiGreen)
                    ) {
                        Row(
                            modifier = Modifier
                                .background(
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(SwamiGreen, Color(0xFF047857))
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CUSTOMER ACCEPTED → CREATE INVOICE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Lock quoted price & generate official tax invoice (Bill)",
                                    fontSize = 11.5.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Linked Document Banner
            if (isInvoice && doc.parentEstimateId != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToLinkedDoc(doc.parentEstimateId) },
                        colors = CardDefaults.cardColors(containerColor = SwamiNavy.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = SwamiNavy)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Created from Estimate", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Tap to view the source estimate", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            } else if (!isInvoice && childInvoice != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToLinkedDoc(childInvoice!!.id) },
                        colors = CardDefaults.cardColors(containerColor = SwamiGreen.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SwamiGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Converted to Invoice: ${childInvoice!!.docNo}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Tap to view active tax invoice (Bill) and payment receipts", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }

            // Payments Section (Invoices only) - Ultra Eye-Catchy Add Receipt / Payment Button
            if (isInvoice) {
                if (currentDetail.pending > 0.01) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPaymentDialog = true }
                                .shadow(4.dp, RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SwamiNavy)
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            listOf(SwamiNavy, MaterialTheme.colorScheme.primary)
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(SwamiGold.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = SwamiGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "+ ADD RECEIPT [RECORD PAYMENT]",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Record Cash / UPI / NEFT & update balance (${Formatters.money(currentDetail.pending)} due)",
                                        fontSize = 11.5.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = SwamiGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Payment Receipts & History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentDetail.pending > 0.01) {
                                Text(
                                    text = "Pending: ${Formatters.money(currentDetail.pending)}",
                                    fontSize = 12.sp,
                                    color = SwamiRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "Fully Paid (Nil Balance)",
                                    fontSize = 12.sp,
                                    color = SwamiGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (currentDetail.payments.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = "No payments recorded yet. Tap '+ ADD RECEIPT' above to record cash, UPI, cheque, or bank transfer and create instant receipt.",
                                fontSize = 12.5.sp,
                                modifier = Modifier.padding(14.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(currentDetail.payments) { payment ->
                        var menuExpanded by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(SwamiGreen.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SwamiGreen, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = Formatters.money(payment.amount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.5.sp,
                                            color = SwamiGreen
                                        )
                                        val refPart = if (payment.reference.isNotBlank()) " • Ref: ${payment.reference}" else ""
                                        Text(
                                            text = "${Formatters.fmtDate(payment.payDate)} • ${payment.mode}$refPart",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (payment.note.isNotBlank()) {
                                            Text(
                                                text = payment.note,
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Box {
                                        IconButton(onClick = { menuExpanded = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Payment actions")
                                        }
                                        DropdownMenu(
                                            expanded = menuExpanded,
                                            onDismissRequest = { menuExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Delete Payment", color = SwamiRed) },
                                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = SwamiRed) },
                                                onClick = {
                                                    menuExpanded = false
                                                    viewModel.deletePayment(payment) {
                                                        Toast.makeText(context, "Payment deleted", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Direct One-Tap Action Buttons for Payment Receipts
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val comp = company ?: Company()
                                            val docFirm = businessProfiles.firstOrNull { it.id == currentDetail.doc.businessId } ?: activeBusinessProfile
                                            val pdf = PdfGenerator.generateReceiptPdf(context, currentDetail, payment, comp, business = docFirm, themeStr = selectedPdfTheme)
                                            ShareHelper.viewPdf(context, pdf)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = SwamiNavy, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("View Receipt", fontSize = 11.5.sp, color = SwamiNavy, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            selectedReceiptPayment = payment
                                            whatsAppDialogType = com.example.utils.ShareHelper.TemplateType.RECEIPT
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("WhatsApp Receipt", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Items breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (doc.isConstruction) {
                            Text(
                                text = "Swami Construction Work Items",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SwamiNavy
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            currentDetail.items.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${idx + 1}. ${item.description.ifBlank { "Civil Work" }}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val unitStr = if (item.unit.isNotBlank()) " ${item.unit}" else ""
                                        Text(
                                            text = "Rate: ₹${Formatters.plain(item.rate)} × ${item.qty}$unitStr",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "₹${Formatters.plain(item.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = SwamiNavy
                                    )
                                }
                                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        } else {
                            val watts = (doc.kw * 1000).toInt()
                            Text(
                                text = "$watts w solar roof top on Grid System",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            currentDetail.items.forEach { item ->
                                val desc = item.description.replace("{KW}", "$watts W")
                                Text(
                                    text = "${item.label}] $desc",
                                    fontSize = 12.5.sp,
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (doc.note.isNotBlank()) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = "Note: ${doc.note}",
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }

            // Terms & Conditions Card (8 Numbered Clauses)
            val activeTerms = if (doc.terms.isNotBlank()) {
                doc.terms
            } else if (!isInvoice) {
                com.example.data.model.DEFAULT_ESTIMATE_TERMS
            } else {
                ""
            }

            if (activeTerms.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = SwamiNavy,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Terms & Conditions",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SwamiNavy
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        editingTermsText = activeTerms
                                        showEditTermsDialog = true
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = SwamiNavy, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit Terms", fontSize = 12.sp, color = SwamiNavy, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val termsLines = activeTerms.split("\n").filter { it.isNotBlank() }
                            termsLines.forEachIndexed { index, term ->
                                val cleanText = term.trim().removePrefix("${index + 1}.").trim().removePrefix("${index + 1})").trim()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(SwamiNavy.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SwamiNavy
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (cleanText.isEmpty()) term else cleanText,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155),
                                        lineHeight = 16.5.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (index < termsLines.size - 1) {
                                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Add Payment Dialog
    if (showPaymentDialog) {
        AddPaymentDialog(
            balanceDue = currentDetail.pending,
            onDismiss = { showPaymentDialog = false },
            onSave = { amt, mode, date, ref, note ->
                showPaymentDialog = false
                val payment = Payment(
                    docId = doc.id,
                    payDate = date,
                    amount = amt,
                    mode = mode,
                    reference = ref,
                    note = note
                )
                viewModel.addPayment(payment) {
                    Toast.makeText(context, "Payment recorded: ${Formatters.money(amt)}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete ${doc.docNo}?") },
            text = { Text("This will permanently delete this document and any recorded payments. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteDoc(doc) {
                            Toast.makeText(context, "Document deleted", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwamiRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Date Picker Dialog for Due Date
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val newDue = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
                        viewModel.updateDueDate(doc.id, newDue)
                        Toast.makeText(context, "Due date updated to ${Formatters.fmtDate(newDue)}", Toast.LENGTH_SHORT).show()
                    }
                    showDatePickerDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (whatsAppDialogType != null) {
        val type = whatsAppDialogType!!
        val payAmt = selectedReceiptPayment?.amount ?: 0.0
        val docFirm = businessProfiles.firstOrNull { it.id == currentDetail.doc.businessId } ?: activeBusinessProfile
        com.example.ui.components.WhatsAppShareDialog(
            detail = currentDetail,
            type = type,
            amount = payAmt,
            onDismiss = {
                whatsAppDialogType = null
                selectedReceiptPayment = null
            },
            onSendWhatsApp = { msg, isWithPdf ->
                if (type == com.example.utils.ShareHelper.TemplateType.RECEIPT && selectedReceiptPayment != null) {
                    if (isWithPdf) {
                        val comp = company ?: com.example.data.model.Company()
                        val pdf = com.example.utils.PdfGenerator.generateReceiptPdf(context, currentDetail, selectedReceiptPayment!!, comp, business = docFirm, themeStr = selectedPdfTheme)
                        com.example.utils.ShareHelper.sharePdf(context, pdf, msg, targetWhatsAppDirectly = true)
                    } else {
                        com.example.utils.ShareHelper.openWhatsAppChat(context, currentDetail.customerMobile, msg)
                    }
                } else if (type == com.example.utils.ShareHelper.TemplateType.REMINDER) {
                    com.example.utils.ShareHelper.openWhatsAppChat(context, currentDetail.customerMobile, msg)
                } else {
                    if (isWithPdf) {
                        val comp = company ?: com.example.data.model.Company()
                        val pdf = com.example.utils.PdfGenerator.generatePdf(context, currentDetail, comp, business = docFirm, themeStr = selectedPdfTheme)
                        viewModel.savePdfPath(currentDetail.doc.id, pdf.absolutePath)
                        com.example.utils.ShareHelper.sharePdf(context, pdf, msg, targetWhatsAppDirectly = true)
                    } else {
                        com.example.utils.ShareHelper.openWhatsAppChat(context, currentDetail.customerMobile, msg)
                    }
                }
                whatsAppDialogType = null
                selectedReceiptPayment = null
            }
        )
    }

    // Switch Document Firm Dialog
    if (showFirmSwitchDialog) {
        val currentFirmId = currentDetail.doc.businessId.takeIf { it > 0 } ?: activeBusinessId
        AlertDialog(
            onDismissRequest = { showFirmSwitchDialog = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Apartment, contentDescription = null, tint = Color(0xFF2563EB))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Letterhead / Firm", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SwiggyDark)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(businessProfiles) { biz ->
                        val isSelected = biz.id == currentFirmId
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showFirmSwitchDialog = false
                                    viewModel.updateDocBusiness(currentDetail.doc.id, biz.id) {
                                        viewModel.selectDoc(currentDetail.doc.id)
                                        Toast.makeText(context, "Assigned to ${biz.brandName.ifBlank { biz.legalName }}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        biz.brandName.ifBlank { biz.legalName },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "${biz.category} • ${biz.city.ifBlank { "Dhule" }}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFirmSwitchDialog = false }) {
                    Text("Cancel", color = SwiggyGray)
                }
            }
        )
    }

    // Edit Document Firm Details Dialog
    if (showFirmEditDialog) {
        val firmToEdit = businessProfiles.firstOrNull { it.id == currentDetail.doc.businessId }
            ?: activeBusinessProfile
            ?: BusinessProfile(legalName = "", brandName = "", address = "", city = "Dhule", state = "Maharashtra", pincode = "424306", gstin = null, pan = null, bankAccountNo = null, ifsc = null, upiVpa = null, signatoryName = null, logoPath = null, signaturePath = null, businessTemplateId = 3L)
        com.example.ui.components.BusinessProfileEditDialog(
            profile = firmToEdit,
            onDismiss = { showFirmEditDialog = false },
            onSave = { updated ->
                showFirmEditDialog = false
                viewModel.saveBusinessProfile(updated) {
                    viewModel.selectDoc(currentDetail.doc.id)
                    Toast.makeText(context, "Firm details updated", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Real-Time PDF Preview Dialog
    if (showPdfPreviewDialog) {
        val docFirm = businessProfiles.firstOrNull { it.id == currentDetail.doc.businessId } ?: activeBusinessProfile
        com.example.ui.components.PdfPreviewDialog(
            detail = currentDetail,
            company = company,
            activeFirm = docFirm,
            initialTheme = selectedPdfTheme,
            onDismiss = { showPdfPreviewDialog = false },
            onShareWhatsApp = { pdfFile, shareMsg ->
                showPdfPreviewDialog = false
                viewModel.trackWhatsAppShare(currentDetail.doc.id)
                ShareHelper.sharePdf(context, pdfFile, shareMsg)
            }
        )
    }

    // Quick Edit Terms & Conditions Dialog
    if (showEditTermsDialog) {
        AlertDialog(
            onDismissRequest = { showEditTermsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = SwamiNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Terms & Conditions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "Update the terms for ${doc.docNo}:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = editingTermsText,
                        onValueChange = { editingTermsText = it },
                        minLines = 8,
                        maxLines = 14,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                editingTermsText = com.example.data.model.DEFAULT_ESTIMATE_TERMS
                            }
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp), tint = SwamiGreen)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Standard Terms", fontSize = 11.5.sp, color = SwamiGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditTermsDialog = false
                        viewModel.updateDocTerms(doc.id, editingTermsText.trim()) {
                            viewModel.selectDoc(doc.id)
                            Toast.makeText(context, "Terms updated successfully", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwamiNavy, contentColor = Color.White)
                ) {
                    Text("Save Terms", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTermsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DocumentLifecycleTracker(
    doc: com.example.data.model.Doc,
    currentDetail: com.example.data.model.DocWithDetails,
    childInvoice: com.example.data.model.Doc? = null,
    onNavigateToLinkedDoc: (Int) -> Unit = {},
    onAddPaymentClick: () -> Unit = {},
    onConvertClick: () -> Unit = {}
) {
    val isConst = doc.isConstruction
    val isInvoice = doc.isInvoice
    val status = currentDetail.computedStatus

    if (isConst) {
        // --- SEPARATE LIFECYCLE 1: SWAMI CONSTRUCTION CIVIL ESTIMATE ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Construction Civil Lifecycle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        "Civil BOQ & Pricing",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LifecycleStep(
                        title = "1. Site BOQ",
                        subtitle = "Dimensions",
                        isDone = true,
                        isActive = false,
                        activeColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LifecycleLine(isActive = true, activeColor = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    LifecycleStep(
                        title = "2. Costing",
                        subtitle = "Civil Rates",
                        isDone = true,
                        isActive = false,
                        activeColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LifecycleLine(isActive = true, activeColor = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    LifecycleStep(
                        title = "3. Quotation",
                        subtitle = "Formal BOQ",
                        isDone = true,
                        isActive = true,
                        activeColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        // --- SEPARATE LIFECYCLE 2: SWAMI SOLAR (ESTIMATE -> INVOICE -> PAYMENTS -> DONE) ---
        val step1Done = true
        val step2Done = isInvoice || status == com.example.data.model.DocStatus.CONVERTED || childInvoice != null
        val step3Done = isInvoice && (currentDetail.totalPaid > 0.01 || status == com.example.data.model.DocStatus.PARTIAL || status == com.example.data.model.DocStatus.PAID)
        val step4Done = isInvoice && status == com.example.data.model.DocStatus.PAID

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BizNavy.copy(alpha = 0.04f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(com.example.ui.theme.BizNavy)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Business Document Lifecycle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = com.example.ui.theme.BizNavy
                        )
                    }
                    Text(
                        when {
                            step4Done -> "Settled & Complete"
                            step3Done -> "Partially Paid"
                            isInvoice -> "Invoice Issued"
                            step2Done -> "Converted to Invoice"
                            else -> "Quotation Stage"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (step4Done) SwamiGreen else com.example.ui.theme.BizNavy
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step 1: Solar Estimate
                    LifecycleStep(
                        title = "1. Estimate",
                        subtitle = if (!isInvoice) "Active" else "Quotation",
                        isDone = step1Done,
                        isActive = !isInvoice,
                        onClick = if (isInvoice && doc.parentEstimateId != null) {
                            { onNavigateToLinkedDoc(doc.parentEstimateId) }
                        } else null
                    )
                    LifecycleLine(isActive = step2Done, modifier = Modifier.weight(1f))

                    // Step 2: Tax Invoice
                    LifecycleStep(
                        title = "2. Invoice",
                        subtitle = if (isInvoice) "Active" else if (childInvoice != null) "View Bill" else "Create Bill",
                        isDone = step2Done,
                        isActive = isInvoice && !step3Done,
                        onClick = when {
                            !isInvoice && childInvoice != null -> {
                                { onNavigateToLinkedDoc(childInvoice.id) }
                            }
                            !isInvoice && childInvoice == null -> onConvertClick
                            else -> null
                        }
                    )
                    LifecycleLine(isActive = step3Done, modifier = Modifier.weight(1f))

                    // Step 3: Payments / Receipts
                    LifecycleStep(
                        title = "3. Payment",
                        subtitle = if (isInvoice && currentDetail.pending > 0.01) "+ Add" else if (step3Done) "Recorded" else "Pending",
                        isDone = step3Done,
                        isActive = isInvoice && currentDetail.pending > 0.01,
                        onClick = if (isInvoice && currentDetail.pending > 0.01) onAddPaymentClick else null
                    )
                    LifecycleLine(isActive = step4Done, modifier = Modifier.weight(1f))

                    // Step 4: Fully Settled
                    LifecycleStep(
                        title = "4. Settled",
                        subtitle = if (step4Done) "Nil Balance" else "Final Due",
                        isDone = step4Done,
                        isActive = step4Done
                    )
                }
            }
        }
    }
}

@Composable
fun LifecycleStep(
    title: String,
    subtitle: String,
    isDone: Boolean,
    isActive: Boolean,
    activeColor: Color = SwamiGreen,
    onClick: (() -> Unit)? = null
) {
    val color = when {
        isActive -> activeColor
        isDone -> SwamiNavy.copy(alpha = 0.6f)
        else -> Color.LightGray
    }

    val isDoneIcon = isDone && !isActive

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        if (isDoneIcon) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(if (isActive) color else Color.Transparent, CircleShape)
                    .padding(2.dp)
                    .background(if (isActive) Color.White else Color.LightGray, CircleShape)
            ) {
                if (isActive) {
                    Box(modifier = Modifier.fillMaxSize().padding(3.dp).background(color, CircleShape))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontSize = 10.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, color = color)
        Text(subtitle, fontSize = 9.sp, color = color.copy(alpha = 0.85f))
    }
}

@Composable
fun LifecycleLine(isActive: Boolean, modifier: Modifier, activeColor: Color = SwamiGreen) {
    Box(
        modifier = modifier
            .height(2.dp)
            .background(if (isActive) activeColor.copy(alpha = 0.6f) else Color.LightGray.copy(alpha = 0.4f))
            .padding(horizontal = 2.dp)
    )
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isBold) 14.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = if (isBold) 15.sp else 13.5.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
        )
    }
}
