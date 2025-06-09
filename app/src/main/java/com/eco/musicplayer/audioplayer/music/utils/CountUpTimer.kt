package com.eco.musicplayer.audioplayer.music.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class CountUpTimer(
    private val lifecycleOwner: LifecycleOwner,
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
        for (i in currentTick until  maxTick) {
            emit(currentTick + 1)
            delay(delayPerStep)
        }
    }

    private fun collectTimer() {
        job = lifecycleOwner.lifecycleScope.launch {
            createFlow().collect { tick ->
                onTick?.let { it(tick) }
                if (tick == maxTick) {
                    onComplete?.let { it() }
                    stop()
                }
            }
        }
    }

    fun start() {
        stop()
        collectTimer()
    }

    fun resume() {
        stop()
        start()
    }

    fun pause() {
        stop()
    }

    fun stop() {
        isRunning = false
        job?.cancel()
        job = null
    }

    fun destroy() {
        stop()
        onTick = null
        onComplete = null
    }

}

