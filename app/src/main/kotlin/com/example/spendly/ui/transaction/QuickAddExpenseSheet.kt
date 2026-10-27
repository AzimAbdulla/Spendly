package com.example.spendly.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Category
import com.example.spendly.ui.settings.supportedCurrencies
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.getIcon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAddExpenseSheet(
    onDismiss: () -> Unit,
    onSave: (amount: Double, currency: String, merchant: String, category: Category, notes: String) -> Unit,
    defaultCurrency: String = "INR",
    prefillAmount: Double? = null
) {
    val colors = SpendlyTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var amountStr by remember { mutableStateOf(prefillAmount?.let { String.format("%.2f", it) } ?: "0") }
    var selectedCurrency by remember { mutableStateOf(defaultCurrency) }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(Category.FOOD) }
    var merchant by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val currencySymbol = when (selectedCurrency) {
        "INR" -> "₹"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> "$"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        text = "Quick Add Expense",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W700,
                        color = colors.textPrimary
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(colors.container, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Hero Outflow Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.container
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Currency selector pill
                    ExposedDropdownMenuBox(
                        expanded = currencyDropdownExpanded,
                        onExpandedChange = { currencyDropdownExpanded = it }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = colors.cardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                            modifier = Modifier
                                .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .clickable { currencyDropdownExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "$currencySymbol $selectedCurrency ($currencySymbol)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.W600,
                                    color = colors.textPrimary
                                )
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = currencyDropdownExpanded,
                            onDismissRequest = { currencyDropdownExpanded = false }
                        ) {
                            supportedCurrencies.forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text(curr) },
                                    onClick = {
                                        selectedCurrency = curr
                                        currencyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Large Amount Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currencySymbol,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.W600,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = amountStr,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.W700,
                            color = colors.textPrimary,
                            letterSpacing = (-1).sp
                        )
                        // Cursor bar
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .width(3.dp)
                                .height(36.dp)
                                .background(colors.primary, CircleShape)
                        )
                    }

                    Text(
                        text = "Outflow recorded to Personal Ledger",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.W400
                    )
                }
            }

            // SELECT CATEGORY section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT CATEGORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.W700,
                        color = colors.textSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "${selectedCategory.displayName} active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W600,
                        color = colors.primary
                    )
                }

                // 10 category chips in 2 rows
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 5
                ) {
                    Category.entries.forEach { cat ->
                        val isSelected = selectedCategory == cat

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(68.dp)
                                .clickable { selectedCategory = cat },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colors.container else colors.container.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) colors.primary else colors.border
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = cat.getIcon(),
                                    contentDescription = cat.displayName,
                                    tint = if (isSelected) colors.primary else colors.textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = cat.shortName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.W700 else FontWeight.W500,
                                    color = if (isSelected) colors.primary else colors.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Merchant / Payee input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Merchant / Payee",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W600,
                    color = colors.textSecondary
                )
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    placeholder = { Text("e.g. Starbucks, Groceries, Metro", fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.container,
                        unfocusedContainerColor = colors.container,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Notes (Optional)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Notes (Optional)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W600,
                    color = colors.textSecondary
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Add any details or context", fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.container,
                        unfocusedContainerColor = colors.container,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // QUICK KEYPAD (Tactile 4x4 Grid)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.container
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "QUICK KEYPAD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.W700,
                            color = colors.textSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Instant Entry",
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }

                    val keys = listOf(
                        listOf("1", "2", "3", "+100"),
                        listOf("4", "5", "6", "+500"),
                        listOf("7", "8", "9", "⌫"),
                        listOf(".", "0", "00", "✓")
                    )

                    keys.forEach { rowKeys ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowKeys.forEach { key ->
                                val isQuickAdd = key.startsWith("+")
                                val isBackspace = key == "⌫"
                                val isCheck = key == "✓"

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clickable {
                                            when (key) {
                                                "+100" -> {
                                                    val curr = amountStr.toDoubleOrNull() ?: 0.0
                                                    amountStr = String.format("%.0f", curr + 100)
                                                }
                                                "+500" -> {
                                                    val curr = amountStr.toDoubleOrNull() ?: 0.0
                                                    amountStr = String.format("%.0f", curr + 500)
                                                }
                                                "⌫" -> {
                                                    if (amountStr.length > 1) {
                                                        amountStr = amountStr.dropLast(1)
                                                    } else {
                                                        amountStr = "0"
                                                    }
                                                }
                                                "✓" -> {
                                                    // Done entering amount
                                                }
                                                "." -> {
                                                    if (!amountStr.contains(".")) {
                                                        amountStr += "."
                                                    }
                                                }
                                                "00" -> {
                                                    if (amountStr != "0") {
                                                        amountStr += "00"
                                                    }
                                                }
                                                else -> {
                                                    if (amountStr == "0") {
                                                        amountStr = key
                                                    } else {
                                                        amountStr += key
                                                    }
                                                }
                                            }
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = colors.cardBackground,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = key,
                                            fontSize = if (isQuickAdd) 13.sp else 16.sp,
                                            fontWeight = FontWeight.W700,
                                            color = when {
                                                isQuickAdd -> colors.primary
                                                isBackspace -> colors.error
                                                isCheck -> colors.primary
                                                else -> colors.textPrimary
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom CTA Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.container),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W600,
                        color = colors.textPrimary
                    )
                }

                Button(
                    onClick = {
                        val parsed = amountStr.toDoubleOrNull() ?: 0.0
                        if (parsed > 0.0) {
                            val finalMerchant = if (merchant.isNotBlank()) merchant else selectedCategory.displayName
                            onSave(parsed, selectedCurrency, finalMerchant, selectedCategory, notes)
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0.0,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "Save Expense",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W600,
                        color = colors.onPrimary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
