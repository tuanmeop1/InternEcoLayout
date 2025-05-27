package com.eco.musicplayer.audioplayer.model

import com.eco.musicplayer.audioplayer.music.utils.BillingProductType

abstract class BaseProductInfo {
    abstract val productId: String
    abstract val type: BillingProductType
    abstract val title: String
    abstract val description: String
    abstract val formattedPrice: String
    abstract val priceAmountMicros: Long
    abstract val billingPeriod: String?
    abstract val hasFreeTrial: Boolean
    abstract val hasIntroPrice: Boolean
    abstract val freeTrialPeriod: String?
    abstract val introPricePeriod: String?
    abstract val introFormattedPrice: String?
    abstract val offerToken: String?
}