package com.example.spendly.ui.components

import androidx.compose.foundation.background
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.ui.theme.CalendarToday
import com.example.spendly.ui.theme.Motion
import com.example.spendly.ui.theme.PieChart
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.Tune
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

enum class StitchNavDestination {
    HOME, CALENDAR, INSIGHTS, SETTINGS
}

/** How far the fade behind the dock rises above it. */
private val DockFadeRise = 24.dp

/**
 * Room a scrolling screen leaves at its end, above the navigation bar: the dock's height and a
 * small gap, so the last item stops just above the dock with nothing empty to scroll into.
 */
val DockClearance = 96.dp

@Composable
fun StitchBottomDock(
    currentDestination: StitchNavDestination,
    onNavigate: (StitchNavDestination) -> Unit,
    // Gets the + button's centre on screen, so Add expense can grow out of it
    onFabClick: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SpendlyTheme.colors
    val tabBounds = remember { mutableStateMapOf<StitchNavDestination, Rect>() }
    // Only read when the + is tapped, so updating it never recomposes the dock
    var fabCenter by remember { mutableStateOf(Offset.Unspecified) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            // Content fades out above the dock instead of sliding under it
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.45f to colors.background.copy(alpha = 0.88f),
                    0.7f to colors.background
                )
            )
            .padding(top = DockFadeRise)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating pill in the tiles' colour, with a hairline edge
        GlassSurface(
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.dockBorder, CircleShape),
            tint = colors.dockBackground
        ) {
            // The selected tab's pill, in a layer of its own under the tabs so the goo effect
            // that lets it move like liquid never touches the icons
            LiquidPill(
                target = tabBounds[currentDestination],
                color = colors.dockPill,
                inset = 6.dp,
                modifier = Modifier.matchParentSize()
            )
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
                    modifier = Modifier
                        .weight(1f)
                        .onPlaced { tabBounds[StitchNavDestination.HOME] = it.boundsInParent() }
                )

                // Tab 2: Calendar
                DockItem(
                    label = "Calendar",
                    icon = Icons.Filled.CalendarToday,
                    isSelected = currentDestination == StitchNavDestination.CALENDAR,
                    onClick = { onNavigate(StitchNavDestination.CALENDAR) },
                    modifier = Modifier
                        .weight(1f)
                        .onPlaced { tabBounds[StitchNavDestination.CALENDAR] = it.boundsInParent() }
                )

                // Center add action: the green disc, the one bright control in the dock; Add
                // expense grows out of it
                Box(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val fabInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .onGloballyPositioned { fabCenter = it.boundsInRoot().center }
                            .glassPress(fabInteraction)
                            .clip(CircleShape)
                            .background(colors.dockAction)
                            .clickable(interactionSource = fabInteraction, indication = null) { onFabClick(fabCenter) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Expense",
                            tint = colors.onDockAction,
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
                    modifier = Modifier
                        .weight(1f)
                        .onPlaced { tabBounds[StitchNavDestination.INSIGHTS] = it.boundsInParent() }
                )

                // Tab 4: Settings
                DockItem(
                    label = "Settings",
                    icon = Icons.Filled.Tune,
                    isSelected = currentDestination == StitchNavDestination.SETTINGS,
                    onClick = { onNavigate(StitchNavDestination.SETTINGS) },
                    modifier = Modifier
                        .weight(1f)
                        .onPlaced { tabBounds[StitchNavDestination.SETTINGS] = it.boundsInParent() }
                )
            }
        }
    }
}

/**
 * The selected tab's pill. Moving to another tab it stretches like liquid: the leading edge runs
 * ahead, the trailing edge catches up, and a drop pulls out behind it as a tail and is drawn back
 * in. [target] is in the tab row's coordinates, which start [inset] inside this layer. The goo
 * effect needs Android 12+; earlier phones get the stretch without the tail.
 */
@Composable
private fun LiquidPill(target: Rect?, color: Color, inset: Dp, modifier: Modifier) {
    if (target == null) return
    // The first bounds snap into place; later selections flow there
    val left = remember { Animatable(target.left) }
    val right = remember { Animatable(target.right) }
    // How far through a move the tail is: it pulls out and comes back, so it's out only mid-move
    val tail = remember { Animatable(1f) }
    var trailingLeft by remember { mutableStateOf(true) }
    LaunchedEffect(target) {
        val forward = target.center.x > (left.value + right.value) / 2
        trailingLeft = forward
        val (lead, trail) = if (forward) right to left else left to right
        launch { lead.animateTo(if (forward) target.right else target.left, tween(220, easing = Motion.MoveEasing)) }
        launch { trail.animateTo(if (forward) target.left else target.right, tween(300, delayMillis = 60, easing = Motion.MoveEasing)) }
        launch {
            tail.snapTo(0f)
            tail.animateTo(1f, tween(380, easing = LinearEasing))
        }
    }
    val density = LocalDensity.current
    val goo = remember(density) { gooEffect(with(density) { 8.dp.toPx() }) }
    Canvas(modifier.graphicsLayer { renderEffect = goo }) {
        val offset = inset.toPx()
        val width = right.value - left.value
        // A little flatter while stretched
        val stretch = ((width - target.width) / target.width).coerceIn(0f, 1f)
        val height = target.height * (1f - 0.18f * stretch)
        val top = offset + target.center.y - height / 2
        drawRoundRect(color, Offset(offset + left.value, top), Size(width, height), CornerRadius(height / 2))
        // The tail's drop sits in the trailing end's curve at rest and pulls out behind it mid-move,
        // close enough that the goo keeps it joined; without goo it would only be a stray dot
        if (goo != null) {
            val pull = sin(PI * tail.value).toFloat() * (height / 2 + 14.dp.toPx())
            val dropX = if (trailingLeft) left.value + height / 2 - pull else right.value - height / 2 + pull
            drawCircle(color, radius = height * 0.24f, center = Offset(offset + dropX, offset + target.center.y))
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
    val interaction = remember { MutableInteractionSource() }
    // The dock draws the selection pill; each item only shifts its tint
    val tint by animateColorAsState(
        if (isSelected) colors.dockContentSelected else colors.dockContent,
        animationSpec = Motion.move(),
        label = "dockItemTint"
    )

    Box(
        modifier = modifier
            .glassPress(interaction)
            .clip(CircleShape)
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
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = tint,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Medium,
                letterSpacing = 0.sp
            )
        }
    }
}
