package com.example.spendly.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Category
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.StitchCatEntertainment
import com.example.spendly.ui.theme.StitchCatFood
import com.example.spendly.ui.theme.StitchCatGroceries
import com.example.spendly.ui.theme.StitchCatHealth
import com.example.spendly.ui.theme.StitchCatOther
import com.example.spendly.ui.theme.StitchCatShopping
import com.example.spendly.ui.theme.StitchCatSubscriptions
import com.example.spendly.ui.theme.StitchCatTransport
import com.example.spendly.ui.theme.StitchCatTravel
import com.example.spendly.ui.theme.StitchCatUtilities

val stitchCategoryColors = mapOf(
    Category.FOOD to StitchCatFood,
    Category.GROCERIES to StitchCatGroceries,
    Category.TRANSPORT to StitchCatTransport,
    Category.ENTERTAINMENT to StitchCatEntertainment,
    Category.UTILITIES to StitchCatUtilities,
    Category.SHOPPING to StitchCatShopping,
    Category.HEALTH to StitchCatHealth,
    Category.SUBSCRIPTIONS to StitchCatSubscriptions,
    Category.TRAVEL to StitchCatTravel,
    Category.OTHER to StitchCatOther
)

@Composable
fun DonutChart(
    categoryTotals: Map<Category, Double>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹",
    size: Dp = 170.dp,
    strokeWidth: Dp = 20.dp
) {
    val colors = SpendlyTheme.colors
    val total = categoryTotals.values.sum()
    val nonZero = categoryTotals.filter { it.value > 0 }.entries.sortedByDescending { it.value }
    val trackColor = colors.containerHigh

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Centered Donut with Core Text
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                // Background track
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )

                if (total > 0 && nonZero.isNotEmpty()) {
                    var currentStartAngle = -90f
                    val gapDegrees = 4f
                    val totalGap = nonZero.size * gapDegrees
                    val availableAngle = (360f - totalGap).coerceAtLeast(0f)

                    nonZero.forEach { (category, amount) ->
                        val proportion = (amount / total).toFloat()
                        val sweep = proportion * availableAngle
                        val catColor = stitchCategoryColors[category] ?: StitchCatOther

                        if (sweep > 0f) {
                            drawArc(
                                color = catColor,
                                startAngle = currentStartAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                            )
                            currentStartAngle += sweep + gapDegrees
                        }
                    }
                }
            }

            // Core center info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$currencySymbol${String.format("%,.2f", total)}",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                )
                Text(
                    text = "Spent",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.textSecondary,
                    letterSpacing = 0.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Donut Legend Rows
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            val legendItems = if (nonZero.isNotEmpty()) nonZero.take(5) else emptyList()
            legendItems.forEach { (cat, amt) ->
                val catColor = stitchCategoryColors[cat] ?: StitchCatOther
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(catColor, CircleShape)
                        )
                        Text(
                            text = cat.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = colors.textPrimary
                        )
                    }
                    Text(
                        text = "$currencySymbol${String.format("%,.2f", amt)}",
                        fontFamily = SpaceGroteskFontFamily,
                        style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}
