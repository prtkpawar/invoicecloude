package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.DocStatus
import com.example.ui.theme.SwamiGold
import com.example.ui.theme.SwamiGoldLight
import com.example.ui.theme.SwamiGreen
import com.example.ui.theme.SwamiNavy
import com.example.ui.theme.SwamiNavyDark
import com.example.ui.theme.SwamiRed
import com.example.ui.theme.SwamiTeal
import com.example.utils.Formatters

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        DocStatus.PAID -> MaterialTheme.colorScheme.surface to SwamiGreen
        DocStatus.PARTIAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        DocStatus.UNPAID -> MaterialTheme.colorScheme.errorContainer to SwamiRed
        DocStatus.CONVERTED -> MaterialTheme.colorScheme.surfaceVariant to SwamiTeal
        DocStatus.ESTIMATE -> MaterialTheme.colorScheme.primaryContainer to SwamiNavy
        DocStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.primaryContainer to SwamiNavy
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(0.5.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.25f)))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SwamiNavy.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SwamiNavy.copy(alpha = 0.6f),
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SwamiSolarEmblem(modifier: Modifier = Modifier.size(38.dp)) {
    val arcColor = MaterialTheme.colorScheme.onSurfaceVariant
    val globeColor = MaterialTheme.colorScheme.primary
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val scale = minOf(w / 44f, h / 44f)

        // Center of the globe
        val centerX = 23f * scale
        val centerY = 22f * scale
        val globeRadius = 15f * scale

        // 1. Golden Orbital Crescent Arc sweeping around upper-left of globe
        val goldenArcPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(4f * scale, 22f * scale)
            cubicTo(
                4f * scale, 9f * scale,
                14f * scale, 2f * scale,
                29f * scale, 2f * scale
            )
            cubicTo(
                37f * scale, 2f * scale,
                42f * scale, 6f * scale,
                43.5f * scale, 11f * scale
            )
            cubicTo(
                40f * scale, 8f * scale,
                34f * scale, 5.5f * scale,
                27f * scale, 5.5f * scale
            )
            cubicTo(
                15f * scale, 5.5f * scale,
                7.5f * scale, 13f * scale,
                7.5f * scale, 23f * scale
            )
            cubicTo(
                7.5f * scale, 33f * scale,
                15f * scale, 41f * scale,
                28f * scale, 41f * scale
            )
            cubicTo(
                35f * scale, 41f * scale,
                40f * scale, 38f * scale,
                42.5f * scale, 34f * scale
            )
            cubicTo(
                38f * scale, 42f * scale,
                30f * scale, 43.5f * scale,
                25f * scale, 43.5f * scale
            )
            cubicTo(
                11f * scale, 43.5f * scale,
                4f * scale, 33f * scale,
                4f * scale, 22f * scale
            )
            close()
        }
        drawPath(
            path = goldenArcPath,
            color = arcColor
        )

        // 2. Base Sphere Globe
        drawCircle(
            color = globeColor,
            radius = globeRadius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )

        // 3. Solar Panel Photovoltaic Facet Tiles (3D Curved Grid)
        val tiles = listOf(
            // Top Row
            Triple(centerX - 4f * scale, centerY - 11f * scale, Color(0xFF0288D1)),
            Triple(centerX + 3f * scale, centerY - 10.5f * scale, Color(0xFF29B6F6)),
            Triple(centerX - 10f * scale, centerY - 9f * scale, Color(0xFF00A0E9)),

            // Upper Mid Row
            Triple(centerX - 11f * scale, centerY - 4.5f * scale, Color(0xFF0D47A1)),
            Triple(centerX - 5f * scale, centerY - 5f * scale, Color(0xFF0288D1)),
            Triple(centerX + 2f * scale, centerY - 4.5f * scale, Color(0xFF00A0E9)),
            Triple(centerX + 8f * scale, centerY - 3.5f * scale, Color(0xFF29B6F6)),

            // Equator Row
            Triple(centerX - 12f * scale, centerY + 1.5f * scale, Color(0xFF0D47A1)),
            Triple(centerX - 6f * scale, centerY + 1.5f * scale, Color(0xFF1565C0)),
            Triple(centerX + 1f * scale, centerY + 1.5f * scale, Color(0xFF0288D1)),
            Triple(centerX + 8f * scale, centerY + 1.5f * scale, Color(0xFF00A0E9)),

            // Lower Mid Row
            Triple(centerX - 10f * scale, centerY + 7.5f * scale, Color(0xFF0A3161)),
            Triple(centerX - 4f * scale, centerY + 7.5f * scale, Color(0xFF0D47A1)),
            Triple(centerX + 3f * scale, centerY + 7.5f * scale, Color(0xFF1565C0)),
            Triple(centerX + 8f * scale, centerY + 6.5f * scale, Color(0xFF0288D1)),

            // Bottom Row
            Triple(centerX - 5f * scale, centerY + 12f * scale, Color(0xFF0A3161)),
            Triple(centerX + 2f * scale, centerY + 12f * scale, Color(0xFF0D47A1))
        )

        val tileW = 5f * scale
        val tileH = 4.2f * scale
        for ((tx, ty, tColor) in tiles) {
            drawRoundRect(
                color = tColor,
                topLeft = androidx.compose.ui.geometry.Offset(tx, ty),
                size = androidx.compose.ui.geometry.Size(tileW, tileH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(0.8f * scale, 0.8f * scale)
            )
        }

        // 4. Fine 3D Globe Latitude / Longitude White Grid
        val gridStroke = 0.5f * scale
        // Horizontal latitude arcs
        for (dy in listOf(-5f, 1f, 7f)) {
            val yOffset = centerY + dy * scale
            val halfW = (globeRadius * 0.9f)
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = androidx.compose.ui.geometry.Offset(centerX - halfW, yOffset),
                end = androidx.compose.ui.geometry.Offset(centerX + halfW, yOffset),
                strokeWidth = gridStroke
            )
        }
        // Vertical longitude arc
        drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = androidx.compose.ui.geometry.Offset(centerX, centerY - globeRadius + 2f * scale),
            end = androidx.compose.ui.geometry.Offset(centerX, centerY + globeRadius - 2f * scale),
            strokeWidth = gridStroke
        )
    }
}

@Composable
fun SwamiBrandLogo(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SwamiSolarEmblem(modifier = Modifier.size(38.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SWAMI SOLAR",
                    color = if (darkTheme) Color.White else Color(0xFF0B4B8B),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "“Powering a Brighter Future”",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
fun CustomerDialog(
    existing: Customer? = null,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var category by remember { mutableStateOf(existing?.category ?: com.example.data.model.PartyCategory.CUSTOMER) }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var mobile by remember { mutableStateOf(existing?.mobile ?: existing?.phone ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: existing?.mobile ?: "") }
    var email by remember { mutableStateOf(existing?.email ?: "") }
    var gstin by remember { mutableStateOf(existing?.gstin ?: "") }
    var village by remember { mutableStateOf(existing?.village ?: "") }
    var city by remember { mutableStateOf(existing?.city ?: "") }
    var address by remember { mutableStateOf(existing?.address ?: "") }
    var consumerNumber by remember { mutableStateOf(existing?.consumerNumber ?: "") }
    var sanctionLoad by remember { mutableStateOf(existing?.sanctionLoad ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var nameError by remember { mutableStateOf(false) }

    var showMoreDetails by remember {
        mutableStateOf(
            existing != null && (email.isNotBlank() || gstin.isNotBlank() || village.isNotBlank() || city.isNotBlank() || address.isNotBlank() || consumerNumber.isNotBlank() || sanctionLoad.isNotBlank() || notes.isNotBlank())
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing == null) "New Party" else "Edit Party",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val dialogFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.onSurface
                )
                val dialogTextStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)

                // Category Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    com.example.data.model.PartyCategory.values().forEach { cat ->
                        val selected = category == cat
                        val catLabel = when (cat) {
                            com.example.data.model.PartyCategory.CUSTOMER -> "Customer"
                            com.example.data.model.PartyCategory.VENDOR -> "Vendor"
                            com.example.data.model.PartyCategory.BOTH -> "Both"
                        }
                        val activeColor = MaterialTheme.colorScheme.onSurface
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) activeColor else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { category = cat }
                        ) {
                            Text(
                                text = catLabel,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Name *") },
                    placeholder = { Text("e.g. Sharma Electronics", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Name is required") } } else null,
                    textStyle = dialogTextStyle,
                    colors = dialogFieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = {
                        mobile = it
                        phone = it
                    },
                    label = { Text("Mobile (WhatsApp)") },
                    placeholder = { Text("10 digit number", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    textStyle = dialogTextStyle,
                    colors = dialogFieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // ===== "More Details" collapsible section =====
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMoreDetails = !showMoreDetails },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (showMoreDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showMoreDetails) "Hide Details" else "More Details (Optional)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (showMoreDetails) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("name@email.com", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        textStyle = dialogTextStyle,
                        colors = dialogFieldColors,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = gstin,
                        onValueChange = { gstin = it.uppercase() },
                        label = { Text("GSTIN (Optional)") },
                        placeholder = { Text("27ABCDE1234F1Z5", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        textStyle = dialogTextStyle,
                        colors = dialogFieldColors,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = village,
                        onValueChange = { village = it },
                        label = { Text("Village / Town") },
                        placeholder = { Text("e.g. Pimpalner", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        textStyle = dialogTextStyle,
                        colors = dialogFieldColors,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City / District") },
                        placeholder = { Text("e.g. Dhule", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        textStyle = dialogTextStyle,
                        colors = dialogFieldColors,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        textStyle = dialogTextStyle,
                        colors = dialogFieldColors,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (category == com.example.data.model.PartyCategory.CUSTOMER || category == com.example.data.model.PartyCategory.BOTH) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = consumerNumber,
                                onValueChange = { consumerNumber = it },
                                label = { Text("Consumer #") },
                                placeholder = { Text("MSEDCL / Discom", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                textStyle = dialogTextStyle,
                                colors = dialogFieldColors,
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sanctionLoad,
                                onValueChange = { sanctionLoad = it },
                                label = { Text("Sanction Load") },
                                placeholder = { Text("e.g. 5 kW", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                textStyle = dialogTextStyle,
                                colors = dialogFieldColors,
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Remarks") },
                        placeholder = { Text("Special instructions or vendor terms", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        textStyle = dialogTextStyle,
                        colors = dialogFieldColors,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val customer = (existing ?: Customer()).copy(
                        category = category,
                        name = name.trim(),
                        mobile = mobile.trim(),
                        phone = if (phone.isNotBlank()) phone.trim() else mobile.trim(),
                        email = email.trim(),
                        gstin = gstin.trim(),
                        village = village.trim(),
                        city = city.trim(),
                        address = address.trim(),
                        consumerNumber = consumerNumber.trim(),
                        sanctionLoad = sanctionLoad.trim(),
                        notes = notes.trim()
                    )
                    onSave(customer)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text("Save", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentDialog(
    balanceDue: Double,
    onDismiss: () -> Unit,
    onSave: (amount: Double, mode: String, date: String, ref: String, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("CASH") }
    var dateText by remember { mutableStateOf(Formatters.todayIso()) }
    var ref by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    var expandedMode by remember { mutableStateOf(false) }
    val modes = listOf("CASH", "UPI", "BANK", "CHEQUE")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add Payment", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val payFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = SwamiNavy,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = SwamiNavy,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = SwamiNavy
                )
                val payTextStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)

                Text(
                    text = "Balance Due: ${Formatters.money(balanceDue)}",
                    color = SwamiRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = (it.toDoubleOrNull() ?: 0.0) <= 0.0
                    },
                    label = { Text("Amount Received *") },
                    prefix = { Text("₹ ") },
                    isError = amountError,
                    textStyle = payTextStyle,
                    colors = payFieldColors,
                    singleLine = true,
                    trailingIcon = {
                        TextButton(onClick = { amountText = balanceDue.toString() }) {
                            Text("Full", color = SwamiGreen, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedMode,
                    onExpandedChange = { expandedMode = !expandedMode }
                ) {
                    OutlinedTextField(
                        value = mode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Mode") },
                        textStyle = payTextStyle,
                        colors = payFieldColors,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMode) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMode,
                        onDismissRequest = { expandedMode = false }
                    ) {
                        modes.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    mode = m
                                    expandedMode = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    textStyle = payTextStyle,
                    colors = payFieldColors,
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = SwamiNavy) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ref,
                    onValueChange = { ref = it },
                    label = { Text("Reference (UPI/UTR / Cheque No.)") },
                    textStyle = payTextStyle,
                    colors = payFieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    textStyle = payTextStyle,
                    colors = payFieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt <= 0.0) {
                        amountError = true
                        return@Button
                    }
                    onSave(amt, mode, dateText, ref, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SwamiGreen)
            ) {
                Text("Record Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
