package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserStats::class,
        UserGameStateEntity::class,
        MinerEntity::class,
        DailyCheckInEntity::class,
        DailyProductivityEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameStateDao(): GameStateDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun dailyCheckInDao(): DailyCheckInDao
    abstract fun dailyProductivityDao(): DailyProductivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crypto_miner_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
