package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocStatus
import com.example.data.model.DocType
import com.example.data.model.DocWithDetails
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SwamiGold
import com.example.ui.theme.SwamiGreen
import com.example.ui.theme.SwamiNavy
import com.example.ui.theme.SwamiRed
import com.example.ui.theme.SwamiRed
import com.example.ui.viewmodel.BillingViewModel
import com.example.utils.Formatters
import com.example.utils.appString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocListScreen(
    docType: String,
    viewModel: BillingViewModel,
    onNavigateToNewDoc: () -> Unit,
    onNavigateToNewConstructionDoc: () -> Unit = {},
    onNavigateToDocDetail: (Int) -> Unit
) {
    val isInvoice = docType == DocType.INVOICE
    val isConstEstimate = docType == DocType.CONST_ESTIMATE || docType == "CONSTRUCTION"

    var currentDocTypeTab by remember { mutableStateOf(if (isInvoice) 0 else 1) }
    val activeIsInvoice = if (isConstEstimate) false else currentDocTypeTab == 0

    // Source list selection based on active tab
    val list by (when {
        isConstEstimate -> viewModel.constructionDocsState
        activeIsInvoice -> viewModel.invoicesState
        else -> viewModel.estimatesState
    }).collectAsState()

    val activeBusinessProfile by viewModel.activeBusinessProfileState.collectAsState()
    val isSolar = activeBusinessProfile?.category == "SOLAR"

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf("ALL") }
    var sortByPending by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val filters = if (activeIsInvoice) {
        listOf("ALL", DocStatus.UNPAID, DocStatus.PARTIAL, DocStatus.PAID, "OVERDUE")
    } else {
        listOf("ALL", DocStatus.ESTIMATE, DocStatus.CONVERTED)
    }

    // Filter & Search Logic
    val filteredList = remember(list, searchQuery, selectedFilter, sortByPending, isConstEstimate, isInvoice) {
        var res = list.filter { detail ->
            // Category check: Universal across all businesses
            val matchesCycle = when {
                isConstEstimate -> detail.doc.isConstruction
                isInvoice -> detail.doc.isInvoice
                else -> !detail.doc.isInvoice
            }
            if (!matchesCycle) return@filter false

            val matchesQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                detail.doc.docNo.lowercase().contains(q) ||
                        detail.customerName.lowercase().contains(q) ||
                        detail.customerMobile.contains(q) ||
                        detail.customerVillage.lowercase().contains(q) ||
                        detail.doc.total.toString().contains(q)
            }

            val matchesFilter = when (selectedFilter) {
                "ALL" -> true
                "ESTIMATES" -> detail.doc.docType == DocType.CONST_ESTIMATE || detail.doc.docType == DocType.ESTIMATE
                "INVOICES" -> detail.doc.isInvoice
                "OVERDUE" -> {
                    val today = Formatters.todayIso()
                    detail.doc.isInvoice && detail.pending > 0.01 &&
                            detail.doc.dueDate.isNotBlank() && detail.doc.dueDate < today
                }
                DocStatus.UNPAID -> detail.doc.isInvoice && detail.pending > 0.01
                DocStatus.PAID -> detail.doc.isInvoice && detail.pending <= 0.01
                else -> detail.computedStatus == selectedFilter
            }
            matchesQuery && matchesFilter
        }

        if (sortByPending) {
            res = res.sortedByDescending { it.pending }
        } else {
            res = res.sortedWith(compareByDescending<DocWithDetails> { it.doc.docDate }.thenByDescending { it.doc.id })
        }
        res
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when {
                                isConstEstimate -> "Construction & BOQ"
                                isInvoice -> appString("all_invoices")
                                else -> "Quotations & Estimates"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${activeBusinessProfile?.brandName ?: "Business"} • ${if (isConstEstimate) "Civil & Contracting" else if (isInvoice) "Sales Bills & GST" else "Offers & Estimates"}",
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.ui.theme.BizNavy,
                    titleContentColor = Color.White
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Newest first") },
                                onClick = {
                                    sortByPending = false
                                    showSortMenu = false
                                }
                            )
                            if (isInvoice || isConstEstimate) {
                                DropdownMenuItem(
                                    text = { Text("Highest pending first") },
                                    onClick = {
                                        sortByPending = true
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isConstEstimate) {
                FloatingActionButton(
                    onClick = onNavigateToNewConstructionDoc,
                    containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ New Civil BOQ",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else if (isInvoice) {
                FloatingActionButton(
                    onClick = onNavigateToNewDoc,
                    containerColor = com.example.ui.theme.BizPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSolar) "+ New Solar Invoice" else "+ " + appString("create_invoice"),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                FloatingActionButton(
                    onClick = onNavigateToNewDoc,
                    containerColor = com.example.ui.theme.BizPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSolar) "+ New Solar Quote" else "+ " + appString("new_proposal"),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF6F8FA))
        ) {
            // Search Bar
            Padding(12.dp) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(appString("search_hint")) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = if (isConstEstimate) MaterialTheme.colorScheme.onSurfaceVariant else com.example.ui.theme.SwiggyOrange
                        )
                    },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = if (isConstEstimate) MaterialTheme.colorScheme.onSurfaceVariant else com.example.ui.theme.SwiggyOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filterTag ->
                    val isSelected = selectedFilter == filterTag
                    val activeColor = if (filterTag == "OVERDUE") SwamiRed else if (isConstEstimate) MaterialTheme.colorScheme.onSurfaceVariant else com.example.ui.theme.SwiggyOrange
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filterTag },
                        label = {
                            Text(
                                text = when (filterTag) {
                                    "ALL" -> "All"
                                    "ESTIMATES" -> "Estimates / BOQ"
                                    "INVOICES" -> "Bills / Invoices"
                                    "OVERDUE" -> "⚠️ Overdue"
                                    else -> filterTag
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = activeColor,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) Color.Transparent else Color(0xFFCBD5E1),
                            selectedBorderColor = Color.Transparent,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Documents List
            if (filteredList.isEmpty()) {
                EmptyStateView(
                    icon = if (isConstEstimate) Icons.Default.Engineering else if (isInvoice) Icons.Default.ReceiptLong else Icons.Default.Description,
                    title = when {
                        isConstEstimate -> "No construction documents found"
                        isInvoice -> "No solar invoices found"
                        else -> "No solar estimates found"
                    },
                    subtitle = if (searchQuery.isNotBlank() || selectedFilter != "ALL") {
                        "Try changing your search term or filter"
                    } else {
                        when {
                            isConstEstimate -> "Tap the button below to create a Civil BOQ or Estimate"
                            isInvoice -> "Tap the button below to create a Solar Tax Invoice"
                            else -> "Tap the button below to create a Solar Quotation"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.doc.id }) { item ->
                        DocCard(
                            detail = item,
                            onClick = { onNavigateToDocDetail(item.doc.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
                    }
                }
            }
        }
    }
}

@Composable
fun DocCard(
    detail: DocWithDetails,
    onClick: () -> Unit
) {
    val doc = detail.doc
    val kwStr = if (doc.kw > 0) {
        val formattedKw = if (doc.kw % 1.0 == 0.0) doc.kw.toInt().toString() else doc.kw.toString()
        " • ⚡ $formattedKw kW"
    } else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = detail.customerName.ifEmpty { "(Customer)" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (doc.isConstruction) {
                        Spacer(modifier = Modifier.width(6.dp))
                        androidx.compose.material3.Surface(
                            color = if (doc.isInvoice) Color(0xFFE0E7FF) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (doc.isInvoice) "CIVIL BILL" else "CIVIL BOQ",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (doc.isInvoice) Color(0xFF3730A3) else Color(0xFF78350F),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                StatusBadge(status = detail.computedStatus)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${doc.docNo} • ${Formatters.fmtDate(doc.docDate)}$kwStr${if (detail.customerVillage.isNotBlank()) " • 📍 ${detail.customerVillage}" else ""}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (doc.isInvoice && doc.total > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val paidPct = ((detail.totalPaid / doc.total) * 100).coerceIn(0.0, 100.0).toInt()
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { (paidPct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (paidPct == 100) SwamiGreen else com.example.ui.theme.SwiggyOrange,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ${Formatters.money(doc.total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (doc.isInvoice && detail.pending > 0.01) {
                    Text(
                        text = "Pending: ${Formatters.money(detail.pending)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = SwamiRed
                    )
                }
            }
        }
    }
}

@Composable
private fun Padding(all: androidx.compose.ui.unit.Dp, content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(all)) {
        content()
    }
}
