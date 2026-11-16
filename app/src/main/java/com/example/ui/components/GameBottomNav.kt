package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.WoodBrown
import com.example.ui.theme.WoodBrownDark
import com.example.ui.theme.WoodBrownLight
import com.example.viewmodel.CurrentScreen

@Composable
fun GameBottomNav(
    currentScreen: CurrentScreen,
    onTabSelected: (CurrentScreen) -> Unit,
    hasBonusAvailable: Boolean = true,
    hasTasksAvailable: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0E111B))
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(WoodBrownLight, WoodBrown, WoodBrownDark)
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFC78A52), Color(0xFF4A2A11))
                    ),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                // 1. Shop
                GameNavItem(
                    title = "Shop",
                    emoji = "🎪",
                    isSelected = currentScreen == CurrentScreen.SHOP,
                    testTag = "nav_shop",
                    onClick = { onTabSelected(CurrentScreen.SHOP) }
                )

                // 2. Bonus
                GameNavItem(
                    title = "Bonus",
                    emoji = "📅",
                    isSelected = currentScreen == CurrentScreen.BONUS,
                    hasNotification = hasBonusAvailable,
                    testTag = "nav_bonus",
                    onClick = { onTabSelected(CurrentScreen.BONUS) }
                )

                // 3. Wallet (Center prominent button)
                CenterWalletNavItem(
                    isSelected = currentScreen == CurrentScreen.WALLET,
                    testTag = "nav_wallet",
                    onClick = {
                        if (currentScreen == CurrentScreen.WALLET) {
                            onTabSelected(CurrentScreen.MINE)
                        } else {
                            onTabSelected(CurrentScreen.WALLET)
                        }
                    }
                )

                // 4. Tasks
                GameNavItem(
                    title = "Tasks",
                    emoji = "📋",
                    isSelected = currentScreen == CurrentScreen.TASKS,
                    hasNotification = hasTasksAvailable,
                    testTag = "nav_tasks",
                    onClick = { onTabSelected(CurrentScreen.TASKS) }
                )

                // 5. Miners
                GameNavItem(
                    title = "Miners",
                    emoji = "⛑️",
                    isSelected = currentScreen == CurrentScreen.MINERS,
                    testTag = "nav_miners",
                    onClick = { onTabSelected(CurrentScreen.MINERS) }
                )
            }
        }

        // AdMob Adaptive Banner Ad anchored safely below navigation bar
        AdMobBannerAd(modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
fun GameNavItem(
    title: String,
    emoji: String,
    isSelected: Boolean,
    testTag: String,
    hasNotification: Boolean = false,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) GoldPrimary.copy(alpha = 0.25f) else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 24.sp)
            }

            if (hasNotification) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF9100))
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = title,
            color = if (isSelected) GoldLight else Color.White,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
        )
    }
}

@Composable
fun CenterWalletNavItem(
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "wallet_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset(y = (-8).dp)
            .scale(scale)
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 4.dp)
    ) {
        // Large rounded wallet emblem
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF8D5524), Color(0xFF5D3A1A), Color(0xFF3E2723))
                    )
                )
                .border(2.5.dp, GoldPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "👛", fontSize = 20.sp)
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary)
                        .border(1.dp, GoldLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₿",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4A2A00)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Wallet",
            color = if (isSelected) GoldLight else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}
