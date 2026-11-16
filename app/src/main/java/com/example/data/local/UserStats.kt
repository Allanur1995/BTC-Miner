package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity tracking the player's core mining statistics:
 * Bitcoin balance, accumulated pink gems, blue diamonds, and total hash rate.
 */
@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "btc_balance")
    val btcBalance: Double = 0.00000000594293,

    @ColumnInfo(name = "pink_gems")
    val pinkGems: Int = 55,

    @ColumnInfo(name = "blue_diamonds")
    val blueDiamonds: Int = 0,

    @ColumnInfo(name = "total_hash_rate_gh")
    val totalHashRateGh: Double = 5.0,

    @ColumnInfo(name = "active_miner_count")
    val activeMinerCount: Int = 1,

    @ColumnInfo(name = "total_taps")
    val totalTaps: Int = 0,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
