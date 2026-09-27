package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Editable numbered list of terms & conditions.
 * Each term can be edited inline, removed, or reordered.
 * A "+ Add Term" button appends new terms.
 * A "Reset to Default" button restores the original terms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsEditor(
    terms: List<String>,
    onTermsChanged: (List<String>) -> Unit,
    defaultTerms: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        terms.forEachIndexed { index, term ->
            AnimatedVisibility(visible = true) {
                TextField(
                    value = term,
                    onValueChange = { newValue ->
                        val newTerms = terms.toMutableList()
                        newTerms[index] = newValue
                        onTermsChanged(newTerms)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    trailingIcon = if (terms.size > 1) {
                        {
                            IconButton(
                                onClick = {
                                    val newTerms = terms.toMutableList()
                                    newTerms.removeAt(index)
                                    onTermsChanged(newTerms)
                                },
                                modifier = Modifier.semantics { contentDescription = "Remove term ${index + 1}" }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    } else null
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                val newTerms = terms.toMutableList()
                newTerms.add("")
                onTermsChanged(newTerms)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Term")
                Spacer(Modifier.width(4.dp))
                Text("Add Term")
            }
            
            if (terms != defaultTerms) {
                TextButton(onClick = { onTermsChanged(defaultTerms) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset to Default")
                    Spacer(Modifier.width(4.dp))
                    Text("Reset to Default")
                }
            }
        }
    }
}
