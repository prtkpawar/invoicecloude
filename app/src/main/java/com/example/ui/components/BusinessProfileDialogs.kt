package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import com.example.data.model.BusinessTerminologyRegistry
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BusinessProfile
import com.example.ui.theme.SwiggyBorder
import com.example.ui.theme.SwiggyDark
import com.example.ui.theme.SwiggyGray
import com.example.ui.theme.SwiggyGreen
import com.example.ui.theme.SwiggyOrange
import com.example.ui.theme.SwiggyOrangeLight

@Composable
fun BusinessSwitcherDialog(
    businesses: List<BusinessProfile>,
    activeBusinessId: Long,
    onSelectBusiness: (Long) -> Unit,
    onAddNewBusiness: () -> Unit,
    onEditBusiness: (BusinessProfile) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(SwiggyOrangeLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = SwiggyOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Select Active Business",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = SwiggyDark
                    )
                    Text(
                        text = "Switch legal entity, quotation & invoice headers",
                        fontSize = 11.5.sp,
                        color = SwiggyGray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (businesses.isEmpty()) {
                    Text(
                        text = "No business profiles registered yet.",
                        fontSize = 13.sp,
                        color = SwiggyGray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    businesses.forEach { business ->
                        val isActive = business.id == activeBusinessId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectBusiness(business.id)
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) Color(0xFFFFF7ED) else Color(0xFFF8FAFC)
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (isActive) SwiggyOrange else Color(0xFFE2E8F0)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isActive) SwiggyOrange else Color(0xFF94A3B8),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = business.brandName.ifBlank { business.legalName },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp,
                                            color = SwiggyDark
                                        )
                                        if (isActive) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = SwiggyGreen.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SwiggyGreen,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = business.legalName,
                                        fontSize = 11.5.sp,
                                        color = SwiggyGray,
                                        maxLines = 1
                                    )
                                    if (!business.gstin.isNullOrBlank()) {
                                        Text(
                                            text = "GSTIN: ${business.gstin}",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        onEditBusiness(business)
                                        onDismiss()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = SwiggyOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = {
                        onAddNewBusiness()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SwiggyOrange)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = SwiggyOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add Another Business Entity", color = SwiggyOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SwiggyDark, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileEditDialog(
    profile: BusinessProfile? = null,
    onDismiss: () -> Unit,
    onSave: (BusinessProfile) -> Unit
) {
    var brandName by remember { mutableStateOf(profile?.brandName ?: "") }
    var legalName by remember { mutableStateOf(profile?.legalName ?: "") }
    var address by remember { mutableStateOf(profile?.address ?: "") }
    var city by remember { mutableStateOf(profile?.city ?: "Dhule") }
    var state by remember { mutableStateOf(profile?.state ?: "Maharashtra") }
    var pincode by remember { mutableStateOf(profile?.pincode ?: "424306") }
    var gstin by remember { mutableStateOf(profile?.gstin ?: "") }
    var pan by remember { mutableStateOf(profile?.pan ?: "") }
    var bankAccountNo by remember { mutableStateOf(profile?.bankAccountNo ?: "") }
    var ifsc by remember { mutableStateOf(profile?.ifsc ?: "") }
    var upiVpa by remember { mutableStateOf(profile?.upiVpa ?: "") }
    var signatoryName by remember { mutableStateOf(profile?.signatoryName ?: "") }
    var selectedTemplateId by remember { mutableStateOf(profile?.businessTemplateId ?: 3L) }
    var industryDropdownExpanded by remember { mutableStateOf(false) }

    var brandError by remember { mutableStateOf(false) }

    val currentTerminology = remember(selectedTemplateId) {
        BusinessTerminologyRegistry.getForTemplateId(selectedTemplateId)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = SwiggyDark,
        unfocusedTextColor = SwiggyDark,
        focusedBorderColor = SwiggyOrange,
        unfocusedBorderColor = SwiggyBorder,
        focusedLabelColor = SwiggyOrange,
        unfocusedLabelColor = SwiggyGray,
        cursorColor = SwiggyOrange,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )
    val textStyle = androidx.compose.ui.text.TextStyle(color = SwiggyDark, fontSize = 13.5.sp)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(SwiggyOrangeLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = null,
                        tint = SwiggyOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (profile == null) "New Business Profile" else "Edit Business Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SwiggyDark
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Industry / Business Type Dropdown
                Text(
                    text = "Business Type & Industry (Dropdown) *",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = SwiggyDark
                )

                ExposedDropdownMenuBox(
                    expanded = industryDropdownExpanded,
                    onExpandedChange = { industryDropdownExpanded = !industryDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${currentTerminology.iconEmoji} ${currentTerminology.title} — ${currentTerminology.industryKey}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Industry / Business Vertical *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = industryDropdownExpanded)
                        },
                        colors = fieldColors,
                        textStyle = textStyle,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = industryDropdownExpanded,
                        onDismissRequest = { industryDropdownExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        BusinessTerminologyRegistry.ALL.forEach { term ->
                            val isSel = selectedTemplateId == term.templateId
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(term.iconEmoji, fontSize = 18.sp)
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = term.title,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.5.sp,
                                                color = if (isSel) SwiggyOrange else SwiggyDark
                                            )
                                            Text(
                                                text = "Default terms & format for ${term.industryKey}",
                                                fontSize = 11.sp,
                                                color = SwiggyGray
                                            )
                                        }
                                        if (isSel) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = SwiggyOrange,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedTemplateId = term.templateId
                                    industryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Quick Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BusinessTerminologyRegistry.ALL.forEach { term ->
                        val isSel = selectedTemplateId == term.templateId
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedTemplateId = term.templateId },
                            label = { Text("${term.iconEmoji} ${term.title}", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SwiggyOrange,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = SwiggyDark
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = brandName,
                    onValueChange = {
                        brandName = it
                        brandError = it.isBlank()
                    },
                    label = { Text("Brand / Trade Name *") },
                    placeholder = { Text("e.g. Swami Solar Tech", color = Color(0xFF94A3B8)) },
                    isError = brandError,
                    supportingText = if (brandError) { { Text("Brand name is required") } } else null,
                    colors = fieldColors,
                    textStyle = textStyle,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = legalName,
                    onValueChange = { legalName = it },
                    label = { Text("Full Legal Entity Name") },
                    placeholder = { Text("e.g. SWAMI SAMARTH ELECTRICAL AND SOLAR SERVICES", color = Color(0xFF94A3B8)) },
                    colors = fieldColors,
                    textStyle = textStyle,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = gstin,
                        onValueChange = { gstin = it.uppercase() },
                        label = { Text("GSTIN") },
                        placeholder = { Text("27AAXCS1234A1Z5", color = Color(0xFF94A3B8)) },
                        colors = fieldColors,
                        textStyle = textStyle,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = pan,
                        onValueChange = { pan = it.uppercase() },
                        label = { Text("PAN") },
                        placeholder = { Text("AAXCS1234A", color = Color(0xFF94A3B8)) },
                        colors = fieldColors,
                        textStyle = textStyle,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Office Address") },
                    colors = fieldColors,
                    textStyle = textStyle,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        colors = fieldColors,
                        textStyle = textStyle,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = pincode,
                        onValueChange = { pincode = it },
                        label = { Text("Pincode") },
                        colors = fieldColors,
                        textStyle = textStyle,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Bank & Settlement Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = SwiggyDark,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = bankAccountNo,
                        onValueChange = { bankAccountNo = it },
                        label = { Text("Bank Account #") },
                        colors = fieldColors,
                        textStyle = textStyle,
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                    OutlinedTextField(
                        value = ifsc,
                        onValueChange = { ifsc = it.uppercase() },
                        label = { Text("IFSC Code") },
                        colors = fieldColors,
                        textStyle = textStyle,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = upiVpa,
                        onValueChange = { upiVpa = it },
                        label = { Text("UPI VPA / QR ID") },
                        placeholder = { Text("e.g. mobile@upi", color = Color(0xFF94A3B8)) },
                        colors = fieldColors,
                        textStyle = textStyle,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = signatoryName,
                        onValueChange = { signatoryName = it },
                        label = { Text("Authorized Signatory") },
                        colors = fieldColors,
                        textStyle = textStyle,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (brandName.isBlank()) {
                        brandError = true
                        return@Button
                    }
                    val updated = (profile ?: BusinessProfile(
                        legalName = legalName.ifBlank { brandName }.trim(),
                        brandName = brandName.trim(),
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
                        logoPath = null,
                        signaturePath = null,
                        businessTemplateId = selectedTemplateId
                    )).copy(
                        brandName = brandName.trim(),
                        legalName = legalName.ifBlank { brandName }.trim(),
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
                        businessTemplateId = selectedTemplateId
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SwiggyOrange)
            ) {
                Text("Save Profile", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SwiggyDark)
            }
        }
    )
}
