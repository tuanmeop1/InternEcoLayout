package com.eco.musicplayer.audioplayer.music.ui.component.paywall.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.eco.musicplayer.audioplayer.music.manager.BillingConnectionState
import com.eco.musicplayer.audioplayer.music.manager.BillingManager
import com.eco.musicplayer.audioplayer.music.ui.component.paywall.state.SubscriptionUiState
import com.eco.musicplayer.audioplayer.music.utils.BillingConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    var currentPlan = 0

    private val subscriptionIds = listOf(
        BillingConstants.PRODUCT_ID_MONTHLY,
        BillingConstants.PRODUCT_ID_YEARLY
    )

    private val oneTimePurchaseIds = listOf(
        BillingConstants.PRODUCT_ID_LIFETIME
    )

    private val billingManager = BillingManager(
        application,
        viewModelScope,
        listSubscriptionId = subscriptionIds,
        listOneTimePurchaseId = oneTimePurchaseIds
    )

    private val _uiState = MutableStateFlow<SubscriptionUiState>(SubscriptionUiState.Loading)
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        observeBillingUpdates()
    }

    private fun observeBillingUpdates() {
        // Observe billing connection state
        viewModelScope.launch {
            billingManager.billingConnectionState.collect { state ->
                when (state) {
                    is BillingConnectionState.Connected -> updateUiState()
                    is BillingConnectionState.Error -> _uiState.value = SubscriptionUiState.Error("Billing error: ${state.code}")
                    is BillingConnectionState.Connecting, is BillingConnectionState.Disconnected -> _uiState.value = SubscriptionUiState.Loading
                }
            }
        }

        // Observe product details updates
        viewModelScope.launch {
            billingManager.availableProducts.collect {
                updateUiState()
            }
        }

        // Observe purchase updates
        viewModelScope.launch {
            billingManager.purchases.collect {
                updateUiState()
            }
        }
    }

    private fun updateUiState() {
        val products = billingManager.availableProducts.value
        val purchases = billingManager.purchases.value

        if (products.isEmpty()) {
            _uiState.value = SubscriptionUiState.Error("No subscription products available")
            return
        }

        // Check if user has any active subscriptions
        val hasActiveSubscription = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED && purchase.isAcknowledged
        }

        if (hasActiveSubscription) {
            _uiState.value = SubscriptionUiState.Subscribed(purchases)
        } else {
            _uiState.value = SubscriptionUiState.Available(sortProducts(products))
        }
    }

    // Initiates the purchase flow
    fun purchase(activity: Activity, productDetails: ProductDetails, offerToken: String? = null) {
        billingManager.launchBillingFlow(activity, productDetails, offerToken)
    }

    private fun sortProducts(products: List<ProductDetails>): List<ProductDetails> {
        val order = subscriptionIds + oneTimePurchaseIds

        return products.sortedBy { product ->
            order.indexOf(product.productId).takeIf { it >= 0 } ?: Int.MAX_VALUE
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}