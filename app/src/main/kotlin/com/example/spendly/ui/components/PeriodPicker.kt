package com.example.spendly.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme
import java.time.Month
import java.time.YearMonth
import java.util.Locale
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Drops down from a month pill: a year switcher over the twelve months. Months after the
 * current one can't be picked, since nothing has been spent in them yet.
 */
@Composable
fun MonthPickerMenu(
    expanded: Boolean,
    selected: YearMonth,
    onDismiss: () -> Unit,
    onSelect: (YearMonth) -> Unit
) {
    val colors = SpendlyTheme.colors
    val current = YearMonth.now()
    var year by remember(expanded, selected) { mutableIntStateOf(selected.year) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = colors.popup,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .width(264.dp)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { year-- }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous year", tint = colors.textSecondary)
                }
                Text(
                    text = year.toString(),
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                IconButton(onClick = { year++ }, enabled = year < current.year) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next year",
                        tint = if (year < current.year) colors.textSecondary else colors.textMuted.copy(alpha = 0.4f)
                    )
                }
            }

            Month.entries.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { month ->
                        val option = YearMonth.of(year, month)
                        val isSelected = option == selected
                        val isFuture = option.isAfter(current)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) colors.primary else Color.Transparent)
                                .clickable(enabled = !isFuture) { onSelect(option) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = when {
                                    isSelected -> colors.onPrimary
                                    isFuture -> colors.textMuted.copy(alpha = 0.4f)
                                    else -> colors.textPrimary
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** The 7 days ending [offset] weeks before [today]; offset 0 is the last 7 days. */
fun weekRange(offset: Int, today: LocalDate = LocalDate.now()): Pair<LocalDate, LocalDate> {
    val end = today.minusWeeks(offset.toLong())
    return end.minusDays(6) to end
}

/** "Last 7 days", then ranges like "Oct 22 – 28" or "Sep 28 – Oct 4". */
fun weekLabel(offset: Int, today: LocalDate = LocalDate.now()): String {
    if (offset == 0) return "Last 7 days"
    val (start, end) = weekRange(offset, today)
    val monthDay = DateTimeFormatter.ofPattern("MMM d")
    val range = if (start.month == end.month) {
        "${start.format(monthDay)} – ${end.dayOfMonth}"
    } else {
        "${start.format(monthDay)} – ${end.format(monthDay)}"
    }
    return if (end.year != today.year) "$range, ${end.year}" else range
}

/** Drops down from the period pill in Weekly mode: the last twelve weeks, newest first. */
@Composable
fun WeekPickerMenu(
    expanded: Boolean,
    selectedOffset: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val colors = SpendlyTheme.colors
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = colors.popup,
        border = BorderStroke(1.dp, colors.border)
    ) {
        repeat(12) { offset ->
            val isSelected = offset == selectedOffset
            DropdownMenuItem(
                text = {
                    Text(
                        text = weekLabel(offset),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) colors.primary else colors.textPrimary
                    )
                },
                onClick = { onSelect(offset) },
                trailingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = "Selected", tint = colors.primary) }
                } else {
                    null
                }
            )
        }
    }
}
