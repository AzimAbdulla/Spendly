package com.example.spendly.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Category
import com.example.spendly.ui.components.stitchCategoryColors
import com.example.spendly.ui.theme.FabGradient
import com.example.spendly.ui.theme.GeistFontFamily
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.getIcon
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddExpenseSheet(
    onDismiss: () -> Unit,
    onSave: (amount: Double, currency: String, merchant: String, category: Category, notes: String) -> Unit,
    defaultCurrency: String = "INR",
    prefillAmount: Double? = null
) {
    val colors = SpendlyTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var amountStr by remember { mutableStateOf(prefillAmount?.let { String.format("%.0f", it) } ?: "0") }
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

    val parsedAmount = amountStr.toDoubleOrNull() ?: 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (colors.isDark) Color(0xFF0A0A10) else colors.cardBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(40.dp)
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
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title & Close Button
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
                            .size(6.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0xB39E8CFC))
                            .background(colors.primary, CircleShape)
                    )
                    Text(
                        text = "Quick Log",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Merchant / Payee input
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Merchant / Payee",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) Color(0xFF08080D) else colors.containerLow)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    BasicTextField(
                        value = merchant,
                        onValueChange = { merchant = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        ),
                        cursorBrush = SolidColor(colors.primary),
                        decorationBox = { innerTextField ->
                            if (merchant.isEmpty()) {
                                Text(
                                    text = "e.g. Starbucks, Uber, Swiggy",
                                    fontSize = 14.sp,
                                    color = colors.textSecondary.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            // Date & Source Row (2 columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Date & Time
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Date & Time",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (colors.isDark) Color(0xFF08080D) else colors.containerLow)
                            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Today, ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))}",
                                fontSize = 11.sp,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Logging Mode
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Logging Mode",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (colors.isDark) Color(0xFF08080D) else colors.containerLow)
                            .border(1.dp, colors.secondary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Manual Slip",
                                fontSize = 11.sp,
                                color = colors.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .shadow(6.dp, CircleShape, spotColor = Color(0x992FE575))
                                .background(colors.secondary, CircleShape)
                        )
                    }
                }
            }

            // Notes Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Notes (Optional)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) Color(0xFF08080D) else colors.containerLow)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                    BasicTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        ),
                        cursorBrush = SolidColor(colors.primary),
                        decorationBox = { innerTextField ->
                            if (notes.isEmpty()) {
                                Text(
                                    text = "Add receipt details or memo",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            // Amount Hero Input Section (OLED Dark Well)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (colors.isDark) Color(0xFF050508) else colors.containerLow)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Currency Selector Pill
                Surface(
                    shape = CircleShape,
                    color = if (colors.isDark) Color(0xFF0E0E16) else colors.container,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.primary.copy(alpha = 0.25f)),
                    onClick = { currencyDropdownExpanded = !currencyDropdownExpanded }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "$selectedCurrency ($currencySymbol)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.primary
                        )
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Big Numerical Amount with Glowing Neon Cursor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currencySymbol,
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.primary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = if (amountStr.isEmpty()) "0" else amountStr,
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = ".00",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary.copy(alpha = 0.7f)
                    )
                    // Glowing Neon Cursor
                    Box(
                        modifier = Modifier
                            .padding(start = 3.dp)
                            .width(3.dp)
                            .height(28.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0xB3CABEFE))
                            .background(colors.primary, CircleShape)
                    )
                }

                Text(
                    text = "Outflow recorded to Personal Ledger",
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // Category Picker Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT CATEGORY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "${selectedCategory.displayName} active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary
                    )
                }

                // Grid of 5 categories per row
                val categories = listOf(
                    Category.FOOD,
                    Category.GROCERIES,
                    Category.TRANSPORT,
                    Category.ENTERTAINMENT,
                    Category.UTILITIES
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        val catColor = stitchCategoryColors[cat] ?: colors.primary

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = cat }
                                .background(
                                    if (isSelected) {
                                        if (colors.isDark) Color(0xFF17112C) else colors.primary.copy(alpha = 0.12f)
                                    } else {
                                        if (colors.isDark) Color(0xFF0E0E16) else colors.containerLow
                                    }
                                )
                                .border(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) colors.primary else colors.border,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 10.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(catColor.copy(alpha = if (isSelected) 0.25f else 0.12f))
                                    .then(
                                        if (isSelected) {
                                            Modifier.shadow(8.dp, RoundedCornerShape(8.dp), spotColor = Color(0x66CABEFE))
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cat.getIcon(),
                                    contentDescription = cat.displayName,
                                    tint = if (isSelected) colors.primary else catColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = cat.displayName.split(" ").first(),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else colors.textSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Numeric Keypad
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val numpad = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf(".", "0", "BACKSPACE")
                )

                numpad.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { key ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (colors.isDark) Color(0xFF0E0E16) else colors.containerHigh,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                                onClick = {
                                    when (key) {
                                        "BACKSPACE" -> {
                                            if (amountStr.length > 1) {
                                                amountStr = amountStr.dropLast(1)
                                            } else {
                                                amountStr = "0"
                                            }
                                        }
                                        "." -> {
                                            if (!amountStr.contains(".")) {
                                                amountStr += "."
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
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (key == "BACKSPACE") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Clear button
                Surface(
                    modifier = Modifier
                        .weight(0.35f)
                        .height(48.dp),
                    shape = CircleShape,
                    color = if (colors.isDark) Color(0xFF0E0E16) else colors.containerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                    onClick = { amountStr = "0" }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Clear",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary
                        )
                    }
                }

                // Save Outflow Button (Luminous Gradient Pill)
                Surface(
                    modifier = Modifier
                        .weight(0.65f)
                        .height(48.dp)
                        .shadow(12.dp, CircleShape, spotColor = Color(0xB39E8CFC)),
                    shape = CircleShape,
                    color = Color.Transparent,
                    onClick = {
                        val finalAmount = parsedAmount.coerceAtLeast(1.0)
                        val finalMerchant = if (merchant.isBlank()) selectedCategory.displayName else merchant
                        onSave(finalAmount, selectedCurrency, finalMerchant, selectedCategory, notes)
                        onDismiss()
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(FabGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF18103D),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Save Outflow",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF18103D)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}
