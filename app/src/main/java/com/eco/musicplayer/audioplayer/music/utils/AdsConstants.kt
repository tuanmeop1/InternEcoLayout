package com.eco.musicplayer.audioplayer.music.utils

object AdsConstants {
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"
    const val NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    const val NATIVE_VIDEO_AD_UNIT_ID = "ca-app-pub-3940256099942544/1044960115"

    const val EXPIRATION_TIME_MS = 60 * 60 * 1000L

    enum class CollapseType(val value: String) {
        BOTTOM("bottom"),
        TOP("top")
    }
}