package com.example

import com.example.utils.Calc
import com.example.utils.Formatters
import com.example.utils.NumWords
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testExactEst00068GstCalculation() {
    // 5kW system, ₹1,85,000 round total with 5% GST (2.5% CGST + 2.5% SGST)
    val totals = Calc.compute(
      pricingMode = Calc.MODE_ROUND,
      gstPercent = 5.0,
      kw = 5.0,
      ratePerKw = 60000.0,
      roundTotal = 185000.0
    )

    assertEquals(185000.0, totals.total, 0.01)
    assertEquals(176190.48, totals.taxable, 0.01)
    assertEquals(4404.76, totals.cgst, 0.01)
    assertEquals(4404.76, totals.sgst, 0.01)
    assertEquals(8809.52, totals.tax, 0.01)

    val inWords = NumWords.rupees(totals.total)
    assertEquals("One Lakh Eighty Five Thousand Rupees", inWords)
  }

  @Test
  fun testForwardCalculation() {
    val totals = Calc.compute(
      pricingMode = Calc.MODE_FORWARD,
      gstPercent = 5.0,
      kw = 3.0,
      ratePerKw = 40000.0
    )

    assertEquals(120000.0, totals.taxable, 0.01)
    assertEquals(3000.0, totals.cgst, 0.01)
    assertEquals(3000.0, totals.sgst, 0.01)
    assertEquals(126000.0, totals.total, 0.01)
  }

  @Test
  fun testZeroGstCalculation() {
    val totals = Calc.compute(
      pricingMode = Calc.MODE_ROUND,
      gstPercent = 0.0,
      kw = 5.0,
      roundTotal = 180000.0
    )

    assertEquals(180000.0, totals.taxable, 0.01)
    assertEquals(0.0, totals.cgst, 0.01)
    assertEquals(0.0, totals.sgst, 0.01)
    assertEquals(180000.0, totals.total, 0.01)
  }

  @Test
  fun testFormatters() {
    assertEquals("₹ 1,85,000.00", Formatters.money(185000.0))
    assertEquals("1,85,000.00", Formatters.plain(185000.0))
    assertEquals("919876543210", Formatters.waNumber("+91 98765 43210"))
  }
}
