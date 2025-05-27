package com.eco.musicplayer.audioplayer.music.manager

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams.ReplacementMode
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.android.billingclient.api.querySkuDetails
import com.eco.musicplayer.audioplayer.model.BaseProductInfo
import com.eco.musicplayer.audioplayer.music.utils.BillingConstants
import com.eco.musicplayer.audioplayer.music.utils.PurchaseStorage
import com.eco.musicplayer.audioplayer.music.utils.toProductInfo
import com.eco.musicplayer.audioplayer.music.utils.toSkuInfo
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
    context: Context,
    private val coroutineScope: CoroutineScope,
    private val purchaseStorage: PurchaseStorage

) : PurchasesUpdatedListener {

    private val TAG = "BillingManager"

    private val _billingConnectionState =
        MutableStateFlow<BillingConnectionState>(BillingConnectionState.Disconnected)

    val billingConnectionState: StateFlow<BillingConnectionState> =
        _billingConnectionState.asStateFlow()

    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val availableProducts: StateFlow<List<ProductDetails>> = _availableProducts

    private val _availableSkuProducts = MutableStateFlow<List<SkuDetails>>(emptyList())
    val availableSkuProducts: StateFlow<List<SkuDetails>> = _availableSkuProducts

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private val productsMap = mutableMapOf<String, BaseProductInfo>()
    private val productDetailsMap = mutableMapOf<String, ProductDetails>()
    private val skuDetailsMap = mutableMapOf<String, SkuDetails>()

    private var isSupportProductDetails: Boolean = true

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

    fun connect() {
        _billingConnectionState.value = BillingConnectionState.Connecting
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    val featureResponse =
                        billingClient.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS)
                    isSupportProductDetails =
                        featureResponse.responseCode == BillingClient.BillingResponseCode.OK

                    Log.d(TAG, "ProductDetails support: $isSupportProductDetails")

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

    suspend fun queryProducts() {
        if (isSupportProductDetails) {
            Log.d(TAG, "Using ProductDetails API")
            queryProductsByDetail()
        } else {
            Log.d(TAG, "Using SkuDetails API (fallback)")
            queryProductsSku()
        }
    }

    private suspend fun queryProductsByDetail() {
        withContext(Dispatchers.IO) {
            val subsDeferred = if (subscriptionProducts.isNotEmpty()) {
                async {
                    try {
                        val subsParams = QueryProductDetailsParams.newBuilder()
                            .setProductList(subscriptionProducts)
                            .build()
                        billingClient.queryProductDetails(subsParams)
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception querying subscription products: ${e.message}")
                        null
                    }
                }
            } else null

            val inAppDeferred = if (oneTimeProducts.isNotEmpty()) {
                async {
                    try {
                        val inAppParams = QueryProductDetailsParams.newBuilder()
                            .setProductList(oneTimeProducts)
                            .build()
                        billingClient.queryProductDetails(inAppParams)
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception querying one-time products: ${e.message}")
                        null
                    }
                }
            } else null

            val subsResult = subsDeferred?.await()
            subsResult?.takeIf { it.billingResult.responseCode == BillingClient.BillingResponseCode.OK }
                ?.let { result ->
                    Log.d(
                        TAG,
                        "Successfully queried ${result.productDetailsList?.size ?: 0} subscription products"
                    )
                    result.productDetailsList?.forEach { product ->
                        productDetailsMap[product.productId] = product
                        productsMap[product.productId] = product.toProductInfo()
                        Log.d(
                            TAG,
                            "Added subscription product: ${product.productId}, title: ${product.title}"
                        )
                    }
                    _availableProducts.value += (result.productDetailsList ?: emptyList())
                } ?: Log.e(TAG, "Failed to query subscription products or exception occurred")

            val inAppResult = inAppDeferred?.await()
            inAppResult?.takeIf { it.billingResult.responseCode == BillingClient.BillingResponseCode.OK }
                ?.let { result ->
                    Log.d(
                        TAG,
                        "Successfully queried ${result.productDetailsList?.size ?: 0} one-time products"
                    )
                    result.productDetailsList?.forEach { product ->
                        productDetailsMap[product.productId] = product
                        productsMap[product.productId] = product.toProductInfo()
                        Log.d(
                            TAG,
                            "Added one-time product: ${product.productId}, title: ${product.title}"
                        )
                    }
                    _availableProducts.value += (result.productDetailsList ?: emptyList())
                } ?: Log.e(TAG, "Failed to query one-time products or exception occurred")
        }
    }

    private suspend fun queryProductsSku() {
        withContext(Dispatchers.IO) {
            val allSkuDetails = mutableListOf<SkuDetails>()

            val subsDeferred = if (BillingConstants.subscriptionProductIds.isNotEmpty()) {
                async {
                    try {
                        val subsParams = SkuDetailsParams.newBuilder()
                            .setSkusList(BillingConstants.subscriptionProductIds)
                            .setType(BillingClient.SkuType.SUBS)
                            .build()
                        billingClient.querySkuDetails(subsParams)
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception while querying subscription SKUs: ${e.message}")
                        null
                    }
                }
            } else null

            val inAppDeferred = if (BillingConstants.oneTimePurchaseProductIds.isNotEmpty()) {
                async {
                    try {
                        val inAppParams = SkuDetailsParams.newBuilder()
                            .setSkusList(BillingConstants.oneTimePurchaseProductIds)
                            .setType(BillingClient.SkuType.INAPP)
                            .build()
                        billingClient.querySkuDetails(inAppParams)
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception while querying one-time SKUs: ${e.message}")
                        null
                    }
                }
            } else null

            val subsResult = subsDeferred?.await()
            val inAppResult = inAppDeferred?.await()

            subsResult?.takeIf {
                it.billingResult.responseCode == BillingClient.BillingResponseCode.OK
            }?.skuDetailsList?.forEach { skuDetails ->
                skuDetailsMap[skuDetails.sku] = skuDetails
                productsMap[skuDetails.sku] = skuDetails.toSkuInfo()
                allSkuDetails.add(skuDetails)
                Log.d(TAG, "Added subscription SKU: ${skuDetails.sku}, price: ${skuDetails.price}")
            }

            inAppResult?.takeIf {
                it.billingResult.responseCode == BillingClient.BillingResponseCode.OK
            }?.skuDetailsList?.forEach { skuDetails ->
                skuDetailsMap[skuDetails.sku] = skuDetails
                productsMap[skuDetails.sku] = skuDetails.toSkuInfo()
                allSkuDetails.add(skuDetails)
                Log.d(TAG, "Added one-time SKU: ${skuDetails.sku}, price: ${skuDetails.price}")
            }

            _availableSkuProducts.value = allSkuDetails
        }
    }


    suspend fun queryPurchases() {
        // Query subscription purchases
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        // Query one-time purchases
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        withContext(Dispatchers.IO) {
            val subsDeferred = async {
                billingClient.queryPurchasesAsync(subsParams)
            }

            val inAppDeferred = async {
                billingClient.queryPurchasesAsync(inAppParams)
            }

            val subsResult = subsDeferred.await()
            val inAppResult = inAppDeferred.await()

            val allPurchases = subsResult.purchasesList + inAppResult.purchasesList
            _purchases.value = allPurchases
            purchaseStorage.updateAcknowledgedProducts(allPurchases)
        }
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
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val productId = purchase.products[0]
            purchaseStorage.saveAcknowledgedProductId(productId)
            Log.d(TAG, "Granting entitlement for product: $productId")

            if (BillingConstants.subscriptionProductIds.contains(productId)) {
                Log.d(TAG, "Granting subscription entitlement")
            }

            if (BillingConstants.oneTimePurchaseProductIds.contains(productId)) {
                Log.d(TAG, "Granting lifetime entitlement")
            }
        }
    }

//    fun launchBillingFlow(activity: Activity, productId: String, offerToken: String?) {
//        if (isSupportProductDetails) {
//            launchBillingFlowWithProductDetails(activity, productId, offerToken)
//        } else {
//            launchBillingFlowWithSkuDetails(activity, productId)
//        }
//    }
//
//    private fun launchBillingFlowWithProductDetails(
//        activity: Activity,
//        productId: String,
//        offerToken: String?
//    ) {
//        val productDetails = productDetailsMap[productId] ?: run {
//            Log.e(TAG, "Product details not available for $productId")
//            return
//        }
//
//        val productDetailsParamsList = listOf(
//            BillingFlowParams.ProductDetailsParams.newBuilder()
//                .setProductDetails(productDetails)
//                .apply {
//                    offerToken?.let { token ->
//                        setOfferToken(token)
//                    }
//                }
//                .build()
//        )
//
//        val billingFlowParams = BillingFlowParams.newBuilder()
//            .setProductDetailsParamsList(productDetailsParamsList)
//            .build()
//
//        billingClient.launchBillingFlow(activity, billingFlowParams)
//    }
//
//    private fun launchBillingFlowWithSkuDetails(activity: Activity, productId: String) {
//        val skuDetails = skuDetailsMap[productId] ?: run {
//            Log.e(TAG, "SKU details not available for $productId")
//            return
//        }
//
//        val billingFlowParams = BillingFlowParams.newBuilder()
//            .setSkuDetails(skuDetails)
//            .build()
//
//        billingClient.launchBillingFlow(activity, billingFlowParams)
//    }

    fun purchaseOrChange(
        activity: Activity,
        productId: String,
        offerToken: String?,
        oldPurchaseToken: String? = null,
        replaceMode: Int = ReplacementMode.CHARGE_PRORATED_PRICE
    ) {
        if (isSupportProductDetails) {
            purchaseOrChangeWithProductDetails(
                activity,
                productId,
                offerToken,
                oldPurchaseToken,
                replaceMode
            )
        } else {
            purchaseOrChangeWithSkuDetails(
                activity,
                productId,
                oldPurchaseToken,
                replaceMode
            )
        }
    }

    private fun purchaseOrChangeWithProductDetails(
        activity: Activity,
        productId: String,
        offerToken: String?,
        oldPurchaseToken: String?,
        replaceMode: Int = ReplacementMode.CHARGE_PRORATED_PRICE
    ) {
        val productDetails = productDetailsMap[productId] ?: run {
            Log.e(TAG, "Product details not available for $productId")
            return
        }

        val isSubscription = productDetails.productType == BillingClient.ProductType.SUBS

        if (isSubscription && offerToken == null) {
            Log.e(TAG, "Subscription requires non-null offerToken for product: $productId")
            return
        }

        val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (isSubscription) {
            productParamsBuilder.setOfferToken(offerToken!!)
        }

        val flowBuilder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParamsBuilder.build()))

        if (isSubscription && oldPurchaseToken != null) {
            val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(oldPurchaseToken)
                .setSubscriptionReplacementMode(ReplacementMode.CHARGE_FULL_PRICE)
                .build()
            flowBuilder.setSubscriptionUpdateParams(updateParams)
        }

        val result = billingClient.launchBillingFlow(activity, flowBuilder.build())
        Log.d(TAG, "launchBillingFlow (ProductDetails) result: ${result}, oldPurchaseToken: $oldPurchaseToken")
    }

    private fun purchaseOrChangeWithSkuDetails(
        activity: Activity,
        productId: String,
        oldPurchaseToken: String? = null,
        replaceMode: Int = ReplacementMode.CHARGE_PRORATED_PRICE
    ) {
        val skuDetails = skuDetailsMap[productId] ?: run {
            Log.e(TAG, "SKU details not available for $productId")
            return
        }

        val flowBuilder = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)

        if (skuDetails.type == BillingClient.SkuType.SUBS && oldPurchaseToken != null) {
            val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(oldPurchaseToken)
                .setSubscriptionReplacementMode(replaceMode)
                .build()
            flowBuilder.setSubscriptionUpdateParams(updateParams)
        }

        val result = billingClient.launchBillingFlow(activity, flowBuilder.build())
        Log.d(TAG, "launchBillingFlow (SkuDetails) result: ${result.responseCode}")
    }

    suspend fun consumePurchase(purchaseToken: String): Boolean {
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

    fun getProductInfo(productId: String): BaseProductInfo? {
        return productsMap[productId]
    }

    fun disconnect() {
        billingClient.endConnection()
    }
}