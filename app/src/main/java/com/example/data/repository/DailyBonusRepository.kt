package com.example.data.repository

import com.example.data.local.DailyCheckInDao
import com.example.data.local.DailyCheckInEntity
import com.example.data.local.UserStatsDao
import com.example.model.DailyBonusDay
import kotlinx.coroutines.flow.Flow

sealed class ClaimResult {
    data class Success(
        val day: DailyBonusDay,
        val newStreak: Int,
        val message: String
    ) : ClaimResult()

    data class Failure(
        val remainingMillis: Long,
        val message: String
    ) : ClaimResult()
}

/**
 * Data repository responsible for enforcing the 24-hour daily check-in reward rule,
 * managing streak tracking, and applying rewards.
 */
class DailyBonusRepository(
    private val dailyCheckInDao: DailyCheckInDao,
    private val userStatsDao: UserStatsDao? = null
) {
    companion object {
        const val TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000L // 86,400,000 ms
        const val FORTY_EIGHT_HOURS_MS = 48 * 60 * 60 * 1000L // 172,800,000 ms

        val REWARD_SCHEDULE = listOf(
            DailyBonusDay(1, "50 Sats + 5 Gems", satsReward = 50, gemReward = 5, diamondReward = 0),
            DailyBonusDay(2, "100 Sats + 10 Gems", satsReward = 100, gemReward = 10, diamondReward = 0),
            DailyBonusDay(3, "250 Sats + 15 Gems + 1 Diamond", satsReward = 250, gemReward = 15, diamondReward = 1),
            DailyBonusDay(4, "500 Sats + 20 Gems + 2 Diamonds", satsReward = 500, gemReward = 20, diamondReward = 2),
            DailyBonusDay(5, "1,000 Sats + 30 Gems + 5 Diamonds", satsReward = 1000, gemReward = 30, diamondReward = 5),
            DailyBonusDay(6, "2,500 Sats + 50 Gems + 8 Diamonds", satsReward = 2500, gemReward = 50, diamondReward = 8),
            DailyBonusDay(7, "10,000 Sats + 100 Gems + 20 Diamonds", satsReward = 10000, gemReward = 100, diamondReward = 20)
        )
    }

    val checkInFlow: Flow<DailyCheckInEntity?> = dailyCheckInDao.getCheckInFlow()

    suspend fun getOrInitCheckIn(): DailyCheckInEntity {
        val existing = dailyCheckInDao.getCheckIn()
        if (existing != null) {
            return existing
        }
        val defaultEntity = DailyCheckInEntity(
            id = 1,
            currentStreakDay = 1,
            lastClaimTimestamp = 0L,
            highestStreak = 1,
            totalClaimsCount = 0
        )
        dailyCheckInDao.insertOrUpdateCheckIn(defaultEntity)
        return defaultEntity
    }

    /**
     * Checks if at least 24 hours have elapsed since the previous claim.
     */
    fun canClaim(lastClaimTimestamp: Long, currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        if (lastClaimTimestamp == 0L) return true
        val elapsed = currentTimeMs - lastClaimTimestamp
        return elapsed >= TWENTY_FOUR_HOURS_MS
    }

    /**
     * Returns remaining milliseconds before the next reward can be claimed.
     */
    fun getRemainingMillis(lastClaimTimestamp: Long, currentTimeMs: Long = System.currentTimeMillis()): Long {
        if (lastClaimTimestamp == 0L) return 0L
        val elapsed = currentTimeMs - lastClaimTimestamp
        return (TWENTY_FOUR_HOURS_MS - elapsed).coerceAtLeast(0L)
    }

    /**
     * Attempts to claim the reward for today based on 24-hour verification.
     */
    suspend fun claimDailyBonus(currentTimeMs: Long = System.currentTimeMillis()): ClaimResult {
        val state = getOrInitCheckIn()
        val remaining = getRemainingMillis(state.lastClaimTimestamp, currentTimeMs)

        if (remaining > 0L) {
            val hours = remaining / (60 * 60 * 1000)
            val minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000)
            return ClaimResult.Failure(
                remainingMillis = remaining,
                message = "Next daily reward available in ${hours}h ${minutes}m"
            )
        }

        // Determine current streak day:
        // If elapsed time is greater than 48 hours, the streak is broken and resets to Day 1
        val isBrokenStreak = state.lastClaimTimestamp > 0L && (currentTimeMs - state.lastClaimTimestamp > FORTY_EIGHT_HOURS_MS)
        val targetDayNumber = if (isBrokenStreak || state.currentStreakDay > 7) 1 else state.currentStreakDay

        val reward = REWARD_SCHEDULE.find { it.day == targetDayNumber } ?: REWARD_SCHEDULE.first()

        // Apply reward to Room UserStats
        userStatsDao?.let { dao ->
            val userStats = dao.getUserStats()
            if (userStats != null) {
                val btcReward = reward.satsReward * 0.00000001
                dao.updateStatsValues(
                    id = 1,
                    btc = userStats.btcBalance + btcReward,
                    gems = userStats.pinkGems + reward.gemReward,
                    diamonds = userStats.blueDiamonds + reward.diamondReward,
                    hashRate = userStats.totalHashRateGh,
                    activeMiners = userStats.activeMinerCount,
                    taps = userStats.totalTaps,
                    timestamp = currentTimeMs
                )
            }
        }

        // Advance to next streak day (cycles 1..7)
        val nextStreakDay = if (targetDayNumber >= 7) 1 else targetDayNumber + 1
        dailyCheckInDao.recordClaim(streak = nextStreakDay, timestamp = currentTimeMs)

        return ClaimResult.Success(
            day = reward,
            newStreak = nextStreakDay,
            message = "🎉 Day $targetDayNumber Claimed! +${reward.satsReward} Sats, +${reward.gemReward} Gems, +${reward.diamondReward} Diamonds!"
        )
    }
}
