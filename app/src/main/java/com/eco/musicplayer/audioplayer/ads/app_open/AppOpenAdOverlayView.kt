package com.eco.musicplayer.audioplayer.ads.app_open

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.eco.musicplayer.audioplayer.music.R

class AppOpenAdOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_app_open_overlay, this)
        visibility = View.GONE
    }

    fun show() {
        visibility = View.VISIBLE
    }

    fun hide() {
        visibility = View.GONE
    }
}