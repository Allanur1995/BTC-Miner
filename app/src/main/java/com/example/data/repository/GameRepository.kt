package com.example.data.repository

import com.example.data.local.DailyProductivityDao
import com.example.data.local.DailyProductivityEntity
import com.example.data.local.GameStateDao
import com.example.data.local.MinerEntity
import com.example.data.local.UserGameStateEntity
import com.example.data.local.UserStats
import com.example.data.local.UserStatsDao
import com.example.model.DailyProductivityPoint
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GameRepository(
    private val gameStateDao: GameStateDao,
    private val userStatsDao: UserStatsDao? = null,
    private val dailyProductivityDao: DailyProductivityDao? = null
) {

    val gameStateFlow: Flow<UserGameStateEntity?> = gameStateDao.getGameStateFlow()
    val minersFlow: Flow<List<MinerEntity>> = gameStateDao.getAllMinersFlow()
    val userStatsFlow: Flow<UserStats?>? = userStatsDao?.getUserStatsFlow()
    val activeHashRateFlow: Flow<Double?>? = userStatsDao?.getHashRateFlow()
    val recentProductivityFlow: Flow<List<DailyProductivityEntity>>? = dailyProductivityDao?.getRecentDaysFlow(7)

    suspend fun getActiveHashRate(): Double {
        return userStatsDao?.getActiveHashRate() ?: 5.0
    }

    suspend fun updateBtcBalance(btcBalance: Double) {
        userStatsDao?.updateBtcBalance(btcBalance)
    }

    // ==================== UserStats CRUD ====================

    suspend fun getOrInitUserStats(): UserStats {
        val existing = userStatsDao?.getUserStats()
        if (existing != null) {
            return existing
        }
        val defaultStats = UserStats(
            id = 1,
            btcBalance = 0.00000000594293,
            pinkGems = 55,
            blueDiamonds = 0,
            totalHashRateGh = 5.0,
            activeMinerCount = 1,
            totalTaps = 0,
            lastUpdated = System.currentTimeMillis()
        )
        userStatsDao?.insertUserStats(defaultStats)
        return defaultStats
    }

    suspend fun saveUserStats(stats: UserStats) {
        userStatsDao?.insertUserStats(stats)
    }

    suspend fun updateUserStats(
        btc: Double,
        gems: Int,
        diamonds: Int,
        hashRate: Double,
        activeMiners: Int,
        taps: Int
    ) {
        userStatsDao?.updateStatsValues(
            id = 1,
            btc = btc,
            gems = gems,
            diamonds = diamonds,
            hashRate = hashRate,
            activeMiners = activeMiners,
            taps = taps
        )
    }

    suspend fun deleteUserStats(id: Int = 1) {
        userStatsDao?.deleteUserStatsById(id)
    }

    // ==================== GameState & Miners ====================

    suspend fun getOrInitGameState(defaultMinersCount: Int = 1): UserGameStateEntity {
        val existing = gameStateDao.getGameState()
        if (existing != null) {
            return existing
        }
        val defaultState = UserGameStateEntity(
            id = 1,
            btcBalance = 0.00000000594293,
            pinkGems = 55,
            blueDiamonds = 0,
            activeMinerCount = defaultMinersCount,
            totalHashRateGh = 5.0,
            totalTaps = 0,
            tapPowerSats = 1,
            vipLevel = 0,
            lastSavedTimestamp = System.currentTimeMillis()
        )
        gameStateDao.insertOrUpdateGameState(defaultState)
        return defaultState
    }

    suspend fun saveGameState(state: UserGameStateEntity) {
        gameStateDao.insertOrUpdateGameState(state)
    }

    suspend fun updateBalances(
        btcBalance: Double,
        pinkGems: Int,
        blueDiamonds: Int,
        activeMinerCount: Int,
        totalHashRateGh: Double,
        totalTaps: Int
    ) {
        gameStateDao.updateBalances(
            btcBalance = btcBalance,
            pinkGems = pinkGems,
            blueDiamonds = blueDiamonds,
            activeMinerCount = activeMinerCount,
            totalHashRateGh = totalHashRateGh,
            totalTaps = totalTaps
        )
        // Keep UserStats table synchronized
        userStatsDao?.updateStatsValues(
            id = 1,
            btc = btcBalance,
            gems = pinkGems,
            diamonds = blueDiamonds,
            hashRate = totalHashRateGh,
            activeMiners = activeMinerCount,
            taps = totalTaps
        )
    }

    suspend fun getOrInitMiners(defaultMiners: List<MinerEntity>): List<MinerEntity> {
        val existing = gameStateDao.getAllMiners()
        if (existing.isNotEmpty()) {
            return existing
        }
        gameStateDao.insertMiners(defaultMiners)
        return defaultMiners
    }

    suspend fun saveMiners(miners: List<MinerEntity>) {
        gameStateDao.insertMiners(miners)
    }

    suspend fun saveMiner(miner: MinerEntity) {
        gameStateDao.insertOrUpdateMiner(miner)
    }

    suspend fun recordDailyProductivity(
        dateKey: String,
        addedSats: Double,
        addedTaps: Int,
        currentHashRateGh: Double
    ) {
        val dao = dailyProductivityDao ?: return
        val existing = dao.getByDateKey(dateKey)
        val updated = if (existing != null) {
            existing.copy(
                satsMined = (existing.satsMined + addedSats).coerceAtLeast(0.0),
                tapsCount = (existing.tapsCount + addedTaps).coerceAtLeast(0),
                peakHashRateGh = maxOf(existing.peakHashRateGh, currentHashRateGh),
                updatedAt = System.currentTimeMillis()
            )
        } else {
            DailyProductivityEntity(
                dateKey = dateKey,
                satsMined = addedSats.coerceAtLeast(0.0),
                tapsCount = addedTaps.coerceAtLeast(0),
                peakHashRateGh = currentHashRateGh.coerceAtLeast(0.0),
                updatedAt = System.currentTimeMillis()
            )
        }
        dao.insertOrUpdate(updated)
    }

    suspend fun getRecentProductivity(limit: Int = 7): List<DailyProductivityEntity> {
        return dailyProductivityDao?.getRecentDays(limit) ?: emptyList()
    }

    suspend fun resetDatabase(defaultState: UserGameStateEntity, defaultMiners: List<MinerEntity>) {
        gameStateDao.clearGameState()
        gameStateDao.clearMiners()
        userStatsDao?.clearAllUserStats()
        dailyProductivityDao?.clearAll()
        gameStateDao.insertOrUpdateGameState(defaultState)
        gameStateDao.insertMiners(defaultMiners)
        userStatsDao?.insertUserStats(
            UserStats(
                id = 1,
                btcBalance = defaultState.btcBalance,
                pinkGems = defaultState.pinkGems,
                blueDiamonds = defaultState.blueDiamonds,
                totalHashRateGh = defaultState.totalHashRateGh,
                activeMinerCount = defaultState.activeMinerCount,
                totalTaps = defaultState.totalTaps
            )
        )
    }

    companion object {
        fun dateKeyFor(timeMs: Long = System.currentTimeMillis()): String {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return fmt.format(timeMs)
        }

        /**
         * Builds an ordered 7-day productivity series (oldest -> Today) from sparse Room records.
         */
        fun buildLast7DaysSeries(
            records: List<DailyProductivityEntity>,
            referenceTimeMs: Long = System.currentTimeMillis(),
            todayPendingSats: Double = 0.0,
            todayPendingTaps: Int = 0,
            todayCurrentHashRateGh: Double = 0.0
        ): List<DailyProductivityPoint> {
            val byKey = records.associateBy { it.dateKey }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dayFormat = SimpleDateFormat("EEE", Locale.US)
            val cal = Calendar.getInstance().apply {
                timeInMillis = referenceTimeMs
            }

            // Build keys from 6 days ago up to today
            val points = ArrayList<DailyProductivityPoint>(7)
            cal.add(Calendar.DAY_OF_YEAR, -6)
            for (i in 0 until 7) {
                val key = dateFormat.format(cal.time)
                val isToday = (i == 6)
                val label = if (isToday) "Today" else dayFormat.format(cal.time)
                val record = byKey[key]

                val sats = (record?.satsMined ?: 0.0) + if (isToday) todayPendingSats else 0.0
                val taps = (record?.tapsCount ?: 0) + if (isToday) todayPendingTaps else 0
                val peakHash = maxOf(
                    record?.peakHashRateGh ?: 0.0,
                    if (isToday) todayCurrentHashRateGh else 0.0
                )

                points.add(
                    DailyProductivityPoint(
                        dateKey = key,
                        dayLabel = label,
                        satsMined = sats,
                        tapsCount = taps,
                        peakHashRateGh = peakHash,
                        isToday = isToday
                    )
                )
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return points
        }
    }
}
