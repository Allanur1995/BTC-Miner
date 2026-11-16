package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStateDao {

    @Query("SELECT * FROM user_game_state WHERE id = 1 LIMIT 1")
    fun getGameStateFlow(): Flow<UserGameStateEntity?>

    @Query("SELECT * FROM user_game_state WHERE id = 1 LIMIT 1")
    suspend fun getGameState(): UserGameStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGameState(state: UserGameStateEntity)

    @Query("""
        UPDATE user_game_state 
        SET btcBalance = :btcBalance,
            pinkGems = :pinkGems,
            blueDiamonds = :blueDiamonds,
            activeMinerCount = :activeMinerCount,
            totalHashRateGh = :totalHashRateGh,
            totalTaps = :totalTaps,
            lastSavedTimestamp = :timestamp
        WHERE id = 1
    """)
    suspend fun updateBalances(
        btcBalance: Double,
        pinkGems: Int,
        blueDiamonds: Int,
        activeMinerCount: Int,
        totalHashRateGh: Double,
        totalTaps: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM miners")
    fun getAllMinersFlow(): Flow<List<MinerEntity>>

    @Query("SELECT * FROM miners")
    suspend fun getAllMiners(): List<MinerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMiners(miners: List<MinerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMiner(miner: MinerEntity)

    @Query("DELETE FROM user_game_state")
    suspend fun clearGameState()

    @Query("DELETE FROM miners")
    suspend fun clearMiners()
}
