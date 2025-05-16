package com.eco.musicplayer.audioplayer.music.ui.component.paywall.state

import com.android.billingclient.api.Purchase
import com.eco.musicplayer.audioplayer.model.ProductInfo

sealed class SubscriptionUiState {
    data object Loading : SubscriptionUiState()
    data class Available(val products: List<ProductInfo>) : SubscriptionUiState()
    data class Subscribed(val purchases: List<Purchase>) : SubscriptionUiState()
    data class Error(val message: String) : SubscriptionUiState()
}