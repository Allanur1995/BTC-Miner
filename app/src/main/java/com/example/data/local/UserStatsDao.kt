package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for UserStats providing complete CRUD operations.
 */
@Dao
interface UserStatsDao {

    // ==================== CREATE ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(userStats: UserStats)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(userStats: UserStats): Long

    // ==================== READ ====================

    @Query("SELECT * FROM user_stats WHERE id = :id LIMIT 1")
    fun getUserStatsFlow(id: Int = 1): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = :id LIMIT 1")
    suspend fun getUserStats(id: Int = 1): UserStats?

    @Query("SELECT * FROM user_stats")
    fun getAllUserStatsFlow(): Flow<List<UserStats>>

    @Query("SELECT btc_balance FROM user_stats WHERE id = :id LIMIT 1")
    fun getBtcBalanceFlow(id: Int = 1): Flow<Double?>

    @Query("SELECT total_hash_rate_gh FROM user_stats WHERE id = :id LIMIT 1")
    fun getHashRateFlow(id: Int = 1): Flow<Double?>

    @Query("SELECT total_hash_rate_gh FROM user_stats WHERE id = :id LIMIT 1")
    suspend fun getActiveHashRate(id: Int = 1): Double?

    // ==================== UPDATE ====================

    @Update
    suspend fun updateUserStats(userStats: UserStats)

    @Query("""
        UPDATE user_stats 
        SET btc_balance = :btc,
            pink_gems = :gems,
            blue_diamonds = :diamonds,
            total_hash_rate_gh = :hashRate,
            active_miner_count = :activeMiners,
            total_taps = :taps,
            last_updated = :timestamp
        WHERE id = :id
    """)
    suspend fun updateStatsValues(
        id: Int = 1,
        btc: Double,
        gems: Int,
        diamonds: Int,
        hashRate: Double,
        activeMiners: Int,
        taps: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE user_stats SET btc_balance = :btcBalance, last_updated = :timestamp WHERE id = :id")
    suspend fun updateBtcBalance(
        btcBalance: Double,
        id: Int = 1,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE user_stats SET pink_gems = :gems, last_updated = :timestamp WHERE id = :id")
    suspend fun updateGems(
        gems: Int,
        id: Int = 1,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE user_stats SET blue_diamonds = :diamonds, last_updated = :timestamp WHERE id = :id")
    suspend fun updateDiamonds(
        diamonds: Int,
        id: Int = 1,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE user_stats SET total_hash_rate_gh = :hashRate, last_updated = :timestamp WHERE id = :id")
    suspend fun updateHashRate(
        hashRate: Double,
        id: Int = 1,
        timestamp: Long = System.currentTimeMillis()
    )

    // ==================== DELETE ====================

    @Delete
    suspend fun deleteUserStats(userStats: UserStats)

    @Query("DELETE FROM user_stats WHERE id = :id")
    suspend fun deleteUserStatsById(id: Int = 1)

    @Query("DELETE FROM user_stats")
    suspend fun clearAllUserStats()
}
