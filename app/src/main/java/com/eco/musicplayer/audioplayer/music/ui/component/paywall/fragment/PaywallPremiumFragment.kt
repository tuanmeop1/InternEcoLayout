package com.eco.musicplayer.audioplayer.music.ui.component.paywall.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.FragmentPaywallPremiumBinding
import com.eco.musicplayer.audioplayer.music.ui.component.paywall.state.SubscriptionUiState
import com.eco.musicplayer.audioplayer.music.ui.component.paywall.viewmodel.SubscriptionViewModel
import com.eco.musicplayer.audioplayer.music.utils.BillingConstants
import com.eco.musicplayer.audioplayer.music.utils.setPolicyText
import com.eco.musicplayer.audioplayer.music.utils.underline
import kotlinx.coroutines.launch

class PaywallPremiumFragment : Fragment() {

    private var _binding: FragmentPaywallPremiumBinding? = null
    private val binding get() = _binding!!

    private lateinit var billingClient: BillingClient
    private val viewModel: SubscriptionViewModel by viewModels()

    private var selectedProduct: ProductDetails? = null
    private var selectedOfferToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaywallPremiumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        initData()
        initListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        billingClient.endConnection()
    }

    private fun initObserver() {
        // Lắng nghe uiState từ ViewModel để cập nhật UI theo trạng thái hiện tại
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.uiState.collect { state ->
//                    when (state) {
//                        is SubscriptionUiState.Loading -> {
//                            // TODO: Hiển thị loading indicator nếu cần
//                        }
//                        is SubscriptionUiState.Available -> {
//                            showSubscriptionOptions(state.products)
//                        }
//                        is SubscriptionUiState.Subscribed -> {
//                            showSubscribedState(state.purchases)
//                        }
//                        is SubscriptionUiState.Error -> {
//                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
//                        }
//                    }
//                }
//            }
//        }
    }

    private fun initData() {
        setUpSubscriptionTextView()
        setUpPrivacyTextView()
        binding.tvLimited.underline()
    }

    private fun initListeners() {
        binding.ivClose.setOnClickListener {
            findNavController().navigateUp()
        }

//        // Nút xác nhận đăng ký
//        binding.llStartFreeTrial.setOnClickListener {
//            selectedProduct?.let {
//                viewModel.purchaseOrChange(requireActivity(), it, selectedOfferToken)
//            } ?: Toast.makeText(requireContext(), "Please select a subscription first", Toast.LENGTH_SHORT).show()
//        }
    }

    private fun showSubscriptionOptions(products: List<ProductDetails>) {
        //setupToggleSubscriptionOptions(products)
        // Optionally auto-select the first option:
        //simulateSelectProductAtIndex(products, index = 0)
    }

    private fun showSubscribedState(purchases: List<Purchase>) {
        // TODO: Update UI khi user đã mua (ẩn các lựa chọn, hiện status v.v.)
    }

    private fun setUpSubscriptionTextView() {
        binding.mFreeTrial.apply {
            tvTitle.text = getString(R.string.free_trial)
            tvDescription.text = getString(R.string.free_trial_description)
            tvPrice.text = getString(R.string.free_trial_price)
            tvPeriod.text = getString(R.string.free_trial_price_period)
            tvPeriod.append(" ")
        }
        binding.mMonthly.apply {
            tvTitle.text = getString(R.string.monthly)
            tvDescription.text = getString(R.string.monthly_description)
            tvPrice.text = getString(R.string.monthly_price)
            tvPeriod.text = getString(R.string.monthly_price_period)
            tvPeriod.append(" ")
        }
        binding.mLifetime.apply {
            tvTitle.text = getString(R.string.lifetime)
            tvDescription.text = getString(R.string.lifetime_description)
            tvPrice.text = getString(R.string.lifetime_price)
            tvPeriod.text = getString(R.string.lifetime_price_period)
            tvPeriod.append(" ")
        }
    }

    private fun setUpPrivacyTextView() {
        val bulletStrings = resources.getStringArray(R.array.subscription_items).toList()
        binding.tvPolicy.setPolicyText(
            onTermsClick = { /* TODO: Navigate to Terms */ },
            onPrivacyClick = { /* TODO: Navigate to Privacy */ },
            bulletTextList = bulletStrings
        )
    }

//    private fun simulateSelectProductAtIndex(products: List<ProductDetails>, index: Int) {
//        val productIds = listOf(
//            BillingConstants.PRODUCT_ID_FREE_TRIAL,
//            BillingConstants.PRODUCT_ID_MONTHLY,
//            BillingConstants.PRODUCT_ID_LIFETIME
//        )
//        val product = products.find { it.productId == productIds.getOrNull(index) }
//        selectedProduct = product
//        selectedOfferToken = product?.subscriptionOfferDetails?.firstOrNull()?.offerToken
//
//        // Đánh dấu UI được chọn
//        val cards = listOf(binding.clFreeTrial, binding.clMonthly, binding.clLifetime)
//        val cardsInner = listOf(binding.mFreeTrial, binding.mMonthly, binding.mLifetime)
//        cards.forEachIndexed { i, view ->
//            view.isSelected = (i == index)
//            cardsInner[i].ivCheckButton.isSelected = (i == index)
//        }
//    }

//    private fun setupToggleSubscriptionOptions(products: List<ProductDetails>) {
//        val cards = listOf(binding.clFreeTrial, binding.clMonthly, binding.clLifetime)
//        val cardsInner = listOf(binding.mFreeTrial, binding.mMonthly, binding.mLifetime)
//
//        cards.forEachIndexed { index, card ->
//            card.setOnClickListener {
//                // Cập nhật UI toggle
//                cards.forEach { it.isSelected = false }
//                cardsInner.forEach { it.ivCheckButton.isSelected = false }
//                card.isSelected = true
//                cardsInner[index].ivCheckButton.isSelected = true
//
//                // Lưu thông tin sản phẩm được chọn
//                val product = products.find { it.productId == productIds.getOrNull(index) }
//                selectedProduct = product
//                selectedOfferToken = product?.subscriptionOfferDetails?.firstOrNull()?.offerToken
//            }
//        }
//    }
}
