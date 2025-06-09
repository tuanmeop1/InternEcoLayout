package com.eco.musicplayer.audioplayer.music.ui.base

interface BaseView {
    fun getContentViewId(): Int

    fun initializeViews()

    fun registerListeners()

    fun initializeData()
}