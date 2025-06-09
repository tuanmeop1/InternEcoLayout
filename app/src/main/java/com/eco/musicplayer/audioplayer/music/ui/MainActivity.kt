package com.eco.musicplayer.audioplayer.music.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.eco.musicplayer.audioplayer.App
import com.eco.musicplayer.audioplayer.ads.app_open.AppOpenAdApplication
import com.eco.musicplayer.audioplayer.ads.app_open.AppOpenAdManager
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainBinding
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

//    private var adapter = SimpleAdapter()
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val appOpenAdApplication: AppOpenAdApplication by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appOpenAdApplication.appOpenAdManager.attachOverlayToActivity(this)
    }

    fun getTopNavigationFragment(): Fragment? {
        val navHostFragment = supportFragmentManager.primaryNavigationFragment as? NavHostFragment
            ?: return null

        return navHostFragment.childFragmentManager.fragments
            .lastOrNull { it.isVisible && it !is DialogFragment }
    }

}