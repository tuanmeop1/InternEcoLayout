package com.eco.musicplayer.audioplayer.ads.banner

import com.google.android.gms.ads.LoadAdError

interface BannerAdListener {
    fun onAdLoaded()
    fun onAdFailedToLoad(error: LoadAdError)
}