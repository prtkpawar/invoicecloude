package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocItem
import com.example.data.model.ItemMaster
import com.example.ui.theme.pro.Brand100
import com.example.ui.theme.pro.Brand600
import com.example.ui.theme.pro.Brand700
import com.example.ui.theme.pro.Danger600
import com.example.ui.theme.pro.Ink400
import com.example.ui.theme.pro.Ink600
import com.example.ui.theme.pro.Ink900
import com.example.ui.theme.pro.Line
import com.example.ui.theme.pro.Spacing
import com.example.ui.theme.pro.Success600
import com.example.ui.theme.pro.Surface
import com.example.ui.theme.pro.SurfaceSoft
import com.example.utils.Formatters

/**
 * Compact field row container that aligns 2 or 3 inputs side-by-side.
 */
@Composable
fun CompactFieldRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * Space-optimized OutlinedTextField with 52dp standard height.
 */
@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        placeholder = if (placeholder != null) {
            { Text(placeholder, style = MaterialTheme.typography.bodySmall, color = Ink400) }
        } else null,
        modifier = modifier.height(52.dp),
        singleLine = singleLine,
        readOnly = readOnly,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink900),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Brand600,
            unfocusedBorderColor = Line,
            focusedLabelColor = Brand600,
            unfocusedLabelColor = Ink600,
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            cursorColor = Brand600
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon
    )
}

/**
 * Compact numeric field for rates and quantities.
 */
@Composable
fun CompactNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    prefix: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = if (label != null) {
            { Text(label, style = MaterialTheme.typography.labelSmall) }
        } else null,
        prefix = if (prefix != null) {
            { Text(prefix, style = MaterialTheme.typography.bodySmall, color = Ink600, fontWeight = FontWeight.SemiBold) }
        } else null,
        modifier = modifier.height(48.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink900, fontWeight = FontWeight.Bold),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Brand600,
            unfocusedBorderColor = Line,
            focusedContainerColor = Surface,
            unfocusedContainerColor = SurfaceSoft,
            cursorColor = Brand600
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

/**
 * Collapsible section card that tucks secondary/optional fields away.
 */
@Composable
fun CollapsibleSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Surface,
        border = BorderStroke(1.dp, Line),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = Spacing.md, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Brand100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = Brand600, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(Spacing.sm))
                    Text(title, style = MaterialTheme.typography.titleSmall, color = Ink900, fontWeight = FontWeight.Bold)
                    if (badge != null) {
                        Spacer(Modifier.width(Spacing.xs))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SurfaceSoft,
                            border = BorderStroke(0.5.dp, Line)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = Ink600,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Ink600
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = Spacing.md,
                        end = Spacing.md,
                        bottom = Spacing.md
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    content = content
                )
            }
        }
    }
}

/**
 * Spreadsheet-style compact item row with direct editing.
 */
@Composable
fun CompactItemRow(
    item: DocItem,
    index: Int,
    onUpdate: (DocItem) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Surface,
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm)
        ) {
            // Row 1: Item Index + Editable Item Name/Description + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Ink600,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 6.dp)
                )

                CompactTextField(
                    value = item.description.ifBlank { item.label.takeIf { !it.all { ch -> ch.isDigit() } } ?: "" },
                    onValueChange = { newDesc ->
                        onUpdate(item.copy(description = newDesc, label = if (item.label.isBlank() || item.label.all { ch -> ch.isDigit() }) newDesc else item.label))
                    },
                    label = "Item Name / Description",
                    placeholder = "e.g. Sugar 1kg / Solar Panel / Consulting",
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove Item",
                        tint = Danger600,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Row 2: Editable HSN, Unit, Qty, Rate, Line Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // HSN Code
                CompactTextField(
                    value = item.hsn,
                    onValueChange = { newHsn -> onUpdate(item.copy(hsn = newHsn)) },
                    label = "HSN",
                    placeholder = "HSN",
                    modifier = Modifier.weight(0.8f)
                )

                // Unit
                CompactTextField(
                    value = item.unit,
                    onValueChange = { newUnit -> onUpdate(item.copy(unit = newUnit)) },
                    label = "Unit",
                    placeholder = "NOS",
                    modifier = Modifier.weight(0.8f)
                )

                // Qty
                CompactNumberField(
                    value = if (item.qty == 0.0) "" else if (item.qty % 1.0 == 0.0) item.qty.toInt().toString() else item.qty.toString(),
                    onValueChange = { newQtyStr ->
                        val q = newQtyStr.toDoubleOrNull() ?: 0.0
                        onUpdate(item.copy(qty = q, amount = q * item.rate))
                    },
                    label = "Qty",
                    modifier = Modifier.weight(0.9f)
                )

                // Rate
                CompactNumberField(
                    value = if (item.rate == 0.0) "" else if (item.rate % 1.0 == 0.0) item.rate.toInt().toString() else item.rate.toString(),
                    onValueChange = { newRateStr ->
                        val r = newRateStr.toDoubleOrNull() ?: 0.0
                        onUpdate(item.copy(rate = r, amount = item.qty * r))
                    },
                    label = "Rate",
                    prefix = "₹",
                    modifier = Modifier.weight(1.1f)
                )

                // Line Amount
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1.1f)
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink600,
                        fontSize = 10.sp
                    )
                    Text(
                        text = Formatters.money(item.qty * item.rate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink900,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 3-Step Wizard header indicator.
 */
@Composable
fun WizardStepIndicator(
    currentStep: Int,
    totalSteps: Int = 3,
    stepTitles: List<String> = listOf("1. Customer", "2. Line Items", "3. Review & Save"),
    onStepClick: (Int) -> Unit = {}
) {
    Surface(
        color = Surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            stepTitles.forEachIndexed { index, title ->
                val isDone = index < currentStep
                val isCurrent = index == currentStep

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = index <= currentStep) { onStepClick(index) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDone -> Success600
                                    isCurrent -> Brand600
                                    else -> SurfaceSoft
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        } else {
                            Text(
                                text = "${index + 1}",
                                color = if (isCurrent) Color.White else Ink400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            isCurrent -> Ink900
                            isDone -> Success600
                            else -> Ink400
                        }
                    )
                }

                if (index < totalSteps - 1) {
                    Text("•", color = Line, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Wizard bottom navigation bar with Back, Next and Save buttons.
 */
@Composable
fun WizardBottomBar(
    currentStep: Int,
    totalSteps: Int = 3,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit,
    nextLabel: String = "Next Step →",
    saveLabel: String = "Save & Generate PDF",
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        border = BorderStroke(1.dp, Line),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Line),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink900),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(0.35f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Back", fontWeight = FontWeight.Bold)
                }
            }

            if (currentStep < totalSteps - 1) {
                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                ) {
                    Text(nextLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            } else {
                Button(
                    onClick = onSave,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(saveLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

/**
 * Item catalog bottom sheet with live search & one-tap add.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCatalogBottomSheet(
    items: List<ItemMaster>,
    onSelect: (ItemMaster) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        var searchQuery by remember { mutableStateOf("") }
        val filtered = remember(items, searchQuery) {
            if (searchQuery.isBlank()) items
            else items.filter {
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.label.contains(searchQuery, ignoreCase = true) ||
                it.hsn.contains(searchQuery, ignoreCase = true)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Item from Catalog", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Ink900)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Ink600)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or HSN...", color = Ink400) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Ink600) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Brand600,
                    unfocusedBorderColor = Line,
                    focusedContainerColor = SurfaceSoft,
                    unfocusedContainerColor = SurfaceSoft
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm)
            )

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No items found matching '$searchQuery'", color = Ink400, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered) { item ->
                        ListItem(
                            headlineContent = { Text(item.description.ifBlank { item.label }, fontWeight = FontWeight.SemiBold, color = Ink900) },
                            supportingContent = { Text("HSN: ${item.hsn.ifBlank { "N/A" }} • Rate: ₹${item.rate} / ${item.unit}", color = Ink600, fontSize = 12.sp) },
                            trailingContent = {
                                Button(
                                    onClick = {
                                        onSelect(item)
                                        onDismiss()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Brand600, contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("+ Add", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                        HorizontalDivider(color = Line.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
