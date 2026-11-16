package com.example.data

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("crypto_miner_prefs", Context.MODE_PRIVATE)

    var btcBalance: Double
        get() = java.lang.Double.longBitsToDouble(
            prefs.getLong("btc_balance_bits", java.lang.Double.doubleToRawLongBits(0.00000000594293))
        )
        set(value) {
            prefs.edit().putLong("btc_balance_bits", java.lang.Double.doubleToRawLongBits(value)).apply()
        }

    var pinkGems: Int
        get() = prefs.getInt("pink_gems", 55)
        set(value) = prefs.edit().putInt("pink_gems", value).apply()

    var blueDiamonds: Int
        get() = prefs.getInt("blue_diamonds", 0)
        set(value) = prefs.edit().putInt("blue_diamonds", value).apply()

    var vipLevel: Int
        get() = prefs.getInt("vip_level", 0)
        set(value) = prefs.edit().putInt("vip_level", value).apply()

    var totalTaps: Int
        get() = prefs.getInt("total_taps", 0)
        set(value) = prefs.edit().putInt("total_taps", value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean("haptics_enabled", true)
        set(value) = prefs.edit().putBoolean("haptics_enabled", value).apply()

    var dailyStreak: Int
        get() = prefs.getInt("daily_streak", 1)
        set(value) = prefs.edit().putInt("daily_streak", value).apply()

    fun resetAll() {
        prefs.edit().clear().apply()
    }
}
