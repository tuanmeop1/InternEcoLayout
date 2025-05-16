package com.eco.musicplayer.audioplayer.model

import com.eco.musicplayer.audioplayer.music.utils.BillingProductType

data class ProductInfo(
    val productId: String,
    val type: BillingProductType,
    val title: String = "",
    val description: String = "",
    val formattedPrice: String = "",
    val priceAmountMicros: Long = 0,
    val billingPeriod: String? = null,
    val hasFreeTrial: Boolean = false,
    val hasIntroPrice: Boolean = false,
    val isSubscription: Boolean = type == BillingProductType.SUBS,
    val freeTrialPeriod: String? = null,
    val introPricePeriod: String? = null,
    val introFormattedPrice: String? = null,
    val offerToken: String? = null
)