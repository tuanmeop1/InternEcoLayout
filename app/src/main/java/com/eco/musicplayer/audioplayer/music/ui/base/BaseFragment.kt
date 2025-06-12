package com.eco.musicplayer.audioplayer.music.ui.base

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.eco.musicplayer.audioplayer.ads.BaseAdManager
import com.eco.musicplayer.audioplayer.ads.LoadFullScreenAdDialog
import com.eco.musicplayer.audioplayer.extensions.waitForAdOrTimeout

abstract class BaseFragment : BaseView, Fragment() {

    //abstract override fun getContentViewId(): Int
    val loadFullScreenAdDialog by lazy { LoadFullScreenAdDialog(requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getContentViewId(), container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        initializeData()
        registerListeners()
    }

    fun isActive() = isAdded && activity != null

    fun getActivitySafety(action: (Activity) -> Unit) {
        if (isActive()) action(requireActivity())
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun showAd(adManager: BaseAdManager, action: ((Boolean) -> Unit)) {
        loadFullScreenAdDialog.showDialog()
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.waitForAdOrTimeout(5, adManager) {
            if (adManager.isAdLoaded()) {
                adManager.showAd(appCompatActivity) {
                    action(it)
                    loadFullScreenAdDialog.hideDialog()
                }
            } else {
                action(false)
                loadFullScreenAdDialog.hideDialog()
            }
        }
    }
}