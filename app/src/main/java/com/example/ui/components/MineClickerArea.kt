package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.TapParticle
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenHashRate
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun FloatingParticle(
    particle: TapParticle,
    modifier: Modifier = Modifier
) {
    val animOffset = remember { Animatable(0f) }
    val animAlpha = remember { Animatable(1f) }

    LaunchedEffect(particle.id) {
        launch {
            animOffset.animateTo(
                targetValue = -130f,
                animationSpec = tween(durationMillis = 1200, easing = LinearEasing)
            )
        }
        launch {
            animAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1200, easing = FastOutLinearInEasing)
            )
        }
    }

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = particle.x.roundToInt(),
                    y = (particle.y + animOffset.value).roundToInt()
                )
            }
            .alpha(animAlpha.value)
    ) {
        Text(
            text = particle.text,
            color = GoldLight,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black,
                    blurRadius = 4f
                )
            )
        )
    }
}

@Composable
fun Sparkle(
    size: Float = 24f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Text(
        text = "✨",
        fontSize = size.sp,
        modifier = modifier.scale(scale)
    )
}

/**
 * Visual Progress Indicator tracking proof-of-work block difficulty completion.
 */
@Composable
fun MiningBlockProgressBar(
    blockNumber: Long,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "block_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141926).copy(alpha = 0.92f))
            .border(1.5.dp, GoldBorder.copy(alpha = pulseAlpha), RoundedCornerShape(12.dp))
            .testTag("mining_progress_indicator")
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⛏️", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Block #$blockNumber",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}% Mining Progress",
                    color = GreenHashRate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Animated Visual Progress Bar
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GreenHashRate,
                trackColor = Color(0xFF263238)
            )
        }
    }
}

/**
 * Large tactile interactive button designed specifically for manual tapping.
 */
@Composable
fun LargeInteractiveMineButton(
    tapPowerSats: Long,
    comboMultiplier: Float,
    comboCount: Int,
    onTap: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val buttonScaleAnim = remember { Animatable(1f) }

    Box(
        modifier = modifier
            .scale(buttonScaleAnim.value)
            .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = GoldPrimary)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GoldLight,
                        GoldPrimary,
                        GoldDark
                    )
                )
            )
            .border(
                width = 3.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFDE7), GoldPrimary, Color(0xFF8D5524))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        coroutineScope.launch {
                            buttonScaleAnim.animateTo(0.92f, spring(stiffness = Spring.StiffnessHigh))
                            buttonScaleAnim.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
                        }
                        onTap(offset.x - 120f, offset.y - 30f)
                    }
                )
            }
            .testTag("manual_tap_button")
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Pickaxe Emblem
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3E2723).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Construction,
                    contentDescription = "Pickaxe",
                    tint = Color(0xFF3E2723),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TAP TO MINE",
                    color = Color(0xFF3E2723),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )

                val effectiveSats = (tapPowerSats * comboMultiplier).toLong().coerceAtLeast(1L)
                Text(
                    text = "+$effectiveSats Sats / Tap",
                    color = Color(0xFF4E342E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (comboMultiplier > 1.0f) {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE65100))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "x${String.format("%.1f", comboMultiplier)} 🔥",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun MineClickerArea(
    particles: List<TapParticle>,
    blockNumber: Long,
    miningBlockProgress: Float,
    tapPowerSats: Long,
    comboMultiplier: Float,
    comboCount: Int,
    onTap: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Visual Progress Indicator for Mining
            MiningBlockProgressBar(
                blockNumber = blockNumber,
                progress = miningBlockProgress,
                modifier = Modifier.padding(top = 4.dp)
            )

            // 2. Interactive Gold Boulder Pile
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(scaleAnim.value)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                coroutineScope.launch {
                                    scaleAnim.animateTo(0.92f, spring(stiffness = Spring.StiffnessHigh))
                                    scaleAnim.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
                                }
                                onTap(offset.x - 120f, offset.y - 120f)
                            }
                        )
                    }
                    .testTag("gold_mine_pile"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_gold_pile),
                    contentDescription = "Gold Mine Pile",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Sparkle accents
                Sparkle(size = 20f, modifier = Modifier.align(Alignment.TopCenter).offset(y = 10.dp))
                Sparkle(size = 16f, modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-16).dp, y = (-10).dp))
                Sparkle(size = 18f, modifier = Modifier.align(Alignment.BottomStart).offset(x = 24.dp, y = (-16).dp))
            }

            // 3. Large Interactive Button to Simulate Manual Tapping
            LargeInteractiveMineButton(
                tapPowerSats = tapPowerSats,
                comboMultiplier = comboMultiplier,
                comboCount = comboCount,
                onTap = onTap,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // Floating Tap Particles Overlay
        particles.forEach { particle ->
            FloatingParticle(particle = particle)
        }
    }
}
