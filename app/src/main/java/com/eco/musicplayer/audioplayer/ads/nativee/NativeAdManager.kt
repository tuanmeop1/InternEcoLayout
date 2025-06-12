package com.eco.musicplayer.audioplayer.ads.nativee

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

class NativeAdManager(private val context: Context) {

    private val TAG = "NativeAdManager"

    private var adsUnitId: String = ""
    private var nativeAdListener: NativeAdListener? = null
    private var adLoader: AdLoader? = null
    private var currentNativeAd: NativeAd? = null

    fun setAdUnitId(adUnitId: String) {
        this.adsUnitId = adUnitId
    }

    fun setListener(listener: NativeAdListener) {
        nativeAdListener = listener
    }

    fun loadAd(
        container: ViewGroup,
        layoutId: Int,
        bind: (binding: ViewDataBinding, ad: NativeAd, adView: NativeAdView) -> Unit
    ) {
        val inflater = LayoutInflater.from(context)
        val videoOptions = VideoOptions.Builder().setStartMuted(false).build()
        adLoader = AdLoader.Builder(context, adsUnitId)
            .forNativeAd { ad ->
                currentNativeAd?.destroy()
                currentNativeAd = ad
                val binding = DataBindingUtil.inflate<ViewDataBinding>(
                    inflater, layoutId, container, false
                )
                val adView = binding.root as NativeAdView

                bind(binding, ad, adView)
                //  adView.setNativeAd(ad)

//                container.removeAllViews()
//                container.addView(adView)

                nativeAdListener?.onAdLoaded()
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    nativeAdListener?.onAdFailedToLoad(error)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().setVideoOptions(videoOptions).build())
            .build()
        adLoader?.loadAd(AdRequest.Builder().build())
    }

    fun loadAd(bind: (ad: NativeAd) -> Unit) {
        adLoader = AdLoader.Builder(context, adsUnitId)
            .forNativeAd { ad ->
                currentNativeAd?.destroy()
                currentNativeAd = ad
                bind(ad)
                nativeAdListener?.onAdLoaded()
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    nativeAdListener?.onAdFailedToLoad(error)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build())
            .build()

        adLoader?.loadAd(AdRequest.Builder().build())
    }

    fun destroy() {
        currentNativeAd?.destroy()
        currentNativeAd = null
    }

}
