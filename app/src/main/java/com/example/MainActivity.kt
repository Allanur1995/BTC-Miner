package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.AdMobManager
import com.example.ads.findActivity
import com.example.ui.screens.DailyBonusScreen
import com.example.ui.screens.MineScreen
import com.example.ui.screens.MinersScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.CaveDarkBg
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CurrentScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels { GameViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CaveDarkBg
                ) {
                    CryptoMinerApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CryptoMinerApp(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()

    val navigateToTab: (CurrentScreen) -> Unit = { target ->
        if (target == CurrentScreen.WALLET || target == CurrentScreen.SHOP) {
            AdMobManager.showInterstitialIfReady(activity)
        }
        viewModel.switchScreen(target)
    }

    // Handle Android system back gesture to navigate back to MINE screen or close dialog
    BackHandler(enabled = uiState.activeDialog != null || uiState.currentScreen != CurrentScreen.MINE) {
        if (uiState.activeDialog != null) {
            viewModel.closeDialog()
        } else if (uiState.currentScreen != CurrentScreen.MINE) {
            viewModel.switchScreen(CurrentScreen.MINE)
        }
    }

    when (uiState.currentScreen) {
        CurrentScreen.MINE -> {
            MineScreen(
                uiState = uiState,
                onMineTap = { x, y -> viewModel.onMineTap(x, y) },
                onTabSelected = navigateToTab,
                onOpenDialog = { dialog -> viewModel.openDialog(dialog) },
                onCloseDialog = { viewModel.closeDialog() },
                onRecruitClick = {
                    AdMobManager.showRewardedAd(
                        activity = activity,
                        onRewardEarned = { viewModel.grantAdRewardFreeMiner() },
                        onNotReady = { msg -> viewModel.showBannerMessage(msg) }
                    )
                },
                onToggleSound = { viewModel.toggleSound() },
                onToggleHaptics = { viewModel.toggleHaptics() },
                onResetGame = { viewModel.resetGame() },
                onClearBanner = { viewModel.clearBanner() }
            )
        }
        CurrentScreen.SHOP -> {
            ShopScreen(
                uiState = uiState,
                onBuyItem = { itemId -> viewModel.buyShopItem(itemId) },
                onWatchAdForGems = {
                    AdMobManager.showRewardedAd(
                        activity = activity,
                        onRewardEarned = { viewModel.grantAdRewardGems(25) },
                        onNotReady = { msg -> viewModel.showBannerMessage(msg) }
                    )
                },
                onWatchAdForBoost = {
                    AdMobManager.showRewardedAd(
                        activity = activity,
                        onRewardEarned = { viewModel.grantAdRewardBoost(300) },
                        onNotReady = { msg -> viewModel.showBannerMessage(msg) }
                    )
                },
                onTabSelected = navigateToTab,
                onBack = { viewModel.switchScreen(CurrentScreen.MINE) }
            )
        }
        CurrentScreen.BONUS -> {
            DailyBonusScreen(
                uiState = uiState,
                onClaimBonus = { day -> viewModel.claimDailyBonus(day) },
                onWatchAdBonus = {
                    AdMobManager.showRewardedAd(
                        activity = activity,
                        onRewardEarned = { viewModel.grantAdRewardGems(15) },
                        onNotReady = { msg -> viewModel.showBannerMessage(msg) }
                    )
                },
                onTabSelected = navigateToTab,
                onBack = { viewModel.switchScreen(CurrentScreen.MINE) }
            )
        }
        CurrentScreen.WALLET -> {
            WalletScreen(
                uiState = uiState,
                onWithdraw = { amt, addr -> viewModel.simulateWithdrawal(amt, addr) },
                onTabSelected = navigateToTab,
                onBack = { viewModel.switchScreen(CurrentScreen.MINE) }
            )
        }
        CurrentScreen.TASKS -> {
            TasksScreen(
                uiState = uiState,
                onClaimTask = { taskId -> viewModel.claimTaskReward(taskId) },
                onTabSelected = navigateToTab,
                onBack = { viewModel.switchScreen(CurrentScreen.MINE) }
            )
        }
        CurrentScreen.MINERS -> {
            MinersScreen(
                uiState = uiState,
                onHireMiner = { minerId -> viewModel.hireOrExtendMiner(minerId) },
                onTabSelected = navigateToTab,
                onBack = { viewModel.switchScreen(CurrentScreen.MINE) }
            )
        }
    }
}
