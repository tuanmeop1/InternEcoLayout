package com.example.internecolayout.ui.component.bottom_sheet

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.internecolayout.R
import com.example.internecolayout.databinding.BottomSheetPaywallEditingBinding
import com.example.internecolayout.utils.CustomTypefaceSpan
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetPaywallEditing(private val discount: Int, private val oldPrice: Double, private val discountedPrice: Double) : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetPaywallEditingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetPaywallEditingBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        binding.ivBlackFridayOffer.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.ivBlackFridayOffer.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val halfHeight = binding.ivBlackFridayOffer.height / 2
                    val params = binding.scrollView2.layoutParams as ConstraintLayout.LayoutParams
                    params.topMargin = halfHeight
                    binding.scrollView2.layoutParams = params
                }
            }
        )
    }

    private fun setPriceOffValue() {
        binding.tvPriceOff.text = getString(R.string.offer, discount)
    }

    private fun setPriceTextView() {
        val context = requireContext()
        val grayColor = ContextCompat.getColor(context, R.color.gray)

        val original = getString(R.string.price_format, oldPrice.toFloat())
        val toSymbol = getString(R.string.to_symbol)
        val discounted = getString(R.string.price_format, discountedPrice.toFloat())

        val fullText = "$original$toSymbol$discounted"
        val spannable = SpannableStringBuilder(fullText)

        val originalStart = 0
        val originalEnd = original.length
        val symbolEnd = originalEnd + toSymbol.length
        val discountedEnd = fullText.length

        val firstTypeFace = Typeface.create(
            ResourcesCompat.getFont(requireContext(), R.font.roboto_regular), Typeface.NORMAL
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
        ) // Roboto Regular

//        spannable.setSpan(
//            TypefaceSpan("roboto_regular"),
//            originalEnd,
//            symbolEnd,
//            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//
//        spannable.setSpan(
//            StyleSpan(Typeface.BOLD),
//            symbolEnd,
//            discountedEnd,
//            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//        )

        binding.tvPrice.text = spannable
    }

    private fun setEditingToolsDescriptions() {
        binding.apply {
            transition.tvQuantity.text = formatRoundedNumber(40)
            effect.tvQuantity.text = formatRoundedNumber(50)
            frame.tvQuantity.text = formatRoundedNumber(50)

            transition.tvTools.text = getString(R.string.transitions)
            effect.tvTools.text = getString(R.string.effects)
            frame.tvTools.text = getString(R.string.frames)

            transition.shapeableImageView.setImageResource(R.drawable.img_transition)
            effect.shapeableImageView.setImageResource(R.drawable.img_effect)
            frame.shapeableImageView.setImageResource(R.drawable.img_frame)
        }
    }

    private fun setUpPrivacyAndPolicyTextView() {
        val prefix = getString(R.string.subscription_terms_prefix)
        val terms = getString(R.string.subscription_terms)
        val privacy = getString(R.string.subscription_privacy)

        val fullIntro = "$prefix$terms and $privacy."
        val spannable = SpannableStringBuilder(fullIntro)

        val termsStart = fullIntro.indexOf(terms)
        spannable.setSpan(UnderlineSpan(), termsStart, termsStart + terms.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Mở link Terms
            }
        }, termsStart, termsStart + terms.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val privacyStart = fullIntro.indexOf(privacy)
        spannable.setSpan(UnderlineSpan(), privacyStart, privacyStart + privacy.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Mở link Privacy
            }
        }, privacyStart, privacyStart + privacy.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val finalText = SpannableStringBuilder()
            .append(spannable)
            .append("\n")
            .append(getString(R.string.subscription_item1))
            .append("\n")
            .append(getString(R.string.subscription_item2))
            .append("\n")
            .append(getString(R.string.subscription_item3))
            .append("\n")
            .append(getString(R.string.subscription_item4))

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