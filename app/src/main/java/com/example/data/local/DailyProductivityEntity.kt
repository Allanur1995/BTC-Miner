package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity storing daily mining productivity metrics keyed by local calendar date (yyyy-MM-dd)
 * so 7-day productivity trends persist across app sessions.
 */
@Entity(tableName = "daily_productivity")
data class DailyProductivityEntity(
    @PrimaryKey
    @ColumnInfo(name = "date_key")
    val dateKey: String,

    @ColumnInfo(name = "sats_mined")
    val satsMined: Double = 0.0,

    @ColumnInfo(name = "taps_count")
    val tapsCount: Int = 0,

    @ColumnInfo(name = "peak_hash_rate_gh")
    val peakHashRateGh: Double = 0.0,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
