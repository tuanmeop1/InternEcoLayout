package com.eco.musicplayer.audioplayer.ads.nativee

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.eco.musicplayer.audioplayer.music.databinding.NativeAdBinding
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

class NativeView250 @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var binding: NativeAdBinding? = null

    init {
        binding = NativeAdBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun loaded(ad: NativeAd) {
        binding?.let {
            val nativeAdView = it.layoutRoot

            it.tvHeadline.text = ad.headline
            it.btnAdCallToAction.text = ad.callToAction
            it.tvAdDescription.text = ad.body
            it.ivAppIcon.setImageDrawable(ad.icon?.drawable)
            nativeAdView.apply {
                headlineView = it.tvHeadline
                mediaView = it.adMedia
                ad.mediaContent?.hasVideoContent()
                callToActionView = it.btnAdCallToAction
                setNativeAd(ad)
            }

            //Star-rating
            if (ad.starRating != null && ad.starRating!! > 0.0) {
                it.ratingBar.rating = ad.starRating!!.toFloat()
                it.ratingBar.visibility = VISIBLE
                nativeAdView.starRatingView = it.ratingBar
            } else {
                it.ratingBar.visibility = GONE
            }


        }
    }
}