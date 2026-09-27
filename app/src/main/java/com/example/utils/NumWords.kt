package com.example.utils

import kotlin.math.roundToLong

object NumWords {
    private val ones = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )

    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy",
        "Eighty", "Ninety"
    )

    private fun two(n: Long): String {
        if (n == 0L) return ""
        if (n < 20L) return ones[n.toInt()]
        val t = tens[(n / 10).toInt()]
        val o = (n % 10).toInt()
        return if (o == 0) t else "$t ${ones[o]}"
    }

    private fun three(n: Long): String {
        val h = n / 100
        val rest = n % 100
        val parts = mutableListOf<String>()
        if (h > 0) parts.add("${ones[h.toInt()]} Hundred")
        if (rest > 0) parts.add(two(rest))
        return parts.joinToString(" ")
    }

    private fun number(num: Long): String {
        if (num == 0L) return ""
        var n = num
        val parts = mutableListOf<String>()

        val crore = n / 10_000_000
        n %= 10_000_000

        val lakh = n / 100_000
        n %= 100_000

        val thousand = n / 1_000
        n %= 1_000

        if (crore > 0) parts.add("${two(crore)} Crore")
        if (lakh > 0) parts.add("${two(lakh)} Lakh")
        if (thousand > 0) parts.add("${two(thousand)} Thousand")
        if (n > 0) parts.add(three(n))

        return parts.joinToString(" ").trim()
    }

    fun rupees(amount: Double): String {
        val totalPaise = (amount * 100).roundToLong()
        val rs = totalPaise / 100
        val paise = totalPaise % 100

        var words = number(rs)
        if (words.isEmpty()) words = "Zero"
        val sb = StringBuilder("$words Rupees")
        if (paise > 0) {
            sb.append(" and ${two(paise)} Paise")
        }
        return sb.toString()
    }
}
