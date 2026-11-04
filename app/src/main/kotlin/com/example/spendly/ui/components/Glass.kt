package com.example.spendly.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.ui.theme.SpendlyTheme

// =========================================================================
// Liquid glass primitives (after glisse.kelvinzhang.com)
// Content is recorded into a GraphicsLayer; floating glass redraws the part
// behind it through a blur (Android 12+), with a translucent fill fallback.
// =========================================================================

class GlassBackdrop internal constructor(internal val layer: GraphicsLayer) {
    internal var origin by mutableStateOf(Offset.Zero)
}

@Composable
fun rememberGlassBackdrop(): GlassBackdrop {
    val layer = rememberGraphicsLayer()
    return remember(layer) { GlassBackdrop(layer) }
}

/** The app-wide backdrop that floating glass (dock, overlays) samples. */
val LocalGlassBackdrop = staticCompositionLocalOf<GlassBackdrop?> { null }

/** Records this node's content so glass placed above it can blur it. */
fun Modifier.glassBackdropSource(backdrop: GlassBackdrop): Modifier = this
    .onGloballyPositioned { backdrop.origin = it.positionInRoot() }
    .drawWithContent {
        backdrop.layer.record { this@drawWithContent.drawContent() }
        drawLayer(backdrop.layer)
    }

/** Bright top edge fading out toward the bottom, like light caught on a glass rim. */
@Composable
fun glassRimBrush(): Brush {
    val colors = SpendlyTheme.colors
    return Brush.verticalGradient(listOf(colors.glassRimTop, colors.glassRimBottom))
}

/**
 * Frosted glass that blurs whatever sits behind it in [backdrop].
 * Must not be drawn inside the backdrop's own source node.
 */
@Composable
fun GlassSurface(
    shape: Shape,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = LocalGlassBackdrop.current,
    blurRadius: Dp = 28.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = SpendlyTheme.colors
    val canBlur = backdrop != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    var origin by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .onGloballyPositioned { origin = it.positionInRoot() }
            .clip(shape)
    ) {
        if (canBlur && backdrop != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        val r = blurRadius.toPx()
                        renderEffect = BlurEffect(r, r, TileMode.Clamp)
                        clip = true
                    }
                    .drawBehind {
                        val offset = backdrop.origin - origin
                        translate(offset.x, offset.y) { drawLayer(backdrop.layer) }
                    }
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(if (canBlur) colors.glassTint else colors.glassFallback)
                .border(1.dp, glassRimBrush(), shape)
        )
        content()
    }
}

/** Soft color field behind the whole app, so translucent glass has something to catch. */
@Composable
fun GlassWallpaper(modifier: Modifier = Modifier) {
    val colors = SpendlyTheme.colors
    val blobs = colors.wallpaperBlobs
    Canvas(modifier = modifier.background(colors.background)) {
        val w = size.width
        val h = size.height
        val spots = listOf(
            Offset(w * 0.05f, h * 0.08f) to w * 0.95f,
            Offset(w * 1.0f, h * 0.48f) to w * 0.85f,
            Offset(w * 0.15f, h * 0.95f) to w * 0.9f
        )
        spots.forEachIndexed { i, (center, radius) ->
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(blobs[i % blobs.size], Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
    }
}

/** Press feedback: glass swells slightly under the finger, like a lens lifting. */
@Composable
fun Modifier.glassPress(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "glassPress"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Glass pill segmented control (Day / Week / Month style). */
@Composable
fun <T> GlassSegmentedControl(
    items: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SpendlyTheme.colors
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(colors.containerLow)
            .border(1.dp, glassRimBrush(), CircleShape)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (value, label) ->
            val isSel = value == selected
            val interaction = remember { MutableInteractionSource() }
            val bg by animateColorAsState(
                if (isSel) colors.glassSelected else Color.Transparent,
                label = "segmentBg"
            )
            Box(
                modifier = Modifier
                    .glassPress(interaction)
                    .clip(CircleShape)
                    .background(bg)
                    .clickable(interactionSource = interaction, indication = null) { onSelect(value) }
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSel) colors.textPrimary else colors.textSecondary
                )
            }
        }
    }
}
