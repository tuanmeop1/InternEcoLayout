package com.eco.musicplayer.audioplayer.music.utils

import com.android.billingclient.api.BillingClient

enum class BillingProductType(val googleType: String) {
    INAPP(BillingClient.ProductType.INAPP),
    SUBS(BillingClient.ProductType.SUBS);

    companion object {
        fun fromGoogleType(type: String): BillingProductType? {
            return entries.firstOrNull { it.googleType == type }
        }
    }
}