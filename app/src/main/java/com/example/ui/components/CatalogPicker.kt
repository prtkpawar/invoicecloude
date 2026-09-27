package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CatalogCategory
import com.example.data.model.CatalogItem

/**
 * Category-tabbed catalog picker with item chips.
 *
 * Shows scrollable tabs at the top (one per category),
 * then a grid/list of items in the selected category.
 * Tapping an item calls [onItemAdded].
 *
 * @param categories Available catalog categories with items
 * @param onItemAdded Called when user taps an item to add it to the form
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogPicker(
    categories: List<CatalogCategory>,
    onItemAdded: (CatalogItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) return

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    // Guard against index out of bounds if list changes
    if (selectedTabIndex >= categories.size) {
        selectedTabIndex = 0
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 8.dp,
            divider = {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = if (category.iconEmoji.isNotBlank()) "${category.iconEmoji} ${category.label}" else category.label,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }

        val selectedCategory = categories[selectedTabIndex]

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(selectedCategory.items) { item ->
                OutlinedCard(
                    onClick = { onItemAdded(item) },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier.defaultMinSize(minHeight = 100.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (!item.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "₹${item.defaultRate}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        text = item.unit,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = { onItemAdded(item) },
                                modifier = Modifier.size(32.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add ${item.label}",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact horizontal chip picker for quick-add items.
 * Shows a row of tappable chips from a flat items list.
 */
@Composable
fun QuickItemChips(
    items: List<CatalogItem>,
    onItemAdded: (CatalogItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(items) { item ->
            SuggestionChip(
                onClick = { onItemAdded(item) },
                label = { Text(item.label) }
            )
        }
    }
}
