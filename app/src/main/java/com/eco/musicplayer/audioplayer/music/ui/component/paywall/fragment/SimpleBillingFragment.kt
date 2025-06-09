package com.eco.musicplayer.audioplayer.music.ui.component.paywall.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.eco.musicplayer.audioplayer.model.BaseProductInfo
import com.eco.musicplayer.audioplayer.model.ProductInfo
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.FragmentSimpleBillingBinding
import com.eco.musicplayer.audioplayer.music.ui.base.BaseFragmentBinding
import com.eco.musicplayer.audioplayer.music.ui.component.paywall.state.SubscriptionUiState
import com.eco.musicplayer.audioplayer.music.ui.component.paywall.viewmodel.SubscriptionViewModel
import com.eco.musicplayer.audioplayer.music.utils.BillingConstants
import com.eco.musicplayer.audioplayer.music.utils.BillingProductType
import com.eco.musicplayer.audioplayer.music.utils.getSortedProductBindings
import com.eco.musicplayer.audioplayer.music.utils.parsePeriodToDays
import com.eco.musicplayer.audioplayer.music.utils.parsePeriodToReadableText
import com.eco.musicplayer.audioplayer.music.utils.setPolicyText
import com.eco.musicplayer.audioplayer.music.utils.underline
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SimpleBillingFragment : BaseFragmentBinding<FragmentSimpleBillingBinding>() {

    private val TAG = "SimpleBillingFragment"
    private val viewModel: SubscriptionViewModel by viewModel()

    private var selectedProductId: String? = null
    private var selectedOfferToken: String? = null


    override fun getContentViewId(): Int = R.layout.fragment_simple_billing

    override fun initializeViews() {
        viewModel.start()
        hideProductButtons()
        observeViewModel()
    }


    override fun initializeData() {
        setUpSubscriptionTextView()
        setUpPrivacyTextView()
        binding.tvLimited.underline()
    }

    override fun registerListeners() {
        binding.llStartFreeTrial.setOnClickListener {
            purchase()
        }

        binding.ivClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SubscriptionUiState.Loading -> {
                        showLoading(true)
                    }

                    is SubscriptionUiState.Available -> {
                        showLoading(false)
                        Log.d(TAG, "Available products: ${state.products.size}")
                        val purchasedIds = state.purchases.flatMap { purchase ->
                            @Suppress("DEPRECATION")
                            purchase.products.ifEmpty { purchase.skus }
                        }
                        setupProductButtons(state.products, purchasedIds.firstOrNull())
                        updateSubscriptionUI(purchasedIds)
                        Log.d(TAG, "Purchased products: ${state.purchases}")
                    }

                    is SubscriptionUiState.Subscribed -> {
                        showLoading(false)
                        val purchasedIds = state.purchases.flatMap { purchase ->
                            @Suppress("DEPRECATION")
                            purchase.products.ifEmpty { purchase.skus ?: emptyList() }
                        }
                        updateSubscriptionUI(purchasedIds)
                        Log.d(TAG, "Purchased products: ${state.purchases}")
                    }

                    is SubscriptionUiState.Error -> {
                        showLoading(false)
                        Log.e(TAG, "Error state is: ${state.message}")
                    }
                }
            }
        }
    }

    private fun setupProductButtons(
        productList: List<BaseProductInfo>,
        currentPurchasedProductId: String?
    ) {
        Log.d(TAG, "Im here setupProductButton")
        val productMap = mapOf(
            BillingConstants.SUB_1 to Pair(binding.clFreeTrial, binding.mFreeTrial),
            BillingConstants.SUB_2 to Pair(binding.clMonthly, binding.mMonthly),
            BillingConstants.INAPP to Pair(binding.clLifetime, binding.mLifetime)
        )

        val allCards = listOf(binding.clFreeTrial, binding.clMonthly, binding.clLifetime)
        val allInnerCards = listOf(binding.mFreeTrial, binding.mMonthly, binding.mLifetime)

        for ((productId, cardDetails) in productMap) {
            val productInfo = productList.find { it.productId == productId }
            val (card, inner) = cardDetails
            if (productInfo != null) {
                card.visibility = View.VISIBLE

                val descriptionText: String
                val priceText: String
                val periodText: String?

                if (productInfo.type == BillingProductType.INAPP) {
                    // Handle in-app product
                    descriptionText = getString(R.string.lifetime_description)
                    priceText = productInfo.formattedPrice
                    periodText = null
                } else {
                    // Handle subscription
                    descriptionText = getString(R.string.free_trial_description)
                    priceText = productInfo.formattedPrice
                    periodText = productInfo.billingPeriod?.let {
                        parsePeriodToReadableText(
                            it,
                            requireContext()
                        )
                    }
                }

                // Get billing cycle type based on the billing period
                val billingCycleType = when {
                    productInfo.type == BillingProductType.INAPP -> getString(R.string.lifetime)
                    productInfo.billingPeriod?.contains("P1W") == true -> getString(R.string.weekly)
                    productInfo.billingPeriod?.contains("P1M") == true -> getString(R.string.monthly)
                    productInfo.billingPeriod?.contains("P1Y") == true -> getString(R.string.yearly)
                    else -> getString(R.string.unknown)
                }

                card.findViewById<TextView>(R.id.tvTitle).text = billingCycleType
                card.findViewById<TextView>(R.id.tvPrice).text = priceText
                card.findViewById<TextView>(R.id.tvDescription).text = descriptionText
                card.findViewById<TextView>(R.id.tvPeriod).text = periodText ?: ""
                card.findViewById<TextView>(R.id.tvPeriod).append(" ")
                card.setOnClickListener {
                    selectedProductId = productId
                    selectedOfferToken = productInfo.offerToken
                    binding.tvStartSubscription.text = getButtonLabel(
                        currentPurchasedProductId = currentPurchasedProductId,
                        currentSelectedProductInfo = productInfo
                    )
                    allCards.forEach { it.isSelected = false }
                    allInnerCards.forEach { it.ivCheckButton.isSelected = false }

                    card.isSelected = true
                    inner.ivCheckButton.isSelected = true
                    getOfferInfo(productInfo)?.let { offerText ->
                        Log.d("Detail", offerText)
                        binding.tvOfferDetails.apply {
                            text = offerText
                            visibility = View.VISIBLE
                        }
                    } ?: run {
                        binding.tvOfferDetails.visibility = View.GONE
                    }
                }
            } else {
                card.visibility = View.GONE
                Log.d(TAG, "$productId not found")
            }
        }
    }

    private fun getOfferInfo(product: BaseProductInfo): String? {
        return when {
            product.hasFreeTrial && product.freeTrialPeriod != null -> {
                val trialDays = parsePeriodToDays(product.freeTrialPeriod!!)
                getString(
                    R.string.subscription_free_trial, trialDays, product.formattedPrice,
                    parsePeriodToReadableText(product.billingPeriod ?: "", requireContext())
                )
            }

            product.hasIntroPrice && product.introPricePeriod != null && product.introFormattedPrice != null -> {
                val introCycles = 1 // Default to 1 since we can't get the exact number easily
                val introPriceFormatted = product.introFormattedPrice
                val regularPriceFormatted = product.formattedPrice
                val introPeriod =
                    parsePeriodToReadableText(product.introPricePeriod!!, requireContext())
                val regularPeriod =
                    parsePeriodToReadableText(product.billingPeriod ?: "", requireContext())
                getString(
                    R.string.subscription_intro_offer,
                    introCycles,
                    introPriceFormatted,
                    introPeriod,
                    regularPriceFormatted,
                    regularPeriod
                )
            }

            product.type == BillingProductType.SUBS -> {
                val basePriceFormatted = product.formattedPrice
                val period =
                    parsePeriodToReadableText(product.billingPeriod ?: "", requireContext())
                getString(R.string.subscription_base_plan, basePriceFormatted, period)
            }

            else -> null
        }
    }

    private fun getButtonLabel(
        currentPurchasedProductId: String?,
        currentSelectedProductInfo: BaseProductInfo
    ): String {
        return when {
            currentSelectedProductInfo.hasFreeTrial && currentSelectedProductInfo.freeTrialPeriod != null -> {
                getString(R.string.free_trial_button)
            }

            else -> {
                if (currentPurchasedProductId == null) {
                    getString(R.string.continue_buy)
                } else {
                    getString(R.string.upgrade_subscription)
                }
            }
        }
    }

    private fun purchase() {
        selectedProductId?.let { productId ->
            viewModel.purchaseOrChange(requireActivity(), productId, selectedOfferToken)
        } ?: run {
            Toast.makeText(
                requireContext(),
                getString(R.string.select_plan_prompt),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setUpSubscriptionTextView() {
        binding.mFreeTrial.apply {
            tvTitle.text = getString(R.string.free_trial)
            tvDescription.text = getString(R.string.free_trial_description)
            tvPrice.text = getString(R.string.free_trial_price_dollar)
            tvPeriod.text = getString(R.string.free_trial_price_period)
            tvPeriod.append(" ")

        }
        binding.mMonthly.apply {
            tvTitle.text = getString(R.string.monthly)
            tvDescription.text = getString(R.string.monthly_description)
            tvPrice.text = getString(R.string.monthly_price_dollar)
            tvPeriod.text = getString(R.string.monthly_price_period)
            tvPeriod.append(" ")
        }
        binding.mLifetime.apply {
            tvTitle.text = getString(R.string.lifetime)
            tvDescription.text = getString(R.string.lifetime_description)
            tvPrice.text = getString(R.string.lifetime_price_dollar)
            tvPeriod.text = getString(R.string.lifetime_price_period)
            tvPeriod.append(" ")
        }
    }

    private fun setUpPrivacyTextView() {
        val bulletStrings = resources.getStringArray(R.array.subscription_items).toList()

        binding.tvPolicy.setPolicyText(
            onTermsClick = {
                // handle terms click
            },
            onPrivacyClick = {
                // handle privacy click
            },
            bulletTextList = bulletStrings
        )
    }

    private fun updateSubscriptionUI(purchasedProductIds: List<String>) {
        val productMap = mapOf(
            BillingConstants.SUB_1 to Pair(binding.clFreeTrial, binding.tvBestDeal1),
            BillingConstants.SUB_2 to Pair(binding.clMonthly, binding.tvBestDeal2),
            BillingConstants.INAPP to Pair(binding.clLifetime, binding.tvBestDeal3)
        )

        val sortedProductBinding = getSortedProductBindings(viewModel.products, productMap)
        val currentPurchasedProductId = purchasedProductIds.firstOrNull() ?: return
        Log.d("SortedProduct", sortedProductBinding.toString())
        Log.d("CurrentPurchasedProductId", currentPurchasedProductId)
        Log.d("PurchasedProduct", purchasedProductIds.toString())
        val currentCard = productMap[currentPurchasedProductId]
        Log.d("CurrentCard1", currentCard.toString())
        val currentPurchasedProductInfo = viewModel.products[currentPurchasedProductId]
        if (currentPurchasedProductInfo != null) {
            Log.d("CurrentCard2", currentCard.toString())
            if (currentPurchasedProductInfo.type == BillingProductType.SUBS) {
                disableSubscriptionCard(sortedProductBinding.last().card)
            }
            currentCard?.let {
                Log.d("CurrentCard2", currentCard.toString())
                highlightPurchasedSubscription(currentCard)
            }
        }

        sortedProductBinding.forEach { productBinding ->
            if (productBinding.productId == currentPurchasedProductId) {
                return
            }
            disableSubscriptionCard(productBinding.card)
        }
    }

    private fun hideProductButtons() {
        binding.clFreeTrial.visibility = View.GONE
        binding.clMonthly.visibility = View.GONE
        binding.clLifetime.visibility = View.GONE
    }

    private fun highlightPurchasedSubscription(subscriptionCard: Pair<View, AppCompatTextView>) {
        val text = subscriptionCard.second
        disableSubscriptionCard(subscriptionCard)
        text.visibility = View.VISIBLE
        text.text = getString(R.string.active)
    }

    private fun disableSubscriptionCard(subscriptionCard: Pair<View, AppCompatTextView>) {
        val card = subscriptionCard.first
        val text = subscriptionCard.second
        card.isSelected = false
        card.setBackgroundResource(R.drawable.bg_subscription_card_disable)
        card.isClickable = false
        text.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}