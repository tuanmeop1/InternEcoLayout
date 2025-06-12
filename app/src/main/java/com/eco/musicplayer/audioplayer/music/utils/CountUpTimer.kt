package com.eco.musicplayer.audioplayer.music.utils

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class CountUpTimer(
    private val lifecycleScope: LifecycleCoroutineScope,
    private val maxTick: Int = 100,
    private val durationMillis: Long = 10000L,
) {

    private var currentTick = 0
    private var isRunning = false
    private var job: Job? = null
    private val delayPerStep = durationMillis / maxTick

    var onTick: ((Int) -> Unit)? = null
    var onComplete: (() -> Unit)? = null

    private fun createFlow(): Flow<Int> = flow {
        for (i in currentTick .. maxTick) {
            currentTick = i
            emit(currentTick)
            delay(delayPerStep)
        }
    }

    private fun collectTimer() {
        if (isRunning) return
        isRunning = true

        job = lifecycleScope.launch {
            createFlow().collect { tick ->
                if (!isRunning) return@collect
                onTick?.invoke(tick)
                if (tick >= maxTick) {
                    onComplete?.invoke()
                    stop()
                }
            }
        }
    }

    fun start() {
        stop()
        currentTick = 1
        collectTimer()
    }

    fun resume() {
        if (!isRunning && currentTick < maxTick) {
            collectTimer()
        }
    }

    fun pause() {
        isRunning = false
        job?.cancel()
        job = null
    }

    fun stop() {
        isRunning = false
        job?.cancel()
        job = null
    }

    fun destroy() {
        stop()
        currentTick = 0
        onTick = null
        onComplete = null
    }
}