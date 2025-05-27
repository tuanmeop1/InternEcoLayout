package com.eco.musicplayer.audioplayer.music.utils

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.eco.musicplayer.audioplayer.model.BaseProductInfo
import com.eco.musicplayer.audioplayer.model.ProductInfo

fun ProductDetails.toProductInfo(): BaseProductInfo {
    val type = when (productType) {
        BillingClient.ProductType.SUBS -> BillingProductType.SUBS
        else -> BillingProductType.INAPP
    }

    return when (type) {
        BillingProductType.SUBS -> toSubscriptionProductInfo()
        BillingProductType.INAPP -> toInAppProductInfo()
    }
}

private fun ProductDetails.toSubscriptionProductInfo(): ProductInfo {
    val offerDetails = subscriptionOfferDetails?.firstOrNull()
    val pricingPhases = offerDetails?.pricingPhases?.pricingPhaseList

    // Get basic price information
    var formattedPrice = ""
    var priceAmountMicros = 0L
    var billingPeriod: String? = null
    var freeTrialPeriod: String? = null
    var introPricePeriod: String? = null
    var introFormattedPrice: String? = null

    // Find the base or final pricing phase
    val basePhase = pricingPhases?.firstOrNull { phase ->
        phase.billingCycleCount == 0
    }

    basePhase?.let {
        formattedPrice = it.formattedPrice
        priceAmountMicros = it.priceAmountMicros
        billingPeriod = it.billingPeriod
    }

    // Check for free trial
    val freeTrialPhase = pricingPhases?.firstOrNull { phase ->
        phase.priceAmountMicros == 0L && phase.billingCycleCount > 0
    }

    freeTrialPhase?.let {
        freeTrialPeriod = it.billingPeriod
    }

    // Check for intro pricing
    val introPhase = pricingPhases?.firstOrNull { phase ->
        phase.priceAmountMicros > 0L && phase.billingCycleCount > 0
    }

    introPhase?.let {
        introPricePeriod = it.billingPeriod
        introFormattedPrice = it.formattedPrice
    }

    return ProductInfo(
        productDetails = this,
        productId = productId,
        type = BillingProductType.SUBS,
        title = title,
        description = description,
        formattedPrice = formattedPrice,
        priceAmountMicros = priceAmountMicros,
        billingPeriod = billingPeriod,
        hasFreeTrial = hasFreeTrial(),
        hasIntroPrice = hasIntroPrice(),
        freeTrialPeriod = freeTrialPeriod,
        introPricePeriod = introPricePeriod,
        introFormattedPrice = introFormattedPrice,
        offerToken = offerDetails?.offerToken
    )
}

private fun ProductDetails.toInAppProductInfo(): ProductInfo {
    val oneTimePurchase = oneTimePurchaseOfferDetails

    return ProductInfo(
        productDetails = this,
        productId = productId,
        type = BillingProductType.INAPP,
        title = title,
        description = description,
        formattedPrice = oneTimePurchase?.formattedPrice ?: "",
        priceAmountMicros = oneTimePurchase?.priceAmountMicros ?: 0L,
    )
}

private fun ProductDetails.hasFreeTrial(): Boolean {
    if (productType != BillingClient.ProductType.SUBS) {
        return false
    }

    val pricingPhases = subscriptionOfferDetails
        ?.firstOrNull()?.pricingPhases?.pricingPhaseList

    return pricingPhases?.any { phase ->
        phase.priceAmountMicros == 0L && phase.billingCycleCount > 0
    } ?: false
}

private fun ProductDetails.hasIntroPrice(): Boolean {
    if (productType != BillingClient.ProductType.SUBS) {
        return false
    }

    val pricingPhases = subscriptionOfferDetails
        ?.firstOrNull()?.pricingPhases?.pricingPhaseList

    return pricingPhases?.any { phase ->
        phase.priceAmountMicros > 0L && phase.billingCycleCount > 0
    } ?: false
}
