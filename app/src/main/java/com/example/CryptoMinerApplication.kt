package com.example

import android.app.Application
import com.example.ads.AdMobManager
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer

class CryptoMinerApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = DefaultAppContainer(this)
        AdMobManager.initialize(this)
    }
}
