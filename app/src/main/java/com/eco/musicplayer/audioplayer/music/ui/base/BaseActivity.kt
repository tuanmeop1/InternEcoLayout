package com.eco.musicplayer.audioplayer.music.ui.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity: BaseView, AppCompatActivity() {

    protected lateinit var view: View

    //abstract override fun getContentViewId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = layoutInflater.inflate(getContentViewId(), null)
        setContentView(view)
    }
}