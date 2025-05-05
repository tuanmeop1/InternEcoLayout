package com.eco.musicplayer.audioplayer.music.manager

sealed class BillingConnectionState {
    data object Connecting : BillingConnectionState()
    data object Connected : BillingConnectionState()
    data object Disconnected : BillingConnectionState()
    data class Error(val code: Int) : BillingConnectionState()
}