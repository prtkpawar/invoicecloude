package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.entity.BusinessProfile
import com.example.ui.viewmodel.BillingViewModel

private val SwamiNavy = Color(0xFF1E3A8A)
private val SwamiNavyLight = Color(0xFFEFF6FF)
private val SwamiGreen = Color(0xFF16A34A)

private data class IndustryTemplateOption(
    val id: Long,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val subtitle: String,
    val defaultLicenseLabel: String
)

private val INDUSTRY_OPTIONS = listOf(
    IndustryTemplateOption(1L, "Solar & Energy", Icons.Default.SolarPower, Color(0xFFF59E0B), "Rooftop, Net-Metering & kW Capacity", "MNRE / Discom Vendor Reg"),
    IndustryTemplateOption(2L, "Construction & Civil", Icons.Default.Engineering, Color(0xFFEA580C), "Civil Works, BOQ & Brass/Sq.Ft", "PWD Contractor Reg"),
    IndustryTemplateOption(3L, "Kirana & Grocery", Icons.Default.ShoppingCart, Color(0xFF16A34A), "FMCG, Weights (kg/g/L) & MRP", "FSSAI License No"),
    IndustryTemplateOption(4L, "Medical & Pharmacy", Icons.Default.LocalPharmacy, Color(0xFF0284C7), "Rx Drugs, Batch No & Expiry (MM/YY)", "Drug License (D.L. 20B/21B)"),
    IndustryTemplateOption(5L, "Salon & Wellness", Icons.Default.Spa, Color(0xFFDB2777), "Services, Packages & Appointments", "Shop & Establishment Reg"),
    IndustryTemplateOption(6L, "Retail & General", Icons.Default.Receipt, Color(0xFF0F172A), "General POS & Tax Invoice", "MSME / Udyam Reg No"),
    IndustryTemplateOption(7L, "Wholesale & Trading", Icons.Default.LocalShipping, Color(0xFF0284C7), "Cartons, Cases, FMCG & Agency", "Trade & Tax License No"),
    IndustryTemplateOption(8L, "Restaurant & Cafe", Icons.Default.Restaurant, Color(0xFFDC2626), "Dine-in, F&B, KOT & Table Billing", "FSSAI Food License No"),
    IndustryTemplateOption(9L, "Hardware & Sanitary", Icons.Default.Build, Color(0xFF334155), "Paints, Pipes, Electrical & Tools", "Trade License / GSTIN"),
    IndustryTemplateOption(10L, "Services & Consulting", Icons.Default.BusinessCenter, Color(0xFF312E81), "Consultancy, AMC & IT Projects", "Professional Tax / MSME"),
    IndustryTemplateOption(11L, "Electronics & Mobile", Icons.Default.Tv, Color(0xFF7C3AED), "Serial No, Brands & Warranty Periods", "Trade / Brand Dealership No"),
    IndustryTemplateOption(12L, "Sweet Shop & Bakery", Icons.Default.Cake, Color(0xFFD97706), "Mithai, Farsan, Cakes & Confectionery", "FSSAI Food License No")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirmMasterFormScreen(
    firmId: Long? = null,
    viewModel: BillingViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val businessProfiles by viewModel.businessProfilesState.collectAsState()
    val activeProfile by viewModel.activeBusinessProfileState.collectAsState()

    val existingProfile = remember(firmId, businessProfiles) {
        if (firmId != null && firmId > 0L) {
            businessProfiles.firstOrNull { it.id == firmId }
        } else null
    }

    val isEditing = existingProfile != null

    // Form States
    var brandName by remember { mutableStateOf(existingProfile?.brandName ?: "") }
    var legalName by remember { mutableStateOf(existingProfile?.legalName ?: "") }
    var selectedTemplateId by remember { mutableLongStateOf(existingProfile?.businessTemplateId ?: 3L) }
    var address by remember { mutableStateOf(existingProfile?.address ?: "") }
    var city by remember { mutableStateOf(existingProfile?.city ?: "Dhule") }
    var state by remember { mutableStateOf(existingProfile?.state ?: "Maharashtra") }
    var pincode by remember { mutableStateOf(existingProfile?.pincode ?: "424306") }
    var gstin by remember { mutableStateOf(existingProfile?.gstin ?: "") }
    var pan by remember { mutableStateOf(existingProfile?.pan ?: "") }
    var bankAccountNo by remember { mutableStateOf(existingProfile?.bankAccountNo ?: "") }
    var ifsc by remember { mutableStateOf(existingProfile?.ifsc ?: "") }
    var upiVpa by remember { mutableStateOf(existingProfile?.upiVpa ?: "") }
    var signatoryName by remember { mutableStateOf(existingProfile?.signatoryName ?: "") }
    var logoUriString by remember { mutableStateOf(existingProfile?.logoPath ?: "") }
    var signatureUriString by remember { mutableStateOf(existingProfile?.signaturePath ?: "") }
    var setAsActive by remember { mutableStateOf(existingProfile?.id == activeProfile?.id || !isEditing) }

    var brandError by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            logoUriString = uri.toString()
            Toast.makeText(context, "Firm Logo Loaded", Toast.LENGTH_SHORT).show()
        }
    }

    val selectedIndustry = INDUSTRY_OPTIONS.firstOrNull { it.id == selectedTemplateId } ?: INDUSTRY_OPTIONS[2]

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF0F172A),
        unfocusedTextColor = Color(0xFF0F172A),
        focusedBorderColor = SwamiNavy,
        unfocusedBorderColor = Color(0xFFCBD5E1),
        focusedLabelColor = SwamiNavy,
        unfocusedLabelColor = Color(0xFF64748B),
        cursorColor = SwamiNavy,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color(0xFFF8FAFC)
    )

    fun performSave() {
        if (brandName.trim().isBlank()) {
            brandError = true
            Toast.makeText(context, "Please enter Firm / Brand Name", Toast.LENGTH_SHORT).show()
            return
        }

        val profileToSave = (existingProfile ?: BusinessProfile(
            legalName = legalName.trim().ifBlank { brandName.trim() },
            brandName = brandName.trim(),
            address = address.trim(),
            city = city.trim(),
            state = state.trim(),
            pincode = pincode.trim(),
            gstin = gstin.trim().takeIf { it.isNotBlank() },
            pan = pan.trim().takeIf { it.isNotBlank() },
            bankAccountNo = bankAccountNo.trim().takeIf { it.isNotBlank() },
            ifsc = ifsc.trim().takeIf { it.isNotBlank() },
            upiVpa = upiVpa.trim().takeIf { it.isNotBlank() },
            signatoryName = signatoryName.trim().takeIf { it.isNotBlank() },
            logoPath = logoUriString.takeIf { it.isNotBlank() },
            signaturePath = signatureUriString.takeIf { it.isNotBlank() },
            businessTemplateId = selectedTemplateId
        )).copy(
            brandName = brandName.trim(),
            legalName = legalName.trim().ifBlank { brandName.trim() },
            address = address.trim(),
            city = city.trim(),
            state = state.trim(),
            pincode = pincode.trim(),
            gstin = gstin.trim().takeIf { it.isNotBlank() },
            pan = pan.trim().takeIf { it.isNotBlank() },
            bankAccountNo = bankAccountNo.trim().takeIf { it.isNotBlank() },
            ifsc = ifsc.trim().takeIf { it.isNotBlank() },
            upiVpa = upiVpa.trim().takeIf { it.isNotBlank() },
            signatoryName = signatoryName.trim().takeIf { it.isNotBlank() },
            logoPath = logoUriString.takeIf { it.isNotBlank() },
            signaturePath = signatureUriString.takeIf { it.isNotBlank() },
            businessTemplateId = selectedTemplateId
        )

        viewModel.saveBusinessProfile(profileToSave) { newId ->
            if (setAsActive) {
                viewModel.switchBusiness(newId)
            }
            Toast.makeText(context, "Firm Letterhead & Identity Saved Successfully!", Toast.LENGTH_LONG).show()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEditing) "Edit Firm & Letterhead" else "New Firm Identity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (brandName.isNotBlank()) brandName else "Dynamic PDF Letterhead Master",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = { performSave() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = SwamiNavy, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Firm", color = SwamiNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SwamiNavy)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // SECTION 1: THE LIVE LETTERHEAD PREVIEW (HERO)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, SwamiNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = SwamiNavyLight, modifier = Modifier.size(28.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Apartment, contentDescription = null, tint = SwamiNavy, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Live PDF Letterhead Preview",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SwamiNavy
                            )
                        }

                        Surface(
                            color = SwamiGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, SwamiGreen)
                        ) {
                            Text(
                                "LIVE MOCKUP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = SwamiGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Letterhead Mockup Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (brandName.isNotBlank()) brandName else "YOUR FIRM / SHOP NAME",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A),
                                        fontFamily = FontFamily.SansSerif
                                    )
                                    Text(
                                        text = if (legalName.isNotBlank() && legalName != brandName) legalName else selectedIndustry.subtitle,
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569),
                                        fontWeight = FontWeight.Medium
                                    )
                                    val fullLoc = listOfNotNull(
                                        address.takeIf { it.isNotBlank() },
                                        "$city, $state - $pincode".takeIf { city.isNotBlank() }
                                    ).joinToString(", ")
                                    Text(
                                        text = if (fullLoc.isNotBlank()) fullLoc else "Shop Address, City, State - PIN",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    if (gstin.isNotBlank()) {
                                        Text(
                                            text = "GSTIN: $gstin",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SwamiNavy
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Dynamic Logo Box
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(selectedIndustry.color.copy(alpha = 0.15f))
                                        .border(1.dp, selectedIndustry.color.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (logoUriString.isNotBlank()) {
                                        AsyncImage(
                                            model = logoUriString,
                                            contentDescription = "Firm Logo",
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Icon(
                                            imageVector = selectedIndustry.icon,
                                            contentDescription = null,
                                            tint = selectedIndustry.color,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Bottom Preview: Bank & Signatory
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "A/C: ${if (bankAccountNo.isNotBlank()) bankAccountNo else "XXXX-XXXX-XXXX"}  IFSC: ${if (ifsc.isNotBlank()) ifsc else "XXXX0000"}",
                                        fontSize = 9.sp,
                                        color = Color(0xFF64748B),
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (upiVpa.isNotBlank()) {
                                        Text(
                                            text = "UPI: $upiVpa",
                                            fontSize = 9.sp,
                                            color = SwamiGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (signatoryName.isNotBlank()) signatoryName else "Authorized Signatory",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Proprietor / Signatory",
                                        fontSize = 8.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2: INDUSTRY SELECTOR (HORIZONTAL CAROUSEL)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Industry Vertical", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                    Text("Customizes tax rules, units & letterhead templates", fontSize = 11.5.sp, color = Color(0xFF64748B))
                    Spacer(Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(INDUSTRY_OPTIONS) { option ->
                            val isSelected = selectedTemplateId == option.id
                            Card(
                                onClick = { selectedTemplateId = option.id },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) SwamiNavyLight else Color.White
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) SwamiNavy else Color(0xFFE2E8F0)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .width(120.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        option.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) SwamiNavy else option.color,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        option.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        color = if (isSelected) SwamiNavy else Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: BRAND & LEGAL IDENTITY (THE CORE)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Brand & Legal Identity", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))

                    // Row 1: Brand / Trade Name (Full width)
                    OutlinedTextField(
                        value = brandName,
                        onValueChange = { brandName = it; brandError = false },
                        label = { Text("Brand / Trade Name (Header Title) *") },
                        placeholder = { Text("e.g. Apex Pharmacy, Swami Solar, Ganesh Kirana") },
                        isError = brandError,
                        supportingText = if (brandError) { { Text("Brand Name is required") } } else null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = fieldColors,
                        singleLine = true
                    )

                    // Row 2: Registered Legal Name (Full width)
                    OutlinedTextField(
                        value = legalName,
                        onValueChange = { legalName = it },
                        label = { Text("Registered Legal Name (as per GST / PAN)") },
                        placeholder = { Text("e.g. Apex Healthcare Pvt. Ltd. / Swami Solar Solutions") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = fieldColors,
                        singleLine = true
                    )

                    // Row 3: Logo Uploader button & Signatory Name field (Side-by-side)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                logoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (logoUriString.isNotBlank()) SwamiGreen else Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (logoUriString.isNotBlank()) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                            )
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = if (logoUriString.isNotBlank()) SwamiGreen else SwamiNavy,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (logoUriString.isNotBlank()) "Logo Loaded ✓" else "Upload Logo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (logoUriString.isNotBlank()) SwamiGreen else SwamiNavy
                            )
                        }

                        OutlinedTextField(
                            value = signatoryName,
                            onValueChange = { signatoryName = it },
                            label = { Text("Signatory Name") },
                            placeholder = { Text("e.g. Pratik Patil") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = fieldColors,
                            singleLine = true
                        )
                    }
                }
            }

            // SECTION 4: ADDRESS DETAILS (SIDE-BY-SIDE)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Premises & Address Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))

                    // Row 1: Address / Street (Full width)
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Street Address / Shop No.") },
                        placeholder = { Text("e.g. Shop No 4, Market Yard, Near Old Bus Stand") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = fieldColors,
                        singleLine = true
                    )

                    // Row 2: City (Weight 1f) + State (Weight 1f) + Pincode (Weight 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            colors = fieldColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State") },
                            colors = fieldColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = pincode,
                            onValueChange = { pincode = it },
                            label = { Text("Pincode") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = fieldColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // SECTION 5: TAX & BANKING (SIDE-BY-SIDE)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tax & Banking Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))

                    // Row 1: GSTIN (Weight 1f) + PAN (Weight 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = gstin,
                            onValueChange = {
                                gstin = it.uppercase()
                                if (it.length >= 12 && pan.isBlank()) {
                                    pan = it.substring(2, kotlin.math.min(12, it.length)).uppercase()
                                }
                            },
                            label = { Text("GSTIN") },
                            placeholder = { Text("27BAZPT3492D1Z0") },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            colors = fieldColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = pan,
                            onValueChange = { pan = it.uppercase() },
                            label = { Text("PAN Number") },
                            placeholder = { Text("BAZPT3492D") },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            colors = fieldColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Row 2: Bank Account No (Weight 1f) + IFSC Code (Weight 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = bankAccountNo,
                            onValueChange = { bankAccountNo = it },
                            label = { Text("Bank Account No") },
                            placeholder = { Text("324201010510532") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = fieldColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = ifsc,
                            onValueChange = { ifsc = it.uppercase() },
                            label = { Text("IFSC Code") },
                            placeholder = { Text("UBIN0532428") },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            colors = fieldColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.9f),
                            singleLine = true
                        )
                    }

                    // Row 3: UPI ID (VPA) (Full width)
                    OutlinedTextField(
                        value = upiVpa,
                        onValueChange = { upiVpa = it },
                        label = { Text("UPI ID (VPA) for NPCI Dynamic QR Codes") },
                        placeholder = { Text("e.g. yourbusiness@okaxis / 9890950000@upi") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = fieldColors,
                        singleLine = true
                    )
                }
            }

            // SECTION 6: ACTIVE FIRM TOGGLE
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Set as Current Active Firm", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                        Text("New invoices and estimates will default to this firm letterhead.", fontSize = 11.5.sp, color = Color(0xFF64748B))
                    }
                    Switch(
                        checked = setAsActive,
                        onCheckedChange = { setAsActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SwamiNavy
                        )
                    )
                }
            }

            // PRIMARY SAVE BUTTON (SWAMINAVY)
            Button(
                onClick = { performSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SwamiNavy),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Save Changes & Update Letterhead" else "Create Firm Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
