package com.eco.musicplayer.audioplayer.music.manager

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams.ReplacementMode
import com.eco.musicplayer.audioplayer.model.ProductInfo
import com.eco.musicplayer.audioplayer.music.utils.BillingConstants
import com.eco.musicplayer.audioplayer.music.utils.BillingProductType
import com.eco.musicplayer.audioplayer.music.utils.isBasePlan
import com.eco.musicplayer.audioplayer.music.utils.isFreeTrial
import com.eco.musicplayer.audioplayer.music.utils.isIntroPricing
import com.eco.musicplayer.audioplayer.music.utils.parsePeriodToDays
import com.eco.musicplayer.audioplayer.music.utils.parsePeriodToReadableText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class BillingManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
) : PurchasesUpdatedListener {

    private val _billingConnectionState =
        MutableStateFlow<BillingConnectionState>(BillingConnectionState.Disconnected)
    val billingConnectionState: StateFlow<BillingConnectionState> =
        _billingConnectionState.asStateFlow()

    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val availableProducts: StateFlow<List<ProductDetails>> = _availableProducts.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    // Define subscription products based on the products list
    private val subscriptionProducts by lazy {
        BillingConstants.subscriptionProductIds.map { product ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(product)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
    }

    // Define one-time purchase products based on the products list
    private val oneTimeProducts by lazy {
        BillingConstants.oneTimePurchaseProductIds.map { product ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(product)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
    }

    init {
        connectToGooglePlay()
    }

    private fun connectToGooglePlay() {
        _billingConnectionState.value = BillingConnectionState.Connecting
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _billingConnectionState.value = BillingConnectionState.Connected
                    coroutineScope.launch {
                        queryProductDetails()
                        queryPurchases()
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
        connectToGooglePlay()
    }

    private suspend fun queryProductDetails() {
        withContext(Dispatchers.IO) {
            if (subscriptionProducts.isNotEmpty()) {
                val subsParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(subscriptionProducts)
                    .build()

                val subsDeferred = async { billingClient.queryProductDetails(subsParams) }
                val subsResult = subsDeferred.await()
                Log.d("BillingManager", "Subscription products: ${subsResult.productDetailsList}")

                _availableProducts.value += (subsResult.productDetailsList ?: emptyList())
            }

            if (oneTimeProducts.isNotEmpty()) {
                val inAppParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(oneTimeProducts)
                    .build()

                val inAppDeferred = async { billingClient.queryProductDetails(inAppParams) }
                val inAppResult = inAppDeferred.await()

                Log.d("BillingManager", "One-time purchase products: ${inAppResult.productDetailsList}")

                _availableProducts.value += (inAppResult.productDetailsList ?: emptyList())
            }
        }
    }

    private suspend fun queryPurchases() {
        // Query subscription purchases
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val subsResult = billingClient.queryPurchasesAsync(subsParams)

        // Query one-time purchases
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val inAppResult = billingClient.queryPurchasesAsync(inAppParams)

        // Combine the results
        val allPurchases = subsResult.purchasesList + inAppResult.purchasesList
        _purchases.value = allPurchases
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
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        // Grant entitlement
        grantEntitlement(purchase)

        // Acknowledge purchase if needed
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            val result = suspendCancellableCoroutine { continuation ->
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    continuation.resume(billingResult)
                }
            }

            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                // Handle acknowledgement error
                Log.e("BillingManager", "Failed to acknowledge purchase: ${result.responseCode}")
            }
        }
    }

    private fun grantEntitlement(purchase: Purchase) {
        // This implementation depends on your app's subscription logic
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val productId = purchase.products[0]
            Log.d("BillingManager", "Granting entitlement for product: $productId")

            // Handle subscription products
            if (BillingConstants.subscriptionProductIds.contains(productId)) {
                // Grant subscription entitlement
                Log.d("BillingManager", "Granting subscription entitlement")
            }

            // Handle one-time purchase products
            if (BillingConstants.oneTimePurchaseProductIds.contains(productId)) {
                // Grant lifetime entitlement
                Log.d("BillingManager", "Granting lifetime entitlement")
            }
        }
    }

    fun launchBillingFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String? = null
    ) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .apply {
                    offerToken?.let { token ->
                        setOfferToken(token)
                    }
                }
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    fun purchaseOrChange(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String? = null,
        oldPurchase: Purchase? = null,
        prorationMode: Int = ReplacementMode.CHARGE_PRORATED_PRICE
    ) {
        // 1) build the one ProductDetailsParams for this flow
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .apply {
                // only subscriptions need an offer token
                offerToken?.let { setOfferToken(it) }
            }
            .build()

        // 2) prepare the BillingFlowParams builder
        val flowBuilder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))

        // 3) if there is an existing subscription, attach update (proration) params
        oldPurchase?.let { purchase ->
            val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(purchase.purchaseToken)
                .setSubscriptionReplacementMode(prorationMode)
                .build()
            flowBuilder.setSubscriptionUpdateParams(updateParams)
        }

        // 4) launch the Google Play Billing flow
        billingClient.launchBillingFlow(activity, flowBuilder.build())
    }


    fun consumePurchase(purchase: Purchase, onSuccess: () -> Unit, onError: (Int) -> Unit) {
        coroutineScope.launch {
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            val result = suspendCancellableCoroutine { continuation ->
                billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                    continuation.resume(billingResult)
                }
            }

            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                onSuccess()
            } else {
                onError(result.responseCode)
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}