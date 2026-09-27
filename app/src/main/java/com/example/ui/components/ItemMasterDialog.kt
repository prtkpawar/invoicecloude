package com.example.ui.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessCatalogPresets
import com.example.data.model.BusinessTypePreset
import com.example.data.model.ItemCategory
import com.example.data.model.ItemMaster
import com.example.data.model.ItemUnit
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemMasterEditDialog(
    item: ItemMaster? = null,
    businessTypeHint: String? = null,
    onDismiss: () -> Unit,
    onSave: (ItemMaster) -> Unit
) {
    val initialPreset = remember {
        if (!item?.businessType.isNullOrBlank()) {
            BusinessCatalogPresets.getPreset(item!!.businessType)
        } else {
            BusinessCatalogPresets.getPresetByBusinessType(businessTypeHint)
        }
    }

    var selectedPreset by remember { mutableStateOf(initialPreset) }

    // Core universal inputs
    var label by remember { mutableStateOf(item?.label ?: "") }
    var category by remember { mutableStateOf(item?.category ?: initialPreset.defaultCategories.firstOrNull() ?: ItemCategory.SOLAR_PANEL) }
    var unit by remember { mutableStateOf(item?.unit ?: initialPreset.defaultUnits.firstOrNull() ?: ItemUnit.NOS) }
    var taxRateText by remember { mutableStateOf((item?.taxRate ?: 18.0).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }) }
    var rateText by remember { mutableStateOf(if (item != null && item.rate > 0) item.rate.toString() else "") }
    var purchasePriceText by remember { mutableStateOf(if (item != null && (item.purchasePrice ?: 0.0) > 0) item.purchasePrice.toString() else "") }
    var hsn by remember { mutableStateOf(item?.hsn ?: "85414300") }
    var description by remember { mutableStateOf(item?.description ?: "") }

    // Specialized Preset-Aware Inputs
    var mrpText by remember { mutableStateOf(if (item?.mrp != null && item.mrp!! > 0) item.mrp.toString() else "") }
    var barcode by remember { mutableStateOf(item?.barcode ?: "") }
    var batchNo by remember { mutableStateOf(item?.batchNo ?: "") }
    var expiry by remember { mutableStateOf(item?.expiry ?: "") }
    var warranty by remember { mutableStateOf(item?.warranty ?: "") }
    var brand by remember { mutableStateOf(item?.brand ?: "") }
    var extraAttributes by remember { mutableStateOf(item?.extraAttributes ?: "") }

    var labelError by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Ink900,
        unfocusedTextColor = Ink900,
        focusedBorderColor = Brand600,
        unfocusedBorderColor = Line,
        focusedLabelColor = Brand600,
        unfocusedLabelColor = Ink600,
        cursorColor = Brand600,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )
    val textStyle = androidx.compose.ui.text.TextStyle(color = Ink900, fontSize = 13.5.sp)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Brand100, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selectedPreset.iconEmoji, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (item == null) "New Catalog Item" else "Edit Item Master",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.5.sp,
                                color = Ink900
                            )
                            Text(
                                text = "${selectedPreset.title} • Adaptive Form",
                                fontSize = 11.5.sp,
                                color = Brand600,
                                fontWeight = FontWeight.Medium
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
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Preset Selector Chips
                Text(
                    text = "Business Catalog Preset",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink600
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BusinessCatalogPresets.ALL_PRESETS.forEach { preset ->
                        val isSelected = selectedPreset.id == preset.id
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Brand600 else SurfaceSoft,
                            border = if (!isSelected) BorderStroke(1.dp, Line) else null,
                            modifier = Modifier.clickable {
                                selectedPreset = preset
                                // If adding fresh, suggest first unit/category from the newly chosen preset
                                if (item == null) {
                                    preset.defaultUnits.firstOrNull()?.let { unit = it }
                                    preset.defaultCategories.firstOrNull()?.let { category = it }
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(preset.iconEmoji, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = preset.title.split("&").first().trim(),
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Ink900
                                )
                            }
                        }
                    }
                }

                // Dynamic Placeholder depending on active preset
                val namePlaceholder = when (selectedPreset.id) {
                    BusinessCatalogPresets.KIRANA -> "e.g. Basmati Rice 1kg or Fortune Oil 1L"
                    BusinessCatalogPresets.MEDICAL -> "e.g. Paracetamol 650mg or Amoxicillin 500mg"
                    BusinessCatalogPresets.SALON -> "e.g. Men's Haircut & Styling or Organic Facial"
                    BusinessCatalogPresets.RETAIL -> "e.g. Cotton Casual Shirt or Running Shoes"
                    BusinessCatalogPresets.DISTRIBUTOR -> "e.g. Cold Drinks Case 24 or Maggi Carton 48"
                    BusinessCatalogPresets.RESTAURANT -> "e.g. Paneer Butter Masala or Veg Thali"
                    BusinessCatalogPresets.HARDWARE -> "e.g. UltraTech Cement 50kg or 1.5 sq mm Wire"
                    BusinessCatalogPresets.SWEET_SHOP -> "e.g. Special Kaju Katli or Gulab Jamun 1kg"
                    BusinessCatalogPresets.SOLAR -> "e.g. 540W Mono PERC Solar Panel"
                    BusinessCatalogPresets.CONSTRUCTION -> "e.g. RCC Slab Casting Work or Brick Masonry"
                    BusinessCatalogPresets.ELECTRONICS -> "e.g. Fast Type-C Charger 33W or TWS Earbuds"
                    BusinessCatalogPresets.SERVICES -> "e.g. Annual Maintenance Contract (AMC)"
                    else -> "e.g. Standard Product or Service"
                }

                // Core 1: Item Name / Label
                OutlinedTextField(
                    value = label,
                    onValueChange = {
                        label = it
                        labelError = it.isBlank()
                    },
                    label = { Text("Item / Service Name *") },
                    placeholder = { Text(namePlaceholder, color = Ink400) },
                    isError = labelError,
                    supportingText = if (labelError) { { Text("Item name is required", color = Color(0xFFDC2626)) } } else null,
                    colors = fieldColors,
                    textStyle = textStyle,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Core 2: Category Selector
                Text(
                    text = "Category (Tailored for ${selectedPreset.title.split("&").first().trim()})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink600
                )
                val relevantCategories = remember(selectedPreset) {
                    (selectedPreset.defaultCategories + ItemCategory.ALL).distinct()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    relevantCategories.forEach { cat ->
                        val isSelected = category == cat
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Brand600 else SurfaceSoft,
                            border = if (!isSelected) BorderStroke(1.dp, Line) else null,
                            modifier = Modifier.clickable { category = cat }
                        ) {
                            Text(
                                text = cat.replace("_", " "),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Ink900,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Core 3: Unit of Measurement
                Text(
                    text = "Unit of Measure (Primary Units for ${selectedPreset.title.split("&").first().trim()})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink600
                )
                val relevantUnits = remember(selectedPreset) {
                    (selectedPreset.defaultUnits + ItemUnit.ALL).distinct()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    relevantUnits.forEach { u ->
                        val isSelected = unit == u
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(0xFF0284C7) else SurfaceSoft,
                            border = if (!isSelected) BorderStroke(1.dp, Line) else null,
                            modifier = Modifier.clickable { unit = u }
                        ) {
                            Text(
                                text = u.replace("_", " "),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Ink900,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Core 4: Selling Rate & Purchase Cost Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text("Selling Rate (₹) *") },
                        placeholder = { Text("0.00", color = Ink400) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = fieldColors,
                        textStyle = textStyle,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = purchasePriceText,
                        onValueChange = { purchasePriceText = it },
                        label = { Text("Cost / Purchase (₹)") },
                        placeholder = { Text("0.00", color = Ink400) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = fieldColors,
                        textStyle = textStyle,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Core 5: Tax Rate & HSN Code Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = taxRateText,
                        onValueChange = { taxRateText = it },
                        label = { Text("GST Rate (%)") },
                        placeholder = { Text("18", color = Ink400) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = fieldColors,
                        textStyle = textStyle,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = hsn,
                        onValueChange = { hsn = it },
                        label = { Text("HSN / SAC Code") },
                        placeholder = { Text("e.g. 85414300", color = Ink400) },
                        colors = fieldColors,
                        textStyle = textStyle,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // ---------------- ADAPTIVE PRESET-SPECIFIC INPUTS ----------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Brand600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${selectedPreset.title} • Specialized Fields",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Ink900
                            )
                        }

                        when (selectedPreset.id) {
                            BusinessCatalogPresets.KIRANA,
                            BusinessCatalogPresets.RETAIL,
                            BusinessCatalogPresets.DISTRIBUTOR -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = mrpText,
                                        onValueChange = { mrpText = it },
                                        label = { Text("MRP (₹)") },
                                        placeholder = { Text("e.g. 100", color = Ink400) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = barcode,
                                        onValueChange = { barcode = it },
                                        label = { Text("Barcode / EAN") },
                                        placeholder = { Text("89010309...", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                OutlinedTextField(
                                    value = brand,
                                    onValueChange = { brand = it },
                                    label = { Text("Brand / Manufacturer") },
                                    placeholder = { Text("e.g. Tata, Amul, Fortune, Britannia", color = Ink400) },
                                    colors = fieldColors,
                                    textStyle = textStyle,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            BusinessCatalogPresets.MEDICAL -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = batchNo,
                                        onValueChange = { batchNo = it.uppercase() },
                                        label = { Text("Batch #") },
                                        placeholder = { Text("e.g. BCH-2401", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = expiry,
                                        onValueChange = { expiry = it },
                                        label = { Text("Expiry (MM/YY)") },
                                        placeholder = { Text("e.g. 12/26", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                OutlinedTextField(
                                    value = extraAttributes,
                                    onValueChange = { extraAttributes = it },
                                    label = { Text("Drug Salt / Composition") },
                                    placeholder = { Text("e.g. Paracetamol IP 650mg + Caffeine 30mg", color = Ink400) },
                                    colors = fieldColors,
                                    textStyle = textStyle,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            BusinessCatalogPresets.SOLAR -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = extraAttributes,
                                        onValueChange = { extraAttributes = it },
                                        label = { Text("Capacity / Wattage") },
                                        placeholder = { Text("e.g. 540W / 3.3 kW", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = warranty,
                                        onValueChange = { warranty = it },
                                        label = { Text("Warranty Terms") },
                                        placeholder = { Text("e.g. 10Y Product / 25Y Performance", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                OutlinedTextField(
                                    value = brand,
                                    onValueChange = { brand = it },
                                    label = { Text("Tier-1 Brand / OEM") },
                                    placeholder = { Text("e.g. Waaree, Tata Power, Adani, Growatt", color = Ink400) },
                                    colors = fieldColors,
                                    textStyle = textStyle,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            BusinessCatalogPresets.CONSTRUCTION -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = extraAttributes,
                                        onValueChange = { extraAttributes = it },
                                        label = { Text("Work Phase") },
                                        placeholder = { Text("e.g. Slab Casting / Foundation", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = warranty,
                                        onValueChange = { warranty = it },
                                        label = { Text("Scope / Contract Type") },
                                        placeholder = { Text("e.g. Material + Labor", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            BusinessCatalogPresets.ELECTRONICS -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = brand,
                                        onValueChange = { brand = it },
                                        label = { Text("Brand / Model") },
                                        placeholder = { Text("e.g. Samsung / Apple", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = warranty,
                                        onValueChange = { warranty = it },
                                        label = { Text("Warranty") },
                                        placeholder = { Text("e.g. 1 Year Brand Warranty", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            BusinessCatalogPresets.SALON -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = extraAttributes,
                                        onValueChange = { extraAttributes = it },
                                        label = { Text("Service Duration") },
                                        placeholder = { Text("e.g. 45 Mins", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = brand,
                                        onValueChange = { brand = it },
                                        label = { Text("Audience / Target") },
                                        placeholder = { Text("e.g. Unisex / Men / Women", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            BusinessCatalogPresets.RESTAURANT -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = extraAttributes,
                                        onValueChange = { extraAttributes = it },
                                        label = { Text("Dietary Type") },
                                        placeholder = { Text("Pure Veg / Non-Veg / Egg", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = warranty,
                                        onValueChange = { warranty = it },
                                        label = { Text("Portion Size") },
                                        placeholder = { Text("e.g. Full / Half / 2 Pcs", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            BusinessCatalogPresets.HARDWARE -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = extraAttributes,
                                        onValueChange = { extraAttributes = it },
                                        label = { Text("Size / Dimensions") },
                                        placeholder = { Text("e.g. 1/2\", 25mm, 20L Bucket", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = brand,
                                        onValueChange = { brand = it },
                                        label = { Text("Grade / Brand") },
                                        placeholder = { Text("e.g. UltraTech, Asian Paints", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            BusinessCatalogPresets.SWEET_SHOP -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = extraAttributes,
                                        onValueChange = { extraAttributes = it },
                                        label = { Text("Shelf Life / Freshness") },
                                        placeholder = { Text("e.g. 3 Days / 1 Week", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = warranty,
                                        onValueChange = { warranty = it },
                                        label = { Text("Preparation Type") },
                                        placeholder = { Text("Pure Desi Ghee / Mawa", color = Ink400) },
                                        colors = fieldColors,
                                        textStyle = textStyle,
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            else -> {
                                OutlinedTextField(
                                    value = extraAttributes,
                                    onValueChange = { extraAttributes = it },
                                    label = { Text("Custom Industry Attributes / Scope") },
                                    placeholder = { Text("e.g. Milestone basis, Turnaround time", color = Ink400) },
                                    colors = fieldColors,
                                    textStyle = textStyle,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Core 6: Description & Specifications
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Item Specifications / Notes") },
                    placeholder = { Text("e.g. Technical specs, pack size, certification, or warranty terms", color = Ink400) },
                    colors = fieldColors,
                    textStyle = textStyle,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (label.isBlank()) {
                        labelError = true
                        return@Button
                    }
                    val rate = rateText.toDoubleOrNull() ?: 0.0
                    val purchasePrice = purchasePriceText.toDoubleOrNull()
                    val taxRate = taxRateText.toDoubleOrNull() ?: 18.0
                    val mrp = mrpText.toDoubleOrNull()

                    val updated = (item ?: ItemMaster(
                        label = label.trim(),
                        description = description.trim(),
                        category = category,
                        unit = unit,
                        taxRate = taxRate,
                        rate = rate,
                        purchasePrice = purchasePrice,
                        hsn = hsn.trim().ifBlank { "85414300" },
                        businessType = selectedPreset.id,
                        mrp = mrp,
                        barcode = barcode.trim().ifBlank { null },
                        batchNo = batchNo.trim().ifBlank { null },
                        expiry = expiry.trim().ifBlank { null },
                        warranty = warranty.trim().ifBlank { null },
                        brand = brand.trim().ifBlank { null },
                        extraAttributes = extraAttributes.trim().ifBlank { null }
                    )).copy(
                        label = label.trim(),
                        description = description.trim(),
                        category = category,
                        unit = unit,
                        taxRate = taxRate,
                        rate = rate,
                        purchasePrice = purchasePrice,
                        hsn = hsn.trim().ifBlank { "85414300" },
                        businessType = selectedPreset.id,
                        mrp = mrp,
                        barcode = barcode.trim().ifBlank { null },
                        batchNo = batchNo.trim().ifBlank { null },
                        expiry = expiry.trim().ifBlank { null },
                        warranty = warranty.trim().ifBlank { null },
                        brand = brand.trim().ifBlank { null },
                        extraAttributes = extraAttributes.trim().ifBlank { null }
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Item", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Ink600)
            }
        }
    )
}
