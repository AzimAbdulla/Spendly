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
import com.example.spendly.ui.theme.ActiveBarGradient
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
    val primaryColor = colors.primary
    val trackColor = if (colors.isDark) Color(0xFF1A1A22) else colors.containerHigh

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
            val shortDay = label.take(1).uppercase()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // Floating tooltip over today
                if (isToday && todayAmount > 0) {
                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(4.dp),
                                spotColor = Color(0x669E8CFC)
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .background(primaryColor)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$currencySymbol${String.format("%,.0f", todayAmount)}",
                            fontFamily = SpaceGroteskFontFamily,
                            color = colors.onPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                } else {
                    Spacer(Modifier.height(18.dp))
                }

                // Capsule bar (height 80dp)
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(80.dp)
                        .clip(CircleShape)
                        .background(if (isToday) primaryColor.copy(alpha = 0.2f) else trackColor)
                        .then(
                            if (isToday) Modifier.border(1.dp, primaryColor.copy(alpha = 0.4f), CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val fillHeight = (76 * fraction).coerceAtLeast(0f).dp
                    if (fillHeight > 0.dp) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(fillHeight)
                                .clip(CircleShape)
                                .then(
                                    if (isToday) {
                                        Modifier
                                            .background(ActiveBarGradient)
                                            .shadow(8.dp, CircleShape, spotColor = Color(0x999E8CFC))
                                    } else {
                                        Modifier.background(primaryColor.copy(alpha = 0.35f))
                                    }
                                )
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Day Label
                Text(
                    text = shortDay,
                    fontSize = 11.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = if (isToday) primaryColor else colors.textSecondary
                )
            }
        }
    }
}
