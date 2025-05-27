package com.eco.musicplayer.audioplayer.music.manager

import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.eco.musicplayer.audioplayer.music.utils.PurchaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object BillingClientFactory {

    suspend fun create(context: Context, coroutineScope: CoroutineScope, purchaseStorage: PurchaseStorage): BillingClientWrapper {

        if (purchaseStorage.isSupportTypeCached()) {
            val supportsProductDetails = purchaseStorage.isSupportProductDetail()

            return if (supportsProductDetails) {
                NewBillingClient(context, coroutineScope, purchaseStorage)
            } else {
                LegacyBillingClient(context, coroutineScope, purchaseStorage)
            }
        }

        return suspendCancellableCoroutine { continuation ->
            val tempClient = BillingClient.newBuilder(context)
                .setListener { _, _ -> }
                .enablePendingPurchases()
                .build()

            tempClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    val responseCode = billingResult.responseCode

                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                        val featureResponse = tempClient.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS)
                        val supportsProductDetails =
                            featureResponse.responseCode == BillingClient.BillingResponseCode.OK
                        purchaseStorage.saveSupportProductDetail(supportsProductDetails)
                        tempClient.endConnection()

                        val clientWrapper = if (supportsProductDetails) {
                            NewBillingClient(context, coroutineScope, purchaseStorage)
                        } else {
                            LegacyBillingClient(context, coroutineScope, purchaseStorage)
                        }

                        continuation.resume(clientWrapper)
                    } else {
                        tempClient.endConnection()
                        continuation.resumeWithException(
                            RuntimeException("Billing setup failed: ${billingResult.debugMessage}")
                        )
                    }
                }

                override fun onBillingServiceDisconnected() {
                }
            })

            continuation.invokeOnCancellation {
                tempClient.endConnection()
            }
        }
    }
}
