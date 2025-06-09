package com.eco.musicplayer.audioplayer.music.ui.base

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivityBinding<T: ViewDataBinding>: BaseActivity() {
    open lateinit var binding: T

    protected val isInitialized get() = this::binding.isInitialized

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = DataBindingUtil.bind(view)!!
            binding.lifecycleOwner = this
        } catch (e: Exception) {
            Log.e("BaseActivityBinding", e.message.toString())
            return
        }
    }


    override fun onDestroy() {
        if (this::binding.isInitialized)
            binding.unbind()
        super.onDestroy()
    }

}