package com.eco.musicplayer.audioplayer.music.ui.component.bottom_sheet

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.BottomSheetPaywallEditingBinding
import com.eco.musicplayer.audioplayer.music.utils.CustomTypefaceSpan
import com.eco.musicplayer.audioplayer.music.utils.setPolicyText
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetPaywallEditing : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetPaywallEditingBinding

    private var discount: Int = 0
    private var oldPrice: Double = 0.0
    private var discountedPrice: Double = 0.0

    companion object {
        fun newInstance(discount: Int, oldPrice: Double, discountedPrice: Double): BottomSheetPaywallEditing {
            val fragment = BottomSheetPaywallEditing()
            val args = Bundle().apply {
                putInt("discount", discount)
                putDouble("oldPrice", oldPrice)
                putDouble("discountedPrice", discountedPrice)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            discount = it.getInt("discount")
            oldPrice = it.getDouble("oldPrice")
            discountedPrice = it.getDouble("discountedPrice")
        }
    }

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
        )

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

    private fun formatRoundedNumber(number: Int): String {
        val rounded = (number / 10) * 10
        return "${rounded}+"
    }
}
