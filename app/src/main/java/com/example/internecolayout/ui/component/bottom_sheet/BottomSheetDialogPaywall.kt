package com.example.internecolayout.ui.component.bottom_sheet

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.BulletSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.internecolayout.R
import com.example.internecolayout.databinding.BottomSheetPaywallEditingBinding
import com.example.internecolayout.utils.CustomTypefaceSpan
import com.example.internecolayout.utils.setPolicyText
import com.google.android.material.bottomsheet.BottomSheetDialog

class BottomSheetDialogPaywall(
    context: Context,
    private val discount: Int,
    private val oldPrice: Double,
    private val discountedPrice: Double
) : BottomSheetDialog(context) {

    private val binding: BottomSheetPaywallEditingBinding by lazy {
        BottomSheetPaywallEditingBinding.inflate(LayoutInflater.from(context))
    }

    init {
        setContentView(binding.root)
        initializeData()
        registerListeners()
    }

    override fun show() {
        super.show()

        val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.post {
            bottomSheet.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun initializeData() {
        //adjustBanner()
        setPriceOffValue()
        setEditingToolsDescriptions()
        setPriceTextView()
        setUpPrivacyTextView()
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

    private fun setUpPrivacyTextView() {
        val bulletStrings = context.resources.getStringArray(R.array.subscription_items).toList()

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

    private fun formatRoundedNumber(number: Int): String {
        val rounded = (number / 10) * 10
        return "${rounded}+"
    }
}