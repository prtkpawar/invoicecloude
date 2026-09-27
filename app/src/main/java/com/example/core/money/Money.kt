package com.example.core.money

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Immutable monetary value. Scale is FIXED at 2, rounding HALF_UP.
 * NEVER expose the wrapped BigDecimal as Double anywhere.
 */
@JvmInline
value class Money(val value: BigDecimal) : Comparable<Money> {

    init { require(value.scale() <= 2) { "Money must be scale ≤ 2, got ${value.scale()}" } }

    operator fun plus(o: Money)  = Money((value + o.value).normalized())
    operator fun minus(o: Money) = Money((value - o.value).normalized())
    operator fun times(qty: BigDecimal) = Money((value * qty).normalized())
    fun pct(percent: Money): Money =
        Money(value.multiply(percent.value).divide(HUNDRED, 2, RoundingMode.HALF_UP))
    fun half(): Money = Money(value.divide(BigDecimal("2"), 2, RoundingMode.HALF_UP))

    fun isZero() = value.signum() == 0
    fun isNegative() = value.signum() < 0
    fun isPositive() = value.signum() > 0
    fun coerceAtLeast(min: Money) = if (this < min) min else this
    override fun compareTo(other: Money) = value.compareTo(other.value)
    fun toDouble(): Double = value.toDouble() // Only for legacy interop — avoid new usage

    /**
     * Indian-system grouping (1,00,000) via the JDK.
     */
    fun format(
        locale: Locale = Locale.forLanguageTag("en-IN"),
        currency: Currency = Currency.getInstance("INR")
    ): String = NumberFormat.getCurrencyInstance(locale)
        .apply { this.currency = currency; maximumFractionDigits = 2; minimumFractionDigits = 2 }
        .format(value)

    fun formatPlain(): String = value.toPlainString()

    companion object {
        private val HUNDRED = BigDecimal("100")
        val ZERO = Money(BigDecimal.ZERO.setScale(2))

        fun of(amount: BigDecimal): Money = Money(amount.setScale(2, RoundingMode.HALF_UP))

        fun of(amount: Double): Money = of(BigDecimal.valueOf(amount))

        /**
         * Safe parser: rejects scientific notation, multiple dots, garbage.
         * Returns null on invalid input instead of silently returning 0.
         */
        fun of(amount: String): Money? {
            val stripped = amount.trim().replace(",", "")
            if (stripped.isEmpty()) return null
            // Only allow: optional minus, digits, optional single dot with up to 2 decimal places
            if (!stripped.matches(Regex("-?\\d+(\\.\\d{1,2})?"))) return null
            return runCatching { of(BigDecimal(stripped)) }.getOrNull()
        }

        /** Legacy interop: wraps a Double into Money with proper rounding */
        fun fromLegacyDouble(d: Double): Money = of(BigDecimal.valueOf(d))
    }
}

private fun BigDecimal.normalized(): BigDecimal = setScale(2, RoundingMode.HALF_UP)
