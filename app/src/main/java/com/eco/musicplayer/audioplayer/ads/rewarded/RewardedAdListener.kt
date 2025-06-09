package com.eco.musicplayer.audioplayer.ads.rewarded

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

interface RewardedAdListener {
    fun onAdLoaded()
    fun onAdFailedToLoad(error: LoadAdError)
    fun onAdDismissed()
    fun onAdFailedToShow(adError: AdError)
    fun onUserEarnedReward(amount: Int)
}