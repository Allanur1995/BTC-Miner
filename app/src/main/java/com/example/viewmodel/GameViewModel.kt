package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.example.CryptoMinerApplication
import com.example.audio.SoundController
import com.example.audio.SoundManager
import com.example.di.DatabaseModule
import com.example.data.GamePreferences
import com.example.data.local.AppDatabase
import com.example.data.local.DailyProductivityEntity
import com.example.data.local.MinerEntity
import com.example.data.local.UserGameStateEntity
import com.example.data.repository.ClaimResult
import com.example.data.repository.DailyBonusRepository
import com.example.data.repository.GameRepository
import com.example.model.DailyBonusAwardEvent
import com.example.model.DailyBonusDay
import com.example.model.DailyProductivityPoint
import com.example.model.GameTask
import com.example.model.Miner
import com.example.model.ShopCategory
import com.example.model.ShopItem
import com.example.model.TapParticle
import com.example.model.WalletTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class CurrentScreen {
    MINE,
    SHOP,
    BONUS,
    WALLET,
    TASKS,
    MINERS,
    LEADERBOARD
}

data class GameUiState(
    val btcBalance: Double = 0.00000000594293,
    val pinkGems: Int = 55,
    val blueDiamonds: Int = 0,
    val activeMinerCount: Int = 1,
    val vipLevel: Int = 0,
    val totalHashRateGh: Double = 5.0,
    val totalTaps: Int = 0,
    val tapPowerSats: Long = 1,
    val boostMultiplier: Double = 1.0,
    val boostRemainingSeconds: Int = 0,
    val autoTapActive: Boolean = false,
    val autoTapRemainingSeconds: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val miners: List<Miner> = emptyList(),
    val shopItems: List<ShopItem> = emptyList(),
    val dailyBonuses: List<DailyBonusDay> = emptyList(),
    val tasks: List<GameTask> = emptyList(),
    val transactions: List<WalletTransaction> = emptyList(),
    val particles: List<TapParticle> = emptyList(),
    val currentScreen: CurrentScreen = CurrentScreen.MINE,
    val activeDialog: GameDialogType? = null,
    val bannerNotice: String? = null,
    val miningBlockProgress: Float = 0.45f,
    val blockNumber: Long = 842915L,
    val tapCombo: Int = 0,
    val comboMultiplier: Float = 1.0f,
    val adsWatchedCount: Int = 0,
    val dailyBonusAwardPopup: DailyBonusAwardEvent? = null,
    val productivity7Days: List<DailyProductivityPoint> = emptyList()
)

enum class GameDialogType {
    SETTINGS,
    FAQ,
    FREE_MINER,
    PREMIUM_PACK,
    BOGO_OFFER,
    LILITH_EVENT,
    VIP_STATUS,
    SUBSCRIBE,
    RECRUIT_MODAL,
    WITHDRAW_MODAL
}

fun Miner.toEntity(): MinerEntity = MinerEntity(
    id = id,
    name = name,
    title = title,
    hashRateGh = hashRateGh,
    shiftDurationSeconds = shiftDurationSeconds,
    remainingSeconds = remainingSeconds,
    hireCostSats = hireCostSats,
    hireCostGems = hireCostGems,
    isActive = isActive,
    isUnlocked = isUnlocked,
    level = level,
    description = description,
    isNew = isNew
)

fun MinerEntity.toModel(): Miner = Miner(
    id = id,
    name = name,
    title = title,
    hashRateGh = hashRateGh,
    shiftDurationSeconds = shiftDurationSeconds,
    remainingSeconds = remainingSeconds,
    hireCostSats = hireCostSats,
    hireCostGems = hireCostGems,
    isActive = isActive,
    isUnlocked = isUnlocked,
    level = level,
    description = description,
    isNew = isNew
)

class GameViewModel(
    application: Application,
    private val repository: GameRepository = (application as? CryptoMinerApplication)?.appContainer?.gameRepository
        ?: DatabaseModule.provideGameRepository(application),
    private val dailyBonusRepository: DailyBonusRepository = (application as? CryptoMinerApplication)?.appContainer?.dailyBonusRepository
        ?: DatabaseModule.provideDailyBonusRepository(application),
    private val soundController: SoundController = (application as? CryptoMinerApplication)?.appContainer?.soundManager
        ?: SoundManager(application, GamePreferences(application).soundEnabled)
) : AndroidViewModel(application) {
    private val prefs = GamePreferences(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    @Volatile
    private var isInitialStateLoaded = false
    private val dailyBonusMutex = Mutex()
    private val productivityLock = Any()
    private var cachedProductivityRecords: List<DailyProductivityEntity> = emptyList()
    private var pendingProductivitySats: Double = 0.0
    private var pendingProductivityTaps: Int = 0

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        soundController.setEnabled(prefs.soundEnabled)
        loadInitialState()
        observeDailyBonusSync()
        observeProductivitySync()
        startGameLoop()
    }

    private fun loadInitialState() {
        val defaultMiners = listOf(
            Miner(
                id = "intern_miner",
                name = "Intern Miner",
                title = "Entry Level",
                hashRateGh = 5.0,
                shiftDurationSeconds = 8 * 3600L,
                remainingSeconds = 7 * 3600L + 59 * 60L + 43L,
                hireCostSats = 0L,
                hireCostGems = 0,
                isActive = true,
                isUnlocked = true,
                level = 1,
                description = "Energetic rookie running budget GPU rigs.",
                isNew = true
            ),
            Miner(
                id = "junior_miner",
                name = "Junior Miner",
                title = "Pickaxe Enthusiast",
                hashRateGh = 15.0,
                shiftDurationSeconds = 12 * 3600L,
                remainingSeconds = 0L,
                hireCostSats = 10000L,
                hireCostGems = 15,
                isActive = false,
                isUnlocked = true,
                level = 1,
                description = "Experienced prospector who knows underground veins."
            ),
            Miner(
                id = "senior_miner",
                name = "Senior Miner",
                title = "Cave Master",
                hashRateGh = 45.0,
                shiftDurationSeconds = 24 * 3600L,
                remainingSeconds = 0L,
                hireCostSats = 50000L,
                hireCostGems = 35,
                isActive = false,
                isUnlocked = true,
                level = 1,
                description = "Veteran miner operating high-density FPGA rigs."
            ),
            Miner(
                id = "cyber_rigger",
                name = "Cyber ASIC Rigger",
                title = "Hardware Wizard",
                hashRateGh = 120.0,
                shiftDurationSeconds = 24 * 3600L,
                remainingSeconds = 0L,
                hireCostSats = 200000L,
                hireCostGems = 75,
                isActive = false,
                isUnlocked = true,
                level = 1,
                description = "Deploys custom Antminer S19 Pro hydro arrays."
            ),
            Miner(
                id = "quantum_hashbot",
                name = "Quantum Hashbot",
                title = "AI Automation",
                hashRateGh = 350.0,
                shiftDurationSeconds = 48 * 3600L,
                remainingSeconds = 0L,
                hireCostSats = 1000000L,
                hireCostGems = 150,
                isActive = false,
                isUnlocked = false,
                level = 1,
                description = "Quantum computing core solving blocks with AI algorithms."
            ),
            Miner(
                id = "lilith_demonic",
                name = "Lilith Overlord",
                title = "Demonic Hash Power",
                hashRateGh = 1000.0,
                shiftDurationSeconds = 72 * 3600L,
                remainingSeconds = 0L,
                hireCostSats = 5000000L,
                hireCostGems = 300,
                isActive = false,
                isUnlocked = false,
                level = 1,
                description = "Dark underworld titan supercharging your mining pool."
            )
        )

        val defaultShopItems = listOf(
            ShopItem(
                id = "pickaxe_iron",
                name = "Iron Pickaxe",
                category = ShopCategory.PICKAXE,
                description = "Sturdy iron pickaxe for faster rock chipping.",
                bonusDescription = "+2 Sats per manual tap",
                tapBonusSats = 2,
                costSats = 500,
                costGems = 5,
                currentLevel = 0,
                maxLevel = 5
            ),
            ShopItem(
                id = "pickaxe_diamond",
                name = "Diamond Pickaxe",
                category = ShopCategory.PICKAXE,
                description = "Faceted diamond head delivers explosive tap earnings.",
                bonusDescription = "+10 Sats per manual tap",
                tapBonusSats = 10,
                costSats = 5000,
                costGems = 20,
                currentLevel = 0,
                maxLevel = 5
            ),
            ShopItem(
                id = "pickaxe_laser",
                name = "Plasma Laser Pick",
                category = ShopCategory.PICKAXE,
                description = "High-energy beam vaporizes gold rocks instantaneously.",
                bonusDescription = "+50 Sats per manual tap",
                tapBonusSats = 50,
                costSats = 50000,
                costDiamonds = 10,
                currentLevel = 0,
                maxLevel = 5
            ),
            ShopItem(
                id = "rig_usb",
                name = "USB ASIC Stick",
                category = ShopCategory.HARDWARE,
                description = "Compact plug-and-play miner.",
                bonusDescription = "+5 GH/s passive speed",
                hashBonusGh = 5.0,
                costSats = 1500,
                costGems = 10,
                currentLevel = 0,
                maxLevel = 10
            ),
            ShopItem(
                id = "rig_gpu_farm",
                name = "8x GPU Mining Rig",
                category = ShopCategory.HARDWARE,
                description = "Overclocked graphics array with liquid cooling.",
                bonusDescription = "+25 GH/s passive speed",
                hashBonusGh = 25.0,
                costSats = 25000,
                costGems = 30,
                currentLevel = 0,
                maxLevel = 10
            ),
            ShopItem(
                id = "rig_antminer",
                name = "Antminer S21 Immersion",
                category = ShopCategory.HARDWARE,
                description = "Industrial-grade ASIC immersed in dielectric oil.",
                bonusDescription = "+100 GH/s passive speed",
                hashBonusGh = 100.0,
                costSats = 150000,
                costDiamonds = 15,
                currentLevel = 0,
                maxLevel = 10
            ),
            ShopItem(
                id = "boost_2x",
                name = "Overclock Surge (2x)",
                category = ShopCategory.BOOST,
                description = "Doubles entire hash rate output for 120 seconds.",
                bonusDescription = "2x Speed for 2 minutes",
                costGems = 15,
                costDiamonds = 0,
                currentLevel = 0,
                maxLevel = 99
            ),
            ShopItem(
                id = "boost_auto_drone",
                name = "Golden Auto-Drone",
                category = ShopCategory.BOOST,
                description = "Autonomous drone taps gold pile 10 times/sec for 60s.",
                bonusDescription = "Auto-Tap for 1 minute",
                costGems = 25,
                costDiamonds = 0,
                currentLevel = 0,
                maxLevel = 99
            ),
            ShopItem(
                id = "gems_pouch",
                name = "Pouch of 50 Gems",
                category = ShopCategory.GEMS,
                description = "Instantly obtain 50 sparkling Pink Gems.",
                bonusDescription = "+50 Pink Gems",
                costSats = 10000,
                costGems = 0,
                currentLevel = 0,
                maxLevel = 999
            ),
            ShopItem(
                id = "diamonds_chest",
                name = "Chest of 10 Diamonds",
                category = ShopCategory.GEMS,
                description = "Ultra-rare Blue Diamonds for legendary miners.",
                bonusDescription = "+10 Blue Diamonds",
                costGems = 50,
                costDiamonds = 0,
                currentLevel = 0,
                maxLevel = 999
            )
        )

        val defaultDailyBonuses = DailyBonusRepository.REWARD_SCHEDULE

        val defaultTasks = listOf(
            GameTask("task_tap_50", "Rock Crusher I", "Tap the central gold pile 50 times", 200, 10, 50, 0),
            GameTask("task_tap_200", "Rock Crusher II", "Tap the central gold pile 200 times", 1000, 25, 200, 0),
            GameTask("task_recruit_1", "Miner Boss", "Recruit or activate at least 1 miner", 500, 15, 1, 1),
            GameTask("task_hash_20", "Speed Demon", "Reach a total hash rate of 20 GH/s", 1500, 20, 20, 5),
            GameTask("task_shop_1", "Investor", "Purchase any upgrade in the Shop", 800, 10, 1, 0),
            GameTask("task_gems_50", "Gem Collector", "Hold at least 50 Pink Gems", 1200, 30, 50, 50)
        )

        val defaultTransactions = listOf(
            WalletTransaction("tx_1", "Block Reward Payout", 0.00000000420000, 420, "2 min ago", true),
            WalletTransaction("tx_2", "Daily Streak Bonus", 0.00000000050000, 50, "1 hr ago", true),
            WalletTransaction("tx_3", "Intern Miner Recruitment", 0.00000000000000, 0, "8 hrs ago", true)
        )

        _uiState.value = GameUiState(
            btcBalance = 0.00000000594293,
            pinkGems = 55,
            blueDiamonds = 0,
            activeMinerCount = 1,
            vipLevel = 0,
            totalHashRateGh = 5.0,
            totalTaps = 0,
            soundEnabled = prefs.soundEnabled,
            hapticsEnabled = prefs.hapticsEnabled,
            miners = defaultMiners,
            shopItems = defaultShopItems,
            dailyBonuses = defaultDailyBonuses,
            tasks = defaultTasks,
            transactions = defaultTransactions,
            productivity7Days = GameRepository.buildLast7DaysSeries(emptyList(), todayCurrentHashRateGh = 5.0)
        )

        // Asynchronously load Room persisted state
        viewModelScope.launch(Dispatchers.IO) {
            val defaultEntities = defaultMiners.map { it.toEntity() }
            val loadedMiners = repository.getOrInitMiners(defaultEntities).map { it.toModel() }
            val activeMinersCount = loadedMiners.count { it.isActive }
            val loadedState = repository.getOrInitGameState(defaultMinersCount = activeMinersCount)
            val userStats = repository.getOrInitUserStats()

            // Calculate offline earnings if any
            val now = System.currentTimeMillis()
            val elapsedSeconds = ((now - loadedState.lastSavedTimestamp) / 1000).coerceAtLeast(0L)
            var offlineBtcMined = 0.0

            val adjustedMiners = loadedMiners.map { miner ->
                if (miner.isActive && miner.remainingSeconds > 0) {
                    val remaining = (miner.remainingSeconds - elapsedSeconds).coerceAtLeast(0L)
                    val activeSeconds = (miner.remainingSeconds - remaining).coerceAtLeast(0L)
                    offlineBtcMined += (miner.hashRateGh * 0.000000000001) * activeSeconds
                    miner.copy(remainingSeconds = remaining, isActive = remaining > 0)
                } else miner
            }

            val finalActiveCount = adjustedMiners.count { it.isActive }
            val finalBtc = (userStats.btcBalance.takeIf { it > 0 } ?: loadedState.btcBalance) + offlineBtcMined
            val finalHashRate = userStats.totalHashRateGh.takeIf { it > 0 } ?: loadedState.totalHashRateGh

            _uiState.update { current ->
                current.copy(
                    btcBalance = finalBtc,
                    pinkGems = userStats.pinkGems,
                    blueDiamonds = userStats.blueDiamonds,
                    activeMinerCount = finalActiveCount,
                    totalHashRateGh = finalHashRate,
                    totalTaps = userStats.totalTaps,
                    vipLevel = loadedState.vipLevel,
                    miners = adjustedMiners,
                    bannerNotice = if (offlineBtcMined > 0) {
                        "⛏️ Welcome back! Your miners generated +${String.format(Locale.US, "%.10f", offlineBtcMined)} BTC while you were away!"
                    } else current.bannerNotice
                )
            }
            recalculateHashRate()
            if (offlineBtcMined > 0.0) {
                addProductivityDelta(addedSats = offlineBtcMined * 100_000_000.0, addedTaps = 0)
                flushDailyProductivityToRoom()
            } else {
                refreshProductivityUiState()
            }
            isInitialStateLoaded = true

            // Automatically check & award 24-hour random crypto Daily Bonus on app open
            evaluateDailyBonusOnOpenInternal()

            // Observe active hash rate updates from Room
            repository.activeHashRateFlow?.collect { dbHashRate ->
                if (dbHashRate != null && dbHashRate > 0) {
                    _uiState.update { it.copy(totalHashRateGh = dbHashRate) }
                }
            }
        }
    }

    private fun observeProductivitySync() {
        viewModelScope.launch(Dispatchers.IO) {
            val initial = repository.getRecentProductivity(7)
            synchronized(productivityLock) {
                cachedProductivityRecords = initial
            }
            refreshProductivityUiState()

            repository.recentProductivityFlow?.collect { records ->
                synchronized(productivityLock) {
                    cachedProductivityRecords = records
                }
                refreshProductivityUiState()
            }
        }
    }

    private fun addProductivityDelta(addedSats: Double, addedTaps: Int) {
        synchronized(productivityLock) {
            pendingProductivitySats += addedSats.coerceAtLeast(0.0)
            pendingProductivityTaps += addedTaps.coerceAtLeast(0)
        }
        refreshProductivityUiState()
    }

    private fun refreshProductivityUiState() {
        val series = synchronized(productivityLock) {
            GameRepository.buildLast7DaysSeries(
                records = cachedProductivityRecords,
                todayPendingSats = pendingProductivitySats,
                todayPendingTaps = pendingProductivityTaps,
                todayCurrentHashRateGh = _uiState.value.totalHashRateGh
            )
        }
        _uiState.update { it.copy(productivity7Days = series) }
    }

    private suspend fun flushDailyProductivityToRoom() {
        val (satsDelta, tapsDelta) = synchronized(productivityLock) {
            val s = pendingProductivitySats
            val t = pendingProductivityTaps
            pendingProductivitySats = 0.0
            pendingProductivityTaps = 0
            s to t
        }
        val currentHash = _uiState.value.totalHashRateGh
        if (satsDelta > 0.0 || tapsDelta > 0 || currentHash > 0.0) {
            repository.recordDailyProductivity(
                dateKey = GameRepository.dateKeyFor(),
                addedSats = satsDelta,
                addedTaps = tapsDelta,
                currentHashRateGh = currentHash
            )
        }
    }

    private fun observeDailyBonusSync() {
        viewModelScope.launch(Dispatchers.IO) {
            dailyBonusRepository.getOrInitCheckIn()
            dailyBonusRepository.checkInFlow.collect { checkIn ->
                if (checkIn != null) {
                    val now = System.currentTimeMillis()
                    val canClaim = dailyBonusRepository.canClaim(checkIn.lastClaimTimestamp, now)
                    val claimedTargetDay = if (checkIn.currentStreakDay <= 1) 7 else checkIn.currentStreakDay - 1
                    val syncedDays = DailyBonusRepository.REWARD_SCHEDULE.map { scheduleDay ->
                        val isClaimed = if (canClaim) {
                            scheduleDay.day < checkIn.currentStreakDay
                        } else {
                            if (checkIn.currentStreakDay == 1 && checkIn.lastClaimTimestamp > 0L) {
                                true
                            } else {
                                scheduleDay.day <= claimedTargetDay
                            }
                        }
                        val isCurrent = if (canClaim) {
                            scheduleDay.day == checkIn.currentStreakDay
                        } else {
                            false
                        }
                        scheduleDay.copy(isClaimed = isClaimed, isCurrent = isCurrent)
                    }
                    _uiState.update { it.copy(dailyBonuses = syncedDays) }
                }
            }
        }
    }

    fun checkAndAwardDailyBonusOnOpen() {
        if (!isInitialStateLoaded) return
        viewModelScope.launch(Dispatchers.IO) {
            evaluateDailyBonusOnOpenInternal()
        }
    }

    private suspend fun evaluateDailyBonusOnOpenInternal() {
        dailyBonusMutex.withLock {
            if (!isInitialStateLoaded) return
            when (val result = dailyBonusRepository.claimDailyBonus(applyToUserStats = false)) {
                is ClaimResult.Success -> {
                    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    val tx = WalletTransaction(
                        id = "tx_daily_${System.currentTimeMillis()}",
                        title = "Daily Random Crypto Bonus (Day ${result.streakDayClaimed})",
                        amountBtc = result.awardedBtc,
                        amountSats = result.awardedSats,
                        timeAgo = timeStr,
                        isPositive = true
                    )
                    val event = DailyBonusAwardEvent(
                        streakDay = result.streakDayClaimed,
                        awardedSats = result.awardedSats,
                        awardedBtc = result.awardedBtc,
                        awardedGems = result.awardedGems,
                        awardedDiamonds = result.awardedDiamonds
                    )
                    _uiState.update { state ->
                        state.copy(
                            btcBalance = state.btcBalance + result.awardedBtc,
                            pinkGems = state.pinkGems + result.awardedGems,
                            blueDiamonds = state.blueDiamonds + result.awardedDiamonds,
                            transactions = listOf(tx) + state.transactions,
                            dailyBonusAwardPopup = event,
                            bannerNotice = result.message
                        )
                    }
                    soundController.playRewardChime()
                    val s = _uiState.value
                    repository.updateBalances(
                        btcBalance = s.btcBalance,
                        pinkGems = s.pinkGems,
                        blueDiamonds = s.blueDiamonds,
                        activeMinerCount = s.activeMinerCount,
                        totalHashRateGh = s.totalHashRateGh,
                        totalTaps = s.totalTaps
                    )
                }
                is ClaimResult.Failure -> {
                    // 24h cooldown still active; no bonus awarded
                }
            }
        }
    }

    fun dismissDailyBonusPopup() {
        _uiState.update { it.copy(dailyBonusAwardPopup = null) }
    }

    /**
     * Core Game Loop coroutine:
     * Periodically computes and increments the Bitcoin balance based on the current
     * active hash rate stored in the Room database, and syncs the updated balance back to Room.
     */
    private fun startGameLoop() {
        viewModelScope.launch {
            var tickCount = 0
            while (true) {
                delay(100) // 10 ticks per second (100ms interval for smooth ticker)
                tickCount++

                // Active hash rate retrieved from Room-backed state
                val activeHashRateGh = _uiState.value.totalHashRateGh
                val boostMultiplier = _uiState.value.boostMultiplier

                // Formula: 1 GH/s produces approx 0.0000000000001 BTC per 100ms
                val btcIncrement = (activeHashRateGh * 0.0000000000001) * boostMultiplier

                // Check boost countdown
                val newBoostRemaining = if (_uiState.value.boostRemainingSeconds > 0) {
                    if (tickCount % 10 == 0) _uiState.value.boostRemainingSeconds - 1 else _uiState.value.boostRemainingSeconds
                } else 0
                val currentMultiplier = if (newBoostRemaining > 0) 2.0 else 1.0

                // Auto-tap handling
                var extraAutoTapBtc = 0.0
                val newAutoTapRemaining = if (_uiState.value.autoTapRemainingSeconds > 0) {
                    extraAutoTapBtc = _uiState.value.tapPowerSats * 0.00000000000001
                    if (tickCount % 10 == 0) _uiState.value.autoTapRemainingSeconds - 1 else _uiState.value.autoTapRemainingSeconds
                } else 0

                // Clean old floating particles
                val now = System.currentTimeMillis()
                val activeParticles = _uiState.value.particles.filter { now - it.createdAt < 1500 }

                // Decrement miner shift timers every second
                val updatedMiners = if (tickCount % 10 == 0) {
                    _uiState.value.miners.map { miner ->
                        if (miner.isActive && miner.remainingSeconds > 0) {
                            val remaining = miner.remainingSeconds - 1
                            miner.copy(
                                remainingSeconds = remaining,
                                isActive = remaining > 0
                            )
                        } else miner
                    }
                } else _uiState.value.miners

                val currentActiveMinersCount = updatedMiners.count { it.isActive }

                // Block Mining Progress calculation
                val blockProgressDelta = ((activeHashRateGh / 100.0) * 0.008f).toFloat() * currentMultiplier.toFloat()
                var newBlockProgress = _uiState.value.miningBlockProgress + blockProgressDelta
                var currentBlockNum = _uiState.value.blockNumber
                var blockRewardBtc = 0.0
                var blockNotice: String? = _uiState.value.bannerNotice

                if (newBlockProgress >= 1.0f) {
                    newBlockProgress = 0f
                    currentBlockNum += 1
                    blockRewardBtc = 0.00000050 // 50 Satoshis bonus
                    blockNotice = "🎉 Block #$currentBlockNum Mined! +50 Sats Block Reward!"
                }

                // Combo decay every second
                val newCombo = if (tickCount % 10 == 0) {
                    (_uiState.value.tapCombo - 1).coerceAtLeast(0)
                } else _uiState.value.tapCombo

                val newComboMultiplier = when {
                    newCombo >= 20 -> 2.5f
                    newCombo >= 10 -> 2.0f
                    newCombo >= 5 -> 1.5f
                    else -> 1.0f
                }

                val tickBtcAdded = btcIncrement + extraAutoTapBtc + blockRewardBtc
                _uiState.update { state ->
                    state.copy(
                        btcBalance = state.btcBalance + tickBtcAdded,
                        boostMultiplier = currentMultiplier,
                        boostRemainingSeconds = newBoostRemaining,
                        autoTapActive = newAutoTapRemaining > 0,
                        autoTapRemainingSeconds = newAutoTapRemaining,
                        particles = activeParticles,
                        miners = updatedMiners,
                        activeMinerCount = currentActiveMinersCount,
                        miningBlockProgress = newBlockProgress.coerceIn(0f, 1f),
                        blockNumber = currentBlockNum,
                        tapCombo = newCombo,
                        comboMultiplier = newComboMultiplier,
                        bannerNotice = blockNotice
                    )
                }

                // Accumulate passive & block BTC output into today's productivity metrics
                synchronized(productivityLock) {
                    pendingProductivitySats += tickBtcAdded * 100_000_000.0
                }
                if (tickCount % 10 == 0) {
                    refreshProductivityUiState()
                }

                // Periodically update the Bitcoin balance in the Room database every 10 ticks (1 second)
                if (tickCount % 10 == 0) {
                    val currentBtc = _uiState.value.btcBalance
                    launch(Dispatchers.IO) {
                        repository.updateBtcBalance(currentBtc)
                    }
                }

                // Recalculate hash rate every second
                if (tickCount % 10 == 0) {
                    recalculateHashRate()
                }

                // Periodically persist full state & daily productivity to Room Database every 30 ticks (3 seconds)
                if (tickCount % 30 == 0) {
                    val s = _uiState.value
                    launch(Dispatchers.IO) {
                        repository.updateBalances(
                            btcBalance = s.btcBalance,
                            pinkGems = s.pinkGems,
                            blueDiamonds = s.blueDiamonds,
                            activeMinerCount = s.activeMinerCount,
                            totalHashRateGh = s.totalHashRateGh,
                            totalTaps = s.totalTaps
                        )
                        flushDailyProductivityToRoom()
                    }
                }
            }
        }
    }

    private fun recalculateHashRate() {
        _uiState.update { state ->
            val activeMinerHash = state.miners.filter { it.isActive }.sumOf { it.hashRateGh }
            val hardwareHash = state.shopItems
                .filter { it.category == ShopCategory.HARDWARE }
                .sumOf { it.hashBonusGh * it.currentLevel }
            val total = (activeMinerHash + hardwareHash).coerceAtLeast(1.0)
            val activeCount = state.miners.count { it.isActive }
            state.copy(totalHashRateGh = total, activeMinerCount = activeCount)
        }
        val currentHash = _uiState.value.totalHashRateGh
        val activeCount = _uiState.value.activeMinerCount
        val btc = _uiState.value.btcBalance
        val gems = _uiState.value.pinkGems
        val diamonds = _uiState.value.blueDiamonds
        val taps = _uiState.value.totalTaps
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserStats(
                btc = btc,
                gems = gems,
                diamonds = diamonds,
                hashRate = currentHash,
                activeMiners = activeCount,
                taps = taps
            )
        }
    }

    fun onMineTap(x: Float, y: Float) {
        val tapSats = _uiState.value.tapPowerSats
        val comboMult = _uiState.value.comboMultiplier
        val effectiveTapSats = (tapSats * comboMult).toLong().coerceAtLeast(1L)
        val tapBtc = effectiveTapSats * 0.00000001

        if (_uiState.value.soundEnabled) {
            soundController.playMineTap(comboCount = _uiState.value.tapCombo)
        }
        if (_uiState.value.hapticsEnabled) {
            triggerHaptic()
        }

        val particle = TapParticle(
            id = System.nanoTime(),
            x = x + Random.nextInt(-40, 40),
            y = y + Random.nextInt(-40, 20),
            text = "+${String.format(Locale.US, "%.8f", tapBtc)} BTC"
        )

        var minedBlockOnTap = false
        _uiState.update { state ->
            val newTotalTaps = state.totalTaps + 1
            val updatedTasks = state.tasks.map { task ->
                if (task.id.startsWith("task_tap_")) {
                    task.copy(currentProgress = (task.currentProgress + 1).coerceAtMost(task.targetCount))
                } else task
            }

            val foundGem = Random.nextInt(100) < 5
            val newGems = if (foundGem) state.pinkGems + 1 else state.pinkGems

            // Advance mining block progress on manual tap!
            var newProgress = state.miningBlockProgress + 0.04f
            var newBlockNum = state.blockNumber
            var bonusBtc = 0.0
            var notice = if (foundGem) "💎 Lucky! Found 1 Pink Gem while mining!" else state.bannerNotice

            if (newProgress >= 1.0f) {
                newProgress = 0f
                newBlockNum += 1
                bonusBtc = 0.00000050
                minedBlockOnTap = true
                notice = "🎉 Block #$newBlockNum Mined! +50 Sats Block Reward!"
            }

            val newCombo = (state.tapCombo + 1).coerceAtMost(30)
            val newComboMultiplier = when {
                newCombo >= 20 -> 2.5f
                newCombo >= 10 -> 2.0f
                newCombo >= 5 -> 1.5f
                else -> 1.0f
            }

            state.copy(
                btcBalance = state.btcBalance + tapBtc + bonusBtc,
                totalTaps = newTotalTaps,
                pinkGems = newGems,
                particles = state.particles + particle,
                tasks = updatedTasks,
                miningBlockProgress = newProgress.coerceIn(0f, 1f),
                blockNumber = newBlockNum,
                tapCombo = newCombo,
                comboMultiplier = newComboMultiplier,
                bannerNotice = notice
            )
        }

        val tapYieldSats = effectiveTapSats.toDouble() + if (minedBlockOnTap) 50.0 else 0.0
        addProductivityDelta(addedSats = tapYieldSats, addedTaps = 1)

        if (minedBlockOnTap) {
            soundController.playRewardChime()
        }

        // Persist immediately on tap milestones
        if (_uiState.value.totalTaps % 10 == 0) {
            persistStateToRoom()
        }
    }

    private fun persistStateToRoom() {
        val s = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBalances(
                btcBalance = s.btcBalance,
                pinkGems = s.pinkGems,
                blueDiamonds = s.blueDiamonds,
                activeMinerCount = s.activeMinerCount,
                totalHashRateGh = s.totalHashRateGh,
                totalTaps = s.totalTaps
            )
        }
    }

    private fun triggerHaptic() {
        vibrator?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(30)
            }
        }
    }

    fun switchScreen(screen: CurrentScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun openDialog(type: GameDialogType) {
        _uiState.update { it.copy(activeDialog = type) }
    }

    fun closeDialog() {
        _uiState.update { it.copy(activeDialog = null) }
    }

    fun clearBanner() {
        _uiState.update { it.copy(bannerNotice = null) }
    }

    fun showBannerMessage(message: String) {
        _uiState.update { it.copy(bannerNotice = message) }
    }

    fun recruitRandomMiner() {
        val candidate = _uiState.value.miners.firstOrNull { !it.isActive } ?: _uiState.value.miners.first()
        hireOrExtendMiner(candidate.id, isFree = true)
        _uiState.update {
            it.copy(
                bannerNotice = "🎉 Recruited ${candidate.name}! +${candidate.hashRateGh.toInt()} GH/s active for ${candidate.shiftDurationSeconds / 3600} hours!",
                activeDialog = null
            )
        }
    }

    fun grantAdRewardFreeMiner() {
        val candidate = _uiState.value.miners.firstOrNull { !it.isActive } ?: _uiState.value.miners.first()
        hireOrExtendMiner(candidate.id, isFree = true)
        _uiState.update {
            it.copy(
                adsWatchedCount = it.adsWatchedCount + 1,
                bannerNotice = "📺 Ad Reward! Recruited ${candidate.name} (+${candidate.hashRateGh.toInt()} GH/s for ${candidate.shiftDurationSeconds / 3600}h)!",
                activeDialog = null
            )
        }
    }

    fun grantAdRewardGems(amount: Int = 25) {
        _uiState.update {
            it.copy(
                pinkGems = it.pinkGems + amount,
                adsWatchedCount = it.adsWatchedCount + 1,
                bannerNotice = "📺 Ad Reward! Earned +$amount Pink Gems!"
            )
        }
        soundController.playRewardChime()
        persistStateToRoom()
    }

    fun grantAdRewardBoost(seconds: Int = 300) {
        _uiState.update {
            it.copy(
                boostRemainingSeconds = it.boostRemainingSeconds + seconds,
                boostMultiplier = 2.0,
                adsWatchedCount = it.adsWatchedCount + 1,
                bannerNotice = "📺 Ad Reward! 2x Mining Speed Boost active for ${seconds / 60} minutes!"
            )
        }
        soundController.playUpgradeComplete()
        persistStateToRoom()
    }

    fun hireOrExtendMiner(minerId: String, isFree: Boolean = false) {
        val miner = _uiState.value.miners.find { it.id == minerId } ?: return

        if (!isFree) {
            val state = _uiState.value
            val costSats = miner.hireCostSats
            val costGems = miner.hireCostGems
            val btcCost = costSats * 0.00000001

            if (state.btcBalance < btcCost && state.pinkGems < costGems) {
                soundController.playError()
                _uiState.update { it.copy(bannerNotice = "❌ Not enough balance or gems to hire ${miner.name}!") }
                return
            }

            _uiState.update {
                val newBal = if (it.btcBalance >= btcCost && btcCost > 0) it.btcBalance - btcCost else it.btcBalance
                val newGems = if (it.btcBalance < btcCost && costGems > 0) it.pinkGems - costGems else it.pinkGems
                it.copy(btcBalance = newBal, pinkGems = newGems)
            }
        }

        _uiState.update { state ->
            val updated = state.miners.map {
                if (it.id == minerId) {
                    it.copy(
                        isActive = true,
                        isUnlocked = true,
                        remainingSeconds = it.shiftDurationSeconds
                    )
                } else it
            }
            val activeCount = updated.count { it.isActive }
            state.copy(
                miners = updated,
                activeMinerCount = activeCount,
                bannerNotice = "⛏️ ${miner.name} is now mining at ${miner.hashRateGh.toInt()} GH/s!"
            )
        }
        soundController.playUpgradeComplete()
        recalculateHashRate()

        // Persist miners and state to Room
        val updatedMiners = _uiState.value.miners
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMiners(updatedMiners.map { it.toEntity() })
            persistStateToRoom()
        }
    }

    fun buyShopItem(itemId: String) {
        val item = _uiState.value.shopItems.find { it.id == itemId } ?: return
        val state = _uiState.value

        val btcCost = item.costSats * 0.00000001
        if (item.costSats > 0 && state.btcBalance < btcCost) {
            soundController.playError()
            _uiState.update { it.copy(bannerNotice = "❌ Not enough BTC to purchase ${item.name}!") }
            return
        }
        if (item.costGems > 0 && state.pinkGems < item.costGems) {
            soundController.playError()
            _uiState.update { it.copy(bannerNotice = "❌ Not enough Pink Gems to purchase ${item.name}!") }
            return
        }
        if (item.costDiamonds > 0 && state.blueDiamonds < item.costDiamonds) {
            soundController.playError()
            _uiState.update { it.copy(bannerNotice = "❌ Not enough Blue Diamonds to purchase ${item.name}!") }
            return
        }

        _uiState.update {
            it.copy(
                btcBalance = it.btcBalance - btcCost,
                pinkGems = it.pinkGems - item.costGems,
                blueDiamonds = it.blueDiamonds - item.costDiamonds
            )
        }

        when (item.category) {
            ShopCategory.PICKAXE -> {
                _uiState.update { s ->
                    val newTapPower = s.tapPowerSats + item.tapBonusSats
                    val updatedItems = s.shopItems.map { if (it.id == itemId) it.copy(currentLevel = it.currentLevel + 1) else it }
                    s.copy(tapPowerSats = newTapPower, shopItems = updatedItems, bannerNotice = "⚡ Pickaxe upgraded! +${item.tapBonusSats} Sats/tap")
                }
            }
            ShopCategory.HARDWARE -> {
                _uiState.update { s ->
                    val updatedItems = s.shopItems.map { if (it.id == itemId) it.copy(currentLevel = it.currentLevel + 1) else it }
                    s.copy(shopItems = updatedItems, bannerNotice = "🚀 Hardware upgraded! +${item.hashBonusGh.toInt()} GH/s")
                }
                recalculateHashRate()
            }
            ShopCategory.BOOST -> {
                if (item.id == "boost_2x") {
                    _uiState.update { it.copy(boostRemainingSeconds = it.boostRemainingSeconds + 120, bannerNotice = "🔥 2x Overclock activated for 2 minutes!") }
                } else if (item.id == "boost_auto_drone") {
                    _uiState.update { it.copy(autoTapRemainingSeconds = it.autoTapRemainingSeconds + 60, bannerNotice = "🤖 Golden Auto-Drone activated for 60 seconds!") }
                }
            }
            ShopCategory.GEMS -> {
                if (item.id == "gems_pouch") {
                    _uiState.update { it.copy(pinkGems = it.pinkGems + 50, bannerNotice = "💎 Acquired +50 Pink Gems!") }
                } else if (item.id == "diamonds_chest") {
                    _uiState.update { it.copy(blueDiamonds = it.blueDiamonds + 10, bannerNotice = "🔷 Acquired +10 Blue Diamonds!") }
                }
            }
        }
        soundController.playUpgradeComplete()
        persistStateToRoom()
    }

    fun claimDailyBonus(@Suppress("UNUSED_PARAMETER") dayNumber: Int = 1) {
        checkAndAwardDailyBonusOnOpen()
    }

    fun claimTaskReward(taskId: String) {
        val task = _uiState.value.tasks.find { it.id == taskId } ?: return
        if (task.isClaimed || !task.isCompleted) return

        val btcReward = task.rewardSats * 0.00000001
        _uiState.update { state ->
            val updatedTasks = state.tasks.map {
                if (it.id == taskId) it.copy(isClaimed = true) else it
            }
            state.copy(
                btcBalance = state.btcBalance + btcReward,
                pinkGems = state.pinkGems + task.rewardGems,
                tasks = updatedTasks,
                bannerNotice = "🏆 Task Completed! +${task.rewardSats} Sats, +${task.rewardGems} Gems!"
            )
        }
        soundController.playRewardChime()
        persistStateToRoom()
    }

    fun simulateWithdrawal(amountBtc: Double, address: String): Boolean {
        if (amountBtc <= 0 || amountBtc > _uiState.value.btcBalance) {
            soundController.playError()
            return false
        }
        if (address.isBlank() || address.length < 8) {
            soundController.playError()
            return false
        }

        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val tx = WalletTransaction(
            id = "tx_${System.currentTimeMillis()}",
            title = "Withdrawal to ${address.take(6)}...${address.takeLast(4)}",
            amountBtc = amountBtc,
            amountSats = (amountBtc * 100_000_000).toLong(),
            timeAgo = timeStr,
            isPositive = false
        )

        _uiState.update {
            it.copy(
                btcBalance = it.btcBalance - amountBtc,
                transactions = listOf(tx) + it.transactions,
                bannerNotice = "🚀 Withdrawal of ${String.format(Locale.US, "%.8f", amountBtc)} BTC dispatched to blockchain!"
            )
        }
        soundController.playUpgradeComplete()
        persistStateToRoom()
        return true
    }

    fun toggleSound() {
        val newSound = !_uiState.value.soundEnabled
        prefs.soundEnabled = newSound
        soundController.setEnabled(newSound)
        if (newSound) {
            soundController.playUiTick()
        }
        _uiState.update { it.copy(soundEnabled = newSound) }
    }

    fun toggleHaptics() {
        val newHaptics = !_uiState.value.hapticsEnabled
        prefs.hapticsEnabled = newHaptics
        _uiState.update { it.copy(hapticsEnabled = newHaptics) }
    }

    fun resetGame() {
        prefs.resetAll()
        isInitialStateLoaded = false
        viewModelScope.launch(Dispatchers.IO) {
            val defaultMiners = listOf(
                Miner(
                    id = "intern_miner",
                    name = "Intern Miner",
                    title = "Entry Level",
                    hashRateGh = 5.0,
                    shiftDurationSeconds = 8 * 3600L,
                    remainingSeconds = 7 * 3600L + 59 * 60L + 43L,
                    hireCostSats = 0L,
                    hireCostGems = 0,
                    isActive = true,
                    isUnlocked = true,
                    level = 1,
                    description = "Energetic rookie running budget GPU rigs.",
                    isNew = true
                )
            )
            val defaultState = UserGameStateEntity(
                id = 1,
                btcBalance = 0.00000000594293,
                pinkGems = 55,
                blueDiamonds = 0,
                activeMinerCount = 1,
                totalHashRateGh = 5.0,
                totalTaps = 0,
                tapPowerSats = 1,
                vipLevel = 0,
                lastSavedTimestamp = System.currentTimeMillis()
            )
            repository.resetDatabase(defaultState, defaultMiners.map { it.toEntity() })
            dailyBonusRepository.resetCheckIn()
            loadInitialState()
        }
        soundController.setEnabled(true)
        synchronized(productivityLock) {
            cachedProductivityRecords = emptyList()
            pendingProductivitySats = 0.0
            pendingProductivityTaps = 0
        }
        _uiState.update {
            it.copy(
                soundEnabled = true,
                dailyBonusAwardPopup = null,
                productivity7Days = GameRepository.buildLast7DaysSeries(emptyList(), todayCurrentHashRateGh = 5.0),
                bannerNotice = "🔄 Game reset to beginning!"
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val app = application as? CryptoMinerApplication
                val repository = app?.appContainer?.gameRepository
                    ?: DatabaseModule.provideGameRepository(application)
                val dailyBonusRepository = app?.appContainer?.dailyBonusRepository
                    ?: DatabaseModule.provideDailyBonusRepository(application)
                val soundController = app?.appContainer?.soundManager
                    ?: SoundManager(application, GamePreferences(application).soundEnabled)
                return GameViewModel(application, repository, dailyBonusRepository, soundController) as T
            }
        }
    }
}
