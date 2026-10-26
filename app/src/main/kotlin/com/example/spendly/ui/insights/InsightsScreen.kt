package com.example.spendly.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Category
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.theme.StitchBackground
import com.example.spendly.ui.theme.StitchBorder
import com.example.spendly.ui.theme.StitchCardSurface
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
import com.example.spendly.ui.theme.StitchContainer
import com.example.spendly.ui.theme.StitchContainerLow
import com.example.spendly.ui.theme.StitchOnPrimary
import com.example.spendly.ui.theme.StitchOnSurface
import com.example.spendly.ui.theme.StitchOnSurfaceVariant
import com.example.spendly.ui.theme.StitchPrimary
import com.example.spendly.ui.theme.StitchPrimaryFixed
import com.example.spendly.ui.theme.StitchSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    categoryTotals: Map<Category, Double>,
    monthlyTotal: Double,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹",
    defaultCurrency: String = "INR"
) {
    var isMonthlySelected by remember { mutableStateOf(true) }
    val monthLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    val activeCategories = categoryTotals.filter { it.value > 0 }
    val topCategoryEntry = activeCategories.maxByOrNull { it.value }
    val topCategory = topCategoryEntry?.key ?: Category.FOOD
    val topCategoryAmount = topCategoryEntry?.value ?: 0.0
    val topCategoryPct = if (monthlyTotal > 0) ((topCategoryAmount / monthlyTotal) * 100).toInt() else 0

    val dailyAverage = if (monthlyTotal > 0) monthlyTotal / LocalDate.now().dayOfMonth else 0.0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(StitchPrimary, CircleShape)
                        )
                        Text(
                            text = "Insights",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.W700,
                            color = StitchOnSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = StitchOnSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(32.dp)
                            .background(StitchPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = StitchOnPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StitchBackground)
            )
        },
        containerColor = StitchBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Picker & Segmented Pill Switcher
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Month dropdown pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = StitchContainer,
                        modifier = Modifier.clickable {}
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🗓", fontSize = 12.sp)
                            Text(
                                text = monthLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W600,
                                color = StitchOnSurfaceVariant
                            )
                            Text("▼", fontSize = 9.sp, color = StitchOnSurfaceVariant)
                        }
                    }

                    // Monthly | Weekly pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = StitchContainerLow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isMonthlySelected) StitchCardSurface else Color.Transparent)
                                    .clickable { isMonthlySelected = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Monthly",
                                    fontSize = 11.sp,
                                    fontWeight = if (isMonthlySelected) FontWeight.W700 else FontWeight.W500,
                                    color = if (isMonthlySelected) StitchOnSurface else StitchOnSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (!isMonthlySelected) StitchCardSurface else Color.Transparent)
                                    .clickable { isMonthlySelected = false }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Weekly",
                                    fontSize = 11.sp,
                                    fontWeight = if (!isMonthlySelected) FontWeight.W700 else FontWeight.W500,
                                    color = if (!isMonthlySelected) StitchOnSurface else StitchOnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Two Metric Cards Side-by-Side
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: TOTAL SPENT
                    SpendlyCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("💸", fontSize = 14.sp)
                                Text(
                                    text = "TOTAL SPENT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.W700,
                                    color = StitchOnSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = "$currencySymbol${String.format("%,.0f", monthlyTotal)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.W700,
                                color = StitchOnSurface
                            )
                            Text(
                                text = "Avg daily: $currencySymbol${String.format("%,.0f", dailyAverage)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.W500,
                                color = StitchPrimary
                            )
                        }
                    }

                    // Card 2: TOP CATEGORY
                    SpendlyCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🔥", fontSize = 14.sp)
                                Text(
                                    text = "TOP CATEGORY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.W700,
                                    color = StitchOnSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = topCategory.displayName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.W700,
                                color = StitchOnSurface,
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(StitchPrimary, CircleShape))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "$topCategoryPct% of total",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.W500,
                                    color = StitchOnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Category Breakdown Card with Horizontal Progress Bars
            item {
                SpendlyCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(StitchPrimary)
                                )
                                Text(
                                    text = "Category Breakdown",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W700,
                                    color = StitchOnSurface
                                )
                            }
                            Text(
                                text = "${activeCategories.size} active categories",
                                fontSize = 12.sp,
                                color = StitchOnSurfaceVariant
                            )
                        }

                        if (activeCategories.isEmpty()) {
                            Text(
                                text = "No category data for this period",
                                fontSize = 13.sp,
                                color = StitchOnSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            activeCategories.entries
                                .sortedByDescending { it.value }
                                .forEach { (cat, amt) ->
                                    val pct = if (monthlyTotal > 0) ((amt / monthlyTotal) * 100).toInt() else 0
                                    val catColor = when (cat) {
                                        Category.FOOD -> StitchCatFood
                                        Category.GROCERIES -> StitchCatGroceries
                                        Category.TRANSPORT -> StitchCatTransport
                                        Category.SHOPPING -> StitchCatShopping
                                        Category.ENTERTAINMENT -> StitchCatEntertainment
                                        Category.UTILITIES -> StitchCatUtilities
                                        Category.HEALTH -> StitchCatHealth
                                        Category.SUBSCRIPTIONS -> StitchCatSubscriptions
                                        Category.TRAVEL -> StitchCatTravel
                                        Category.OTHER -> StitchCatOther
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(catColor.copy(alpha = 0.15f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(cat.emoji, fontSize = 16.sp)
                                                }
                                                Column {
                                                    Text(
                                                        text = cat.displayName,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.W600,
                                                        color = StitchOnSurface
                                                    )
                                                    Text(
                                                        text = "Active spending",
                                                        fontSize = 11.sp,
                                                        color = StitchOnSurfaceVariant
                                                    )
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "$currencySymbol${String.format("%,.0f", amt)}",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.W700,
                                                    color = StitchOnSurface
                                                )
                                                Text(
                                                    text = "$pct%",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.W600,
                                                    color = StitchPrimary
                                                )
                                            }
                                        }

                                        // Horizontal Progress bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(CircleShape)
                                                .background(StitchContainer)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction = (pct / 100f).coerceIn(0.02f, 1f))
                                                    .height(6.dp)
                                                    .clip(CircleShape)
                                                    .background(catColor)
                                            )
                                        }
                                    }
                                }
                        }
                    }
                }
            }

            // Historical Outflow 6-Month Trend Card
            item {
                SpendlyCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(StitchPrimary)
                                )
                                Text(
                                    text = "Historical Outflow",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W700,
                                    color = StitchOnSurface
                                )
                            }
                            Text(
                                text = "6-Month Trend",
                                fontSize = 12.sp,
                                color = StitchOnSurfaceVariant
                            )
                        }

                        // 6-Month Bar Chart
                        val months = listOf("Aug", "Sep", "Oct", "Nov", "Dec", "Jan")
                        val monthFractions = listOf(0.45f, 0.60f, 0.40f, 0.75f, 0.85f, 1.0f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            months.zip(monthFractions).forEachIndexed { idx, (m, frac) ->
                                val isCurrent = idx == months.size - 1

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isCurrent) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(StitchPrimary)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$currencySymbol${String.format("%,.1fk", monthlyTotal / 1000)}",
                                                color = StitchOnPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.W700
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(26.dp)
                                            .height((80 * frac).dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(if (isCurrent) StitchPrimary else StitchPrimaryFixed)
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = m,
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrent) FontWeight.W700 else FontWeight.W500,
                                        color = if (isCurrent) StitchPrimary else StitchOnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Privacy guarantee card
            item {
                SpendlyCard(
                    backgroundColor = StitchContainerLow,
                    borderColor = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🛡️", fontSize = 18.sp)
                        Text(
                            text = "Amounts calculated strictly in $currencySymbol ($defaultCurrency). Offline ledger privacy maintained. Foreign card logs retain original billing receipts without speculative recalculation.",
                            fontSize = 11.sp,
                            color = StitchOnSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Bottom clearance spacer
            item {
                Spacer(Modifier.height(88.dp))
            }
        }
    }
}
