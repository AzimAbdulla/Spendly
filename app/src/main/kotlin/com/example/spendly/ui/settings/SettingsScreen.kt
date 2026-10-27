package com.example.spendly.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.spendly.ui.theme.ChevronRight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.spendly.sms.SmsScanResult
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.Tune

val supportedCurrencies = listOf("INR", "USD", "EUR", "GBP", "CAD", "AUD", "AED", "CHF", "SGD", "JPY")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    defaultCurrency: String = "INR",
    onCurrencySelected: (String) -> Unit = {},
    smsAutoSave: Boolean = true,
    onSmsAutoSaveChanged: (Boolean) -> Unit = {},
    isScanningSms: Boolean = false,
    onScanSms: ((SmsScanResult) -> Unit) -> Unit = {},
    onReviewInboxClick: () -> Unit = {},
    onClearAllData: () -> Unit = {},
    onLoadSampleData: () -> Unit = {},
    themeMode: String = "SYSTEM",
    onThemeModeSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = SpendlyTheme.colors
    val context = LocalContext.current
    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var scanResultMessage by remember { mutableStateOf<String?>(null) }

    var hasSmsPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.RECEIVE_SMS] == true &&
                      results[Manifest.permission.READ_SMS] == true
        hasSmsPermissions = granted
        if (granted) {
            scanResultMessage = "SMS permissions granted. Automatic spend ingestion active."
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(colors.primary, CircleShape)
                        )
                        Text(
                            text = "Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W700,
                            color = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // GENERAL PREFERENCES Section
            item {
                Text(
                    text = "GENERAL PREFERENCES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    color = colors.textSecondary,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            item {
                SpendlyCard {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Default Reporting Currency Row
                        ExposedDropdownMenuBox(
                            expanded = currencyDropdownExpanded,
                            onExpandedChange = { currencyDropdownExpanded = it }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .clickable { currencyDropdownExpanded = true },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp).background(colors.container, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val symbol = when (defaultCurrency) {
                                            "INR" -> "₹"
                                            "EUR" -> "€"
                                            "GBP" -> "£"
                                            else -> "$"
                                        }
                                        Text(symbol, fontSize = 16.sp, fontWeight = FontWeight.W700, color = colors.textPrimary)
                                    }
                                    Column {
                                        Text("Default Reporting Currency", fontSize = 14.sp, fontWeight = FontWeight.W600, color = colors.textPrimary)
                                        Text("$defaultCurrency · Tap to change", fontSize = 12.sp, color = colors.primary)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.textSecondary)
                            }

                            ExposedDropdownMenu(
                                expanded = currencyDropdownExpanded,
                                onDismissRequest = { currencyDropdownExpanded = false }
                            ) {
                                supportedCurrencies.forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text(curr) },
                                        onClick = {
                                            onCurrencySelected(curr)
                                            currencyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Info Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.container,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Stored transactions retain original logged currency. All calculations are executed strictly on device without third-party sync.",
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        HorizontalDivider(color = colors.border)

                        // Appearance Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Appearance", fontSize = 14.sp, fontWeight = FontWeight.W600, color = colors.textPrimary)
                                val modeLabel = when (themeMode) {
                                    "LIGHT" -> "Light"
                                    "DARK" -> "Dark"
                                    else -> "System Default"
                                }
                                Text(modeLabel, fontSize = 12.sp, color = colors.textSecondary)
                            }

                            // Auto | Light | Dark pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = colors.container,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                            ) {
                                Row(modifier = Modifier.padding(2.dp)) {
                                    listOf(
                                        "Auto" to "SYSTEM",
                                        "Light" to "LIGHT",
                                        "Dark" to "DARK"
                                    ).forEach { (label, modeKey) ->
                                        val isSel = themeMode == modeKey
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(if (isSel) colors.cardBackground else Color.Transparent)
                                                .clickable { onThemeModeSelected(modeKey) }
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.W700 else FontWeight.W500,
                                                color = if (isSel) colors.textPrimary else colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SMS EXPENSE TRACKING Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SMS EXPENSE TRACKING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.W700,
                        color = colors.textSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(colors.primary, CircleShape))
                        Text(
                            text = "Active Daemon",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.W600,
                            color = colors.primary
                        )
                    }
                }
            }

            item {
                SpendlyCard {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Automated SMS Ingestion Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(colors.container, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Tune,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text("Automated SMS Ingestion", fontSize = 14.sp, fontWeight = FontWeight.W600, color = colors.textPrimary)
                                    Text("Instant spend detection from bank SMS", fontSize = 12.sp, color = colors.textSecondary)
                                }
                            }
                            Switch(
                                checked = hasSmsPermissions,
                                onCheckedChange = {
                                    if (!hasSmsPermissions) {
                                        permissionLauncher.launch(
                                            arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                                        )
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.onPrimary,
                                    checkedTrackColor = colors.primary
                                )
                            )
                        }

                        // Detection Engine badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.container,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Detection Engine", fontSize = 12.sp, fontWeight = FontWeight.W600, color = colors.textPrimary)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.cardBackground)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "On-Device Parsing (100% Private)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.W600,
                                        color = colors.primary
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = colors.border)

                        // SMS Read Permission Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("SMS Read Permission", fontSize = 14.sp, fontWeight = FontWeight.W600, color = colors.textPrimary)
                                Text("Standard Android runtime privilege", fontSize = 12.sp, color = colors.textSecondary)
                            }

                            if (hasSmsPermissions) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.container)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Granted",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.W700,
                                        color = colors.primary
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        permissionLauncher.launch(
                                            arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Grant", fontSize = 12.sp)
                                }
                            }
                        }

                        HorizontalDivider(color = colors.border)

                        // Auto-save High Confidence Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-save High Confidence", fontSize = 14.sp, fontWeight = FontWeight.W600, color = colors.textPrimary)
                                Text("Skip manual check for recognizable merchants", fontSize = 12.sp, color = colors.textSecondary)
                            }
                            Switch(
                                checked = smsAutoSave,
                                onCheckedChange = onSmsAutoSaveChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.onPrimary,
                                    checkedTrackColor = colors.primary
                                )
                            )
                        }

                        // Scan SMS Inbox Button
                        OutlinedButton(
                            onClick = {
                                if (!hasSmsPermissions) {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                                    )
                                } else {
                                    onScanSms { result ->
                                        scanResultMessage = if (result.importedCount > 0) {
                                            "Successfully imported ${result.importedCount} transaction(s) from ${result.scannedCount} SMS messages."
                                        } else {
                                            "Scanned ${result.scannedCount} SMS messages. No new financial expenses found."
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isScanningSms,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isScanningSms) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Scanning Inbox...")
                            } else {
                                Text("Scan SMS Inbox Now", fontSize = 13.sp)
                            }
                        }

                        scanResultMessage?.let { msg ->
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = if (msg.contains("Successfully")) colors.primary else colors.textSecondary,
                                fontWeight = FontWeight.W500
                            )
                        }
                    }
                }
            }

            // DATA MANAGEMENT & BACKUP Section
            item {
                Text(
                    text = "DATA MANAGEMENT & BACKUP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    color = colors.textSecondary,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            item {
                SpendlyCard {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Load Sample Data / Restore
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onLoadSampleData),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Load Sample Data", fontSize = 14.sp, fontWeight = FontWeight.W600, color = colors.textPrimary)
                                Text("Populate ledger with realistic spending entries", fontSize = 12.sp, color = colors.textSecondary)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.textSecondary)
                        }

                        HorizontalDivider(color = colors.border)

                        // Clear All Data
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onClearAllData),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Clear All Data", fontSize = 14.sp, fontWeight = FontWeight.W600, color = colors.error)
                                Text("Permanently wipe stored transactions and categories", fontSize = 12.sp, color = colors.textSecondary)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.error)
                        }
                    }
                }
            }

            // Version Pill & Tagline Footer
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.container)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(colors.primary, CircleShape))
                            Text(
                                text = "Spendly v1.4.0 (Offline-First Build)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.W600,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Bottom clearance spacer
            item {
                Spacer(Modifier.height(88.dp))
            }
        }
    }
}
