package com.example.data.repository

import com.example.data.local.GameStateDao
import com.example.data.local.MinerEntity
import com.example.data.local.UserGameStateEntity
import com.example.data.local.UserStats
import com.example.data.local.UserStatsDao
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val gameStateDao: GameStateDao,
    private val userStatsDao: UserStatsDao? = null
) {

    val gameStateFlow: Flow<UserGameStateEntity?> = gameStateDao.getGameStateFlow()
    val minersFlow: Flow<List<MinerEntity>> = gameStateDao.getAllMinersFlow()
    val userStatsFlow: Flow<UserStats?>? = userStatsDao?.getUserStatsFlow()
    val activeHashRateFlow: Flow<Double?>? = userStatsDao?.getHashRateFlow()

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

    suspend fun resetDatabase(defaultState: UserGameStateEntity, defaultMiners: List<MinerEntity>) {
        gameStateDao.clearGameState()
        gameStateDao.clearMiners()
        userStatsDao?.clearAllUserStats()
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
}
