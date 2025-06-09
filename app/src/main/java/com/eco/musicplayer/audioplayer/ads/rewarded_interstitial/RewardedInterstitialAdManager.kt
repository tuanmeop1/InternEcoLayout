package com.eco.musicplayer.audioplayer.ads.rewarded_interstitial

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.eco.musicplayer.audioplayer.ads.FullScreenAdManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

class RewardedInterstitialAdManager(
    private val context: Context,
    private val fullScreenAdManager: FullScreenAdManager
) {

    private val TAG = "RewardedInterstitialAdMgr"
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var adUnitId: String = ""
    private var listener: RewardedInterstitialAdListener? = null

    fun setAdUnitId(unitId: String) {
        adUnitId = unitId
    }

    fun setListener(listener: RewardedInterstitialAdListener) {
        this.listener = listener
    }

    fun loadAd() {
        RewardedInterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Log.d(TAG, "Ad loaded")
                    rewardedInterstitialAd = ad
                    setFullScreenContentCallback()
                    listener?.onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Failed to load ad: ${error.message}")
                    rewardedInterstitialAd = null
                    listener?.onAdFailedToLoad(error)
                }
            }
        )
    }

    private fun setFullScreenContentCallback() {
        rewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                Log.d(TAG, "Ad clicked")
            }

            override fun onAdImpression() {
                Log.d(TAG, "Ad impression")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showing")
                fullScreenAdManager.onAdStarted()
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed")
                rewardedInterstitialAd = null
                listener?.onAdDismissed()
                fullScreenAdManager.onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show: ${adError.message}")
                rewardedInterstitialAd = null
                listener?.onAdFailedToShow(adError)
            }
        }
    }

    fun showAd(activity: Activity) {
        if (rewardedInterstitialAd != null) {
            rewardedInterstitialAd?.show(activity) { rewardItem: RewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                listener?.onUserEarnedReward(rewardItem.amount)
            }
        } else {
            Log.w(TAG, "Ad not ready to show")
            listener?.onAdNotReady()
        }
    }
}