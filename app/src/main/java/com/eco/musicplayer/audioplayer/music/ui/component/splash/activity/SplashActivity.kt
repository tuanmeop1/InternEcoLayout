package com.eco.musicplayer.audioplayer.music.ui.component.splash.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.eco.musicplayer.audioplayer.ads.app_open.AppOpenAdListener
import com.eco.musicplayer.audioplayer.ads.app_open.AppOpenAdManager
import com.eco.musicplayer.audioplayer.ads.banner.BannerAdPreloader
import com.eco.musicplayer.audioplayer.music.databinding.ActivitySplashBinding
import com.eco.musicplayer.audioplayer.music.manager.BillingConnectionState
import com.eco.musicplayer.audioplayer.music.manager.BillingManager
import com.eco.musicplayer.audioplayer.music.ui.MainActivity
import com.eco.musicplayer.audioplayer.music.utils.AdsConstants
import com.eco.musicplayer.audioplayer.music.utils.CountUpTimer
import com.eco.musicplayer.audioplayer.music.utils.PurchaseStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import kotlin.time.Duration.Companion.seconds

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(), AndroidScopeComponent {

    private val bannerAdPreloader: BannerAdPreloader by inject()
    private val purchaseStorage: PurchaseStorage by inject()
    private val billingClient: BillingManager by inject()
    private val appOpenAdManager: AppOpenAdManager by inject()

    private val countUpTimer: CountUpTimer by lazy {
        CountUpTimer(this, 100, 10000L)
    }
    private var timerCompleted = false
    private var isPremium: Boolean? = null
    private lateinit var binding: ActivitySplashBinding
    private var hasNavigated = false
    private var isBillingComplete = false
    private var isAdComplete = false
    private var billingJob: Job? = null
    private val billingTimeDuration = 3000L

    override val scope by activityScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("SplashLog", "OnCreate: Checkpoint")
        setupUI()

        if (purchaseStorage.isAnyProductAcknowledged()) goToMainScreen()

        setupBilling()
        setupAppOpenAd()
        setUpTimer()

        countUpTimer.start()
        lifecycleScope.launch {
            delay(billingTimeDuration)
            if (isBillingComplete) return@launch
            isPremium = false
        }
    }


    override fun onResume() {
        super.onResume()
        countUpTimer.resume()
    }

    override fun onPause() {
        super.onPause()
        countUpTimer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        billingJob?.cancel()
        billingClient.disconnect()
        countUpTimer.destroy()
        Log.e("SplashActivity", "Destroyed")
        appOpenAdManager.setListener(null)
    }

    private fun setupUI() {
        binding.progressBar.visibility = View.VISIBLE
    }


    private fun setupAppOpenAd() {
        appOpenAdManager.setAdUnitId(AdsConstants.APP_OPEN_AD_UNIT_ID)
        appOpenAdManager.setListener(object : AppOpenAdListener {
            override fun onAdLoaded() {
                Log.d("SplashActivity", "Ad loaded $this")
            }

            override fun onAdShowed() {
                Log.d("SplashActivity", "App open ad showed")
                hideLoadingUI()
            }

            override fun onShowAdComplete() {
                Log.d("SplashActivity", "App open ad completed")
                isAdComplete = true
                goToMainScreen()
            }

            override fun onAdFailedToShow(error: String) {
                Log.e("SplashActivity", "App open ad failed to show: $error")
                isAdComplete = true
                goToMainScreen()
            }

            override fun onAdNotAvailable() {
                Log.d("SplashActivity", "App open ad not available")
                isAdComplete = true
            }
        })
        appOpenAdManager.loadAd(this@SplashActivity)
    }

    private fun setupBilling() {
        billingJob = lifecycleScope.launch {
            try {
                billingClient.connectInSplash()

                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    billingClient.billingConnectionState.collect { state ->
                        if (state is BillingConnectionState.Purchased) {

                            if(state.isPremium) {
                                isPremium = true
                                goToMainScreen()
                            } else {
                                isPremium = false
                            }
                            isBillingComplete = true
                        }
//                        when (state) {
//                            is BillingConnectionState.Connected -> {
//
//                                isBillingComplete = true
//                                if (isPremium == true) {
//                                    goToMainScreen()
//                                }
//
//                                //purchaseStorage.fakePremium()
//                            }
//
//                            is BillingConnectionState.Error -> {
//                                Toast.makeText(
//                                    this@SplashActivity,
//                                    "Billing Error: ${state.code}",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                isBillingComplete = true
//                                checkIfReadyToNavigate()
//                            }
//
//                            BillingConnectionState.Connecting -> {
//                                Log.d("SplashActivity", "Billing connecting...")
//                            }
//
//                            BillingConnectionState.Disconnected -> {
//                                billingClient.retryConnection()
//                            }
//                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SplashActivity", "Billing flow failed", e)
                isBillingComplete = true
                checkIfReadyToNavigate()
            }
        }
    }

    private fun setUpTimer() {
        countUpTimer.onTick = {
            if (isBillingComplete) {
                if (!isPremium!!) if (appOpenAdManager.isAdAvailable()) {
                    appOpenAdManager.showAdIfAvailable(this@SplashActivity)
                } else {
                    appOpenAdManager.loadAd(this@SplashActivity)
                }
            }
        }

        countUpTimer.onComplete = {
            if (appOpenAdManager.isAdAvailable()) {
                appOpenAdManager.showAdIfAvailable(this@SplashActivity)
            } else goToMainScreen()
        }
    }

    private fun checkIfReadyToNavigate() {
        Log.d("SplashActivity", "Checking navigation: billing=$isBillingComplete, ad=$isAdComplete")
        if (isPremium == true) {
            goToMainScreen()
        } else if (isPremium == false) {
            appOpenAdManager.showAdIfAvailable(this@SplashActivity)
        } else if (isPremium == null && timerCompleted) {
            goToMainScreen()
        }
    }

    private fun goToMainScreen() {
        if (hasNavigated) return
        hasNavigated = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showLoadingUI() {
        runOnUiThread {
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private fun hideLoadingUI() {
        runOnUiThread {
            binding.progressBar.visibility = View.GONE
        }
    }

}
