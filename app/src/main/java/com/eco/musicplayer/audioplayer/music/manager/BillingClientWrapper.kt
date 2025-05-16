package com.eco.musicplayer.audioplayer.music.manager

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import com.android.billingclient.api.Purchase
import com.eco.musicplayer.audioplayer.model.ProductInfo
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface to abstract both old (SkuDetails) and new (ProductDetails) Google Play Billing APIs
 */
interface BillingClientWrapper {
    val billingConnectionState: StateFlow<BillingConnectionState>
    val purchases: StateFlow<List<Purchase>>

    /**
     * Connects to Google Play Billing service
     */
    fun connect()

    /**
     * Disconnect from Google Play Billing service
     */
    fun disconnect()

    /**
     * Query available products (subscribtions and one-time)
     */
    suspend fun queryProducts()

    /**
     * Query existing purchases
     */
    suspend fun queryPurchases()

    /**
     * Launch the billing flow for a product
     */
    fun launchBillingFlow(activity: Activity, productId: String, offerToken: String? = null)

    /**
     * Launch purchase flow for upgrading/downgrading subscription
     */
    fun purchaseOrChange(
        activity: Activity,
        productId: String,
        offerToken: String? = null,
        oldPurchaseToken: String? = null,
        prorationMode: Int? = null
    )

    /**
     * Consume a one-time purchase to allow re-purchase
     */
    suspend fun consumePurchase(purchaseToken: String): Boolean

    /**
     * Get product details for display
     */
    fun getProductInfo(productId: String): ProductInfo?

    /**
     * Get localized price for a product
     */
    fun getFormattedPrice(productId: String): String

    /**
     * Get price amount in micros for a product
     */
    fun getPriceAmountMicros(productId: String): Long

    /**
     * Check if a product has free trial
     */
    fun hasFreeTrial(productId: String): Boolean

    /**
     * Check if a product has introductory pricing
     */
    fun hasIntroPrice(productId: String): Boolean

    /**
     * Check if user has an active subscription or one-time purchase
     */
    fun hasActiveProduct(productId: String): Boolean
}