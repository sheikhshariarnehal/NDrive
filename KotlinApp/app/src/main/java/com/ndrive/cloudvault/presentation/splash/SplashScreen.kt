package com.ndrive.cloudvault.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen() {
    val brandBlue = Color(0xFF1A73E8)
    val brandDark = Color(0xFF0A1929)

    val iconScale = remember { Animatable(0f) }
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showDots by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        iconScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = FastOutSlowInEasing),
        )
        showTitle = true
        delay(200)
        showSubtitle = true
        delay(200)
        showDots = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(brandDark, Color(0xFF0F2744)),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Cloud icon with scale-in animation
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(iconScale.value)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(brandBlue, Color(0xFF42A5F5)),
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "NDrive",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(tween(400)),
            ) {
                Text(
                    text = "NDrive",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(tween(400)),
            ) {
                Text(
                    text = "Your files, everywhere",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Loading dots
            AnimatedVisibility(
                visible = showDots,
                enter = fadeIn(tween(300)),
            ) {
                LoadingDots()
            }
        }
    }
}

@Composable
private fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotCount = 3

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(dotCount) { index ->
            val animatedAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot_$index",
            )
            val animatedOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot_offset_$index",
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(y = animatedOffset.dp)
                    .alpha(animatedAlpha)
                    .background(Color(0xFF42A5F5), CircleShape),
            )

            if (index < dotCount - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
