package com.eco.musicplayer.audioplayer.model

import com.android.billingclient.api.SkuDetails
import com.eco.musicplayer.audioplayer.music.utils.BillingProductType

data class SkuInfo(
    val skuDetail: SkuDetails? = null,
    override val productId: String = "",
    override val type: BillingProductType = BillingProductType.INAPP,
    override val title: String = "",
    override val description: String = "",
    override val formattedPrice: String = "",
    override val priceAmountMicros: Long = 0L,
    override val billingPeriod: String? = null,
    override val hasFreeTrial: Boolean = false,
    override val hasIntroPrice: Boolean = false,
    override val freeTrialPeriod: String? = null,
    override val introPricePeriod: String? = null,
    override val introFormattedPrice: String? = null,
    override val offerToken: String? = null
) : BaseProductInfo()