package com.example.spendly.ui.components

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.spendly.ui.theme.Motion
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// =========================================================================
// Squircle ("goo") effects, after Skiper UI's Apple squircle effect: blur a layer, then cut the
// blur back to a hard edge. Corners come out as smooth squircles and shapes that come close
// melt into each other like liquid. The effect needs Android 12+; earlier phones keep the motion
// without it.
// =========================================================================

/** The goo effect for a layer, or null before Android 12 or once the blur is gone. */
fun gooEffect(blurPx: Float): RenderEffect? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || blurPx < 0.5f) return null
    // Alpha times 20, less 7/20 of full: the soft edge of the blur snaps back to a hard one
    val threshold = ColorMatrix(
        floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 20f, -7f * 255f
        )
    )
    val blur = android.graphics.RenderEffect.createBlurEffect(blurPx, blurPx, Shader.TileMode.DECAL)
    return android.graphics.RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(threshold), blur)
        .asComposeRenderEffect()
}

// ---------- Squish: a tile gives under the finger ----------

/** How far a tile is pressed in: 0 at rest, 1 held down; it springs a little past 0 on release. */
class Squish internal constructor(internal val amount: Animatable<Float, AnimationVector1D>) {
    /** The tile's corner radius right now: rounder, more squircle-like, while pressed. */
    fun cornerRadius(rest: Dp = 24.dp): Dp = rest + 14.dp * amount.value.coerceAtLeast(0f)
}

@Composable
fun rememberSquish(): Squish = remember { Squish(Animatable(0f)) }

/**
 * Shrinks a tile while it's held and springs it back on release. It's only a touch response, so
 * it adds no click action, and scrolling away cancels it.
 */
fun Modifier.squish(squish: Squish): Modifier = this
    .graphicsLayer {
        val scale = 1f - 0.06f * squish.amount.value
        scaleX = scale
        scaleY = scale
    }
    .pointerInput(squish) {
        coroutineScope {
            detectTapGestures(onPress = {
                launch { squish.amount.animateTo(1f, Motion.press()) }
                tryAwaitRelease()
                launch { squish.amount.animateTo(0f, Motion.press()) }
            })
        }
    }
