package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.core.money.Money

/**
 * Amount input field with built-in validation.
 *
 * Features:
 * - Only allows digits, single decimal point, and optional leading minus
 * - Rejects scientific notation, multiple dots, alphabetic characters
 * - Shows inline error if input is invalid
 * - Returns parsed [Money] value via [onValueChange]
 * - Auto-formats display with currency symbol on focus loss (optional)
 *
 * Usage:
 * ```
 * MoneyTextField(
 *     value = rateText,
 *     onValueChange = { text, money ->
 *         rateText = text
 *         parsedRate = money  // null if invalid
 *     },
 *     label = "Rate per kW",
 *     placeholder = "e.g. 60,000"
 * )
 * ```
 */
@Composable
fun MoneyTextField(
    value: String,
    onValueChange: (text: String, parsed: Money?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    prefix: String = "₹",
    allowNegative: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
) {
    // Validate on every change
    var hasBeenEdited by remember { mutableStateOf(false) }

    val validationRegex = if (allowNegative) {
        Regex("^-?\\d*\\.?\\d{0,2}$")
    } else {
        Regex("^\\d*\\.?\\d{0,2}$")
    }

    val showError = hasBeenEdited && value.isNotEmpty() && Money.of(value) == null
    val effectiveError = isError || showError
    val effectiveErrorMessage = when {
        showError -> "Enter a valid amount"
        isError && errorMessage != null -> errorMessage
        else -> null
    }

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            hasBeenEdited = true
            // Strip currency symbols, spaces, and commas for internal storage
            val cleaned = newValue
                .replace("₹", "")
                .replace("$", "")
                .replace(" ", "")
                .replace(",", "")
                .trim()

            // Only accept if it matches the allowed pattern (partial input is OK)
            if (cleaned.isEmpty() || cleaned.matches(validationRegex)) {
                val parsed = Money.of(cleaned)
                onValueChange(cleaned, parsed)
            }
            // If doesn't match, silently reject the keystroke (old value kept)
        },
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        prefix = if (prefix.isNotEmpty()) {
            { Text(prefix, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = singleLine,
        isError = effectiveError,
        enabled = enabled,
        readOnly = readOnly,
        supportingText = if (effectiveErrorMessage != null) {
            { Text(effectiveErrorMessage, color = MaterialTheme.colorScheme.error) }
        } else null,
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Quantity input field — similar to [MoneyTextField] but for non-monetary quantities.
 * Allows more decimal places (up to 4) for measurements like kW, sq.ft, etc.
 */
@Composable
fun QuantityTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    suffix: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
) {
    val validationRegex = Regex("^\\d*\\.?\\d{0,4}$")

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val cleaned = newValue.replace(",", "").replace(" ", "").trim()
            if (cleaned.isEmpty() || cleaned.matches(validationRegex)) {
                onValueChange(cleaned)
            }
        },
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        suffix = if (suffix.isNotEmpty()) {
            { Text(suffix, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = singleLine,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
    )
}
