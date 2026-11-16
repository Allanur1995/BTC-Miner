package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenHashRate
import com.example.ui.theme.WoodBrown
import com.example.ui.theme.WoodBrownDark
import com.example.ui.theme.WoodBrownLight
import java.util.Locale

@Composable
fun MetalBolt(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFE2E8F0), Color(0xFF64748B), Color(0xFF334155))
                )
            )
            .border(1.dp, Color(0xFF1E293B), CircleShape)
    )
}

@Composable
fun HashRateBar(
    totalHashRateGh: Double,
    boostMultiplier: Double = 1.0,
    boostRemainingSeconds: Int = 0,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hash_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp)
            .shadow(6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(WoodBrownLight, WoodBrown, WoodBrownDark)
                )
            )
            .border(2.dp, Color(0xFF3D2311), RoundedCornerShape(14.dp))
            .testTag("hash_rate_bar")
            .padding(horizontal = 8.dp, vertical = 7.dp)
    ) {
        // Corner Bolts
        MetalBolt(Modifier.align(Alignment.TopStart))
        MetalBolt(Modifier.align(Alignment.TopEnd))
        MetalBolt(Modifier.align(Alignment.BottomStart))
        MetalBolt(Modifier.align(Alignment.BottomEnd))

        // Center Content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bitcoin green-arrow emblem
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GoldLight, GoldPrimary, GoldDark)
                        )
                    )
                    .border(1.5.dp, Color(0xFFFFD54F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₿",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF5D3A00)
                    )
                    Text(
                        text = "↗",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00C853)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Total: ",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )

            val displayHash = if (totalHashRateGh >= 1000) {
                String.format(Locale.US, "%.1f TH/s", totalHashRateGh / 1000.0)
            } else {
                "${totalHashRateGh.toInt()} GH/s"
            }

            Text(
                text = displayHash,
                color = GreenHashRate.copy(alpha = glowAlpha),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )

            if (boostMultiplier > 1.0 && boostRemainingSeconds > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE53935))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "2X (${boostRemainingSeconds}s)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
