package com.eco.musicplayer.audioplayer.ads.interstitial

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.eco.musicplayer.audioplayer.ads.FullScreenAdManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdManager(val context: Context, private val fullScreenAdManager: FullScreenAdManager) {
    private var adsUnitId: String = ""
    private var interstitialAdListener: InterstitialAdListener? = null
    private var interstitialAd: InterstitialAd? = null
    private var shouldLoad: Boolean = true

    fun setAdUnitId(adUnitId: String) {
        adsUnitId = adUnitId
    }

    fun setListener(listener: InterstitialAdListener) {
        interstitialAdListener = listener
    }

    fun loadInterstitialAd() {
        if(!shouldLoad) return
        shouldLoad = false
        InterstitialAd.load(
            context,
            adsUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d("InterstitialAdManager", "Ad was loaded.")
                    interstitialAd = ad
                    setupFullScreenContentCallback()
                    interstitialAdListener?.onAdLoaded()
                    shouldLoad = false
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("InterstitialAdManager", adError.message)
                    interstitialAd = null
                    interstitialAdListener?.onAdFailedToLoad(adError)
                    shouldLoad = true
                }
            },
        )

    }

    fun setupFullScreenContentCallback() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d("InterstitialAdManager", "Ad showing")
                fullScreenAdManager.onAdStarted()
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when fullscreen content is dismissed.
                Log.d("InterstitialAdManager", "Ad was dismissed.")
                // Don't forget to set the ad reference to null so you
                // don't show the ad a second time.
                interstitialAd = null
                interstitialAdListener?.onAdDismissed()
                fullScreenAdManager.onAdDismissed()
                shouldLoad = true
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when fullscreen content failed to show.
                Log.d("InterstitialAdManager", "Ad failed to show.")
                // Don't forget to set the ad reference to null so you
                // don't show the ad a second time.
                interstitialAd = null
                interstitialAdListener?.onAdFailedToShow(adError)
            }
        }
    }

    fun showAd(activity: Activity) {
        if(interstitialAd != null) {
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

}