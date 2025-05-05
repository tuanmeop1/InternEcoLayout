package com.eco.musicplayer.audioplayer.music.ui.component.paywall.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.FragmentSimpleBillingBinding
import com.eco.musicplayer.audioplayer.music.ui.component.paywall.state.SubscriptionUiState
import com.eco.musicplayer.audioplayer.music.ui.component.paywall.viewmodel.SubscriptionViewModel
import com.eco.musicplayer.audioplayer.music.ui.component.paywall.viewmodel.SubscriptionViewModelFactory
import com.eco.musicplayer.audioplayer.music.utils.BillingConstants
import com.eco.musicplayer.audioplayer.music.utils.setPolicyText
import com.eco.musicplayer.audioplayer.music.utils.underline
import kotlinx.coroutines.launch

class SimpleBillingFragment : Fragment() {

    private val TAG = "SimpleBillingFragment"
    private lateinit var viewModel: SubscriptionViewModel

    private var _binding: FragmentSimpleBillingBinding? = null

    private val binding get() = _binding!!

    private var selectedProduct: ProductDetails? = null
    private var selectedOfferToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimpleBillingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = SubscriptionViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[SubscriptionViewModel::class.java]
        initData()
        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initData() {
        setUpSubscriptionTextView()
        setUpPrivacyTextView()
        binding.tvLimited.underline()
    }

    private fun setupListeners() {
//        binding.btnRestore.setOnClickListener {
//            Toast.makeText(requireContext(), getString(R.string.checking_purchases), Toast.LENGTH_SHORT).show()
//            // BillingManager sẽ tự truy vấn các gói đã mua khi khởi tạo
//        }
        //setupToggleSubscriptionOptions()
        binding.llStartFreeTrial.setOnClickListener {
            purchase()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SubscriptionUiState.Loading -> {
                        showLoading(true)
                        //binding.tvStatus.text = getString(R.string.loading_products)
                    }
                    is SubscriptionUiState.Available -> {
                        showLoading(false)
                        //binding.tvStatus.text = getString(R.string.select_product)

                        Log.d(TAG, "Available products: ${state.products.size}")
                        state.products.forEach { product ->
                            Log.d(TAG, "Product: ${product.productId} - Type: ${product.productType}")
                        }

                        setupProductButtons(state.products)
                    }
                    is SubscriptionUiState.Subscribed -> {
                        showLoading(false)
                        val subscriptionInfo = getSubscriptionInfo(state)
                        //binding.tvStatus.text = getString(R.string.subscribed_message, subscriptionInfo)
                        hideProductButtons()
                    }
                    is SubscriptionUiState.Error -> {
                        showLoading(false)
                        Log.e(TAG, "Error state: ${state.message}")
                        //binding.tvStatus.text = getString(R.string.error_message, state.message)
                    }
                }
            }
        }
    }

    private fun getSubscriptionInfo(state: SubscriptionUiState.Subscribed): String {
        return when {
            state.purchases.any { it.products.contains(BillingConstants.PRODUCT_ID_LIFETIME) } ->
                getString(R.string.lifetime_plan)
            state.purchases.any { it.products.contains(BillingConstants.PRODUCT_ID_YEARLY) } ->
                getString(R.string.yearly_plan)
            state.purchases.any { it.products.contains(BillingConstants.PRODUCT_ID_MONTHLY) } ->
                getString(R.string.monthly_plan)
            state.purchases.any { it.products.contains(BillingConstants.PRODUCT_ID_WEEKLY) } ->
                getString(R.string.weekly_plan)
            else -> getString(R.string.premium_plan)
        }
    }

//    private fun setupProductButtons(products: List<ProductDetails>) {
//        Log.d(TAG, "Setting up product buttons. Products count: ${products.size}")
//
//        //val weeklyProduct = products.find { it.productId == BillingConstants.PRODUCT_ID_WEEKLY }
//        val monthlyProduct = products.find { it.productId == BillingConstants.PRODUCT_ID_MONTHLY }
//        val yearlyProduct = products.find { it.productId == BillingConstants.PRODUCT_ID_YEARLY }
//        val lifetimeProduct = products.find { it.productId == BillingConstants.PRODUCT_ID_LIFETIME }
//
//        Log.d(TAG, "Monthly: $monthlyProduct, Yearly: $yearlyProduct, Lifetime: $lifetimeProduct")
//
////        weeklyProduct?.let { product ->
////            binding.clFreeTrial.visibility = View.VISIBLE
////            binding.clFreeTrial.findViewById<TextView>(R.id.tvPrice).text = getPrice(product)
////            binding.clFreeTrial.setOnClickListener {
////                selectedProduct = product
////                selectedOfferToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken
////                purchase()
////            }
////        } ?: run {
////            binding.clFreeTrial.visibility = View.GONE
////            Log.d(TAG, "Weekly product not found")
////        }
//
//        monthlyProduct?.let { product ->
//            binding.clFreeTrial.visibility = View.VISIBLE
//            binding.clFreeTrial.findViewById<TextView>(R.id.tvPrice).text = getPrice(product)
//            binding.clFreeTrial.setOnClickListener {
//                selectedProduct = product
//                selectedOfferToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken
//            }
//        } ?: run {
//            binding.clFreeTrial.visibility = View.GONE
//            Log.d(TAG, "Monthly product not found")
//        }
//
//        yearlyProduct?.let { product ->
//            binding.clMonthly.visibility = View.VISIBLE
//            binding.clMonthly.findViewById<TextView>(R.id.tvPrice).text = getPrice(product)
//            binding.clMonthly.setOnClickListener {
//                selectedProduct = product
//                selectedOfferToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken
//            }
//        } ?: run {
//            binding.clMonthly.visibility = View.GONE
//            Log.d(TAG, "Yearly product not found")
//        }
//
//        lifetimeProduct?.let { product ->
//            binding.clLifetime.visibility = View.VISIBLE
//            binding.clLifetime.findViewById<TextView>(R.id.tvPrice).text = getPrice(product)
//            binding.clLifetime.setOnClickListener {
//                selectedProduct = product
//            }
//        } ?: run {
//            binding.clLifetime.visibility = View.GONE
//            Log.d(TAG, "Lifetime product not found")
//        }
//    }

    private fun setupProductButtons(products: List<ProductDetails>) {
        Log.d(TAG, "Setting up product buttons. Products count: ${products.size}")

        val productMap = mapOf(
            BillingConstants.PRODUCT_ID_MONTHLY to Triple(binding.clFreeTrial, binding.mFreeTrial, R.id.tvPrice),
            BillingConstants.PRODUCT_ID_YEARLY to Triple(binding.clMonthly, binding.mMonthly, R.id.tvPrice),
            BillingConstants.PRODUCT_ID_LIFETIME to Triple(binding.clLifetime, binding.mLifetime, R.id.tvPrice)
        )

        val allCards = listOf(binding.clFreeTrial, binding.clMonthly, binding.clLifetime)
        val allInnerCards = listOf(binding.mFreeTrial, binding.mMonthly, binding.mLifetime)

        for ((productId, productDetails) in productMap) {
            val product = products.find { it.productId == productId }
            val (card, inner, priceTextViewId) = productDetails

            if (product != null) {
                card.visibility = View.VISIBLE
                card.findViewById<TextView>(priceTextViewId).text = getPrice(product)
                card.setOnClickListener {
                    selectedProduct = product
                    selectedOfferToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken

                    // Toggle option
                    allCards.forEach { it.isSelected = false }
                    allInnerCards.forEach { it.ivCheckButton.isSelected = false }

                    card.isSelected = true
                    inner.ivCheckButton.isSelected = true
                }
            } else {
                card.visibility = View.GONE
                Log.d(TAG, "$productId not found")
            }
        }
    }


    private fun getPrice(product: ProductDetails): String {
        val pricingPhases = product.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList
        return if (product.productType == "subs" && !pricingPhases.isNullOrEmpty()) {
            pricingPhases[0].formattedPrice
        } else {
            product.oneTimePurchaseOfferDetails?.formattedPrice ?: getString(R.string.unknown_price)
        }
    }

    private fun purchase() {
        selectedProduct?.let { product ->
            viewModel.purchase(requireActivity(), product, selectedOfferToken)
        } ?: run {
            Toast.makeText(requireContext(), getString(R.string.select_plan_prompt), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpSubscriptionTextView() {
        binding.mFreeTrial.apply {
            tvTitle.text = getString(R.string.free_trial)
            tvDescription.text = getString(R.string.free_trial_description)
            tvPrice.text = getString(R.string.free_trial_price_dollar)
            ivCheckButton.setImageResource(R.drawable.bg_check_button_orange_selector)
            tvPeriod.text = getString(R.string.free_trial_price_period)
            tvPeriod.append(" ")

        }
        binding.mMonthly.apply {
            tvTitle.text = getString(R.string.monthly)
            tvDescription.text = getString(R.string.monthly_description)
            tvPrice.text = getString(R.string.monthly_price_dollar)
            ivCheckButton.setImageResource(R.drawable.bg_check_button_orange_selector)
            tvPeriod.text = getString(R.string.monthly_price_period)
            tvPeriod.append(" ")
        }
        binding.mLifetime.apply {
            tvTitle.text = getString(R.string.lifetime)
            tvDescription.text = getString(R.string.lifetime_description)
            tvPrice.text = getString(R.string.lifetime_price_dollar)
            ivCheckButton.setImageResource(R.drawable.bg_check_button_orange_selector)
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

    private fun setupToggleSubscriptionOptions() {
        val cards = listOf(binding.clFreeTrial, binding.clMonthly, binding.clLifetime)
        val cardsInner = listOf(binding.mFreeTrial, binding.mMonthly, binding.mLifetime)

        cards.forEachIndexed { index, card ->
            card.setOnClickListener {

                cards.forEach { it.isSelected = false }
                cardsInner.forEach { it.ivCheckButton.isSelected = false }

                card.isSelected = true
                cardsInner[index].ivCheckButton.isSelected = true
            }
        }
    }

    private fun hideProductButtons() {
        binding.clFreeTrial.visibility = View.GONE
        binding.clMonthly.visibility = View.GONE
        binding.clLifetime.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}