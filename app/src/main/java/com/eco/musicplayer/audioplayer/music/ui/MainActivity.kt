package com.eco.musicplayer.audioplayer.music.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

//    private var adapter = SimpleAdapter()
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
//        binding.recyclerView.adapter = adapter
//        binding.recyclerView.layoutManager = LinearLayoutManager(this)
//        val listUser = listOf(User("0", "Natu Nguyen", 19),
//            User("1", "Tuna Nguyen", 19),
//            User("2", "Natu Le", 20),
//            User("3", "Tanu Tran", 29),
//            User("1", "Tuna Nguyen", 19),
//            User("2", "Natu Le", 20),
//            User("3", "Tanu Tran", 29),
//            User("1", "Tuna Nguyen", 19),
//            User("2", "Natu Le", 20),
//            User("3", "Tanu Tran", 29),
//            User("1", "Tuna Nguyen", 19),
//            User("2", "Natu Le", 20),
//            User("3", "Tanu Tran", 29),
//            User("1", "Tuna Nguyen", 19),
//            User("2", "Natu Le", 20),
//            User("3", "Tanu Tran", 29),
//            User("1", "Tuna Nguyen", 19),
//            User("2", "Natu Le", 20),
//            User("3", "Tanu Tran", 29))
//        adapter.submitList(listUser)
    }

}