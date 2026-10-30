package com.example.spendly.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme

@Composable
fun WeeklyBarChart(
    values: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹"
) {
    val maxVal = (values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val todayIndex = (values.size - 1).coerceAtLeast(0)
    val todayAmount = values.getOrNull(todayIndex) ?: 0.0

    val colors = SpendlyTheme.colors
    val accent = colors.primary
    val barColor = colors.textMuted.copy(alpha = 0.45f)
    val baseline = colors.textPrimary.copy(alpha = 0.08f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        values.zip(labels).forEachIndexed { index, (value, label) ->
            val fraction = (value / maxVal).toFloat().coerceIn(0f, 1f)
            val isToday = index == todayIndex
            val shortDay = label.take(3).lowercase().replaceFirstChar { it.uppercase() }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // Today's figure as quiet text above its bar
                Box(modifier = Modifier.height(20.dp), contentAlignment = Alignment.BottomCenter) {
                    if (isToday && todayAmount > 0) {
                        Text(
                            text = "$currencySymbol${String.format("%,.0f", todayAmount)}",
                            fontFamily = SpaceGroteskFontFamily,
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))

                // Bar area: 80dp tall, a hairline baseline, a bar only where money was spent
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(80.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(baseline)
                    )
                    val fillHeight = (80 * fraction).coerceAtLeast(0f).dp
                    if (value > 0 && fillHeight > 0.dp) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(maxOf(fillHeight, 4.dp))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (isToday) accent else barColor)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = shortDay,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isToday) colors.textPrimary else colors.textMuted,
                    maxLines = 1
                )
            }
        }
    }
}
