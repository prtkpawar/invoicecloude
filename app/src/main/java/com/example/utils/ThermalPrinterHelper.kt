package com.example.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.example.data.entity.BusinessProfile
import com.example.data.model.DocItem
import com.example.data.model.DocWithDetails
import java.io.OutputStream
import java.util.UUID

/**
 * Bluetooth Thermal Printer Helper using raw ESC/POS byte commands.
 * Supports 58mm (2-inch) and 80mm (3-inch) thermal POS printers.
 */
object ThermalPrinterHelper {

    // Standard SPP (Serial Port Profile) UUID for Bluetooth printers
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private const val PREFS_KEY = "saved_printer_mac"

    /**
     * Get list of already-paired Bluetooth devices.
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
            adapter.bondedDevices?.toList() ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Save the selected printer's MAC address.
     */
    fun savePrinterMac(context: Context, mac: String) {
        context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
            .edit().putString(PREFS_KEY, mac).apply()
    }

    /**
     * Get the saved printer MAC address.
     */
    fun getSavedPrinterMac(context: Context): String? {
        return context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
            .getString(PREFS_KEY, null)
    }

    /**
     * Print a receipt for the given invoice.
     */
    @SuppressLint("MissingPermission")
    fun printReceipt(
        context: Context,
        detail: DocWithDetails,
        items: List<DocItem>,
        firmProfile: BusinessProfile?
    ): Boolean {
        val mac = getSavedPrinterMac(context) ?: return false
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return false
        
        return try {
            val device: BluetoothDevice = adapter.getRemoteDevice(mac)
            var socket: BluetoothSocket? = null
            var outputStream: OutputStream? = null

            try {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()
                outputStream = socket.outputStream

                val receipt = buildReceiptBytes(detail, items, firmProfile)
                outputStream.write(receipt)
                outputStream.flush()
                true
            } finally {
                try { outputStream?.close() } catch (_: Exception) {}
                try { socket?.close() } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Build the full ESC/POS byte array for a thermal receipt.
     */
    private fun buildReceiptBytes(
        detail: DocWithDetails,
        items: List<DocItem>,
        firm: BusinessProfile?
    ): ByteArray {
        val out = mutableListOf<Byte>()

        fun addBytes(vararg bytes: Int) { bytes.forEach { out.add(it.toByte()) } }
        fun addText(text: String) { out.addAll(text.toByteArray(Charsets.UTF_8).toList()) }
        fun newLine() { addBytes(0x0A) }
        fun initialize() { addBytes(0x1B, 0x40) }
        fun boldOn() { addBytes(0x1B, 0x45, 0x01) }
        fun boldOff() { addBytes(0x1B, 0x45, 0x00) }
        fun centerAlign() { addBytes(0x1B, 0x61, 0x01) }
        fun leftAlign() { addBytes(0x1B, 0x61, 0x00) }
        fun cutPaper() { addBytes(0x1D, 0x56, 0x00) }
        fun dashes() { addText("--------------------------------"); newLine() }

        // === BUILD RECEIPT ===
        initialize()

        // Firm Name (centered, bold)
        centerAlign()
        boldOn()
        addText(firm?.brandName?.takeIf { it.isNotBlank() } ?: firm?.legalName ?: "SHOP")
        newLine()
        boldOff()
        if (!firm?.address.isNullOrBlank()) {
            addText(firm.address.take(32))
            newLine()
        }
        if (!firm?.gstin.isNullOrBlank()) {
            addText("GSTIN: ${firm.gstin}")
            newLine()
        }
        dashes()

        // Invoice details
        leftAlign()
        addText("Invoice: ${detail.doc.docNo}")
        newLine()
        addText("Date: ${detail.doc.docDate}")
        newLine()
        addText("Customer: ${detail.customerName.take(28)}")
        newLine()
        dashes()

        // Column headers
        boldOn()
        addText(String.format(java.util.Locale.US, "%-18s %4s %8s", "Item", "Qty", "Amount"))
        newLine()
        boldOff()
        dashes()

        // Line items
        for (item in items) {
            val name = item.description.take(18)
            val qty = if (item.qty % 1.0 == 0.0) item.qty.toInt().toString() else item.qty.toString()
            val amt = Formatters.plain(item.amount)
            addText(String.format(java.util.Locale.US, "%-18s %4s %8s", name, qty, amt))
            newLine()
        }

        dashes()

        // Totals
        boldOn()
        addText(String.format(java.util.Locale.US, "%24s %8s", "TOTAL:", Formatters.plain(detail.doc.total)))
        newLine()
        if (detail.totalPaid > 0) {
            boldOff()
            addText(String.format(java.util.Locale.US, "%24s %8s", "Paid:", Formatters.plain(detail.totalPaid)))
            newLine()
            boldOn()
            addText(String.format(java.util.Locale.US, "%24s %8s", "PENDING:", Formatters.plain(detail.pending)))
            newLine()
        }
        boldOff()

        dashes()
        centerAlign()
        addText("Thank You!")
        newLine()
        newLine()
        newLine()  // Feed paper before cut
        cutPaper()

        return out.toByteArray()
    }
}
