package com.eco.musicplayer.audioplayer.music.ui.component.admob.fragment

import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.eco.musicplayer.audioplayer.ads.BaseAdManager
import com.eco.musicplayer.audioplayer.ads.RewardDialog
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
import com.eco.musicplayer.audioplayer.music.utils.CountUpTimer
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdmobLoverFragment : BaseFragmentBinding<FragmentAdmobLoverBinding>(), AppOpenAdAllowed {

    private val TAG = "AdmobLoverFragment"

    private val coinStorage: CoinStorage by inject()
    private val interstitialAdManager: InterstitialAdManager by inject()
    private val rewardedAdManager: RewardedAdManager by inject()
    private val rewardedInterstitialAdManager: RewardedInterstitialAdManager by inject()

    private val viewModel: AdmobLoverViewModel by viewModel()

    private val rewardAdDialog: RewardDialog by lazy {
        RewardDialog()
    }

    private val loadAdTimer: CountUpTimer by lazy {
        CountUpTimer(lifecycleScope, 100, 3000)
    }

    private val showAdTimer: CountUpTimer by lazy {
        CountUpTimer(lifecycleScope, 100, 1000)
    }

    override fun getContentViewId(): Int = R.layout.fragment_admob_lover

    override fun onResume() {
        super.onResume()

        loadAdTimer.resume()
        showAdTimer.resume()
    }

    override fun onPause() {
        super.onPause()
        loadAdTimer.pause()
        showAdTimer.pause()
    }

    override fun initializeData() {
        Log.d(TAG, this.toString())
        //isDisplayed = args.isDisplayed
    }

    override fun initializeViews() {
        createInterstitialAd()
        createRewardedInterstitialAd()
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
            if (interstitialAdManager.shouldShowAd()) {
                showInterstitialAd()
            } else {
                findNavController().navigate(R.id.action_admobLoverFragment_to_admobDestinationFragment)
            }
        }

        binding.llShowRewardedInterstitialAd.setOnClickListener {
            showRewardedInterstitialAd()
        }

        binding.llShowRewaredAd.setOnClickListener {
            showRewardDialog(onRewardAccepted = {
                showRewardedAd()
            })
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
                loadFullScreenAdDialog.hideDialog()
                findNavController().navigate(R.id.action_admobLoverFragment_to_admobDestinationFragment)
            }

            override fun onAdDismissed() {
                Log.d(TAG, "Interstitial Ad is dismissed")
                loadFullScreenAdDialog.hideDialog()
                loadAdTimer.destroy()
                showAdTimer.destroy()
                findNavController().navigate(R.id.action_admobLoverFragment_to_admobDestinationFragment)
            }

            override fun onAdFailedToShow(adError: AdError) {
                Log.d(TAG, "Interstitial Ad FAILED to show: ${adError.message}")
                loadFullScreenAdDialog.hideDialog()
                findNavController().navigate(R.id.action_admobLoverFragment_to_admobDestinationFragment)
            }

        })
        interstitialAdManager.loadAd()
    }

    private fun createRewardedAd() {
        rewardedAdManager.setAdUnitId(AdsConstants.REWARDED_AD_UNIT_ID)
        rewardedAdManager.setListener(object : RewardedAdListener {
            override fun onAdLoaded() {
                Log.d(TAG, "Rewarded Ad Loaded Successfully")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.d(TAG, "Rewarded Ad FAILED to load")
                loadFullScreenAdDialog.hideDialog()
            }

            override fun onAdDismissed() {
                rewardedAdManager.loadAd()
                loadFullScreenAdDialog.hideDialog()
                loadAdTimer.destroy()
                showAdTimer.destroy()
                Log.d(TAG, "Rewarded Ad Dissmised")
            }

            override fun onAdFailedToShow(adError: AdError) {
                Log.d(TAG, "Rewarded Ad Failed to show: ${adError.message}")
                loadFullScreenAdDialog.hideDialog()
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
                loadFullScreenAdDialog.hideDialog()
                findNavController().navigate(R.id.action_admobLoverFragment_to_admobDestinationFragment)
            }

            override fun onAdDismissed() {
                Log.d(TAG, "Rewarded Ad Dismissed")
                loadFullScreenAdDialog.hideDialog()
                loadAdTimer.destroy()
                showAdTimer.destroy()
                findNavController().navigate(R.id.action_admobLoverFragment_to_admobDestinationFragment)
            }

            override fun onAdFailedToShow(adError: AdError) {
                Log.d(TAG, "Rewarded Ad Failed to show: ${adError.message}")
                loadFullScreenAdDialog.hideDialog()
                findNavController().navigate(R.id.action_admobLoverFragment_to_admobDestinationFragment)
            }

            override fun onAdNotReady() {
                Log.d(TAG, "Rewarded Ad not ready")
            }

            override fun onUserEarnedReward(amount: Int) {
                coinStorage.addCoin(amount)
                viewModel.addCoins(amount)
            }

        })

        rewardedInterstitialAdManager.loadAd()
    }

    private fun showRewardedInterstitialAd() {
        setUpShowAdTimer(rewardedInterstitialAdManager) {
            loadFullScreenAdDialog.hideDialog()
            findNavController().navigate(R.id.action_admobLoverFragment_to_admobDestinationFragment)
        }
    }

    private fun showRewardedAd() {
        setUpShowAdTimer(rewardedAdManager) {
            loadFullScreenAdDialog.hideDialog()
        }
    }

    private fun showInterstitialAd() {
        showAd(interstitialAdManager) {
            findNavController().navigate(R.id.action_admobLoverFragment_to_admobDestinationFragment)
        }

//        setUpShowAdTimer(interstitialAdManager) {
//            loadFullScreenAdDialog.hideDialog()
//            findNavController().navigate(R.id.action_admobLoverFragment_to_admobDestinationFragment)
//        }
    }

    private fun setUpShowAdTimer(adManager: BaseAdManager, action: (() -> Unit)) {
        loadFullScreenAdDialog.showDialog()
        if(adManager.isAdLoading()) {
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
            action()
        }
    }

    private fun showRewardDialog(onRewardAccepted: () -> Unit) {
        val dialog = RewardDialog()
        dialog.onRewardAccepted = onRewardAccepted
        dialog.showDialog(parentFragmentManager)
    }
}