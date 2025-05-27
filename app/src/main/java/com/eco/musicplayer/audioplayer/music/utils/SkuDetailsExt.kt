package com.eco.musicplayer.audioplayer.music.utils

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.eco.musicplayer.audioplayer.model.BaseProductInfo
import com.eco.musicplayer.audioplayer.model.SkuInfo

fun SkuDetails.toSkuInfo(): BaseProductInfo {
    val type = when (this.type) {
        BillingClient.SkuType.SUBS -> BillingProductType.SUBS
        else -> BillingProductType.INAPP
    }

    return SkuInfo(
        skuDetail = this,
        productId = sku,
        type = type,
        title = title,
        description = description,
        formattedPrice = price,
        priceAmountMicros = priceAmountMicros,
        billingPeriod = if (type == BillingProductType.SUBS) {
            subscriptionPeriod.takeIf { it.isNotEmpty() }
        } else null,
        hasFreeTrial = freeTrialPeriod.isNotEmpty(),
        hasIntroPrice = introductoryPrice.isNotEmpty(),
        freeTrialPeriod = freeTrialPeriod.takeIf { it.isNotEmpty() },
        introPricePeriod = introductoryPricePeriod.takeIf { it.isNotEmpty() },
        introFormattedPrice = introductoryPrice.takeIf { it.isNotEmpty() }
    )
}