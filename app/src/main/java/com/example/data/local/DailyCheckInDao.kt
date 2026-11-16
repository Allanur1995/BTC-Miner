package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCheckInDao {

    @Query("SELECT * FROM daily_check_in WHERE id = 1 LIMIT 1")
    fun getCheckInFlow(): Flow<DailyCheckInEntity?>

    @Query("SELECT * FROM daily_check_in WHERE id = 1 LIMIT 1")
    suspend fun getCheckIn(): DailyCheckInEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCheckIn(checkIn: DailyCheckInEntity)

    @Query("UPDATE daily_check_in SET current_streak_day = :streak, last_claim_timestamp = :timestamp, total_claims_count = total_claims_count + 1 WHERE id = 1")
    suspend fun recordClaim(streak: Int, timestamp: Long)

    @Query("DELETE FROM daily_check_in")
    suspend fun clearCheckIn()
}
