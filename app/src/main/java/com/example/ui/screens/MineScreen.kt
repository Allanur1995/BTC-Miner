package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.FaqDialog
import com.example.ui.components.FreeMinerDialog
import com.example.ui.components.GameBottomNav
import com.example.ui.components.HashRateBar
import com.example.ui.components.LeftSideActions
import com.example.ui.components.LilithEventDialog
import com.example.ui.components.MineClickerArea
import com.example.ui.components.MinersTray
import com.example.ui.components.PremiumPackDialog
import com.example.ui.components.RightSideActions
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TopBalanceBar
import com.example.ui.theme.CaveDarkBg
import com.example.ui.theme.GoldPrimary
import com.example.viewmodel.CurrentScreen
import com.example.viewmodel.GameDialogType
import com.example.viewmodel.GameUiState

@Composable
fun MineScreen(
    uiState: GameUiState,
    onMineTap: (Float, Float) -> Unit,
    onTabSelected: (CurrentScreen) -> Unit,
    onOpenDialog: (GameDialogType) -> Unit,
    onCloseDialog: () -> Unit,
    onRecruitClick: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onResetGame: () -> Unit,
    onClearBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(CaveDarkBg)) {
        // Underground Cave Background Image
        Image(
            painter = painterResource(id = R.drawable.bg_mine),
            contentDescription = "Mine Cave",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle gradient overlay for readability of UI elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // Foreground Content Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Top Balance Bar
            TopBalanceBar(
                btcBalance = uiState.btcBalance,
                pinkGems = uiState.pinkGems,
                blueDiamonds = uiState.blueDiamonds,
                onGemClick = { onTabSelected(CurrentScreen.SHOP) },
                onDiamondClick = { onTabSelected(CurrentScreen.SHOP) },
                onWalletClick = { onTabSelected(CurrentScreen.WALLET) }
            )

            // Banner Notification (if present)
            uiState.bannerNotice?.let { notice ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF263238).copy(alpha = 0.95f))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                        .clickable(onClick = onClearBanner)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = notice,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.LightGray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // 2. Central Middle Section: Left Badges + Center Rock Clicker + Right Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Interactive Gold Clicker in the center with progress indicator and large tap button
                MineClickerArea(
                    particles = uiState.particles,
                    blockNumber = uiState.blockNumber,
                    miningBlockProgress = uiState.miningBlockProgress,
                    tapPowerSats = uiState.tapPowerSats,
                    comboMultiplier = uiState.comboMultiplier,
                    comboCount = uiState.tapCombo,
                    onTap = onMineTap,
                    modifier = Modifier.fillMaxSize()
                )

                // Left Side Action Buttons
                LeftSideActions(
                    onSettingsClick = { onOpenDialog(GameDialogType.SETTINGS) },
                    onFaqClick = { onOpenDialog(GameDialogType.FAQ) },
                    onFreeMinerClick = { onOpenDialog(GameDialogType.FREE_MINER) },
                    onPremiumPackClick = { onOpenDialog(GameDialogType.PREMIUM_PACK) },
                    onBogoClick = { onOpenDialog(GameDialogType.BOGO_OFFER) },
                    onLilithClick = { onOpenDialog(GameDialogType.LILITH_EVENT) },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp)
                )

                // Right Side Action Buttons
                RightSideActions(
                    vipLevel = uiState.vipLevel,
                    onVipClick = { onOpenDialog(GameDialogType.VIP_STATUS) },
                    onSubscribeClick = { onOpenDialog(GameDialogType.SUBSCRIBE) },
                    onLeaderboardClick = { onTabSelected(CurrentScreen.LEADERBOARD) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 6.dp, top = 8.dp)
                )
            }

            // 3. Total Hash Rate Bar (Wooden plank with rivets)
            HashRateBar(
                totalHashRateGh = uiState.totalHashRateGh,
                boostMultiplier = uiState.boostMultiplier,
                boostRemainingSeconds = uiState.boostRemainingSeconds
            )

            Spacer(modifier = Modifier.height(2.dp))

            // 4. Miners Tray (Recruit Card + Active Miner Cards)
            MinersTray(
                miners = uiState.miners,
                onRecruitClick = onRecruitClick,
                onMinerClick = { onTabSelected(CurrentScreen.MINERS) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 5. Bottom Navigation Bar
            GameBottomNav(
                currentScreen = uiState.currentScreen,
                onTabSelected = onTabSelected,
                hasBonusAvailable = uiState.dailyBonuses.any { it.isCurrent && !it.isClaimed },
                hasTasksAvailable = uiState.tasks.any { it.isCompleted && !it.isClaimed }
            )
        }

        // Active Dialogs Overlay
        when (uiState.activeDialog) {
            GameDialogType.SETTINGS -> {
                SettingsDialog(
                    soundEnabled = uiState.soundEnabled,
                    hapticsEnabled = uiState.hapticsEnabled,
                    totalTaps = uiState.totalTaps,
                    totalHashRate = uiState.totalHashRateGh,
                    adsWatchedCount = uiState.adsWatchedCount,
                    onToggleSound = onToggleSound,
                    onToggleHaptics = onToggleHaptics,
                    onResetGame = onResetGame,
                    onDismiss = onCloseDialog
                )
            }
            GameDialogType.FAQ -> {
                FaqDialog(onDismiss = onCloseDialog)
            }
            GameDialogType.FREE_MINER, GameDialogType.BOGO_OFFER -> {
                FreeMinerDialog(
                    onClaim = {
                        onRecruitClick()
                    },
                    onDismiss = onCloseDialog
                )
            }
            GameDialogType.PREMIUM_PACK, GameDialogType.VIP_STATUS, GameDialogType.SUBSCRIBE -> {
                PremiumPackDialog(onDismiss = onCloseDialog)
            }
            GameDialogType.LILITH_EVENT -> {
                LilithEventDialog(onDismiss = onCloseDialog)
            }
            null, GameDialogType.RECRUIT_MODAL, GameDialogType.WITHDRAW_MODAL -> { /* Handled in specific contexts */ }
        }
    }
}
