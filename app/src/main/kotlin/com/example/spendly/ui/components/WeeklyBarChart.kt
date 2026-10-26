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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.ui.theme.StitchContainer
import com.example.spendly.ui.theme.StitchOnPrimary
import com.example.spendly.ui.theme.StitchOnSurfaceVariant
import com.example.spendly.ui.theme.StitchPrimary

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

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        values.zip(labels).forEachIndexed { index, (value, label) ->
            val fraction = (value / maxVal).toFloat().coerceIn(0f, 1f)
            val isToday = index == todayIndex
            val shortDay = label.take(1)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // Floating tooltip over today
                if (isToday && todayAmount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StitchPrimary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$currencySymbol${String.format("%,.0f", todayAmount)}",
                            color = StitchOnPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.W700,
                            letterSpacing = (-0.2).sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                } else {
                    Spacer(Modifier.height(18.dp))
                }

                // Bar capsule
                Canvas(
                    modifier = Modifier
                        .width(22.dp)
                        .height(72.dp)
                ) {
                    // Inactive full capsule track
                    drawRoundRect(
                        color = StitchContainer,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(size.width / 2, size.width / 2)
                    )

                    // Filled bar
                    val fillHeight = (size.height * fraction).coerceAtLeast(0f)
                    val top = size.height - fillHeight
                    val color = if (isToday) StitchPrimary else StitchPrimary.copy(alpha = 0.45f)

                    if (fillHeight > 0) {
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(0f, top),
                            size = Size(size.width, fillHeight),
                            cornerRadius = CornerRadius(size.width / 2, size.width / 2)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Single letter day label
                Text(
                    text = shortDay,
                    fontSize = 11.sp,
                    fontWeight = if (isToday) FontWeight.W700 else FontWeight.W500,
                    color = if (isToday) StitchPrimary else StitchOnSurfaceVariant
                )
            }
        }
    }
}
