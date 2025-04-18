package com.example.internecolayout.ui.component.paywall.fragment

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.RelativeSizeSpan
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.internecolayout.R
import com.example.internecolayout.databinding.FragmentPaywallBinding
import com.example.internecolayout.utils.CustomTypefaceSpan



class PaywallFragment : Fragment() {
    private var _binding: FragmentPaywallBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaywallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeData()
        registerListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeData() {
        val firstPart = getString(R.string.remove_ads_for_the)
        val secondPart = getString(R.string.best_experience)
        val originalPrice = "$32.99"
        val price = "$27.99"
        binding.apply {
            tvRemoveAds.text = setRemoveAdsSpannable(firstPart, secondPart)
            tvBulletInfo.text = buildBulletList()

            lifeTime.tvOriginalPrice.text = originalPrice
            lifeTime.tvPrice.text = price

            oneWeek.tvOriginalPrice.text = originalPrice
            oneWeek.tvPrice.text = price
            oneWeek.tvBestOffer.visibility = View.GONE
            oneWeek.tvTitle.text = getString(R.string.one_week)
            oneWeek.tvDescription.text = getString(R.string.cancel_anytime)

            monthLy.tvOriginalPrice.text = originalPrice
            monthLy.tvPrice.text = price
            monthLy.tvBestOffer.visibility = View.GONE
            monthLy.tvTitle.text = getString(R.string.monthly)
            monthLy.tvDescription.text = getString(R.string.billed_monthly)
        }
    }

    private fun registerListeners() {
        setupToggleSubscriptionOptions()
    }

    private fun setRemoveAdsSpannable(firstPart: String, secondPart: String): SpannableStringBuilder {
        val firstPartSize = resources.getDimension(com.intuit.sdp.R.dimen._24sdp)
        val secondPartSize = resources.getDimension(com.intuit.sdp.R.dimen._32sdp)
        val textSizeRatioPart2ToPart1 = secondPartSize / firstPartSize

        val firstTypeFace = Typeface.create(
            ResourcesCompat.getFont(requireContext(), R.font.nunito_sans_bold), Typeface.NORMAL
        )

        val secondTypeFace = Typeface.create(
            ResourcesCompat.getFont(requireContext(), R.font.nunito_sans_extra_bold), Typeface.NORMAL
        )

        val firstPartStyled = SpannableString(firstPart).apply {
            setSpan(CustomTypefaceSpan(firstTypeFace), 0, firstPart.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val secondPartStyled = SpannableString(secondPart).apply {
            setSpan(CustomTypefaceSpan(secondTypeFace), 0, secondPart.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.orange)), 0, secondPart.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(RelativeSizeSpan(textSizeRatioPart2ToPart1), 0, secondPart.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val result = SpannableStringBuilder().apply {
            append(firstPartStyled)
            append("\n")
            append(secondPartStyled)
        }

        return result
    }


    private fun setupToggleSubscriptionOptions() {
        val cards = listOf(binding.clLifetime, binding.clOneWeek, binding.clMonthly)

        cards.forEach { card ->
            card.setOnClickListener {
                cards.forEach { it.isSelected = false }
                card.isSelected = true
            }
        }
    }

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