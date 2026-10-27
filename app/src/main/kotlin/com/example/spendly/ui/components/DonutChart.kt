package com.example.spendly.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
    Category.SHOPPING to StitchCatShopping,
    Category.ENTERTAINMENT to StitchCatEntertainment,
    Category.UTILITIES to StitchCatUtilities,
    Category.HEALTH to StitchCatHealth,
    Category.SUBSCRIPTIONS to StitchCatSubscriptions,
    Category.TRAVEL to StitchCatTravel,
    Category.OTHER to StitchCatOther
)

val categoryColors = stitchCategoryColors

@Composable
fun DonutChart(
    categoryTotals: Map<Category, Double>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹",
    size: Dp = 180.dp,
    strokeWidth: Dp = 22.dp
) {
    val total = categoryTotals.values.sum()
    val nonZero = categoryTotals.filter { it.value > 0 }
    val trackColor = SpendlyTheme.colors.container

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
                    val gapDegrees = 3f
                    val totalGap = nonZero.size * gapDegrees
                    val availableAngle = (360f - totalGap).coerceAtLeast(0f)

                    nonZero.forEach { (category, amount) ->
                        val proportion = (amount / total).toFloat()
                        val sweep = proportion * availableAngle
                        val color = stitchCategoryColors[category] ?: StitchCatOther

                        drawArc(
                            color = color,
                            startAngle = currentStartAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                        )
                        currentStartAngle += sweep + gapDegrees
                    }
                }
            }

            // Donut Center Hub
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$currencySymbol${String.format("%,.0f", total)}",
                    color = SpendlyTheme.colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W700,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "TOTAL SPENT",
                    color = SpendlyTheme.colors.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Donut Legend Cards (Clean minimal pill rows)
        if (nonZero.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                nonZero.entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .forEach { (cat, amt) ->
                        val color = stitchCategoryColors[cat] ?: StitchCatOther
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SpendlyTheme.colors.containerLow)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                        .background(color, CircleShape)
                                )
                                Text(
                                    text = cat.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.W500,
                                    color = SpendlyTheme.colors.textPrimary
                                )
                            }
                            Text(
                                text = "$currencySymbol${String.format("%,.0f", amt)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.W600,
                                color = SpendlyTheme.colors.textPrimary
                            )
                        }
                    }
            }
        }
    }
}
