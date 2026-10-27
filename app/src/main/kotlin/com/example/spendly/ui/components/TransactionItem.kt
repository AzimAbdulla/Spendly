package com.example.spendly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionStatus
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.getIcon
import java.time.format.DateTimeFormatter

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading category badge (Clean Monochrome Vector Icon - No Emojis)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(SpendlyTheme.colors.container, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transaction.category.getIcon(),
                contentDescription = transaction.category.displayName,
                tint = SpendlyTheme.colors.textPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        // Title & metadata
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.merchant,
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
                color = SpendlyTheme.colors.textPrimary,
                maxLines = 1
            )
            Text(
                text = "${transaction.category.displayName} • ${transaction.date.format(DateTimeFormatter.ofPattern("d MMM"))}",
                fontSize = 12.sp,
                color = SpendlyTheme.colors.textSecondary,
                fontWeight = FontWeight.W400
            )
        }

        // Trailing amount in Expense Outflow color
        Column(horizontalAlignment = Alignment.End) {
            val curr = if (transaction.currency == "INR" || transaction.currency == "USD") currencySymbol else "${transaction.currency} "
            Text(
                text = "-$curr${String.format("%,.2f", transaction.amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.W700,
                color = SpendlyTheme.colors.error
            )
            if (transaction.status == TransactionStatus.NEEDS_REVIEW) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SpendlyTheme.colors.tertiaryContainer)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "Review",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.W700,
                        color = SpendlyTheme.colors.tertiary
                    )
                }
            }
        }
    }
}
