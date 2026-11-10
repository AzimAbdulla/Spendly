package com.example.spendly.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spendly.data.SampleData
import com.example.spendly.data.model.Category
import com.example.spendly.ui.settings.supportedCurrencies
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.components.ScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transactionId: Long,
    transaction: com.example.spendly.data.model.Transaction? = null,
    onSaved: (com.example.spendly.data.model.Transaction) -> Unit,
    onDelete: (Long) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTransaction = transaction
        ?: SampleData.transactions.find { it.id == transactionId }
        ?: SampleData.transactions.first()

    var amount by remember(currentTransaction) { mutableStateOf(String.format(java.util.Locale.US, "%.2f", currentTransaction.amount)) }
    var item by remember(currentTransaction) { mutableStateOf(currentTransaction.item) }
    var merchant by remember(currentTransaction) { mutableStateOf(currentTransaction.merchant) }
    var notes by remember(currentTransaction) { mutableStateOf(currentTransaction.notes) }
    var currency by remember(currentTransaction) { mutableStateOf(currentTransaction.currency) }
    var category by remember(currentTransaction) { mutableStateOf(currentTransaction.category) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Same field treatment as Quick Add: raised surface, hairline border, 10dp corners
    val colors = SpendlyTheme.colors
    val fieldShape = RoundedCornerShape(14.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colors.container,
        unfocusedContainerColor = colors.container,
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.border,
        focusedLabelColor = colors.textSecondary,
        unfocusedLabelColor = colors.textSecondary,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        cursorColor = colors.primary,
        focusedTrailingIconColor = colors.textSecondary,
        unfocusedTrailingIconColor = colors.textSecondary
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenHeader(title = "Edit expense", onBack = onCancel)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = it },
                    modifier = Modifier.weight(0.4f)
                ) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Currency") },
                        shape = fieldShape,
                        colors = fieldColors,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false }
                    ) {
                        supportedCurrencies.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { currency = c; currencyExpanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    shape = fieldShape,
                    colors = fieldColors,
                    textStyle = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 18.sp, fontFeatureSettings = "tnum"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(0.6f)
                )
            }

            OutlinedTextField(
                value = item,
                onValueChange = { item = it },
                label = { Text("Item") },
                shape = fieldShape,
                colors = fieldColors,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant (optional)") },
                shape = fieldShape,
                colors = fieldColors,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = category.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    shape = fieldShape,
                    colors = fieldColors,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    Category.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName) },
                            onClick = { category = cat; categoryExpanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                shape = fieldShape,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull() ?: currentTransaction.amount
                    onSaved(
                        currentTransaction.copy(
                            amount = parsedAmount,
                            merchant = merchant.trim(),
                            item = item.trim(),
                            currency = currency,
                            category = category,
                            notes = notes,
                            status = com.example.spendly.data.model.TransactionStatus.CONFIRMED
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = amount.toDoubleOrNull() != null,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    disabledContainerColor = colors.containerHigh,
                    disabledContentColor = colors.textMuted
                )
            ) {
                Text("Save changes", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = {
                    onDelete(transactionId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.container, contentColor = colors.error)
            ) {
                Text("Delete expense", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditTransactionScreenPreview() {
    SpendlyTheme { EditTransactionScreen(transactionId = 1L, onSaved = {}, onDelete = {}, onCancel = {}) }
}
