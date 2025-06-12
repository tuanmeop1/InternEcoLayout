package com.eco.musicplayer.audioplayer.ads

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import com.eco.musicplayer.audioplayer.music.R

class LoadFullScreenAdDialog(context: Context) : AlertDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.load_fullscreen_ad_dialog)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    fun showDialog() {
        if (isShowing) return
        show()
    }

    fun hideDialog() {
        if (isShowing.not()) return
        dismiss()
    }
}