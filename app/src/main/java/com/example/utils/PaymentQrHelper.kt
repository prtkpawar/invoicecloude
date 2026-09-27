package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.min

/**
 * Utility to manage the Business UPI / Bank Payment QR code image.
 * Handles photo picker import, auto-cropping/scaling to high-DPI square format,
 * disk persistence, and rendering in PDF documents.
 */
object PaymentQrHelper {

    private const val QR_FILE_NAME = "business_payment_qr.png"
    private const val MAX_DIMENSION = 800

    fun getQrFile(context: Context): File {
        return File(context.filesDir, QR_FILE_NAME)
    }

    fun hasQrCode(context: Context): Boolean {
        val file = getQrFile(context)
        return file.exists() && file.length() > 0
    }

    fun getQrBitmap(context: Context): Bitmap? {
        val file = getQrFile(context)
        if (!file.exists() || file.length() == 0L) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Reads the chosen image URI, scales and center-crops into a clean square,
     * and saves as an optimized PNG file in internal storage.
     */
    fun saveQrCode(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return false

            // Ensure square aspect ratio and max dimension for crisp PDF rendering
            val width = originalBitmap.width
            val height = originalBitmap.height
            val squareDim = min(width, height)
            val cropX = (width - squareDim) / 2
            val cropY = (height - squareDim) / 2

            val croppedBitmap = Bitmap.createBitmap(originalBitmap, cropX, cropY, squareDim, squareDim)

            val finalBitmap = if (squareDim > MAX_DIMENSION) {
                Bitmap.createScaledBitmap(croppedBitmap, MAX_DIMENSION, MAX_DIMENSION, true)
            } else {
                croppedBitmap
            }

            val targetFile = getQrFile(context)
            val outputStream = FileOutputStream(targetFile)
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            if (croppedBitmap != originalBitmap) croppedBitmap.recycle()
            if (finalBitmap != croppedBitmap && finalBitmap != originalBitmap) finalBitmap.recycle()
            originalBitmap.recycle()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteQrCode(context: Context): Boolean {
        return try {
            val file = getQrFile(context)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
