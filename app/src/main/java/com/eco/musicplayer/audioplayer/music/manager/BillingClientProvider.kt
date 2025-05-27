package com.eco.musicplayer.audioplayer.music.manager

import android.content.Context
import com.eco.musicplayer.audioplayer.music.utils.PurchaseStorage
import kotlinx.coroutines.CoroutineScope

object BillingClientProvider {
    @Volatile
    private var _instance: BillingClientWrapper? = null

    val instance: BillingClientWrapper
        get() = _instance ?: throw IllegalStateException("BillingClientWrapper is not initialized")

    suspend fun init(
        context: Context,
        coroutineScope: CoroutineScope,
        purchaseStorage: PurchaseStorage
    ) {
        if (_instance == null) {
            val created = BillingClientFactory.create(context, coroutineScope, purchaseStorage)

            synchronized(this) {
                if (_instance == null) {
                    _instance = created
                }
            }
        }
    }
}
