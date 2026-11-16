package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.GameStateDao
import com.example.data.repository.GameRepository

/**
 * Dependency injection module providing database, DAO, and repository instances.
 */
object DatabaseModule {

    @Volatile
    private var databaseInstance: AppDatabase? = null

    /**
     * Provides a singleton instance of AppDatabase.
     */
    fun provideDatabase(context: Context): AppDatabase {
        return databaseInstance ?: synchronized(this) {
            val instance = AppDatabase.getInstance(context)
            databaseInstance = instance
            instance
        }
    }

    /**
     * Provides the GameStateDao from the provided AppDatabase.
     */
    fun provideGameStateDao(database: AppDatabase): GameStateDao {
        return database.gameStateDao()
    }

    /**
     * Provides the UserStatsDao from the provided AppDatabase.
     */
    fun provideUserStatsDao(database: AppDatabase): com.example.data.local.UserStatsDao {
        return database.userStatsDao()
    }

    /**
     * Provides the DailyCheckInDao from the provided AppDatabase.
     */
    fun provideDailyCheckInDao(database: AppDatabase): com.example.data.local.DailyCheckInDao {
        return database.dailyCheckInDao()
    }

    /**
     * Provides the DailyBonusRepository backed by DailyCheckInDao and UserStatsDao.
     */
    fun provideDailyBonusRepository(context: Context): com.example.data.repository.DailyBonusRepository {
        val database = provideDatabase(context)
        val checkInDao = provideDailyCheckInDao(database)
        val userStatsDao = provideUserStatsDao(database)
        return com.example.data.repository.DailyBonusRepository(checkInDao, userStatsDao)
    }

    /**
     * Provides the GameRepository backed by GameStateDao and UserStatsDao.
     */
    fun provideGameRepository(context: Context): GameRepository {
        val database = provideDatabase(context)
        val gameStateDao = provideGameStateDao(database)
        val userStatsDao = provideUserStatsDao(database)
        return GameRepository(gameStateDao, userStatsDao)
    }
}
