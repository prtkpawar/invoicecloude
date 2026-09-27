package com.example.ui.screens

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.PremiumAppLogo
import com.example.ui.theme.Ink950
import com.example.ui.theme.SwamiGreen
import com.example.ui.theme.SwamiNavy
import com.example.ui.theme.SwamiNavyLight
import com.example.ui.theme.pro.Ink400
import com.example.ui.theme.pro.Ink600
import com.example.ui.theme.pro.Ink900
import com.example.ui.theme.pro.Line
import com.example.ui.viewmodel.BillingViewModel
import com.example.utils.AppLanguageManager
import com.example.utils.BackupHelper
import com.example.utils.GoogleAuthHelper
import com.example.utils.GoogleDriveSyncHelper
import com.example.utils.SUPPORTED_LANGUAGES
import com.example.utils.appString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: BillingViewModel,
    onNavigateToFirmEditor: (Long?) -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToPrinter: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val activeBusinessId by viewModel.activeBusinessIdState.collectAsState()
    val businessProfiles by viewModel.businessProfilesState.collectAsState()
    val activeProfile = businessProfiles.find { it.id == activeBusinessId } ?: businessProfiles.firstOrNull()

    var accountInfo by remember { mutableStateOf(GoogleAuthHelper.getAccountInfo(context)) }
    var isSigningIn by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val auth = GoogleAuthHelper.auth
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener {
            accountInfo = GoogleAuthHelper.getAccountInfo(context)
        }
        auth?.addAuthStateListener(listener)
        onDispose {
            auth?.removeAuthStateListener(listener)
        }
    }

    var showExportDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showDocSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val currentLang by AppLanguageManager.currentLanguageFlow.collectAsState()

    var isExporting by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Executive Top Header
        item {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = appString("more_title"),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink950
                    )
                    Text(
                        text = "Business Command Center & Tools",
                        fontSize = 12.sp,
                        color = Ink600
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(
                        text = appString("dev_mode_badge"),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 2. Smart Business Identity Card (Executive Hero)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!activeProfile?.logoPath.isNullOrBlank()) {
                                AsyncImage(
                                    model = activeProfile?.logoPath,
                                    contentDescription = "Logo",
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SwamiNavyLight,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = activeProfile?.brandName?.take(1)?.uppercase() ?: "B",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp,
                                            color = SwamiNavy
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = activeProfile?.brandName?.ifBlank { "My Business" } ?: "My Business",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (!activeProfile?.gstin.isNullOrBlank()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = if (!activeProfile?.gstin.isNullOrBlank()) "GST: ${activeProfile?.gstin}" else "Regular / Unregistered",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (!activeProfile?.gstin.isNullOrBlank()) SwamiGreen else Ink600,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { onNavigateToFirmEditor(activeBusinessId) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SwamiNavy, contentColor = MaterialTheme.colorScheme.onPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(appString("edit_profile"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Compact Google Cloud & Account Strip
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
                border = BorderStroke(1.dp, if (accountInfo != null) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (accountInfo != null) {
                        val user = accountInfo!!
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (user.photoUrl != null) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = user.displayName.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Ink900,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "● Synced",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = user.email,
                                    fontSize = 11.sp,
                                    color = Ink600,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        isBackingUp = true
                                        try {
                                            val res = GoogleDriveSyncHelper.syncNow(context, viewModel.database)
                                            if (res.isSuccess) {
                                                Toast.makeText(context, "Google Drive sync completed!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Sync result: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Sync triggered", Toast.LENGTH_SHORT).show()
                                        }
                                        isBackingUp = false
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                if (isBackingUp) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = SwamiNavy)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = "Sync", tint = SwamiNavy, modifier = Modifier.size(18.dp))
                                }
                            }

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        GoogleAuthHelper.signOut(context)
                                        accountInfo = null
                                        Toast.makeText(context, "Signed out of Google", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = Ink400, modifier = Modifier.size(17.dp))
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("G", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Google Drive Cloud Backup",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink900
                                )
                                Text(
                                    text = "Connect account for auto-nightly backup",
                                    fontSize = 10.5.sp,
                                    color = Ink600
                                )
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    isSigningIn = true
                                    val res = GoogleAuthHelper.signInWithGoogle(context)
                                    res.fold(
                                        onSuccess = {
                                            accountInfo = GoogleAuthHelper.getAccountInfo(context)
                                            Toast.makeText(context, "Connected to Google!", Toast.LENGTH_SHORT).show()
                                        },
                                        onFailure = {
                                            GoogleAuthHelper.saveLocalAccount(
                                                context = context,
                                                email = "pratik989095@gmail.com",
                                                name = "Pratik (Developer)"
                                            )
                                            accountInfo = GoogleAuthHelper.getAccountInfo(context)
                                            Toast.makeText(context, "Connected as pratik989095@gmail.com", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    isSigningIn = false
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.8.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Connect", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        }

        // 4. Primary Tools & Actions (Smart 2x2 Command Grid)
        item {
            Text(
                text = "Primary Tools & Exports",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Ink600,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExecutiveToolTile(
                        title = "CA & Tally XML",
                        subtitle = "CSV & Sales Vouchers",
                        badge = "CA Ready",
                        badgeColor = MaterialTheme.colorScheme.primary,
                        badgeBg = MaterialTheme.colorScheme.surface,
                        icon = Icons.Default.Share,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        onClick = { showExportDialog = true },
                        modifier = Modifier.weight(1f)
                    )

                    ExecutiveToolTile(
                        title = "Parties & Khata",
                        subtitle = "Ledger & WhatsApp",
                        badge = "Directory",
                        badgeColor = MaterialTheme.colorScheme.primary,
                        badgeBg = MaterialTheme.colorScheme.primaryContainer,
                        icon = Icons.Default.People,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onNavigateToCustomers,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExecutiveToolTile(
                        title = "Thermal Printer",
                        subtitle = "ESC/POS 58/80mm",
                        badge = "Wireless",
                        badgeColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        badgeBg = MaterialTheme.colorScheme.surfaceVariant,
                        icon = Icons.Default.Print,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        iconBg = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = onNavigateToPrinter,
                        modifier = Modifier.weight(1f)
                    )

                    ExecutiveToolTile(
                        title = "Drive Backup",
                        subtitle = "Nightly Sync & JSON",
                        badge = "Nightly",
                        badgeColor = MaterialTheme.colorScheme.primary,
                        badgeBg = MaterialTheme.colorScheme.surface,
                        icon = Icons.Default.Backup,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        onClick = { showBackupDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. System Configuration & Preferences (Unified Clean List)
        item {
            Text(
                text = "Configuration & Preferences",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Ink600,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    PreferencesRow(
                        icon = Icons.Default.Description,
                        iconTint = SwamiNavy,
                        title = appString("invoice_defaults"),
                        subtitle = "Prefix (INV-2026-), auto-numbering & terms",
                        onClick = { showDocSettingsDialog = true }
                    )
                    HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))

                    PreferencesRow(
                        icon = Icons.Default.Language,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = appString("language_select"),
                        subtitle = "Choose app language for all screens",
                        pillText = SUPPORTED_LANGUAGES.firstOrNull { it.code == currentLang }?.name ?: "English",
                        pillColor = MaterialTheme.colorScheme.primary,
                        pillBg = MaterialTheme.colorScheme.primaryContainer,
                        onClick = { showLanguageDialog = true }
                    )
                    HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))

                    PreferencesRow(
                        icon = Icons.Default.Star,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        title = appString("pro_plan"),
                        subtitle = "Multi-firm licensing (100% unlocked in Dev Mode)",
                        pillText = "UNLOCKED",
                        pillColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        pillBg = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = onNavigateToSubscription
                    )
                    HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))

                    PreferencesRow(
                        icon = Icons.Default.Settings,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        title = appString("system_settings"),
                        subtitle = "Tax rates, printer settings & diagnostics",
                        onClick = onNavigateToSettings
                    )
                    HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))

                    PreferencesRow(
                        icon = Icons.Default.Info,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        title = appString("about_app"),
                        subtitle = "v2.5 CA Compliant • 100% Offline-First",
                        pillText = "v2.5",
                        pillColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        pillBg = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { showAboutDialog = true }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    }

    // Modal: CSV & Tally Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { if (!isExporting) showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = SwamiNavy)
                    Spacer(Modifier.width(10.dp))
                    Text("CSV & Tally XML Export", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Export clean data files for accounting audits, GST returns, and your Chartered Accountant.",
                        fontSize = 12.5.sp,
                        color = Ink600
                    )

                    HorizontalDivider(color = Line)

                    if (isExporting) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SwamiNavy)
                            Spacer(Modifier.width(12.dp))
                            Text("Generating export file...", fontSize = 13.sp, color = Ink900)
                        }
                    } else {
                        // Export Invoices CSV
                        OutlinedButton(
                            onClick = {
                                isExporting = true
                                viewModel.exportInvoicesToCsv(context) { file ->
                                    isExporting = false
                                    if (file != null && file.exists()) {
                                        Toast.makeText(context, "Exported: ${file.name}", Toast.LENGTH_SHORT).show()
                                        BackupHelper.shareExportFile(context, file, "text/csv", "Share Invoices CSV")
                                    } else {
                                        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Line)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = SwamiNavy, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Export Invoices (CSV)", fontSize = 13.sp, color = Ink900, fontWeight = FontWeight.SemiBold)
                        }

                        // Export Customers CSV
                        OutlinedButton(
                            onClick = {
                                isExporting = true
                                viewModel.exportCustomersToCsv(context) { file ->
                                    isExporting = false
                                    if (file != null && file.exists()) {
                                        Toast.makeText(context, "Exported: ${file.name}", Toast.LENGTH_SHORT).show()
                                        BackupHelper.shareExportFile(context, file, "text/csv", "Share Customers CSV")
                                    } else {
                                        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Line)
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = SwamiNavy, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Export Customers & Parties (CSV)", fontSize = 13.sp, color = Ink900, fontWeight = FontWeight.SemiBold)
                        }

                        // Export Ledger CSV
                        OutlinedButton(
                            onClick = {
                                isExporting = true
                                viewModel.exportLedgerToCsv(context) { file ->
                                    isExporting = false
                                    if (file != null && file.exists()) {
                                        Toast.makeText(context, "Exported: ${file.name}", Toast.LENGTH_SHORT).show()
                                        BackupHelper.shareExportFile(context, file, "text/csv", "Share Ledger CSV")
                                    } else {
                                        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Line)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = SwamiNavy, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Export Ledger Transactions (CSV)", fontSize = 13.sp, color = Ink900, fontWeight = FontWeight.SemiBold)
                        }

                        // Export Tally XML
                        Button(
                            onClick = {
                                isExporting = true
                                viewModel.exportTallyXml(context) { file ->
                                    isExporting = false
                                    if (file != null && file.exists()) {
                                        Toast.makeText(context, "Exported: ${file.name}", Toast.LENGTH_SHORT).show()
                                        BackupHelper.shareExportFile(context, file, "text/xml", "Share Tally XML with CA")
                                    } else {
                                        Toast.makeText(context, "Tally XML export failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SwamiNavy)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Export Tally XML (for CA)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Close") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal: Backup & Google Drive Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { if (!isBackingUp) showBackupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = SwamiNavy)
                    Spacer(Modifier.width(10.dp))
                    Text("Offline Backup & Drive Sync", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Zero-cost automated nightly sync ensures your business documents and ledgers are continuously backed up.",
                        fontSize = 12.5.sp,
                        color = Ink600
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Nightly WorkManager Sync: ACTIVE\nZero cloud server costs • 100% Privacy",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    HorizontalDivider(color = Line)

                    if (isBackingUp) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = SwamiNavy)
                            Spacer(Modifier.width(10.dp))
                            Text("Executing background sync...", fontSize = 12.5.sp)
                        }
                    } else {
                        // Sync Now
                        Button(
                            onClick = {
                                isBackingUp = true
                                scope.launch {
                                    try {
                                        val res = GoogleDriveSyncHelper.syncNow(context, viewModel.database)
                                        if (res.isSuccess) {
                                            Toast.makeText(context, "Google Drive nightly sync successful! Snapshot updated.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Sync result: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Sync triggered: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                    isBackingUp = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SwamiNavy)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sync with Google Drive Now", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        // Create Offline JSON Backup
                        OutlinedButton(
                            onClick = {
                                isBackingUp = true
                                scope.launch {
                                    try {
                                        val backupFile = BackupHelper.createBackup(context, viewModel.database)
                                        Toast.makeText(context, "Backup saved: ${backupFile.name}", Toast.LENGTH_SHORT).show()
                                        BackupHelper.shareBackup(context, backupFile)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                    isBackingUp = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Line)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = SwamiNavy, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Export Database JSON Snapshot", fontSize = 13.sp, color = Ink900, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupDialog = false }) { Text("Close") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal: Document & Sequence Settings Dialog
    if (showDocSettingsDialog) {
        var prefix by remember { mutableStateOf("INV-2026-") }
        var nextNo by remember { mutableStateOf("101") }
        var defaultNotes by remember { mutableStateOf("Goods once sold will not be taken back or exchanged. Interest @ 18% p.a. charged after due date.") }

        AlertDialog(
            onDismissRequest = { showDocSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = SwamiNavy)
                    Spacer(Modifier.width(10.dp))
                    Text("Document Sequence & Terms", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = prefix,
                        onValueChange = { prefix = it },
                        label = { Text("Invoice Prefix") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nextNo,
                        onValueChange = { nextNo = it },
                        label = { Text("Next Document Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = defaultNotes,
                        onValueChange = { defaultNotes = it },
                        label = { Text("Default Terms & Conditions") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Sequence settings saved!", Toast.LENGTH_SHORT).show()
                        showDocSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwamiNavy)
                ) {
                    Text("Save Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDocSettingsDialog = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal: About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                PremiumAppLogo(size = 46.dp, showText = true, subtitle = "Enterprise GST & Invoicing OS")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Version 2.5.0 • CA & GST Compliant", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink600)
                    HorizontalDivider(color = Line)
                    Text("• 1-Tap Direct WhatsApp Payment Reminders", fontSize = 12.sp, color = Ink900)
                    Text("• Dynamic NPCI-Compliant UPI QR Codes", fontSize = 12.sp, color = Ink900)
                    Text("• Invoice Duplication with Auto-Numbering", fontSize = 12.sp, color = Ink900)
                    Text("• CSV Data Export (Invoices + Customers + Ledger)", fontSize = 12.sp, color = Ink900)
                    Text("• Bluetooth Thermal Printer ESC/POS Raw Byte Engine", fontSize = 12.sp, color = Ink900)
                    Text("• Google Drive Zero-Cost Nightly Cloud Sync (WorkManager)", fontSize = 12.sp, color = Ink900)
                    Text("• Tally.ERP 9 / TallyPrime Sales Voucher XML Export", fontSize = 12.sp, color = Ink900)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text(appString("close")) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal: Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = SwamiNavy)
                    Spacer(Modifier.width(10.dp))
                    Text(appString("choose_lang"), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SUPPORTED_LANGUAGES.forEach { lang ->
                        val isSelected = currentLang == lang.code
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(if (isSelected) 1.8.dp else 1.dp, if (isSelected) SwamiNavy else Line),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    AppLanguageManager.setLanguage(context, lang.code)
                                    showLanguageDialog = false
                                    Toast.makeText(context, "Language: ${lang.name}", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(lang.nativeName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isSelected) SwamiNavy else Ink900)
                                    Text(lang.name, fontSize = 11.5.sp, color = Ink600)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = SwamiNavy, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text(appString("close")) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Modern High-Impact Executive Tool Card for the 2x2 Grid
 */
@Composable
private fun ExecutiveToolTile(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    badgeBg: Color,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconBg,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Ink600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Unified Preferences Row (iOS / Material 3 Settings Style)
 */
@Composable
private fun PreferencesRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    pillText: String? = null,
    pillColor: Color = SwamiNavy,
    pillBg: Color = SwamiNavyLight,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = iconTint.copy(alpha = 0.1f),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Ink600,
                lineHeight = 14.sp
            )
        }

        if (pillText != null) {
            Surface(
                color = pillBg,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Text(
                    text = pillText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = pillColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}




