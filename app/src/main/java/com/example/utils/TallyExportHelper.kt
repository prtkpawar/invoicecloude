package com.example.utils

import com.example.data.db.AppDatabase
import com.example.data.model.Doc
import com.example.data.model.DocItem
import com.example.data.model.Party
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Generates Tally-compatible XML for importing Sales Vouchers.
 * Format follows Tally.ERP 9 / TallyPrime XML schema.
 */
object TallyExportHelper {
    suspend fun generateTallyXml(
        database: AppDatabase,
        businessId: Long,
        monthYear: String  // Format: "2026-09" for September 2026 or empty for all
    ): String = withContext(Dispatchers.IO) {
        val allDocs = database.docDao().getAllDocsFlow(businessId).first()
        val invoices = allDocs.filter { 
            it.isInvoice && (monthYear.isBlank() || it.docDate.startsWith(monthYear))
        }
        
        val parties = database.partyDao().getAllPartiesFlow(businessId).first()
        val partyMap = parties.associateBy { it.id }
        
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("""<ENVELOPE>""")
        sb.appendLine("""  <HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER>""")
        sb.appendLine("""  <BODY><IMPORTDATA><REQUESTDESC>""")
        sb.appendLine("""    <REPORTNAME>Vouchers</REPORTNAME>""")
        sb.appendLine("""  </REQUESTDESC><REQUESTDATA>""")
        
        for (doc in invoices) {
            val customer = partyMap[doc.customerId]
            val items = database.docItemDao().getItemsForDoc(doc.id)
            sb.append(buildVoucherXml(doc, customer, items))
        }
        
        sb.appendLine("""  </REQUESTDATA></IMPORTDATA></BODY>""")
        sb.appendLine("""</ENVELOPE>""")
        sb.toString()
    }
    
    private fun buildVoucherXml(doc: Doc, customer: Party?, items: List<DocItem>): String {
        val customerName = (customer?.name ?: "Cash Customer").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        val voucherDate = doc.docDate.replace("-", "").replace("/", "")  // "20260926" format
        
        val sb = StringBuilder()
        sb.appendLine("""    <TALLYMESSAGE xmlns:UDF="TallyUDF">""")
        sb.appendLine("""      <VOUCHER VCHTYPE="Sales" ACTION="Create">""")
        sb.appendLine("""        <DATE>$voucherDate</DATE>""")
        sb.appendLine("""        <VOUCHERTYPENAME>Sales</VOUCHERTYPENAME>""")
        sb.appendLine("""        <VOUCHERNUMBER>${doc.docNo}</VOUCHERNUMBER>""")
        sb.appendLine("""        <PARTYLEDGERNAME>$customerName</PARTYLEDGERNAME>""")
        
        // Debtor entry (customer owes this amount)
        sb.appendLine("""        <ALLLEDGERENTRIES.LIST>""")
        sb.appendLine("""          <LEDGERNAME>$customerName</LEDGERNAME>""")
        sb.appendLine("""          <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>""")
        sb.appendLine("""          <AMOUNT>-${doc.total}</AMOUNT>""")
        sb.appendLine("""        </ALLLEDGERENTRIES.LIST>""")
        
        // Sales account entry
        sb.appendLine("""        <ALLLEDGERENTRIES.LIST>""")
        sb.appendLine("""          <LEDGERNAME>Sales Account</LEDGERNAME>""")
        sb.appendLine("""          <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>""")
        sb.appendLine("""          <AMOUNT>${doc.taxable}</AMOUNT>""")
        sb.appendLine("""        </ALLLEDGERENTRIES.LIST>""")
        
        // CGST entry
        if (doc.cgst > 0) {
            sb.appendLine("""        <ALLLEDGERENTRIES.LIST>""")
            sb.appendLine("""          <LEDGERNAME>CGST</LEDGERNAME>""")
            sb.appendLine("""          <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>""")
            sb.appendLine("""          <AMOUNT>${doc.cgst}</AMOUNT>""")
            sb.appendLine("""        </ALLLEDGERENTRIES.LIST>""")
        }
        
        // SGST entry
        if (doc.sgst > 0) {
            sb.appendLine("""        <ALLLEDGERENTRIES.LIST>""")
            sb.appendLine("""          <LEDGERNAME>SGST</LEDGERNAME>""")
            sb.appendLine("""          <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>""")
            sb.appendLine("""          <AMOUNT>${doc.sgst}</AMOUNT>""")
            sb.appendLine("""        </ALLLEDGERENTRIES.LIST>""")
        }
        
        sb.appendLine("""      </VOUCHER>""")
        sb.appendLine("""    </TALLYMESSAGE>""")
        return sb.toString()
    }
}
