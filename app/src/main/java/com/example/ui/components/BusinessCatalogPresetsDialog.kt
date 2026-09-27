package com.example.ui.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessCatalogPresets
import com.example.data.model.BusinessTypePreset
import com.example.data.model.StarterItem
import com.example.ui.theme.pro.Brand100
import com.example.ui.theme.pro.Brand600
import com.example.ui.theme.pro.Brand700
import com.example.ui.theme.pro.Ink400
import com.example.ui.theme.pro.Ink600
import com.example.ui.theme.pro.Ink900
import com.example.ui.theme.pro.Line
import com.example.ui.theme.pro.Success50
import com.example.ui.theme.pro.Success600
import com.example.ui.theme.pro.SurfaceSoft

@Composable
fun BusinessCatalogPresetsDialog(
    activeBusinessTypeHint: String? = null,
    onDismiss: () -> Unit,
    onImportAllPreset: (BusinessTypePreset) -> Unit,
    onImportSingleItem: (StarterItem, String) -> Unit
) {
    val context = LocalContext.current
    val recommendedPreset = remember {
        BusinessCatalogPresets.getPresetByBusinessType(activeBusinessTypeHint)
    }

    var selectedPreset by remember { mutableStateOf(recommendedPreset) }
    var searchQuery by remember { mutableStateOf("") }
    var importedItemLabels by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Brand100, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = Brand600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Business Catalog Presets",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Ink900
                        )
                        Text(
                            text = "12 Industry Starter Kits with HSN & GST",
                            fontSize = 11.5.sp,
                            color = Ink600
                        )
                    }
                }

                androidx.compose.material3.IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Ink400, modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Preset Selection Horizontal Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BusinessCatalogPresets.ALL_PRESETS.forEach { preset ->
                        val isSelected = selectedPreset.id == preset.id
                        val isRecommended = preset.id == recommendedPreset.id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Brand600 else SurfaceSoft,
                            border = if (!isSelected) BorderStroke(1.dp, Line) else null,
                            modifier = Modifier.clickable {
                                selectedPreset = preset
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(preset.iconEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = preset.title.split("&").first().trim(),
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Ink900
                                )
                                if (isRecommended) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "★",
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color(0xFFFDE047) else Color(0xFFD97706)
                                    )
                                }
                            }
                        }
                    }
                }

                // Active Preset Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Brand100.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, Brand100)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedPreset.iconEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = selectedPreset.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Brand700
                                )
                                Text(
                                    text = selectedPreset.subtitle,
                                    fontSize = 11.sp,
                                    color = Ink600,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = {
                                onImportAllPreset(selectedPreset)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Brand600, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Import All (${selectedPreset.starterItems.size})",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Search Bar inside Preset Items
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter items in ${selectedPreset.title.split("&").first().trim()}...", fontSize = 12.sp, color = Ink400) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Ink400, modifier = Modifier.size(16.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Ink900,
                        unfocusedTextColor = Ink900,
                        focusedBorderColor = Brand600,
                        unfocusedBorderColor = Line,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Items list
                val filteredItems = remember(selectedPreset, searchQuery) {
                    val q = searchQuery.trim().lowercase()
                    if (q.isBlank()) selectedPreset.starterItems
                    else selectedPreset.starterItems.filter {
                        it.label.lowercase().contains(q) ||
                        it.description.lowercase().contains(q) ||
                        it.category.lowercase().contains(q) ||
                        it.hsn.contains(q)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (filteredItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No items found matching '$searchQuery'", fontSize = 12.sp, color = Ink600)
                        }
                    } else {
                        filteredItems.forEach { item ->
                            val isImported = importedItemLabels.contains(item.label)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Line)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Ink900
                                        )
                                        if (item.description.isNotBlank()) {
                                            Text(
                                                text = item.description,
                                                fontSize = 11.sp,
                                                color = Ink600,
                                                maxLines = 1
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = Brand100.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = item.category.replace("_", " "),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Brand700,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }

                                            Text(
                                                text = "₹${item.rate.toInt()} / ${item.unit}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Ink900
                                            )

                                            Text(
                                                text = "GST: ${item.taxRate.toInt()}%",
                                                fontSize = 10.5.sp,
                                                color = Ink600
                                            )

                                            Text(
                                                text = "HSN: ${item.hsn}",
                                                fontSize = 10.5.sp,
                                                color = Ink400
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (isImported) {
                                        Surface(
                                            color = Success50,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Success600, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Added", color = Success600, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                onImportSingleItem(item, selectedPreset.id)
                                                importedItemLabels = importedItemLabels + item.label
                                                Toast.makeText(context, "Added '${item.label}' to catalog", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Brand600),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = Brand600, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("+ Add", color = Brand600, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = Brand600, fontWeight = FontWeight.Bold)
            }
        }
    )
}
