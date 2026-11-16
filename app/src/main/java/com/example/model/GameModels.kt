package com.example.model

enum class ShopCategory {
    PICKAXE,
    HARDWARE,
    BOOST,
    GEMS
}

data class Miner(
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

data class ShopItem(
    val id: String,
    val name: String,
    val category: ShopCategory,
    val description: String,
    val bonusDescription: String,
    val tapBonusSats: Long = 0,
    val hashBonusGh: Double = 0.0,
    val costSats: Long = 0,
    val costGems: Int = 0,
    val costDiamonds: Int = 0,
    val currentLevel: Int = 0,
    val maxLevel: Int = 10,
    val isUnlocked: Boolean = true
)

data class DailyBonusDay(
    val day: Int,
    val rewardLabel: String,
    val satsReward: Long,
    val gemReward: Int,
    val diamondReward: Int,
    val isClaimed: Boolean = false,
    val isCurrent: Boolean = false,
    val maxSatsReward: Long = satsReward,
    val maxGemReward: Int = gemReward,
    val maxDiamondReward: Int = diamondReward
)

data class DailyBonusAwardEvent(
    val streakDay: Int,
    val awardedSats: Long,
    val awardedBtc: Double,
    val awardedGems: Int,
    val awardedDiamonds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class GameTask(
    val id: String,
    val title: String,
    val description: String,
    val rewardSats: Long,
    val rewardGems: Int,
    val targetCount: Int,
    val currentProgress: Int,
    val isClaimed: Boolean = false
) {
    val isCompleted: Boolean get() = currentProgress >= targetCount
}

data class WalletTransaction(
    val id: String,
    val title: String,
    val amountBtc: Double,
    val amountSats: Long,
    val timeAgo: String,
    val isPositive: Boolean
)

data class TapParticle(
    val id: Long,
    val x: Float,
    val y: Float,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class DailyProductivityPoint(
    val dateKey: String,
    val dayLabel: String,
    val satsMined: Double,
    val tapsCount: Int,
    val peakHashRateGh: Double,
    val isToday: Boolean = false
)
