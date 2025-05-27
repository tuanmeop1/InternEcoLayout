package com.eco.musicplayer.audioplayer.model

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView

data class ProductViewBinding(
    val productId: String,
    val card: Pair<ViewGroup, AppCompatTextView>
)