package com.eco.musicplayer.audioplayer.ads.rewarded_interstitial

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

interface RewardedInterstitialAdListener {
    fun onAdLoaded()
    fun onAdFailedToLoad(error: LoadAdError)
    fun onAdFailedToShow(adError: AdError)
    fun onAdDismissed()
    fun onAdNotReady()
    fun onUserEarnedReward(amount: Int)
}
