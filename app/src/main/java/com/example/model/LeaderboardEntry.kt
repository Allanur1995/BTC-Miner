package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class LeaderboardEntry(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val displayName: String = "Miner",
    val totalSatsMined: Long = 0L,
    val totalBtcMined: Double = 0.0,
    val hashRateGh: Double = 0.0,
    val activeMiners: Int = 0,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)
