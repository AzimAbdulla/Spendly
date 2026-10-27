package com.example.spendly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating pill container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Color(0x38000000),
                    ambientColor = Color(0x38000000)
                ),
            shape = CircleShape,
            color = SpendlyTheme.colors.dockBackground.copy(alpha = 0.96f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
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

                // Center FAB Action
                Box(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .size(54.dp)
                            .offset(y = (-6).dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = CircleShape,
                                spotColor = SpendlyTheme.colors.primary.copy(alpha = 0.5f)
                            ),
                        shape = CircleShape,
                        color = SpendlyTheme.colors.primary,
                        onClick = onFabClick
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Expense",
                                tint = SpendlyTheme.colors.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
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
    val activeBg = SpendlyTheme.colors.primary.copy(alpha = 0.18f)
    val activeColor = SpendlyTheme.colors.primary
    val inactiveColor = Color(0xFFA0A0AB)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.background(activeBg)
                else Modifier
            )
            .padding(vertical = 6.dp, horizontal = 2.dp),
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
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                color = if (isSelected) activeColor else inactiveColor,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.W600 else FontWeight.W500,
                letterSpacing = 0.02.sp
            )
        }
    }
}
