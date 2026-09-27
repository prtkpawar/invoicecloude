package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.db.AppDatabase
import com.example.data.model.AppSetting
import com.example.data.model.Company
import com.example.data.model.Customer
import com.example.data.model.Party
import com.example.data.model.PartyCategory
import com.example.data.model.Doc
import com.example.data.model.DocItem
import com.example.data.model.ItemMaster
import com.example.data.model.Payment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupSnapshot(
    val file: File,
    val fileName: String,
    val sizeStr: String,
    val timestamp: Long,
    val isAuto: Boolean,
    val dateFormatted: String,
    val summary: String = ""
)

object BackupHelper {

    suspend fun generateBackupJson(database: AppDatabase): JSONObject = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Swami Solar Billing")
        root.put("version", 2)
        root.put("exported_at", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

        // 1. Company
        val company = database.companyDao().getCompany() ?: Company()
        val compJson = JSONObject().apply {
            put("name", company.name)
            put("address", company.address)
            put("email", company.email)
            put("gstin", company.gstin)
            put("placeOfSupply", company.placeOfSupply)
            put("bankName", company.bankName)
            put("accountName", company.accountName)
            put("accountNumber", company.accountNumber)
            put("ifsc", company.ifsc)
            put("signatoryName", company.signatoryName)
        }
        root.put("company", compJson)

        // 2. Customers / Parties
        val customers = database.partyDao().getAllPartiesFlow().first()
        val custArray = JSONArray()
        for (c in customers) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("businessId", c.businessId)
                put("name", c.name)
                put("phone", c.phone)
                put("mobile", c.phone)
                put("email", c.email)
                put("address", c.address)
                put("village", c.village)
                put("city", c.city)
                put("state", c.state)
                put("pincode", c.pincode)
                put("gstin", c.gstin)
                put("consumerNumber", c.consumerNumber)
                put("sanctionLoad", c.sanctionLoad)
                put("category", c.category.name)
                put("photoPath", c.photoPath ?: "")
                put("notes", c.notes)
                put("createdAt", c.createdAt)
            }
            custArray.put(obj)
        }
        root.put("customers", custArray)
        root.put("parties", custArray)

        // 3. Documents
        val docs = database.docDao().getAllDocsFlow().first()
        val docArray = JSONArray()
        for (d in docs) {
            val obj = JSONObject().apply {
                put("id", d.id)
                put("docType", d.docType)
                put("docNo", d.docNo)
                put("docDate", d.docDate)
                put("validTill", d.validTill)
                put("dueDate", d.dueDate)
                put("customerId", d.customerId)
                put("kw", d.kw)
                put("ratePerKw", d.ratePerKw)
                put("pricingMode", d.pricingMode)
                put("gstMode", d.gstMode)
                put("gstPercent", d.gstPercent)
                put("discount", d.discount)
                put("taxable", d.taxable)
                put("cgst", d.cgst)
                put("sgst", d.sgst)
                put("total", d.total)
                put("amountWords", d.amountWords)
                put("note", d.note)
                put("terms", d.terms)
                put("status", d.status)
                put("parentEstimateId", d.parentEstimateId ?: JSONObject.NULL)
                put("createdAt", d.createdAt)
                put("updatedAt", d.updatedAt)
            }
            docArray.put(obj)
        }
        root.put("documents", docArray)

        // 4. Document items
        val itemArray = JSONArray()
        for (d in docs) {
            val items = database.docItemDao().getItemsForDoc(d.id)
            for (it in items) {
                val obj = JSONObject().apply {
                    put("docId", it.docId)
                    put("label", it.label)
                    put("description", it.description)
                    put("hsn", it.hsn)
                    put("qty", it.qty)
                    put("unit", it.unit)
                    put("rate", it.rate)
                    put("amount", it.amount)
                    put("sortOrder", it.sortOrder)
                }
                itemArray.put(obj)
            }
        }
        root.put("doc_items", itemArray)

        // 5. Payments
        val payments = database.paymentDao().getAllPaymentsFlow().first()
        val payArray = JSONArray()
        for (p in payments) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("docId", p.docId)
                put("payDate", p.payDate)
                put("amount", p.amount)
                put("mode", p.mode)
                put("reference", p.reference)
                put("note", p.note)
                put("createdAt", p.createdAt)
            }
            payArray.put(obj)
        }
        root.put("payments", payArray)

        // 6. Settings
        val settings = database.settingDao().getAllSettingsFlow().first()
        val settArray = JSONArray()
        for (s in settings) {
            val obj = JSONObject().apply {
                put("key", s.key)
                put("value", s.value)
            }
            settArray.put(obj)
        }
        root.put("settings", settArray)

        // 7. Master Items
        val masterItems = database.itemMasterDao().getActiveItems()
        val masterArray = JSONArray()
        for (m in masterItems) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("businessId", m.businessId)
                put("label", m.label)
                put("description", m.description)
                put("hsn", m.hsn)
                put("category", m.category)
                put("unit", m.unit)
                put("taxRate", m.taxRate)
                put("rate", m.rate)
                put("purchasePrice", m.purchasePrice ?: JSONObject.NULL)
                put("isActive", m.isActive)
                put("sortOrder", m.sortOrder)
                put("businessType", m.businessType)
                put("mrp", m.mrp ?: JSONObject.NULL)
                put("barcode", m.barcode ?: JSONObject.NULL)
                put("batchNo", m.batchNo ?: JSONObject.NULL)
                put("expiry", m.expiry ?: JSONObject.NULL)
                put("warranty", m.warranty ?: JSONObject.NULL)
                put("brand", m.brand ?: JSONObject.NULL)
                put("extraAttributes", m.extraAttributes ?: JSONObject.NULL)
            }
            masterArray.put(obj)
        }
        root.put("item_masters", masterArray)

        root
    }

    suspend fun createBackup(context: Context, database: AppDatabase): File = withContext(Dispatchers.IO) {
        val root = generateBackupJson(database)
        val backupDir = File(context.filesDir, "Backups/Manual").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val backupFile = File(backupDir, "SwamiSolar_Backup_$stamp.json")

        FileOutputStream(backupFile).use { out ->
            out.write(root.toString(2).toByteArray())
        }

        val nowStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date())
        database.settingDao().setSetting(AppSetting("last_backup_at", nowStr))

        backupFile
    }

    suspend fun triggerAutoBackup(context: Context, database: AppDatabase): File? = withContext(Dispatchers.IO) {
        try {
            val autoEnabled = database.settingDao().getSetting("auto_backup_enabled") ?: "true"
            if (autoEnabled == "false") return@withContext null

            val root = generateBackupJson(database)
            val autoDir = File(context.filesDir, "Backups/Auto").apply { mkdirs() }
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val autoFile = File(autoDir, "SwamiSolar_Auto_$stamp.json")

            FileOutputStream(autoFile).use { out ->
                out.write(root.toString(2).toByteArray())
            }

            // Prune older auto snapshots to keep maximum 15 snapshots
            val files = autoDir.listFiles()?.filter { it.isFile && it.name.endsWith(".json") }
                ?.sortedByDescending { it.lastModified() } ?: emptyList()
            if (files.size > 15) {
                files.drop(15).forEach { it.delete() }
            }

            val docCount = root.optJSONArray("documents")?.length() ?: 0
            val custCount = root.optJSONArray("customers")?.length() ?: 0
            val payCount = root.optJSONArray("payments")?.length() ?: 0
            val nowStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date())
            val summary = "$docCount Docs, $custCount Customers, $payCount Receipts"

            database.settingDao().setSetting(AppSetting("last_auto_backup_at", nowStr))
            database.settingDao().setSetting(AppSetting("last_auto_backup_summary", summary))

            autoFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun listSnapshots(context: Context): List<BackupSnapshot> {
        val list = mutableListOf<BackupSnapshot>()
        val dateFmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)

        fun scanDir(dir: File, isAuto: Boolean) {
            if (!dir.exists()) return
            dir.listFiles()?.filter { it.isFile && it.name.endsWith(".json") }?.forEach { f ->
                val sizeKb = (f.length() / 1024f)
                val sizeStr = if (sizeKb >= 1024) String.format(Locale.US, "%.1f MB", sizeKb / 1024f) else String.format(Locale.US, "%.1f KB", sizeKb)
                val dateStr = dateFmt.format(Date(f.lastModified()))
                list.add(
                    BackupSnapshot(
                        file = f,
                        fileName = f.name,
                        sizeStr = sizeStr,
                        timestamp = f.lastModified(),
                        isAuto = isAuto,
                        dateFormatted = dateStr
                    )
                )
            }
        }

        scanDir(File(context.filesDir, "Backups/Auto"), true)
        scanDir(File(context.filesDir, "Backups/Manual"), false)
        scanDir(File(context.filesDir, "Backups"), false)

        return list.sortedByDescending { it.timestamp }
    }

    fun deleteSnapshot(file: File): Boolean {
        return try {
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun restoreBackup(database: AppDatabase, jsonString: String): String = withContext(Dispatchers.IO) {
        val root = JSONObject(jsonString)
        val appTag = root.optString("app", "")
        if (appTag.isBlank() && !root.has("company") && !root.has("documents")) {
            return@withContext "Invalid backup file format"
        }

        // 1. Restore Company if present
        if (root.has("company")) {
            val cObj = root.getJSONObject("company")
            database.companyDao().insert(
                Company(
                    name = cObj.optString("name", "swami construction"),
                    address = cObj.optString("address", ""),
                    email = cObj.optString("email", ""),
                    gstin = cObj.optString("gstin", ""),
                    placeOfSupply = cObj.optString("placeOfSupply", ""),
                    bankName = cObj.optString("bankName", ""),
                    accountName = cObj.optString("accountName", ""),
                    accountNumber = cObj.optString("accountNumber", ""),
                    ifsc = cObj.optString("ifsc", ""),
                    signatoryName = cObj.optString("signatoryName", "")
                )
            )
        }

        // 2. Customers / Parties
        var custCount = 0
        val custArr = if (root.has("parties")) root.getJSONArray("parties") else if (root.has("customers")) root.getJSONArray("customers") else null
        if (custArr != null) {
            for (i in 0 until custArr.length()) {
                val o = custArr.getJSONObject(i)
                val phoneVal = if (o.has("phone")) o.optString("phone", "") else o.optString("mobile", "")
                val categoryVal = try {
                    com.example.data.model.PartyCategory.valueOf(o.optString("category", "CUSTOMER"))
                } catch (_: Exception) {
                    com.example.data.model.PartyCategory.CUSTOMER
                }
                database.partyDao().insert(
                    Party(
                        id = o.optInt("id", 0),
                        businessId = o.optLong("businessId", 1L),
                        name = o.optString("name", ""),
                        phone = phoneVal,
                        email = o.optString("email", ""),
                        address = o.optString("address", ""),
                        village = o.optString("village", ""),
                        city = o.optString("city", ""),
                        state = o.optString("state", "Maharashtra"),
                        pincode = o.optString("pincode", ""),
                        gstin = o.optString("gstin", ""),
                        consumerNumber = o.optString("consumerNumber", ""),
                        sanctionLoad = o.optString("sanctionLoad", ""),
                        notes = o.optString("notes", ""),
                        category = categoryVal,
                        photoPath = if (o.optString("photoPath", "").isBlank()) null else o.optString("photoPath"),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
                custCount++
            }
        }

        // 3. Documents
        var docCount = 0
        if (root.has("documents")) {
            val arr = root.getJSONArray("documents")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val parentEstId = if (o.isNull("parentEstimateId")) null else o.optInt("parentEstimateId")
                database.docDao().insert(
                    Doc(
                        id = o.optInt("id", 0),
                        docType = o.optString("docType", "ESTIMATE"),
                        docNo = o.optString("docNo", ""),
                        docDate = o.optString("docDate", ""),
                        validTill = o.optString("validTill", ""),
                        dueDate = o.optString("dueDate", ""),
                        customerId = o.optInt("customerId", 0),
                        kw = o.optDouble("kw", 0.0),
                        ratePerKw = o.optDouble("ratePerKw", 0.0),
                        pricingMode = o.optString("pricingMode", "ROUND_TOTAL"),
                        gstMode = o.optString("gstMode", "GST_5"),
                        gstPercent = o.optDouble("gstPercent", 5.0),
                        discount = o.optDouble("discount", 0.0),
                        taxable = o.optDouble("taxable", 0.0),
                        cgst = o.optDouble("cgst", 0.0),
                        sgst = o.optDouble("sgst", 0.0),
                        total = o.optDouble("total", 0.0),
                        amountWords = o.optString("amountWords", ""),
                        note = o.optString("note", ""),
                        terms = o.optString("terms", ""),
                        status = o.optString("status", "ESTIMATE"),
                        parentEstimateId = parentEstId,
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
                docCount++
            }
        }

        // 4. Doc Items
        if (root.has("doc_items")) {
            val arr = root.getJSONArray("doc_items")
            val itemsList = mutableListOf<DocItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                itemsList.add(
                    DocItem(
                        docId = o.optInt("docId", 0),
                        label = o.optString("label", ""),
                        description = o.optString("description", ""),
                        hsn = o.optString("hsn", "85414300"),
                        qty = o.optDouble("qty", 1.0),
                        unit = o.optString("unit", "set"),
                        rate = o.optDouble("rate", 0.0),
                        amount = o.optDouble("amount", 0.0),
                        sortOrder = o.optInt("sortOrder", i)
                    )
                )
            }
            database.docItemDao().insertAll(itemsList)
        }

        // 5. Payments
        var payCount = 0
        if (root.has("payments")) {
            val arr = root.getJSONArray("payments")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                database.paymentDao().insert(
                    Payment(
                        id = o.optInt("id", 0),
                        docId = o.optInt("docId", 0),
                        payDate = o.optString("payDate", ""),
                        amount = o.optDouble("amount", 0.0),
                        mode = o.optString("mode", "CASH"),
                        reference = o.optString("reference", ""),
                        note = o.optString("note", ""),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
                payCount++
            }
        }

        // 6. Master Items
        if (root.has("item_masters")) {
            val arr = root.getJSONArray("item_masters")
            val mList = mutableListOf<ItemMaster>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val pPrice = if (o.isNull("purchasePrice")) null else o.optDouble("purchasePrice")
                val mrpVal = if (o.isNull("mrp")) null else o.optDouble("mrp")
                mList.add(
                    ItemMaster(
                        id = o.optInt("id", 0),
                        businessId = o.optLong("businessId", 1L),
                        label = o.optString("label", ""),
                        description = o.optString("description", ""),
                        hsn = o.optString("hsn", "85414300"),
                        category = o.optString("category", com.example.data.model.ItemCategory.SOLAR_PANEL),
                        unit = o.optString("unit", com.example.data.model.ItemUnit.NOS),
                        taxRate = o.optDouble("taxRate", 18.0),
                        rate = o.optDouble("rate", 0.0),
                        purchasePrice = pPrice,
                        isActive = o.optBoolean("isActive", true),
                        sortOrder = o.optInt("sortOrder", i),
                        businessType = o.optString("businessType", ""),
                        mrp = mrpVal,
                        barcode = if (o.isNull("barcode")) null else o.optString("barcode"),
                        batchNo = if (o.isNull("batchNo")) null else o.optString("batchNo"),
                        expiry = if (o.isNull("expiry")) null else o.optString("expiry"),
                        warranty = if (o.isNull("warranty")) null else o.optString("warranty"),
                        brand = if (o.isNull("brand")) null else o.optString("brand"),
                        extraAttributes = if (o.isNull("extraAttributes")) null else o.optString("extraAttributes")
                    )
                )
            }
            database.itemMasterDao().insertAll(mList)
        }

        "Restored successfully: $docCount documents, $custCount customers, $payCount payments."
    }

    suspend fun restoreFromFile(database: AppDatabase, file: File): String = withContext(Dispatchers.IO) {
        try {
            val jsonString = file.readText()
            restoreBackup(database, jsonString)
        } catch (e: Exception) {
            "Failed to restore from file: ${e.localizedMessage}"
        }
    }

    fun shareBackup(context: Context, backupFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, backupFile)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Swami Solar Billing backup - ${backupFile.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Backup File"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun exportLedgerToJson(context: Context, database: AppDatabase, businessId: Long = 1L): File = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val profile = database.businessProfileDao().getByIdDirect(businessId)
        val comp = database.companyDao().getCompany() ?: Company()
        val firmName = profile?.brandName?.ifBlank { profile.legalName } ?: comp.name

        root.put("app", "Enterprise Business Billing OS")
        root.put("export_type", "FIRM_SPECIFIC_LEDGER_OFFLINE_SNAPSHOT")
        root.put("exported_at", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
        root.put("business_id", businessId)

        // Firm Profile
        val firmJson = JSONObject().apply {
            put("id", businessId)
            put("brandName", firmName)
            put("legalName", profile?.legalName ?: firmName)
            put("gstin", profile?.gstin ?: comp.gstin)
            put("address", profile?.address ?: comp.address)
            put("city", profile?.city ?: "")
            put("state", profile?.state ?: comp.placeOfSupply)
            put("pincode", profile?.pincode ?: "")
            put("signatoryName", profile?.signatoryName ?: comp.signatoryName)
            put("upiVpa", profile?.upiVpa ?: "")
            put("bankAccountNo", profile?.bankAccountNo ?: comp.accountNumber)
            put("ifsc", profile?.ifsc ?: comp.ifsc)
        }
        root.put("firm_profile", firmJson)

        // Firm Parties / Customers
        val parties = database.partyDao().getAllPartiesFlow(businessId).first()
        val custArray = JSONArray()
        for (c in parties) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("address", c.address)
                put("city", c.city)
                put("state", c.state)
                put("gstin", c.gstin)
                put("category", c.category.name)
            }
            custArray.put(obj)
        }
        root.put("parties", custArray)

        // Firm Documents
        val docs = database.docDao().getAllDocsFlow(businessId).first()
        val docArray = JSONArray()
        val docIds = docs.map { it.id }.toSet()
        for (d in docs) {
            val obj = JSONObject().apply {
                put("id", d.id)
                put("docType", d.docType)
                put("docNo", d.docNo)
                put("docDate", d.docDate)
                put("dueDate", d.dueDate)
                put("customerId", d.customerId)
                put("taxable", d.taxable)
                put("cgst", d.cgst)
                put("sgst", d.sgst)
                put("total", d.total)
                put("note", d.note)
                put("terms", d.terms)
                put("status", d.status)
                put("parentEstimateId", d.parentEstimateId ?: JSONObject.NULL)
            }
            docArray.put(obj)
        }
        root.put("documents", docArray)

        // Firm Document Items
        val itemArray = JSONArray()
        for (d in docs) {
            val items = database.docItemDao().getItemsForDoc(d.id)
            for (it in items) {
                val obj = JSONObject().apply {
                    put("docId", it.docId)
                    put("label", it.label)
                    put("description", it.description)
                    put("hsn", it.hsn)
                    put("qty", it.qty)
                    put("unit", it.unit)
                    put("rate", it.rate)
                    put("amount", it.amount)
                }
                itemArray.put(obj)
            }
        }
        root.put("items", itemArray)

        // Firm Payments
        val allPayments = database.paymentDao().getAllPaymentsFlow(businessId).first()
        val firmPayments = allPayments.filter { it.docId in docIds }
        val payArray = JSONArray()
        for (p in firmPayments) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("docId", p.docId)
                put("payDate", p.payDate)
                put("amount", p.amount)
                put("mode", p.mode)
                put("reference", p.reference)
                put("note", p.note)
            }
            payArray.put(obj)
        }
        root.put("payments", payArray)

        val exportDir = File(context.filesDir, "Exports").apply { mkdirs() }
        val sanitizedFirm = firmName.replace(Regex("[^a-zA-Z0-9]"), "_").take(15)
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val exportFile = File(exportDir, "Ledger_${sanitizedFirm}_$stamp.json")

        FileOutputStream(exportFile).use { out ->
            out.write(root.toString(2).toByteArray())
        }
        exportFile
    }

    suspend fun exportLedgerToCsv(context: Context, database: AppDatabase, businessId: Long = 1L): File = withContext(Dispatchers.IO) {
        val profile = database.businessProfileDao().getByIdDirect(businessId)
        val comp = database.companyDao().getCompany() ?: Company()
        val firmName = profile?.brandName?.ifBlank { profile.legalName } ?: comp.name

        val docs = database.docDao().getAllDocsFlow(businessId).first()
        val parties = database.partyDao().getAllPartiesFlow(businessId).first().associateBy { it.id }
        val docIds = docs.map { it.id }.toSet()
        val allPayments = database.paymentDao().getAllPaymentsFlow(businessId).first()
        val payments = allPayments.filter { it.docId in docIds }

        val csvBuilder = StringBuilder()
        csvBuilder.append("\"# FIRM LEDGER EXPORT: ${firmName.replace("\"", "")}\"\n")
        csvBuilder.append("\"# GSTIN: ${profile?.gstin ?: comp.gstin} | City: ${profile?.city ?: ""} | State: ${profile?.state ?: comp.placeOfSupply}\"\n")
        csvBuilder.append("\"Date\",\"Transaction Type\",\"Doc / Ref No\",\"Party Name\",\"Phone\",\"Description\",\"Debit Amount (Billed ₹)\",\"Credit Amount (Received ₹)\",\"Status\"\n")

        val escapeCsv = { text: String ->
            "\"${text.replace("\"", "\"\"")}\""
        }

        for (d in docs) {
            val party = parties[d.customerId]
            val partyName = party?.name ?: "Unknown"
            val partyPhone = party?.phone ?: ""
            val desc = if (d.isInvoice) "Tax Invoice" else "Quotation / Estimate"

            csvBuilder.append(
                "${escapeCsv(d.docDate)}," +
                "${escapeCsv(if (d.isInvoice) "INVOICE" else "ESTIMATE")}," +
                "${escapeCsv(d.docNo)}," +
                "${escapeCsv(partyName)}," +
                "${escapeCsv(partyPhone)}," +
                "${escapeCsv(desc)}," +
                "\"${d.total}\"," +
                "\"0.00\"," +
                "${escapeCsv(d.status)}\n"
            )
        }

        for (p in payments) {
            val doc = docs.firstOrNull { it.id == p.docId }
            val party = doc?.let { parties[it.customerId] }
            val partyName = party?.name ?: "Customer"
            val partyPhone = party?.phone ?: ""
            val desc = "Payment Received (${p.mode}) Ref: ${p.reference}"

            csvBuilder.append(
                "${escapeCsv(p.payDate)}," +
                "\"PAYMENT_RECEIPT\"," +
                "${escapeCsv(p.reference.ifBlank { "REC-#${p.id}" })}," +
                "${escapeCsv(partyName)}," +
                "${escapeCsv(partyPhone)}," +
                "${escapeCsv(desc)}," +
                "\"0.00\"," +
                "\"${p.amount}\"," +
                "\"PAID\"\n"
            )
        }

        val exportDir = File(context.filesDir, "Exports").apply { mkdirs() }
        val sanitizedFirm = firmName.replace(Regex("[^a-zA-Z0-9]"), "_").take(15)
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val csvFile = File(exportDir, "Ledger_${sanitizedFirm}_$stamp.csv")

        FileOutputStream(csvFile).use { out ->
            out.write(csvBuilder.toString().toByteArray(Charsets.UTF_8))
        }
        csvFile
    }

    suspend fun exportInvoicesToCsv(context: Context, database: AppDatabase, businessId: Long = 1L): File = withContext(Dispatchers.IO) {
        val profile = database.businessProfileDao().getByIdDirect(businessId)
        val comp = database.companyDao().getCompany() ?: Company()
        val firmName = profile?.brandName?.ifBlank { profile.legalName } ?: comp.name

        val docs = database.docDao().getAllDocsFlow(businessId).first().filter { it.isInvoice }
        val parties = database.partyDao().getAllPartiesFlow(businessId).first().associateBy { it.id }

        val csvBuilder = StringBuilder()
        csvBuilder.append("\"# INVOICE REGISTER EXPORT: ${firmName.replace("\"", "")}\"\n")
        csvBuilder.append("\"Invoice No\",\"Invoice Date\",\"Due Date\",\"Customer Name\",\"Phone\",\"Taxable Amount (₹)\",\"CGST (₹)\",\"SGST (₹)\",\"Discount (₹)\",\"Total Amount (₹)\",\"Status\"\n")

        val escapeCsv = { text: String -> "\"${text.replace("\"", "\"\"")}\"" }

        for (d in docs) {
            val party = parties[d.customerId]
            val partyName = party?.name ?: "Customer"
            val partyPhone = party?.phone ?: ""

            csvBuilder.append(
                "${escapeCsv(d.docNo)}," +
                "${escapeCsv(d.docDate)}," +
                "${escapeCsv(d.dueDate)}," +
                "${escapeCsv(partyName)}," +
                "${escapeCsv(partyPhone)}," +
                "\"${d.taxable}\"," +
                "\"${d.cgst}\"," +
                "\"${d.sgst}\"," +
                "\"${d.discount}\"," +
                "\"${d.total}\"," +
                "${escapeCsv(d.status)}\n"
            )
        }

        val exportDir = File(context.filesDir, "Exports").apply { mkdirs() }
        val sanitizedFirm = firmName.replace(Regex("[^a-zA-Z0-9]"), "_").take(15)
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val csvFile = File(exportDir, "Invoices_${sanitizedFirm}_$stamp.csv")

        FileOutputStream(csvFile).use { out ->
            out.write(csvBuilder.toString().toByteArray(Charsets.UTF_8))
        }
        csvFile
    }

    suspend fun exportCustomersToCsv(context: Context, database: AppDatabase, businessId: Long = 1L): File = withContext(Dispatchers.IO) {
        val profile = database.businessProfileDao().getByIdDirect(businessId)
        val comp = database.companyDao().getCompany() ?: Company()
        val firmName = profile?.brandName?.ifBlank { profile.legalName } ?: comp.name

        val parties = database.partyDao().getAllPartiesFlow(businessId).first()

        val csvBuilder = StringBuilder()
        csvBuilder.append("\"# CUSTOMER DIRECTORY EXPORT: ${firmName.replace("\"", "")}\"\n")
        csvBuilder.append("\"Customer ID\",\"Name\",\"Mobile / WhatsApp\",\"Category\",\"GSTIN\",\"Email\",\"Village / Area\",\"Address\"\n")

        val escapeCsv = { text: String -> "\"${text.replace("\"", "\"\"")}\"" }

        for (p in parties) {
            csvBuilder.append(
                "\"CUST-#${p.id.toString().padStart(4, '0')}\"," +
                "${escapeCsv(p.name)}," +
                "${escapeCsv(p.phone)}," +
                "${escapeCsv(p.category.name)}," +
                "${escapeCsv(p.gstin)}," +
                "${escapeCsv(p.email)}," +
                "${escapeCsv(p.village)}," +
                "${escapeCsv(p.address)}\n"
            )
        }

        val exportDir = File(context.filesDir, "Exports").apply { mkdirs() }
        val sanitizedFirm = firmName.replace(Regex("[^a-zA-Z0-9]"), "_").take(15)
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val csvFile = File(exportDir, "Customers_${sanitizedFirm}_$stamp.csv")

        FileOutputStream(csvFile).use { out ->
            out.write(csvBuilder.toString().toByteArray(Charsets.UTF_8))
        }
        csvFile
    }

    fun shareExportFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "$title - ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
