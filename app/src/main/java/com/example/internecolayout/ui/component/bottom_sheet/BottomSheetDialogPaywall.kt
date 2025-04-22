package com.example.internecolayout.ui.component.bottom_sheet

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.internecolayout.R
import com.example.internecolayout.databinding.BottomSheetPaywallEditingBinding
import com.example.internecolayout.utils.CustomTypefaceSpan
import com.google.android.material.bottomsheet.BottomSheetDialog

class BottomSheetDialogPaywall(
    context: Context,
    private val discount: Int,
    private val oldPrice: Double,
    private val discountedPrice: Double
) : BottomSheetDialog(context) {

    private val binding: BottomSheetPaywallEditingBinding

    init {
        binding = BottomSheetPaywallEditingBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // Make dialog background transparent
        findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.setBackgroundColor(Color.TRANSPARENT)

        initializeData()
        registerListeners()
    }

    private fun initializeData() {
        adjustBanner()
        setPriceOffValue()
        setEditingToolsDescriptions()
        setPriceTextView()
        setUpPrivacyAndPolicyTextView()
    }

    private fun registerListeners() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }

    private fun adjustBanner() {
        binding.llBlackFridayOffer.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.llBlackFridayOffer.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val halfHeight = binding.llBlackFridayOffer.height / 2
                    val params = binding.scrollView2.layoutParams as ConstraintLayout.LayoutParams
                    params.topMargin = halfHeight
                    binding.scrollView2.layoutParams = params
                }
            }
        )
    }

    private fun setPriceOffValue() {
        binding.tvPriceOff.text = context.getString(R.string.offer, discount)
    }

    private fun setPriceTextView() {
        val grayColor = ContextCompat.getColor(context, R.color.gray)

        val original = context.getString(R.string.price_format, oldPrice.toFloat())
        val toSymbol = context.getString(R.string.to_symbol)
        val discounted = context.getString(R.string.price_format, discountedPrice.toFloat())

        val fullText = "$original$toSymbol$discounted"
        val spannable = SpannableStringBuilder(fullText)

        val originalStart = 0
        val originalEnd = original.length
        val symbolEnd = originalEnd + toSymbol.length

        val firstTypeFace = Typeface.create(
            ResourcesCompat.getFont(context, R.font.roboto_regular), Typeface.NORMAL
        )
        spannable.setSpan(
            StrikethroughSpan(),
            originalStart,
            originalEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(grayColor),
            originalStart,
            originalEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            CustomTypefaceSpan(firstTypeFace),
            originalStart,
            symbolEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvPrice.text = spannable
    }

    private fun setEditingToolsDescriptions() {
        binding.apply {
            transition.tvQuantity.text = formatRoundedNumber(40)
            effect.tvQuantity.text = formatRoundedNumber(50)
            frame.tvQuantity.text = formatRoundedNumber(50)

            transition.tvTools.text = context.getString(R.string.transitions)
            effect.tvTools.text = context.getString(R.string.effects)
            frame.tvTools.text = context.getString(R.string.frames)

            transition.shapeableImageView.setImageResource(R.drawable.img_transition)
            effect.shapeableImageView.setImageResource(R.drawable.img_effect)
            frame.shapeableImageView.setImageResource(R.drawable.img_frame)
        }
    }

    private fun setUpPrivacyAndPolicyTextView() {
        val prefix = context.getString(R.string.subscription_terms_prefix)
        val terms = context.getString(R.string.subscription_terms)
        val privacy = context.getString(R.string.subscription_privacy)

        val fullIntro = "$prefix$terms and $privacy."
        val spannable = SpannableStringBuilder(fullIntro)

        val termsStart = fullIntro.indexOf(terms)
        spannable.setSpan(UnderlineSpan(), termsStart, termsStart + terms.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Open Terms link
            }
        }, termsStart, termsStart + terms.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val privacyStart = fullIntro.indexOf(privacy)
        spannable.setSpan(UnderlineSpan(), privacyStart, privacyStart + privacy.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Open Privacy link
            }
        }, privacyStart, privacyStart + privacy.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val finalText = SpannableStringBuilder()
            .append(spannable)
            .append("\n")
            .append(context.getString(R.string.subscription_item1))
            .append("\n")
            .append(context.getString(R.string.subscription_item2))
            .append("\n")
            .append(context.getString(R.string.subscription_item3))
            .append("\n")
            .append(context.getString(R.string.subscription_item4))

        val tv = binding.tvBulletInfo
        tv.text = finalText
        tv.movementMethod = LinkMovementMethod.getInstance()
        tv.highlightColor = Color.TRANSPARENT
    }

    private fun formatRoundedNumber(number: Int): String {
        val rounded = (number / 10) * 10
        return "${rounded}+"
    }
}