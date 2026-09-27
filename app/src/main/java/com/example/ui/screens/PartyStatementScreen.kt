package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocType
import com.example.data.model.LedgerEntry
import com.example.data.model.LedgerTransactionType
import com.example.data.model.PartyCategory
import com.example.data.model.PartyStatementData
import com.example.ui.components.AddPaymentDialog
import com.example.ui.theme.SwamiGold
import com.example.ui.theme.SwamiGreen
import com.example.ui.theme.SwamiNavy
import com.example.ui.theme.SwiggyBorder
import com.example.ui.theme.SwiggyDark
import com.example.ui.theme.SwiggyGray
import com.example.ui.theme.SwiggyOrange
import com.example.ui.theme.SwiggyOrangeLight
import com.example.ui.viewmodel.BillingViewModel
import com.example.utils.Formatters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class StatementDateFilter(val label: String) {
    ALL("All Time"),
    THIS_MONTH("This Month"),
    LAST_30_DAYS("Last 30 Days"),
    THIS_YEAR("This Year")
}

enum class StatementTypeFilter(val label: String) {
    ALL("All Transactions"),
    INVOICES("Invoices Only"),
    PAYMENTS("Payments Only")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyStatementScreen(
    partyId: Int,
    viewModel: BillingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDocDetail: (Int) -> Unit = {},
    onNavigateToNewDoc: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val statement: PartyStatementData? by viewModel.getPartyStatementFlow(partyId).collectAsState(initial = null)

    var dateFilter by remember { mutableStateOf(StatementDateFilter.ALL) }
    var typeFilter by remember { mutableStateOf(StatementTypeFilter.ALL) }
    var showQuickPaymentDialog by remember { mutableStateOf(false) }

    if (statement == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading Party Statement...", color = SwiggyGray)
        }
        return
    }

    val st: PartyStatementData = statement!!
    val party = st.party
    val phoneToCall = party.mobile.ifBlank { party.phone }

    // Filter transactions based on date and type
    val filteredEntries: List<LedgerEntry> = remember(st.entries, dateFilter, typeFilter) {
        val now = Calendar.getInstance()
        val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now.time)

        val monthStartCal = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
        val monthStartIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(monthStartCal.time)

        val last30DaysCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
        val last30DaysIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(last30DaysCal.time)

        val yearStartCal = Calendar.getInstance().apply {
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val yearStartIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(yearStartCal.time)

        st.entries.filter { entry ->
            val dateMatches = when (dateFilter) {
                StatementDateFilter.ALL -> true
                StatementDateFilter.THIS_MONTH -> entry.date >= monthStartIso
                StatementDateFilter.LAST_30_DAYS -> entry.date >= last30DaysIso
                StatementDateFilter.THIS_YEAR -> entry.date >= yearStartIso
            }

            val typeMatches = when (typeFilter) {
                StatementTypeFilter.ALL -> true
                StatementTypeFilter.INVOICES -> entry.type == LedgerTransactionType.INVOICE
                StatementTypeFilter.PAYMENTS -> entry.type == LedgerTransactionType.PAYMENT_RECEIVED
            }

            dateMatches && typeMatches
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = party.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Party Statement & Account Ledger",
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Call
                    val phoneToCall = party.mobile.ifBlank { party.phone }
                    if (phoneToCall.isNotBlank()) {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall"))
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                        }
                    }

                    // Share Statement PDF
                    IconButton(onClick = {
                        viewModel.generateAndSharePartyStatementPdf(context, st)
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Share PDF Statement", tint = Color.White)
                    }

                    // WhatsApp Share
                    IconButton(onClick = {
                        val shareText = buildString {
                            appendLine("📊 *STATEMENT OF ACCOUNT - ${party.name.uppercase(Locale.US)}*")
                            appendLine("Date: ${Formatters.todayIso()}")
                            appendLine("━━━━━━━━━━━━━━━━━━━")
                            appendLine("• Total Billed (Dr): ₹ ${Formatters.formatInr(st.totalDebit)}")
                            appendLine("• Total Paid (Cr):   ₹ ${Formatters.formatInr(st.totalCredit)}")
                            if (st.isReceivable) {
                                appendLine("• *Net Balance Due: ₹ ${Formatters.formatInr(st.netBalance)} Dr (Pending)*")
                            } else if (st.isPayable) {
                                appendLine("• *Advance Balance: ₹ ${Formatters.formatInr(kotlin.math.abs(st.netBalance))} Cr*")
                            } else {
                                appendLine("• *Balance: ₹ 0.00 (All Settled)*")
                            }
                            appendLine("━━━━━━━━━━━━━━━━━━━")
                            appendLine("Transactions Recorded: ${st.entries.size}")
                            appendLine("\nPlease contact us for any discrepancies.")
                        }

                        val cleanPhone = phoneToCall.replace(Regex("[^0-9]"), "")
                        val finalPhone = when {
                            cleanPhone.length == 10 -> "91$cleanPhone"
                            cleanPhone.startsWith("0") && cleanPhone.length == 11 -> "91${cleanPhone.substring(1)}"
                            else -> cleanPhone
                        }

                        val uri = if (finalPhone.isNotBlank()) {
                            Uri.parse("https://api.whatsapp.com/send?phone=$finalPhone&text=${Uri.encode(shareText)}")
                        } else {
                            Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(shareText)}")
                        }
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (_: Exception) {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Statement"))
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share WhatsApp", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwamiNavy,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Sticky Action Bar
            Surface(
                shadowElevation = 10.dp,
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // + Record Payment In
                    Button(
                        onClick = { showQuickPaymentDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SwamiGreen)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Payment In", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // + New Bill / Invoice
                    Button(
                        onClick = { onNavigateToNewDoc(DocType.INVOICE) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SwiggyOrange)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ New Bill", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Reminder Button (if receivable)
                    if (st.isReceivable && phoneToCall.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val cleanPhone = phoneToCall.replace(Regex("[^0-9]"), "")
                                val finalPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                                val reminderText = "Dear ${party.name},\nThis is a gentle payment reminder regarding the outstanding balance of ₹ ${Formatters.formatInr(st.netBalance)}. Kindly settle the pending dues at your earliest convenience.\nThank you!"
                                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$finalPhone&text=${Uri.encode(reminderText)}")
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFEF2F2), CircleShape)
                        ) {
                            Icon(Icons.Default.NotificationImportant, contentDescription = "Send Reminder", tint = Color(0xFFDC2626))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Party Overview Header Card
            item {
                PartyProfileCard(
                    party = party,
                    statement = st,
                    onPreviewPdf = { viewModel.previewPartyStatementPdf(context, st) }
                )
            }

            // 2. Ledger KPI Summary Card
            item {
                LedgerSummaryKpiCard(statement = st)
            }

            // 3. Filter Chips (Date & Type)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Date Filter Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatementDateFilter.values().forEach { df ->
                            val isSelected = dateFilter == df
                            FilterChip(
                                selected = isSelected,
                                onClick = { dateFilter = df },
                                label = { Text(df.label, fontSize = 11.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SwiggyOrangeLight,
                                    selectedLabelColor = SwiggyOrange
                                )
                            )
                        }
                    }

                    // Type Filter Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatementTypeFilter.values().forEach { tf ->
                            val isSelected = typeFilter == tf
                            FilterChip(
                                selected = isSelected,
                                onClick = { typeFilter = tf },
                                label = { Text(tf.label, fontSize = 11.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE2E8F0),
                                    selectedLabelColor = SwamiNavy
                                )
                            )
                        }
                    }
                }
            }

            // 4. Ledger Transaction Table Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions (${filteredEntries.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SwiggyDark
                    )
                    Text(
                        text = "Running Khata Balance",
                        fontSize = 11.sp,
                        color = SwiggyGray
                    )
                }
            }

            // 5. Transaction Rows
            if (filteredEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SwiggyBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = SwiggyGray,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Ledger Entries",
                                fontWeight = FontWeight.Bold,
                                color = SwiggyDark,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Transactions, sales bills, or payments for ${party.name} will appear here.",
                                fontSize = 12.sp,
                                color = SwiggyGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredEntries, key = { it.id }) { entry ->
                    LedgerEntryCard(
                        entry = entry,
                        onClick = {
                            if (entry.docId != null) {
                                onNavigateToDocDetail(entry.docId)
                            }
                        }
                    )
                }
            }
        }
    }

    // Quick Payment In Dialog
    if (showQuickPaymentDialog) {
        val pendingBalance = if (st.isReceivable) st.netBalance else 0.0
        AddPaymentDialog(
            balanceDue = pendingBalance,
            onDismiss = { showQuickPaymentDialog = false },
            onSave = { amount, mode, date, ref, note ->
                viewModel.recordQuickPaymentForParty(
                    partyId = partyId,
                    amount = amount,
                    mode = mode,
                    date = date,
                    ref = ref,
                    note = note,
                    onResult = {
                        showQuickPaymentDialog = false
                    }
                )
            }
        )
    }
}

@Composable
private fun PartyProfileCard(
    party: com.example.data.model.Party,
    statement: PartyStatementData,
    onPreviewPdf: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initial Avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            when (party.category) {
                                PartyCategory.CUSTOMER -> Color(0xFFEFF6FF)
                                PartyCategory.VENDOR -> Color(0xFFFAF5FF)
                                PartyCategory.BOTH -> Color(0xFFF0FDF4)
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = party.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = when (party.category) {
                            PartyCategory.CUSTOMER -> Color(0xFF2563EB)
                            PartyCategory.VENDOR -> Color(0xFF7C3AED)
                            PartyCategory.BOTH -> Color(0xFF059669)
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = party.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SwiggyDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Category Pill
                        val catBg = when (party.category) {
                            PartyCategory.CUSTOMER -> Color(0xFFDBEAFE)
                            PartyCategory.VENDOR -> Color(0xFFF3E8FF)
                            PartyCategory.BOTH -> Color(0xFFDCFCE7)
                        }
                        val catText = when (party.category) {
                            PartyCategory.CUSTOMER -> Color(0xFF1D4ED8)
                            PartyCategory.VENDOR -> Color(0xFF6B21A8)
                            PartyCategory.BOTH -> Color(0xFF15803D)
                        }
                        Box(
                            modifier = Modifier
                                .background(catBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = party.category.name,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = catText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    val phoneStr = party.mobile.ifBlank { party.phone }
                    if (phoneStr.isNotBlank()) {
                        Text(text = "📱 $phoneStr", fontSize = 12.sp, color = SwiggyGray)
                    }

                    val locStr = listOf(party.village, party.city).filter { it.isNotBlank() }.joinToString(", ")
                    if (locStr.isNotBlank()) {
                        Text(text = "📍 $locStr", fontSize = 11.5.sp, color = SwiggyGray)
                    }

                    if (party.gstin.isNotBlank()) {
                        Text(text = "GSTIN: ${party.gstin}", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    }
                }

                // PDF Preview Button
                OutlinedButton(
                    onClick = onPreviewPdf,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SwamiNavy),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = SwamiNavy, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 11.5.sp, color = SwamiNavy, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LedgerSummaryKpiCard(statement: PartyStatementData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Net Balance Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Net Balance",
                        fontSize = 12.sp,
                        color = SwiggyGray,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹ ${Formatters.formatInr(kotlin.math.abs(statement.netBalance))}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                statement.isReceivable -> Color(0xFFDC2626) // Red = To Collect
                                statement.isPayable -> Color(0xFF7C3AED) // Purple = Advance / To Pay
                                else -> Color(0xFF059669) // Green = All Settled
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Status Badge
                        val (statusText, statusBg, statusColor) = when {
                            statement.isReceivable -> Triple("To Collect (Dr)", Color(0xFFFEE2E2), Color(0xFFDC2626))
                            statement.isPayable -> Triple("Advance (Cr)", Color(0xFFF3E8FF), Color(0xFF7C3AED))
                            else -> Triple("All Settled", Color(0xFFDCFCE7), Color(0xFF15803D))
                        }
                        Box(
                            modifier = Modifier
                                .background(statusBg, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (statement.isReceivable) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (statement.isReceivable) Icons.Default.ArrowDownward else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (statement.isReceivable) Color(0xFFDC2626) else Color(0xFF059669),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Sub-metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Billed (Debit)
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Total Billed (Dr)", fontSize = 11.sp, color = SwiggyGray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹ ${Formatters.formatInr(statement.totalDebit)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SwiggyDark
                    )
                    Text(
                        text = "${statement.invoicesCount} Invoices",
                        fontSize = 10.sp,
                        color = SwiggyGray
                    )
                }

                // Total Received (Credit)
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Total Paid (Cr)", fontSize = 11.sp, color = SwiggyGray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹ ${Formatters.formatInr(statement.totalCredit)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                    Text(
                        text = "${statement.paymentsCount} Payments",
                        fontSize = 10.sp,
                        color = SwiggyGray
                    )
                }

                // Opening Balance
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Opening Bal", fontSize = 11.sp, color = SwiggyGray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹ ${Formatters.formatInr(statement.openingBalance)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SwiggyDark
                    )
                    Text(
                        text = "0.00 Dr",
                        fontSize = 10.sp,
                        color = SwiggyGray
                    )
                }
            }
        }
    }
}

@Composable
private fun LedgerEntryCard(
    entry: LedgerEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Icon
            val iconBg = if (entry.isDebit) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
            val iconTint = if (entry.isDebit) Color(0xFFDC2626) else Color(0xFF059669)
            val iconVec = if (entry.isDebit) Icons.Default.ReceiptLong else Icons.Default.Payments

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVec,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.voucherNo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SwiggyDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Mode Tag if payment
                    if (!entry.paymentMode.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = entry.paymentMode,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = entry.particulars,
                    fontSize = 11.5.sp,
                    color = SwiggyGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = entry.date,
                    fontSize = 10.5.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Debit / Credit & Running Balance
            Column(horizontalAlignment = Alignment.End) {
                if (entry.isDebit) {
                    Text(
                        text = "+ ₹ ${Formatters.formatInr(entry.debit)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color(0xFFDC2626) // Debit = Billed
                    )
                } else if (entry.isCredit) {
                    Text(
                        text = "- ₹ ${Formatters.formatInr(entry.credit)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color(0xFF059669) // Credit = Received
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Bal: ₹ ${Formatters.formatInr(entry.runningBalance)} ${entry.balanceType}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
