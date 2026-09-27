package com.example.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatIndianGrouping(amount: Double, withDecimals: Boolean = true): String {
        val rounded = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
        val parts = rounded.toPlainString().split(".")
        val intPart = parts[0]
        val decPart = if (parts.size > 1) parts[1] else "00"

        val isNegative = intPart.startsWith("-")
        val digits = if (isNegative) intPart.substring(1) else intPart

        val formattedInt = if (digits.length <= 3) {
            digits
        } else {
            val last3 = digits.takeLast(3)
            val rest = digits.dropLast(3)
            val sb = StringBuilder()
            var count = 0
            for (i in rest.length - 1 downTo 0) {
                sb.insert(0, rest[i])
                count++
                if (count % 2 == 0 && i != 0) {
                    sb.insert(0, ',')
                }
            }
            sb.append(',').append(last3).toString()
        }

        val prefix = if (isNegative) "-" else ""
        return if (withDecimals) "$prefix$formattedInt.$decPart" else "$prefix$formattedInt"
    }

    fun money(amount: Double): String {
        return "₹ " + formatIndianGrouping(amount, true)
    }

    /**
     * Format a [Money] value with currency symbol.
     * Preferred over the Double overload for new code.
     */
    fun money(amount: com.example.core.money.Money): String {
        return amount.format()
    }

    /**
     * Format with a configurable currency symbol.
     * @param currencySymbol defaults to "₹" for backward compatibility
     */
    fun money(amount: Double, currencySymbol: String): String {
        return "$currencySymbol " + formatIndianGrouping(amount, true)
    }

    fun plain(amount: Double): String {
        return formatIndianGrouping(amount, true)
    }

    fun plain0(amount: Double): String {
        return formatIndianGrouping(amount, false)
    }

    fun formatInr(amount: Double): String {
        return formatIndianGrouping(amount, true)
    }


    fun todayIso(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun datePlusDaysIso(days: Int): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }

    fun fmtDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(isoDate) ?: return isoDate
            SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun fmtDateFile(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "doc"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(isoDate) ?: return "doc"
            SimpleDateFormat("dd-MM-yyyy", Locale.US).format(date)
        } catch (_: Exception) {
            "doc"
        }
    }

    fun waNumber(mobile: String?): String {
        val digits = (mobile ?: "").filter { it.isDigit() }
        return when {
            digits.length == 10 -> "91$digits"
            digits.length == 12 && digits.startsWith("91") -> digits
            digits.length == 11 && digits.startsWith("0") -> "91${digits.substring(1)}"
            else -> digits
        }
    }

    fun safeFileName(text: String): String {
        return text.trim()
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .replace(Regex("\\s+"), "_")
            .ifEmpty { "Customer" }
    }
}
