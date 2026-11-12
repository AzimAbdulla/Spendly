package com.example.spendly.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.spendly.data.model.Transaction
import com.example.spendly.ui.theme.Motion
import com.example.spendly.ui.theme.SpendlyTheme
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import com.example.spendly.ui.theme.TintSafe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * What one screen's animations last showed, kept while the screen stays in the back stack.
 * A screen leaves composition when another covers it, and rememberSaveable state inside a lazy
 * list doesn't survive that, so without this every animation would replay from scratch on return.
 * Navigation provides one per screen; each helper below keys its slot by its place in the UI.
 */
class ShownMemory {
    internal val slots = HashMap<Long, Any>()
}

val LocalShownMemory = staticCompositionLocalOf<ShownMemory?> { null }

/**
 * Closed while the system splash still covers the app at launch, so Home's first totals and
 * charts count up and grow in view rather than underneath it. MainActivity opens it as the splash
 * leaves; it opens by itself after three seconds in case no splash showed.
 */
object LaunchGate {
    var open by mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.S)

    suspend fun await() {
        withTimeoutOrNull(3_000) { snapshotFlow { open }.first { it } }
        open = true
    }
}

// Value-returning helpers get no composition group of their own, so two different helpers
// called from one composable see the same key hash; a salt per helper keeps their slots apart
@Composable
private fun shownSlot(salt: Int): Long = currentCompositeKeyHashCode * 31 + salt

/** Counts from the amount shown last time to [target]. */
@Composable
fun animateAmountAsState(target: Double): State<Double> {
    val memory = LocalShownMemory.current
    val slot = shownSlot(1)
    var to by remember { mutableDoubleStateOf(memory?.slots?.get(slot) as? Double ?: target) }
    var from by remember { mutableDoubleStateOf(to) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(target) {
        memory?.slots?.set(slot, target)
        if (target == to) return@LaunchedEffect
        LaunchGate.await()
        from += (to - from) * progress.value // start from whatever is on screen now
        to = target
        progress.snapTo(0f)
        progress.animateTo(1f, Motion.enter(Motion.Long, delayMillis = Motion.AfterScreen))
    }
    return remember { derivedStateOf { from + (to - from) * progress.value } }
}

/**
 * Animates each of [targets] from the value last drawn for it; the first show grows from zero.
 * Read `.value` inside draw code where possible so only drawing repeats.
 */
@Composable
fun rememberAnimatedValues(targets: List<Float>): List<Animatable<Float, AnimationVector1D>> {
    val memory = LocalShownMemory.current
    val slot = shownSlot(2)
    val lastShown = remember(targets.size) {
        (memory?.slots?.get(slot) as? FloatArray)?.takeIf { it.size == targets.size }
            ?: FloatArray(targets.size).also { memory?.slots?.set(slot, it) }
    }
    val values = remember(targets.size) { List(targets.size) { Animatable(lastShown[it]) } }
    targets.forEachIndexed { index, target ->
        LaunchedEffect(values, target) {
            lastShown[index] = target
            LaunchGate.await()
            values[index].animateTo(target, Motion.enter(Motion.Long, delayMillis = Motion.AfterScreen))
        }
    }
    return values
}

private class SeenRows(val resetKey: Any?, val ids: Set<Long>)

/**
 * Transaction rows with hairline dividers. A row that wasn't on screen last time grows in from
 * the top, so a new expense slides into place and pushes the rest down. Changing [resetKey]
 * (another day or month) starts over without animating.
 */
@Composable
fun TransactionRows(
    transactions: List<Transaction>,
    onTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹",
    resetKey: Any? = null,
    onConfirm: ((Long) -> Unit)? = null,
    divider: @Composable () -> Unit = {
        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = SpendlyTheme.colors.divider)
    }
) {
    val ids = transactions.map { it.id }
    val memory = LocalShownMemory.current
    val slot = shownSlot(3)
    // Null until the first non-empty list, so the initial load never animates
    var seen by remember(resetKey) {
        mutableStateOf((memory?.slots?.get(slot) as? SeenRows)?.takeIf { it.resetKey == resetKey }?.ids)
    }
    val seenBefore = seen
    LaunchedEffect(ids, resetKey) {
        if (ids.isNotEmpty() || seen != null) {
            seen = ids.toSet()
            memory?.slots?.set(slot, SeenRows(resetKey, ids.toSet()))
        }
    }

    Column(modifier = modifier) {
        transactions.forEachIndexed { index, tx ->
            key(tx.id) {
                val isNew = seenBefore != null && tx.id !in seenBefore
                val visibility = remember { MutableTransitionState(!isNew).apply { targetState = true } }
                // A new row lands with a green wash that fades once it's in place
                val flash = remember { Animatable(if (isNew) 1f else 0f) }
                LaunchedEffect(Unit) {
                    if (isNew) {
                        flash.animateTo(0f, tween(900, delayMillis = Motion.AfterScreen + Motion.Long, easing = LinearEasing))
                    }
                }
                AnimatedVisibility(
                    visibleState = visibility,
                    enter = fadeIn(Motion.enter(delayMillis = Motion.AfterScreen)) +
                        expandVertically(Motion.enter(Motion.Long, delayMillis = Motion.AfterScreen), expandFrom = Alignment.Top)
                ) {
                    Column(
                        Modifier.drawBehind {
                            if (flash.value > 0f) {
                                drawRoundRect(TintSafe.copy(alpha = 0.24f * flash.value), cornerRadius = CornerRadius(12.dp.toPx()))
                            }
                        }
                    ) {
                        TransactionItem(
                            transaction = tx,
                            onClick = { onTransactionClick(tx.id) },
                            currencySymbol = currencySymbol,
                            onConfirm = onConfirm?.let { confirm -> { confirm(tx.id) } }
                        )
                        if (index < transactions.lastIndex) divider()
                    }
                }
            }
        }
    }
}

/** Each character rolls up into place when it appears or changes, like a counter wheel. */
@Composable
fun RollingText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        text.forEachIndexed { index, char ->
            key(index) {
                val appear = remember { MutableTransitionState(false).apply { targetState = true } }
                AnimatedVisibility(
                    visibleState = appear,
                    enter = slideInVertically(Motion.enter()) { it / 2 } + fadeIn(Motion.enter())
                ) {
                    AnimatedContent(
                        targetState = char,
                        transitionSpec = {
                            (slideInVertically(Motion.enter()) { it / 2 } + fadeIn(Motion.enter()))
                                .togetherWith(slideOutVertically(Motion.exit()) { -it / 2 } + fadeOut(Motion.exit()))
                                .using(SizeTransform(clip = false))
                        },
                        label = "rollingChar"
                    ) { shown ->
                        Text(text = shown.toString(), style = style)
                    }
                }
            }
        }
    }
}
