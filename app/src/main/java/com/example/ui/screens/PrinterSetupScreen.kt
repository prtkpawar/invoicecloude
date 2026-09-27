package com.example.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.ThermalPrinterHelper

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun PrinterSetupScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val savedMac = remember { ThermalPrinterHelper.getSavedPrinterMac(context) }
    var selectedMac by remember { mutableStateOf(savedMac) }
    val pairedDevices = remember { ThermalPrinterHelper.getPairedDevices() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🖨️ Printer Setup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Text("Select your Bluetooth Thermal Printer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "Make sure the printer is turned ON and paired via Android Bluetooth settings first.",
                    fontSize = 13.sp, color = Color(0xFF52525B)
                )
                Spacer(Modifier.height(16.dp))
            }

            if (pairedDevices.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))) {
                        Text(
                            "No paired Bluetooth devices found.\nGo to Android Settings → Bluetooth → Pair your printer first.",
                            modifier = Modifier.padding(16.dp), fontSize = 13.sp
                        )
                    }
                }
            }

            items(pairedDevices) { device ->
                val isSelected = device.address == selectedMac
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            selectedMac = device.address
                            ThermalPrinterHelper.savePrinterMac(context, device.address)
                            Toast.makeText(context, "Printer saved: ${device.name ?: device.address}", Toast.LENGTH_SHORT).show()
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFEEF2FF) else Color.White
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4338CA)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFF4338CA) else Color(0xFF52525B)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(device.name ?: "Unknown Device", fontWeight = FontWeight.SemiBold)
                            Text(device.address, fontSize = 12.sp, color = Color(0xFFA1A1AA))
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color(0xFF4338CA))
                        }
                    }
                }
            }
        }
    }
}
