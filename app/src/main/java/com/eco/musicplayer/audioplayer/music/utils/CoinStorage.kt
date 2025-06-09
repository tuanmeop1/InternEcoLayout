package com.eco.musicplayer.audioplayer.music.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class CoinStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("reward_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_COIN = "key_coin"
    }

    fun getCoin(): Int {
        return prefs.getInt(KEY_COIN, 0)
    }

    fun addCoin(amount: Int) {
        val current = getCoin()
        prefs.edit() { putInt(KEY_COIN, current + amount) }
    }

    fun resetCoin() {
        prefs.edit() { putInt(KEY_COIN, 0) }
    }
}