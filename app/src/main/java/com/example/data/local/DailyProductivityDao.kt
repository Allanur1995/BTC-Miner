package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyProductivityDao {

    @Query("SELECT * FROM daily_productivity ORDER BY date_key DESC LIMIT :limit")
    fun getRecentDaysFlow(limit: Int = 7): Flow<List<DailyProductivityEntity>>

    @Query("SELECT * FROM daily_productivity ORDER BY date_key DESC LIMIT :limit")
    suspend fun getRecentDays(limit: Int = 7): List<DailyProductivityEntity>

    @Query("SELECT * FROM daily_productivity WHERE date_key = :dateKey LIMIT 1")
    suspend fun getByDateKey(dateKey: String): DailyProductivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: DailyProductivityEntity)

    @Query("DELETE FROM daily_productivity")
    suspend fun clearAll()
}
