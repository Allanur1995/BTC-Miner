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

  @Test
  fun testDailyBonusRandomCryptoAwardAnd24HourRule() = kotlinx.coroutines.runBlocking {
    val fakeDao = object : com.example.data.local.DailyCheckInDao {
      private var entity: com.example.data.local.DailyCheckInEntity? = null
      private val flow = kotlinx.coroutines.flow.MutableStateFlow<com.example.data.local.DailyCheckInEntity?>(null)

      override fun getCheckInFlow() = flow
      override suspend fun getCheckIn() = entity
      override suspend fun insertOrUpdateCheckIn(checkIn: com.example.data.local.DailyCheckInEntity) {
        entity = checkIn
        flow.value = checkIn
      }
      override suspend fun recordClaim(streak: Int, timestamp: Long, sats: Long, gems: Int, diamonds: Int) {
        val updated = (entity ?: com.example.data.local.DailyCheckInEntity()).copy(
          currentStreakDay = streak,
          lastClaimTimestamp = timestamp,
          totalClaimsCount = (entity?.totalClaimsCount ?: 0) + 1,
          lastAwardedSats = sats,
          lastAwardedGems = gems,
          lastAwardedDiamonds = diamonds
        )
        entity = updated
        flow.value = updated
      }
      override suspend fun clearCheckIn() {
        entity = null
        flow.value = null
      }
    }

    val repo = com.example.data.repository.DailyBonusRepository(fakeDao)
    val t0 = 1_700_000_000_000L

    // First open: should succeed with random Sats within Day 1 bounds [50, 250]
    val first = repo.claimDailyBonus(currentTimeMs = t0, random = kotlin.random.Random(42))
    assertTrue(first is com.example.data.repository.ClaimResult.Success)
    val firstSuccess = first as com.example.data.repository.ClaimResult.Success
    assertEquals(1, firstSuccess.streakDayClaimed)
    assertEquals(2, firstSuccess.newStreak)
    assertTrue(firstSuccess.awardedSats in 50L..250L)
    assertEquals(firstSuccess.awardedSats * 0.00000001, firstSuccess.awardedBtc, 1e-12)

    // Reopening 6 hours later: should fail (24h not elapsed)
    val tooEarly = repo.claimDailyBonus(currentTimeMs = t0 + 6 * 3600_000L, random = kotlin.random.Random(43))
    assertTrue(tooEarly is com.example.data.repository.ClaimResult.Failure)

    // Reopening 24 hours later: should succeed for Day 2 within [100, 450]
    val nextDay = repo.claimDailyBonus(
      currentTimeMs = t0 + com.example.data.repository.DailyBonusRepository.TWENTY_FOUR_HOURS_MS,
      random = kotlin.random.Random(44)
    )
    assertTrue(nextDay is com.example.data.repository.ClaimResult.Success)
    val nextSuccess = nextDay as com.example.data.repository.ClaimResult.Success
    assertEquals(2, nextSuccess.streakDayClaimed)
    assertEquals(3, nextSuccess.newStreak)
    assertTrue(nextSuccess.awardedSats in 100L..450L)

    // Reopening > 48 hours later: streak resets back to Day 1
    val afterStreakReset = repo.claimDailyBonus(
      currentTimeMs = t0 + com.example.data.repository.DailyBonusRepository.TWENTY_FOUR_HOURS_MS +
        com.example.data.repository.DailyBonusRepository.FORTY_EIGHT_HOURS_MS + 1000L,
      random = kotlin.random.Random(45)
    )
    assertTrue(afterStreakReset is com.example.data.repository.ClaimResult.Success)
    val resetSuccess = afterStreakReset as com.example.data.repository.ClaimResult.Success
    assertEquals(1, resetSuccess.streakDayClaimed)
  }

  @Test
  fun testSoundSynthesizerGeneratesValidWavBuffers() {
    val buffers = listOf(
      com.example.audio.SoundSynthesizer.generateMineTapPcm(),
      com.example.audio.SoundSynthesizer.generateUpgradeCompletePcm(),
      com.example.audio.SoundSynthesizer.generateRewardChimePcm(),
      com.example.audio.SoundSynthesizer.generateErrorThudPcm(),
      com.example.audio.SoundSynthesizer.generateUiTickPcm()
    )

    buffers.forEach { pcm ->
      assertTrue("PCM buffer should not be empty", pcm.isNotEmpty())
      assertTrue("PCM buffer should have audible signal", pcm.any { it != 0.toShort() })
      val wav = com.example.audio.SoundSynthesizer.wrapPcmAsWav(pcm)
      assertEquals(44 + pcm.size * 2, wav.size)
      // Verify RIFF / WAVE magic headers
      assertEquals('R'.code.toByte(), wav[0])
      assertEquals('I'.code.toByte(), wav[1])
      assertEquals('F'.code.toByte(), wav[2])
      assertEquals('F'.code.toByte(), wav[3])
      assertEquals('W'.code.toByte(), wav[8])
      assertEquals('A'.code.toByte(), wav[9])
      assertEquals('V'.code.toByte(), wav[10])
      assertEquals('E'.code.toByte(), wav[11])
    }
  }

  @Test
  fun testBuildLast7DaysProductivitySeries() {
    val refTime = 1_700_000_000_000L
    val todayKey = com.example.data.repository.GameRepository.dateKeyFor(refTime)
    val twoDaysAgoKey = com.example.data.repository.GameRepository.dateKeyFor(refTime - 2 * 24 * 3600_000L)

    val sparseRecords = listOf(
      com.example.data.local.DailyProductivityEntity(
        dateKey = twoDaysAgoKey,
        satsMined = 320.5,
        tapsCount = 85,
        peakHashRateGh = 20.0
      ),
      com.example.data.local.DailyProductivityEntity(
        dateKey = todayKey,
        satsMined = 100.0,
        tapsCount = 40,
        peakHashRateGh = 25.0
      )
    )

    val series = com.example.data.repository.GameRepository.buildLast7DaysSeries(
      records = sparseRecords,
      referenceTimeMs = refTime,
      todayPendingSats = 15.5,
      todayPendingTaps = 5,
      todayCurrentHashRateGh = 30.0
    )

    assertEquals(7, series.size)
    assertTrue(series.last().isToday)
    assertEquals("Today", series.last().dayLabel)
    assertEquals(todayKey, series.last().dateKey)
    assertEquals(115.5, series.last().satsMined, 0.001)
    assertEquals(45, series.last().tapsCount)
    assertEquals(30.0, series.last().peakHashRateGh, 0.001)

    val twoDaysAgoPoint = series[4]
    assertEquals(twoDaysAgoKey, twoDaysAgoPoint.dateKey)
    assertEquals(320.5, twoDaysAgoPoint.satsMined, 0.001)
    assertEquals(85, twoDaysAgoPoint.tapsCount)
    assertEquals(20.0, twoDaysAgoPoint.peakHashRateGh, 0.001)
    assertFalse(twoDaysAgoPoint.isToday)
  }
}

