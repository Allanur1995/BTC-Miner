package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.PinkGem
import com.example.ui.theme.PurpleLilith
import com.example.ui.theme.PurpleLilithLight

@Composable
fun SideActionItem(
    title: String,
    icon: ImageVector? = null,
    emoji: String? = null,
    accentColor: Color,
    bgColor: Color = Color(0xFF222736),
    testTag: String,
    badgeText: String? = null,
    pulseAnimation: Boolean = false,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "side_item_pulse")
    val scale by if (pulseAnimation) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "static"
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 4.dp, vertical = 3.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.25f),
                                bgColor
                            )
                        )
                    )
                    .border(2.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (emoji != null) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }

            badgeText?.let { badge ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE53935))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp,
            modifier = Modifier.size(width = 62.dp, height = 24.dp)
        )
    }
}

@Composable
fun LeftSideActions(
    onSettingsClick: () -> Unit,
    onFaqClick: () -> Unit,
    onFreeMinerClick: () -> Unit,
    onPremiumPackClick: () -> Unit,
    onBogoClick: () -> Unit,
    onLilithClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SideActionItem(
            title = "Settings",
            icon = Icons.Default.Settings,
            accentColor = Color(0xFF90A4AE),
            testTag = "btn_settings",
            onClick = onSettingsClick
        )
        SideActionItem(
            title = "FAQ",
            icon = Icons.Default.Description,
            accentColor = Color(0xFFFFCC80),
            testTag = "btn_faq",
            onClick = onFaqClick
        )
        SideActionItem(
            title = "Free\nMiner",
            icon = Icons.Default.CardGiftcard,
            accentColor = Color(0xFF26C6DA),
            testTag = "btn_free_miner",
            badgeText = "FREE",
            pulseAnimation = true,
            onClick = onFreeMinerClick
        )
        SideActionItem(
            title = "Premium\nPack",
            icon = Icons.Default.WorkspacePremium,
            accentColor = GoldPrimary,
            testTag = "btn_premium_pack",
            onClick = onPremiumPackClick
        )
        SideActionItem(
            title = "Buy 1\nGet 1 Free",
            icon = Icons.Default.LocalOffer,
            accentColor = PinkGem,
            testTag = "btn_bogo",
            badgeText = "HOT",
            onClick = onBogoClick
        )
        SideActionItem(
            title = "Lilith\nDescends",
            emoji = "😈",
            accentColor = PurpleLilithLight,
            testTag = "btn_lilith",
            pulseAnimation = true,
            onClick = onLilithClick
        )
    }
}

@Composable
fun RightSideActions(
    vipLevel: Int,
    onVipClick: () -> Unit,
    onSubscribeClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // VIP Shield Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onVipClick)
                .testTag("btn_vip")
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GoldLight, GoldPrimary, Color(0xFFB8860B))
                        )
                    )
                    .border(2.dp, Color(0xFFFFF176), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$vipLevel",
                        color = Color(0xFF3E2723),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "VIP",
                        color = Color(0xFF3E2723),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "VIP",
                color = GoldPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        SideActionItem(
            title = "Subscribe",
            icon = Icons.Default.Campaign,
            accentColor = Color(0xFFFF9800),
            testTag = "btn_subscribe",
            onClick = onSubscribeClick
        )

        Spacer(modifier = Modifier.height(6.dp))

        SideActionItem(
            title = "Top\nMiners",
            icon = Icons.Default.EmojiEvents,
            accentColor = GoldPrimary,
            testTag = "btn_leaderboard",
            badgeText = "LIVE",
            pulseAnimation = true,
            onClick = onLeaderboardClick
        )
    }
}
