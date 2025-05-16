package com.eco.musicplayer.audioplayer.music.ui.component.paywall.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SubscriptionViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubscriptionViewModel::class.java)) {
            return SubscriptionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}