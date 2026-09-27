package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SwiggyDark
import com.example.ui.theme.SwiggyGray
import com.example.ui.theme.SwiggyGreen
import com.example.ui.theme.SwiggyOrange
import com.example.utils.SubscriptionManager
import com.example.utils.SubscriptionPlan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val subManager = remember { SubscriptionManager(context) }
    val subStatus by subManager.subscriptionState.collectAsState()

    var selectedPlan by remember { mutableStateOf(SubscriptionManager.PLANS[1]) } // Default to Yearly 800
    var showBlueprintDialog by remember { mutableStateOf(false) }
    var showUpiCheckoutDialog by remember { mutableStateOf(false) }
    var showLicenseKeyDialog by remember { mutableStateOf(false) }

    var utrInput by remember { mutableStateOf("") }
    var licenseKeyInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Plans & Subscriptions", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                        Text("BillBook Pro Licensing", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showBlueprintDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Implementation Architecture", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SwiggyDark)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ACTIVE STATUS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SwiggyDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(SwiggyOrange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("CURRENT PLAN", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = SwiggyOrange, letterSpacing = 0.5.sp)
                                Text(subStatus.planTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Surface(
                            color = if (subStatus.isValid) SwiggyGreen.copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (subStatus.isValid) "ACTIVE" else "EXPIRED",
                                color = if (subStatus.isValid) SwiggyGreen else Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Validity Expiry", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            Text(subStatus.formattedExpiry, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Firm Profiles Limit", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            Text(
                                if (subStatus.firmSlots >= 900) "Unlimited Multi-Firm" else "${subStatus.firmSlots} Active Firm",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SwiggyOrange
                            )
                        }
                    }
                }
            }

            // SECTION HEADER
            Text("Choose Your Subscription Tier", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SwiggyDark)

            // PLANS LIST
            SubscriptionManager.PLANS.forEach { plan ->
                val isSelected = selectedPlan.id == plan.id
                val isCurrentPlan = subStatus.planId == plan.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPlan = plan },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFFFF7ED) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) SwiggyOrange else Color(0xFFE2E8F0)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (plan.badge != null) {
                                    Surface(
                                        color = if (plan.badge == "BEST VALUE") SwiggyGreen else SwiggyOrange,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = plan.badge,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                Text(plan.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SwiggyDark)
                                Text(plan.billingCycle, fontSize = 11.5.sp, color = SwiggyGray)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("₹${plan.priceInInr}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = SwiggyDark)
                                    Text(" / ${plan.durationText}", fontSize = 11.sp, color = SwiggyGray, modifier = Modifier.padding(bottom = 3.dp, start = 2.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            plan.features.forEach { feat ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isSelected) SwiggyOrange else SwiggyGreen,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(feat, fontSize = 12.sp, color = Color(0xFF334155))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    selectedPlan = plan
                                    showUpiCheckoutDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) SwiggyOrange else SwiggyDark
                                )
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Instant UPI Pay", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    subManager.launchUpiPayment(context, plan)
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = SwiggyDark, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // REDEEM LICENSE KEY & ARCHITECTURE BLUEPRINT
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = SwiggyOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Have a License Key / Promo Code?", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = SwiggyDark)
                        }

                        TextButton(onClick = { showLicenseKeyDialog = true }) {
                            Text("Redeem", color = SwiggyOrange, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Activate subscriptions purchased via direct bank transfer, partner retailers, or sales coupons.",
                        fontSize = 11.5.sp,
                        color = SwiggyGray
                    )
                }
            }

            // HOW WE IMPLEMENT & MONETIZE ON PRACTICAL BASIS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Monetization & Implementation Guide", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E3A8A))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Detailed practical blueprint: Google Play Billing v7, Razorpay UPI Gateway, automated license token generation, and multi-firm database quotas.",
                        fontSize = 12.sp,
                        color = Color(0xFF1E40AF)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showBlueprintDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("View Complete Architecture Plan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // UPI CHECKOUT DIALOG
    if (showUpiCheckoutDialog) {
        val upiUri = subManager.buildUpiIntentUri(selectedPlan)
        AlertDialog(
            onDismissRequest = { showUpiCheckoutDialog = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = SwiggyOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pay via UPI / QR Code", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SwiggyDark)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(selectedPlan.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SwiggyDark)
                            Text("Amount: ₹${selectedPlan.priceInInr} (${selectedPlan.durationText})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SwiggyOrange)
                        }
                    }

                    Text("Option 1: Pay via Installed UPI Apps (GPay, PhonePe, Paytm)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SwiggyDark)
                    Button(
                        onClick = {
                            subManager.launchUpiPayment(context, selectedPlan)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SwiggyDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open GPay / PhonePe / Paytm", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    Text("Option 2: Direct UPI ID Transfer", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SwiggyDark)
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Merchant UPI ID", fontSize = 10.sp, color = Color(0xFF2563EB))
                                Text(SubscriptionManager.DEFAULT_UPI_VPA, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), fontFamily = FontFamily.Monospace)
                            }
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(SubscriptionManager.DEFAULT_UPI_VPA))
                                Toast.makeText(context, "UPI ID Copied!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = utrInput,
                        onValueChange = { utrInput = it },
                        label = { Text("Enter 12-Digit UTR / Ref No.") },
                        placeholder = { Text("e.g. 426819002341") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (utrInput.trim().isBlank()) {
                            Toast.makeText(context, "Please enter UTR or Transaction Ref Number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        subManager.activatePlan(selectedPlan, utrInput.trim())
                        showUpiCheckoutDialog = false
                        Toast.makeText(context, "🎉 Plan Activated Successfully!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwiggyGreen)
                ) {
                    Text("Confirm & Activate Plan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpiCheckoutDialog = false }) {
                    Text("Cancel", color = SwiggyGray)
                }
            }
        )
    }

    // LICENSE KEY DIALOG
    if (showLicenseKeyDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseKeyDialog = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = SwiggyOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Redeem Activation Key", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SwiggyDark)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter the license key provided with your purchase or partner coupon.", fontSize = 12.sp, color = SwiggyGray)
                    OutlinedTextField(
                        value = licenseKeyInput,
                        onValueChange = { licenseKeyInput = it.uppercase() },
                        label = { Text("Activation Key") },
                        placeholder = { Text("e.g. PRO-800-YEARLY-2026") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (licenseKeyInput.trim().isBlank()) {
                            Toast.makeText(context, "Please enter a license key", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val success = subManager.activateViaLicenseKey(licenseKeyInput.trim())
                        if (success) {
                            showLicenseKeyDialog = false
                            Toast.makeText(context, "🎉 Pro Subscription Activated Successfully!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Invalid key. Please check and try again.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwiggyOrange)
                ) {
                    Text("Activate", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLicenseKeyDialog = false }) {
                    Text("Cancel", color = SwiggyGray)
                }
            }
        )
    }

    // PRACTICAL IMPLEMENTATION ARCHITECTURE BLUEPRINT MODAL
    if (showBlueprintDialog) {
        AlertDialog(
            onDismissRequest = { showBlueprintDialog = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2563EB))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Monetization Plan & Blueprint", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SwiggyDark)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Here is the practical roadmap to monetize 'BillBook' with ₹100/mo and ₹800/yr plans:",
                        fontSize = 12.sp,
                        color = SwiggyDark
                    )

                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("1. Google Play Billing (Play Store)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color(0xFF0F172A))
                            Text("• Add 'com.android.billingclient:billing-ktx:7.0.0'\n• Create Products in Google Play Console:\n   - 'billbook_sub_monthly_100' (Base plan: ₹100/month recurring)\n   - 'billbook_sub_yearly_800' (Base plan: ₹800/year recurring)\n• Automatic renewal & cancellation handled by Google Play.", fontSize = 11.sp, color = Color(0xFF475569))
                        }
                    }

                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("2. Direct Indian UPI / Razorpay Gateway", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color(0xFF0F172A))
                            Text("• Zero Google 15% commission on direct APK/web distribution.\n• Integrate Razorpay Android Standard SDK (`com.razorpay:checkout`).\n• Webhook receives payment.captured event and sends license key via WhatsApp/SMS.", fontSize = 11.sp, color = Color(0xFF475569))
                        }
                    }

                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("3. Multi-Firm Database Quotas", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color(0xFF0F172A))
                            Text("• Single-Firm Plan (₹100/mo or ₹800/yr) allows 1 active BusinessProfile.\n• If user taps '+ Add Firm', prompt upgrade to Multi-Firm Pro (₹1,499/yr).\n• Completely offline-tolerant: local token stores expiry timestamp.", fontSize = 11.sp, color = Color(0xFF475569))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBlueprintDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Got It", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
