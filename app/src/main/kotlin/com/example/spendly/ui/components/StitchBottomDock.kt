package com.example.spendly.ui.components

import androidx.compose.foundation.background
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.ui.theme.CalendarToday
import com.example.spendly.ui.theme.PieChart
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.Tune

enum class StitchNavDestination {
    HOME, CALENDAR, INSIGHTS, SETTINGS
}

@Composable
fun StitchBottomDock(
    currentDestination: StitchNavDestination,
    onNavigate: (StitchNavDestination) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SpendlyTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating glass pill: blurs the screen scrolling underneath
        GlassSurface(
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Home
                DockItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    isSelected = currentDestination == StitchNavDestination.HOME,
                    onClick = { onNavigate(StitchNavDestination.HOME) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 2: Calendar
                DockItem(
                    label = "Calendar",
                    icon = Icons.Filled.CalendarToday,
                    isSelected = currentDestination == StitchNavDestination.CALENDAR,
                    onClick = { onNavigate(StitchNavDestination.CALENDAR) },
                    modifier = Modifier.weight(1f)
                )

                // Center add action: off-white disc, the one bright control in the dock
                Box(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val fabInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .glassPress(fabInteraction)
                            .clip(CircleShape)
                            .background(colors.textPrimary)
                            .clickable(interactionSource = fabInteraction, indication = null, onClick = onFabClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Expense",
                            tint = colors.background,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Tab 3: Insights
                DockItem(
                    label = "Insights",
                    icon = Icons.Filled.PieChart,
                    isSelected = currentDestination == StitchNavDestination.INSIGHTS,
                    onClick = { onNavigate(StitchNavDestination.INSIGHTS) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 4: Settings
                DockItem(
                    label = "Settings",
                    icon = Icons.Filled.Tune,
                    isSelected = currentDestination == StitchNavDestination.SETTINGS,
                    onClick = { onNavigate(StitchNavDestination.SETTINGS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DockItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SpendlyTheme.colors
    val activeColor = colors.primary
    val inactiveColor = colors.textPrimary
    val interaction = remember { MutableInteractionSource() }
    val activeBg by animateColorAsState(
        if (isSelected) colors.glassSelected else Color.Transparent,
        label = "dockItemBg"
    )

    Box(
        modifier = modifier
            // Lifted glass pill, concentric with the dock so its ends follow the curve
            .glassPress(interaction)
            .clip(CircleShape)
            .background(activeBg)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = if (isSelected) activeColor else inactiveColor,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Medium,
                letterSpacing = 0.sp
            )
        }
    }
}
