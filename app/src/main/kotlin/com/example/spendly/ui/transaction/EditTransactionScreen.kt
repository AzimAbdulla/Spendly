package com.example.spendly.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spendly.data.SampleData
import com.example.spendly.data.model.Category
import com.example.spendly.ui.settings.supportedCurrencies
import com.example.spendly.ui.theme.SpendlyExpense
import com.example.spendly.ui.theme.SpendlyTheme

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

    var amount by remember(currentTransaction) { mutableStateOf(currentTransaction.amount.toString()) }
    var merchant by remember(currentTransaction) { mutableStateOf(currentTransaction.merchant) }
    var notes by remember(currentTransaction) { mutableStateOf(currentTransaction.notes) }
    var currency by remember(currentTransaction) { mutableStateOf(currentTransaction.currency) }
    var category by remember(currentTransaction) { mutableStateOf(currentTransaction.category) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(0.6f)
                )
            }

            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant") },
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
                            merchant = merchant,
                            currency = currency,
                            category = category,
                            notes = notes,
                            status = com.example.spendly.data.model.TransactionStatus.CONFIRMED
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotBlank() && merchant.isNotBlank()
            ) {
                Text("Save changes", modifier = Modifier.padding(vertical = 4.dp))
            }
            OutlinedButton(
                onClick = {
                    onDelete(transactionId)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SpendlyExpense)
            ) {
                Text("Delete expense")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditTransactionScreenPreview() {
    SpendlyTheme { EditTransactionScreen(transactionId = 1L, onSaved = {}, onDelete = {}, onCancel = {}) }
}
