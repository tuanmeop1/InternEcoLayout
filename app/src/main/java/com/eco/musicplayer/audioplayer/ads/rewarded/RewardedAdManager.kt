package com.eco.musicplayer.audioplayer.ads.rewarded

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.eco.musicplayer.audioplayer.ads.FullScreenAdManager
import com.eco.musicplayer.audioplayer.music.utils.CoinStorage
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdManager(val context: Context, private val fullScreenAdManager: FullScreenAdManager) {
    private var adsUnitId: String = ""
    private var rewardedAdListener: RewardedAdListener? = null
    private var rewardedAd: RewardedAd? = null
    private val TAG = "RewardedAdManager"

    fun setAdUnitId(adUnitId: String) {
        adsUnitId = adUnitId
    }

    fun setListener(listener: RewardedAdListener) {
        rewardedAdListener = listener
    }

    fun loadAd() {
        RewardedAd.load(
            context,
            adsUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("RewardedAdManager", "Ad was loaded.")
                    rewardedAd = ad
                    setupFullScreenContentCallback()
                    rewardedAdListener?.onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("RewardedAdManager", adError.message)
                    rewardedAd = null
                    rewardedAdListener?.onAdFailedToLoad(adError)
                }
            },
        )

    }

    fun setupFullScreenContentCallback() {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showing")
                fullScreenAdManager.onAdStarted()
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when fullscreen content is dismissed.
                Log.d("RewardedAdManager", "Ad was dismissed.")
                // Don't forget to set the ad reference to null so you
                // don't show the ad a second time.
                rewardedAd = null
                rewardedAdListener?.onAdDismissed()
                fullScreenAdManager.onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when fullscreen content failed to show.
                Log.d("RewardedAdManager", "Ad failed to show.")
                // Don't forget to set the ad reference to null so you
                // don't show the ad a second time.
                rewardedAd = null
                rewardedAdListener?.onAdFailedToShow(adError)
            }
        }
    }

    fun showAd(activity: Activity) {
        rewardedAd?.let { ad ->
            ad.show(activity, OnUserEarnedRewardListener { rewardItem ->
                // Handle the reward.
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type

                rewardedAdListener?.onUserEarnedReward(rewardAmount)
                Log.d(TAG, "User earned the reward.")
            })
        } ?: run {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
        }
    }

    fun showAd(fragment: Fragment) {
        rewardedAd?.let { ad ->
            ad.show(fragment.requireActivity(), OnUserEarnedRewardListener { rewardItem ->
                // Handle the reward.
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type

                rewardedAdListener?.onUserEarnedReward(rewardAmount)
                Log.d(TAG, "User earned the reward.")
            })
        } ?: run {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
        }
    }

}