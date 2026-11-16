package com.example.ui.screens

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.DailyBonusViewModel
import com.example.model.DailyBonusDay
import com.example.ui.components.GameBottomNav
import com.example.ui.components.TopBalanceBar
import com.example.ui.theme.CaveCardBg
import com.example.ui.theme.CaveDarkBg
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenHashRate
import com.example.ui.theme.PinkGem
import com.example.viewmodel.CurrentScreen
import com.example.viewmodel.GameUiState

@Composable
fun DailyBonusScreen(
    uiState: GameUiState,
    onClaimBonus: (Int) -> Unit,
    onWatchAdBonus: () -> Unit = {},
    onTabSelected: (CurrentScreen) -> Unit,
    onBack: () -> Unit,
    dailyBonusViewModel: DailyBonusViewModel = viewModel(factory = DailyBonusViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val bonusUiState by dailyBonusViewModel.uiState.collectAsStateWithLifecycle()
    val displayDays = if (bonusUiState.days.isNotEmpty()) bonusUiState.days else uiState.dailyBonuses
    val currentDay = displayDays.find { it.isCurrent }

    val infiniteTransition = rememberInfiniteTransition(label = "bonus_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CaveDarkBg)
            .statusBarsPadding()
    ) {
        // Top Balance Bar
        TopBalanceBar(
            btcBalance = uiState.btcBalance,
            pinkGems = uiState.pinkGems,
            blueDiamonds = uiState.blueDiamonds,
            onGemClick = { onTabSelected(CurrentScreen.SHOP) },
            onDiamondClick = { onTabSelected(CurrentScreen.SHOP) },
            onWalletClick = { onTabSelected(CurrentScreen.WALLET) }
        )

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF263238))
                    .testTag("bonus_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Mine",
                    tint = GoldPrimary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "📅 Daily Random Crypto Bonus",
                    color = GoldLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Auto-awards random Sats & Gems once every 24h when you open the app!",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 7-Day Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(displayDays, key = { it.day }) { day ->
                BonusDayCard(day = day, pulseAlpha = pulseAlpha)
            }
        }

        // Status banner if present
        bonusUiState.statusBanner?.let { banner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF263238))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = banner,
                    color = GoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Claim Today's Bonus Action Banner
        if (bonusUiState.canClaimToday) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Button(
                    onClick = {
                        onClaimBonus(bonusUiState.currentStreakDay)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp))
                        .testTag("claim_daily_btn")
                ) {
                    Text(
                        text = "🎁 CLAIM DAY ${bonusUiState.currentStreakDay} RANDOM CRYPTO!",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B2234))
                    .border(1.dp, GreenHashRate.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "✅ Today's random crypto bonus awarded!",
                        color = GreenHashRate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (bonusUiState.lastAwardedSats > 0L) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "🎁 +${bonusUiState.lastAwardedSats} Sats • +${bonusUiState.lastAwardedGems} Gems" +
                                if (bonusUiState.lastAwardedDiamonds > 0) " • +${bonusUiState.lastAwardedDiamonds} Diamonds" else "",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "⏳ ${bonusUiState.remainingTimeText}",
                        color = GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Extra Rewarded Video Ad Bonus Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Button(
                onClick = onWatchAdBonus,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, PinkGem, RoundedCornerShape(12.dp))
                    .testTag("btn_watch_ad_daily_gems")
            ) {
                Text(
                    text = "📺 Watch Video Ad for +15 Pink Gems!",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Bottom Navigation Bar
        GameBottomNav(
            currentScreen = uiState.currentScreen,
            onTabSelected = onTabSelected,
            hasBonusAvailable = uiState.dailyBonuses.any { it.isCurrent && !it.isClaimed },
            hasTasksAvailable = uiState.tasks.any { it.isCompleted && !it.isClaimed }
        )
    }
}

@Composable
fun BonusDayCard(
    day: DailyBonusDay,
    pulseAlpha: Float
) {
    val borderColor = when {
        day.isCurrent -> GoldLight.copy(alpha = pulseAlpha)
        day.isClaimed -> GreenHashRate
        else -> Color(0xFF374151)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (day.isCurrent) Color(0xFF2E2614) else CaveCardBg
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .border(if (day.isCurrent) 2.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .shadow(if (day.isCurrent) 6.dp else 2.dp, RoundedCornerShape(14.dp))
            .testTag("bonus_day_${day.day}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Day badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Day ${day.day}",
                    color = if (day.isCurrent) GoldLight else Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                if (day.isClaimed) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Claimed",
                        tint = GreenHashRate,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (!day.isCurrent) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Reward Icon
            val emoji = when (day.day) {
                1 -> "🪙"
                2 -> "💎"
                3 -> "⚡"
                4 -> "🎁"
                5 -> "💠"
                6 -> "👑"
                7 -> "🏆"
                else -> "🪙"
            }
            Text(text = emoji, fontSize = 28.sp)

            // Reward text
            Text(
                text = day.rewardLabel,
                color = if (day.isCurrent) GoldPrimary else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}
