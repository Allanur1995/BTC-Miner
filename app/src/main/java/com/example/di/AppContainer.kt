package com.example.di

import android.content.Context
import com.example.audio.SoundController
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import com.example.data.local.AppDatabase
import com.example.data.local.GameStateDao
import com.example.data.repository.GameRepository

/**
 * Dependency container for application-level dependencies.
 */
interface AppContainer {
    val database: AppDatabase
    val gameStateDao: GameStateDao
    val userStatsDao: com.example.data.local.UserStatsDao
    val dailyCheckInDao: com.example.data.local.DailyCheckInDao
    val dailyProductivityDao: com.example.data.local.DailyProductivityDao
    val gameRepository: GameRepository
    val dailyBonusRepository: com.example.data.repository.DailyBonusRepository
    val soundManager: SoundController
}

/**
 * Implementation of AppContainer using DatabaseModule to instantiate singletons.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        DatabaseModule.provideDatabase(context)
    }

    override val gameStateDao: GameStateDao by lazy {
        DatabaseModule.provideGameStateDao(database)
    }

    override val userStatsDao: com.example.data.local.UserStatsDao by lazy {
        DatabaseModule.provideUserStatsDao(database)
    }

    override val dailyCheckInDao: com.example.data.local.DailyCheckInDao by lazy {
        DatabaseModule.provideDailyCheckInDao(database)
    }

    override val dailyProductivityDao: com.example.data.local.DailyProductivityDao by lazy {
        DatabaseModule.provideDailyProductivityDao(database)
    }

    override val gameRepository: GameRepository by lazy {
        GameRepository(gameStateDao, userStatsDao, dailyProductivityDao)
    }

    override val dailyBonusRepository: com.example.data.repository.DailyBonusRepository by lazy {
        DatabaseModule.provideDailyBonusRepository(context)
    }

    override val soundManager: SoundController by lazy {
        SoundManager(
            context = context,
            initialEnabled = GamePreferences(context).soundEnabled
        )
    }
}
