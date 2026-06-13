package com.ndrive.cloudvault.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Holds the resolved shimmer colors so they can be captured once
 * and shared across all shimmer items in the same composition tree.
 */
@Stable
data class ShimmerColors(
    val base: Color,
    val highlight: Color,
)

/**
 * Creates and remembers the shimmer colors from the current theme.
 * Call this once at a high level (e.g. the screen) and pass it down
 * to avoid each item resolving theme colors independently.
 */
@Composable
fun rememberShimmerColors(): ShimmerColors {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    return remember(base, highlight) { ShimmerColors(base, highlight) }
}

/**
 * High-performance shimmer modifier that uses [drawBehind] to paint
 * the animated gradient. Unlike the old `Modifier.composed` approach,
 * this only triggers **draw-phase** invalidation — no recomposition.
 *
 * @param shimmerProgress An animated float (0f‥1f) that drives the sweep.
 * @param colors Pre-resolved shimmer colors (avoids per-item theme reads).
 */
@Stable
fun Modifier.shimmerEffect(
    shimmerProgress: Float,
    colors: ShimmerColors,
): Modifier = this
    .clip(RoundedCornerShape(8.dp))
    .drawBehind {
        val width = size.width
        val sweepWidth = width * 0.9f
        val offset = -sweepWidth + (width + sweepWidth) * shimmerProgress

        val brush = Brush.linearGradient(
            colors = listOf(colors.base, colors.highlight, colors.base),
            start = Offset(offset, 0f),
            end = Offset(offset + sweepWidth, size.height),
        )
        drawRect(brush = brush)
    }

/**
 * Backward-compatible shimmer modifier. Uses `Modifier.composed` internally
 * so it works anywhere without hoisting animation state.
 *
 * For optimal performance inside LazyColumn/Grid, prefer the two-arg
 * [shimmerEffect(shimmerProgress, colors)] variant with hoisted state.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val colors = rememberShimmerColors()
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_progress",
    )
    shimmerEffect(shimmerProgress = progress, colors = colors)
}
