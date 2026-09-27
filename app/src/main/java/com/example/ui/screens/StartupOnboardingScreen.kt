package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import com.example.ui.components.GoogleSignInCard
import com.example.utils.AppLanguageManager
import com.example.utils.SUPPORTED_LANGUAGES
import com.example.utils.appString
import com.example.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BusinessProfile
import com.example.ui.theme.BizNavy
import com.example.ui.theme.pro.Brand100
import com.example.ui.theme.pro.Brand600
import com.example.ui.theme.pro.Brand700
import com.example.ui.theme.pro.Canvas
import com.example.ui.theme.pro.Ink400
import com.example.ui.theme.pro.Ink600
import com.example.ui.theme.pro.Ink900
import com.example.ui.theme.pro.Line
import com.example.ui.theme.pro.Spacing
import com.example.ui.theme.pro.Surface
import com.example.ui.theme.pro.SurfaceSoft
import com.example.ui.viewmodel.BillingViewModel

private data class IndustryCardItem(
    val templateId: Long,
    val title: String,
    val emoji: String,
    val highlight: String,
    val sampleBusiness: String,
    val sampleGst: String
)

private val INDUSTRY_PRESETS = listOf(
    IndustryCardItem(3L, "Kirana & Grocery", "🛒", "FMCG, Weights (kg/g/L) & MRP", "Mahalaxmi Kirana Stores", "27CCCCC2222C1Z9"),
    IndustryCardItem(1L, "Solar & Renewable", "☀️", "kW Rooftop, DCR Panels & MNRE", "Swami Solar & EPC Solutions", "27AAAAA0000A1Z5"),
    IndustryCardItem(4L, "Pharmacy & Medical", "💊", "Rx Drugs, Batch No & Expiry", "Sanjivani Medical & General Stores", "27BBBBB1111B1Z2"),
    IndustryCardItem(2L, "Civil Construction", "🏗️", "BOQ Schedules, Brass & Sq.Ft", "Apex Civil Infra & Contractors", "27DDDDD3333D1Z6"),
    IndustryCardItem(11L, "Electronics & Mobile", "📱", "IMEI / Serial Numbers & Warranty", "Digital World Electronics", "27EEEEE4444E1Z3"),
    IndustryCardItem(8L, "Restaurant & Cafe", "🍽️", "Table / Token Billing & KOT", "Royal Treat Family Restaurant", "27FFFFF5555F1Z0"),
    IndustryCardItem(9L, "Hardware & Sanitary", "🔧", "Paints, Pipes, Grades & Tools", "Shree Ganesh Hardware & Paints", "27GGGGG6666G1Z7"),
    IndustryCardItem(5L, "Salon & Wellness", "💇", "Stylists, Durations & Packages", "Aura Luxury Unisex Salon & Spa", "27HHHHH7777H1Z4"),
    IndustryCardItem(10L, "Professional Services", "💼", "Consulting, SAC Codes & Retainers", "NexGen Advisory & IT Solutions", "27JJJJJ8888J1Z1"),
    IndustryCardItem(6L, "General Trading", "🏢", "Multi-Item B2B & B2C Tax Invoices", "Bharat Enterprise & Traders", "27KKKKK9999K1Z8")
)

/**
 * GO-LIVE EDITION — white/black onboarding.
 * Fixes vs old version:
 *  - White background (old dark gradient made text invisible)
 *  - All text black-on-white / white-on-black (nothing can vanish)
 *  - Compact: 4 short steps, tighter grid, slim inputs
 */
@Composable
fun StartupOnboardingScreen(
    viewModel: BillingViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(0) }

    var selectedTemplateId by remember { mutableLongStateOf(3L) }
    val selectedIndustry = remember(selectedTemplateId) {
        INDUSTRY_PRESETS.firstOrNull { it.templateId == selectedTemplateId } ?: INDUSTRY_PRESETS.first()
    }

    var brandName by remember { mutableStateOf("Mahalaxmi Kirana Stores") }
    var legalName by remember { mutableStateOf("Mahalaxmi Super Market") }
    var address by remember { mutableStateOf("Shop No. 4, Market Yard, Station Road") }
    var city by remember { mutableStateOf("Dhule") }
    var state by remember { mutableStateOf("Maharashtra") }
    var pincode by remember { mutableStateOf("424001") }
    var gstin by remember { mutableStateOf("27CCCCC2222C1Z9") }
    var pan by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var signatoryName by remember { mutableStateOf("") }
    var upiVpa by remember { mutableStateOf("") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Ink900,
        unfocusedTextColor = Ink900,
        focusedBorderColor = Brand600,
        unfocusedBorderColor = Line,
        focusedLabelColor = Brand600,
        unfocusedLabelColor = Ink400,
        cursorColor = Brand600,
        focusedContainerColor = Surface,
        unfocusedContainerColor = Surface
    )
    val fieldTextStyle = androidx.compose.ui.text.TextStyle(color = Ink900, fontSize = 14.sp)

    // Light canvas — this alone fixes the "invisible page" problem
    Box(modifier = Modifier.fillMaxSize().background(Canvas).statusBarsPadding().navigationBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg)) {
            // Header: back + step dots + counter
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    IconButton(onClick = { currentStep-- }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink900)
                    }
                } else Spacer(modifier = Modifier.size(36.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..3).forEach { stepIdx ->
                        Box(
                            modifier = Modifier
                                .height(5.dp)
                                .width(if (stepIdx == currentStep) 26.dp else 10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (stepIdx <= currentStep) Brand600 else Color(0xFFE4E4E7)
                                )
                        )
                    }
                }
                Text(
                    text = "${currentStep + 1}/4",
                    color = Ink600, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                )
            }

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                    }
                },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "OnboardingSteps"
            ) { step ->
                when (step) {
                    0 -> StepWelcome(
                        onNext = { currentStep = 1 },
                        onSkip = onComplete
                    )
                    1 -> StepIndustry(
                        selectedTemplateId = selectedTemplateId,
                        onSelect = { templateId ->
                            selectedTemplateId = templateId
                            INDUSTRY_PRESETS.firstOrNull { it.templateId == templateId }?.let {
                                brandName = it.sampleBusiness
                                legalName = it.sampleBusiness
                                gstin = it.sampleGst
                            }
                        },
                        onNext = { currentStep = 2 }
                    )
                    2 -> StepFirm(
                        selectedIndustry = selectedIndustry,
                        brandName = brandName, onBrand = { brandName = it },
                        legalName = legalName, onLegal = { legalName = it },
                        gstin = gstin, onGstin = { gstin = it.uppercase() },
                        address = address, onAddress = { address = it },
                        city = city, onCity = { city = it },
                        state = state, onState = { state = it },
                        phone = phone, onPhone = { phone = it },
                        upiVpa = upiVpa, onUpi = { upiVpa = it },
                        signatoryName = signatoryName, onSignatory = { signatoryName = it },
                        fieldColors = fieldColors, fieldTextStyle = fieldTextStyle,
                        onNext = { currentStep = 3 }
                    )
                    3 -> StepReviewLaunch(
                        brandName = brandName, legalName = legalName,
                        selectedIndustry = selectedIndustry,
                        address = address, city = city, state = state,
                        gstin = gstin, upiVpa = upiVpa,
                        onLaunch = {
                            if (brandName.trim().isBlank()) {
                                Toast.makeText(context, "Please enter Firm / Brand Name", Toast.LENGTH_SHORT).show()
                                return@StepReviewLaunch
                            }
                            val profile = BusinessProfile(
                                brandName = brandName.trim(),
                                legalName = legalName.trim().ifBlank { brandName.trim() },
                                address = address.trim(),
                                city = city.trim(),
                                state = state.trim(),
                                pincode = pincode.trim(),
                                gstin = gstin.trim().takeIf { it.isNotBlank() },
                                pan = pan.trim().takeIf { it.isNotBlank() },
                                signatoryName = signatoryName.trim().takeIf { it.isNotBlank() },
                                upiVpa = upiVpa.trim().takeIf { it.isNotBlank() },
                                businessTemplateId = selectedTemplateId
                            )
                            viewModel.saveBusinessProfile(profile) { newId ->
                                viewModel.switchBusiness(newId)
                                viewModel.completeOnboarding()
                                Toast.makeText(context, "Firm '${profile.brandName}' Ready!", Toast.LENGTH_SHORT).show()
                                onComplete()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BlackButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = if (enabled) Brand600 else Color(0xFFD4D4D8),
        modifier = modifier.height(52.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun StepWelcome(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by AppLanguageManager.currentLanguageFlow.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = Spacing.md)) {
            // App Header Bar with Official BillBook Logo (neatly organized, full circular emblem visible)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_billbook_logo),
                        contentDescription = "BillBook Official Logo",
                        modifier = Modifier.size(46.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BillBook",
                                color = Ink900,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.3).sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFDBEAFE),
                                border = BorderStroke(0.5.dp, Color(0xFF93C5FD))
                            ) {
                                Text(
                                    text = "PRO",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E3A8A),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Smart Invoicing & Billing OS",
                            color = Ink600,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(0.8.dp, Color(0xFF86EFAC))
                ) {
                    Text(
                        text = "100% OFFLINE",
                        color = Color(0xFF15803D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Startup Hero Image Card (1st attached image replaced with startup image)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_startup_illustration),
                    contentDescription = "BillBook Startup Invoicing",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = appString("welcome_title"),
                color = Ink900, fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )
            Text(
                text = appString("welcome_subtitle"),
                color = Ink600, fontSize = 12.5.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = Spacing.md)
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // 1. Language Selection Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Line),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌐", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = appString("choose_lang"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Ink900
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SUPPORTED_LANGUAGES.forEach { lang ->
                            val isSelected = currentLang == lang.code
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    width = if (isSelected) 1.8.dp else 1.dp,
                                    color = if (isSelected) Brand600 else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        AppLanguageManager.setLanguage(context, lang.code)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = lang.nativeName,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Brand600 else Ink900,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = lang.name,
                                        fontSize = 10.sp,
                                        color = if (isSelected) Brand600 else Ink600,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // 2. Google Sign-In Hero Card on Welcome Page
            GoogleSignInCard(
                modifier = Modifier.fillMaxWidth(),
                onSignInSuccess = {
                    Toast.makeText(context, "Signed in! You can now configure your firm.", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Feature Highlights
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FeatureRow(Icons.Default.Store, "Multi-Firm Architecture", "Separate GSTINs, shops & verticals, one account.")
                FeatureRow(Icons.Default.FlashOn, "Dynamic Industry Engines", "Solar kW, Pharmacy Rx batches, Kirana weights, Civil BOQ.")
                FeatureRow(Icons.Default.ReceiptLong, "Instant PDF + WhatsApp", "CA-compliant letterheads with UPI QR codes.")
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            BlackButton(appString("get_started"), onNext, Modifier.fillMaxWidth())

            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Line)
            ) {
                Text(appString("skip"), fontSize = 13.sp, color = Ink600, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(Brand100),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = Brand700, modifier = Modifier.size(19.dp)) }
            Spacer(modifier = Modifier.width(Spacing.md))
            Column {
                Text(title, color = Ink900, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, color = Ink600, fontSize = 12.sp, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
private fun StepIndustry(selectedTemplateId: Long, onSelect: (Long) -> Unit, onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = Spacing.md)
    ) {
        Text("CHOOSE YOUR INDUSTRY", color = Ink600, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Select Business Vertical", color = Ink900, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Invoice fields, units and GST schedules configure automatically.",
            color = Ink600, fontSize = 12.sp, modifier = Modifier.padding(bottom = Spacing.sm)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(INDUSTRY_PRESETS) { item ->
                val isSelected = item.templateId == selectedTemplateId
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Brand600 else Surface,
                    border = BorderStroke(1.3.dp, if (isSelected) Brand600 else Line),
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(item.templateId) }
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.emoji, fontSize = 20.sp)
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            item.title,
                            color = if (isSelected) Color.White else Ink900,
                            fontWeight = FontWeight.Bold, fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            item.highlight,
                            color = if (isSelected) Color(0xFFD4D4D8) else Ink600,
                            fontSize = 10.5.sp, maxLines = 2, lineHeight = 13.sp
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(Spacing.md))
        BlackButton("Next: Setup Firm", onNext, Modifier.fillMaxWidth())
    }
}

@Composable
private fun SlimField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    colors: androidx.compose.material3.TextFieldColors,
    textStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontSize = 12.sp) },
        placeholder = { if (placeholder.isNotBlank()) Text(placeholder, fontSize = 12.sp, color = Ink400) },
        colors = colors,
        textStyle = textStyle,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = capitalization),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    )
}

@Composable
private fun StepFirm(
    selectedIndustry: IndustryCardItem,
    brandName: String, onBrand: (String) -> Unit,
    legalName: String, onLegal: (String) -> Unit,
    gstin: String, onGstin: (String) -> Unit,
    address: String, onAddress: (String) -> Unit,
    city: String, onCity: (String) -> Unit,
    state: String, onState: (String) -> Unit,
    phone: String, onPhone: (String) -> Unit,
    upiVpa: String, onUpi: (String) -> Unit,
    signatoryName: String, onSignatory: (String) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors,
    fieldTextStyle: androidx.compose.ui.text.TextStyle,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text("FIRM IDENTITY", color = Ink600, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Configure Primary Firm", color = Ink900, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Shown on all Invoices, Estimates & Receipts.", color = Ink600, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(Spacing.md))

            // 1. Core Brand Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Line)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1. BUSINESS IDENTITY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Ink600, letterSpacing = 1.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(0.5.dp, Color(0xFFBFDBFE))
                        ) {
                            Text(
                                "${selectedIndustry.emoji} ${selectedIndustry.title}",
                                fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Brand700,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    SlimField(brandName, onBrand, "Firm / Brand Name *", fieldColors, fieldTextStyle, Modifier.fillMaxWidth(), selectedIndustry.sampleBusiness)
                    SlimField(legalName, onLegal, "Legal Registered Name", fieldColors, fieldTextStyle, Modifier.fillMaxWidth(), "As on PAN / GST certificate")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. GST & Location Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Line)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. GSTIN & ADDRESS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Ink600, letterSpacing = 1.sp)
                    SlimField(gstin, onGstin, "GSTIN (Optional)", fieldColors, fieldTextStyle, Modifier.fillMaxWidth(), "27AAAAA0000A1Z5", KeyboardType.Text, KeyboardCapitalization.Characters)
                    SlimField(address, onAddress, "Shop / Office Address", fieldColors, fieldTextStyle, Modifier.fillMaxWidth(), "Shop No. 4, Market Yard")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SlimField(city, onCity, "City", fieldColors, fieldTextStyle, Modifier.weight(1f))
                        SlimField(state, onState, "State", fieldColors, fieldTextStyle, Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Payment & Signatory Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Line)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. PAYMENTS & SIGNATURE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Ink600, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SlimField(phone, onPhone, "Phone / WhatsApp", fieldColors, fieldTextStyle, Modifier.weight(1f), keyboardType = KeyboardType.Phone)
                        SlimField(upiVpa, onUpi, "UPI ID (for QR)", fieldColors, fieldTextStyle, Modifier.weight(1f), "shop@upi")
                    }
                    SlimField(signatoryName, onSignatory, "Authorized Signatory", fieldColors, fieldTextStyle, Modifier.fillMaxWidth(), "Proprietor / Director")
                }
            }
        }
        Spacer(modifier = Modifier.height(Spacing.lg))
        BlackButton("Review & Launch Firm", onNext, Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Spacing.md))
    }
}

@Composable
private fun StepReviewLaunch(
    brandName: String, legalName: String, selectedIndustry: IndustryCardItem,
    address: String, city: String, state: String, gstin: String, upiVpa: String,
    onLaunch: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_billbook_logo),
                contentDescription = "BillBook Official Logo",
                modifier = Modifier.size(76.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Text("READY TO BILL", color = Color(0xFF1E3A8A), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Your Firm is Configured!", color = Ink900, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Spacing.lg))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = BorderStroke(1.4.dp, Line)
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Brand100) {
                            Text(
                                "OFFICIAL LETTERHEAD", color = Brand700, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text("${selectedIndustry.emoji} ${selectedIndustry.title}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Ink600)
                    }
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(brandName.ifBlank { "Primary Firm" }, fontSize = 19.sp, fontWeight = FontWeight.Black, color = Ink900)
                    if (legalName.isNotBlank() && legalName != brandName) Text(legalName, fontSize = 12.sp, color = Ink600)
                    Spacer(modifier = Modifier.height(4.dp))
                    val loc = listOfNotNull(address.takeIf { it.isNotBlank() }, city.takeIf { it.isNotBlank() }, state.takeIf { it.isNotBlank() }).joinToString(", ")
                    if (loc.isNotBlank()) Text(loc, fontSize = 11.sp, color = Ink600)
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("GSTIN: ${gstin.ifBlank { "Unregistered" }}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Brand700)
                        if (upiVpa.isNotBlank()) Text("UPI: $upiVpa", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceSoft,
                border = BorderStroke(1.dp, Line),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "You can add more firms and switch anytime from the Home header or Settings.",
                    color = Ink600, fontSize = 11.5.sp,
                    modifier = Modifier.padding(Spacing.md)
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.xl))
        BlackButton("Launch BillBook Dashboard", onLaunch, Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Spacing.md))
    }
}
