package com.example.spendly.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.spendly.sms.SmsScanResult
import com.example.spendly.ui.components.GlassSegmentedControl
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.theme.ChevronRight
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme

val supportedCurrencies = listOf("INR", "USD", "EUR", "GBP", "CAD", "AUD", "AED", "CHF", "SGD", "JPY")

val currencyChipList = listOf(
    "INR" to "₹ INR",
    "USD" to "$ USD",
    "EUR" to "€ EUR",
    "GBP" to "£ GBP",
    "AED" to "د.إ AED",
    "JPY" to "¥ JPY",
    "CAD" to "$ CAD",
    "AUD" to "$ AUD",
    "SGD" to "$ SGD"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    themeMode: String = "DARK",
    onThemeModeSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = SpendlyTheme.colors
    val context = LocalContext.current
    var currencyExpanded by remember { mutableStateOf(false) }
    var scanResultMessage by remember { mutableStateOf<String?>(null) }
    var showWipeDialog by remember { mutableStateOf(false) }

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
        modifier = modifier,
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp)
    ) { _ ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Privacy Hero Highlight Banner
            item {
                SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.containerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Stays on this phone",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )

                            }
                            Text(
                                text = "Spendly has no internet permission. Your SMS and expenses never leave this phone.",
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // GROUP 1: General Preferences
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "General",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        letterSpacing = 0.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Currency Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { currencyExpanded = !currencyExpanded }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colors.containerHigh)
                                            .border(1.dp, colors.border, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CurrencyRupee,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Currency",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "$defaultCurrency (${when(defaultCurrency) { "INR" -> "₹ - Indian Rupee"; "USD" -> "$ - US Dollar"; "EUR" -> "€ - Euro"; "GBP" -> "£ - British Pound"; "AED" -> "د.إ - Dirham"; else -> defaultCurrency }})",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Expandable Currency Chip Grid
                            if (currencyExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(colors.containerLow)
                                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Choose currency",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        currencyChipList.forEach { (code, label) ->
                                            val isSelected = code == defaultCurrency
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isSelected) colors.primary else colors.containerHigh,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    if (isSelected) colors.primary else colors.border
                                                ),
                                                onClick = {
                                                    onCurrencySelected(code)
                                                    currencyExpanded = false
                                                }
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Medium,
                                                    color = if (isSelected) colors.onPrimary else colors.textPrimary,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Currency Footnote
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.containerLow)
                                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Existing expenses keep the currency they were saved in. Nothing is converted.",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 15.sp
                                )
                            }

                            HorizontalDivider(color = colors.divider)

                            // Appearance Row
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
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colors.containerHigh)
                                            .border(1.dp, colors.border, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Appearance",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = when (themeMode) {
                                                "DARK" -> "Dark Mode (OLED)"
                                                "LIGHT" -> "Light Mode"
                                                else -> "System Default"
                                            },
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                // Segmented Control (Auto, Light, Dark)
                                GlassSegmentedControl(
                                    items = listOf("SYSTEM" to "Auto", "LIGHT" to "Light", "DARK" to "Dark"),
                                    selected = themeMode,
                                    onSelect = onThemeModeSelected
                                )
                            }
                        }
                    }
                }
            }

            // GROUP 2: SMS Expense Tracking (Core Engine)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SMS tracking",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary,
                            letterSpacing = 0.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val smsActive = smsAutoSave && hasSmsPermissions
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (smsActive) colors.secondary else colors.textMuted, CircleShape)
                            )
                            Text(
                                text = if (smsActive) "On" else "Off",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.textSecondary
                            )
                        }
                    }

                    SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Master Ingestion Switch
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
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colors.containerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sms,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Read bank SMS",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "Adds an expense when a debit SMS arrives",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                                Switch(
                                    checked = smsAutoSave,
                                    onCheckedChange = onSmsAutoSaveChanged,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colors.secondary,
                                        checkedBorderColor = Color.Transparent,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = colors.containerHigh,
                                        uncheckedBorderColor = Color.Transparent
                                    )
                                )
                            }

                            // Engine Architecture Pill
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.containerLow)
                                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Memory,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Parsing",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary
                                    )
                                }
                                Text(
                                    text = "On this device",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = colors.textSecondary
                                )
                            }

                            // Permission Status Row
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
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colors.containerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "SMS permission",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = if (hasSmsPermissions) "Allowed" else "Needed to read bank SMS",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                if (hasSmsPermissions) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Granted",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.RECEIVE_SMS,
                                                    Manifest.permission.READ_SMS
                                                )
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                                    ) {
                                        Text(
                                            text = "Allow",
                                            fontSize = 13.sp,
                                            color = colors.onPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Review Inbox Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = onReviewInboxClick)
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colors.containerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MarkEmailUnread,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Review inbox",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.textPrimary
                                            )

                                        }
                                        Text(
                                            text = "SMS expenses waiting for a check",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Scan Past SMS Button
                            OutlinedButton(
                                onClick = {
                                    onScanSms { res ->
                                        scanResultMessage = "Scanned ${res.scannedCount} SMS · ${res.importedCount} added · ${res.skippedDuplicates} already saved"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isScanningSms
                            ) {
                                if (isScanningSms) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Scanning SMS…")
                                } else {
                                    Text(
                                        "Scan past SMS",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary
                                    )
                                }
                            }

                            if (scanResultMessage != null) {
                                Text(
                                    text = scanResultMessage!!,
                                    fontSize = 11.sp,
                                    color = colors.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Strict Privacy Stamp
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.containerLow)
                                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Bank and UPI SMS are read on this phone only. Message text and account numbers are never sent anywhere.",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // GROUP 3: Data Management & Backup
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Data",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        letterSpacing = 0.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Export Transactions
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {}
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colors.containerHigh)
                                            .border(1.dp, colors.border, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileDownload,
                                            contentDescription = null,
                                            tint = colors.textPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Export expenses",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "Save your expenses as a file",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            HorizontalDivider(color = colors.divider)

                            // Load Sample Data
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onLoadSampleData()
                                        scanResultMessage = "Sample data loaded successfully."
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colors.containerHigh)
                                            .border(1.dp, colors.border, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Storage,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Load sample data",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "Adds demo expenses for testing",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            HorizontalDivider(color = colors.divider)

                            // Wipe Local Ledger
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showWipeDialog = true }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colors.error.copy(alpha = 0.15f))
                                            .border(1.dp, colors.error.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = null,
                                            tint = colors.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Delete all data",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.error
                                        )
                                        Text(
                                            text = "Removes every expense from this phone",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Version Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Spendly v1.0.0 (Build 2026.1)",
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Stored on device · No network access",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            // Bottom clearance for dock
            item {
                Spacer(Modifier.height(64.dp))
            }
        }
    }

    if (showWipeDialog) {
        AlertDialog(
            onDismissRequest = { showWipeDialog = false },
            title = {
                Text(
                    text = "Delete all data?",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "This removes every expense from this phone. It cannot be undone.",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllData()
                        showWipeDialog = false
                    }
                ) {
                    Text("Delete all", color = colors.error, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.popup
        )
    }
}
