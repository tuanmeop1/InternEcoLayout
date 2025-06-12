package com.eco.musicplayer.audioplayer.ads

class AdCoolOffTime {
    private var lastTimeFullScreenAdShow = 0L
    private val coolOffTime = 15000L

    fun setLastTimeFullScreenAdShow() {
        lastTimeFullScreenAdShow = System.currentTimeMillis()
    }

    fun isAdCoolOff(): Boolean {
        return System.currentTimeMillis() - lastTimeFullScreenAdShow >= coolOffTime
    }
}