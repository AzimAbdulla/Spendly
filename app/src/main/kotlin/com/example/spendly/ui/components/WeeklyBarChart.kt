package com.example.spendly.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.spendly.ui.theme.SpendlyLavender
import com.example.spendly.ui.theme.SpendlyLavenderContainer

@Composable
fun WeeklyBarChart(
    values: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = SpendlyLavender,
    inactiveColor: Color = SpendlyLavenderContainer
) {
    val maxVal = values.maxOrNull() ?: 1.0
    val todayIndex = values.indices.last

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        values.zip(labels).forEachIndexed { index, (value, label) ->
            val fraction = (value / maxVal).toFloat()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(24.dp)
                        .height(80.dp)
                ) {
                    val barHeight = size.height * fraction
                    val top = size.height - barHeight
                    drawRoundRect(
                        color = inactiveColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                    drawRoundRect(
                        color = if (index == todayIndex) barColor else barColor.copy(alpha = 0.65f),
                        topLeft = Offset(0f, top),
                        size = Size(size.width, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == todayIndex)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
