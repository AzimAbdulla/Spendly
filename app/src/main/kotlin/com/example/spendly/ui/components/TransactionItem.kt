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
import com.example.spendly.data.model.TransactionSource
import com.example.spendly.data.model.TransactionStatus
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.foundation.layout.height
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
    val catColor = stitchCategoryColors[transaction.category] ?: colors.textMuted

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading category badge: neutral surface, icon in the secondary text role
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.containerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transaction.category.getIcon(),
                contentDescription = transaction.category.displayName,
                tint = colors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        // Title & metadata
        Column(modifier = Modifier.weight(1f)) {
            // The item leads; without one (e.g. SMS imports) the category does
            val title = transaction.item.ifBlank { transaction.category.displayName }
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Category dot carries the category hue, nothing else does
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(catColor, CircleShape)
                )
                val merchantLabel = transaction.merchant.takeIf {
                    it.isNotBlank() && it != transaction.category.displayName && it != "Unknown Merchant"
                }
                val meta = listOfNotNull(
                    transaction.category.displayName.takeIf { it != title },
                    merchantLabel,
                    formattedDate
                ).joinToString(" · ")
                Text(
                    text = meta,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
                // Signature: quiet mark on entries read from a bank SMS
                if (transaction.source == TransactionSource.SMS) {
                    Icon(
                        imageVector = Icons.Outlined.Sms,
                        contentDescription = "From bank SMS",
                        tint = colors.textMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Trailing amount (Space Grotesk, tabular)
        Column(horizontalAlignment = Alignment.End) {
            val curr = if (transaction.currency == "INR" || transaction.currency == "USD") currencySymbol else "${transaction.currency} "
            Text(
                text = "-$curr${String.format("%,.2f", transaction.amount)}",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
            )
            if (transaction.status == TransactionStatus.NEEDS_REVIEW) {
                Text(
                    text = "Needs review",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.tertiary
                )
            }
        }
    }
}
