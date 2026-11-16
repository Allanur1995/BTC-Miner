package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "miners")
data class MinerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val title: String,
    val hashRateGh: Double,
    val shiftDurationSeconds: Long,
    val remainingSeconds: Long,
    val hireCostSats: Long,
    val hireCostGems: Int,
    val isActive: Boolean,
    val isUnlocked: Boolean,
    val level: Int = 1,
    val description: String = "",
    val isNew: Boolean = false
)
