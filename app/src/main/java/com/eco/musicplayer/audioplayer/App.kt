package com.eco.musicplayer.audioplayer

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.eco.musicplayer.audioplayer.ads.FullScreenAdManager
import com.eco.musicplayer.audioplayer.ads.app_open.AppOpenAdApplication
import com.eco.musicplayer.audioplayer.ads.app_open.AppOpenAdListener
import com.eco.musicplayer.audioplayer.ads.app_open.AppOpenAdManager
import com.eco.musicplayer.audioplayer.di.admobLoverModule
import com.eco.musicplayer.audioplayer.di.bannerAdModule
import com.eco.musicplayer.audioplayer.di.billingModule
import com.eco.musicplayer.audioplayer.di.paywallModule
import com.eco.musicplayer.audioplayer.music.ui.MainActivity
import com.eco.musicplayer.audioplayer.music.ui.base.AppOpenAdAllowed
import com.eco.musicplayer.audioplayer.music.utils.AdsConstants
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.get

class App : Application() {

    private lateinit var appOpenAdApplication: AppOpenAdApplication

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                listOf(
                    billingModule, bannerAdModule, paywallModule, admobLoverModule
                )
            )
        }

        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@App) {}
        }

        appOpenAdApplication = get<AppOpenAdApplication>()
        appOpenAdApplication.init()
    }

}
