package com.example.spendly.ui.components

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.example.spendly.ui.theme.Motion
import kotlin.math.hypot
import kotlin.math.max

/**
 * Grows a screen out of a circle at [origin] (the + button, in root coordinates) as it opens and
 * shrinks it back as it closes; the back gesture scrubs it. The circle starts in [flash], the
 * button's colour, and clears to the screen as it grows. Without an origin it grows from the
 * bottom centre. Use it inside a NavDisplay entry whose own transitions stay out of the way.
 */
@Composable
fun CircleReveal(origin: Offset, flash: Color, content: @Composable () -> Unit) {
    val transition = LocalNavAnimatedContentScope.current.transition
    val progress by transition.animateFloat(
        transitionSpec = {
            // Eases out of the button so the growing circle is visible, then settles
            if (targetState == EnterExitState.Visible) tween(500, easing = Motion.MoveEasing)
            else tween(320, easing = Motion.ExitEasing)
        },
        label = "circleReveal"
    ) { state -> if (state == EnterExitState.Visible) 1f else 0f }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val center = if (origin.isSpecified) origin else Offset(size.width / 2, size.height)
                // Far enough to reach the corner furthest from the button
                val reach = max(
                    max(hypot(center.x, center.y), hypot(size.width - center.x, center.y)),
                    max(hypot(center.x, size.height - center.y), hypot(size.width - center.x, size.height - center.y))
                )
                val radius = lerp(24.dp.toPx(), reach, progress)
                shape = GenericShape { _, _ -> addOval(Rect(center, radius)) }
                clip = true
            }
            .drawWithContent {
                drawContent()
                // Solid button colour while the circle is small, clearing over the rest of the way
                drawRect(flash, alpha = (1f - (progress - 0.3f) / 0.6f).coerceIn(0f, 1f))
            }
    ) {
        content()
    }
}
