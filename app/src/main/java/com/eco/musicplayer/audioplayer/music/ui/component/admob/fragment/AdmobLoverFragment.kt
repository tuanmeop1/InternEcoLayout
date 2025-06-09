package com.eco.musicplayer.audioplayer.music.ui.component.admob.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.fragment.findNavController
import com.eco.musicplayer.audioplayer.ads.interstitial.InterstitialAdListener
import com.eco.musicplayer.audioplayer.ads.interstitial.InterstitialAdManager
import com.eco.musicplayer.audioplayer.ads.rewarded.RewardedAdListener
import com.eco.musicplayer.audioplayer.ads.rewarded.RewardedAdManager
import com.eco.musicplayer.audioplayer.ads.rewarded_interstitial.RewardedInterstitialAdListener
import com.eco.musicplayer.audioplayer.ads.rewarded_interstitial.RewardedInterstitialAdManager
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.FragmentAdmobLoverBinding
import com.eco.musicplayer.audioplayer.music.ui.base.AppOpenAdAllowed
import com.eco.musicplayer.audioplayer.music.ui.base.BaseFragmentBinding
import com.eco.musicplayer.audioplayer.music.ui.component.admob.viewmodel.AdmobLoverViewModel
import com.eco.musicplayer.audioplayer.music.utils.AdsConstants
import com.eco.musicplayer.audioplayer.music.utils.CoinStorage
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdmobLoverFragment : BaseFragmentBinding<FragmentAdmobLoverBinding>(), AppOpenAdAllowed {

    private var isDisplayed: Boolean = false
    private var isRIDisplayed: Boolean = false
    private val TAG = "AdmobLoverFragment"

    private val coinStorage: CoinStorage by inject()
    private val interstitialAdManager: InterstitialAdManager by inject()
    private val rewardedAdManager: RewardedAdManager by inject()
    private val rewardedInterstitialAdManager: RewardedInterstitialAdManager by inject()

    private val viewModel: AdmobLoverViewModel by viewModel()

    override fun getContentViewId(): Int = R.layout.fragment_admob_lover


    override fun initializeData() {
        //isDisplayed = args.isDisplayed
    }

    override fun initializeViews() {
        if(!isDisplayed) {
            createInterstitialAd()
        }

        if(!isRIDisplayed) createRewardedInterstitialAd()
        createRewardedAd()
        initObserver()
    }

    private fun initObserver() {
        viewModel.coins.observe(viewLifecycleOwner) { coins ->
            binding.tvCoin.text = coins.toString()
        }
    }

    override fun registerListeners() {
        binding.llShowInterstitialAd.setOnClickListener {
            if(!isDisplayed) showInterstitialAd()
            else findNavController().navigate(AdmobLoverFragmentDirections.actionAdmobLoverFragmentToAdmobDestinationFragment(isDisplayed, isRIDisplayed))
        }

        binding.llShowRewardedInterstitialAd.setOnClickListener {
            if(!isRIDisplayed) showRewardedInterstitialAd()
            else findNavController().navigate(AdmobLoverFragmentDirections.actionAdmobLoverFragmentToAdmobDestinationFragment(isDisplayed, isRIDisplayed))
        }

        binding.llShowRewaredAd.setOnClickListener {
            showRewardedAd()
        }

        binding.ivClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun createInterstitialAd() {
        interstitialAdManager.setAdUnitId(AdsConstants.INTERSTITIAL_AD_UNIT_ID)
        interstitialAdManager.setListener(object : InterstitialAdListener {
            override fun onAdLoaded() {
                Log.d(TAG, "Interstitial Ad Loaded Successfully")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.d(TAG, "Interstitial Ad FAILED to load")
            }

            override fun onAdDismissed() {
                isDisplayed = true
                Log.d(TAG, "Interstitial Ad is dismissed")
                Log.d(TAG, "showInterstitialAd: $isDisplayed")
                findNavController().navigate(AdmobLoverFragmentDirections.actionAdmobLoverFragmentToAdmobDestinationFragment(isDisplayed, isRIDisplayed))
            }

            override fun onAdFailedToShow(adError: AdError) {
                isDisplayed = false
                Log.d(TAG, "Interstitial Ad FAILED to show: ${adError.message}")
            }

        })
        interstitialAdManager.loadInterstitialAd()
    }

    private fun createRewardedAd() {
        rewardedAdManager.setAdUnitId(AdsConstants.REWARDED_AD_UNIT_ID)
        rewardedAdManager.setListener(object : RewardedAdListener {
            override fun onAdLoaded() {
                Log.d(TAG, "Rewarded Ad Loaded Successfully")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.d(TAG, "Rewarded Ad FAILED to load")
            }

            override fun onAdDismissed() {
                rewardedAdManager.loadAd()
                Log.d(TAG, "Rewarded Ad Dissmised")
            }

            override fun onAdFailedToShow(adError: AdError) {
                Log.d(TAG, "Rewarded Ad Failed to show: ${adError.message}")
            }

            override fun onUserEarnedReward(amount: Int) {
                coinStorage.addCoin(amount)
                viewModel.addCoins(amount)
            }

        })

        rewardedAdManager.loadAd()
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
                isRIDisplayed = true
                Log.d(TAG, "Rewarded Ad Dissmised")
                findNavController().navigate(AdmobLoverFragmentDirections.actionAdmobLoverFragmentToAdmobDestinationFragment(isDisplayed, isRIDisplayed))
            }

            override fun onAdFailedToShow(adError: AdError) {
                isRIDisplayed = false
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

    private fun showRewardedInterstitialAd() {
        rewardedInterstitialAdManager.showAd(requireActivity())
    }

    private fun showRewardedAd() {
        rewardedAdManager.showAd(requireActivity())
    }

    private fun showInterstitialAd() {
        interstitialAdManager.showAd(requireActivity())
    }
}