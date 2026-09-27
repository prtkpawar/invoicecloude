package com.example.data.model

data class DocWithDetails(
    val doc: Doc,
    val customer: Party?,
    val items: List<DocItem> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val totalPaid: Double = 0.0,
    val parentEstimateNo: String? = null
) {
    val party: Party? get() = customer
    val pending: Double get() = doc.computePending(totalPaid)
    val computedStatus: String get() = doc.computeStatus(totalPaid)
    val customerName: String get() = customer?.name ?: ""
    val customerMobile: String get() = customer?.phone ?: customer?.mobile ?: ""
    val customerAddress: String get() = customer?.address ?: ""
    val customerVillage: String get() = customer?.village ?: ""
    val partyName: String get() = customer?.name ?: ""
    val partyPhone: String get() = customer?.phone ?: customer?.mobile ?: ""
}
