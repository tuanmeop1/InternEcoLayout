package com.eco.musicplayer.audioplayer.ads.banner

import android.content.Context
import android.view.ViewGroup
import com.eco.musicplayer.audioplayer.music.utils.AdsConstants
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

class BannerAdPreloader(val context: Context) {
    private var adView: AdView? = null
    private var lastLoadTime: Long = 0L
    private val expirationTimeInMs = AdsConstants.EXPIRATION_TIME_MS
    private val adUnitId = AdsConstants.AD_UNIT_ID

    fun preLoad() {
        val view = AdView(context)
        view.adUnitId = adUnitId
        view.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, 360))
        view.loadAd(AdRequest.Builder().build())
        adView = view
        lastLoadTime = System.currentTimeMillis()
    }

    fun isAdValid(): Boolean {
        return adView != null && System.currentTimeMillis() - lastLoadTime < expirationTimeInMs
    }

    fun getValidAdview(): AdView? {
        if(!isAdValid()) preLoad()
        (adView?.parent as? ViewGroup)?.removeView(adView)
        return adView
    }

    fun clear() {
        adView = null
        lastLoadTime = 0L
    }

}