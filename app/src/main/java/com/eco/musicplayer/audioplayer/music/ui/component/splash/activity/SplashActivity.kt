package com.eco.musicplayer.audioplayer.music.ui.component.splash.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated

import com.eco.musicplayer.audioplayer.music.R

import com.eco.musicplayer.audioplayer.music.manager.BillingConnectionState
import com.eco.musicplayer.audioplayer.music.manager.BillingManager
import com.eco.musicplayer.audioplayer.music.ui.MainActivity
import com.eco.musicplayer.audioplayer.music.utils.PurchaseStorage
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {

    private val purchaseStorage: PurchaseStorage by inject()
    private val coroutineScope: CoroutineScope by inject()
    private val billingClient: BillingManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        lifecycleScope.launch(Dispatchers.IO) {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@SplashActivity) {}
        }
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    billingClient.connect()
                }

                billingClient.billingConnectionState.collect { state ->
                        when (state) {
                            is BillingConnectionState.Connected -> {
                                // only the blocking calls run on IO
                                withContext(Dispatchers.IO) {
                                    billingClient.queryPurchases()
                                    billingClient.disconnect()
                                }
                                goToMainScreen()              // Main-safe
                            }
                            is BillingConnectionState.Error -> {
                                Toast.makeText(
                                    this@SplashActivity,
                                    "Error: ${state.code}",
                                    Toast.LENGTH_SHORT
                                ).show()                     // Main-safe
                                goToMainScreen()
                            }
                            BillingConnectionState.Connecting -> Log.d("Splash", "Connecting")
                            BillingConnectionState.Disconnected -> billingClient.retryConnection()
                        }
                    }

            } catch (e: Exception) {
                // still on Main: show/log error and navigate
                Log.e("SplashActivity", "Billing flow failed", e)
                goToMainScreen()
            }
        }



    }

    private fun goToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}
