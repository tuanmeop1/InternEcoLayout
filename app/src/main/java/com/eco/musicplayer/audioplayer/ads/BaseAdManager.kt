package com.eco.musicplayer.audioplayer.ads

import android.app.Activity

interface BaseAdManager {
    fun isAdLoading(): Boolean
    fun isAdLoaded(): Boolean
    fun showAd(activity: Activity, action: ((Boolean) -> Unit)? = null){}
    fun showAd(activity: Activity)
}
