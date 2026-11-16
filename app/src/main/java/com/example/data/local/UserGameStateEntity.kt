package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_game_state")
data class UserGameStateEntity(
    @PrimaryKey
    val id: Int = 1,
    val btcBalance: Double = 0.00000000594293,
    val pinkGems: Int = 55,
    val blueDiamonds: Int = 0,
    val activeMinerCount: Int = 1,
    val totalHashRateGh: Double = 5.0,
    val totalTaps: Int = 0,
    val tapPowerSats: Long = 1,
    val vipLevel: Int = 0,
    val lastSavedTimestamp: Long = System.currentTimeMillis()
)
