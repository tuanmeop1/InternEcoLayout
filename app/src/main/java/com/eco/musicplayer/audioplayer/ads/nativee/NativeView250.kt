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
            it.tvHeadline.text = ad.headline
            it.layoutRoot.headlineView = it.tvHeadline
            it.layoutRoot.mediaView = it.adMedia
            it.layoutRoot.setNativeAd(ad)
        }
    }
}