package com.eco.musicplayer.audioplayer.music.manager

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object BillingClientFactory {

    suspend fun create(context: Context, coroutineScope: CoroutineScope): BillingClientWrapper {
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

                        tempClient.endConnection()

                        val clientWrapper = if (supportsProductDetails) {
                            NewBillingClient(context, coroutineScope)
                        } else {
                            LegacyBillingClient(context, coroutineScope)
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
