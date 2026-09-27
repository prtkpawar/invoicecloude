package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.data.entity.BusinessProfile
import com.example.data.model.DocStatus
import com.example.data.model.DocType
import com.example.data.model.DocWithDetails
import com.example.ui.components.BusinessProfileEditDialog
import com.example.ui.components.BusinessSwitcherDialog
import com.example.ui.components.FirmManagerDialog
import com.example.ui.components.WhatsAppShareDialog
import com.example.ui.components.CurrencyText
import com.example.ui.components.pro.*
import com.example.ui.theme.*
import com.example.utils.appString
import com.example.ui.theme.pro.Spacing
import com.example.ui.viewmodel.BillingViewModel
import com.example.utils.Formatters
import com.example.utils.PdfGenerator
import com.example.utils.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BillingViewModel,
    onNavigateToNewDoc: (String) -> Unit,
    onNavigateToDocDetail: (Int) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onNavigateToPaymentReceived: () -> Unit = {},
    onNavigateToFirmEditor: (Long?) -> Unit = {},
    onNavigateToSubscription: () -> Unit = {}
) {
    val stats by viewModel.dashboardStatsState.collectAsState()
    val allEstimates by viewModel.estimatesState.collectAsState()
    val allInvoices by viewModel.invoicesState.collectAsState()
    val allConstruction by viewModel.constructionDocsState.collectAsState()
    val company by viewModel.companyState.collectAsState()
    val businessProfiles by viewModel.businessProfilesState.collectAsState()
    val activeBusinessProfile by viewModel.activeBusinessProfileState.collectAsState()
    val activeBusinessId by viewModel.activeBusinessIdState.collectAsState()
    val context = LocalContext.current

    var showBusinessSwitcher by remember { mutableStateOf(false) }
    var businessToEdit by remember { mutableStateOf<BusinessProfile?>(null) }
    var showBusinessEditDialog by remember { mutableStateOf(false) }
    var whatsAppTargetDoc by remember { mutableStateOf<DocWithDetails?>(null) }
    var whatsAppTargetType by remember { mutableStateOf<ShareHelper.TemplateType?>(null) }

    // Live search query & filter
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("ALL") }

    // Business identity
    val currentBusinessName = activeBusinessProfile?.brandName?.takeIf { it.isNotBlank() }
        ?: activeBusinessProfile?.legalName?.takeIf { it.isNotBlank() }
        ?: company?.name?.takeIf { it.isNotBlank() }
        ?: "Universal Business OS"

    val businessCity = activeBusinessProfile?.city?.takeIf { it.isNotBlank() } ?: "Main HQ"
    val businessCategory = activeBusinessProfile?.category ?: "RETAIL"
    val terminology = remember(businessCategory) { com.example.data.model.BusinessTerminologyRegistry.getForCategory(businessCategory) }

    // Combined recent transactions
    val allDocuments = remember(allInvoices, allEstimates, allConstruction, activeBusinessId) {
        (allInvoices + allEstimates + allConstruction)
            .filter { it.doc.businessId == activeBusinessId || it.doc.businessId <= 0L }
            .distinctBy { it.doc.id }
            .sortedByDescending { it.doc.createdAt }
    }

    val filteredDocuments = remember(allDocuments, searchQuery, activeFilter) {
        allDocuments.filter { item ->
            val matchesFilter = when (activeFilter) {
                "INVOICES" -> item.doc.isInvoice
                "ESTIMATES" -> !item.doc.isInvoice
                "UNPAID" -> item.doc.isInvoice && item.doc.status != DocStatus.PAID
                else -> true
            }
            if (!matchesFilter) return@filter false

            if (searchQuery.isBlank()) true
            else {
                val q = searchQuery.trim().lowercase()
                item.customer?.name?.lowercase()?.contains(q) == true ||
                item.doc.docNo.lowercase().contains(q) ||
                (item.customer?.phone ?: "").contains(q)
            }
        }
    }

    Scaffold(
        containerColor = Canvas
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // 1. Top Executive Business Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showBusinessSwitcher = true }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BizNavy,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentBusinessName.take(2).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.width(Spacing.sm))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentBusinessName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Ink900,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = BizNavy.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, BizNavy.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        "PRO",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BizNavy
                                    )
                                }
                            }
                        }
                    }
                    IconButton(onClick = { onNavigateToTab(2) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Ink600)
                    }
                }
            }

            // 2. Greeting & Status
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = appString("greeting_title"),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Ink900,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = Color(0xFF22C55E), modifier = Modifier.size(8.dp)) {}
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = appString("greeting_sub"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BizGray
                                )
                            }
                        }

                        // Developer Mode Testing Badge
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(appString("dev_mode_badge"), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF92400E))
                            }
                        }
                    }
                }
            }

            // 3. Executive Financial Overview (Hero Card)
            item {
                Card(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BizBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = appString("collected_month"),
                            style = MaterialTheme.typography.labelLarge,
                            color = BizGray
                        )
                        Spacer(Modifier.height(Spacing.xs))
                        CurrencyText(
                            text = Formatters.money(stats.receivedThisMonth),
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(Modifier.height(Spacing.lg))
                        HorizontalDivider(color = BizBorder)
                        Spacer(Modifier.height(Spacing.lg))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(appString("pending_dues"), style = MaterialTheme.typography.labelSmall, color = BizGray)
                                Text(Formatters.money(stats.totalPending), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text(appString("lifetime_billed"), style = MaterialTheme.typography.labelSmall, color = BizGray)
                                Text(Formatters.money(stats.totalInvoiced), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text(appString("open_proposals"), style = MaterialTheme.typography.labelSmall, color = BizGray)
                                Text("${allEstimates.count { it.doc.status != DocStatus.CONVERTED }}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // 4. Hero Action Button
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onNavigateToNewDoc(DocType.INVOICE) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text(
                            appString("create_invoice"),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }

                    // Analytics & WhatsApp Insights Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigateToTab(2) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF09090B)),
                        border = BorderStroke(1.dp, Color(0xFFFEF08A))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF27272A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = Color(0xFFFACC15),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "WhatsApp & D3 Analytics",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "INSIGHTS",
                                            color = Color(0xFF09090B),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            modifier = Modifier
                                                .background(Color(0xFFFACC15), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Text(
                                        text = "Invoices created & WhatsApp share conversion",
                                        color = Color(0xFFA1A1AA),
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFFFACC15)
                            )
                        }
                    }
                }
            }

            // 5. Quick actions
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(appString("quick_actions"), style = MaterialTheme.typography.labelLarge, color = BizGray)
                    Spacer(Modifier.height(Spacing.md))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                    QuickActionTile(Icons.Default.Description, appString("new_proposal"), "Create estimate", { onNavigateToNewDoc(DocType.ESTIMATE) }, Modifier.weight(1f))
                    QuickActionTile(Icons.Default.Payments, appString("record_payment"), "Mark payment", onNavigateToPaymentReceived, Modifier.weight(1f))
                    QuickActionTile(Icons.Default.People, appString("add_customer"), "New client", { onNavigateToTab(3) }, Modifier.weight(1f))
                    }
                }
            }

            // 5. Document Search & Filter Chips
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(appString("search_hint"), style = MaterialTheme.typography.bodyMedium, color = Ink400)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Ink400)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Ink400)
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Line
                        )
                    )

                    // Filter Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        val filters = listOf(
                            "ALL" to "All (${allDocuments.size})",
                            "INVOICES" to "${terminology.invoiceDocLabel}s (${allInvoices.size})",
                            "ESTIMATES" to "${terminology.estimateDocLabel}s (${allEstimates.size})",
                            "UNPAID" to "Unpaid (${stats.dueInvoices.size})"
                        )
                        items(filters) { (id, label) ->
                            val selected = activeFilter == id
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selected) Color.Black else SurfaceCard,
                                border = BorderStroke(1.dp, if (selected) Color.Black else Line),
                                modifier = Modifier.clickable { activeFilter = id }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) Color.White else Ink600,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Recent Document Items
            if (filteredDocuments.isEmpty()) {
                item {
                    ProCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.xl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = Ink400,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(Spacing.sm))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No matching documents found" else "No documents created yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = Ink900
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                text = "Create your first professional tax invoice or quotation.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink400
                            )
                        }
                    }
                }
            } else {
                items(filteredDocuments.take(15), key = { it.doc.id }) { item ->
                    UniversalDocCard(
                        doc = item,
                        onClick = { onNavigateToDocDetail(item.doc.id) },
                        onShareWhatsApp = {
                            whatsAppTargetDoc = item
                            whatsAppTargetType = if (item.doc.isInvoice) ShareHelper.TemplateType.INVOICE else ShareHelper.TemplateType.ESTIMATE
                        },
                        onOpenPdf = {
                            viewModel.previewPdf(context, item)
                        }
                    )
                }
            }
        }
    }

    // Firm Manager Hub Dialog
    if (showBusinessSwitcher) {
        FirmManagerDialog(
            businesses = businessProfiles,
            activeBusinessId = activeBusinessId,
            onSelectBusiness = { id ->
                viewModel.switchBusiness(id)
                showBusinessSwitcher = false
                Toast.makeText(context, "Active Firm switched successfully", Toast.LENGTH_SHORT).show()
            },
            onSaveBusiness = { profile ->
                viewModel.saveBusinessProfile(profile)
                Toast.makeText(context, "Firm profile saved!", Toast.LENGTH_SHORT).show()
            },
            onDeleteBusiness = { id ->
                viewModel.deleteBusinessProfile(id)
                Toast.makeText(context, "Firm archived", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showBusinessSwitcher = false }
        )
    }

    // Business Profile Edit / Add Dialog
    if (showBusinessEditDialog) {
        BusinessProfileEditDialog(
            profile = businessToEdit,
            onSave = { updated ->
                viewModel.saveBusinessProfile(updated)
                showBusinessEditDialog = false
                Toast.makeText(context, "Firm Profile Saved!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showBusinessEditDialog = false }
        )
    }

    // WhatsApp Share Dialog
    whatsAppTargetDoc?.let { targetDoc ->
        whatsAppTargetType?.let { targetType ->
            WhatsAppShareDialog(
                detail = targetDoc,
                type = targetType,
                onDismiss = {
                    whatsAppTargetDoc = null
                    whatsAppTargetType = null
                },
                onSendWhatsApp = { message, _ ->
                    val phone = targetDoc.customer?.phone ?: ""
                    val opened = if (phone.isNotBlank()) {
                        ShareHelper.openWhatsAppChat(context, phone, message)
                    } else false

                    if (!opened) {
                        ShareHelper.copyToClipboard(context, message, "Invoice/Estimate Message")
                        Toast.makeText(context, "Message copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                    whatsAppTargetDoc = null
                    whatsAppTargetType = null
                }
            )
        }
    }
}

/** Professional Universal Document Card */
@Composable
private fun UniversalDocCard(
    doc: DocWithDetails,
    onClick: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onOpenPdf: () -> Unit
) {
    val customerName = doc.customer?.name?.takeIf { it.isNotBlank() } ?: "Cash Customer"
    val docNumber = doc.doc.docNo.ifBlank { "#${doc.doc.id}" }
    val dateStr = Formatters.fmtDate(doc.doc.docDate).ifBlank { "Recent" }
    val totalFormatted = Formatters.money(doc.doc.total)

    val payStatus = when {
        doc.doc.status == DocStatus.PAID -> PayStatus.PAID
        doc.doc.status == DocStatus.PARTIAL -> PayStatus.PARTIAL
        doc.doc.status == DocStatus.ESTIMATE || !doc.doc.isInvoice -> PayStatus.DRAFT
        else -> PayStatus.UNPAID
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = SurfaceCard,
        border = BorderStroke(1.dp, Line),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Avatar(
                        name = customerName,
                        size = 38,
                        bg = Brand100,
                        fg = Brand700
                    )
                    Spacer(Modifier.width(Spacing.md))
                    Column {
                        Text(
                            text = customerName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$docNumber • $dateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = Ink400
                        )
                    }
                }

                StatusPill(status = payStatus)
            }

            Spacer(Modifier.height(Spacing.md))

            Divider(color = Line, thickness = 0.8.dp)

            Spacer(Modifier.height(Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (doc.doc.isInvoice) "Total Amount" else "Estimated Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink400
                    )
                    Text(
                        text = totalFormatted,
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink900,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick PDF action
                    IconButton(
                        onClick = onOpenPdf,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            tint = Ink600,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Quick WhatsApp action
                    IconButton(
                        onClick = onShareWhatsApp,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "WhatsApp Share",
                            tint = Success600,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Call Customer if mobile present
                    val phone = doc.customer?.phone?.takeIf { it.isNotBlank() }
                    if (phone != null) {
                        val ctx = LocalContext.current
                        IconButton(
                            onClick = {
                                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                try {
                                    ctx.startActivity(callIntent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Call,
                                contentDescription = "Call",
                                tint = Brand600,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Compact Quick Action Tile */
@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = SurfaceSoft,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Brand100),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Ink900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Ink400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
