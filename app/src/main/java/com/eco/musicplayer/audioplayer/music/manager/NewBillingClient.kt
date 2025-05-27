package com.eco.musicplayer.audioplayer.music.manager

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams.ReplacementMode
import com.eco.musicplayer.audioplayer.model.BaseProductInfo
import com.eco.musicplayer.audioplayer.model.ProductInfo
import com.eco.musicplayer.audioplayer.music.utils.BillingConstants
import com.eco.musicplayer.audioplayer.music.utils.BillingProductType
import com.eco.musicplayer.audioplayer.music.utils.PurchaseStorage
import com.eco.musicplayer.audioplayer.music.utils.isFreeTrial
import com.eco.musicplayer.audioplayer.music.utils.isIntroPricing
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume

class NewBillingClient(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val sharedPref: PurchaseStorage

) : BillingClientWrapper, PurchasesUpdatedListener {

    private val TAG = "NewBillingClient"

    private val _billingConnectionState =
        MutableStateFlow<BillingConnectionState>(BillingConnectionState.Disconnected)
    override val billingConnectionState: StateFlow<BillingConnectionState> =
        _billingConnectionState.asStateFlow()

    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    private val availableProducts: StateFlow<List<ProductDetails>> = _availableProducts.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    override val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    // Define subscription products
    private val subscriptionProducts by lazy {
        BillingConstants.subscriptionProductIds.map { product ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(product)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
    }

    // Define one-time products
    private val oneTimeProducts by lazy {
        BillingConstants.oneTimePurchaseProductIds.map { product ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(product)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
    }

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
        connect()
    }

    override suspend fun queryProducts() {
        withContext(Dispatchers.IO) {
            var successfulQueries = 0

            // Query subscription products
            if (subscriptionProducts.isNotEmpty()) {
                try {
                    val subsParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(subscriptionProducts)
                        .build()

                    val subsDeferred = async { billingClient.queryProductDetails(subsParams) }
                    val subsResult = subsDeferred.await()

                    if (subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Successfully queried ${subsResult.productDetailsList?.size ?: 0} subscription products")

                        subsResult.productDetailsList?.forEach { product ->
                            productDetailsMap[product.productId] = product
                            Log.d(TAG, "Added subscription product: ${product.productId}, title: ${product.title}")
                        }

                        _availableProducts.value += (subsResult.productDetailsList ?: emptyList())
                        successfulQueries++
                    } else {
                        Log.e(TAG, "Failed to query subscription products: ${subsResult.billingResult.responseCode}, " +
                                "message: ${subsResult.billingResult.debugMessage}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception while querying subscription products: ${e.message}")
                }
            }

            // Query one-time purchase products
            if (oneTimeProducts.isNotEmpty()) {
                try {
                    val inAppParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(oneTimeProducts)
                        .build()

                    val inAppDeferred = async { billingClient.queryProductDetails(inAppParams) }
                    val inAppResult = inAppDeferred.await()

                    if (inAppResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        inAppResult.productDetailsList?.forEach { product ->
                            productDetailsMap[product.productId] = product
                            Log.d(TAG, "Added one-time product: ${product.productId}, title: ${product.title}")
                        }

                        _availableProducts.value += (inAppResult.productDetailsList ?: emptyList())
                        successfulQueries++
                    } else {
                        Log.e(TAG, "Failed to query one-time products: ${inAppResult.billingResult.responseCode}, " +
                                "message: ${inAppResult.billingResult.debugMessage}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception while querying one-time products: ${e.message}")
                }
            }

            if (successfulQueries == 0 && (subscriptionProducts.isNotEmpty() || oneTimeProducts.isNotEmpty())) {
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
                if (subscriptionProducts.isNotEmpty()) {
                    try {
                        val subsParams = QueryProductDetailsParams.newBuilder()
                            .setProductList(subscriptionProducts)
                            .build()

                        val subsResult = billingClient.queryProductDetails(subsParams)

                        if (subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "Retry successful: queried ${subsResult.productDetailsList?.size ?: 0} subscription products")

                            subsResult.productDetailsList?.forEach { product ->
                                productDetailsMap[product.productId] = product
                            }

                            _availableProducts.value += (subsResult.productDetailsList ?: emptyList())
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception in retry query of subscription products: ${e.message}")
                    }
                }

                // Try one-time products again
                if (oneTimeProducts.isNotEmpty()) {
                    try {
                        val inAppParams = QueryProductDetailsParams.newBuilder()
                            .setProductList(oneTimeProducts)
                            .build()

                        val inAppResult = billingClient.queryProductDetails(inAppParams)

                        if (inAppResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "Retry successful: queried ${inAppResult.productDetailsList?.size ?: 0} one-time purchase products")

                            inAppResult.productDetailsList?.forEach { product ->
                                productDetailsMap[product.productId] = product
                            }

                            _availableProducts.value += (inAppResult.productDetailsList ?: emptyList())
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

    override suspend fun queryPurchases() {
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

        sharedPref.updateAcknowledgedProducts(allPurchases)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            coroutineScope.launch {
                purchases.forEach { purchase ->
                    handlePurchase(purchase)
                }
                queryPurchases()
            }
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
            val productId = purchase.products[0]
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
        val productDetails = productDetailsMap[productId] ?: run {
            Log.e(TAG, "Product details not available for $productId")
            return
        }

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

    override fun purchaseOrChange(
        activity: Activity,
        productId: String,
        offerToken: String?,
        oldPurchaseToken: String?,
        prorationMode: Int?
    ) {
        val productDetails = productDetailsMap[productId] ?: run {
            Log.e(TAG, "Product details not available for $productId")
            return
        }

        // 1) build ProductDetailsParams for this flow
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

        // 3) if there is an existing subscription, attach update params
        oldPurchaseToken?.let { token ->
            val updateMode = prorationMode ?: ReplacementMode.CHARGE_PRORATED_PRICE
            val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(token)
                .setSubscriptionReplacementMode(updateMode)
                .build()
            flowBuilder.setSubscriptionUpdateParams(updateParams)
        }

        // 4) launch the Google Play Billing flow
        billingClient.launchBillingFlow(activity, flowBuilder.build())
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

    override fun getProductInfo(productId: String): BaseProductInfo? {
        val productDetails = productDetailsMap[productId] ?: return null

        val type = if (productDetails.productType == BillingClient.ProductType.SUBS) {
            BillingProductType.SUBS
        } else {
            BillingProductType.INAPP
        }

        // Get basic price information
        var formattedPrice = ""
        var priceAmountMicros = 0L
        var billingPeriod: String? = null
        var freeTrialPeriod: String? = null
        var introPricePeriod: String? = null
        var introFormattedPrice: String? = null
        var offerToken: String? = null

        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val offerDetails = productDetails.subscriptionOfferDetails?.firstOrNull()
            val pricingPhases = offerDetails?.pricingPhases?.pricingPhaseList

            // Get offer token
            offerToken = offerDetails?.offerToken

            // Find the base or final pricing phase
            val basePhase = pricingPhases?.lastOrNull { phase ->
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
                phase.priceAmountMicros > 0 && phase.billingCycleCount > 0
            }

            introPhase?.let {
                introPricePeriod = it.billingPeriod
                introFormattedPrice = it.formattedPrice
            }
        } else {
            // One-time purchase
            productDetails.oneTimePurchaseOfferDetails?.let {
                formattedPrice = it.formattedPrice
                priceAmountMicros = it.priceAmountMicros
            }
        }

        return ProductInfo(
            productDetails = productDetails,
            productId = productId,
            type = type,
            title = productDetails.title,
            description = productDetails.description,
            formattedPrice = formattedPrice,
            priceAmountMicros = priceAmountMicros,
            billingPeriod = billingPeriod,
            hasFreeTrial = hasFreeTrial(productId),
            hasIntroPrice = hasIntroPrice(productId),
            freeTrialPeriod = freeTrialPeriod,
            introPricePeriod = introPricePeriod,
            introFormattedPrice = introFormattedPrice,
            offerToken = offerToken
        )
    }

    override fun getFormattedPrice(productId: String): String {
        val productDetails = productDetailsMap[productId] ?: return ""

        return if (productDetails.productType == BillingClient.ProductType.SUBS) {
            productDetails.subscriptionOfferDetails?.firstOrNull()
                ?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice
                ?: ""
        } else {
            productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
        }
    }

    override fun getPriceAmountMicros(productId: String): Long {
        val productDetails = productDetailsMap[productId] ?: return 0

        return if (productDetails.productType == BillingClient.ProductType.SUBS) {
            productDetails.subscriptionOfferDetails?.firstOrNull()
                ?.pricingPhases?.pricingPhaseList?.lastOrNull()?.priceAmountMicros
                ?: 0
        } else {
            productDetails.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0
        }
    }

    override fun hasFreeTrial(productId: String): Boolean {
        val productDetails = productDetailsMap[productId] ?: return false

        if (productDetails.productType != BillingClient.ProductType.SUBS) {
            return false
        }

        val pricingPhases = productDetails.subscriptionOfferDetails
            ?.firstOrNull()?.pricingPhases?.pricingPhaseList

        return pricingPhases?.let { isFreeTrial(it) } ?: false
    }

    override fun hasIntroPrice(productId: String): Boolean {
        val productDetails = productDetailsMap[productId] ?: return false

        if (productDetails.productType != BillingClient.ProductType.SUBS) {
            return false
        }

        val pricingPhases = productDetails.subscriptionOfferDetails
            ?.firstOrNull()?.pricingPhases?.pricingPhaseList

        return pricingPhases?.let { isIntroPricing(it) } ?: false
    }

    override fun hasActiveProduct(productId: String): Boolean {
        return purchases.value.any { purchase ->
            purchase.products.contains(productId) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    purchase.isAcknowledged
        }
    }

    override fun disconnect() {
        billingClient.endConnection()
    }
}