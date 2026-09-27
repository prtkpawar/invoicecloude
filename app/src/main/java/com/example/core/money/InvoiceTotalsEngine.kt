package com.example.core.money

import java.math.BigDecimal

/**
 * Discount can be flat amount OR percentage.
 */
sealed interface Discount {
    data class Flat(val amount: Money) : Discount
    data class Percent(val rate: Money) : Discount {
        fun amountOn(base: Money): Money = base.pct(rate)
    }
    companion object {
        val NONE: Discount = Flat(Money.ZERO)
    }
}

/** A single tax component with name, rate, and computed amount. */
data class TaxComponent(
    val name: String,
    val ratePct: Money,
    val amount: Money
)

/** A per-item line with pre-computed extension (qty × rate). */
data class LineMoney(
    val label: String = "",
    val qty: BigDecimal = BigDecimal.ONE,
    val rate: Money = Money.ZERO,
    val extension: Money = Money.ZERO
)

/** Tax rule definition. */
data class TaxRule(
    val name: String,
    val ratePct: Money,
    val order: Int = 0
)

/**
 * Immutable result of invoice calculation. All values are in Money (BigDecimal scale-2).
 */
data class InvoiceTotals(
    val grossSubtotal: Money,
    val discountAmount: Money,
    val taxable: Money,
    val taxes: List<TaxComponent>,
    val grandTotal: Money,
) {
    val taxTotal: Money get() = taxes.fold(Money.ZERO) { acc, t -> acc + t.amount }
}

/**
 * Pure, deterministic invoice calculation engine.
 * Same inputs ALWAYS produce the same outputs. Zero floating-point.
 *
 * Replaces the legacy [com.example.utils.Calc] which used Double-based math
 * with precision-destroying r2() round-trips.
 */
object InvoiceTotalsEngine {

    /**
     * Forward calculation: line items → discount → tax → grand total.
     *
     * @param lineItems Pre-resolved line items (qty × rate = extension)
     * @param discount  Flat amount or percentage discount
     * @param taxRules  Tax rules to apply (e.g., CGST 9%, SGST 9%)
     * @param compound  If true, each tax is applied on (taxable + prior taxes)
     */
    fun compute(
        lineItems: List<LineMoney>,
        discount: Discount = Discount.NONE,
        taxRules: List<TaxRule> = emptyList(),
        compound: Boolean = false,
    ): InvoiceTotals {
        val gross = lineItems.fold(Money.ZERO) { acc, l -> acc + l.extension }
        val discountAmount = when (discount) {
            is Discount.Flat -> discount.amount.coerceAtLeast(Money.ZERO)
            is Discount.Percent -> discount.amountOn(gross)
        }
        val taxable = (gross - discountAmount).coerceAtLeast(Money.ZERO)

        val taxes = mutableListOf<TaxComponent>()
        var runningBase = taxable
        for (rule in taxRules.sortedBy { it.order }) {
            val base = if (compound) runningBase else taxable
            val amount = base.pct(rule.ratePct)
            taxes += TaxComponent(rule.name, rule.ratePct, amount)
            if (compound) runningBase = runningBase + amount
        }
        val taxSum = taxes.fold(Money.ZERO) { acc, t -> acc + t.amount }
        return InvoiceTotals(gross, discountAmount, taxable, taxes, taxable + taxSum)
    }

    /**
     * Reverse (round-total) calculation: given a fixed total, back-compute taxable and taxes.
     * Used for "Fixed Total" pricing mode.
     */
    fun computeReverse(
        fixedTotal: Money,
        discount: Discount = Discount.NONE,
        taxRules: List<TaxRule> = emptyList(),
    ): InvoiceTotals {
        if (taxRules.isEmpty()) {
            val discountAmount = when (discount) {
                is Discount.Flat -> discount.amount.coerceAtLeast(Money.ZERO)
                is Discount.Percent -> Money.ZERO // Can't reverse with % discount and no tax
            }
            val gross = fixedTotal + discountAmount
            return InvoiceTotals(gross, discountAmount, fixedTotal, emptyList(), fixedTotal)
        }

        // Sum of tax rates for simple (non-compound) reverse calc
        val totalTaxRate = taxRules.fold(Money.ZERO) { acc, r -> acc + r.ratePct }
        val divisor = Money.ZERO.value.add(BigDecimal("100")).add(totalTaxRate.value)
        val netTaxable = Money.of(
            fixedTotal.value.multiply(BigDecimal("100")).divide(divisor, 2, java.math.RoundingMode.HALF_UP)
        )

        val taxes = taxRules.sortedBy { it.order }.map { rule ->
            TaxComponent(rule.name, rule.ratePct, netTaxable.pct(rule.ratePct))
        }
        val taxSum = taxes.fold(Money.ZERO) { acc, t -> acc + t.amount }
        // Adjust taxable so that taxable + taxes = fixedTotal exactly
        val adjTaxable = fixedTotal - taxSum

        val discountAmount = when (discount) {
            is Discount.Flat -> discount.amount.coerceAtLeast(Money.ZERO)
            is Discount.Percent -> Money.ZERO
        }
        val gross = adjTaxable + discountAmount

        return InvoiceTotals(gross, discountAmount, adjTaxable, taxes, fixedTotal)
    }

    /**
     * Legacy bridge: converts legacy Double parameters to Money and calls compute().
     * Use this for gradual migration — new code should call compute() directly.
     */
    fun computeLegacy(
        itemsTotal: Double,
        gstPercent: Double,
        discount: Double = 0.0
    ): InvoiceTotals {
        val lineItems = listOf(LineMoney(extension = Money.of(itemsTotal)))
        val discountMoney = Discount.Flat(Money.of(discount))
        val halfGst = gstPercent / 2.0
        val taxRules = if (gstPercent > 0.0) listOf(
            TaxRule("CGST", Money.of(halfGst), 0),
            TaxRule("SGST", Money.of(halfGst), 1)
        ) else emptyList()
        return compute(lineItems, discountMoney, taxRules)
    }
}
