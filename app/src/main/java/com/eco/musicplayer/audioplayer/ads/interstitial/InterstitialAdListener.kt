package com.eco.musicplayer.audioplayer.ads.interstitial

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

interface InterstitialAdListener {
    fun onAdLoaded()
    fun onAdFailedToLoad(error: LoadAdError)
    fun onAdDismissed()
    fun onAdFailedToShow(adError: AdError)
}