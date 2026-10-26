package com.example.spendly.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.spendly.sms.SmsScanResult
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.theme.SpendlyAmber
import com.example.spendly.ui.theme.SpendlyExpense
import com.example.spendly.ui.theme.SpendlyIncome
import com.example.spendly.ui.theme.SpendlyTheme

val supportedCurrencies = listOf("USD", "EUR", "GBP", "INR", "JPY", "CAD", "AUD", "CHF", "SGD", "AED")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    defaultCurrency: String = "USD",
    onCurrencySelected: (String) -> Unit = {},
    smsAutoSave: Boolean = true,
    onSmsAutoSaveChanged: (Boolean) -> Unit = {},
    isScanningSms: Boolean = false,
    onScanSms: ((SmsScanResult) -> Unit) -> Unit = {},
    onClearAllData: () -> Unit = {},
    onLoadSampleData: () -> Unit = {}
) {
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
            scanResultMessage = "SMS permissions granted. Automatic expense tracking active."
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.W600) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Currency section
            item {
                Text(
                    "General",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            item {
                SpendlyCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Default Currency",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.W500
                        )
                        Text(
                            "Used for reporting and dashboard totals",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        ExposedDropdownMenuBox(
                            expanded = currencyDropdownExpanded,
                            onExpandedChange = { currencyDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = defaultCurrency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Currency") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = currencyDropdownExpanded,
                                onDismissRequest = { currencyDropdownExpanded = false }
                            ) {
                                supportedCurrencies.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text(currency) },
                                        onClick = {
                                            onCurrencySelected(currency)
                                            currencyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SMS Tracking section
            item {
                Text(
                    "SMS Expense Tracking",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
            item {
                SpendlyCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Permission status badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "SMS Permissions",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.W500
                                )
                                Text(
                                    if (hasSmsPermissions) "Active — Spendly listens for incoming bank SMS"
                                    else "Required for automatic SMS parsing and inbox scan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (hasSmsPermissions) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SpendlyIncome.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SpendlyIncome,
                                        fontWeight = FontWeight.W600
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.RECEIVE_SMS,
                                                Manifest.permission.READ_SMS
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SpendlyAmber)
                                ) {
                                    Text("Grant", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        // Auto-save toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Auto-save High Confidence",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.W500
                                )
                                Text(
                                    "Automatically confirm recognized expenses; uncertain ones route to Review inbox",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = smsAutoSave,
                                onCheckedChange = { checked ->
                                    if (!hasSmsPermissions) {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.RECEIVE_SMS,
                                                Manifest.permission.READ_SMS
                                            )
                                        )
                                    }
                                    onSmsAutoSaveChanged(checked)
                                }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        // Scan SMS Inbox Button
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Import Existing SMS",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.W500
                            )
                            Text(
                                "Scan recent messages from your SMS inbox to import past bank expenses without waiting for new messages.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = {
                                    if (!hasSmsPermissions) {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.RECEIVE_SMS,
                                                Manifest.permission.READ_SMS
                                            )
                                        )
                                    } else {
                                        onScanSms { result ->
                                            scanResultMessage = if (result.importedCount > 0) {
                                                "Successfully imported ${result.importedCount} transaction(s) from ${result.scannedCount} SMS messages (${result.skippedDuplicates} duplicates skipped)."
                                            } else {
                                                "Scanned ${result.scannedCount} SMS messages. No new financial expenses found (${result.skippedDuplicates} already tracked)."
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isScanningSms
                            ) {
                                if (isScanningSms) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Scanning Inbox...")
                                } else {
                                    Text("Scan SMS Inbox Now")
                                }
                            }

                            scanResultMessage?.let { msg ->
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (msg.contains("Successfully")) SpendlyIncome else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.W500
                                )
                            }
                        }
                    }
                }
            }

            // Data Management section
            item {
                Text(
                    "Data Management",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
            item {
                SpendlyCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Manage local financial records stored in device Room database.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = onClearAllData,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SpendlyExpense
                            )
                        ) {
                            Text("Clear All Transactions")
                        }
                        OutlinedButton(
                            onClick = onLoadSampleData,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Load Sample Data")
                        }
                    }
                }
            }

            // About section
            item {
                Text(
                    "About",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
            item {
                SpendlyCard {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Version", style = MaterialTheme.typography.bodyMedium)
                            Text("1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Privacy Guarantee", style = MaterialTheme.typography.bodyMedium)
                            Text("100% Local (No Cloud)", style = MaterialTheme.typography.bodyMedium, color = SpendlyIncome)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    SpendlyTheme { SettingsScreen() }
}
