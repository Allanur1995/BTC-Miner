package com.example.data.repository

import com.example.data.local.DailyCheckInDao
import com.example.data.local.DailyCheckInEntity
import com.example.data.local.UserStatsDao
import com.example.model.DailyBonusDay
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

sealed class ClaimResult {
    data class Success(
        val day: DailyBonusDay,
        val streakDayClaimed: Int,
        val newStreak: Int,
        val awardedSats: Long,
        val awardedBtc: Double,
        val awardedGems: Int,
        val awardedDiamonds: Int,
        val message: String
    ) : ClaimResult()

    data class Failure(
        val remainingMillis: Long,
        val message: String
    ) : ClaimResult()
}

/**
 * Data repository responsible for enforcing the 24-hour daily check-in reward rule,
 * awarding a random amount of crypto once every 24 hours, managing streak tracking,
 * and persisting claim state.
 */
class DailyBonusRepository(
    private val dailyCheckInDao: DailyCheckInDao,
    private val userStatsDao: UserStatsDao? = null
) {
    companion object {
        const val TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000L // 86,400,000 ms
        const val FORTY_EIGHT_HOURS_MS = 48 * 60 * 60 * 1000L // 172,800,000 ms

        val REWARD_SCHEDULE = listOf(
            DailyBonusDay(
                day = 1,
                rewardLabel = "50–250 Sats\n+ Gems",
                satsReward = 50,
                gemReward = 5,
                diamondReward = 0,
                maxSatsReward = 250,
                maxGemReward = 15,
                maxDiamondReward = 0
            ),
            DailyBonusDay(
                day = 2,
                rewardLabel = "100–450 Sats\n+ Gems",
                satsReward = 100,
                gemReward = 8,
                diamondReward = 0,
                maxSatsReward = 450,
                maxGemReward = 20,
                maxDiamondReward = 1
            ),
            DailyBonusDay(
                day = 3,
                rewardLabel = "250–800 Sats\n+ Diamonds",
                satsReward = 250,
                gemReward = 12,
                diamondReward = 1,
                maxSatsReward = 800,
                maxGemReward = 25,
                maxDiamondReward = 2
            ),
            DailyBonusDay(
                day = 4,
                rewardLabel = "500–1,500 Sats\n+ Diamonds",
                satsReward = 500,
                gemReward = 15,
                diamondReward = 1,
                maxSatsReward = 1500,
                maxGemReward = 35,
                maxDiamondReward = 3
            ),
            DailyBonusDay(
                day = 5,
                rewardLabel = "1,000–3,000 Sats\n+ Diamonds",
                satsReward = 1000,
                gemReward = 25,
                diamondReward = 3,
                maxSatsReward = 3000,
                maxGemReward = 50,
                maxDiamondReward = 6
            ),
            DailyBonusDay(
                day = 6,
                rewardLabel = "2,500–6,000 Sats\n+ Diamonds",
                satsReward = 2500,
                gemReward = 40,
                diamondReward = 5,
                maxSatsReward = 6000,
                maxGemReward = 75,
                maxDiamondReward = 10
            ),
            DailyBonusDay(
                day = 7,
                rewardLabel = "5,000–15,000 Sats\n+ Jackpot",
                satsReward = 5000,
                gemReward = 75,
                diamondReward = 10,
                maxSatsReward = 15000,
                maxGemReward = 150,
                maxDiamondReward = 25
            )
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
     * Attempts to claim a randomized daily crypto reward once per 24 hours.
     */
    suspend fun claimDailyBonus(
        currentTimeMs: Long = System.currentTimeMillis(),
        random: Random = Random.Default,
        applyToUserStats: Boolean = true
    ): ClaimResult {
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

        // If elapsed time is greater than 48 hours, the streak resets to Day 1
        val isBrokenStreak = state.lastClaimTimestamp > 0L && (currentTimeMs - state.lastClaimTimestamp > FORTY_EIGHT_HOURS_MS)
        val targetDayNumber = if (isBrokenStreak || state.currentStreakDay > 7) 1 else state.currentStreakDay

        val reward = REWARD_SCHEDULE.find { it.day == targetDayNumber } ?: REWARD_SCHEDULE.first()

        val awardedSats = if (reward.maxSatsReward > reward.satsReward) {
            random.nextLong(reward.satsReward, reward.maxSatsReward + 1)
        } else {
            reward.satsReward
        }
        val awardedBtc = awardedSats * 0.00000001
        val awardedGems = if (reward.maxGemReward > reward.gemReward) {
            random.nextInt(reward.gemReward, reward.maxGemReward + 1)
        } else {
            reward.gemReward
        }
        val awardedDiamonds = if (reward.maxDiamondReward > reward.diamondReward) {
            random.nextInt(reward.diamondReward, reward.maxDiamondReward + 1)
        } else {
            reward.diamondReward
        }

        if (applyToUserStats) {
            userStatsDao?.let { dao ->
                val userStats = dao.getUserStats()
                if (userStats != null) {
                    dao.updateStatsValues(
                        id = 1,
                        btc = userStats.btcBalance + awardedBtc,
                        gems = userStats.pinkGems + awardedGems,
                        diamonds = userStats.blueDiamonds + awardedDiamonds,
                        hashRate = userStats.totalHashRateGh,
                        activeMiners = userStats.activeMinerCount,
                        taps = userStats.totalTaps,
                        timestamp = currentTimeMs
                    )
                }
            }
        }

        // Advance to next streak day (cycles 1..7)
        val nextStreakDay = if (targetDayNumber >= 7) 1 else targetDayNumber + 1
        dailyCheckInDao.recordClaim(
            streak = nextStreakDay,
            timestamp = currentTimeMs,
            sats = awardedSats,
            gems = awardedGems,
            diamonds = awardedDiamonds
        )

        return ClaimResult.Success(
            day = reward,
            streakDayClaimed = targetDayNumber,
            newStreak = nextStreakDay,
            awardedSats = awardedSats,
            awardedBtc = awardedBtc,
            awardedGems = awardedGems,
            awardedDiamonds = awardedDiamonds,
            message = "🎁 Day $targetDayNumber Daily Bonus! Won +$awardedSats Sats, +$awardedGems Gems" +
                if (awardedDiamonds > 0) ", +$awardedDiamonds Diamonds!" else "!"
        )
    }

    suspend fun resetCheckIn() {
        dailyCheckInDao.clearCheckIn()
        dailyCheckInDao.insertOrUpdateCheckIn(
            DailyCheckInEntity(
                id = 1,
                currentStreakDay = 1,
                lastClaimTimestamp = 0L,
                highestStreak = 1,
                totalClaimsCount = 0
            )
        )
    }
}
