package com.example.spendly.ui.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Category
import com.example.spendly.ui.components.stitchCategoryColors
import com.example.spendly.ui.theme.GeistFontFamily
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.getIcon

@Composable
fun AddTransactionScreen(
    prefillAmount: Double? = null,
    defaultCurrency: String = "INR",
    onSaved: (Double, String, String, Category, String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SpendlyTheme.colors

    var amountStr by remember { mutableStateOf(prefillAmount?.let { String.format("%.0f", it) } ?: "0") }
    var selectedCategory by remember { mutableStateOf(Category.FOOD) }
    var merchant by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val currencySymbol = when (defaultCurrency) {
        "INR" -> "₹"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> "$"
    }

    val parsedAmount = amountStr.toDoubleOrNull() ?: 0.0

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = colors.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add expense",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Merchant / Payee input
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Merchant",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.containerLow)
                        .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = colors.textSecondary,
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

            // Notes Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Notes (optional)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.containerLow)
                        .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
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
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.containerLow)
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(vertical = 20.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Amount being typed
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currencySymbol,
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = if (amountStr.isEmpty()) "0" else amountStr,
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        letterSpacing = (-0.8).sp
                    )
                    if (!amountStr.contains(".")) Text(
                        text = ".00",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary.copy(alpha = 0.7f)
                    )
                    // Caret
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .width(2.dp)
                            .height(28.dp)
                            .background(colors.primary, CircleShape)
                    )
                }
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
                        text = "Category",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.textSecondary
                    )
                    Text(
                        text = selectedCategory.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.textSecondary
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == selectedCategory

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = cat }
                                .background(if (isSelected) colors.containerHigh else colors.containerLow)
                                .border(
                                    1.dp,
                                    if (isSelected) colors.primary else colors.border,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 12.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cat.getIcon(),
                                    contentDescription = cat.displayName,
                                    tint = if (isSelected) colors.textPrimary else colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = cat.shortName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) colors.textPrimary else colors.textSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Numeric Keypad
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { key ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = colors.containerHigh,
                                border = BorderStroke(1.dp, colors.border),
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
                                            fontWeight = FontWeight.Medium,
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Clear button
                Surface(
                    modifier = Modifier
                        .weight(0.35f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = colors.containerHigh,
                    border = BorderStroke(1.dp, colors.border),
                    onClick = { amountStr = "0" }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Clear",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }
                }

                // Save: the primary action, off-white fill with dark text
                Surface(
                    modifier = Modifier
                        .weight(0.65f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = colors.textPrimary,
                    onClick = {
                        val finalAmount = parsedAmount.coerceAtLeast(1.0)
                        val finalMerchant = if (merchant.isBlank()) selectedCategory.displayName else merchant
                        onSaved(finalAmount, defaultCurrency, finalMerchant, selectedCategory, notes)
                    }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colors.background,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Save expense",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.background
                            )
                        }
                    }
                }
            }
        }
    }
}
