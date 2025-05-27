package com.eco.musicplayer.audioplayer.music.utils

import android.content.SharedPreferences
import androidx.core.content.edit
import com.android.billingclient.api.Purchase

class PurchaseStorage(
    private val purchasePrefs: SharedPreferences,
    private val typePrefs: SharedPreferences
) {

    private val ALL_PRODUCT_IDS = listOf(
        BillingConstants.SUB_1,
        BillingConstants.SUB_2,
        BillingConstants.INAPP
    )

    fun isSupportTypeCached(): Boolean {
        return typePrefs.contains(BillingConstants.IS_SUPPORT_PRODUCT_DETAIL)
    }

    fun saveSupportProductDetail(support: Boolean) {
        typePrefs.edit {
            putBoolean(BillingConstants.IS_SUPPORT_PRODUCT_DETAIL, support)
        }
    }

    fun isSupportProductDetail(): Boolean =
        typePrefs.getBoolean(BillingConstants.IS_SUPPORT_PRODUCT_DETAIL, false)

    fun saveAcknowledgedProductId(productId: String) {
        purchasePrefs.edit {
            putBoolean(productId, true)
        }
    }

    fun getAcknowledgedProductIds(): List<String> =
        ALL_PRODUCT_IDS.filter { purchasePrefs.getBoolean(it, false) }

    fun isProductAcknowledged(productId: String): Boolean =
        purchasePrefs.getBoolean(productId, false)

    fun isAnyProductAcknowledged(): Boolean = getAcknowledgedProductIds().isNotEmpty()

    fun updateAcknowledgedProducts(purchases: List<Purchase>) {
        purchasePrefs.edit {
            ALL_PRODUCT_IDS.forEach { putBoolean(it, false) }
        }

        purchases.forEach { purchase ->
            if (purchase.isAcknowledged) {
                purchase.products.forEach { productId ->
                    if (productId in ALL_PRODUCT_IDS) {
                        saveAcknowledgedProductId(productId)
                    }
                }
            }
        }
    }
}
