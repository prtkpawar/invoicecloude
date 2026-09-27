package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.AppLanguageManager
import com.example.utils.appString
import com.example.data.entity.BusinessProfile
import com.example.data.model.BusinessTerminologyRegistry
import com.example.ui.components.BusinessSwitcherDialog
import com.example.ui.theme.pro.Brand100
import com.example.ui.theme.pro.Brand600
import com.example.ui.theme.pro.Canvas
import com.example.ui.theme.pro.Ink400
import com.example.ui.theme.pro.Ink600
import com.example.ui.theme.pro.Ink900
import com.example.ui.theme.pro.Line
import com.example.ui.theme.pro.Spacing
import com.example.ui.theme.pro.Success600
import com.example.ui.theme.pro.Surface
import com.example.ui.theme.pro.SurfaceSoft
import com.example.ui.viewmodel.BillingViewModel
import com.example.utils.BackupHelper
import kotlinx.coroutines.launch

/**
 * Clean, categorized "More & Settings" screen.
 * Replaces the 1,936-line kitchen sink with intuitive sub-dialogs and category tiles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BillingViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToFirmEditor: (Long?) -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToPrinter: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val businessProfiles by viewModel.businessProfilesState.collectAsState()
    val activeProfile by viewModel.activeBusinessProfileState.collectAsState()
    val activeBusinessId by viewModel.activeBusinessIdState.collectAsState()

    val currentBusinessName = activeProfile?.brandName?.takeIf { it.isNotBlank() }
        ?: activeProfile?.legalName?.takeIf { it.isNotBlank() }
        ?: "Primary Business"

    val terminology = remember(activeProfile) {
        BusinessTerminologyRegistry.getForProfile(activeProfile)
    }

    // Dialog flags
    var showBusinessSwitcher by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDocSettingsDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Language state
    val currentLanguageCode by AppLanguageManager.currentLanguageFlow.collectAsState()

    val languages = listOf(
        "en" to "English (Default)",
        "hi" to "हिन्दी (Hindi)",
        "mr" to "मराठी (Marathi)",
        "gu" to "ગુજરાતી (Gujarati)",
        "ta" to "தமிழ் (Tamil)",
        "te" to "తెలుగు (Telugu)",
        "kn" to "ಕನ್ನಡ (Kannada)",
        "ml" to "മലയാളം (Malayalam)",
        "bn" to "বাংলা (Bengali)",
        "pa" to "ਪੰਜਾਬੀ (Punjabi)",
        "or" to "ଓଡ଼ିଆ (Odia)",
        "as" to "অসমীয়া (Assamese)"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(appString("settings_title"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink900)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink900)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Canvas)
            )
        },
        containerColor = Canvas
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // 1. Active Firm Card Hero
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Brand600),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Brand600,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = terminology.iconEmoji.ifBlank { "🏢" },
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = currentBusinessName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink900
                                    )
                                    Text(
                                        text = "${terminology.title} • GST: ${activeProfile?.gstin ?: "Unregistered"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Ink600,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Button(
                                onClick = { showBusinessSwitcher = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Brand600, contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Switch ▾", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = Line, modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Registered Firms: ${businessProfiles.size}",
                                fontSize = 12.sp,
                                color = Ink600
                            )
                            Row {
                                TextButton(onClick = { onNavigateToFirmEditor(activeBusinessId) }) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Brand600)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Edit Firm Letterhead", fontSize = 12.sp, color = Brand600, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Settings Category Tiles
            item {
                Text(
                    "Configuration & Tools",
                    style = MaterialTheme.typography.labelMedium,
                    color = Ink600,
                    modifier = Modifier.padding(top = Spacing.xs, bottom = 2.dp)
                )
            }

            // Category: Language
            item {
                SettingsCategoryTile(
                    icon = Icons.Default.Language,
                    title = appString("language_select"),
                    subtitle = languages.firstOrNull { it.first == currentLanguageCode }?.second ?: "English",
                    onClick = { showLanguageDialog = true }
                )
            }

            // Category: Analytics & Insights
            item {
                SettingsCategoryTile(
                    icon = Icons.Default.Analytics,
                    title = appString("financial_analytics"),
                    subtitle = appString("financial_analytics_sub"),
                    onClick = onNavigateToAnalytics
                )
            }

            // Category: Document & Numbering
            item {
                SettingsCategoryTile(
                    icon = Icons.Default.Description,
                    title = appString("invoice_defaults"),
                    subtitle = appString("invoice_defaults_sub"),
                    onClick = { showDocSettingsDialog = true }
                )
            }

            // Category: CSV & Tally Data Export
            item {
                SettingsCategoryTile(
                    icon = Icons.Default.Share,
                    title = appString("ca_export"),
                    subtitle = appString("ca_export_sub"),
                    onClick = { showExportDialog = true }
                )
            }

            // Category: Bluetooth Thermal Printer
            item {
                SettingsCategoryTile(
                    icon = Icons.Default.Print,
                    title = appString("thermal_printer"),
                    subtitle = appString("thermal_printer_sub"),
                    onClick = onNavigateToPrinter
                )
            }

            // Category: Backup & Security
            item {
                SettingsCategoryTile(
                    icon = Icons.Default.Backup,
                    title = appString("drive_sync"),
                    subtitle = appString("drive_sync_sub"),
                    onClick = { showBackupDialog = true }
                )
            }

            // Category: Subscription & License
            item {
                SettingsCategoryTile(
                    icon = Icons.Default.Star,
                    title = appString("pro_plan"),
                    subtitle = appString("pro_plan_sub"),
                    onClick = onNavigateToSubscription
                )
            }

            // Category: About & Developer
            item {
                SettingsCategoryTile(
                    icon = Icons.Default.Info,
                    title = appString("about_app"),
                    subtitle = appString("about_app_sub"),
                    onClick = { showAboutDialog = true }
                )
            }
        }
    }

    // Modal: Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = Brand600)
                    Spacer(Modifier.width(8.dp))
                    Text(appString("choose_lang"), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(languages) { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    AppLanguageManager.setLanguage(context, code)
                                    showLanguageDialog = false
                                    Toast.makeText(context, "Language set to $name", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name, fontSize = 14.sp, fontWeight = if (currentLanguageCode == code) FontWeight.Bold else FontWeight.Normal, color = Ink900)
                            if (currentLanguageCode == code) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Success600, modifier = Modifier.size(18.dp))
                            }
                        }
                        HorizontalDivider(color = Line.copy(alpha = 0.5f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close", color = Ink600)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal: Document & Sequence Settings Dialog
    if (showDocSettingsDialog) {
        var invPrefix by remember { mutableStateOf("INV-") }
        var estPrefix by remember { mutableStateOf("EST-") }
        var defaultTerms by remember { mutableStateOf("") }
        var isLoaded by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            invPrefix = viewModel.repository.getSetting("inv_prefix", "INV-", activeBusinessId)
            estPrefix = viewModel.repository.getSetting("est_prefix", "EST-", activeBusinessId)
            defaultTerms = viewModel.repository.getSetting("default_terms", terminology.defaultInvoiceTerms, activeBusinessId)
            isLoaded = true
        }

        AlertDialog(
            onDismissRequest = { showDocSettingsDialog = false },
            title = { Text("Document & Prefix Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = invPrefix,
                        onValueChange = { invPrefix = it },
                        label = { Text("Invoice Prefix") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estPrefix,
                        onValueChange = { estPrefix = it },
                        label = { Text("Estimate Prefix") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = defaultTerms,
                        onValueChange = { defaultTerms = it },
                        label = { Text("Default Terms & Conditions") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.repository.setSetting("inv_prefix", invPrefix, activeBusinessId)
                            viewModel.repository.setSetting("est_prefix", estPrefix, activeBusinessId)
                            viewModel.repository.setSetting("default_terms", defaultTerms, activeBusinessId)
                            showDocSettingsDialog = false
                            Toast.makeText(context, "Document settings saved!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                ) {
                    Text("Save Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDocSettingsDialog = false }) { Text("Cancel") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal: CSV & Tally Data Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Brand600)
                    Spacer(Modifier.width(8.dp))
                    Text("Export Business Data", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Export clean spreadsheet files (CSV) or official Tally XML sales vouchers for CA audits & accounting:",
                        fontSize = 12.5.sp,
                        color = Ink600
                    )

                    Button(
                        onClick = {
                            viewModel.exportInvoicesToCsv(context) { file ->
                                showExportDialog = false
                                if (file != null) BackupHelper.shareExportFile(context, file, "text/csv", "Invoices CSV Export")
                                else Toast.makeText(context, "Failed to export invoices CSV", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📊 Export Invoices Register (CSV)", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.exportCustomersToCsv(context) { file ->
                                showExportDialog = false
                                if (file != null) BackupHelper.shareExportFile(context, file, "text/csv", "Customers Directory CSV Export")
                                else Toast.makeText(context, "Failed to export customers CSV", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("👥 Export Customers Directory (CSV)", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.exportLedgerToCsv(context) { file ->
                                showExportDialog = false
                                if (file != null) BackupHelper.shareExportFile(context, file, "text/csv", "Full Ledger CSV Export")
                                else Toast.makeText(context, "Failed to export ledger CSV", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📖 Export Full Khata Ledger (CSV)", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.exportTallyXml(context) { file ->
                                showExportDialog = false
                                if (file != null) BackupHelper.shareExportFile(context, file, "text/xml", "Tally Sales Vouchers XML Export")
                                else Toast.makeText(context, "Failed to export Tally XML", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🧾 Export Tally Sales Vouchers (XML for CA)", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Close") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal: Backup & Cloud Sync Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = Brand600)
                    Spacer(Modifier.width(8.dp))
                    Text("Offline Backup & Cloud Sync", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "100% Offline-First. Your data stays safely on your phone with zero server cost.",
                        fontSize = 12.5.sp,
                        color = Ink600
                    )

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("☁️ Google Drive Nightly Backup", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF166534))
                                Text("Nightly automatic sync enabled via WorkManager", fontSize = 11.5.sp, color = Color(0xFF15803D))
                            }
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val res = com.example.utils.GoogleDriveSyncHelper.syncNow(context, viewModel.database)
                                    if (res.isSuccess) {
                                        Toast.makeText(context, "Google Drive nightly sync successful! Snapshot updated.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Sync result: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Sync error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(appString("sync_now"))
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val file = BackupHelper.createBackup(context, viewModel.database)
                                    BackupHelper.shareBackup(context, file)
                                    showBackupDialog = false
                                    Toast.makeText(context, "Full JSON database backup exported!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Export Full JSON Database Backup")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupDialog = false }) { Text("Done") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal: About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(appString("about_app"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Version 1.2.0 (Go-Live Edition)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink900)
                    Text("An enterprise-grade, offline-first billing & invoicing platform tailored for Indian retail, wholesale, pharmacy, and EPC businesses.", fontSize = 12.5.sp, color = Ink600)
                    Spacer(Modifier.height(6.dp))
                    Text("• Instant CA-compliant GST PDF Invoices", fontSize = 12.sp, color = Ink900)
                    Text("• Zero cloud lock-in — 100% offline data privacy", fontSize = 12.sp, color = Ink900)
                    Text("• Instant WhatsApp PDF & Payment Reminders", fontSize = 12.sp, color = Ink900)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Business Switcher Dialog
    if (showBusinessSwitcher) {
        BusinessSwitcherDialog(
            businesses = businessProfiles,
            activeBusinessId = activeBusinessId,
            onSelectBusiness = { selectedId ->
                viewModel.switchBusiness(selectedId)
                showBusinessSwitcher = false
                val chosen = businessProfiles.find { it.id == selectedId }
                Toast.makeText(context, "Active firm set to '${chosen?.brandName}'", Toast.LENGTH_SHORT).show()
            },
            onAddNewBusiness = {
                showBusinessSwitcher = false
                onNavigateToFirmEditor(null)
            },
            onEditBusiness = { biz ->
                showBusinessSwitcher = false
                onNavigateToFirmEditor(biz.id)
            },
            onDismiss = { showBusinessSwitcher = false }
        )
    }
}

/**
 * Clean setting category tile component.
 */
@Composable
private fun SettingsCategoryTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Line),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Brand100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Brand600, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Ink900
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink600,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Ink400, modifier = Modifier.size(20.dp))
        }
    }
}
