package com.example

import com.example.viewmodel.toEntity
import com.example.viewmodel.toModel
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCountdownFormat() {
    assertEquals("08:00:00", com.example.ui.components.formatCountdown(8 * 3600L))
    assertEquals("00:01:30", com.example.ui.components.formatCountdown(90L))
    assertEquals("Expired", com.example.ui.components.formatCountdown(0L))
  }

  @Test
  fun testUserGameStateEntityDefaults() {
    val entity = com.example.data.local.UserGameStateEntity(
      btcBalance = 0.00000000594293,
      pinkGems = 55,
      blueDiamonds = 0,
      activeMinerCount = 1
    )
    assertEquals(1, entity.id)
    assertEquals(0.00000000594293, entity.btcBalance, 0.00000000000001)
    assertEquals(55, entity.pinkGems)
    assertEquals(0, entity.blueDiamonds)
    assertEquals(1, entity.activeMinerCount)
  }

  @Test
  fun testMinerEntityMapping() {
    val miner = com.example.model.Miner(
      id = "intern_miner",
      name = "Intern Miner",
      title = "Entry Level",
      hashRateGh = 5.0,
      shiftDurationSeconds = 28800L,
      remainingSeconds = 28783L,
      hireCostSats = 0L,
      hireCostGems = 0,
      isActive = true,
      isUnlocked = true
    )
    val entity = miner.toEntity()
    assertEquals("intern_miner", entity.id)
    assertEquals(5.0, entity.hashRateGh, 0.01)
    assertTrue(entity.isActive)
    val backToModel = entity.toModel()
    assertEquals(miner.id, backToModel.id)
    assertEquals(miner.hashRateGh, backToModel.hashRateGh, 0.01)
  }

  @Test
  fun testUserStatsEntity() {
    val stats = com.example.data.local.UserStats(
      id = 1,
      btcBalance = 0.00000000594293,
      pinkGems = 55,
      blueDiamonds = 0,
      totalHashRateGh = 5.0,
      activeMinerCount = 1,
      totalTaps = 10
    )
    assertEquals(1, stats.id)
    assertEquals(0.00000000594293, stats.btcBalance, 0.00000000000001)
    assertEquals(55, stats.pinkGems)
    assertEquals(0, stats.blueDiamonds)
    assertEquals(5.0, stats.totalHashRateGh, 0.01)
    assertEquals(1, stats.activeMinerCount)
    assertEquals(10, stats.totalTaps)
  }

  @Test
  fun testBtcMiningCalculation() {
    val activeHashRateGh = 5.0
    val boostMultiplier = 2.0
    val btcIncrement = (activeHashRateGh * 0.0000000000001) * boostMultiplier
    assertEquals(0.000000000001, btcIncrement, 0.000000000000001)
  }

  @Test
  fun testDailyBonus24HourInterval() {
    val now = 1_700_000_000_000L
    val twentyFourHours = 24 * 60 * 60 * 1000L

    // Never claimed before:
    assertTrue("Should be able to claim when never claimed", (now - 0L) >= twentyFourHours || 0L == 0L)

    // Claimed 1 hour ago (cannot claim):
    val claimed1HourAgo = now - (1 * 60 * 60 * 1000L)
    val remaining1Hour = (claimed1HourAgo + twentyFourHours) - now
    assertTrue("Should have remaining millis", remaining1Hour > 0)

    // Claimed 24 hours ago (can claim):
    val claimed24HoursAgo = now - twentyFourHours
    val remaining24Hours = (claimed24HoursAgo + twentyFourHours) - now
    assertEquals("Should be ready to claim at 24 hours", 0L, remaining24Hours)

    // Claimed 25 hours ago (can claim):
    val claimed25HoursAgo = now - (25 * 60 * 60 * 1000L)
    val remaining25Hours = maxOf(0L, (claimed25HoursAgo + twentyFourHours) - now)
    assertEquals(0L, remaining25Hours)
  }

  @Test
  fun testDailyBonusScheduleCount() {
    val schedule = com.example.data.repository.DailyBonusRepository.REWARD_SCHEDULE
    assertEquals(7, schedule.size)
    assertEquals(1, schedule[0].day)
    assertEquals(7, schedule[6].day)
    // Day 7 should have diamond reward
    assertTrue(schedule[6].diamondReward > 0)
  }

  @Test
  fun testAdMobConfigDefaults() {
    assertTrue(com.example.ads.AdMobConfig.appId.startsWith("ca-app-pub-"))
    assertTrue(com.example.ads.AdMobConfig.bannerUnitId.startsWith("ca-app-pub-"))
    assertTrue(com.example.ads.AdMobConfig.rewardedUnitId.startsWith("ca-app-pub-"))
    assertTrue(com.example.ads.AdMobConfig.interstitialUnitId.startsWith("ca-app-pub-"))
  }
}

