package com.eco.musicplayer.audioplayer.music.ui.base

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding


abstract class BaseFragmentBinding<T : ViewDataBinding>: BaseFragment() {

    open lateinit var binding: T

    protected val isInitialized get() = this::binding.isInitialized

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            binding = DataBindingUtil.bind(view)!!
            binding.lifecycleOwner = this
        } catch (e: Exception) {
            Log.e("BaseFragmentBinding", e.message.toString())
            return
        }
        if (isInitialized) {
            super.onViewCreated(view, savedInstanceState)
        }
    }

}