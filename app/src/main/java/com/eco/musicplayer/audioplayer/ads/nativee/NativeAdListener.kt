package com.eco.musicplayer.audioplayer.ads.nativee

import com.google.android.gms.ads.LoadAdError

interface NativeAdListener {
    fun onAdLoaded()
    fun onAdFailedToLoad(error: LoadAdError)
}