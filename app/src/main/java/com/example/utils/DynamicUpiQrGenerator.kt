package com.example.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.net.URLEncoder

/**
 * Generates dynamic UPI QR codes per-invoice.
 *
 * The QR encodes a standard NPCI UPI URI:
 *   upi://pay?pa={VPA}&pn={NAME}&am={AMOUNT}&tr={INVOICE_NO}&cu=INR
 *
 * When scanned by PhonePe / GPay / Paytm, it auto-fills:
 *   - Payee: The business owner's UPI ID
 *   - Amount: The exact invoice total (locked, cannot be changed by payer)
 *   - Reference: The invoice number for reconciliation
 */
object DynamicUpiQrGenerator {
    /**
     * Build a standard NPCI-compliant UPI payment URI string.
     */
    fun buildUpiUri(
        upiVpa: String?,
        payeeName: String,
        amount: Double,
        txnRef: String,
        txnNote: String = "Invoice Payment"
    ): String? {
        if (upiVpa.isNullOrBlank()) return null

        val encodedName = URLEncoder.encode(payeeName.take(50), "UTF-8")
        val encodedNote = URLEncoder.encode(txnNote.take(50), "UTF-8")
        val formattedAmount = String.format(java.util.Locale.US, "%.2f", amount)

        return "upi://pay?" +
                "pa=${upiVpa}" +
                "&pn=${encodedName}" +
                "&am=${formattedAmount}" +
                "&tr=${txnRef}" +
                "&tn=${encodedNote}" +
                "&cu=INR"
    }

    /**
     * Generate a QR code Bitmap from any string.
     */
    fun generateQrBitmap(content: String, size: Int = 400): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,          // Minimal white border
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convenience: Generate a QR Bitmap for a specific invoice payment.
     * Returns null if the business has no UPI VPA configured.
     */
    fun generateInvoicePaymentQr(
        upiVpa: String?,
        firmName: String,
        invoiceNo: String,
        invoiceTotal: Double,
        qrSize: Int = 400
    ): Bitmap? {
        val uri = buildUpiUri(
            upiVpa = upiVpa,
            payeeName = firmName,
            amount = invoiceTotal,
            txnRef = invoiceNo,
            txnNote = "Payment for $invoiceNo"
        ) ?: return null

        return generateQrBitmap(uri, qrSize)
    }
}
