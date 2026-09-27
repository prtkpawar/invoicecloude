package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.entity.BusinessProfile
import com.example.data.model.Company
import com.example.data.model.Customer
import com.example.data.model.Doc
import com.example.data.model.DocItem
import com.example.data.model.DocStatus
import com.example.data.model.DocWithDetails
import com.example.data.model.Firm
import com.example.data.model.FirmBranding
import com.example.data.model.invoice.DynamicInvoiceData
import com.example.data.model.invoice.DynamicLineItem
import com.example.data.model.invoice.InvoiceBusinessType
import com.example.data.model.invoice.InvoiceTemplateFactory
import com.example.data.model.LedgerEntry
import com.example.data.model.Party
import com.example.data.model.PartyStatementData
import com.example.data.model.Payment
import com.example.data.model.PdfTheme
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

/**
 * Adaptive Multi-Theme PDF Generation Engine (Option 3 Hybrid System).
 * Dynamically formats and renders Quotes, Invoices, Receipts, and Ledgers
 * with full adaptability for any Business Profile (Logo, Legal & Brand Name, Bank Mandate,
 * UPI QR, Signatory & Digital Seal, Colors, and Layout Themes).
 */
object PdfGenerator {

    // Global Base Palette
    object Brand {
        val INK = Color.parseColor("#111827")          // Deep black body text
        val MUTED = Color.parseColor("#4B5563")        // Crisp medium gray
        val SUB_ITEM = Color.parseColor("#374151")     // Equipment / subitem gray
        val FAINT = Color.parseColor("#9CA3AF")        // Light gray for fine print
        val LINE = Color.parseColor("#CBD5E1")         // Standard grid rule (0.8pt)
        val LINE_LIGHT = Color.parseColor("#E2E8F0")   // Crisp subtle divider (0.6pt)
        val LINE_STRONG = Color.parseColor("#1E293B")  // Bold frame rule (1.1pt)
        val BG_HEADER = Color.parseColor("#F1F5F9")    // Neutral light gray for table headers
        val BG_SEAL = Color.parseColor("#F8FAFC")      // Subtle tint for signature box
        val WHITE = Color.WHITE

        // Swami Solar Legacy Emblem Palette
        val LOGO_NAVY = Color.parseColor("#0B4B8B")
        val LOGO_GOLD = Color.parseColor("#E68A00")
        val LOGO_GLOBE_DEEP = Color.parseColor("#0D47A1")
        val LOGO_GLOBE_MID = Color.parseColor("#0288D1")
        val LOGO_GLOBE_LIGHT = Color.parseColor("#29B6F6")
        val LOGO_SUB = Color.parseColor("#D97706")
    }

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_LEFT = 34f
    private const val MARGIN_RIGHT = 561f
    private const val CONTENT_WIDTH = MARGIN_RIGHT - MARGIN_LEFT
    const val HSN_SAC_CONSTANT = "85414300"

    private val ROMAN_NUMERALS = listOf("i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x", "xi", "xii")

    fun toRoman(index: Int): String {
        return if (index in ROMAN_NUMERALS.indices) "${ROMAN_NUMERALS[index]}." else "${index + 1}."
    }

    /**
     * Centralized Typography Engine: Loads Times New Roman with subpixel and linear text flags.
     * Prevents horizontal font overlapping and glyph collision.
     */
    class FontKit(context: Context) {
        val regular: Typeface = try {
            ResourcesCompat.getFont(context, R.font.times_new_roman) ?: Typeface.SERIF
        } catch (e: Exception) {
            Typeface.SERIF
        }
        val bold: Typeface = Typeface.create(regular, Typeface.BOLD)
        val italic: Typeface = Typeface.create(regular, Typeface.ITALIC)
        val boldItalic: Typeface = Typeface.create(regular, Typeface.BOLD_ITALIC)

        fun paint(
            color: Int = Brand.INK,
            size: Float = 8.5f,
            bold: Boolean = false,
            italic: Boolean = false,
            align: Paint.Align = Paint.Align.LEFT
        ): Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
            this.color = color
            this.textSize = size
            this.textAlign = align
            this.isSubpixelText = true
            this.isLinearText = true
            this.typeface = when {
                bold && italic -> boldItalic
                bold -> this@FontKit.bold
                italic -> this@FontKit.italic
                else -> this@FontKit.regular
            }
        }
    }

    /**
     * Helper to wrap text into multiple lines so that it does not exceed maxWidth.
     */
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return emptyList()
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "${currentLine} $word"
            if (paint.measureText(candidate) <= maxWidth) {
                currentLine = StringBuilder(candidate)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                } else {
                    lines.add(word)
                    currentLine = StringBuilder()
                }
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    /**
     * Draws the Solar 3D Solar PV Globe with Golden Orbital Arc.
     */
    fun drawSwamiSolarLogo(canvas: Canvas, x: Float, y: Float, height: Float = 44f, firm: FirmBranding? = null) {
        val scale = height / 44f
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        // Golden Orbital Crescent Arc
        val arcPath = Path().apply {
            moveTo(x + 4f * scale, y + 22f * scale)
            cubicTo(x + 4f * scale, y + 9f * scale, x + 14f * scale, y + 2f * scale, x + 29f * scale, y + 2f * scale)
            cubicTo(x + 37f * scale, y + 2f * scale, x + 42f * scale, y + 6f * scale, x + 43.5f * scale, y + 11f * scale)
            cubicTo(x + 40f * scale, y + 8f * scale, x + 34f * scale, y + 5.5f * scale, x + 27f * scale, y + 5.5f * scale)
            cubicTo(x + 15f * scale, y + 5.5f * scale, x + 7.5f * scale, y + 13f * scale, x + 7.5f * scale, y + 23f * scale)
            cubicTo(x + 7.5f * scale, y + 33f * scale, x + 15f * scale, y + 41f * scale, x + 28f * scale, y + 41f * scale)
            cubicTo(x + 35f * scale, y + 41f * scale, x + 40f * scale, y + 38f * scale, x + 42.5f * scale, y + 34f * scale)
            cubicTo(x + 38f * scale, y + 42f * scale, x + 30f * scale, y + 43.5f * scale, x + 25f * scale, y + 43.5f * scale)
            cubicTo(x + 11f * scale, y + 43.5f * scale, x + 4f * scale, y + 33f * scale, x + 4f * scale, y + 22f * scale)
            close()
        }
        fillPaint.color = Brand.LOGO_GOLD
        canvas.drawPath(arcPath, fillPaint)

        // Base Sphere Globe
        val centerX = x + 23f * scale
        val centerY = y + 22f * scale
        val globeRadius = 15f * scale

        fillPaint.color = Brand.LOGO_GLOBE_DEEP
        canvas.drawCircle(centerX, centerY, globeRadius, fillPaint)

        // Solar Panel Photovoltaic Facet Tiles
        val tiles = listOf(
            Triple(centerX - 4f * scale, centerY - 11f * scale, Color.parseColor("#0288D1")),
            Triple(centerX + 3f * scale, centerY - 10.5f * scale, Color.parseColor("#29B6F6")),
            Triple(centerX - 10f * scale, centerY - 9f * scale, Color.parseColor("#00A0E9")),
            Triple(centerX - 11f * scale, centerY - 4.5f * scale, Color.parseColor("#0D47A1")),
            Triple(centerX - 5f * scale, centerY - 5f * scale, Color.parseColor("#0288D1")),
            Triple(centerX + 2f * scale, centerY - 4.5f * scale, Color.parseColor("#00A0E9")),
            Triple(centerX + 8f * scale, centerY - 3.5f * scale, Color.parseColor("#29B6F6")),
            Triple(centerX - 12f * scale, centerY + 1.5f * scale, Color.parseColor("#0D47A1")),
            Triple(centerX - 6f * scale, centerY + 1.5f * scale, Color.parseColor("#1565C0")),
            Triple(centerX + 1f * scale, centerY + 1.5f * scale, Color.parseColor("#0288D1")),
            Triple(centerX + 8f * scale, centerY + 1.5f * scale, Color.parseColor("#00A0E9")),
            Triple(centerX - 10f * scale, centerY + 7.5f * scale, Color.parseColor("#0A3161")),
            Triple(centerX - 4f * scale, centerY + 7.5f * scale, Color.parseColor("#0D47A1")),
            Triple(centerX + 3f * scale, centerY + 7.5f * scale, Color.parseColor("#1565C0")),
            Triple(centerX + 8f * scale, centerY + 6.5f * scale, Color.parseColor("#0288D1")),
            Triple(centerX - 5f * scale, centerY + 12f * scale, Color.parseColor("#0A3161")),
            Triple(centerX + 2f * scale, centerY + 12f * scale, Color.parseColor("#0D47A1"))
        )

        val tileW = 5f * scale
        val tileH = 4.2f * scale
        val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        for ((tx, ty, tColor) in tiles) {
            tilePaint.color = tColor
            canvas.drawRoundRect(tx, ty, tx + tileW, ty + tileH, 0.8f * scale, 0.8f * scale, tilePaint)
        }

        // 3D Globe Grid lines
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 110
            style = Paint.Style.STROKE
            strokeWidth = 0.5f * scale
        }
        for (dy in listOf(-5f, 1f, 7f)) {
            val yOffset = centerY + dy * scale
            val halfW = (globeRadius * 0.9f)
            canvas.drawLine(centerX - halfW, yOffset, centerX + halfW, yOffset, gridPaint)
        }
        canvas.drawLine(centerX, centerY - globeRadius + 2f * scale, centerX, centerY + globeRadius - 2f * scale, gridPaint)

        // Typography
        val textStartX = x + 44f * scale
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
            color = firm?.primaryColor ?: Brand.LOGO_NAVY
            textSize = 17.5f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            letterSpacing = 0.04f
        }
        val brandText = firm?.brandName?.uppercase(Locale.US)?.takeIf { it.isNotBlank() } ?: "SOLAR ENERGY"
        canvas.drawText(brandText, textStartX, y + 21f * scale, namePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
            color = firm?.accentColor ?: Brand.LOGO_SUB
            textSize = 7.6f * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            letterSpacing = 0.02f
        }
        val taglineText = firm?.tagline?.takeIf { it.isNotBlank() } ?: "“Powering a Brighter Future”"
        canvas.drawText(taglineText, textStartX, y + 33f * scale, subPaint)
    }

    /**
     * Draws an adaptive, dynamic logo / emblem that changes automatically according
     * to the firm's industry, uploaded logo bitmap, or generates a stylish brand monogram.
     */
    fun drawAdaptiveEmblem(
        canvas: Canvas,
        x: Float,
        y: Float,
        height: Float = 44f,
        firm: FirmBranding
    ) {
        val scale = height / 44f

        // 1. If the user has a custom logo bitmap uploaded, render it
        if (!firm.logoPath.isNullOrBlank()) {
            val logoFile = File(firm.logoPath)
            if (logoFile.exists() && logoFile.length() > 0) {
                try {
                    val bitmap = BitmapFactory.decodeFile(logoFile.absolutePath)
                    if (bitmap != null) {
                        val maxW = 48f * scale
                        val maxH = height
                        val srcW = bitmap.width.toFloat()
                        val srcH = bitmap.height.toFloat()
                        val ratio = minOf(maxW / srcW, maxH / srcH)
                        val dstW = srcW * ratio
                        val dstH = srcH * ratio
                        val dstRect = RectF(x, y + (maxH - dstH) / 2f, x + dstW, y + (maxH - dstH) / 2f + dstH)
                        val p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                        canvas.drawBitmap(bitmap, null, dstRect, p)
                        return
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 2. Solar industry: Official Solar PV Globe
        if (firm.businessTemplateId == 1L || firm.category == "SOLAR") {
            drawSwamiSolarGlobeOnly(canvas, x, y, height, firm)
            return
        }

        // 3. Dynamic Vector Emblem based on Business Industry
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

        val badgeLeft = x
        val badgeTop = y
        val badgeSize = 38f * scale
        val badgeRect = RectF(badgeLeft, badgeTop + 3f * scale, badgeLeft + badgeSize, badgeTop + 3f * scale + badgeSize)

        // Outer rounded shield / badge
        fillPaint.color = firm.primaryColor
        canvas.drawRoundRect(badgeRect, 6f * scale, 6f * scale, fillPaint)

        // Inner icon motif based on business category
        val cx = badgeRect.centerX()
        val cy = badgeRect.centerY()
        val whiteFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val accentFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = firm.accentColor
            style = Paint.Style.FILL
        }

        when (firm.businessTemplateId) {
            2L -> { // Construction: Structural frame / building blocks
                canvas.drawRect(cx - 8f * scale, cy - 8f * scale, cx - 1f * scale, cy + 8f * scale, whiteFill)
                canvas.drawRect(cx + 1f * scale, cy - 3f * scale, cx + 8f * scale, cy + 8f * scale, accentFill)
                canvas.drawRect(cx - 5f * scale, cy - 6f * scale, cx - 3f * scale, cy - 4f * scale, fillPaint)
                canvas.drawRect(cx - 5f * scale, cy - 2f * scale, cx - 3f * scale, cy, fillPaint)
                canvas.drawRect(cx + 3f * scale, cy - 1f * scale, cx + 6f * scale, cy + 1f * scale, fillPaint)
            }
            3L, 6L, 7L -> { // Kirana / Retail / Grocery: Shopping Bag & Sparkle Sprout
                val bagRect = RectF(cx - 7f * scale, cy - 4f * scale, cx + 7f * scale, cy + 9f * scale)
                canvas.drawRoundRect(bagRect, 2f * scale, 2f * scale, whiteFill)
                strokePaint.color = Color.WHITE
                strokePaint.strokeWidth = 1.8f * scale
                canvas.drawArc(RectF(cx - 4f * scale, cy - 8f * scale, cx + 4f * scale, cy), 180f, 180f, false, strokePaint)
                canvas.drawCircle(cx, cy + 2f * scale, 2.2f * scale, accentFill)
            }
            4L -> { // Pharmacy: Rx Medical Cross Shield
                val crossW = 14f * scale
                val crossT = 4.5f * scale
                canvas.drawRoundRect(RectF(cx - crossW / 2f, cy - crossT / 2f, cx + crossW / 2f, cy + crossT / 2f), 1f, 1f, whiteFill)
                canvas.drawRoundRect(RectF(cx - crossT / 2f, cy - crossW / 2f, cx + crossT / 2f, cy + crossW / 2f), 1f, 1f, whiteFill)
                canvas.drawCircle(cx, cy, 2f * scale, accentFill)
            }
            5L -> { // Salon / Beauty: Chic Star & Stylist Motif
                canvas.drawCircle(cx, cy, 6f * scale, accentFill)
                strokePaint.color = Color.WHITE
                strokePaint.strokeWidth = 1.8f * scale
                canvas.drawLine(cx - 6f * scale, cy - 6f * scale, cx + 6f * scale, cy + 6f * scale, strokePaint)
                canvas.drawLine(cx + 6f * scale, cy - 6f * scale, cx - 6f * scale, cy + 6f * scale, strokePaint)
                canvas.drawCircle(cx, cy, 2.5f * scale, whiteFill)
            }
            8L -> { // Cafe / Restaurant: Cloche / Coffee & Flame
                strokePaint.color = Color.WHITE
                strokePaint.strokeWidth = 1.6f * scale
                canvas.drawArc(RectF(cx - 8f * scale, cy - 5f * scale, cx + 8f * scale, cy + 8f * scale), 180f, 180f, false, whiteFill)
                canvas.drawRect(cx - 9f * scale, cy + 4f * scale, cx + 9f * scale, cy + 6.5f * scale, accentFill)
                canvas.drawCircle(cx, cy - 6f * scale, 2f * scale, whiteFill)
            }
            9L -> { // Hardware: Industrial Hex Bolt & Tool
                canvas.drawCircle(cx, cy, 7.5f * scale, whiteFill)
                canvas.drawCircle(cx, cy, 3.5f * scale, fillPaint)
                canvas.drawRect(cx - 1.2f * scale, cy - 9f * scale, cx + 1.2f * scale, cy + 9f * scale, accentFill)
            }
            10L -> { // Services / IT: Security Crest & Verify Star
                canvas.drawCircle(cx, cy, 7f * scale, whiteFill)
                canvas.drawCircle(cx, cy, 4.5f * scale, accentFill)
                strokePaint.color = Color.WHITE
                strokePaint.strokeWidth = 1.4f * scale
                canvas.drawLine(cx - 3f * scale, cy, cx - 1f * scale, cy + 2.5f * scale, strokePaint)
                canvas.drawLine(cx - 1f * scale, cy + 2.5f * scale, cx + 3.5f * scale, cy - 2f * scale, strokePaint)
            }
            11L -> { // Electronics: Microchip & Lightning Circuit
                canvas.drawRoundRect(RectF(cx - 6f * scale, cy - 6f * scale, cx + 6f * scale, cy + 6f * scale), 1.5f, 1.5f, whiteFill)
                canvas.drawCircle(cx, cy, 3f * scale, accentFill)
                strokePaint.color = Color.WHITE
                strokePaint.strokeWidth = 1.2f * scale
                canvas.drawLine(cx - 8f * scale, cy, cx - 6f * scale, cy, strokePaint)
                canvas.drawLine(cx + 6f * scale, cy, cx + 8f * scale, cy, strokePaint)
                canvas.drawLine(cx, cy - 8f * scale, cx, cy - 6f * scale, strokePaint)
                canvas.drawLine(cx, cy + 6f * scale, cx, cy + 8f * scale, strokePaint)
            }
            12L -> { // Sweets: Indian Festive Sweet Crest
                canvas.drawCircle(cx, cy, 7f * scale, accentFill)
                strokePaint.color = Color.WHITE
                strokePaint.strokeWidth = 1.4f * scale
                canvas.drawCircle(cx, cy, 4.5f * scale, whiteFill)
                canvas.drawCircle(cx, cy, 2f * scale, fillPaint)
            }
            else -> { // Default: Clean Monogram Initials
                val initials = (firm.brandName.ifBlank { firm.legalName }).split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .map { it.first().uppercaseChar() }
                    .joinToString("")
                    .ifBlank { "BB" }

                val monoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                    color = Color.WHITE
                    textSize = 14f * scale
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(initials, cx, cy + 5f * scale, monoPaint)
            }
        }
    }

    /**
     * Draws only the Solar PV Globe emblem (without baked-in text).
     */
    private fun drawSwamiSolarGlobeOnly(
        canvas: Canvas,
        x: Float,
        y: Float,
        height: Float = 44f,
        firm: FirmBranding? = null
    ) {
        val scale = height / 44f
        val globeRadius = 18f * scale
        val centerX = x + globeRadius + 2f * scale
        val centerY = y + height / 2f

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E0E0E0")
            style = Paint.Style.STROKE
            strokeWidth = 1.2f * scale
        }
        canvas.drawCircle(centerX, centerY, globeRadius + 1.5f * scale, ringPaint)

        val globeFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = RadialGradient(
                centerX - globeRadius * 0.35f,
                centerY - globeRadius * 0.35f,
                globeRadius * 1.3f,
                intArrayOf(Color.parseColor("#1E88E5"), Color.parseColor("#0D47A1"), Color.parseColor("#0A2540")),
                floatArrayOf(0.0f, 0.65f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(centerX, centerY, globeRadius, globeFill)

        val tiles = listOf(
            Triple(centerX - 4f * scale, centerY - 11f * scale, Color.parseColor("#0288D1")),
            Triple(centerX + 3f * scale, centerY - 10.5f * scale, Color.parseColor("#29B6F6")),
            Triple(centerX - 10f * scale, centerY - 9f * scale, Color.parseColor("#00A0E9")),
            Triple(centerX - 11f * scale, centerY - 4.5f * scale, Color.parseColor("#0D47A1")),
            Triple(centerX - 5f * scale, centerY - 5f * scale, Color.parseColor("#0288D1")),
            Triple(centerX + 2f * scale, centerY - 4.5f * scale, Color.parseColor("#00A0E9")),
            Triple(centerX + 8f * scale, centerY - 3.5f * scale, Color.parseColor("#29B6F6")),
            Triple(centerX - 12f * scale, centerY + 1.5f * scale, Color.parseColor("#0D47A1")),
            Triple(centerX - 6f * scale, centerY + 1.5f * scale, Color.parseColor("#1565C0")),
            Triple(centerX + 1f * scale, centerY + 1.5f * scale, Color.parseColor("#0288D1")),
            Triple(centerX + 8f * scale, centerY + 1.5f * scale, Color.parseColor("#00A0E9")),
            Triple(centerX - 10f * scale, centerY + 7.5f * scale, Color.parseColor("#0A3161")),
            Triple(centerX - 4f * scale, centerY + 7.5f * scale, Color.parseColor("#0D47A1")),
            Triple(centerX + 3f * scale, centerY + 7.5f * scale, Color.parseColor("#1565C0")),
            Triple(centerX + 8f * scale, centerY + 6.5f * scale, Color.parseColor("#0288D1")),
            Triple(centerX - 5f * scale, centerY + 12f * scale, Color.parseColor("#0A3161")),
            Triple(centerX + 2f * scale, centerY + 12f * scale, Color.parseColor("#0D47A1"))
        )

        val tileW = 5f * scale
        val tileH = 4.2f * scale
        val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        for ((tx, ty, tColor) in tiles) {
            tilePaint.color = tColor
            canvas.drawRoundRect(tx, ty, tx + tileW, ty + tileH, 0.8f * scale, 0.8f * scale, tilePaint)
        }

        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 110
            style = Paint.Style.STROKE
            strokeWidth = 0.5f * scale
        }
        for (dy in listOf(-5f, 1f, 7f)) {
            val yOffset = centerY + dy * scale
            val halfW = (globeRadius * 0.9f)
            canvas.drawLine(centerX - halfW, yOffset, centerX + halfW, yOffset, gridPaint)
        }
        canvas.drawLine(centerX, centerY - globeRadius + 2f * scale, centerX, centerY + globeRadius - 2f * scale, gridPaint)
    }

    /**
     * Draws the Adaptive Multi-Theme Letterhead for the target Business Profile.
     * The Firm Name and Brand is always prominently placed at the very top of the letterhead.
     */
    fun drawAdaptiveLetterhead(
        context: Context,
        canvas: Canvas,
        title: String,
        subtitle: String,
        fonts: FontKit,
        firm: FirmBranding
    ): Float {
        var y = 26f

        when (firm.theme) {
            PdfTheme.CLASSIC_GST -> {
                // CLASSIC CA / TALLY FORMAL GST BOXED LETTERHEAD
                val boxTop = y
                val boxBottom = y + 80f
                val boxRect = RectF(MARGIN_LEFT, boxTop, MARGIN_RIGHT, boxBottom)

                val frameBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Brand.LINE_STRONG
                    style = Paint.Style.STROKE
                    strokeWidth = 1.0f
                }
                val headerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#F8FAFC")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(boxRect, headerBg)
                canvas.drawRect(boxRect, frameBorder)

                // Left: Formal Entity Details
                val displayName = firm.legalName.ifBlank { firm.brandName }
                val namePaint = fonts.paint(Brand.INK, 14f, bold = true)
                canvas.drawText(displayName.uppercase(Locale.US), MARGIN_LEFT + 12f, y + 18f, namePaint)

                if (firm.tagline.isNotBlank()) {
                    val tagPaint = fonts.paint(Brand.MUTED, 7.5f, bold = true)
                    canvas.drawText(firm.tagline, MARGIN_LEFT + 12f, y + 29f, tagPaint)
                }

                val metaPaint = fonts.paint(Brand.MUTED, 7.6f)
                canvas.drawText("${firm.address}, ${firm.city}, ${firm.state} - ${firm.pincode}", MARGIN_LEFT + 12f, y + 41f, metaPaint)

                val gstText = buildString {
                    if (firm.gstin.isNotBlank()) append("GSTIN: ${firm.gstin}   |   ")
                    if (firm.pan.isNotBlank()) append("PAN: ${firm.pan}   |   ")
                    append("State: ${firm.state}")
                }
                canvas.drawText(gstText, MARGIN_LEFT + 12f, y + 53f, metaPaint)

                val contactStr = listOfNotNull(
                    "Email: ${firm.email}".takeIf { firm.email.isNotBlank() },
                    "Phone: ${firm.phone}".takeIf { firm.phone.isNotBlank() }
                ).joinToString("   |   ")
                if (contactStr.isNotBlank()) {
                    canvas.drawText(contactStr, MARGIN_LEFT + 12f, y + 65f, metaPaint)
                }

                // Right: Boxed Document Type
                val titleBoxLeft = MARGIN_RIGHT - 145f
                val titleBoxRect = RectF(titleBoxLeft, boxTop + 8f, MARGIN_RIGHT - 8f, boxBottom - 8f)
                val titleBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }
                canvas.drawRect(titleBoxRect, titleBg)
                canvas.drawRect(titleBoxRect, frameBorder)

                val docTitlePaint = fonts.paint(Brand.INK, 11.5f, bold = true, align = Paint.Align.CENTER)
                val docSubPaint = fonts.paint(Brand.MUTED, 7.0f, bold = true, align = Paint.Align.CENTER)
                val titleCenter = titleBoxRect.centerX()
                canvas.drawText(title.uppercase(Locale.US), titleCenter, titleBoxRect.centerY() - 2f, docTitlePaint)
                canvas.drawText(subtitle.uppercase(Locale.US), titleCenter, titleBoxRect.centerY() + 10f, docSubPaint)

                y = boxBottom + 12f
            }

            PdfTheme.MINIMAL_CLEAN -> {
                // MINIMALIST CONTEMPORARY BOUTIQUE LAYOUT
                drawAdaptiveEmblem(canvas, MARGIN_LEFT, y, height = 40f, firm)
                val textLeft = MARGIN_LEFT + 48f
                val primaryName = firm.brandName.ifBlank { firm.legalName }

                val brandPaint = fonts.paint(firm.primaryColor, 15.5f, bold = true)
                canvas.drawText(primaryName.uppercase(Locale.US), textLeft, y + 16f, brandPaint)

                if (firm.tagline.isNotBlank()) {
                    val subPaint = fonts.paint(firm.accentColor, 7.6f, bold = true)
                    canvas.drawText(firm.tagline, textLeft, y + 27f, subPaint)
                }

                val titlePaint = fonts.paint(firm.primaryColor, 15f, bold = true, align = Paint.Align.RIGHT)
                canvas.drawText(title.uppercase(Locale.US), MARGIN_RIGHT, y + 16f, titlePaint)

                val subPaint = fonts.paint(Brand.MUTED, 7.2f, bold = true, align = Paint.Align.RIGHT)
                canvas.drawText(subtitle.uppercase(Locale.US), MARGIN_RIGHT, y + 27f, subPaint)

                if (firm.gstin.isNotBlank()) {
                    val gstPaint = fonts.paint(Brand.INK, 8.5f, bold = true, align = Paint.Align.RIGHT)
                    canvas.drawText("GSTIN: ${firm.gstin}", MARGIN_RIGHT, y + 39f, gstPaint)
                }

                y += 50f
                val cleanLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Brand.LINE
                    strokeWidth = 0.8f
                }
                canvas.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, cleanLine)
                y += 11f

                val metaPaint = fonts.paint(Brand.MUTED, 7.4f)
                val locText = "${firm.address}, ${firm.city} | Email: ${firm.email} | Mobile: ${firm.phone} | State: ${firm.state}"
                canvas.drawText(locText, MARGIN_LEFT, y, metaPaint)

                y += 14f
                canvas.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, cleanLine)
                y += 12f
            }

            PdfTheme.THERMAL_POS -> {
                // COMPACT POS COUNTER SLIP HEADER
                val center = (MARGIN_LEFT + MARGIN_RIGHT) / 2f
                val primaryName = firm.brandName.ifBlank { firm.legalName }
                val brandPaint = fonts.paint(Brand.INK, 16f, bold = true, align = Paint.Align.CENTER)
                canvas.drawText(primaryName.uppercase(Locale.US), center, y + 14f, brandPaint)

                if (firm.tagline.isNotBlank()) {
                    val tagPaint = fonts.paint(Brand.MUTED, 8f, bold = true, align = Paint.Align.CENTER)
                    canvas.drawText(firm.tagline, center, y + 26f, tagPaint)
                }

                val addrPaint = fonts.paint(Brand.MUTED, 7.2f, align = Paint.Align.CENTER)
                canvas.drawText("${firm.address}, ${firm.city} - ${firm.pincode}", center, y + 38f, addrPaint)

                val gstPaint = fonts.paint(Brand.INK, 8.2f, bold = true, align = Paint.Align.CENTER)
                if (firm.gstin.isNotBlank()) {
                    canvas.drawText("GSTIN: ${firm.gstin}   |   Ph: ${firm.phone}", center, y + 50f, gstPaint)
                } else {
                    canvas.drawText("Ph: ${firm.phone}   |   ${firm.email}", center, y + 50f, gstPaint)
                }

                y += 58f
                val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Brand.LINE_STRONG
                    strokeWidth = 1f
                    pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
                }
                canvas.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, dashPaint)
                y += 12f

                val titlePaint = fonts.paint(Brand.INK, 11f, bold = true, align = Paint.Align.CENTER)
                canvas.drawText(title.uppercase(Locale.US), center, y + 2f, titlePaint)
                y += 14f
                canvas.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, dashPaint)
                y += 12f
            }

            else -> {
                // MODERN_COLOR: VIBRANT POLISHED CORPORATE LETTERHEAD (DEFAULT)
                // Left: Logo Badge
                val logoHeight = 44f
                drawAdaptiveEmblem(canvas, MARGIN_LEFT, y, height = logoHeight, firm)

                val textLeft = MARGIN_LEFT + 48f
                val maxFirmTextWidth = 330f

                // 1. FIRM NAME (Topmost, Largest, Boldest Headline)
                val primaryFirmName = firm.brandName.ifBlank { firm.legalName }
                val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
                    color = firm.primaryColor
                    textSize = 15.5f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    letterSpacing = 0.02f
                }
                val wrappedName = wrapText(primaryFirmName.uppercase(Locale.US), brandPaint, maxFirmTextWidth)
                var leftY = y + 14f
                for (nameLine in wrappedName) {
                    canvas.drawText(nameLine, textLeft, leftY, brandPaint)
                    leftY += 15.5f
                }

                // 2. Firm Tagline / Industry Sub-descriptor
                if (firm.tagline.isNotBlank()) {
                    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                        color = firm.accentColor
                        textSize = 7.6f
                        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    }
                    val wrappedTag = wrapText(firm.tagline, subPaint, maxFirmTextWidth)
                    for (tagLine in wrappedTag) {
                        canvas.drawText(tagLine, textLeft, leftY, subPaint)
                        leftY += 9.5f
                    }
                }

                // 3. Full Address
                val metaPaint = fonts.paint(Brand.MUTED, 7.4f)
                val addrStr = listOfNotNull(
                    firm.address.takeIf { it.isNotBlank() },
                    firm.city.takeIf { it.isNotBlank() },
                    "${firm.state} - ${firm.pincode}".takeIf { firm.pincode.isNotBlank() || firm.state.isNotBlank() }
                ).joinToString(", ")
                if (addrStr.isNotBlank()) {
                    val wrappedAddr = wrapText(addrStr, metaPaint, maxFirmTextWidth)
                    for (addrLine in wrappedAddr) {
                        canvas.drawText(addrLine, textLeft, leftY, metaPaint)
                        leftY += 9.0f
                    }
                }

                // 4. GSTIN, Contact & Email
                val gstAndContact = buildString {
                    if (firm.gstin.isNotBlank()) append("GSTIN: ${firm.gstin}   |   ")
                    if (firm.pan.isNotBlank()) append("PAN: ${firm.pan}   |   ")
                    if (firm.phone.isNotBlank()) append("Mob: ${firm.phone}")
                }
                if (gstAndContact.isNotBlank()) {
                    val gstPaint = fonts.paint(Brand.INK, 7.4f, bold = true)
                    canvas.drawText(gstAndContact, textLeft, leftY, gstPaint)
                    leftY += 9.0f
                }

                if (firm.email.isNotBlank()) {
                    canvas.drawText("Email: ${firm.email}", textLeft, leftY, metaPaint)
                    leftY += 9.0f
                }

                // Right Column: Professional Document Badge Box
                val badgeBoxLeft = MARGIN_RIGHT - 135f
                val badgeBoxTop = y
                val badgeBoxBottom = maxOf(leftY - 4f, y + 54f)
                val badgeBoxRect = RectF(badgeBoxLeft, badgeBoxTop, MARGIN_RIGHT, badgeBoxBottom)

                val badgeBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = firm.bgTint
                    style = Paint.Style.FILL
                }
                val badgeBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = firm.primaryColor
                    alpha = 60
                    style = Paint.Style.STROKE
                    strokeWidth = 0.8f
                }
                canvas.drawRoundRect(badgeBoxRect, 4f, 4f, badgeBg)
                canvas.drawRoundRect(badgeBoxRect, 4f, 4f, badgeBorder)

                val badgeCenter = badgeBoxRect.centerX()
                val titlePaint = fonts.paint(firm.primaryColor, 12f, bold = true, align = Paint.Align.CENTER)
                val subTitlePaint = fonts.paint(Brand.MUTED, 6.8f, bold = true, align = Paint.Align.CENTER)
                val posPaint = fonts.paint(Brand.INK, 6.8f, bold = true, align = Paint.Align.CENTER)

                canvas.drawText(title.uppercase(Locale.US), badgeCenter, badgeBoxTop + 17f, titlePaint)
                canvas.drawText(subtitle.uppercase(Locale.US), badgeCenter, badgeBoxTop + 28f, subTitlePaint)

                val badgeDivider = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = firm.primaryColor
                    alpha = 50
                    strokeWidth = 0.5f
                }
                canvas.drawLine(badgeBoxLeft + 8f, badgeBoxTop + 33f, MARGIN_RIGHT - 8f, badgeBoxTop + 33f, badgeDivider)

                val stateCodeStr = if (firm.placeOfSupply.isNotBlank()) firm.placeOfSupply else "State: ${firm.state} (27)"
                canvas.drawText(stateCodeStr, badgeCenter, badgeBoxTop + 43f, posPaint)

                val letterheadBottom = maxOf(leftY, badgeBoxBottom) + 8f
                y = letterheadBottom

                // Dual Corporate Accent Divider Bar
                val strongRule = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = firm.primaryColor
                    strokeWidth = 1.5f
                }
                val subHairline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = firm.accentColor
                    strokeWidth = 0.8f
                }
                canvas.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, strongRule)
                canvas.drawLine(MARGIN_LEFT, y + 2.5f, MARGIN_RIGHT, y + 2.5f, subHairline)
                y += 12f
            }
        }

        return y
    }

    /**
     * Draws the dynamic Bank Details & Settlement Mandate with optional companion QR Code.
     */
    fun drawAdaptiveBankAndQrSection(
        context: Context,
        canvas: Canvas,
        fonts: FontKit,
        topY: Float,
        boxHeight: Float = 68f,
        firm: FirmBranding,
        boxTitle: String = "OFFICIAL REMITTANCE MANDATE / BANK DETAILS",
        docNo: String = "",
        docTotal: Double = 0.0
    ) {
        val bankBoxX = MARGIN_LEFT
        val dynamicQr = if (firm.upiVpa.isNotBlank() && docTotal > 0.0) {
            DynamicUpiQrGenerator.generateInvoicePaymentQr(
                upiVpa = firm.upiVpa,
                firmName = firm.brandName,
                invoiceNo = docNo,
                invoiceTotal = docTotal
            )
        } else null
        val qrBitmap = dynamicQr ?: PaymentQrHelper.getQrBitmap(context)
        val hasQr = qrBitmap != null

        val bankBoxWidth = if (hasQr) 215f else 288f

        val bankBoxRect = RectF(bankBoxX, topY, bankBoxX + bankBoxWidth, topY + boxHeight)
        val bankBoxBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FAFAFA")
            style = Paint.Style.FILL
        }
        val bankBoxBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        canvas.drawRoundRect(bankBoxRect, 2f, 2f, bankBoxBgPaint)
        canvas.drawRoundRect(bankBoxRect, 2f, 2f, bankBoxBorderPaint)

        // Header strip
        val bankHeaderH = 13f
        val bankHeaderBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.BG_HEADER
            style = Paint.Style.FILL
        }
        val bankHeaderLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE
            strokeWidth = 0.6f
        }
        canvas.drawRect(bankBoxX + 1f, topY + 1f, bankBoxX + bankBoxWidth - 1f, topY + bankHeaderH, bankHeaderBg)
        canvas.drawLine(bankBoxX, topY + bankHeaderH, bankBoxX + bankBoxWidth, topY + bankHeaderH, bankHeaderLine)

        val bankHeadPaint = fonts.paint(Brand.INK, 6.2f, bold = true)
        canvas.drawText(boxTitle, bankBoxX + 5.5f, topY + 9.5f, bankHeadPaint)

        // Rows
        var bRowY = topY + bankHeaderH + 9.5f
        val bLblPaint = fonts.paint(Brand.MUTED, 7.0f)
        val bValPaint = fonts.paint(Brand.INK, 7.0f, bold = true)
        val bValMono = fonts.paint(Brand.INK, 7.3f, bold = true)

        fun drawBankRow(lbl: String, value: String, isMono: Boolean = false) {
            canvas.drawText(lbl, bankBoxX + 5.5f, bRowY, bLblPaint)
            val valPaint = if (isMono) bValMono else bValPaint
            val labelW = 46f
            canvas.drawText(value, bankBoxX + 5.5f + labelW, bRowY, valPaint)
            bRowY += 10.2f
        }

        drawBankRow("Bank:", firm.bankName)
        drawBankRow("A/c Name:", firm.accountName)
        drawBankRow("A/c No.:", firm.accountNumber, isMono = true)
        drawBankRow("IFSC Code:", firm.ifsc, isMono = true)

        if (firm.upiVpa.isNotBlank()) {
            drawBankRow("UPI ID:", firm.upiVpa, isMono = true)
        }

        // Companion QR Code Box
        if (hasQr && qrBitmap != null) {
            val qrBoxX = bankBoxX + bankBoxWidth + 8f
            val qrBoxWidth = 66f
            val qrBoxHeight = boxHeight
            val qrBoxRect = RectF(qrBoxX, topY, qrBoxX + qrBoxWidth, topY + qrBoxHeight)

            canvas.drawRoundRect(qrBoxRect, 2f, 2f, bankBoxBgPaint)
            canvas.drawRoundRect(qrBoxRect, 2f, 2f, bankBoxBorderPaint)

            val qrHeaderH = 13f
            canvas.drawRect(qrBoxX + 1f, topY + 1f, qrBoxX + qrBoxWidth - 1f, topY + qrHeaderH, bankHeaderBg)
            canvas.drawLine(qrBoxX, topY + qrHeaderH, qrBoxX + qrBoxWidth, topY + qrHeaderH, bankHeaderLine)

            val qrHeadPaint = fonts.paint(Brand.INK, 5.8f, bold = true, align = Paint.Align.CENTER)
            canvas.drawText("SCAN TO PAY", qrBoxX + qrBoxWidth / 2f, topY + 9.5f, qrHeadPaint)

            val qrPadding = 3.5f
            val qrInnerWidth = qrBoxWidth - (qrPadding * 2f)
            val qrInnerHeight = qrBoxHeight - qrHeaderH - (qrPadding * 2f)
            val qrInnerSize = minOf(qrInnerWidth, qrInnerHeight)
            val qrImageLeft = qrBoxX + (qrBoxWidth - qrInnerSize) / 2f
            val qrImageTop = topY + qrHeaderH + ((qrBoxHeight - qrHeaderH - qrInnerSize) / 2f)

            val qrDstRect = RectF(qrImageLeft, qrImageTop, qrImageLeft + qrInnerSize, qrImageTop + qrInnerSize)
            val qrPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(qrBitmap, null, qrDstRect, qrPaint)
        }
    }

    /**
     * Draws the dynamic Signature & Digital Seal Block for the target business profile.
     */
    fun drawAdaptiveSignatureBlock(
        context: Context,
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float = 104f,
        firm: FirmBranding,
        fonts: FontKit
    ) {
        val rightX = x + width

        // 1. "For [Legal Entity Name]"
        val forPaint = fonts.paint(Brand.INK, 8.8f, bold = true, align = Paint.Align.RIGHT)
        val forText = "For ${firm.legalName}"
        val wrappedFor = wrapText(forText, forPaint, width)
        var curY = y + 10f
        for (line in wrappedFor) {
            canvas.drawText(line, rightX, curY, forPaint)
            curY += 10.5f
        }

        // 2. Official Digital Seal Badge
        val sealBoxWidth = minOf(width, 185f)
        val sealBoxHeight = 36f
        val sealBoxX = rightX - sealBoxWidth
        val sealBoxY = curY + 2f
        val sealRect = RectF(sealBoxX, sealBoxY, sealBoxX + sealBoxWidth, sealBoxY + sealBoxHeight)

        val sealBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = firm.bgTint
            style = Paint.Style.FILL
        }
        val sealBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        canvas.drawRoundRect(sealRect, 3f, 3f, sealBg)
        canvas.drawRoundRect(sealRect, 3f, 3f, sealBorder)

        // Accent strip on left of seal
        val stripPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = firm.primaryColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(sealBoxX, sealBoxY, sealBoxX + 3.5f, sealBoxY + sealBoxHeight), 3f, 3f, stripPaint)

        // Seal content
        val sealTitlePaint = fonts.paint(firm.primaryColor, 6.8f, bold = true)
        canvas.drawText("DIGITALLY SIGNED DOCUMENT", sealBoxX + 8f, sealBoxY + 11.5f, sealTitlePaint)

        val sealSubPaint = fonts.paint(Brand.MUTED, 5.8f)
        canvas.drawText("System authenticated electronic document", sealBoxX + 8f, sealBoxY + 20.5f, sealSubPaint)

        val sealDatePaint = fonts.paint(Brand.FAINT, 5.4f)
        canvas.drawText("Timestamp: ${Formatters.todayIso()} | Validated", sealBoxX + 8f, sealBoxY + 28.5f, sealDatePaint)

        // 3. User Custom Signature Image (if present)
        curY = sealBoxY + sealBoxHeight + 8f
        if (!firm.signaturePath.isNullOrBlank()) {
            val sigFile = File(firm.signaturePath)
            if (sigFile.exists() && sigFile.length() > 0) {
                try {
                    val sigBitmap = BitmapFactory.decodeFile(sigFile.absolutePath)
                    if (sigBitmap != null) {
                        val maxW = 90f
                        val maxH = 24f
                        val ratio = minOf(maxW / sigBitmap.width.toFloat(), maxH / sigBitmap.height.toFloat())
                        val sW = sigBitmap.width * ratio
                        val sH = sigBitmap.height * ratio
                        val dstRect = RectF(rightX - sW, curY, rightX, curY + sH)
                        val p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                        canvas.drawBitmap(sigBitmap, null, dstRect, p)
                        curY += sH + 4f
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 4. Signatory Person & Designation
        val sigPersonPaint = fonts.paint(Brand.INK, 8.8f, bold = true, align = Paint.Align.RIGHT)
        canvas.drawText(firm.signatoryName, rightX, curY + 9f, sigPersonPaint)

        val authPaint = fonts.paint(Brand.MUTED, 7.5f, align = Paint.Align.RIGHT)
        canvas.drawText(firm.signatoryTitle, rightX, curY + 19.5f, authPaint)
    }

    /**
     * Draws the dynamic document footer line and metadata.
     */
    fun drawAdaptiveFooter(canvas: Canvas, fonts: FontKit, firm: FirmBranding) {
        val footerRule = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE
            strokeWidth = 0.8f
        }
        canvas.drawLine(MARGIN_LEFT, 814f, MARGIN_RIGHT, 814f, footerRule)

        val footerText = fonts.paint(Brand.FAINT, 6.8f)
        val gstinText = if (firm.gstin.isNotBlank()) " | GSTIN ${firm.gstin}" else ""
        val leftStr = "${firm.legalName}$gstinText"
        canvas.drawText(leftStr, MARGIN_LEFT, 826f, footerText)

        val centerStr = "This is a computer generated document."
        val cWidth = footerText.measureText(centerStr)
        canvas.drawText(centerStr, (PAGE_WIDTH - cWidth) / 2f, 826f, footerText)

        val rightStr = "Page 1 of 1"
        val rWidth = footerText.measureText(rightStr)
        canvas.drawText(rightStr, MARGIN_RIGHT - rWidth, 826f, footerText)
    }

    /**
     * Helper to render terms and conditions list.
     */
    private fun drawTermsList(
        canvas: Canvas,
        terms: List<String>,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineSpacing: Float = 8.5f
    ): Float {
        var y = startY
        for (term in terms) {
            val lines = wrapText(term, paint, maxWidth)
            for (line in lines) {
                canvas.drawText(line, x, y, paint)
                y += lineSpacing
            }
        }
        return y
    }

    // =========================================================================================
    // 1. UNIVERSAL & SOLAR ESTIMATE / INVOICE PDF GENERATOR
    // =========================================================================================
    fun generatePdf(
        context: Context,
        detail: DocWithDetails,
        company: Company = Company(),
        parentEstimate: Doc? = null,
        business: BusinessProfile? = null,
        themeStr: String? = null
    ): File {
        val doc = detail.doc
        val customer = detail.customer ?: Customer(name = "Valued Customer")
        val isInvoice = doc.isInvoice

        // Route to Construction BOQ Generator if Civil Estimate
        if (doc.isConstructionEstimate) {
            return generateConstructionPdf(context, detail, company, business, themeStr)
        }

        val dynamicData = InvoiceTemplateFactory.buildDynamicInvoice(detail, business, company, themeStr)
        val firm = dynamicData.firm
        val fonts = FontKit(context)
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // 1. Official Letterhead
        var y = drawAdaptiveLetterhead(context, canvas, dynamicData.docTitle, dynamicData.docSubtitle, fonts, firm)

        // 2. Party & Document Info Block
        val partyBlockTop = y
        val sectionLabelPaint = fonts.paint(Brand.MUTED, 7.8f, bold = true)
        val boldInkPaint = fonts.paint(Brand.INK, 10.5f, bold = true)
        val regularInk = fonts.paint(Brand.INK, 8.5f)
        val regularMuted = fonts.paint(Brand.MUTED, 8.2f)

        // Left Column: Bill To / Party Details
        val billToLabel = if (isInvoice) "BILLED TO / CUSTOMER" else "ESTIMATE FOR / CLIENT"
        canvas.drawText(billToLabel, MARGIN_LEFT, y + 2f, sectionLabelPaint)
        y += 14f

        canvas.drawText(dynamicData.customerName, MARGIN_LEFT, y, boldInkPaint)
        y += 13.5f

        if (dynamicData.customerMobile.isNotBlank()) {
            canvas.drawText("Mobile: ${dynamicData.customerMobile}", MARGIN_LEFT, y, regularInk)
            y += 12.0f
        }

        if (dynamicData.customerAddress.isNotBlank()) {
            canvas.drawText(dynamicData.customerAddress, MARGIN_LEFT, y, regularMuted)
            y += 12.0f
        }

        // Industry-specific header details (Medical doctor / DL, or Site location)
        if (dynamicData.headerMeta.doctorName != null) {
            val rxStr = if (!dynamicData.headerMeta.rxNumber.isNullOrBlank()) "  |  Rx: ${dynamicData.headerMeta.rxNumber}" else ""
            canvas.drawText("Doctor / Ref: ${dynamicData.headerMeta.doctorName}$rxStr", MARGIN_LEFT, y, fonts.paint(Brand.INK, 8.0f, bold = true))
            y += 12.0f
        }
        if (dynamicData.headerMeta.drugLicenseNo != null) {
            canvas.drawText("D.L. No.: ${dynamicData.headerMeta.drugLicenseNo}", MARGIN_LEFT, y, regularMuted)
            y += 12.0f
        }
        if (dynamicData.headerMeta.siteLocation != null && dynamicData.headerMeta.doctorName == null) {
            canvas.drawText("Site Location: ${dynamicData.headerMeta.siteLocation}", MARGIN_LEFT, y, regularMuted)
            y += 12.0f
        }

        canvas.drawText("Place of Supply: ${firm.placeOfSupply}", MARGIN_LEFT, y, regularMuted)

        // Right Column: Metadata Block
        val statsLeft = 345f
        val statColWidth = (MARGIN_RIGHT - statsLeft) / 3f

        val stat1Label = if (isInvoice) "INVOICE NO." else "ESTIMATE NO."
        val stat1Val = dynamicData.docNo
        val stat2Label = if (isInvoice) "INVOICE DATE" else "ESTIMATE DATE"
        val stat2Val = Formatters.fmtDate(dynamicData.docDate)
        val stat3Label = if (isInvoice) "DUE DATE" else "VALID TILL"
        val stat3Val = if (dynamicData.dueDateOrValidTill.isNotBlank()) Formatters.fmtDate(dynamicData.dueDateOrValidTill) else "-"

        val statValPaint = fonts.paint(Brand.INK, 9.6f, bold = true)
        canvas.drawText(stat1Label, statsLeft, partyBlockTop + 2f, sectionLabelPaint)
        canvas.drawText(stat1Val, statsLeft, partyBlockTop + 16f, statValPaint)

        canvas.drawText(stat2Label, statsLeft + statColWidth, partyBlockTop + 2f, sectionLabelPaint)
        canvas.drawText(stat2Val, statsLeft + statColWidth, partyBlockTop + 16f, statValPaint)

        canvas.drawText(stat3Label, statsLeft + statColWidth * 2, partyBlockTop + 2f, sectionLabelPaint)
        canvas.drawText(stat3Val, statsLeft + statColWidth * 2, partyBlockTop + 16f, statValPaint)

        y = maxOf(y + 16f, partyBlockTop + 54f)

        // 3. Line Items Dynamic Grid Table
        val tableTop = y
        val headerHeight = 20f
        val headerBottom = tableTop + headerHeight

        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.BG_HEADER
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN_LEFT, tableTop, MARGIN_RIGHT, headerBottom, headerBgPaint)

        val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE_STRONG
            style = Paint.Style.STROKE
            strokeWidth = 1.0f
        }
        val innerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE
            strokeWidth = 0.6f
        }

        // Calculate Dynamic Column Coordinates
        val columns = dynamicData.columns
        val tableW = MARGIN_RIGHT - MARGIN_LEFT
        val colX = FloatArray(columns.size + 1)
        colX[0] = MARGIN_LEFT
        for (i in columns.indices) {
            colX[i + 1] = if (i == columns.size - 1) MARGIN_RIGHT else colX[i] + tableW * columns[i].weight
        }

        val thPaint = fonts.paint(Brand.INK, 7.4f, bold = true)
        val thCenter = fonts.paint(Brand.INK, 7.4f, bold = true, align = Paint.Align.CENTER)
        val thRight = fonts.paint(Brand.INK, 7.4f, bold = true, align = Paint.Align.RIGHT)

        for (i in columns.indices) {
            val col = columns[i]
            val left = colX[i]
            val right = colX[i + 1]
            val thP = when (col.align) {
                Paint.Align.CENTER -> thCenter
                Paint.Align.RIGHT -> thRight
                else -> thPaint
            }
            val tx = when (col.align) {
                Paint.Align.CENTER -> (left + right) / 2f
                Paint.Align.RIGHT -> right - 4f
                else -> left + 5f
            }
            canvas.drawText(col.header, tx, tableTop + 13.5f, thP)
        }

        canvas.drawLine(MARGIN_LEFT, headerBottom, MARGIN_RIGHT, headerBottom, outerBorderPaint)

        var rowTop = headerBottom
        val tdTextBold = fonts.paint(Brand.INK, 8.4f, bold = true)
        val tdCenter = fonts.paint(Brand.INK, 8.4f, align = Paint.Align.CENTER)
        val tdCenterBold = fonts.paint(Brand.INK, 8.4f, bold = true, align = Paint.Align.CENTER)
        val tdRight = fonts.paint(Brand.INK, 8.4f, align = Paint.Align.RIGHT)
        val tdRightBold = fonts.paint(Brand.INK, 8.4f, bold = true, align = Paint.Align.RIGHT)

        for (item in dynamicData.items) {
            val rowHeight = 24f
            val textBaseline = rowTop + 15.5f

            for (i in columns.indices) {
                val col = columns[i]
                val left = colX[i]
                val right = colX[i + 1]
                val colW = right - left

                when (col.key) {
                    "sr" -> canvas.drawText(item.srNo.toString(), (left + right) / 2f, textBaseline, tdCenter)
                    "desc" -> {
                        val desc = wrapText(item.description.ifBlank { item.label }, tdTextBold, colW - 8f).firstOrNull() ?: item.description.ifBlank { item.label }
                        canvas.drawText(desc, left + 4f, textBaseline, tdTextBold)
                    }
                    "batch" -> canvas.drawText(item.batchNo ?: "-", (left + right) / 2f, textBaseline, tdCenter)
                    "exp" -> canvas.drawText(item.expiryDate ?: "-", (left + right) / 2f, textBaseline, tdCenter)
                    "unit_wt" -> canvas.drawText(item.weightOrMeasure ?: item.unit, (left + right) / 2f, textBaseline, tdCenter)
                    "unit" -> canvas.drawText(item.unit, (left + right) / 2f, textBaseline, tdCenter)
                    "hsn" -> canvas.drawText(item.hsn, (left + right) / 2f, textBaseline, tdCenter)
                    "qty" -> {
                        val qtyStr = if (item.qty % 1.0 == 0.0) item.qty.toInt().toString() else item.qty.toString()
                        canvas.drawText(qtyStr, (left + right) / 2f, textBaseline, tdCenterBold)
                    }
                    "mrp" -> canvas.drawText(item.mrp?.let { Formatters.plain(it) } ?: "-", right - 4f, textBaseline, tdRight)
                    "rate" -> canvas.drawText(Formatters.plain(item.rate), right - 4f, textBaseline, tdRight)
                    "disc" -> canvas.drawText(if (item.discount > 0) Formatters.plain(item.discount) else "-", right - 4f, textBaseline, tdRight)
                    "cgst" -> canvas.drawText(Formatters.plain(item.cgst), right - 4f, textBaseline, tdRight)
                    "sgst" -> canvas.drawText(Formatters.plain(item.sgst), right - 4f, textBaseline, tdRight)
                    "amount" -> canvas.drawText(Formatters.plain(item.totalAmount), right - 4f, textBaseline, tdRightBold)
                    "brand" -> canvas.drawText(item.brand ?: "-", left + 4f, textBaseline, tdCenter)
                    "warranty" -> canvas.drawText(item.warranty ?: "-", (left + right) / 2f, textBaseline, tdCenter)
                    else -> {}
                }
            }

            rowTop += rowHeight
            canvas.drawLine(MARGIN_LEFT, rowTop, MARGIN_RIGHT, rowTop, innerLinePaint)
        }

        val totalRowTop = rowTop
        val totalRowHeight = 20f
        val tableBottom = totalRowTop + totalRowHeight

        val totalBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.BG_HEADER
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN_LEFT, totalRowTop, MARGIN_RIGHT, tableBottom, totalBg)
        canvas.drawLine(MARGIN_LEFT, totalRowTop, MARGIN_RIGHT, totalRowTop, innerLinePaint)
        canvas.drawLine(MARGIN_LEFT, tableBottom, MARGIN_RIGHT, tableBottom, outerBorderPaint)

        val lastColLeft = colX[columns.size - 1]
        canvas.drawText("TOTAL TAXABLE VALUE", lastColLeft - 8f, totalRowTop + 14f, thRight)
        canvas.drawText(Formatters.plain(dynamicData.taxableSubtotal), MARGIN_RIGHT - 4f, totalRowTop + 14f, thRight)

        canvas.drawRect(MARGIN_LEFT, tableTop, MARGIN_RIGHT, tableBottom, outerBorderPaint)
        for (i in 1 until columns.size) {
            canvas.drawLine(colX[i], tableTop, colX[i], if (i >= columns.size - 1) tableBottom else totalRowTop, innerLinePaint)
        }

        y = tableBottom + 13f

        // 4. Amount in Words
        val wordsLabelPaint = fonts.paint(Brand.MUTED, 8.2f)
        val wordsValPaint = fonts.paint(Brand.INK, 8.6f, bold = true)
        canvas.drawText("Amount in Words:  ", MARGIN_LEFT, y, wordsLabelPaint)
        val wLabelWidth = wordsLabelPaint.measureText("Amount in Words:  ")
        canvas.drawText("${NumWords.rupees(doc.total)} Only", MARGIN_LEFT + wLabelWidth, y, wordsValPaint)
        y += 16f

        // 5. Official Remittance Mandate & Totals
        val midSectionTop = y
        val bankBoxHeight = 68f
        drawAdaptiveBankAndQrSection(
            context = context,
            canvas = canvas,
            fonts = fonts,
            topY = midSectionTop,
            boxHeight = bankBoxHeight,
            firm = firm,
            boxTitle = "OFFICIAL REMITTANCE MANDATE / BANK DETAILS",
            docNo = doc.docNo,
            docTotal = doc.total
        )

        val bankTotalBottomY = midSectionTop + bankBoxHeight

        // Right Column: Totals Breakdown
        val totLeft = 345f
        var totY = midSectionTop
        val totLblPaint = fonts.paint(Brand.MUTED, 8.2f)
        val totValPaint = fonts.paint(Brand.INK, 8.2f, align = Paint.Align.RIGHT)

        if (doc.discount > 0.0) {
            val grossVal = doc.taxable + doc.discount
            canvas.drawText("Gross Subtotal", totLeft, totY, totLblPaint)
            canvas.drawText(Formatters.plain(grossVal), MARGIN_RIGHT, totY, totValPaint)
            totY += 11.5f

            val discLblPaint = fonts.paint(Color.parseColor("#15803D"), 8.2f, bold = true)
            val discValPaint = fonts.paint(Color.parseColor("#15803D"), 8.2f, bold = true, align = Paint.Align.RIGHT)
            canvas.drawText("Special Discount", totLeft, totY, discLblPaint)
            canvas.drawText("- ₹ ${Formatters.plain(doc.discount)}", MARGIN_RIGHT, totY, discValPaint)
            totY += 11.5f

            canvas.drawText("Net Taxable Value", totLeft, totY, totLblPaint)
            canvas.drawText(Formatters.plain(doc.taxable), MARGIN_RIGHT, totY, totValPaint)
            totY += 11.5f
        } else {
            canvas.drawText("Taxable Value", totLeft, totY, totLblPaint)
            canvas.drawText(Formatters.plain(doc.taxable), MARGIN_RIGHT, totY, totValPaint)
            totY += 12.0f
        }

        if (doc.gstPercent > 0.0) {
            val halfRate = doc.gstPercent / 2.0
            val rateLabel = if (halfRate % 1.0 == 0.0) halfRate.toInt().toString() else halfRate.toString()
            canvas.drawText("CGST ($rateLabel%)", totLeft, totY, totLblPaint)
            canvas.drawText(Formatters.plain(doc.cgst), MARGIN_RIGHT, totY, totValPaint)
            totY += 12.0f
            canvas.drawText("SGST ($rateLabel%)", totLeft, totY, totLblPaint)
            canvas.drawText(Formatters.plain(doc.sgst), MARGIN_RIGHT, totY, totValPaint)
            totY += 12.0f
        } else {
            canvas.drawText("GST", totLeft, totY, totLblPaint)
            canvas.drawText("Not applicable", MARGIN_RIGHT, totY, totValPaint)
            totY += 12.0f
        }

        val strongRule = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE_STRONG
            strokeWidth = 1.0f
        }
        canvas.drawLine(totLeft, totY - 2f, MARGIN_RIGHT, totY - 2f, strongRule)
        totY += 12.5f

        val grandLabel = if (isInvoice) "INVOICE TOTAL" else "ESTIMATE TOTAL"
        val totalHeadPaint = fonts.paint(Brand.INK, 9.8f, bold = true)
        val totalNumPaint = fonts.paint(firm.primaryColor, 13.0f, bold = true, align = Paint.Align.RIGHT)

        canvas.drawText(grandLabel, totLeft, totY, totalHeadPaint)
        canvas.drawText("₹ ${Formatters.plain(doc.total)}", MARGIN_RIGHT, totY, totalNumPaint)
        totY += 7f

        if (isInvoice) {
            val lightRule = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Brand.LINE
                strokeWidth = 0.8f
            }
            canvas.drawLine(totLeft, totY, MARGIN_RIGHT, totY, lightRule)
            totY += 12.0f
            canvas.drawText("Amount Received", totLeft, totY, totLblPaint)
            canvas.drawText(Formatters.plain(detail.totalPaid), MARGIN_RIGHT, totY, totValPaint)
            totY += 5.5f
            canvas.drawLine(totLeft, totY, MARGIN_RIGHT, totY, strongRule)
            totY += 13.5f

            val balHeadPaint = fonts.paint(Brand.INK, 9.8f, bold = true)
            val balNumPaint = fonts.paint(Brand.INK, 13.0f, bold = true, align = Paint.Align.RIGHT)

            canvas.drawText("BALANCE DUE", totLeft, totY, balHeadPaint)
            canvas.drawText("₹ ${Formatters.plain(detail.pending)}", MARGIN_RIGHT, totY, balNumPaint)
            totY += 7f
        }

        y = maxOf(bankTotalBottomY, totY) + 14f

        // 6. Terms & Conditions and Digital Signature Block
        val bottomSectionTop = y
        val termsX = MARGIN_LEFT
        var termsY = bottomSectionTop
        if (doc.note.isNotBlank()) {
            canvas.drawText("NOTE", termsX, termsY, sectionLabelPaint)
            termsY += 9.5f
            canvas.drawText(doc.note, termsX, termsY, regularInk)
            termsY += 12.0f
        }

        canvas.drawText("TERMS & CONDITIONS", termsX, termsY, sectionLabelPaint)
        termsY += 10.5f
        val tcPaint = fonts.paint(Brand.MUTED, 6.7f)
        val termsList = if (doc.terms.isNotBlank()) {
            doc.terms.split("\n").filter { it.isNotBlank() }
        } else if (!isInvoice) {
            com.example.data.model.DEFAULT_ESTIMATE_TERMS.split("\n")
        } else {
            listOf(
                "1. Goods once sold will not be taken back or exchanged.",
                "2. Subject to local ${firm.city} / ${firm.state} jurisdiction only.",
                "3. Full payment required as per agreed payment milestones.",
                "4. All warranties covered directly by OEM / manufacturers."
            )
        }
        val sigX = 345f
        val maxTermsWidth = sigX - termsX - 10f
        drawTermsList(canvas, termsList, termsX, termsY, maxTermsWidth, tcPaint, lineSpacing = 8.5f)

        val sigWidth = MARGIN_RIGHT - sigX
        drawAdaptiveSignatureBlock(context, canvas, sigX, bottomSectionTop - 2f, sigWidth, height = 104f, firm = firm, fonts = fonts)

        // 7. Adaptive Footer
        drawAdaptiveFooter(canvas, fonts, firm)

        pdfDoc.finishPage(page)

        val pdfDir = File(context.filesDir, "PDFs").apply { mkdirs() }
        val docPrefix = if (isInvoice) "Invoice" else "Estimate"
        val safeName = Formatters.safeFileName(customer.name)
        val fileName = "${docPrefix}_${doc.docNo}_${safeName}_${Formatters.fmtDateFile(doc.docDate)}.pdf"
        val outputFile = File(pdfDir, fileName)

        FileOutputStream(outputFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        return outputFile
    }

    // =========================================================================================
    // 2. CIVIL CONSTRUCTION & BOQ ESTIMATE PDF GENERATOR
    // =========================================================================================
    fun generateConstructionPdf(
        context: Context,
        detail: DocWithDetails,
        company: Company = Company(),
        business: BusinessProfile? = null,
        themeStr: String? = null
    ): File {
        val doc = detail.doc
        val customer = detail.customer ?: Customer(name = "Valued Client")
        val firm = FirmBranding.from(business, company, themeStr)
        val fonts = FontKit(context)
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // 1. Adaptive Letterhead
        var y = drawAdaptiveLetterhead(context, canvas, "CIVIL ESTIMATE", "BILL OF QUANTITIES / WORK SCHEDULE", fonts, firm)

        // 2. Party & Scope Info
        val partyBlockTop = y
        val sectionLabelPaint = fonts.paint(Brand.MUTED, 7.8f, bold = true)
        val boldInkPaint = fonts.paint(Brand.INK, 10.5f, bold = true)
        val regularInk = fonts.paint(Brand.INK, 8.5f)
        val regularMuted = fonts.paint(Brand.MUTED, 8.2f)

        canvas.drawText("CLIENT / SITE DETAILS", MARGIN_LEFT, y + 2f, sectionLabelPaint)
        y += 14f
        canvas.drawText(customer.name, MARGIN_LEFT, y, boldInkPaint)
        y += 13.5f

        if (customer.mobile.isNotBlank()) {
            canvas.drawText("Mobile: ${customer.mobile}", MARGIN_LEFT, y, regularInk)
            y += 12.0f
        }
        val addr = listOf(customer.village, customer.address).filter { it.isNotBlank() }.joinToString(", ")
        if (addr.isNotBlank()) {
            canvas.drawText("Site: $addr", MARGIN_LEFT, y, regularMuted)
            y += 12.0f
        }
        canvas.drawText("Place of Supply: ${firm.placeOfSupply}", MARGIN_LEFT, y, regularMuted)

        val statsLeft = 345f
        val statColWidth = (MARGIN_RIGHT - statsLeft) / 3f

        canvas.drawText("ESTIMATE NO.", statsLeft, partyBlockTop + 2f, sectionLabelPaint)
        canvas.drawText(doc.docNo, statsLeft, partyBlockTop + 16f, fonts.paint(Brand.INK, 9.6f, bold = true))

        canvas.drawText("ESTIMATE DATE", statsLeft + statColWidth, partyBlockTop + 2f, sectionLabelPaint)
        canvas.drawText(Formatters.fmtDate(doc.docDate), statsLeft + statColWidth, partyBlockTop + 16f, fonts.paint(Brand.INK, 9.6f, bold = true))

        canvas.drawText("VALID TILL", statsLeft + statColWidth * 2, partyBlockTop + 2f, sectionLabelPaint)
        val validStr = if (doc.validTill.isNotBlank()) Formatters.fmtDate(doc.validTill) else "-"
        canvas.drawText(validStr, statsLeft + statColWidth * 2, partyBlockTop + 16f, fonts.paint(Brand.INK, 9.6f, bold = true))

        y = maxOf(y + 16f, partyBlockTop + 54f)

        // 3. Civil Grid Table
        val tableTop = y
        val headerHeight = 20f
        val headerBottom = tableTop + headerHeight

        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.BG_HEADER
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN_LEFT, tableTop, MARGIN_RIGHT, headerBottom, headerBgPaint)

        val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE_STRONG
            style = Paint.Style.STROKE
            strokeWidth = 1.0f
        }
        val innerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE
            strokeWidth = 0.6f
        }

        val colX0 = MARGIN_LEFT
        val colX1 = MARGIN_LEFT + 24f
        val colX2 = colX1 + 220f
        val colX3 = colX2 + 48f
        val colX4 = colX3 + 46f
        val colX5 = colX4 + 75f
        val colX6 = MARGIN_RIGHT

        val thPaint = fonts.paint(Brand.INK, 7.6f, bold = true)
        val thCenter = fonts.paint(Brand.INK, 7.6f, bold = true, align = Paint.Align.CENTER)
        val thRight = fonts.paint(Brand.INK, 7.6f, bold = true, align = Paint.Align.RIGHT)

        canvas.drawText("SR", (colX0 + colX1) / 2f, tableTop + 13.5f, thCenter)
        canvas.drawText("ITEM DESCRIPTION / SPECIFICATION", colX1 + 6f, tableTop + 13.5f, thPaint)
        canvas.drawText("QTY", (colX2 + colX3) / 2f, tableTop + 13.5f, thCenter)
        canvas.drawText("UNIT", (colX3 + colX4) / 2f, tableTop + 13.5f, thCenter)
        canvas.drawText("RATE (₹)", colX5 - 6f, tableTop + 13.5f, thRight)
        canvas.drawText("AMOUNT (₹)", colX6 - 6f, tableTop + 13.5f, thRight)

        canvas.drawLine(colX0, headerBottom, colX6, headerBottom, outerBorderPaint)

        var rowTop = headerBottom
        var srNo = 1
        val tdTextBold = fonts.paint(Brand.INK, 8.4f, bold = true)
        val tdCenter = fonts.paint(Brand.INK, 8.4f, align = Paint.Align.CENTER)
        val tdCenterBold = fonts.paint(Brand.INK, 8.4f, bold = true, align = Paint.Align.CENTER)
        val tdRight = fonts.paint(Brand.INK, 8.4f, align = Paint.Align.RIGHT)
        val tdRightBold = fonts.paint(Brand.INK, 8.4f, bold = true, align = Paint.Align.RIGHT)

        for (item in detail.items) {
            val rowHeight = 22f
            val textBaseline = rowTop + 14.5f

            canvas.drawText(srNo.toString(), (colX0 + colX1) / 2f, textBaseline, tdCenter)
            val desc = wrapText(item.description.ifBlank { item.label }, tdTextBold, colX2 - colX1 - 10f).firstOrNull() ?: item.description.ifBlank { item.label }
            canvas.drawText(desc, colX1 + 6f, textBaseline, tdTextBold)

            val qtyStr = if (item.qty % 1.0 == 0.0) item.qty.toInt().toString() else item.qty.toString()
            canvas.drawText(qtyStr, (colX2 + colX3) / 2f, textBaseline, tdCenterBold)

            val unitStr = item.unit.ifBlank { "Nos" }
            canvas.drawText(unitStr, (colX3 + colX4) / 2f, textBaseline, tdCenter)

            canvas.drawText(Formatters.plain(item.rate), colX5 - 6f, textBaseline, tdRight)
            canvas.drawText(Formatters.plain(item.amount), colX6 - 6f, textBaseline, tdRightBold)

            rowTop += rowHeight
            canvas.drawLine(colX0, rowTop, colX6, rowTop, innerLinePaint)
            srNo++
        }

        val totalRowTop = rowTop
        val totalRowHeight = 20f
        val tableBottom = totalRowTop + totalRowHeight

        canvas.drawRect(MARGIN_LEFT, totalRowTop, MARGIN_RIGHT, tableBottom, headerBgPaint)
        canvas.drawLine(MARGIN_LEFT, totalRowTop, MARGIN_RIGHT, totalRowTop, innerLinePaint)
        canvas.drawLine(MARGIN_LEFT, tableBottom, MARGIN_RIGHT, tableBottom, outerBorderPaint)

        canvas.drawText("SUBTOTAL (CIVIL WORKS)", colX5 - 8f, totalRowTop + 14f, thRight)
        canvas.drawText(Formatters.plain(doc.taxable), colX6 - 6f, totalRowTop + 14f, thRight)

        canvas.drawRect(MARGIN_LEFT, tableTop, MARGIN_RIGHT, tableBottom, outerBorderPaint)
        for (bx in floatArrayOf(colX1, colX2, colX3, colX4, colX5)) {
            canvas.drawLine(bx, tableTop, bx, totalRowTop, innerLinePaint)
        }
        canvas.drawLine(colX5, totalRowTop, colX5, tableBottom, innerLinePaint)

        y = tableBottom + 12f

        // 4. Amount in Words
        val wordsLabelPaint = fonts.paint(Brand.MUTED, 7.8f)
        val wordsTextPaint = fonts.paint(Brand.INK, 8.2f, bold = true)
        canvas.drawText("Amount Chargeable (in words):  ", MARGIN_LEFT, y, wordsLabelPaint)
        val wLabelWidth = wordsLabelPaint.measureText("Amount Chargeable (in words):  ")
        canvas.drawText("Rupees ${doc.amountWords} Only", MARGIN_LEFT + wLabelWidth, y, wordsTextPaint)
        y += 16f

        // 5. Bank & Totals
        val bankBoxHeight = 70f
        drawAdaptiveBankAndQrSection(
            context = context,
            canvas = canvas,
            fonts = fonts,
            topY = y,
            boxHeight = bankBoxHeight,
            firm = firm,
            boxTitle = "BANK DETAILS FOR REMITTANCE / CHEQUE"
        )

        var totY = y
        val totLeft = 345f
        val totLbl = fonts.paint(Brand.MUTED, 8.2f)
        val totVal = fonts.paint(Brand.INK, 8.2f, align = Paint.Align.RIGHT)

        if (doc.discount > 0.0) {
            val grossVal = doc.taxable + doc.discount
            canvas.drawText("Gross Subtotal", totLeft, totY + 8f, totLbl)
            canvas.drawText("₹ ${Formatters.plain(grossVal)}", MARGIN_RIGHT, totY + 8f, totVal)
            totY += 13.5f

            val discLbl = fonts.paint(Color.parseColor("#15803D"), 8.2f, bold = true)
            val discVal = fonts.paint(Color.parseColor("#15803D"), 8.2f, bold = true, align = Paint.Align.RIGHT)
            canvas.drawText("Special Discount", totLeft, totY + 8f, discLbl)
            canvas.drawText("- ₹ ${Formatters.plain(doc.discount)}", MARGIN_RIGHT, totY + 8f, discVal)
            totY += 13.5f

            canvas.drawText("Net Taxable Value", totLeft, totY + 8f, totLbl)
            canvas.drawText("₹ ${Formatters.plain(doc.taxable)}", MARGIN_RIGHT, totY + 8f, totVal)
            totY += 13.5f
        } else {
            canvas.drawText("Taxable Value (Works)", totLeft, totY + 8f, totLbl)
            canvas.drawText("₹ ${Formatters.plain(doc.taxable)}", MARGIN_RIGHT, totY + 8f, totVal)
            totY += 14.5f
        }

        val halfGst = doc.gstPercent / 2.0
        if (doc.gstPercent > 0.0) {
            val halfGstStr = if (halfGst % 1.0 == 0.0) "${halfGst.toInt()}%" else "$halfGst%"
            canvas.drawText("CGST ($halfGstStr)", totLeft, totY + 8f, totLbl)
            canvas.drawText("₹ ${Formatters.plain(doc.cgst)}", MARGIN_RIGHT, totY + 8f, totVal)
            totY += 14.5f

            canvas.drawText("SGST ($halfGstStr)", totLeft, totY + 8f, totLbl)
            canvas.drawText("₹ ${Formatters.plain(doc.sgst)}", MARGIN_RIGHT, totY + 8f, totVal)
            totY += 14.5f
        }

        val totRule = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE_STRONG
            strokeWidth = 1.0f
        }
        canvas.drawLine(totLeft, totY + 2f, MARGIN_RIGHT, totY + 2f, totRule)
        totY += 13.0f

        val totHeadPaint = fonts.paint(Brand.INK, 9.8f, bold = true)
        val totNumPaint = fonts.paint(firm.primaryColor, 11.5f, bold = true, align = Paint.Align.RIGHT)
        canvas.drawText("ESTIMATE TOTAL", totLeft, totY + 3f, totHeadPaint)
        canvas.drawText("₹ ${Formatters.plain(doc.total)}", MARGIN_RIGHT, totY + 3f, totNumPaint)

        y = maxOf(y + bankBoxHeight, totY + 14f) + 12f

        // 6. Terms & Signature Block
        val bottomSectionTop = y
        val termsX = MARGIN_LEFT
        var termsY = bottomSectionTop

        if (doc.note.isNotBlank()) {
            canvas.drawText("PROJECT SCOPE NOTE", termsX, termsY, sectionLabelPaint)
            termsY += 9.5f
            canvas.drawText(doc.note, termsX, termsY, regularInk)
            termsY += 12.0f
        }

        canvas.drawText("COMMERCIAL TERMS & SPECIFICATIONS", termsX, termsY, sectionLabelPaint)
        termsY += 10.5f
        val tcPaint = fonts.paint(Brand.MUTED, 6.7f)
        val termsList = if (doc.terms.isNotBlank()) {
            doc.terms.split("\n").filter { it.isNotBlank() }
        } else {
            listOf(
                "1. Measurements based on actual work executed at site.",
                "2. Water and electricity to be provided by client at site.",
                "3. Progress billing will be submitted as per execution stages.",
                "4. All material specifications adhere to standard IS guidelines."
            )
        }
        val sigX = 345f
        val maxTermsWidth = sigX - termsX - 10f
        drawTermsList(canvas, termsList, termsX, termsY, maxTermsWidth, tcPaint, lineSpacing = 8.5f)

        val sigWidth = MARGIN_RIGHT - sigX
        drawAdaptiveSignatureBlock(context, canvas, sigX, bottomSectionTop - 2f, sigWidth, height = 104f, firm = firm, fonts = fonts)

        // 7. Footer
        drawAdaptiveFooter(canvas, fonts, firm)

        pdfDoc.finishPage(page)

        val pdfDir = File(context.filesDir, "PDFs").apply { mkdirs() }
        val safeName = Formatters.safeFileName(customer.name)
        val fileName = "Civil_Estimate_${doc.docNo}_${safeName}_${Formatters.fmtDateFile(doc.docDate)}.pdf"
        val outputFile = File(pdfDir, fileName)

        FileOutputStream(outputFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        return outputFile
    }

    // =========================================================================================
    // 3. OFFICIAL PAYMENT RECEIPT PDF GENERATOR
    // =========================================================================================
    fun generateReceiptPdf(
        context: Context,
        detail: DocWithDetails,
        payment: Payment,
        company: Company = Company(),
        business: BusinessProfile? = null,
        themeStr: String? = null
    ): File {
        val doc = detail.doc
        val customer = detail.customer ?: Customer(name = "Valued Customer")
        val firm = FirmBranding.from(business, company, themeStr)
        val fonts = FontKit(context)
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // 1. Adaptive Letterhead
        var y = drawAdaptiveLetterhead(context, canvas, "PAYMENT RECEIPT", "Official Electronic Acknowledgement", fonts, firm)

        // 2. Receipt Voucher Banner
        val bannerTop = y
        val bannerHeight = 32f
        val bannerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = firm.bgTint
            style = Paint.Style.FILL
        }
        val bannerBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        canvas.drawRect(MARGIN_LEFT, bannerTop, MARGIN_RIGHT, bannerTop + bannerHeight, bannerBg)
        canvas.drawRect(MARGIN_LEFT, bannerTop, MARGIN_RIGHT, bannerTop + bannerHeight, bannerBorder)

        val rcptNoPaint = fonts.paint(Brand.INK, 11f, bold = true)
        val rcptDatePaint = fonts.paint(Brand.MUTED, 8.5f, bold = true, align = Paint.Align.RIGHT)
        canvas.drawText("RECEIPT VOUCHER #${payment.id}", MARGIN_LEFT + 12f, bannerTop + 20f, rcptNoPaint)
        canvas.drawText("Receipt Date: ${Formatters.fmtDate(payment.payDate)}", MARGIN_RIGHT - 12f, bannerTop + 20f, rcptDatePaint)

        y += bannerHeight + 14f

        // 3. Payer & Payment Details Grid Box
        val payBoxTop = y
        val payBoxHeight = 110f
        val payBoxBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN_LEFT, payBoxTop, MARGIN_RIGHT, payBoxTop + payBoxHeight, payBoxBg)
        canvas.drawRect(MARGIN_LEFT, payBoxTop, MARGIN_RIGHT, payBoxTop + payBoxHeight, bannerBorder)

        var pRowY = payBoxTop + 18f
        val pLblPaint = fonts.paint(Brand.MUTED, 8.2f)
        val pValPaint = fonts.paint(Brand.INK, 8.8f, bold = true)
        val pAmtPaint = fonts.paint(Color.parseColor("#15803D"), 11.5f, bold = true)

        fun drawPayRow(lbl: String, value: String, isAmt: Boolean = false) {
            canvas.drawText(lbl, MARGIN_LEFT + 14f, pRowY, pLblPaint)
            val valP = if (isAmt) pAmtPaint else pValPaint
            canvas.drawText(value, MARGIN_LEFT + 140f, pRowY, valP)
            pRowY += 15.5f
        }

        drawPayRow("Received From (Party):", customer.name)
        drawPayRow("Amount Received:", "₹ ${Formatters.formatInr(payment.amount)}  (${NumWords.rupees(payment.amount)} Only)", isAmt = true)
        drawPayRow("Payment Mode / Channel:", payment.mode)
        val refStr = if (payment.reference.isNotBlank()) payment.reference else "Direct Settlement"
        drawPayRow("Transaction Ref / UTR:", refStr)
        drawPayRow("Applied Against Document:", "${if (doc.isInvoice) "Invoice" else "Estimate"} #${doc.docNo} (Total: ₹ ${Formatters.plain(doc.total)})")

        y = payBoxTop + payBoxHeight + 14f

        // 4. Financial Status Breakdown
        val statusBoxTop = y
        val statusBoxHeight = 44f
        val statBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.BG_HEADER
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN_LEFT, statusBoxTop, MARGIN_RIGHT, statusBoxTop + statusBoxHeight, statBg)
        canvas.drawRect(MARGIN_LEFT, statusBoxTop, MARGIN_RIGHT, statusBoxTop + statusBoxHeight, bannerBorder)

        val colWidth = CONTENT_WIDTH / 3f
        val statLbl = fonts.paint(Brand.MUTED, 7.5f, bold = true, align = Paint.Align.CENTER)
        val statVal = fonts.paint(Brand.INK, 10f, bold = true, align = Paint.Align.CENTER)

        // Col 1: Invoice Total
        canvas.drawText("TOTAL BILLED", MARGIN_LEFT + colWidth / 2f, statusBoxTop + 14f, statLbl)
        canvas.drawText("₹ ${Formatters.plain(doc.total)}", MARGIN_LEFT + colWidth / 2f, statusBoxTop + 32f, statVal)

        // Col 2: Total Received to date
        val greenVal = fonts.paint(Color.parseColor("#15803D"), 10f, bold = true, align = Paint.Align.CENTER)
        canvas.drawText("TOTAL RECEIVED", MARGIN_LEFT + colWidth + colWidth / 2f, statusBoxTop + 14f, statLbl)
        canvas.drawText("₹ ${Formatters.plain(detail.totalPaid)}", MARGIN_LEFT + colWidth + colWidth / 2f, statusBoxTop + 32f, greenVal)

        // Col 3: Pending Balance
        val dueColor = if (detail.pending > 0.01) Color.parseColor("#DC2626") else Color.parseColor("#15803D")
        val dueVal = fonts.paint(dueColor, 10f, bold = true, align = Paint.Align.CENTER)
        canvas.drawText("CURRENT BALANCE DUE", MARGIN_LEFT + colWidth * 2f + colWidth / 2f, statusBoxTop + 14f, statLbl)
        canvas.drawText("₹ ${Formatters.plain(detail.pending)}", MARGIN_LEFT + colWidth * 2f + colWidth / 2f, statusBoxTop + 32f, dueVal)

        canvas.drawLine(MARGIN_LEFT + colWidth, statusBoxTop, MARGIN_LEFT + colWidth, statusBoxTop + statusBoxHeight, bannerBorder)
        canvas.drawLine(MARGIN_LEFT + colWidth * 2f, statusBoxTop, MARGIN_LEFT + colWidth * 2f, statusBoxTop + statusBoxHeight, bannerBorder)

        y = statusBoxTop + statusBoxHeight + 14f

        // 5. Bank Details & Signature
        val bankBoxHeight = 68f
        drawAdaptiveBankAndQrSection(
            context = context,
            canvas = canvas,
            fonts = fonts,
            topY = y,
            boxHeight = bankBoxHeight,
            firm = firm,
            boxTitle = "SETTLEMENT / BANK MANDATE"
        )

        val sigX = 345f
        val sigWidth = MARGIN_RIGHT - sigX
        drawAdaptiveSignatureBlock(context, canvas, sigX, y - 2f, sigWidth, height = 104f, firm = firm, fonts = fonts)

        // 6. Footer
        drawAdaptiveFooter(canvas, fonts, firm)

        pdfDoc.finishPage(page)

        val pdfDir = File(context.filesDir, "PDFs").apply { mkdirs() }
        val safeName = Formatters.safeFileName(customer.name)
        val fileName = "Receipt_${payment.id}_${doc.docNo}_${safeName}.pdf"
        val outputFile = File(pdfDir, fileName)

        FileOutputStream(outputFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        return outputFile
    }

    // =========================================================================================
    // 4. PARTY STATEMENT / ACCOUNT LEDGER PDF GENERATOR
    // =========================================================================================
    fun generatePartyStatementPdf(
        context: Context,
        statement: PartyStatementData,
        business: BusinessProfile? = null,
        company: Company = Company(),
        themeStr: String? = null
    ): File {
        val firm = FirmBranding.from(business, company, themeStr)
        val fonts = FontKit(context)
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // 1. Adaptive Letterhead
        var y = drawAdaptiveLetterhead(context, canvas, "STATEMENT OF ACCOUNT", "Khata Ledger & Financial Summary", fonts, firm)

        // 2. Party Information & Period Box
        val partyBoxTop = y
        val partyBoxHeight = 46f
        val boxBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.BG_HEADER
            style = Paint.Style.FILL
        }
        val boxBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        canvas.drawRect(MARGIN_LEFT, partyBoxTop, MARGIN_RIGHT, partyBoxTop + partyBoxHeight, boxBg)
        canvas.drawRect(MARGIN_LEFT, partyBoxTop, MARGIN_RIGHT, partyBoxTop + partyBoxHeight, boxBorder)

        val partyNamePaint = fonts.paint(Brand.INK, 11f, bold = true)
        val partyMetaPaint = fonts.paint(Brand.MUTED, 8f)
        val rightMetaPaint = fonts.paint(Brand.MUTED, 7.8f, align = Paint.Align.RIGHT)

        canvas.drawText(statement.party.name, MARGIN_LEFT + 10f, partyBoxTop + 16f, partyNamePaint)
        val pMeta = listOfNotNull(
            statement.party.mobile.takeIf { it.isNotBlank() }?.let { "Mobile: $it" },
            statement.party.village.takeIf { it.isNotBlank() }?.let { "Site: $it" },
            "Type: ${statement.party.category.name}"
        ).joinToString("   |   ")
        canvas.drawText(pMeta, MARGIN_LEFT + 10f, partyBoxTop + 32f, partyMetaPaint)

        val periodStr = if (!statement.fromDate.isNullOrBlank() && !statement.toDate.isNullOrBlank()) {
            "Period: ${statement.fromDate} to ${statement.toDate}"
        } else {
            "Period: All Transactions"
        }
        canvas.drawText(periodStr, MARGIN_RIGHT - 10f, partyBoxTop + 16f, rightMetaPaint)
        canvas.drawText("Generated: ${Formatters.todayIso()}", MARGIN_RIGHT - 10f, partyBoxTop + 32f, rightMetaPaint)

        y = partyBoxTop + partyBoxHeight + 14f

        // 3. Transactions Table
        val tableTop = y
        val headerHeight = 20f
        val headerBottom = tableTop + headerHeight

        canvas.drawRect(MARGIN_LEFT, tableTop, MARGIN_RIGHT, headerBottom, boxBg)

        val colX0 = MARGIN_LEFT
        val colX1 = MARGIN_LEFT + 55f
        val colX2 = colX1 + 175f
        val colX3 = colX2 + 75f
        val colX4 = colX3 + 75f
        val colX5 = colX4 + 75f
        val colX6 = MARGIN_RIGHT

        val thCenter = fonts.paint(Brand.INK, 7.4f, bold = true, align = Paint.Align.CENTER)
        val thLeft = fonts.paint(Brand.INK, 7.4f, bold = true)
        val thRight = fonts.paint(Brand.INK, 7.4f, bold = true, align = Paint.Align.RIGHT)

        canvas.drawText("DATE", (colX0 + colX1) / 2f, tableTop + 13.5f, thCenter)
        canvas.drawText("TRANSACTION / PARTICULARS", colX1 + 6f, tableTop + 13.5f, thLeft)
        canvas.drawText("DEBIT (₹)", colX3 - 6f, tableTop + 13.5f, thRight)
        canvas.drawText("CREDIT (₹)", colX4 - 6f, tableTop + 13.5f, thRight)
        canvas.drawText("BALANCE (₹)", colX5 - 6f, tableTop + 13.5f, thRight)
        canvas.drawText("TYPE", (colX5 + colX6) / 2f, tableTop + 13.5f, thCenter)

        val outerBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE_STRONG
            style = Paint.Style.STROKE
            strokeWidth = 1.0f
        }
        val innerLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Brand.LINE
            strokeWidth = 0.6f
        }
        canvas.drawLine(colX0, headerBottom, colX6, headerBottom, outerBorder)

        var rowTop = headerBottom
        val tdDate = fonts.paint(Brand.MUTED, 7.6f, align = Paint.Align.CENTER)
        val tdDesc = fonts.paint(Brand.INK, 7.8f, bold = true)
        val tdDebit = fonts.paint(Color.parseColor("#DC2626"), 8f, bold = true, align = Paint.Align.RIGHT)
        val tdCredit = fonts.paint(Color.parseColor("#15803D"), 8f, bold = true, align = Paint.Align.RIGHT)
        val tdBal = fonts.paint(Brand.INK, 8f, bold = true, align = Paint.Align.RIGHT)
        val tdType = fonts.paint(Brand.MUTED, 7.2f, bold = true, align = Paint.Align.CENTER)

        val entries = statement.entries.take(18) // Clean pagination safety
        for (entry in entries) {
            val rowHeight = 20f
            val base = rowTop + 13.5f

            canvas.drawText(entry.date, (colX0 + colX1) / 2f, base, tdDate)
            val desc = wrapText(entry.particulars, tdDesc, colX2 - colX1 - 10f).firstOrNull() ?: entry.particulars
            canvas.drawText(desc, colX1 + 6f, base, tdDesc)

            if (entry.debit > 0) {
                canvas.drawText(Formatters.formatInr(entry.debit), colX3 - 6f, base, tdDebit)
            } else {
                canvas.drawText("-", colX3 - 6f, base, fonts.paint(Brand.FAINT, 7.8f, align = Paint.Align.RIGHT))
            }

            if (entry.credit > 0) {
                canvas.drawText(Formatters.formatInr(entry.credit), colX4 - 6f, base, tdCredit)
            } else {
                canvas.drawText("-", colX4 - 6f, base, fonts.paint(Brand.FAINT, 7.8f, align = Paint.Align.RIGHT))
            }

            canvas.drawText(Formatters.formatInr(kotlin.math.abs(entry.runningBalance)), colX5 - 6f, base, tdBal)
            val balType = if (entry.runningBalance > 0.01) "Dr" else if (entry.runningBalance < -0.01) "Cr" else "-"
            canvas.drawText(balType, (colX5 + colX6) / 2f, base, tdType)

            rowTop += rowHeight
            canvas.drawLine(colX0, rowTop, colX6, rowTop, innerLine)
        }

        val totalRowTop = rowTop
        val totalRowHeight = 20f
        val tableBottom = totalRowTop + totalRowHeight

        canvas.drawRect(MARGIN_LEFT, totalRowTop, MARGIN_RIGHT, tableBottom, boxBg)
        canvas.drawLine(MARGIN_LEFT, totalRowTop, MARGIN_RIGHT, totalRowTop, innerLine)
        canvas.drawLine(MARGIN_LEFT, tableBottom, MARGIN_RIGHT, tableBottom, outerBorder)

        canvas.drawText("TOTALS", colX1 + 6f, totalRowTop + 13.5f, thLeft)
        canvas.drawText(Formatters.formatInr(statement.totalDebit), colX3 - 6f, totalRowTop + 13.5f, thRight)
        canvas.drawText(Formatters.formatInr(statement.totalCredit), colX4 - 6f, totalRowTop + 13.5f, thRight)

        canvas.drawRect(MARGIN_LEFT, tableTop, MARGIN_RIGHT, tableBottom, outerBorder)
        for (bx in floatArrayOf(colX1, colX2, colX3, colX4, colX5)) {
            canvas.drawLine(bx, tableTop, bx, totalRowTop, innerLine)
        }
        for (bx in floatArrayOf(colX3, colX4)) {
            canvas.drawLine(bx, totalRowTop, bx, tableBottom, innerLine)
        }

        y = tableBottom + 14f

        // 4. Net Outstanding Summary Banner
        val netBoxTop = y
        val netBoxHeight = 36f
        val netBgColor = if (statement.isReceivable) Color.parseColor("#FEF2F2") else if (statement.isPayable) Color.parseColor("#F0FDF4") else Color.parseColor("#F8FAFC")
        val netBorderColor = if (statement.isReceivable) Color.parseColor("#FCA5A5") else if (statement.isPayable) Color.parseColor("#86EFAC") else Brand.LINE

        val netBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = netBgColor
            style = Paint.Style.FILL
        }
        val netBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = netBorderColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(RectF(MARGIN_LEFT, netBoxTop, MARGIN_RIGHT, netBoxTop + netBoxHeight), 3f, 3f, netBg)
        canvas.drawRoundRect(RectF(MARGIN_LEFT, netBoxTop, MARGIN_RIGHT, netBoxTop + netBoxHeight), 3f, 3f, netBorder)

        val netTitlePaint = fonts.paint(Brand.INK, 10f, bold = true)
        val netAmtColor = if (statement.isReceivable) Color.parseColor("#DC2626") else if (statement.isPayable) Color.parseColor("#15803D") else Brand.INK
        val netAmtPaint = fonts.paint(netAmtColor, 12f, bold = true, align = Paint.Align.RIGHT)

        val netLabel = if (statement.isReceivable) "NET BALANCE DUE (RECEIVABLE):" else if (statement.isPayable) "NET ADVANCE (PAYABLE):" else "NET BALANCE:"
        val netValue = if (statement.isReceivable) {
            "₹ ${Formatters.formatInr(statement.netBalance)} Dr"
        } else if (statement.isPayable) {
            "₹ ${Formatters.formatInr(kotlin.math.abs(statement.netBalance))} Cr"
        } else {
            "₹ 0.00 (Settled)"
        }

        canvas.drawText(netLabel, MARGIN_LEFT + 12f, netBoxTop + 22f, netTitlePaint)
        canvas.drawText(netValue, MARGIN_RIGHT - 12f, netBoxTop + 22f, netAmtPaint)

        y = netBoxTop + netBoxHeight + 14f

        // 5. Bank & Signatory Block
        val bankBoxHeight = 68f
        drawAdaptiveBankAndQrSection(
            context = context,
            canvas = canvas,
            fonts = fonts,
            topY = y,
            boxHeight = bankBoxHeight,
            firm = firm,
            boxTitle = "BANK REMITTANCE DETAILS"
        )

        val sigX = 345f
        val sigWidth = MARGIN_RIGHT - sigX
        drawAdaptiveSignatureBlock(context, canvas, sigX, y - 2f, sigWidth, height = 104f, firm = firm, fonts = fonts)

        // 6. Footer
        drawAdaptiveFooter(canvas, fonts, firm)

        pdfDoc.finishPage(page)

        val pdfDir = File(context.filesDir, "PDFs").apply { mkdirs() }
        val safeName = Formatters.safeFileName(statement.party.name)
        val fileName = "Statement_${statement.party.id}_${safeName}_${Formatters.fmtDateFile(Formatters.todayIso())}.pdf"
        val outputFile = File(pdfDir, fileName)

        FileOutputStream(outputFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        return outputFile
    }
}
