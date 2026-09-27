package com.example.data.model

enum class LedgerTransactionType {
    OPENING_BALANCE,
    INVOICE,
    PAYMENT_RECEIVED,
    ESTIMATE
}

data class LedgerEntry(
    val id: String,
    val date: String,
    val type: LedgerTransactionType,
    val docType: String? = null,
    val docId: Int? = null,
    val paymentId: Int? = null,
    val voucherNo: String,
    val particulars: String,
    val paymentMode: String? = null,
    val debit: Double, // Invoiced to party (+)
    val credit: Double, // Payment received from party (-)
    val runningBalance: Double,
    val balanceType: String // "Dr" or "Cr"
) {
    val isDebit: Boolean get() = debit > 0.001
    val isCredit: Boolean get() = credit > 0.001
}

data class PartyStatementData(
    val party: Party,
    val openingBalance: Double = 0.0,
    val totalDebit: Double = 0.0,
    val totalCredit: Double = 0.0,
    val netBalance: Double = 0.0,
    val entries: List<LedgerEntry> = emptyList(),
    val invoicesCount: Int = 0,
    val paymentsCount: Int = 0,
    val fromDate: String? = null,
    val toDate: String? = null
) {
    val isReceivable: Boolean get() = netBalance > 0.01
    val isPayable: Boolean get() = netBalance < -0.01
    val isSettled: Boolean get() = !isReceivable && !isPayable
}
