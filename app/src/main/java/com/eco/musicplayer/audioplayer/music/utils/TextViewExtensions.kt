package com.eco.musicplayer.audioplayer.music.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.BulletSpan
import android.text.style.LeadingMarginSpan
import android.text.style.UnderlineSpan
import androidx.core.content.ContextCompat
import com.eco.musicplayer.audioplayer.music.R

fun TextView.setPolicyText(
    context: Context = this.context,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    bulletTextList: List<String>,
    introResId: Int = R.string.subscription_terms_intro,
    termsResId: Int = R.string.subscription_terms,
    privacyResId: Int = R.string.subscription_privacy,
    bulletGapResId: Int = com.intuit.sdp.R.dimen._4sdp, // khoảng cách giữa bullet và nội dung
    bulletIndentResId: Int = com.intuit.sdp.R.dimen._4sdp, // lùi đầu dòng tất cả dòng bullet
    bulletColorResId: Int = R.color.gray
) {
    val terms = context.getString(termsResId)
    val privacy = context.getString(privacyResId)
    val intro = context.getString(introResId, terms, privacy)

    val bulletGap = context.resources.getDimensionPixelSize(bulletGapResId)
    val bulletIndent = context.resources.getDimensionPixelSize(bulletIndentResId)
    val bulletColor = ContextCompat.getColor(context, bulletColorResId)

    val spannableIntro = SpannableStringBuilder(intro)

    val termsStart = intro.indexOf(terms)
    spannableIntro.setSpan(UnderlineSpan(), termsStart, termsStart + terms.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    spannableIntro.setSpan(object : ClickableSpan() {
        override fun onClick(widget: View) = onTermsClick()
    }, termsStart, termsStart + terms.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

    val privacyStart = intro.indexOf(privacy)
    spannableIntro.setSpan(UnderlineSpan(), privacyStart, privacyStart + privacy.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    spannableIntro.setSpan(object : ClickableSpan() {
        override fun onClick(widget: View) = onPrivacyClick()
    }, privacyStart, privacyStart + privacy.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

    val fullText = SpannableStringBuilder()
        .append(spannableIntro)
        .append("\n")

    bulletTextList.forEachIndexed { index, line ->
        val start = fullText.length
        fullText.append(line)

        fullText.setSpan(
            BulletSpan(bulletGap, bulletColor),
            start,
            start + line.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        fullText.setSpan(
            LeadingMarginSpan.Standard(bulletIndent),
            start,
            start + line.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        if (index != bulletTextList.lastIndex) {
            fullText.append("\n")
        }
    }

    this.text = fullText
    this.movementMethod = LinkMovementMethod.getInstance()
    this.highlightColor = Color.TRANSPARENT
}

private fun SpannableStringBuilder.setClickableSpan(
    targetText: String,
    onClick: () -> Unit
) {
    val start = indexOf(targetText)
    if (start < 0) return

    setSpan(
        object : ClickableSpan() {
            override fun onClick(widget: View) = onClick()
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        },
        start,
        start + targetText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

fun TextView.underline() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun TextView.setUnderlinePart(target: String) {
    val content = text.toString()
    val start = content.indexOf(target)
    if (start >= 0) {
        val spannable = SpannableString(content)
        spannable.setSpan(
            UnderlineSpan(),
            start,
            start + target.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text = spannable
    }
}
