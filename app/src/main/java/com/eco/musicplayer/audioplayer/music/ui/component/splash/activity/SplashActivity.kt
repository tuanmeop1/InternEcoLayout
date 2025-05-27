package com.eco.musicplayer.audioplayer.music.ui.component.splash.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.manager.BillingClientProvider
import com.eco.musicplayer.audioplayer.music.manager.BillingConnectionState
import com.eco.musicplayer.audioplayer.music.manager.BillingManager
import com.eco.musicplayer.audioplayer.music.ui.MainActivity
import com.eco.musicplayer.audioplayer.music.utils.PurchaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {

    private val purchaseStorage: PurchaseStorage by inject()
    private val coroutineScope: CoroutineScope by inject()
    private val billingClient: BillingManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        lifecycleScope.launch {
            try {
                billingClient.connect()
                billingClient.billingConnectionState.collect { state ->
                    if (state is BillingConnectionState.Connected) {
                        billingClient.queryPurchases()
                        billingClient.disconnect()
                        goToMainScreen()
                    } else if (state is BillingConnectionState.Error) {
                        Toast.makeText(this@SplashActivity, "Error: ${state.code}", Toast.LENGTH_SHORT).show()
                        goToMainScreen()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

    private fun goToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}
