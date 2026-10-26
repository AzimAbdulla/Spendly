package com.example.spendly.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.spendly.data.model.Category
import com.example.spendly.ui.theme.SpendlyAmber
import com.example.spendly.ui.theme.SpendlyLavender
import com.example.spendly.ui.theme.SpendlySage
import com.example.spendly.ui.theme.SpendlySubtle

val categoryColors = mapOf(
    Category.FOOD to SpendlyLavender,
    Category.TRANSPORT to SpendlySage,
    Category.SHOPPING to SpendlyAmber,
    Category.ENTERTAINMENT to Color(0xFF8BA4C7),
    Category.HEALTH to Color(0xFFC77F8B),
    Category.UTILITIES to Color(0xFF7BC7B8),
    Category.SUBSCRIPTIONS to Color(0xFFC7A87B),
    Category.TRAVEL to Color(0xFFB07BC7),
    Category.OTHER to SpendlySubtle
)

@Composable
fun DonutChart(
    categoryTotals: Map<Category, Double>,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 22.dp
) {
    val total = categoryTotals.values.sum()
    if (total == 0.0) return

    val sortedEntries = categoryTotals.entries.sortedByDescending { it.value }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                var startAngle = -90f
                val gap = 3f
                sortedEntries.forEach { (category, value) ->
                    val sweep = ((value / total) * 360f).toFloat() - gap
                    drawArc(
                        color = categoryColors[category] ?: SpendlySubtle,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    )
                    startAngle += (value / total * 360f).toFloat()
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\$${String.format("%.0f", total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W700
                )
                Text(
                    text = "total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Legend — top 4 categories
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
        ) {
            sortedEntries.take(4).forEach { (category, value) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = CircleShape,
                        color = categoryColors[category] ?: SpendlySubtle
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "\$${String.format("%.0f", value)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.W600,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
