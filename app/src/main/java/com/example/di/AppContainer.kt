package com.example.di

import android.content.Context
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
    val gameRepository: GameRepository
    val dailyBonusRepository: com.example.data.repository.DailyBonusRepository
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

    override val gameRepository: GameRepository by lazy {
        GameRepository(gameStateDao, userStatsDao)
    }

    override val dailyBonusRepository: com.example.data.repository.DailyBonusRepository by lazy {
        DatabaseModule.provideDailyBonusRepository(context)
    }
}
