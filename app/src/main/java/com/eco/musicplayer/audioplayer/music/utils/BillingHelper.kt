package com.eco.musicplayer.audioplayer.music.utils

import android.content.Context
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.android.billingclient.api.ProductDetails
import com.eco.musicplayer.audioplayer.model.BaseProductInfo
import com.eco.musicplayer.audioplayer.model.ProductInfo
import com.eco.musicplayer.audioplayer.model.ProductViewBinding
import com.eco.musicplayer.audioplayer.music.R

fun getSortedProductBindings(
    products: Map<String, BaseProductInfo>,
    productMap: Map<String, Pair<ViewGroup, AppCompatTextView>>
): List<ProductViewBinding> {
    return products.values
        .sortedBy { product ->
            product.billingPeriod?.let { parsePeriodToDays(it) } ?: Int.MAX_VALUE
        }
        .mapNotNull { product ->
            productMap[product.productId]?.let { pair ->
                ProductViewBinding(product.productId, pair)
            }
        }
}

fun isFreeTrial(phases: List<ProductDetails.PricingPhase>): Boolean {
    return phases.isNotEmpty() && phases.first().priceAmountMicros == 0L
}

fun isIntroPricing(phases: List<ProductDetails.PricingPhase>): Boolean {
    return phases.size >= 2 && phases[0].priceAmountMicros > 0 && phases[0].billingCycleCount > 0
}

fun parsePeriodToDays(period: String): Int {
    // Example: P3D = 3 days, P1W = 7 days, P1M = ~30 days
    return when {
        period.contains("D") -> period.filter { it.isDigit() }.toInt()
        period.contains("W") -> period.filter { it.isDigit() }.toInt() * 7
        period.contains("M") -> period.filter { it.isDigit() }.toInt() * 30
        period.contains("Y") -> period.filter { it.isDigit() }.toInt() * 365
        else -> -1
    }
}

fun parsePeriodToReadableText(period: String, context: Context): String {
    return when {
        period.startsWith("P") && period.contains("D") -> context.getString(R.string.week)
        period.contains("W") -> context.getString(R.string.week)
        period.contains("M") -> context.getString(R.string.month)
        period.contains("Y") -> context.getString(R.string.year)
        else -> context.getString(R.string.unknown)
    }
}