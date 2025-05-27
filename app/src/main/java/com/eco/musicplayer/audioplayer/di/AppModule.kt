package com.eco.musicplayer.audioplayer.di

import android.app.Application
import android.content.Context
import com.eco.musicplayer.audioplayer.music.manager.BillingClientFactory
import com.eco.musicplayer.audioplayer.music.manager.BillingClientProvider
import com.eco.musicplayer.audioplayer.music.manager.BillingClientWrapper
import com.eco.musicplayer.audioplayer.music.manager.BillingManager
import com.eco.musicplayer.audioplayer.music.ui.component.paywall.viewmodel.SubscriptionViewModel
import com.eco.musicplayer.audioplayer.music.utils.BillingConstants
import com.eco.musicplayer.audioplayer.music.utils.PurchaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.core.qualifier.named

val purchasePrefs = named("purchasePrefs")
val typePrefs = named("typePrefs")

val billingModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    single(purchasePrefs) {
        androidContext().getSharedPreferences(BillingConstants.PURCHASE_PREF, Context.MODE_PRIVATE)
    }

    single(typePrefs) {
        androidContext().getSharedPreferences(BillingConstants.BILLING_TYPE_PREF, Context.MODE_PRIVATE)
    }

    single {
        PurchaseStorage(
            purchasePrefs = get(purchasePrefs),
            typePrefs = get(typePrefs)
        )
    }

    factory {
        BillingManager(context = get(), coroutineScope = get(), purchaseStorage = get())
    }

    viewModel {
        SubscriptionViewModel(billingClient = get<BillingManager>())
    }
}