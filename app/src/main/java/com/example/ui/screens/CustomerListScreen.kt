package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.PartyCategory
import com.example.ui.components.CustomerDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.SwiggyBorder
import com.example.ui.theme.SwiggyDark
import com.example.ui.theme.SwiggyGray
import com.example.ui.theme.SwiggyOrange
import com.example.ui.theme.SwamiRed
import com.example.ui.viewmodel.BillingViewModel
import com.example.utils.ShareHelper

enum class PartyFilterTab {
    ALL, CUSTOMERS, VENDORS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    viewModel: BillingViewModel,
    onCustomerSelected: ((Customer) -> Unit)? = null,
    onViewStatement: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val customers by viewModel.customersState.collectAsState()
    val activeBusinessProfile by viewModel.activeBusinessProfileState.collectAsState()

    var selectedTab by remember { mutableStateOf(PartyFilterTab.ALL) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    val tabFiltered = remember(customers, selectedTab) {
        when (selectedTab) {
            PartyFilterTab.ALL -> customers
            PartyFilterTab.CUSTOMERS -> customers.filter { it.category == PartyCategory.CUSTOMER || it.category == PartyCategory.BOTH }
            PartyFilterTab.VENDORS -> customers.filter { it.category == PartyCategory.VENDOR || it.category == PartyCategory.BOTH }
        }
    }

    val filteredCustomers = remember(tabFiltered, searchQuery) {
        if (searchQuery.isBlank()) tabFiltered
        else {
            val q = searchQuery.trim().lowercase()
            tabFiltered.filter {
                it.name.lowercase().contains(q) ||
                        it.mobile.contains(q) ||
                        it.phone.contains(q) ||
                        it.village.lowercase().contains(q) ||
                        it.city.lowercase().contains(q) ||
                        it.address.lowercase().contains(q) ||
                        it.gstin.lowercase().contains(q)
            }
        }
    }

    val totalCount = customers.size
    val customerCount = customers.count { it.category == PartyCategory.CUSTOMER || it.category == PartyCategory.BOTH }
    val vendorCount = customers.count { it.category == PartyCategory.VENDOR || it.category == PartyCategory.BOTH }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Parties & Khata", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text("${activeBusinessProfile?.brandName ?: "Business"} • Ledger & Outstandings", color = Color.White.copy(alpha = 0.85f), fontSize = 11.5.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.ui.theme.BizNavy,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    customerToEdit = null
                    showDialog = true
                },
                containerColor = com.example.ui.theme.BizPrimary,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add New Party", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .imePadding()
        ) {
            // Filter Tabs (All / Customers / Vendors)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf(
                    Triple(PartyFilterTab.ALL, "All Parties", totalCount),
                    Triple(PartyFilterTab.CUSTOMERS, "Customers", customerCount),
                    Triple(PartyFilterTab.VENDORS, "Vendors / Suppliers", vendorCount)
                )

                tabs.forEach { (tab, label, count) ->
                    val isSelected = selectedTab == tab
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) SwiggyOrange else Color(0xFFF1F5F9),
                        border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null,
                        modifier = Modifier.clickable { selectedTab = tab }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = "$count",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Search field
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, mobile, GSTIN, village...", color = Color(0xFF9CA3AF), fontSize = 13.5.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SwiggyOrange) },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SwiggyGray)
                            }
                        }
                    } else null,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SwiggyDark,
                        unfocusedTextColor = SwiggyDark,
                        focusedBorderColor = SwiggyOrange,
                        unfocusedBorderColor = SwiggyBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = SwiggyDark, fontSize = 13.5.sp),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (filteredCustomers.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.People,
                    title = "No parties found",
                    subtitle = if (searchQuery.isNotBlank()) "No matching records found" else "Tap 'Add New Party' to add customer or vendor records",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        PartyCard(
                            customer = customer,
                            onClick = {
                                if (onCustomerSelected != null) {
                                    onCustomerSelected(customer)
                                } else {
                                    onViewStatement?.invoke(customer.id)
                                }
                            },
                            onViewStatement = { onViewStatement?.invoke(customer.id) },
                            onEdit = {
                                customerToEdit = customer
                                showDialog = true
                            },
                            onDelete = {
                                customerToDelete = customer
                            },
                            onCall = { ShareHelper.dialPhone(context, customer.mobile.ifBlank { customer.phone }) },
                            onWhatsApp = { ShareHelper.openWhatsAppChat(context, customer.mobile.ifBlank { customer.phone }, "") }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showDialog) {
        CustomerDialog(
            existing = customerToEdit,
            onDismiss = { showDialog = false },
            onSave = { updated ->
                showDialog = false
                viewModel.saveCustomer(updated) {
                    Toast.makeText(context, "Party saved successfully", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Delete Party?", fontWeight = FontWeight.Bold) },
            text = { Text("Delete ${customerToDelete!!.name}? Note: Records with linked invoices or estimates cannot be deleted to maintain accounting integrity.") },
            confirmButton = {
                Button(
                    onClick = {
                        val toDel = customerToDelete!!
                        customerToDelete = null
                        viewModel.deleteCustomer(toDel) { success ->
                            if (success) {
                                Toast.makeText(context, "Party deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Cannot delete: Party has existing estimates or invoices.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwamiRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PartyCard(
    customer: Customer,
    onClick: () -> Unit,
    onViewStatement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                    Text(
                        text = customer.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = SwiggyDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Category Badge
                    val (badgeBg, badgeText, badgeLabel) = when (customer.category) {
                        PartyCategory.CUSTOMER -> Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), "Customer")
                        PartyCategory.VENDOR -> Triple(Color(0xFFF5F3FF), Color(0xFF6D28D9), "Vendor")
                        PartyCategory.BOTH -> Triple(Color(0xFFECFDF5), Color(0xFF047857), "Customer & Vendor")
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeBg,
                        border = BorderStroke(0.5.dp, badgeText.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = badgeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = SwiggyGray)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Ledger / Statement") },
                            leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF0284C7)) },
                            onClick = {
                                showMenu = false
                                onViewStatement()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = SwiggyOrange) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = SwamiRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = SwamiRed) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            val phoneToUse = customer.mobile.ifBlank { customer.phone }
            if (phoneToUse.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Phone: $phoneToUse",
                        fontSize = 12.5.sp,
                        color = SwiggyGray
                    )
                }
            }

            if (customer.gstin.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "GSTIN: ${customer.gstin}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569)
                )
            }

            if (customer.consumerNumber.isNotBlank() || customer.sanctionLoad.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                val consumerDetails = listOfNotNull(
                    customer.consumerNumber.takeIf { it.isNotBlank() }?.let { "Consumer #: $it" },
                    customer.sanctionLoad.takeIf { it.isNotBlank() }?.let { "Load: $it" }
                ).joinToString(" • ")
                Text(
                    text = consumerDetails,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            val addressLine = listOfNotNull(
                customer.village.takeIf { it.isNotBlank() },
                customer.city.takeIf { it.isNotBlank() },
                customer.address.takeIf { it.isNotBlank() }
            ).joinToString(", ")

            if (addressLine.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = addressLine,
                    fontSize = 12.sp,
                    color = SwiggyGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Statement Button
                Button(
                    onClick = onViewStatement,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A), contentColor = Color.White),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ledger / Statement", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (phoneToUse.isNotBlank()) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = onCall,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF0284C7)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF0284C7), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }

                        androidx.compose.material3.OutlinedButton(
                            onClick = onWhatsApp,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF16A34A)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("WhatsApp", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}
