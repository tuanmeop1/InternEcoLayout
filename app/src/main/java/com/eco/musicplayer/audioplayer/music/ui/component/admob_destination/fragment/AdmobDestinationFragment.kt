package com.eco.musicplayer.audioplayer.music.ui.component.admob_destination.fragment

import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.eco.musicplayer.audioplayer.ads.BaseAdManager
import com.eco.musicplayer.audioplayer.ads.LoadFullScreenAdDialog
import com.eco.musicplayer.audioplayer.ads.interstitial.InterstitialAdListener
import com.eco.musicplayer.audioplayer.ads.interstitial.InterstitialAdManager
import com.eco.musicplayer.audioplayer.ads.nativee.NativeAdListener
import com.eco.musicplayer.audioplayer.ads.nativee.NativeAdManager
import com.eco.musicplayer.audioplayer.ads.rewarded_interstitial.RewardedInterstitialAdListener
import com.eco.musicplayer.audioplayer.ads.rewarded_interstitial.RewardedInterstitialAdManager
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.FragmentAdmobDestinationBinding
import com.eco.musicplayer.audioplayer.music.ui.base.AppOpenAdAllowed
import com.eco.musicplayer.audioplayer.music.ui.base.BaseFragmentBinding
import com.eco.musicplayer.audioplayer.music.utils.AdsConstants
import com.eco.musicplayer.audioplayer.music.utils.CoinStorage
import com.eco.musicplayer.audioplayer.music.utils.CountUpTimer
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import org.koin.android.ext.android.inject

class AdmobDestinationFragment : BaseFragmentBinding<FragmentAdmobDestinationBinding>(),
    AppOpenAdAllowed {

    private val interstitialAdManager: InterstitialAdManager by inject()
    private val rewardedInterstitialAdManager: RewardedInterstitialAdManager by inject()
    private val nativeAdManager: NativeAdManager by inject()
    private val coinStorage: CoinStorage by inject()
    private val args: AdmobDestinationFragmentArgs by navArgs()

    private val loadAdTimer: CountUpTimer by lazy {
        CountUpTimer(lifecycleScope, 100, 3000)
    }

    private val showAdTimer: CountUpTimer by lazy {
        CountUpTimer(lifecycleScope, 100, 1000)
    }

    private val TAG = "AdmobDestinationFragment"

    override fun getContentViewId(): Int = R.layout.fragment_admob_destination

    override fun initializeData() {

    }

    override fun initializeViews() {
        createInterstitialAd()
        //createRewardedInterstitialAd()
        createNativeAd()
    }

    private fun initObserver() {

    }

    override fun registerListeners() {
        binding.ivBack.setOnClickListener {
            if (interstitialAdManager.shouldShowAd()) {
                showInterstitialAd()
            }
            else findNavController().navigateUp()
            //else findNavController().navigate(AdmobDestinationFragmentDirections.actionAdmobDestinationFragmentToAdmobLoverFragment(isDisplayed))
        }
    }

    private fun createInterstitialAd() {
        interstitialAdManager.setAdUnitId(AdsConstants.INTERSTITIAL_AD_UNIT_ID)
        interstitialAdManager.setListener(object : InterstitialAdListener {
            override fun onAdLoaded() {
                Log.d(TAG, "Interstitial Banner Ad Loaded Successfully")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.d(TAG, "Interstitial Banner Ad FAILED to load: ${error.message}")
                loadFullScreenAdDialog.hideDialog()
                findNavController().navigateUp()
            }

            override fun onAdDismissed() {
                Log.d(TAG, "Interstitial Banner Ad is dismissed")
                loadFullScreenAdDialog.hideDialog()
                loadAdTimer.destroy()
                showAdTimer.destroy()
                findNavController().navigateUp()
            }

            override fun onAdFailedToShow(adError: AdError) {
                Log.d(TAG, "Interstitial Ad FAILED to show: ${adError.message}")
                loadFullScreenAdDialog.hideDialog()
                findNavController().navigateUp()
            }

        })
        interstitialAdManager.loadAd()
    }

    private fun createRewardedInterstitialAd() {
        rewardedInterstitialAdManager.setAdUnitId(AdsConstants.REWARDED_INTERSTITIAL_AD_UNIT_ID)
        rewardedInterstitialAdManager.setListener(object : RewardedInterstitialAdListener {
            override fun onAdLoaded() {
                Log.d(TAG, "Rewarded Interstitial Ad Loaded Successfully")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.d(TAG, "Rewarded Interstitial Ad FAILED to load")
            }

            override fun onAdDismissed() {
                interstitialAdManager.loadAd()
                findNavController().navigateUp()
                Log.d(TAG, "Rewarded Ad Dissmised")
            }

            override fun onAdFailedToShow(adError: AdError) {
                Log.d(TAG, "Rewarded Ad Failed to show: ${adError.message}")
            }

            override fun onAdNotReady() {
                Log.d(TAG, "Rewarded Ad not ready")
            }

            override fun onUserEarnedReward(amount: Int) {
                coinStorage.addCoin(amount)
            }

        })

        rewardedInterstitialAdManager.loadAd()
    }

    private fun showInterstitialAd() {
        setUpShowAdTimer(interstitialAdManager) {
            findNavController().navigateUp()
        }
    }

    private fun setUpShowAdTimer(adManager: BaseAdManager, action: (() -> Unit)) {
        loadFullScreenAdDialog.showDialog()
        if(interstitialAdManager.isAdLoading()) {
            loadAdTimer.onTick = { adManager.showAd(requireActivity()) }
            loadAdTimer.onComplete = {
                if(adManager.isAdLoaded()) {
                    adManager.showAd(requireActivity())
                } else {
                    action()
                }
            }
            loadAdTimer.start()
        } else if(adManager.isAdLoaded()) {
            showAdTimer.onTick = { adManager.showAd(requireActivity()) }
            showAdTimer.onComplete = {
                adManager.showAd(requireActivity())
            }
            showAdTimer.start()
        } else {
            loadFullScreenAdDialog.hideDialog()
        }
    }

    private fun showRewardedInterstitialAd() {
        rewardedInterstitialAdManager.showAd(requireActivity())
    }

    private fun createNativeAd() {
        //nativeAdManager.setAdUnitId(AdsConstants.NATIVE_AD_UNIT_ID)
        nativeAdManager.setAdUnitId(AdsConstants.NATIVE_VIDEO_AD_UNIT_ID)
        nativeAdManager.setListener(listener = object : NativeAdListener {
            override fun onAdLoaded() {
                Log.d(TAG, "Native Ad Loaded Successfully")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.d(TAG, "Native Ad FAILED to load: ${error.message}")
            }

        })

        nativeAdManager.loadAd { ad ->
        binding.adViewContainer.loaded(ad)
        }
    }
}