package com.eco.musicplayer.audioplayer.ads.interstitial

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.eco.musicplayer.audioplayer.ads.AdCoolOffTime
import com.eco.musicplayer.audioplayer.ads.BaseAdManager
import com.eco.musicplayer.audioplayer.ads.FullScreenAdManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdManager(val context: Context, private val fullScreenAdManager: FullScreenAdManager, private val adCoolOffTime: AdCoolOffTime):
    BaseAdManager {
    private var adsUnitId: String = ""
    private var interstitialAdListener: InterstitialAdListener? = null
    private var interstitialAd: InterstitialAd? = null
    private var isLoading: Boolean = false

    fun setAdUnitId(adUnitId: String) {
        adsUnitId = adUnitId
    }

    fun setListener(listener: InterstitialAdListener) {
        interstitialAdListener = listener
    }

    // Only show ad If Ad time is cooled off
    fun shouldShowAd(): Boolean {
        return adCoolOffTime.isAdCoolOff()
    }

    fun loadAd() {
        if(isLoading || interstitialAd != null) return
        isLoading = true
        Log.d("InterstitialAdManager", "Loading interstitial ad in $context")
        InterstitialAd.load(
            context,
            adsUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d("InterstitialAdManager", "Ad was loaded.")
                    interstitialAd = ad
                    interstitialAdListener?.onAdLoaded()
                    isLoading = false
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("InterstitialAdManager", adError.message)
                    interstitialAd = null
                    interstitialAdListener?.onAdFailedToLoad(adError)
                    isLoading = false
                }
            },
        )

    }

    private fun setupFullScreenContentCallback(action: ((Boolean) -> Unit)?=null) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d("InterstitialAdManager", "Ad showing")
                fullScreenAdManager.onAdStarted()
                adCoolOffTime.setLastTimeFullScreenAdShow()
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when fullscreen content is dismissed.
                Log.d("InterstitialAdManager", "Ad was dismissed.")
                // Don't forget to set the ad reference to null so you
                // don't show the ad a second time.
                interstitialAd = null
                interstitialAdListener?.onAdDismissed()
                fullScreenAdManager.onAdDismissed()
                action?.invoke(true)
               // loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when fullscreen content failed to show.
                Log.d("InterstitialAdManager", "Ad failed to show.")
                // Don't forget to set the ad reference to null so you
                // don't show the ad a second time.
                interstitialAd = null
                interstitialAdListener?.onAdFailedToShow(adError)
                action?.invoke(false)

            }
        }
    }

    override fun showAd(activity: Activity) {
        if(interstitialAd != null) {
            setupFullScreenContentCallback()
            interstitialAd?.show(activity)
        } else {
            Log.d("InterstitialAdManager", "The interstitial ad wasn't ready yet.")
        }
    }

    override fun showAd(activity: Activity, action: ((Boolean) -> Unit)?) {
        if(interstitialAd != null) {
            setupFullScreenContentCallback(action)
            interstitialAd?.show(activity)
        } else {
            Log.d("InterstitialAdManager", "The interstitial ad wasn't ready yet.")
        }
    }

    fun showAd(fragment: Fragment) {
        if(interstitialAd != null) {
            interstitialAd?.show(fragment.requireActivity())
        } else {
            Log.d("InterstitialAdManager", "The interstitial ad wasn't ready yet.")
        }
    }

    override fun isAdLoading(): Boolean {
        return isLoading
    }

    override fun isAdLoaded(): Boolean {
        return interstitialAd != null
    }

}