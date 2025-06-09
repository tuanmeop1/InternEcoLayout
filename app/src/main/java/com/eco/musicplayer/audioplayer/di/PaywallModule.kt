package com.eco.musicplayer.audioplayer.di

import com.eco.musicplayer.audioplayer.ads.banner.BannerAdManager
import com.google.android.gms.ads.AdRequest
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val paywallModule = module {

    factory {
        BannerAdManager(context = androidContext())
    }
}