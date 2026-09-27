package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.data.model.PredefinedKit

/**
 * Displays a horizontally scrollable row of predefined kit cards.
 * When tapped, calls [onKitSelected] with the kit.
 * The selected kit is visually highlighted.
 *
 * Used in Solar form for "3 kW Kit | 5 kW Kit | 10 kW Kit" selection,
 * and "Custom" option at the end.
 */
@Composable
fun KitSelectorRow(
    kits: List<PredefinedKit>,
    selectedIndex: Int,
    onKitSelected: (Int, PredefinedKit) -> Unit,
    onCustomSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        itemsIndexed(kits) { index, kit ->
            val isSelected = index == selectedIndex
            Card(
                modifier = Modifier
                    .width(160.dp)
                    .defaultMinSize(minHeight = 48.dp)
                    .semantics { contentDescription = "Kit option: ${kit.name}" }
                    .clickable { onKitSelected(index, kit) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary) else null
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = kit.iconEmoji, style = MaterialTheme.typography.titleMedium)
                    Text(text = kit.name, style = MaterialTheme.typography.titleMedium)
                    Text(text = kit.description, style = MaterialTheme.typography.bodySmall)
                    Text(text = "Rate: ${kit.defaultRatePerUnit}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item {
            val isCustomSelected = selectedIndex == kits.size
            Card(
                modifier = Modifier
                    .width(120.dp)
                    .defaultMinSize(minHeight = 48.dp)
                    .semantics { contentDescription = "Custom Kit option" }
                    .clickable { onCustomSelected() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCustomSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isCustomSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                border = if (isCustomSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary) else null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "+", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Custom", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
