package com.eco.musicplayer.audioplayer.ads.app_open

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.eco.musicplayer.audioplayer.ads.FullScreenAdManager
import com.eco.musicplayer.audioplayer.music.ui.MainActivity
import com.eco.musicplayer.audioplayer.music.ui.base.AppOpenAdAllowed
import com.eco.musicplayer.audioplayer.music.utils.AdsConstants

class AppOpenAdApplication(
    val appOpenAdManager: AppOpenAdManager,
    private val application: Application
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private val TAG = "AppActivityLifecycle"
    private var currentActivity: Activity? = null

    fun init() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        application.registerActivityLifecycleCallbacks(this)
        appOpenAdManager.setAdUnitId(AdsConstants.APP_OPEN_AD_UNIT_ID)
        appOpenAdManager.loadAd(application)
    }

    fun setListener(listener: AppOpenAdListener) {
        appOpenAdManager.setListener(listener)
    }

    fun showAd(activity: Activity) {
        appOpenAdManager.showAdIfNoFullScreenAd(activity)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        onMoveToForeground()
    }

    private fun onMoveToForeground() {
        val activity = currentActivity ?: return
        if (activity is MainActivity && activity.getTopNavigationFragment() is AppOpenAdAllowed) {
            appOpenAdManager.showAdIfNoFullScreenAd(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {
//        if (currentActivity == activity) {
//            currentActivity = null
//        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
