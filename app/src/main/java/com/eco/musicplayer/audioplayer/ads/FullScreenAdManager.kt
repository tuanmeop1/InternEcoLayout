package com.eco.musicplayer.audioplayer.ads

class FullScreenAdManager {

    private var isFullScreenAdShowing: Boolean = false

    fun isAdShowing(): Boolean = isFullScreenAdShowing

    fun onAdStarted() {
        isFullScreenAdShowing = true
    }

    fun onAdDismissed() {
        isFullScreenAdShowing = false
    }

    fun tryShowAd(showAdBlock: () -> Unit): Boolean {
        if (isFullScreenAdShowing) return false
        isFullScreenAdShowing = true
        showAdBlock()
        return true
    }
}
