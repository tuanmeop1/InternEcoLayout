package com.eco.musicplayer.audioplayer.ads.app_open

interface AppOpenAdListener {
    fun onAdLoaded()
    fun onAdShowed()
    fun onShowAdComplete()
    fun onAdFailedToShow(error: String)
    fun onAdNotAvailable()
}