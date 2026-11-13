package com.example.spendly.ui.components

import com.example.spendly.ui.theme.OnTintSafe
import com.example.spendly.ui.theme.TintSafe
import com.example.spendly.data.currencySymbol
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    currencySymbol: String = "₹",
    // Given on lists where an SMS expense can be confirmed in place, without opening it
    onConfirm: (() -> Unit)? = null
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
            val merchantLabel = transaction.merchant.takeIf {
                it.isNotBlank() && it != transaction.category.displayName && it != "Unknown Merchant"
            }
            // The item leads; without one (SMS imports) the payee does, and failing that the category
            val title = transaction.item.ifBlank { merchantLabel ?: transaction.category.displayName }
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
            // Clock patterns go on transaction.time (a LocalTime), never on the LocalDate
            val is24Hour = android.text.format.DateFormat.is24HourFormat(LocalContext.current)
            val formattedTime = transaction.time?.format(
                DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a")
            )
            val whenLabel = listOfNotNull(formattedDate, formattedTime).joinToString(", ")
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
                val meta = listOfNotNull(
                    transaction.category.displayName.takeIf { it != title },
                    merchantLabel.takeIf { it != title }
                ).joinToString(" · ")
                // Names give way first, so the date and time always stay readable
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                Text(
                    text = if (meta.isNotEmpty()) "· $whenLabel" else whenLabel,
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

        // Trailing amount (Space Grotesk, tabular). Everything here is spending, so no minus sign
        Column(horizontalAlignment = Alignment.End) {
            // An SMS amount in another currency keeps its own symbol until it's been reviewed
            val ownSymbol = currencySymbol(transaction.currency)
            val curr = if (transaction.status == TransactionStatus.NEEDS_REVIEW && ownSymbol != currencySymbol) ownSymbol else currencySymbol
            Text(
                text = "$curr${String.format("%,.2f", transaction.amount)}",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                // Paid back: still on record, but dimmed because it isn't counted
                color = if (transaction.paidBack) colors.textSecondary else colors.textPrimary,
                style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
            )
            if (transaction.paidBack) {
                Text(text = "Paid back", fontSize = 12.sp, color = colors.textSecondary)
            }
            if (transaction.status == TransactionStatus.NEEDS_REVIEW) {
                if (onConfirm != null && ownSymbol == currencySymbol) {
                    Text(
                        text = "Confirm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnTintSafe,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onConfirm)
                            .background(TintSafe)
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                } else {
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
}
