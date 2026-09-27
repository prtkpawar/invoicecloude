package com.example.utils

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToLong

data class Totals(
    val grossSubtotal: Double = 0.0,
    val discount: Double = 0.0,
    val taxable: Double,
    val cgst: Double,
    val sgst: Double,
    val total: Double
) {
    val tax: Double get() = cgst + sgst
}

/**
 * @deprecated Use [com.example.core.money.InvoiceTotalsEngine] instead.
 * This object uses Double-based math which causes silent rounding drift.
 * The new engine uses BigDecimal (Money value class) for zero-error precision.
 */
@Deprecated("Use InvoiceTotalsEngine for precise BigDecimal-based calculations")
object Calc {
    const val MODE_ROUND = "ROUND_TOTAL"
    const val MODE_FORWARD = "FORWARD"

    @Deprecated("Use Money.of() instead", replaceWith = ReplaceWith("Money.of(v)", "com.example.core.money.Money"))
    fun r2(v: Double): Double {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    @Deprecated("Use InvoiceTotalsEngine.compute() or computeReverse() instead")
    fun compute(
        pricingMode: String,
        gstPercent: Double,
        kw: Double = 0.0,
        ratePerKw: Double = 0.0,
        roundTotal: Double = 0.0,
        discount: Double = 0.0,
        installationCharges: Double = 0.0
    ): Totals {
        val g = if (gstPercent <= 0.0) 0.0 else gstPercent
        val disc = if (discount < 0.0) 0.0 else r2(discount)
        val install = if (installationCharges < 0.0) 0.0 else r2(installationCharges)
        return if (pricingMode == MODE_ROUND) {
            val total = r2(roundTotal + install)
            if (g <= 0.0) {
                val gross = r2(total + disc)
                Totals(grossSubtotal = gross, discount = disc, taxable = total, cgst = 0.0, sgst = 0.0, total = total)
            } else {
                val netTaxable = r2(total / (1.0 + g / 100.0))
                val gross = r2(netTaxable + disc)
                val half = r2(netTaxable * (g / 2.0) / 100.0)
                val adjTaxable = r2(total - half - half)
                Totals(grossSubtotal = gross, discount = disc, taxable = adjTaxable, cgst = half, sgst = half, total = total)
            }
        } else {
            val gross = r2(kw * ratePerKw + install)
            val taxable = r2(maxOf(0.0, gross - disc))
            val half = if (g <= 0.0) 0.0 else r2(taxable * (g / 2.0) / 100.0)
            val total = r2(taxable + half + half)
            Totals(grossSubtotal = gross, discount = disc, taxable = taxable, cgst = half, sgst = half, total = total)
        }
    }

    @Deprecated("Use InvoiceTotalsEngine.compute() instead")
    fun computeFromItems(
        itemsTotal: Double,
        gstPercent: Double,
        discount: Double = 0.0
    ): Totals {
        val g = if (gstPercent <= 0.0) 0.0 else gstPercent
        val disc = if (discount < 0.0) 0.0 else r2(discount)
        val gross = r2(itemsTotal)
        val taxable = r2(maxOf(0.0, gross - disc))
        val half = if (g <= 0.0) 0.0 else r2(taxable * (g / 2.0) / 100.0)
        val total = r2(taxable + half + half)
        return Totals(grossSubtotal = gross, discount = disc, taxable = taxable, cgst = half, sgst = half, total = total)
    }
}
