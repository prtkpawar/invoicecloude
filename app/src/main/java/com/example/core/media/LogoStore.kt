package com.example.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Production logo import pipeline.
 *
 * Features:
 * - Off-main-thread decode and compress
 * - BitmapFactory inSampleSize downsampling to [MAX_DIM] (1024px)
 * - Atomic file writes (tmp → rename)
 * - Old file cleanup (no orphaned logo files)
 * - Returns internal path (never content:// URIs that lose permission on reboot)
 *
 * Usage:
 * ```
 * val result = logoStore.importLogo(pickedUri, replacePath = oldLogoPath)
 * result.onSuccess { newPath -> /* update firm.logoPath */ }
 * result.onFailure { error -> /* show snackbar */ }
 * ```
 */
class LogoStore(private val context: Context) {

    private val logoDir: File get() = File(context.filesDir, "logos").apply { mkdirs() }

    /**
     * Imports a logo from a picked content URI.
     *
     * @param source The content:// URI from the image picker
     * @param replacePath Optional path to the previous logo file to delete
     * @return Result containing the new internal file path
     */
    suspend fun importLogo(source: Uri, replacePath: String? = null): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val inputStream = context.contentResolver.openInputStream(source)
                    ?: error("Cannot open picked image")

                // Step 1: Decode bounds only to compute inSampleSize
                val bytes = inputStream.use { it.readBytes() }
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

                var sample = 1
                while (bounds.outWidth / sample > MAX_DIM || bounds.outHeight / sample > MAX_DIM) {
                    sample *= 2
                }

                // Step 2: Decode with computed inSampleSize
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                    ?: error("Failed to decode image")

                // Step 3: Write to temp file, then atomic rename
                val timestamp = System.currentTimeMillis()
                val tmp = File(logoDir, "logo_${timestamp}.tmp")
                FileOutputStream(tmp).use { out ->
                    decoded.compress(Bitmap.CompressFormat.PNG, 90, out)
                }

                val finalFile = File(logoDir, "logo_${timestamp}.png")
                if (!tmp.renameTo(finalFile)) {
                    tmp.delete()
                    error("Atomic rename failed")
                }

                // Step 4: Cleanup old file
                replacePath?.let { oldPath ->
                    val oldFile = File(oldPath)
                    if (oldFile.exists() && oldFile.absolutePath != finalFile.absolutePath) {
                        oldFile.delete()
                    }
                }

                decoded.recycle()
                finalFile.absolutePath
            }
        }

    /**
     * Deletes a logo file by path.
     */
    suspend fun deleteLogo(path: String) = withContext(Dispatchers.IO) {
        File(path).takeIf { it.exists() }?.delete()
    }

    companion object {
        /** Maximum dimension for stored logos (width or height) */
        private const val MAX_DIM = 1024
    }
}
