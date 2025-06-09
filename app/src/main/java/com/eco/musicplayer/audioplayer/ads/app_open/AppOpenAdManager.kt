package com.eco.musicplayer.audioplayer.ads.app_open

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.eco.musicplayer.audioplayer.ads.FullScreenAdManager
import com.eco.musicplayer.audioplayer.ads.banner.BannerAdListener
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

class AppOpenAdManager(private val fullScreenAdManager: FullScreenAdManager) {

    private val TAG = "AppOpenAdManager"
    private var isLoadingAd = false
    var isShowingAd = false
    private var appOpenAdListener: AppOpenAdListener? = null
    private var appOpenAd: AppOpenAd? = null
    private var adUnitId: String = ""
    private var loadTime: Long = 0
    private var overlayView: AppOpenAdOverlayView? = null

    fun setAdUnitId(adUnitId: String) {
        this.adUnitId = adUnitId
    }

    fun setListener(listener: AppOpenAdListener?) {
        appOpenAdListener = listener
    }

    fun loadAd(context: Context) {
        // Do not load ad if there is an unused ad or one is already loading.
        if (isLoadingAd || isAdAvailable()) {
            return
        }


        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context, adUnitId, request,
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(ad: AppOpenAd) {
                    // Called when an app open ad has loaded.
                    Log.d(TAG, "Ad was loaded.")
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    appOpenAdListener?.onAdLoaded()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Called when an app open ad has failed to load.
                    Log.d(TAG, loadAdError.message)
                    isLoadingAd = false;
                }
            })
    }

    /** Shows the ad if one isn't already showing. */
    fun showAdIfAvailable(activity: Activity) {
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            Log.d(TAG, "The app open ad is already showing.")
            return
        }

        // If the app open ad is not available yet, invoke the callback then load the ad.
//        if (!isAdAvailable()) {
//            Log.d(TAG, "The app open ad is not ready yet.")
//            loadAd(activity)
//            //appOpenAdListener?.onShowAdComplete()
//            return
//        }

        attachOverlayToActivity(activity)

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                // Called when full screen content is dismissed.
                // Set the reference to null so isAdAvailable() returns false.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                appOpenAd = null
                isShowingAd = false
                overlayView?.hide()
                fullScreenAdManager.onAdDismissed()
                appOpenAdListener?.onShowAdComplete()
                loadAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when fullscreen content failed to show.
                // Set the reference to null so isAdAvailable() returns false.
                Log.d(TAG, adError.message)
                appOpenAd = null
                isShowingAd = false
                overlayView?.hide()

                appOpenAdListener?.onShowAdComplete()
            }

            override fun onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
                overlayView?.show()
                appOpenAdListener?.onAdShowed()
                fullScreenAdManager.onAdStarted()

            }
        }
        isShowingAd = true
        appOpenAd?.show(activity)
    }

    fun showAdIfNoFullScreenAd(activity: Activity) {
        fullScreenAdManager.tryShowAd {
            showAdIfAvailable(activity)
        }
    }

    fun attachOverlayToActivity(activity: Activity) {
        if (overlayView != null && overlayView!!.isAttachedToWindow) return

        overlayView = AppOpenAdOverlayView(activity)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
        val decorView = activity.window.decorView as ViewGroup
        decorView.addView(overlayView, layoutParams)

    }

    /** Utility method to check if ad was loaded more than n hours ago. */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Check if ad exists and can be shown. */
    fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

}