package com.eco.musicplayer.audioplayer.di


import com.eco.musicplayer.audioplayer.ads.FullScreenAdManager
import com.eco.musicplayer.audioplayer.ads.app_open.AppOpenAdApplication
import com.eco.musicplayer.audioplayer.ads.app_open.AppOpenAdManager
import com.eco.musicplayer.audioplayer.ads.interstitial.InterstitialAdManager
import com.eco.musicplayer.audioplayer.ads.nativee.NativeAdManager
import com.eco.musicplayer.audioplayer.ads.rewarded.RewardedAdManager
import com.eco.musicplayer.audioplayer.ads.rewarded_interstitial.RewardedInterstitialAdManager
import com.eco.musicplayer.audioplayer.music.ui.component.admob.viewmodel.AdmobLoverViewModel
import com.eco.musicplayer.audioplayer.music.ui.component.splash.activity.SplashActivity
import com.eco.musicplayer.audioplayer.music.utils.CoinStorage
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.single

val admobLoverModule = module {

    single {
        CoinStorage(context = androidContext())
    }

    factory {
        InterstitialAdManager(context = androidContext(), fullScreenAdManager = get())
    }

    factory {
        RewardedAdManager(context = androidContext(), fullScreenAdManager = get())
    }

    factory {
        RewardedInterstitialAdManager(context = get(), fullScreenAdManager = get())
    }

    factory {
        NativeAdManager(context = androidContext())
    }

    single {
        FullScreenAdManager()
    }

    single(named("Global")) {
        AppOpenAdManager(fullScreenAdManager = get())
    }

    scope<SplashActivity> {
        scoped { AppOpenAdManager(fullScreenAdManager = get()) }
    }

    single {
        AppOpenAdApplication(appOpenAdManager = get(named("Global")), application = get())
    }

    viewModel {
        AdmobLoverViewModel(coinStorage = get())
    }
}