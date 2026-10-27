package com.example.spendly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionStatus
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
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
    val colors = SpendlyTheme.colors
    val catColor = stitchCategoryColors[transaction.category] ?: colors.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading category badge with category-tinted glow
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(catColor.copy(alpha = if (colors.isDark) 0.18f else 0.12f))
                .border(
                    1.dp,
                    catColor.copy(alpha = if (colors.isDark) 0.35f else 0.25f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transaction.category.getIcon(),
                contentDescription = transaction.category.displayName,
                tint = catColor,
                modifier = Modifier.size(18.dp)
            )
        }

        // Title & metadata
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.merchant,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1
            )
            val today = java.time.LocalDate.now()
            val formattedDate = when (transaction.date) {
                today -> "Today"
                today.minusDays(1) -> "Yesterday"
                else -> {
                    if (transaction.date.year == today.year) {
                        transaction.date.format(DateTimeFormatter.ofPattern("d MMM"))
                    } else {
                        transaction.date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                    }
                }
            }
            Text(
                text = "${transaction.category.displayName} • $formattedDate",
                fontSize = 11.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Normal
            )
        }

        // Trailing amount in Expense Outflow color (Space Grotesk tabular)
        Column(horizontalAlignment = Alignment.End) {
            val curr = if (transaction.currency == "INR" || transaction.currency == "USD") currencySymbol else "${transaction.currency} "
            Text(
                text = "-$curr${String.format("%,.2f", transaction.amount)}",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.error
            )
            if (transaction.status == TransactionStatus.NEEDS_REVIEW) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.tertiary.copy(alpha = 0.2f))
                        .border(1.dp, colors.tertiary.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "Review",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.tertiary
                    )
                }
            }
        }
    }
}
