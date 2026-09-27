package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.entity.BusinessProfile
import com.example.data.model.BusinessCatalogPresets
import com.example.data.model.BusinessTerminologyRegistry
import com.example.data.model.PdfTheme
import com.example.ui.theme.SwiggyBorder
import com.example.ui.theme.SwiggyDark
import com.example.ui.theme.SwiggyGray
import com.example.ui.theme.SwiggyGreen
import com.example.ui.theme.SwiggyOrange
import com.example.ui.theme.SwiggyOrangeLight
import java.io.File
import java.io.FileOutputStream

/**
 * Enterprise Firm Manager Hub.
 * Allows viewing, switching, creating, editing, and deleting business entities/firms
 * with adaptive industry presets, per-firm logos, digital signatures, bank accounts, and PDF themes.
 */
@Composable
fun FirmManagerDialog(
    businesses: List<BusinessProfile>,
    activeBusinessId: Long,
    onSelectBusiness: (Long) -> Unit,
    onSaveBusiness: (BusinessProfile) -> Unit,
    onDeleteBusiness: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var editingProfile by remember { mutableStateOf<BusinessProfile?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var firmToDelete by remember { mutableStateOf<BusinessProfile?>(null) }

    if (isCreatingNew || editingProfile != null) {
        FirmEditorDialog(
            initialProfile = editingProfile,
            onSave = { savedProfile ->
                onSaveBusiness(savedProfile)
                editingProfile = null
                isCreatingNew = false
            },
            onDismiss = {
                editingProfile = null
                isCreatingNew = false
            }
        )
        return
    }

    if (firmToDelete != null) {
        AlertDialog(
            onDismissRequest = { firmToDelete = null },
            title = {
                Text("Delete Firm Entity?", fontWeight = FontWeight.Bold, color = SwiggyDark)
            },
            text = {
                Text(
                    "Are you sure you want to delete '${firmToDelete?.brandName ?: firmToDelete?.legalName}'? Invoices and estimates associated with this firm will remain in the database.",
                    color = SwiggyGray,
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        firmToDelete?.id?.let { onDeleteBusiness(it) }
                        firmToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete Firm", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { firmToDelete = null }) {
                    Text("Cancel", color = SwiggyGray)
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Surface(
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFF4F4F5), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apartment,
                                    contentDescription = null,
                                    tint = Color(0xFF18181B),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Firm Manager",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = SwiggyDark
                                )
                                Text(
                                    text = "${businesses.size} Registered Business Entities",
                                    fontSize = 12.sp,
                                    color = SwiggyGray
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = SwiggyGray)
                        }
                    }
                }

                // Firm List
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    businesses.forEach { biz ->
                        val isActive = biz.id == activeBusinessId
                        val terminology = BusinessTerminologyRegistry.getForTemplateId(biz.businessTemplateId)
                        val brand = biz.brandName.takeIf { it.isNotBlank() } ?: biz.legalName

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectBusiness(biz.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) Color(0xFFF4F4F5) else Color.White
                            ),
                            border = BorderStroke(
                                width = if (isActive) 1.8.dp else 1.dp,
                                color = if (isActive) Color(0xFF18181B) else SwiggyBorder
                            ),
                            elevation = CardDefaults.cardElevation(if (isActive) 3.dp else 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Logo or Industry Emoji
                                        if (!biz.logoPath.isNullOrBlank() && File(biz.logoPath).exists()) {
                                            AsyncImage(
                                                model = File(biz.logoPath),
                                                contentDescription = "Firm Logo",
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, SwiggyBorder, RoundedCornerShape(8.dp))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(
                                                        if (isActive) Color(0xFFDBEAFE) else Color(0xFFF1F5F9),
                                                        RoundedCornerShape(10.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = terminology.iconEmoji,
                                                    fontSize = 22.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = brand,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.5.sp,
                                                    color = SwiggyDark,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (isActive) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = SwiggyGreen,
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "ACTIVE",
                                                            color = Color.White,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${terminology.title} • ${biz.city.ifBlank { "HQ" }}",
                                                fontSize = 11.5.sp,
                                                color = SwiggyGray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { editingProfile = biz },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = Color(0xFF18181B),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        if (businesses.size > 1) {
                                            IconButton(
                                                onClick = { firmToDelete = biz },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = if (isActive) Color(0xFFE4E4E7) else Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(8.dp))

                                // Details Tags Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        if (!biz.gstin.isNullOrBlank()) {
                                            Text(
                                                text = "GSTIN: ${biz.gstin}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = SwiggyDark
                                            )
                                        }
                                        val bankStr = if (!biz.bankAccountNo.isNullOrBlank()) "A/C: ••••${biz.bankAccountNo.takeLast(4)}" else "No Bank Added"
                                        Text(
                                            text = bankStr,
                                            fontSize = 10.5.sp,
                                            color = SwiggyGray
                                        )
                                    }

                                    if (!isActive) {
                                        OutlinedButton(
                                            onClick = { onSelectBusiness(biz.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFF18181B)),
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                        ) {
                                            Text("Set Active", fontSize = 11.sp, color = Color(0xFF18181B), fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = SwiggyGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Current Billing Entity",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SwiggyGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Bar
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Done", color = SwiggyDark)
                        }

                        Button(
                            onClick = { isCreatingNew = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SwiggyOrange)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create New Firm", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-featured Firm Creator and Editor dialog.
 */
@Composable
fun FirmEditorDialog(
    initialProfile: BusinessProfile? = null,
    onSave: (BusinessProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTemplateId by remember { mutableStateOf(initialProfile?.businessTemplateId ?: 3L) }
    var brandName by remember { mutableStateOf(initialProfile?.brandName ?: "") }
    var legalName by remember { mutableStateOf(initialProfile?.legalName ?: "") }
    var address by remember { mutableStateOf(initialProfile?.address ?: "") }
    var city by remember { mutableStateOf(initialProfile?.city ?: "Dhule") }
    var state by remember { mutableStateOf(initialProfile?.state ?: "Maharashtra") }
    var pincode by remember { mutableStateOf(initialProfile?.pincode ?: "424306") }
    var gstin by remember { mutableStateOf(initialProfile?.gstin ?: "") }
    var pan by remember { mutableStateOf(initialProfile?.pan ?: "") }
    var bankAccountNo by remember { mutableStateOf(initialProfile?.bankAccountNo ?: "") }
    var ifsc by remember { mutableStateOf(initialProfile?.ifsc ?: "") }
    var upiVpa by remember { mutableStateOf(initialProfile?.upiVpa ?: "") }
    var signatoryName by remember { mutableStateOf(initialProfile?.signatoryName ?: "") }
    var logoPath by remember { mutableStateOf(initialProfile?.logoPath) }
    var signaturePath by remember { mutableStateOf(initialProfile?.signaturePath) }

    // Logo image picker
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val logoStore = com.example.core.media.LogoStore(context)
                val result = logoStore.importLogo(it, replacePath = logoPath)
                result.onSuccess { newPath -> logoPath = newPath }
                result.onFailure { error ->
                    android.widget.Toast.makeText(context, "Failed to import logo: ${error.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Signature image picker
    val signaturePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val logoStore = com.example.core.media.LogoStore(context)
                val result = logoStore.importLogo(it, replacePath = signaturePath)
                result.onSuccess { newPath -> signaturePath = newPath }
                result.onFailure { error ->
                    android.widget.Toast.makeText(context, "Failed to import signature: ${error.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(SwiggyOrangeLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (initialProfile == null) Icons.Default.Add else Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = SwiggyOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (initialProfile == null) "Create New Firm" else "Edit Firm Profile",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = SwiggyDark
                                )
                                Text(
                                    text = "Branding, Bank Mandate, Tax & Signatures",
                                    fontSize = 11.5.sp,
                                    color = SwiggyGray
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = SwiggyGray)
                        }
                    }
                }

                // Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Industry Preset Selector
                    Text(
                        text = "1. INDUSTRY TEMPLATE & BUSINESS TYPE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BusinessCatalogPresets.ALL_PRESETS.forEachIndexed { index, preset ->
                            val tId = (index + 1).toLong()
                            val isSelected = selectedTemplateId == tId
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTemplateId = tId
                                    if (brandName.isBlank()) {
                                        brandName = preset.title.split("&").first().trim()
                                    }
                                },
                                label = {
                                    Text(
                                        "${preset.iconEmoji} ${preset.title}",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SwiggyOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // 2. Firm Identity & Brand
                    Text(
                        text = "2. FIRM BRANDING & LEGAL IDENTITY",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    OutlinedTextField(
                        value = brandName,
                        onValueChange = { brandName = it },
                        label = { Text("Brand Name / Display Title *") },
                        placeholder = { Text("e.g. AARAV MEDICAL & GENERAL STORE") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = legalName,
                        onValueChange = { legalName = it },
                        label = { Text("Legal Entity Name (Registered with GST / Bank)") },
                        placeholder = { Text("e.g. AARAV ENTERPRISES PVT LTD") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // 3. Address & Location
                    Text(
                        text = "3. REGISTERED ADDRESS",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Shop / Office Address") },
                        placeholder = { Text("Shop No. 4, Market Yard Road") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = pincode,
                            onValueChange = { pincode = it },
                            label = { Text("PIN Code") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    // 4. Tax & Statutory Identification
                    Text(
                        text = "4. TAX & STATUTORY IDENTIFICATION",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = gstin,
                            onValueChange = { gstin = it.uppercase() },
                            label = { Text("GSTIN (Optional)") },
                            placeholder = { Text("27AAAAA0000A1Z5") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = pan,
                            onValueChange = { pan = it.uppercase() },
                            label = { Text("PAN Number") },
                            placeholder = { Text("ABCDE1234F") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    // 5. Bank Account & UPI Details
                    Text(
                        text = "5. OFFICIAL REMITTANCE & BANK MANDATE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = bankAccountNo,
                            onValueChange = { bankAccountNo = it },
                            label = { Text("Bank Account No.") },
                            placeholder = { Text("324201010510532") },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = ifsc,
                            onValueChange = { ifsc = it.uppercase() },
                            label = { Text("IFSC Code") },
                            placeholder = { Text("UBIN0532428") },
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = upiVpa,
                        onValueChange = { upiVpa = it },
                        label = { Text("UPI ID (For QR Code Scan-to-Pay)") },
                        placeholder = { Text("shopname@upi / mobile@okhdfcbank") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // 6. Authorized Signatory & Visual Media
                    Text(
                        text = "6. SIGNATORY & BRAND ASSETS",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    OutlinedTextField(
                        value = signatoryName,
                        onValueChange = { signatoryName = it },
                        label = { Text("Authorized Signatory Person") },
                        placeholder = { Text("e.g. Mr. Pratik Tatar / Proprietor") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Logo & Signature Pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Logo Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { logoPickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, SwiggyBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (!logoPath.isNullOrBlank() && File(logoPath).exists()) {
                                    AsyncImage(
                                        model = File(logoPath),
                                        contentDescription = "Logo",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Change Logo", fontSize = 11.sp, color = SwiggyOrange, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = SwiggyGray, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Upload Logo", fontSize = 11.sp, color = SwiggyDark, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        // Signature Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { signaturePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, SwiggyBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (!signaturePath.isNullOrBlank() && File(signaturePath).exists()) {
                                    AsyncImage(
                                        model = File(signaturePath),
                                        contentDescription = "Signature",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Change Sign", fontSize = 11.sp, color = SwiggyOrange, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Draw, contentDescription = null, tint = SwiggyGray, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Upload Digital Sign", fontSize = 11.sp, color = SwiggyDark, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Save Action
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", color = SwiggyGray)
                        }

                        Button(
                            onClick = {
                                val effectiveBrand = brandName.trim().ifBlank { legalName.trim().ifBlank { "My Business Firm" } }
                                val effectiveLegal = legalName.trim().ifBlank { effectiveBrand }
                                val profileToSave = (initialProfile ?: BusinessProfile(
                                    legalName = effectiveLegal,
                                    brandName = effectiveBrand,
                                    address = address.trim(),
                                    city = city.trim(),
                                    state = state.trim(),
                                    pincode = pincode.trim(),
                                    gstin = gstin.trim().ifBlank { null },
                                    pan = pan.trim().ifBlank { null },
                                    bankAccountNo = bankAccountNo.trim().ifBlank { null },
                                    ifsc = ifsc.trim().ifBlank { null },
                                    upiVpa = upiVpa.trim().ifBlank { null },
                                    signatoryName = signatoryName.trim().ifBlank { null },
                                    logoPath = logoPath,
                                    signaturePath = signaturePath,
                                    businessTemplateId = selectedTemplateId,
                                    isActive = true
                                )).copy(
                                    legalName = effectiveLegal,
                                    brandName = effectiveBrand,
                                    address = address.trim(),
                                    city = city.trim(),
                                    state = state.trim(),
                                    pincode = pincode.trim(),
                                    gstin = gstin.trim().ifBlank { null },
                                    pan = pan.trim().ifBlank { null },
                                    bankAccountNo = bankAccountNo.trim().ifBlank { null },
                                    ifsc = ifsc.trim().ifBlank { null },
                                    upiVpa = upiVpa.trim().ifBlank { null },
                                    signatoryName = signatoryName.trim().ifBlank { null },
                                    logoPath = logoPath,
                                    signaturePath = signaturePath,
                                    businessTemplateId = selectedTemplateId
                                )
                                onSave(profileToSave)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SwiggyGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Firm Profile", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
