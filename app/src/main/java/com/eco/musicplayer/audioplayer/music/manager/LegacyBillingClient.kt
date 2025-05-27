package com.eco.musicplayer.audioplayer.music.manager

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams.ReplacementMode
import com.eco.musicplayer.audioplayer.model.ProductInfo
import com.eco.musicplayer.audioplayer.music.utils.BillingConstants
import com.eco.musicplayer.audioplayer.music.utils.BillingProductType
import com.eco.musicplayer.audioplayer.music.utils.PurchaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume

/**
 * Implementation of BillingClientWrapper for older Android versions (using SkuDetails API)
 */
class LegacyBillingClient(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val sharedPref: PurchaseStorage
) : BillingClientWrapper, PurchasesUpdatedListener {

    private val TAG = "LegacyBillingClient"

    private val _billingConnectionState =
        MutableStateFlow<BillingConnectionState>(BillingConnectionState.Disconnected)
    override val billingConnectionState: StateFlow<BillingConnectionState> =
        _billingConnectionState.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    override val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private val skuDetailsMap = mutableMapOf<String, SkuDetails>()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        connect()
    }

    override fun connect() {
        _billingConnectionState.value = BillingConnectionState.Connecting
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    coroutineScope.launch {
                        try {
                            val productsDeferred = async { queryProducts() }
                            val purchasesDeferred = async { queryPurchases() }
                            productsDeferred.await()
                            purchasesDeferred.await()
                            _billingConnectionState.value = BillingConnectionState.Connected
                        } catch (e: Exception) {
                            Log.e(TAG, "Error during initial product query: ${e.message}")
                            _billingConnectionState.value = BillingConnectionState.Connected
                        }
                    }
                } else {
                    _billingConnectionState.value =
                        BillingConnectionState.Error(billingResult.responseCode)
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingConnectionState.value = BillingConnectionState.Disconnected
                retryConnection()
            }
        })
    }

    private fun retryConnection() {
        // Simple retry implementation
        connect()
    }

    override suspend fun queryProducts() {
        withContext(Dispatchers.IO) {
            var successfulQueries = 0

            // Query subscription products
            if (BillingConstants.subscriptionProductIds.isNotEmpty()) {
                try {
                    val subsParams = SkuDetailsParams.newBuilder()
                        .setSkusList(BillingConstants.subscriptionProductIds)
                        .setType(BillingClient.SkuType.SUBS)
                        .build()

                    val subsDeferred = async {
                        billingClient.querySkuDetails(subsParams)
                    }
                    val subsResult = subsDeferred.await()

                    if (subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        subsResult.skuDetailsList?.forEach { skuDetails ->
                            skuDetailsMap[skuDetails.sku] = skuDetails
                            Log.d(TAG, "Added subscription SKU: ${skuDetails.sku}, price: ${skuDetails.price}")
                        }

                        successfulQueries++
                    } else {
                        Log.e(TAG, "Failed to query subscription SKUs: ${subsResult.billingResult.responseCode}, " +
                                "message: ${subsResult.billingResult.debugMessage}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception while querying subscription products: ${e.message}")
                }
            }

            // Query one-time purchase products
            if (BillingConstants.oneTimePurchaseProductIds.isNotEmpty()) {
                try {
                    val inAppParams = SkuDetailsParams.newBuilder()
                        .setSkusList(BillingConstants.oneTimePurchaseProductIds)
                        .setType(BillingClient.SkuType.INAPP)
                        .build()

                    val inAppDeferred = async {
                        billingClient.querySkuDetails(inAppParams)
                    }
                    val inAppResult = inAppDeferred.await()

                    if (inAppResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                        inAppResult.skuDetailsList?.forEach { skuDetails ->
                            skuDetailsMap[skuDetails.sku] = skuDetails
                            Log.d(TAG, "Added one-time SKU: ${skuDetails.sku}, price: ${skuDetails.price}")
                        }

                        successfulQueries++
                    } else {
                        Log.e(TAG, "Failed to query one-time SKUs: ${inAppResult.billingResult.responseCode}, " +
                                "message: ${inAppResult.billingResult.debugMessage}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception while querying one-time products: ${e.message}")
                }
            }

            if (successfulQueries == 0 && (BillingConstants.subscriptionProductIds.isNotEmpty() ||
                        BillingConstants.oneTimePurchaseProductIds.isNotEmpty())) {
                // No successful queries despite having product IDs
                Log.e(TAG, "No products were successfully queried. Billing might not be available.")

                // Try one more time with a delay
                delay(1000)
                retryQueryProducts()
            }
        }
    }

    private suspend fun retryQueryProducts() {
        Log.d(TAG, "Retrying product query...")
        withContext(Dispatchers.IO) {
            // Check if billing client is still connected
            if (billingClient.isReady) {
                // Try subscription products again
                if (BillingConstants.subscriptionProductIds.isNotEmpty()) {
                    try {
                        val subsParams = SkuDetailsParams.newBuilder()
                            .setSkusList(BillingConstants.subscriptionProductIds)
                            .setType(BillingClient.SkuType.SUBS)
                            .build()

                        val subsResult = billingClient.querySkuDetails(subsParams)

                        if (subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "Retry successful: queried ${subsResult.skuDetailsList?.size ?: 0} subscription SKUs")

                            subsResult.skuDetailsList?.forEach { skuDetails ->
                                skuDetailsMap[skuDetails.sku] = skuDetails
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception in retry query of subscription products: ${e.message}")
                    }
                }

                // Try one-time products again
                if (BillingConstants.oneTimePurchaseProductIds.isNotEmpty()) {
                    try {
                        val inAppParams = SkuDetailsParams.newBuilder()
                            .setSkusList(BillingConstants.oneTimePurchaseProductIds)
                            .setType(BillingClient.SkuType.INAPP)
                            .build()

                        val inAppResult = billingClient.querySkuDetails(inAppParams)

                        if (inAppResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "Retry successful: queried ${inAppResult.skuDetailsList?.size ?: 0} one-time purchase SKUs")

                            inAppResult.skuDetailsList?.forEach { skuDetails ->
                                skuDetailsMap[skuDetails.sku] = skuDetails
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception in retry query of one-time products: ${e.message}")
                    }
                }
            } else {
                Log.e(TAG, "Billing client not ready during retry. Attempting to reconnect...")
                connect()
            }
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun queryPurchases() {
        val purchases = mutableListOf<Purchase>()

        // Query subscription purchases
        val subsResult = billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS)
        if (subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases.addAll(subsResult.purchasesList ?: emptyList())
        }

        // Query one-time purchases
        val inAppResult = billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP)
        if (inAppResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases.addAll(inAppResult.purchasesList ?: emptyList())
        }

        _purchases.value = purchases

        sharedPref.updateAcknowledgedProducts(purchases)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            // Process new purchases
            coroutineScope.launch {
                purchases.forEach { purchase ->
                    handlePurchase(purchase)
                }
                queryPurchases() // Refresh purchases list
            }
        } else {
            Log.e(TAG, "Purchase failed with code: ${billingResult.responseCode}")
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                val result = suspendCancellableCoroutine { continuation ->
                    billingClient.acknowledgePurchase(acknowledgeParams) { billingResult ->
                        continuation.resume(billingResult)
                    }
                }

                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    grantEntitlement(purchase)

                } else {
                    Log.e(TAG, "Failed to acknowledge purchase: ${result.responseCode}")
                }
            } else {
                grantEntitlement(purchase)

            }
        } else {
            Log.w(TAG, "Purchase not in PURCHASED state: ${purchase.purchaseState}")
        }
    }

    private fun grantEntitlement(purchase: Purchase) {
        // This implementation depends on your app's subscription logic
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            @Suppress("DEPRECATION")
            val skusList = purchase.skus // For older versions we use skus instead of products
            val productId = skusList.firstOrNull() ?: return
            sharedPref.saveAcknowledgedProductId(productId)
            Log.d(TAG, "Granting entitlement for product: $productId")

            // Handle subscription products
            if (BillingConstants.subscriptionProductIds.contains(productId)) {
                // Grant subscription entitlement
                Log.d(TAG, "Granting subscription entitlement")
            }

            // Handle one-time purchase products
            if (BillingConstants.oneTimePurchaseProductIds.contains(productId)) {
                // Grant lifetime entitlement
                Log.d(TAG, "Granting lifetime entitlement")
            }
        }
    }

    override fun launchBillingFlow(activity: Activity, productId: String, offerToken: String?) {
        val skuDetails = skuDetailsMap[productId] ?: run {
            Log.e(TAG, "SkuDetails not available for $productId")
            return
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun purchaseOrChange(
        activity: Activity,
        productId: String,
        offerToken: String?,
        oldPurchaseToken: String?,
        prorationMode: Int?
    ) {
        val skuDetails = skuDetailsMap[productId] ?: run {
            Log.e(TAG, "SkuDetails not available for $productId")
            return
        }

        val builder = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)

        // Add subscription update params if needed
        oldPurchaseToken?.let { token ->
            val updateMode = prorationMode ?: ReplacementMode.CHARGE_PRORATED_PRICE
            val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(token)
                .setSubscriptionReplacementMode(updateMode)
                .build()
            builder.setSubscriptionUpdateParams(updateParams)
        }

        billingClient.launchBillingFlow(activity, builder.build())
    }

    // Helper method to find SKU for a purchase token
    private fun getSkuForPurchaseToken(purchaseToken: String): String? {
        return purchases.value.find { it.purchaseToken == purchaseToken }?.let {
            @Suppress("DEPRECATION")
            it.skus.firstOrNull()
        }
    }

    override suspend fun consumePurchase(purchaseToken: String): Boolean {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        val result = suspendCancellableCoroutine { continuation ->
            billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                continuation.resume(billingResult)
            }
        }

        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun getProductInfo(productId: String): ProductInfo? {
        val skuDetails = skuDetailsMap[productId] ?: return null

        val type = if (skuDetails.type == BillingClient.SkuType.SUBS) {
            BillingProductType.SUBS
        } else {
            BillingProductType.INAPP
        }

        return ProductInfo(
            productId = productId,
            type = type,
            title = skuDetails.title,
            description = skuDetails.description,
            formattedPrice = skuDetails.price,
            priceAmountMicros = skuDetails.priceAmountMicros,
            billingPeriod = if (type == BillingProductType.SUBS) skuDetails.subscriptionPeriod else null,
            hasFreeTrial = skuDetails.freeTrialPeriod.isNotEmpty(),
            hasIntroPrice = skuDetails.introductoryPrice.isNotEmpty(),
            freeTrialPeriod = skuDetails.freeTrialPeriod.ifEmpty { null },
            introPricePeriod = skuDetails.introductoryPricePeriod.ifEmpty { null },
            introFormattedPrice = skuDetails.introductoryPrice.ifEmpty { null }
        )
    }

    override fun getFormattedPrice(productId: String): String {
        return skuDetailsMap[productId]?.price ?: ""
    }

    override fun getPriceAmountMicros(productId: String): Long {
        return skuDetailsMap[productId]?.priceAmountMicros ?: 0
    }

    override fun hasFreeTrial(productId: String): Boolean {
        val skuDetails = skuDetailsMap[productId] ?: return false

        if (skuDetails.type != BillingClient.SkuType.SUBS) {
            return false
        }

        return skuDetails.freeTrialPeriod.isNotEmpty()
    }

    override fun hasIntroPrice(productId: String): Boolean {
        val skuDetails = skuDetailsMap[productId] ?: return false

        if (skuDetails.type != BillingClient.SkuType.SUBS) {
            return false
        }

        return skuDetails.introductoryPrice.isNotEmpty() &&
                skuDetails.introductoryPriceAmountMicros > 0 &&
                skuDetails.introductoryPriceCycles > 0
    }

    override fun hasActiveProduct(productId: String): Boolean {
        return purchases.value.any { purchase ->
            @Suppress("DEPRECATION")
            purchase.skus.contains(productId) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    purchase.isAcknowledged
        }
    }

    override fun disconnect() {
        billingClient.endConnection()
    }
}