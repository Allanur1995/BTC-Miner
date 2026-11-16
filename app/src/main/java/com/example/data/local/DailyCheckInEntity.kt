package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity tracking the user's daily login streak and claim timestamps
 * to ensure rewards are strictly claimed once per 24-hour cycle.
 */
@Entity(tableName = "daily_check_in")
data class DailyCheckInEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "current_streak_day")
    val currentStreakDay: Int = 1,

    @ColumnInfo(name = "last_claim_timestamp")
    val lastClaimTimestamp: Long = 0L,

    @ColumnInfo(name = "highest_streak")
    val highestStreak: Int = 1,

    @ColumnInfo(name = "total_claims_count")
    val totalClaimsCount: Int = 0
)
