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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.spendly.ui.theme.StitchBackground
import com.example.spendly.ui.theme.StitchBorder
import com.example.spendly.ui.theme.StitchCardSurface
import com.example.spendly.ui.theme.StitchContainer
import com.example.spendly.ui.theme.StitchContainerLow
import com.example.spendly.ui.theme.StitchError
import com.example.spendly.ui.theme.StitchOnPrimary
import com.example.spendly.ui.theme.StitchOnSurface
import com.example.spendly.ui.theme.StitchOnSurfaceVariant
import com.example.spendly.ui.theme.StitchOnTertiary
import com.example.spendly.ui.theme.StitchPrimary
import com.example.spendly.ui.theme.StitchPrimaryFixed
import com.example.spendly.ui.theme.StitchSecondary
import com.example.spendly.ui.theme.StitchSecondaryFixed
import com.example.spendly.ui.theme.StitchTertiaryContainer
import com.example.spendly.ui.theme.StitchTertiaryFixed

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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var scanResultMessage by remember { mutableStateOf<String?>(null) }
    var selectedAppearance by remember { mutableStateOf("Auto") }

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
                        Box(modifier = Modifier.size(8.dp).background(StitchPrimary, CircleShape))
                        Text(
                            text = "Settings",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.W700,
                            color = StitchOnSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = StitchOnSurfaceVariant)
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(32.dp)
                            .background(StitchPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = StitchOnPrimary, modifier = Modifier.size(18.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StitchBackground)
            )
        },
        containerColor = StitchBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Green Banner: Offline Ledger Security
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEAF5EE),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6D3)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFD2EBDC), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 18.sp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Offline Ledger Security",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.W700,
                                    color = StitchOnSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFBAE3C9))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Local Only",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.W700,
                                        color = StitchSecondary
                                    )
                                }
                            }
                            Text(
                                text = "Spendly operates completely isolated from third-party servers. Your SMS texts and spend logs never leave this handset.",
                                fontSize = 12.sp,
                                color = StitchOnSurfaceVariant,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            // GENERAL PREFERENCES Section
            item {
                Text(
                    text = "GENERAL PREFERENCES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    color = StitchPrimary,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
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
                                        modifier = Modifier.size(36.dp).background(StitchContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("₹", fontSize = 16.sp, fontWeight = FontWeight.W700, color = StitchOnSurface)
                                    }
                                    Column {
                                        Text("Default Reporting Currency", fontSize = 14.sp, fontWeight = FontWeight.W600, color = StitchOnSurface)
                                        Text("$defaultCurrency · Tap to change", fontSize = 12.sp, color = StitchPrimary)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StitchOnSurfaceVariant)
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
                            color = StitchContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("ℹ️", fontSize = 12.sp)
                                Text(
                                    text = "Stored transactions retain original logged currency. No volatile online exchange rate dependencies or remote sync.",
                                    fontSize = 11.sp,
                                    color = StitchOnSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        HorizontalDivider(color = StitchContainer)

                        // Appearance Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(StitchContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎨", fontSize = 16.sp)
                                }
                                Column {
                                    Text("Appearance", fontSize = 14.sp, fontWeight = FontWeight.W600, color = StitchOnSurface)
                                    Text("System Default", fontSize = 12.sp, color = StitchOnSurfaceVariant)
                                }
                            }

                            // Auto | Light | Dark pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = StitchContainerLow,
                                border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                            ) {
                                Row(modifier = Modifier.padding(2.dp)) {
                                    listOf("Auto", "Light", "Dark").forEach { mode ->
                                        val isSel = selectedAppearance == mode
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(if (isSel) StitchCardSurface else Color.Transparent)
                                                .clickable { selectedAppearance = mode }
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = mode,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.W700 else FontWeight.W500,
                                                color = if (isSel) StitchOnSurface else StitchOnSurfaceVariant
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
                        color = StitchPrimary,
                        letterSpacing = 0.8.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(StitchSecondary, CircleShape))
                        Text(
                            text = "Active Daemon",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.W600,
                            color = StitchSecondary
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
                                    modifier = Modifier.size(36.dp).background(StitchPrimaryFixed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💬", fontSize = 16.sp)
                                }
                                Column {
                                    Text("Automated SMS Ingestion", fontSize = 14.sp, fontWeight = FontWeight.W600, color = StitchOnSurface)
                                    Text("Instant spend detection from bank SMS", fontSize = 12.sp, color = StitchOnSurfaceVariant)
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
                                    checkedThumbColor = StitchOnPrimary,
                                    checkedTrackColor = StitchPrimary
                                )
                            )
                        }

                        // Detection Engine badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StitchContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("⚙️", fontSize = 14.sp)
                                    Text("Detection Engine", fontSize = 12.sp, fontWeight = FontWeight.W600, color = StitchOnSurface)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StitchPrimaryFixed)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "On-Device Parsing (100% Private)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.W600,
                                        color = StitchPrimary
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = StitchContainer)

                        // SMS Read Permission Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(Color(0xFFEAF5EE), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🛡️", fontSize = 16.sp)
                                }
                                Column {
                                    Text("SMS Read Permission", fontSize = 14.sp, fontWeight = FontWeight.W600, color = StitchOnSurface)
                                    Text("Standard Android runtime privilege", fontSize = 12.sp, color = StitchOnSurfaceVariant)
                                }
                            }

                            if (hasSmsPermissions) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StitchSecondaryFixed)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "✓ Granted",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.W700,
                                        color = StitchSecondary
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

                        HorizontalDivider(color = StitchContainer)

                        // Review Inbox Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onReviewInboxClick),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(StitchTertiaryFixed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📥", fontSize = 16.sp)
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("Review Inbox", fontSize = 14.sp, fontWeight = FontWeight.W600, color = StitchOnSurface)
                                        Box(
                                            modifier = Modifier.size(16.dp).background(StitchTertiaryContainer, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("2", fontSize = 10.sp, fontWeight = FontWeight.W700, color = StitchOnTertiary)
                                        }
                                    }
                                    Text("Transactions needing attention", fontSize = 12.sp, color = StitchOnSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StitchOnSurfaceVariant)
                        }

                        HorizontalDivider(color = StitchContainer)

                        // Auto-save High Confidence Toggle
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
                                    modifier = Modifier.size(36.dp).background(StitchContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⚡", fontSize = 16.sp)
                                }
                                Column {
                                    Text("Auto-save High Confidence", fontSize = 14.sp, fontWeight = FontWeight.W600, color = StitchOnSurface)
                                    Text("Skip manual check for recognizable merchants", fontSize = 12.sp, color = StitchOnSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = smsAutoSave,
                                onCheckedChange = onSmsAutoSaveChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StitchOnPrimary,
                                    checkedTrackColor = StitchPrimary
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
                                Text("📥 Scan SMS Inbox Now", fontSize = 13.sp)
                            }
                        }

                        scanResultMessage?.let { msg ->
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = if (msg.contains("Successfully")) StitchSecondary else StitchOnSurfaceVariant,
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
                    color = StitchPrimary,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                SpendlyCard {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Export Transactions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {},
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.size(36.dp).background(StitchContainer, CircleShape), contentAlignment = Alignment.Center) {
                                    Text("📥", fontSize = 16.sp)
                                }
                                Column {
                                    Text("Export Transactions", fontSize = 14.sp, fontWeight = FontWeight.W600, color = StitchOnSurface)
                                    Text("Generate clean CSV or encrypted JSON ledger", fontSize = 12.sp, color = StitchOnSurfaceVariant)
                                }
                            }
                            Text("⬇️", fontSize = 14.sp)
                        }

                        HorizontalDivider(color = StitchContainer)

                        // Load Sample Data / Restore
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onLoadSampleData),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.size(36.dp).background(StitchContainer, CircleShape), contentAlignment = Alignment.Center) {
                                    Text("📄", fontSize = 16.sp)
                                }
                                Column {
                                    Text("Load Sample Data", fontSize = 14.sp, fontWeight = FontWeight.W600, color = StitchOnSurface)
                                    Text("Populate ledger with realistic spending entries", fontSize = 12.sp, color = StitchOnSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StitchOnSurfaceVariant)
                        }

                        HorizontalDivider(color = StitchContainer)

                        // Clear All Data
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onClearAllData),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.size(36.dp).background(Color(0xFFFFEAEA), CircleShape), contentAlignment = Alignment.Center) {
                                    Text("🗑️", fontSize = 16.sp)
                                }
                                Column {
                                    Text("Clear All Data", fontSize = 14.sp, fontWeight = FontWeight.W600, color = StitchError)
                                    Text("Permanently wipe stored transactions and categories", fontSize = 12.sp, color = StitchOnSurfaceVariant)
                                }
                            }
                            Text("⚠️", fontSize = 14.sp)
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
                            .background(StitchContainerLow)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(StitchPrimary, CircleShape))
                            Text(
                                text = "Spendly v1.4.0 (Offline-First Build)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.W600,
                                color = StitchOnSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Made for Android with Material 3 ergonomics. Tactile linen utility.",
                        fontSize = 11.sp,
                        color = StitchOnSurfaceVariant,
                        fontWeight = FontWeight.W400
                    )
                }
            }

            // Bottom clearance spacer
            item {
                Spacer(Modifier.height(88.dp))
            }
        }
    }
}
