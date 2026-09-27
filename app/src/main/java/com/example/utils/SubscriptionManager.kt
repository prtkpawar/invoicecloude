package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val priceInInr: Int,
    val durationText: String,
    val billingCycle: String,
    val firmLimit: Int, // 1 for single firm, 999 for unlimited
    val badge: String? = null,
    val features: List<String>,
    val playStoreProductId: String
)

data class SubscriptionStatus(
    val isPro: Boolean,
    val planId: String,
    val planTitle: String,
    val expiresAtTimestamp: Long,
    val firmSlots: Int,
    val isLifetime: Boolean = false,
    val licenseKey: String? = null
) {
    val isExpired: Boolean get() = !isLifetime && System.currentTimeMillis() > expiresAtTimestamp
    val isValid: Boolean get() = isPro && (!isExpired || isLifetime)
    
    val daysRemaining: Int
        get() {
            if (isLifetime) return 9999
            val diff = expiresAtTimestamp - System.currentTimeMillis()
            return if (diff > 0) (diff / (1000 * 60 * 60 * 24)).toInt() else 0
        }

    val formattedExpiry: String
        get() {
            if (isLifetime) return "Lifetime Access (No Expiry)"
            if (expiresAtTimestamp <= 0) return "Trial Expired"
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(expiresAtTimestamp))
        }
}

class SubscriptionManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("bill_book_subscription_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_UPI_VPA = "pratik989095@okaxis" // Merchant UPI VPA for direct QR/UPI payments
        const val MERCHANT_NAME = "BillBook"

        val PLANS = listOf(
            SubscriptionPlan(
                id = "sub_monthly_100",
                title = "1-Firm Monthly",
                priceInInr = 100,
                durationText = "per month",
                billingCycle = "Billed Monthly",
                firmLimit = 1,
                badge = "POPULAR",
                features = listOf(
                    "1 Active Firm Profile & Letterhead",
                    "Unlimited GST Invoices & Estimates",
                    "Custom Logo & Digital Signature",
                    "Instant WhatsApp PDF Sharing",
                    "Multi-Industry Smart BOQ & Batch Presets",
                    "Local Auto & Manual Backups"
                ),
                playStoreProductId = "billbook_sub_monthly_100"
            ),
            SubscriptionPlan(
                id = "sub_yearly_800",
                title = "1-Firm Annual (Save 33%)",
                priceInInr = 800,
                durationText = "per year",
                billingCycle = "Billed Annually (Just ₹66/mo)",
                firmLimit = 1,
                badge = "BEST VALUE",
                features = listOf(
                    "All Monthly Plan Features Included",
                    "Save ₹400 every year (33% Discount)",
                    "Priority Professional PDF Themes",
                    "Live Dynamic UPI QR on Invoices",
                    "Payment Ledger & Account Statement",
                    "Priority Support via WhatsApp"
                ),
                playStoreProductId = "billbook_sub_yearly_800"
            ),
            SubscriptionPlan(
                id = "sub_multi_yearly_1499",
                title = "Multi-Firm Annual Pro",
                priceInInr = 1499,
                durationText = "per year",
                billingCycle = "Billed Annually",
                firmLimit = 999,
                badge = "MULTI-BUSINESS",
                features = listOf(
                    "Unlimited Firm Profiles & Switcher",
                    "Independent Letterheads & Bank Accounts",
                    "Manage Solar, Kirana, Pharmacy & Civil under 1 App",
                    "Complete Multi-Firm Ledger & GST Filing Exports",
                    "Instant 1-Tap Firm Assignment on Invoices",
                    "VIP Priority Features & Early Access"
                ),
                playStoreProductId = "billbook_sub_multi_1499"
            ),
            SubscriptionPlan(
                id = "plan_lifetime_2499",
                title = "Lifetime One-Time License",
                priceInInr = 2499,
                durationText = "one-time",
                billingCycle = "Pay Once, Use Forever",
                firmLimit = 999,
                badge = "LIFETIME DEAL",
                features = listOf(
                    "One-Time Payment — Zero Recurring Fees",
                    "Unlimited Firms, Invoices & Statements",
                    "Lifetime Free Updates & Form Upgrades",
                    "Offline-First — No Server Dependency",
                    "Direct Master Access & License Certificate"
                ),
                playStoreProductId = "billbook_lifetime_2499"
            )
        )
    }

    private val _subscriptionState = MutableStateFlow(loadSubscription())
    val subscriptionState: StateFlow<SubscriptionStatus> = _subscriptionState.asStateFlow()

    fun loadSubscription(): SubscriptionStatus {
        val isPro = prefs.getBoolean("is_pro", true) // Default true for Developer Testing Mode
        val planId = prefs.getString("plan_id", "dev_testing_mode") ?: "dev_testing_mode"
        val planTitle = prefs.getString("plan_title", "Developer Testing Mode (All Features Unlocked)") ?: "Developer Testing Mode (All Features Unlocked)"
        val expiresAt = prefs.getLong("expires_at", System.currentTimeMillis() + 3650L * 24 * 60 * 60 * 1000L)
        val firmSlots = prefs.getInt("firm_slots", 999)
        val isLifetime = prefs.getBoolean("is_lifetime", true)
        val licenseKey = prefs.getString("license_key", "DEV-TEST-MODE-8888")

        return SubscriptionStatus(
            isPro = isPro,
            planId = planId,
            planTitle = planTitle,
            expiresAtTimestamp = expiresAt,
            firmSlots = firmSlots,
            isLifetime = isLifetime,
            licenseKey = licenseKey
        )
    }

    fun activatePlan(plan: SubscriptionPlan, utrOrTransactionId: String? = null) {
        val now = System.currentTimeMillis()
        val durationMillis = when (plan.id) {
            "sub_monthly_100" -> 30L * 24 * 60 * 60 * 1000L
            "sub_yearly_800", "sub_multi_yearly_1499" -> 365L * 24 * 60 * 60 * 1000L
            else -> 3650L * 24 * 60 * 60 * 1000L // 10 years for lifetime
        }
        val isLifetime = plan.id == "plan_lifetime_2499"
        val expiry = now + durationMillis

        prefs.edit()
            .putBoolean("is_pro", true)
            .putString("plan_id", plan.id)
            .putString("plan_title", plan.title)
            .putLong("expires_at", expiry)
            .putInt("firm_slots", plan.firmLimit)
            .putBoolean("is_lifetime", isLifetime)
            .putString("license_key", utrOrTransactionId ?: "TXN-${System.currentTimeMillis().toString().takeLast(6)}")
            .apply()

        _subscriptionState.value = loadSubscription()
    }

    fun activateViaLicenseKey(code: String): Boolean {
        val clean = code.trim().uppercase()
        val now = System.currentTimeMillis()

        return when {
            clean.contains("LIFETIME") || clean.startsWith("LIFE-") -> {
                activatePlan(PLANS.first { it.id == "plan_lifetime_2499" }, clean)
                true
            }
            clean.contains("MULTI") || clean.startsWith("PRO-MULTI") -> {
                activatePlan(PLANS.first { it.id == "sub_multi_yearly_1499" }, clean)
                true
            }
            clean.contains("YEAR") || clean.startsWith("PRO-800") || clean.startsWith("BILLBOOK-800") -> {
                activatePlan(PLANS.first { it.id == "sub_yearly_800" }, clean)
                true
            }
            clean.contains("MONTH") || clean.startsWith("PRO-100") || clean.startsWith("BILLBOOK-100") -> {
                activatePlan(PLANS.first { it.id == "sub_monthly_100" }, clean)
                true
            }
            clean.length >= 8 -> { // General valid custom license key
                activatePlan(PLANS.first { it.id == "sub_yearly_800" }, clean)
                true
            }
            else -> false
        }
    }

    fun buildUpiIntentUri(plan: SubscriptionPlan, upiVpa: String = DEFAULT_UPI_VPA): Uri {
        val note = "Subscription for ${plan.title} on BillBook"
        val uriStr = "upi://pay?pa=$upiVpa&pn=${Uri.encode(MERCHANT_NAME)}&am=${plan.priceInInr}&cu=INR&tn=${Uri.encode(note)}"
        return Uri.parse(uriStr)
    }

    fun launchUpiPayment(context: Context, plan: SubscriptionPlan, upiVpa: String = DEFAULT_UPI_VPA): Boolean {
        return try {
            val uri = buildUpiIntentUri(plan, upiVpa)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
