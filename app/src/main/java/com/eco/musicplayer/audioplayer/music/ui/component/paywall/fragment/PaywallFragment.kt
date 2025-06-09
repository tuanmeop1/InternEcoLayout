package com.eco.musicplayer.audioplayer.music.ui.component.paywall.fragment

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.eco.musicplayer.audioplayer.ads.banner.BannerAdListener
import com.eco.musicplayer.audioplayer.ads.banner.BannerAdManager
import com.eco.musicplayer.audioplayer.ads.banner.BannerAdPreloader
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.FragmentPaywallBinding
import com.eco.musicplayer.audioplayer.music.ui.base.AppOpenAdAllowed
import com.eco.musicplayer.audioplayer.music.ui.base.BaseFragmentBinding
import com.eco.musicplayer.audioplayer.music.ui.component.bottom_sheet.BottomSheetDialogPaywall
import com.eco.musicplayer.audioplayer.music.ui.component.bottom_sheet.BottomSheetPaywallEditing
import com.eco.musicplayer.audioplayer.music.utils.AdsConstants
import com.eco.musicplayer.audioplayer.music.utils.BillingConstants
import com.eco.musicplayer.audioplayer.music.utils.PurchaseStorage
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.koin.android.ext.android.inject


class PaywallFragment : BaseFragmentBinding<FragmentPaywallBinding>(), AppOpenAdAllowed {
    private lateinit var bottomSheet: BottomSheetPaywallEditing
    private lateinit var paywallDialog: BottomSheetDialogPaywall
    private val purchaseStorage: PurchaseStorage by inject()
    private val bannerAdPreloader: BannerAdPreloader by inject()
    private val bannerAdManager: BannerAdManager by inject()

    override fun getContentViewId(): Int = R.layout.fragment_paywall

    override fun onPause() {
        super.onPause()
        bannerAdManager.pause()
    }

    override fun onResume() {
        super.onResume()
        bannerAdManager.resume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bannerAdManager.destroy()
    }

    override fun initializeData() {
        val originalPrice = "$32.99 "
        val price = "$27.99 "
        val offPriceValue = 40
        val oldPrice = 35.00
        val discountedPrice = 29.99

        paywallDialog = BottomSheetDialogPaywall(
            context = requireContext(), // or requireContext() if in Fragment
            discount = offPriceValue,
            oldPrice = oldPrice,
            discountedPrice = discountedPrice
        )
        if (!::bottomSheet.isInitialized) {
            bottomSheet =
                BottomSheetPaywallEditing.newInstance(offPriceValue, oldPrice, discountedPrice)
        }

//        binding.apply {
//            //tvRemoveAds.text = setRemoveAdsSpannable(firstPart, secondPart)
//            tvBulletInfo.text = buildBulletList()
//
//            lifeTime.tvOriginalPrice.text = originalPrice
//            lifeTime.tvPrice.text = price
//
//            oneWeek.tvOriginalPrice.text = originalPrice
//            oneWeek.tvPrice.text = price
//            oneWeek.tvBestOffer.visibility = View.GONE
//            oneWeek.tvTitle.text = getString(R.string.one_week)
//            oneWeek.tvDescription.text = getString(R.string.cancel_anytime)
//
//            monthLy.tvOriginalPrice.text = originalPrice
//            monthLy.tvPrice.text = price
//            monthLy.tvBestOffer.visibility = View.GONE
//            monthLy.tvTitle.text = getString(R.string.monthly)
//            monthLy.tvDescription.text = getString(R.string.billed_monthly)
//
//        }

    }

    override fun initializeViews() {
        checkPremiumStatus()
        //createAnchorBannerAds()
        //createInlineBannerAds()
        createCollapsibleInlineBannerAds()
    }

    override fun registerListeners() {
        //setupToggleSubscriptionOptions()
        binding.apply {
//            llContinue.setOnClickListener {
//                //bottomSheet.show(parentFragmentManager, "MyBottomSheet")
//                paywallDialog.show()
//            }

            llPremium.setOnClickListener {
                findNavController().navigate(R.id.action_paywallFragment_to_simpleBillingFragment)
            }

            llFeature.setOnClickListener {
                //findNavController().navigate(R.id.action_paywallFragment_to_paywallFeatureFragment)
                findNavController().navigate(R.id.action_paywallFragment_to_admobLoverFragment)
            }
        }
    }

//    private fun setRemoveAdsSpannable(firstPart: String, secondPart: String): SpannableStringBuilder {
//        val firstPartSize = resources.getDimension(com.intuit.sdp.R.dimen._24sdp)
//        val secondPartSize = resources.getDimension(com.intuit.sdp.R.dimen._32sdp)
//        val textSizeRatioPart2ToPart1 = secondPartSize / firstPartSize
//
//        val firstTypeFace = Typeface.create(
//            ResourcesCompat.getFont(requireContext(), R.font.nunito_sans_bold), Typeface.NORMAL
//        )
//
//        val secondTypeFace = Typeface.create(
//            ResourcesCompat.getFont(requireContext(), R.font.nunito_sans_extra_bold), Typeface.NORMAL
//        )
//
//        val firstPartStyled = SpannableString(firstPart).apply {
//            setSpan(CustomTypefaceSpan(firstTypeFace), 0, firstPart.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//        }
//
//        val secondPartStyled = SpannableString(secondPart).apply {
//            setSpan(CustomTypefaceSpan(secondTypeFace), 0, secondPart.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//            setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.orange)), 0, secondPart.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//            setSpan(RelativeSizeSpan(textSizeRatioPart2ToPart1), 0, secondPart.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//        }
//
//        val result = SpannableStringBuilder().apply {
//            append(firstPartStyled)
//            append("\n")
//            append(secondPartStyled)
//        }
//
//        return result
//    }

    private fun createAnchorBannerAds() {
        bannerAdManager.setBannerAdListener(listener = object : BannerAdListener {
            override fun onAdLoaded() {
                binding.tvAdsLoadFailed.visibility = View.GONE
                Log.d("PaywallFragment", "Anchor Banner Ad Loaded Successfully")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.tvAdsLoadFailed.visibility = View.VISIBLE
                Log.d("PaywallFragment", "Anchor Banner Ad FAILED to load")
            }
        })
        bannerAdManager.setAdUnitId(AdsConstants.AD_UNIT_ID)
        bannerAdManager.loadAnchorBanner(binding.adViewContainer)
    }

    private fun createInlineBannerAds() {
        bannerAdManager.setBannerAdListener(listener = object : BannerAdListener {
            override fun onAdLoaded() {
                binding.tvAdsLoadFailed.visibility = View.GONE
                Log.d("PaywallFragment", "Inline Banner Ad Loaded Successfully")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.tvAdsLoadFailed.visibility = View.VISIBLE
                Log.d("PaywallFragment", "Inline Banner Ad FAILED to load")
            }
        })
        bannerAdManager.setAdUnitId(AdsConstants.AD_UNIT_ID)
        bannerAdManager.loadInlineBanner(binding.adViewContainer)
    }

    private fun createCollapsibleInlineBannerAds() {
        bannerAdManager.setBannerAdListener(listener = object : BannerAdListener {
            override fun onAdLoaded() {
                binding.tvAdsLoadFailed.visibility = View.GONE
                Log.d("PaywallFragment", "Inline Banner Ad Loaded Successfully")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.tvAdsLoadFailed.visibility = View.VISIBLE
                Log.d("PaywallFragment", "Inline Banner Ad FAILED to load")
            }
        })
        bannerAdManager.setAdUnitId(AdsConstants.AD_UNIT_ID)
        bannerAdManager.loadCollapsibleInlineBanner(binding.adViewContainer, AdsConstants.CollapseType.TOP)
    }

    private fun checkPremiumStatus() {
        val isPremium = purchaseStorage.isAnyProductAcknowledged()
        if (isPremium) {
            binding.ivPremium.setImageResource(R.drawable.ic_premium_enable)
        } else {
            binding.ivPremium.setImageResource(R.drawable.ic_premium_disabled)
        }
    }

//    private fun setupToggleSubscriptionOptions() {
//        val cards = listOf(binding.clLifetime, binding.clOneWeek, binding.clMonthly)
//
//        cards.forEach { card ->
//            card.setOnClickListener {
//                cards.forEach { it.isSelected = false }
//                card.isSelected = true
//            }
//        }
//    }

    private fun buildBulletList(): SpannableStringBuilder {
        val lines = listOf(
            getString(R.string.policy_payment),
            getString(R.string.policy_auto_renew),
            getString(R.string.policy_non_subscribed),
            getString(R.string.policy_manage)
        )

        val bulletGapWidth = 20
        val bulletColor = ContextCompat.getColor(requireContext(), R.color.black)

        val builder = SpannableStringBuilder()

        for (line in lines) {
            val start = builder.length

            val capitalized = line.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }

            builder.append(capitalized)
            builder.append("\n")

            builder.setSpan(
                BulletSpan(bulletGapWidth, bulletColor),
                start,
                start + capitalized.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return builder
    }
}