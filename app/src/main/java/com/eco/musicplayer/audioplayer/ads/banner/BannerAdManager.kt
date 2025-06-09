    package com.eco.musicplayer.audioplayer.ads.banner

    import android.content.Context
    import android.os.Bundle
    import android.view.ViewGroup
    import com.eco.musicplayer.audioplayer.music.utils.AdsConstants
    import com.google.ads.mediation.admob.AdMobAdapter
    import com.google.android.gms.ads.AdListener
    import com.google.android.gms.ads.AdRequest
    import com.google.android.gms.ads.AdSize
    import com.google.android.gms.ads.AdView
    import com.google.android.gms.ads.LoadAdError

    class BannerAdManager(val context: Context) {

        private var adView: AdView? = null
        private var adsUnitId: String? = null
        private var bannerAdListener: BannerAdListener? = null

        fun setAdUnitId(adUnitId: String) {
            adsUnitId = adUnitId
        }

        fun setBannerAdListener(listener: BannerAdListener) {
            this.bannerAdListener = listener
        }

        fun loadAnchorBanner(container: ViewGroup) {
            loadAd(
                container, getAnchorAdSize()
            )
        }

        fun loadInlineBanner(container: ViewGroup) {
            container.post {
                loadAd(
                    container, getInlineAdSize(container.width, container.height)
                )
            }
        }

        fun loadCollapsibleInlineBanner(container: ViewGroup, collapseType: AdsConstants.CollapseType) {
            container.post {
                loadAd(
                    container, getInlineAdSize(container.width, container.height),true, collapseType
                )
            }
        }

        fun loadCollapsibleAnchorBanner(container: ViewGroup, collapseType: AdsConstants.CollapseType) {
            loadAd(
                container, getAnchorAdSize(), true, collapseType
            )
        }

        private fun loadAd(
            container: ViewGroup,
            adSize: AdSize,
            isCollapsible: Boolean = false,
            collapseType: AdsConstants.CollapseType? = null
        ) {
            adView = AdView(container.context).apply {
                setAdSize(adSize)
                this.adUnitId = adsUnitId!!
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        bannerAdListener?.onAdLoaded()
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        bannerAdListener?.onAdFailedToLoad(error)
                    }
                }
            }
            container.removeAllViews()
            container.addView(adView)

            if(isCollapsible && collapseType != null) {
                val extras = Bundle()
                extras.putString("collapsible", collapseType.value)
                val adRequest = AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                    .build()

                adView?.loadAd(adRequest)
            } else {
                adView?.loadAd(AdRequest.Builder().build())
            }

        }

        fun resume() {
            adView?.resume()
        }

        fun pause() {
            adView?.pause()
        }

        fun destroy() {
            val parentView = adView?.parent
            if (parentView is ViewGroup) {
                parentView.removeView(adView)
            }

            adView?.destroy()
            adView = null
        }

        private fun getAnchorAdSize(): AdSize {
            val displayMetrics = context.resources.displayMetrics
            val density: Float = displayMetrics.density
            val adWidth: Int = (displayMetrics.widthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }

        private fun getInlineAdSize(width: Int, height: Int): AdSize {
            val density = context.resources.displayMetrics.density
            val adWidth = (width / density).toInt()
            val adHeight = (height / density).toInt()
            return AdSize.getInlineAdaptiveBannerAdSize(adWidth, adHeight)
        }

    }