package com.example.spendly.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionStatus
import com.example.spendly.ui.theme.SpendlyAmberContainer
import com.example.spendly.ui.theme.SpendlyExpense
import com.example.spendly.ui.theme.SpendlyLavenderContainer
import java.time.format.DateTimeFormatter

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category emoji badge
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = if (transaction.status == TransactionStatus.NEEDS_REVIEW)
                SpendlyAmberContainer else SpendlyLavenderContainer
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transaction.category.emoji,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.merchant,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.W500,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${transaction.category.displayName} · ${transaction.date.format(DateTimeFormatter.ofPattern("MMM d"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "-${transaction.currency} ${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.W600,
                color = SpendlyExpense
            )
            if (transaction.status == TransactionStatus.NEEDS_REVIEW) {
                Text(
                    text = "Review",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
