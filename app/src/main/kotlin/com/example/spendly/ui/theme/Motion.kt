package com.example.spendly.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

// =========================================================================
// Spendly motion tokens: short, calm, consistent
// Arrivals slow into place, exits speed away, and only touch feedback springs.
// Compose follows the system animation scale, so "Remove animations" turns all of it off.
// =========================================================================

object Motion {
    const val Short = 150   // chips, toggles, quick fades
    const val Medium = 250  // screens, cards, sliding pills
    const val Long = 400    // charts and counting numbers

    // Screens share one see-through wallpaper, so a new screen waits this long for the old one
    // to fade out ("fade through") instead of showing through it mid-transition
    const val HandOff = 75

    // Data animations (counting totals, charts, new rows) wait out a screen transition
    // so you actually see them after coming back to a screen
    const val AfterScreen = 250

    val EnterEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val ExitEasing: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val MoveEasing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    fun <T> enter(durationMillis: Int = Medium, delayMillis: Int = 0): FiniteAnimationSpec<T> =
        tween(durationMillis, delayMillis = delayMillis, easing = EnterEasing)

    fun <T> exit(durationMillis: Int = Short): FiniteAnimationSpec<T> =
        tween(durationMillis, easing = ExitEasing)

    fun <T> move(durationMillis: Int = Medium): FiniteAnimationSpec<T> =
        tween(durationMillis, easing = MoveEasing)

    fun <T> press(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.55f, stiffness = 500f)

    /** Opacity of the screen (or month) on its way out: gone before the next one shows. */
    fun <T> fadeOutFirst(): FiniteAnimationSpec<T> = tween(90, easing = LinearEasing)

    /** Opacity of the screen (or month) on its way in, after [HandOff]. */
    fun <T> fadeInAfter(): FiniteAnimationSpec<T> = tween(175, delayMillis = HandOff, easing = EnterEasing)
}
