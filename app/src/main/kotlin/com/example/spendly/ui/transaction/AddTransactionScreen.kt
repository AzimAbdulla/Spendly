package com.example.spendly.ui.transaction

import com.example.spendly.ui.theme.SpendlyThemeColors
import com.example.spendly.data.model.sharing
import com.example.spendly.data.model.Sharing
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.text.style.TextOverflow
import com.example.spendly.data.currencySymbol
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionSource
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.lerp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LocalTextStyle
import com.example.spendly.ui.components.RollingText
import com.example.spendly.ui.components.glassPress
import com.example.spendly.ui.theme.Motion
import com.example.spendly.ui.theme.tint
import com.example.spendly.ui.theme.OnTintSafe
import com.example.spendly.ui.theme.TintSafe
import androidx.compose.ui.graphics.lerp

/**
 * Add expense, and the same screen for changing one: pass [editing] and it opens filled in, with
 * a delete button, and saves over that expense.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    prefillAmount: Double? = null,
    prefillCategory: Category? = null,
    initialDate: LocalDate = LocalDate.now(),
    editing: Transaction? = null,
    defaultCurrency: String = "INR",
    onSaved: (amount: Double, category: Category, item: String, date: LocalDate, sharing: Sharing) -> Unit,
    onDelete: (() -> Unit)? = null,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = SpendlyTheme.colors
    // Add expense is the green the + button opens into: a deep green screen (mint in light), its
    // fields and keys a step lighter and the amount a step lighter again. Category colours still
    // come from the app's own theme, and a few small touches take the chosen category's colour
    val moneyTint = theme.tint(TintSafe)
    val colors = theme.copy(
        background = if (theme.isDark) lerp(theme.background, TintSafe, 0.1f) else lerp(Color.White, TintSafe, 0.12f),
        container = if (theme.isDark) lerp(theme.background, TintSafe, 0.17f) else Color.White,
        textSecondary = moneyTint.secondary,
        textMuted = moneyTint.secondary.copy(alpha = 0.7f),
        primary = TintSafe,
        onPrimary = OnTintSafe
    )
    val amountWell = if (theme.isDark) lerp(theme.background, TintSafe, 0.26f) else lerp(Color.White, TintSafe, 0.24f)

    var amountStr by remember {
        mutableStateOf(
            // An amount in another currency starts empty: what it cost in yours has to be typed
            (editing?.amount?.takeIf { editing.currency == defaultCurrency } ?: prefillAmount)
                ?.let { String.format(Locale.US, "%.2f", it).removeSuffix("00").removeSuffix(".") } ?: "0"
        )
    }
    var selectedCategory by remember { mutableStateOf(editing?.category ?: prefillCategory ?: Category.FOOD) }
    var date by remember { mutableStateOf(editing?.date ?: initialDate) }
    var pickingDate by remember { mutableStateOf(false) }
    var sharing by remember { mutableStateOf(editing?.sharing ?: Sharing()) }
    var pickingSharing by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    LaunchedEffect(saved) {
        if (saved) {
            delay(SavedHold)
            onCancel()
        }
    }
    val categoryAccent by animateColorAsState(
        theme.tint(stitchCategoryColors[selectedCategory] ?: theme.textMuted).accent,
        animationSpec = Motion.move(Motion.Short),
        label = "categoryAccent"
    )
    var item by remember { mutableStateOf(editing?.item.orEmpty()) }

    // Height the screen doesn't need, measured at the spacer above the keypad; up to
    // MaxAmountRoom of it goes to the amount
    var spare by remember(LocalConfiguration.current.screenHeightDp) { mutableStateOf(0.dp) }
    val amountRoom = spare.coerceAtMost(MaxAmountRoom)
    // On a shorter phone the screen must still fit without scrolling: whatever doesn't fit is
    // taken back, evenly, from the keys, the gaps and the padding (see FillOrScrollColumn)
    var squeeze by remember(LocalConfiguration.current.screenHeightDp) { mutableStateOf(0.dp) }
    val tight = squeeze / MaxSqueeze
    val density = LocalDensity.current

    // The amount grows by up to half again into that room, and shrinks smoothly when it's too long
    // for the well, so it never runs off the screen
    var amountWidth by remember { mutableIntStateOf(0) }
    var amountSpace by remember { mutableIntStateOf(0) }
    val amountScale by animateFloatAsState(
        targetValue = minOf(
            1f + amountRoom / MaxAmountRoom * 0.5f,
            // a little short of the edges, so even the longest amount has room around it
            if (amountWidth > 0 && amountSpace > 0) amountSpace * 0.92f / amountWidth else Float.MAX_VALUE
        ),
        animationSpec = Motion.move(Motion.Short),
        label = "amountScale"
    )

    val currencySymbol = currencySymbol(defaultCurrency)

    val parsedAmount = amountStr.toDoubleOrNull() ?: 0.0

    if (pickingSharing) {
        WhoIsThisForSheet(
            current = sharing,
            amount = parsedAmount,
            currencySymbol = currencySymbol,
            onDone = {
                sharing = it
                pickingSharing = false
            },
            onDismiss = { pickingSharing = false }
        )
    }

    if (pickingDate) {
        // The picker works in UTC days; an expense can't be dated in the future
        val endOfToday = LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis < endOfToday
                override fun isSelectableYear(year: Int) = year <= LocalDate.now().year
            }
        )
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                    pickingDate = false
                }) { Text("Done") }
            },
            dismissButton = { TextButton(onClick = { pickingDate = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = pickerState, showModeToggle = false)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = colors.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        FillOrScrollColumn(
            onOverflow = { over -> squeeze = (squeeze + over).coerceAtMost(MaxSqueeze) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(lerp(12.dp, 6.dp, tight))
        ) {
            // Header Row: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editing == null) "Add expense" else "Edit expense",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete expense",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
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

            // Item: what was bought, the main detail of an expense. The placeholder is its label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.container)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = categoryAccent,
                    modifier = Modifier.size(18.dp)
                )
                BasicTextField(
                    value = item,
                    onValueChange = { item = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    ),
                    cursorBrush = SolidColor(colors.primary),
                    decorationBox = { innerTextField ->
                        if (item.isEmpty()) {
                            Text(
                                text = "Item, e.g. Masala dosa",
                                fontSize = 14.sp,
                                color = colors.textSecondary.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // When it was spent, and for an SMS expense who was paid
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val today = LocalDate.now()
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { pickingDate = true }
                        .background(colors.container)
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = "Date",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = when (date) {
                            today -> "Today"
                            today.minusDays(1) -> "Yesterday"
                            else -> date.format(DateTimeFormatter.ofPattern(if (date.year == today.year) "EEE, d MMM" else "d MMM yyyy"))
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
                // Who it's for: just me, a split, owed back or paid back. Opens WhoIsThisForSheet
                SharingChip(
                    sharing = sharing,
                    currencySymbol = currencySymbol,
                    colors = colors,
                    onClick = { pickingSharing = true }
                )
                val payee = editing?.merchant?.takeIf { it.isNotBlank() && it != "Unknown Merchant" }
                val share = sharing.myShare
                val origin = when {
                    share != null && parsedAmount > 0 && share >= parsedAmount -> "Your share is more than the bill"
                    editing == null -> null
                    editing.currency != defaultCurrency -> "Charged ${editing.currency} ${String.format("%,.2f", editing.amount)}. Enter what it cost you"
                    payee != null -> "Paid to $payee"
                    editing.source == TransactionSource.SMS -> "From a bank SMS"
                    else -> null
                }
                if (origin != null) {
                    Text(
                        text = origin,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Amount Hero Input Section (OLED Dark Well): taller when the screen has room to spare
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(amountWell)
                    .border(1.dp, colors.border, RoundedCornerShape(24.dp))
                    .padding(vertical = lerp(14.dp, 6.dp, tight) + amountRoom / 2, horizontal = 12.dp)
                    .onSizeChanged { amountSpace = it.width },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Amount being typed, measured at its full width and scaled to [amountScale];
                // scaling leaves the layout alone, so the room stays exact
                Row(
                    modifier = Modifier
                        .wrapContentWidth(unbounded = true)
                        .onSizeChanged { amountWidth = it.width }
                        .graphicsLayer {
                            scaleX = amountScale
                            scaleY = amountScale
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currencySymbol,
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Medium,
                        color = moneyTint.secondary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    // Digits roll up into place as they're typed
                    RollingText(
                        text = if (amountStr.isEmpty()) "0" else amountStr,
                        style = LocalTextStyle.current.merge(
                            TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                letterSpacing = (-0.8).sp
                            )
                        )
                    )
                    if (!amountStr.contains(".")) Text(
                        text = ".00",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = moneyTint.secondary.copy(alpha = 0.7f)
                    )
                    // Caret
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .width(2.dp)
                            .height(28.dp)
                            .background(moneyTint.accent, CircleShape)
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
                        fontWeight = FontWeight.Medium,
                        color = categoryAccent
                    )
                }

                // Every category, 5 per row (two rows for all 10)
                Category.entries.chunked(5).forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowCategories.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        val catTint = theme.tint(stitchCategoryColors[cat] ?: theme.textMuted)
                        val tileInteraction = remember { MutableInteractionSource() }
                        val tileFill by animateColorAsState(
                            if (isSelected) catTint.background else colors.container,
                            animationSpec = Motion.move(Motion.Short),
                            label = "tileFill"
                        )
                        val tileBorder by animateColorAsState(
                            if (isSelected) catTint.accent else colors.border,
                            animationSpec = Motion.move(Motion.Short),
                            label = "tileBorder"
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .glassPress(tileInteraction)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable(
                                    interactionSource = tileInteraction,
                                    indication = LocalIndication.current
                                ) { selectedCategory = cat }
                                .background(tileFill)
                                .border(1.dp, tileBorder, RoundedCornerShape(18.dp))
                                .padding(vertical = lerp(8.dp, 4.dp, tight), horizontal = 2.dp),
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
                                    tint = if (isSelected) catTint.accent else colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = cat.shortName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) catTint.accent else colors.textSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
                }
            }

            // Spare height goes here, keeping the keypad and Save at the bottom within thumb reach;
            // measuring it tells the amount how much room it can take
            Spacer(
                Modifier
                    .weight(1f)
                    .onSizeChanged { spare = amountRoom + with(density) { it.height.toDp() } }
            )

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
                            val keyInteraction = remember { MutableInteractionSource() }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(lerp(46.dp, 36.dp, tight))
                                    .glassPress(keyInteraction),
                                shape = RoundedCornerShape(14.dp),
                                color = colors.container,
                                border = BorderStroke(1.dp, colors.border),
                                interactionSource = keyInteraction,
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
                                            // Up to nine digits and two decimals: plenty for an
                                            // expense, and the figure stays readable
                                            val decimals = amountStr.substringAfter(".", missingDelimiterValue = "")
                                            when {
                                                amountStr == "0" -> amountStr = key
                                                amountStr.contains(".") -> if (decimals.length < 2) amountStr += key
                                                amountStr.length < 9 -> amountStr += key
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
                    shape = RoundedCornerShape(14.dp),
                    color = colors.container,
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

                // Save: the primary action, mint fill with dark text. Once tapped it folds into a
                // tick, and the screen closes a moment later
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(0.65f)
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val fold by animateFloatAsState(if (saved) 1f else 0f, Motion.move(), label = "saveFold")
                    // Nothing to save until there's an amount: Save stays dimmed and ignores taps
                    // A split needs a share above nothing and below the bill
                    val canSave = parsedAmount > 0 && sharing.myShare.let { it == null || (it > 0 && it < parsedAmount) }
                    val saveAlpha by animateFloatAsState(if (canSave || saved) 1f else 0.4f, Motion.move(Motion.Short), label = "saveAlpha")
                    Surface(
                        modifier = Modifier
                            .width(lerp(maxWidth, 48.dp, fold))
                            .height(48.dp)
                            .alpha(saveAlpha),
                        enabled = canSave || saved,
                        shape = RoundedCornerShape(lerp(14.dp, 24.dp, fold)),
                        color = colors.primary,
                        onClick = {
                            if (!saved && canSave) {
                                saved = true
                                // Saved straight away, so leaving early never loses the expense
                                onSaved(parsedAmount, selectedCategory, item.trim(), date, sharing)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colors.onPrimary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer {
                                        scaleX = 1f + 0.4f * fold
                                        scaleY = 1f + 0.4f * fold
                                    }
                            )
                            AnimatedVisibility(
                                visible = !saved,
                                exit = fadeOut(Motion.exit()) + shrinkHorizontally(Motion.move())
                            ) {
                                Text(
                                    text = if (editing == null) "Save expense" else "Save changes",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onPrimary,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** How long the tick shows before Add expense closes. */
private const val SavedHold = 480L

/**
 * The most height the screen gives back to fit a short phone: 10dp from each of four key rows,
 * 6dp from each of six gaps, 16dp from the amount's padding and 16dp from the category tiles.
 */
private val MaxSqueeze = 108.dp

/** The most spare height the amount takes; the rest stays above the keypad. */
private val MaxAmountRoom = 120.dp

/** Fills the available height, and scrolls only when the content can't fit even fully tightened (landscape, large text). */
@Composable
private fun FillOrScrollColumn(
    // Told how much taller than the screen the content came out, so the screen can tighten itself
    onOverflow: (Dp) -> Unit,
    modifier: Modifier,
    verticalArrangement: Arrangement.Vertical,
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .onSizeChanged { size ->
                    val over = with(density) { size.height.toDp() } - maxHeight
                    if (over > 0.dp) onOverflow(over + 1.dp)
                },
            verticalArrangement = verticalArrangement,
            content = content
        )
    }
}

/** Money without decimals when it's a whole amount, as people say it. */
private fun money(symbol: String, amount: Double): String =
    symbol + String.format(if (amount % 1.0 == 0.0) "%,.0f" else "%,.2f", amount)

/** The chip beside the date: says who the expense is for, in that answer's colour. */
@Composable
private fun SharingChip(sharing: Sharing, currencySymbol: String, colors: SpendlyThemeColors, onClick: () -> Unit) {
    val share = sharing.myShare
    val (label, fill, ink) = when {
        share != null -> Triple("Split \u00B7 my share ${money(currencySymbol, share)}", lerp(colors.container, colors.primary, 0.3f), colors.textPrimary)
        sharing.owed -> Triple("Owed back", colors.tertiary.copy(alpha = 0.22f), colors.tertiary)
        sharing.paidBack -> Triple("Paid back", colors.primary, colors.onPrimary)
        else -> Triple("Just me", colors.container, colors.textPrimary)
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClickLabel = "Choose who this is for", onClick = onClick)
            .background(fill)
            .border(1.dp, if (sharing.isJustMe) colors.border else Color.Transparent, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (sharing.paidBack) {
            Icon(Icons.Default.Check, contentDescription = null, tint = ink, modifier = Modifier.size(16.dp))
        }
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ink, maxLines = 1)
        if (sharing.isJustMe) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

private enum class ShareChoice { JUST_ME, SPLIT, OWED, PAID_BACK }

/**
 * "Who is this for?": the four answers, with a field for my share when the bill is split. An
 * expense that's still owed gets "They've paid me back" on top, which settles it in one tap.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhoIsThisForSheet(
    current: Sharing,
    amount: Double,
    currencySymbol: String,
    onDone: (Sharing) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SpendlyTheme.colors
    var choice by remember {
        mutableStateOf(
            when {
                current.isSplit -> ShareChoice.SPLIT
                current.owed -> ShareChoice.OWED
                current.paidBack -> ShareChoice.PAID_BACK
                else -> ShareChoice.JUST_ME
            }
        )
    }
    var shareText by remember {
        mutableStateOf(current.myShare?.let { String.format(Locale.US, "%.2f", it).removeSuffix("00").removeSuffix(".") } ?: "")
    }
    val share = shareText.toDoubleOrNull()
    val shareOk = share != null && share > 0 && (amount <= 0 || share < amount)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.popup
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Who is this for?",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            if (current.owed) {
                Text(
                    text = "They've paid me back",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnTintSafe,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onDone(current.settled()) }
                        .background(TintSafe)
                        .padding(vertical = 14.dp)
                )
            }
            ShareOption("Just me", "All of it is my spending", choice == ShareChoice.JUST_ME, onSelect = { choice = ShareChoice.JUST_ME })
            ShareOption("Split the bill", "Only my share counts", choice == ShareChoice.SPLIT, onSelect = { choice = ShareChoice.SPLIT }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("My share", fontSize = 14.sp, color = colors.textSecondary)
                    Row(
                        modifier = Modifier
                            .width(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.background)
                            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$currencySymbol ", fontFamily = SpaceGroteskFontFamily, fontSize = 18.sp, color = colors.textSecondary)
                        BasicTextField(
                            value = shareText,
                            onValueChange = { typed ->
                                if (typed.isEmpty() || Regex("^\\d{0,9}(\\.\\d{0,2})?$").matches(typed)) shareText = typed
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 18.sp, color = colors.textPrimary),
                            cursorBrush = SolidColor(colors.textPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Text(
                    text = when {
                        share == null || share <= 0 -> "Enter what your part cost"
                        amount <= 0 -> "Then enter the whole bill as the amount"
                        share >= amount -> "Your share has to be less than the bill"
                        else -> "Others owe you ${money(currencySymbol, amount - share)}"
                    },
                    fontSize = 13.sp,
                    color = if (shareOk) colors.tertiary else colors.textSecondary
                )
            }
            ShareOption("They'll pay me back", "Counts until they do", choice == ShareChoice.OWED, onSelect = { choice = ShareChoice.OWED })
            ShareOption("Already paid back", "Kept on record, not counted", choice == ShareChoice.PAID_BACK, onSelect = { choice = ShareChoice.PAID_BACK })

            val canFinish = choice != ShareChoice.SPLIT || shareOk
            Text(
                text = "Done",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .alpha(if (canFinish) 1f else 0.4f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = canFinish) {
                        onDone(
                            when (choice) {
                                ShareChoice.JUST_ME -> Sharing()
                                // A new split is owed until the others pay; an existing one keeps its state
                                ShareChoice.SPLIT -> Sharing(owed = if (current.isSplit) current.owed else true, myShare = share)
                                ShareChoice.OWED -> Sharing(owed = true)
                                ShareChoice.PAID_BACK -> Sharing(paidBack = true)
                            }
                        )
                    }
                    .background(colors.primary)
                    .padding(vertical = 14.dp)
            )
        }
    }
}

/** One answer in the sheet: a radio, its name and what it means, and anything it reveals when picked. */
@Composable
private fun ShareOption(
    title: String,
    detail: String,
    selected: Boolean,
    onSelect: () -> Unit,
    extra: (@Composable ColumnScope.() -> Unit)? = null
) {
    val colors = SpendlyTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .background(if (selected) colors.glassSelected else colors.container)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            RadioButton(
                selected = selected,
                onClick = onSelect,
                modifier = Modifier.size(20.dp),
                colors = RadioButtonDefaults.colors(selectedColor = colors.textPrimary, unselectedColor = colors.textSecondary)
            )
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Text(detail, fontSize = 13.sp, color = colors.textSecondary)
            }
        }
        if (selected && extra != null) {
            Column(
                modifier = Modifier.padding(start = 34.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = extra
            )
        }
    }
}
