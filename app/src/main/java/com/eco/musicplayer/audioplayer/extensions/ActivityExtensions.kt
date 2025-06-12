package com.eco.musicplayer.audioplayer.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.eco.musicplayer.audioplayer.ads.BaseAdManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun AppCompatActivity.waitForAdOrTimeout(
    totalDelaySeconds: Int,
    condition: BaseAdManager,
    complete: (() -> Unit)
) {
    var job: Job? = null
    val delayPerIteration = (totalDelaySeconds * 1000) / 100
    job = lifecycleScope.launch {
        var progress = 0
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (progress <= 100 && isActive) {
                val isLoadedOrError = condition.isAdLoaded()
                if (isLoadedOrError && progress >= 20) break
                delay(delayPerIteration.toLong())
                progress++
            }
            withContext(Dispatchers.Main) { if (isActive) complete() }
            job?.cancel()
            job = null
        }
    }
}