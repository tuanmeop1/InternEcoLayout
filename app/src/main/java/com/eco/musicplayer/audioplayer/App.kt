package com.eco.musicplayer.audioplayer

import android.app.Application
import com.eco.musicplayer.audioplayer.di.billingModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                listOf(
                    billingModule
                )
            )
        }
    }
}