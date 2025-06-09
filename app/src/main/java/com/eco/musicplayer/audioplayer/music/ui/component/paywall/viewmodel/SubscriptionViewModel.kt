package com.eco.musicplayer.audioplayer.music.ui.component.paywall.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.eco.musicplayer.audioplayer.model.BaseProductInfo
import com.eco.musicplayer.audioplayer.music.manager.BillingConnectionState
import com.eco.musicplayer.audioplayer.music.manager.BillingManager
import com.eco.musicplayer.audioplayer.music.ui.component.paywall.state.SubscriptionUiState
import com.eco.musicplayer.audioplayer.music.utils.BillingConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(private val billingClient: BillingManager) : ViewModel() {

    private val TAG = "SubscriptionVM"

    private val _uiState = MutableStateFlow<SubscriptionUiState>(SubscriptionUiState.Loading)
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    val products = mutableMapOf<String, BaseProductInfo>()

    private var productsFetched = false


    fun start() {
        _uiState.value = SubscriptionUiState.Loading

        viewModelScope.launch {
            try {
                observeBillingUpdates()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize billing client: ${e.message}")
                _uiState.value = SubscriptionUiState.Error("Failed to initialize billing client")
            }
        }
    }

    private fun observeBillingUpdates() {
        // Observe billing connection state
        viewModelScope.launch {
            billingClient.connect()
            billingClient.billingConnectionState.collect { state ->
                Log.d(TAG, "Billing connection state changed: $state")
                when (state) {
                    is BillingConnectionState.Connected -> {
                        // When connected, fetch products first
                        Log.d(TAG, "Billing client connected, fetching products...")
                        fetchProductInfo()
                    }

                    is BillingConnectionState.Error -> {
                        Log.e(TAG, "Billing connection error: ${state.code}")
                        _uiState.value = SubscriptionUiState.Error("Billing error: ${state.code}")
                    }

                    is BillingConnectionState.Connecting -> {
                        Log.d(TAG, "Billing client connecting...")
                        _uiState.value = SubscriptionUiState.Loading
                    }

                    is BillingConnectionState.Disconnected -> {
                        Log.d(TAG, "Billing client disconnected")
                        _uiState.value = SubscriptionUiState.Loading
                    }

                    is BillingConnectionState.Purchased -> {
                        Log.d(TAG, "Billing client query purchase completed")
                    }
                }
            }
        }

        // Observe purchase updates
        viewModelScope.launch {
            billingClient.purchases.collect {
                Log.d(TAG, "Purchases updated, count: ${it.size}")
                if (productsFetched) {
                    updateUiState()
                }
            }
        }
    }

    private fun fetchProductInfo() {
        viewModelScope.launch {
            try {
                // Set loading state while fetching
                _uiState.value = SubscriptionUiState.Loading

                // Clear existing product info
                products.clear()
                productsFetched = false

                var productCount = 0

                BillingConstants.subscriptionProductIds.forEach { productId ->
                    billingClient.getProductInfo(productId)?.let {
                        products[productId] = it
                        productCount++
                        Log.d(TAG, "Added subscription product: $productId")
                    } ?: Log.e(TAG, "Failed to get product info for subscription: $productId")
                }

                BillingConstants.oneTimePurchaseProductIds.forEach { productId ->
                    billingClient.getProductInfo(productId)?.let {
                        products[productId] = it
                        productCount++
                        Log.d(TAG, "Added one-time product: $productId")
                    } ?: Log.e(TAG, "Failed to get product info for one-time purchase: $productId")
                }

                productsFetched = true

                updateUiState()

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching product info: ${e.message}")
                productsFetched = true  // Mark as fetched even on error
                updateUiState()
            }
        }
    }

    private fun updateUiState() {
        val purchases = billingClient.purchases.value

        if (products.isEmpty()) {
            _uiState.value = SubscriptionUiState.Error("No subscription products available")
            return
        }

        // Check if user has any active subscriptions
        val hasActiveSubscription = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED && purchase.isAcknowledged
        }
        _uiState.value = SubscriptionUiState.Available(products.values.toList(), purchases)
//        if (hasActiveSubscription) {
//            _uiState.value = SubscriptionUiState.Subscribed(purchases)
//        } else {
//            _uiState.value = SubscriptionUiState.Available(products.values.toList())
//        }
    }

    // Initiates the purchase flow
//    fun purchase(activity: Activity, productId: String, offerToken: String? = null) {
//        Log.d(TAG, "Launching purchase flow for product: $productId")
//        billingClient.launchBillingFlow(activity, productId, offerToken)
//    }

    fun purchaseOrChange(
        activity: Activity,
        productId: String,
        offerToken: String?
    ) {
        Log.d(TAG, "Processing purchase/change for product: $productId")
        // Find existing purchase that matches this product
        val existingPurchase = billingClient.purchases.value.firstOrNull { it.isAcknowledged }

        when {
            existingPurchase == null -> {
                Log.d(TAG, "null")
                // New purchase
                billingClient.purchaseOrChange(
                    activity = activity,
                    productId = productId,
                    offerToken = offerToken
                )
            }

            else -> {
                Log.d(TAG, "not null")
                // Changing subscription
                val replaceMode =
                    BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_PRORATED_PRICE
                billingClient.purchaseOrChange(
                    activity = activity,
                    productId = productId,
                    offerToken = offerToken,
                    oldPurchaseToken = existingPurchase.purchaseToken,
                    replaceMode = replaceMode
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel being cleared, disconnecting billing client")
        billingClient.disconnect()
    }
}