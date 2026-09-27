package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Company
import com.example.data.model.Customer
import com.example.data.model.DocWithDetails
import com.example.data.model.Payment
import com.example.ui.components.AddPaymentDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SwamiGreen
import com.example.ui.theme.SwamiNavy
import com.example.ui.theme.SwamiRed
import com.example.ui.viewmodel.BillingViewModel
import com.example.utils.Formatters
import com.example.utils.PdfGenerator
import com.example.utils.ShareHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentEntryScreen(
    viewModel: BillingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDocDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val customers by viewModel.customersState.collectAsState()
    val invoices by viewModel.invoicesState.collectAsState()
    val estimates by viewModel.estimatesState.collectAsState()
    val company by viewModel.companyState.collectAsState()
    val businessProfiles by viewModel.businessProfilesState.collectAsState()
    val activeBusinessProfile by viewModel.activeBusinessProfileState.collectAsState()
    val activeBusinessId by viewModel.activeBusinessIdState.collectAsState()

    val allDocs: List<DocWithDetails> = remember(invoices, estimates) {
        invoices + estimates
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var activePaymentInvoice by remember { mutableStateOf<DocWithDetails?>(null) }

    // Dialog state for receipt sharing after recording payment
    var receiptDialogPayment by remember { mutableStateOf<Pair<DocWithDetails, Payment>?>(null) }

    // Filter customers who have documents
    val customerDocMap: Map<Int, List<DocWithDetails>> = remember(allDocs) {
        allDocs.groupBy { it.doc.customerId }
    }

    val customersWithDocs: List<Customer> = remember(customers, customerDocMap, searchQuery) {
        val query = searchQuery.trim().lowercase()
        customers
            .filter { cust -> customerDocMap.containsKey(cust.id) }
            .filter { cust ->
                if (query.isBlank()) true
                else cust.name.lowercase().contains(query) ||
                        cust.mobile.contains(query) ||
                        cust.village.lowercase().contains(query) ||
                        cust.address.lowercase().contains(query)
            }
            .sortedBy { it.name.lowercase() }
    }

    BackHandler(enabled = selectedCustomer != null) {
        selectedCustomer = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedCustomer != null) selectedCustomer!!.name else "Payment Received",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (selectedCustomer != null) "Select Invoice to Record Payment" else "Pick customer with active document",
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedCustomer != null) {
                            selectedCustomer = null
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwamiGreen,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            if (selectedCustomer == null) {
                // Customer selection list
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search customer who has an invoice...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (customersWithDocs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = SwamiGreen.copy(alpha = 0.5f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isBlank()) "No customers with active documents yet" else "No matching customers found",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(customersWithDocs) { cust ->
                                val docs = customerDocMap[cust.id] ?: emptyList()
                                val custInvoices = docs.filter { it.doc.isInvoice }
                                val custEstimates = docs.filter { !it.doc.isInvoice }
                                val pendingTotal = custInvoices.sumOf { it.pending }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCustomer = cust },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(SwamiGreen.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = cust.name.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = SwamiGreen,
                                                fontSize = 18.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cust.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            val loc = listOfNotNull(
                                                cust.mobile.takeIf { it.isNotBlank() },
                                                cust.village.takeIf { it.isNotBlank() }
                                            ).joinToString(" • ")
                                            if (loc.isNotBlank()) {
                                                Text(
                                                    text = loc,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                if (custInvoices.isNotEmpty()) {
                                                    val invLabel = if (custInvoices.size > 1) "${custInvoices.size} Invoices" else "1 Invoice"
                                                    Text(
                                                        text = invLabel,
                                                        fontSize = 11.sp,
                                                        color = SwamiNavy,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                if (custEstimates.isNotEmpty()) {
                                                    val estLabel = if (custEstimates.size > 1) "${custEstimates.size} Estimates" else "1 Estimate"
                                                    Text(
                                                        text = estLabel,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            if (custInvoices.isNotEmpty()) {
                                                if (pendingTotal > 0.01) {
                                                    Text(
                                                        text = "Due: ${Formatters.money(pendingTotal)}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.5.sp,
                                                        color = SwamiRed
                                                    )
                                                } else {
                                                    Text(
                                                        text = "Fully Paid",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = SwamiGreen
                                                    )
                                                }
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Customer's documents view
                val cust = selectedCustomer!!
                val docs = customerDocMap[cust.id] ?: emptyList()
                val custInvoices = docs.filter { it.doc.isInvoice }
                val custEstimates = docs.filter { !it.doc.isInvoice }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SwamiGreen.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = cust.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = SwamiGreen
                                )
                                Text(
                                    text = "Mobile: ${cust.mobile.ifBlank { "N/A" }}  •  ${cust.village.ifBlank { cust.address }}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (custInvoices.isNotEmpty()) {
                        item {
                            Text(
                                text = "Invoices to Receive Payment Against",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        items(custInvoices) { invDetail ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = invDetail.doc.docNo,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = SwamiNavy
                                            )
                                            Text(
                                                text = "Date: ${Formatters.fmtDate(invDetail.doc.docDate)}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        StatusBadge(status = invDetail.computedStatus)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Invoice Total", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(Formatters.money(invDetail.doc.total), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                        Column {
                                            Text("Received", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(Formatters.money(invDetail.totalPaid), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SwamiGreen)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Balance Due", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            val balCol = if (invDetail.pending > 0.01) SwamiRed else SwamiGreen
                                            Text(Formatters.money(invDetail.pending), fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = balCol)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (invDetail.pending > 0.01) {
                                            Button(
                                                onClick = { activePaymentInvoice = invDetail },
                                                colors = ButtonDefaults.buttonColors(containerColor = SwamiGreen),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Record Payment")
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { onNavigateToDocDetail(invDetail.doc.id) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("View Invoice")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (custEstimates.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Estimates (Not yet converted to Invoice)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        items(custEstimates) { estDetail ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = estDetail.doc.docNo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = SwamiNavy
                                        )
                                        val kwStr = if (estDetail.doc.kw % 1.0 == 0.0) estDetail.doc.kw.toInt().toString() else estDetail.doc.kw.toString()
                                        Text(
                                            text = "$kwStr kW System • ${Formatters.money(estDetail.doc.total)}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { onNavigateToDocDetail(estDetail.doc.id) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Convert to Invoice")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Payment Dialog
    if (activePaymentInvoice != null) {
        val inv = activePaymentInvoice!!
        AddPaymentDialog(
            balanceDue = inv.pending,
            onDismiss = { activePaymentInvoice = null },
            onSave = { amt, mode, date, ref, note ->
                val payment = Payment(
                    docId = inv.doc.id,
                    payDate = date,
                    amount = amt,
                    mode = mode,
                    reference = ref,
                    note = note
                )
                scope.launch {
                    viewModel.addPayment(payment) {
                        Toast.makeText(context, "Payment recorded successfully!", Toast.LENGTH_SHORT).show()
                        activePaymentInvoice = null
                        // Trigger receipt offer dialog
                        receiptDialogPayment = Pair(inv, payment)
                    }
                }
            }
        )
    }

    // Post-Payment Receipt Offering Dialog
    if (receiptDialogPayment != null) {
        val (invDetail, payment) = receiptDialogPayment!!
        val cust = invDetail.customer ?: selectedCustomer

        AlertDialog(
            onDismissRequest = { receiptDialogPayment = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = SwamiGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Payment Receipt Ready",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text("Recorded payment of ${Formatters.money(payment.amount)} for ${cust?.name ?: "Customer"}.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Would you like to send the official payment receipt PDF on WhatsApp?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val comp = company ?: Company()
                        val docFirm = businessProfiles.firstOrNull { it.id == invDetail.doc.businessId } ?: activeBusinessProfile
                        val receiptFile = PdfGenerator.generateReceiptPdf(context, invDetail, payment, comp, business = docFirm)
                        val msg = ShareHelper.formatTemplateMessage(
                            ShareHelper.DEFAULT_MSG_RECEIPT,
                            invDetail,
                            payment.amount
                        )
                        ShareHelper.sharePdf(context, receiptFile, msg, targetWhatsAppDirectly = true)
                        receiptDialogPayment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwamiGreen)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send on WhatsApp")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        val comp = company ?: Company()
                        val docFirm = businessProfiles.firstOrNull { it.id == invDetail.doc.businessId } ?: activeBusinessProfile
                        val receiptFile = PdfGenerator.generateReceiptPdf(context, invDetail, payment, comp, business = docFirm)
                        ShareHelper.viewPdf(context, receiptFile)
                        receiptDialogPayment = null
                    }) {
                        Text("View PDF")
                    }
                    TextButton(onClick = { receiptDialogPayment = null }) {
                        Text("Done")
                    }
                }
            }
        )
    }
}
