package com.example.internecolayout.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.internecolayout.R
import com.example.internecolayout.databinding.ActivityMainBinding
import com.example.internecolayout.model.User
import com.example.internecolayout.ui.component.test.adapter.SimpleAdapter

class MainActivity : AppCompatActivity() {

    private var adapter = SimpleAdapter()
    private lateinit var binding: ActivityMainBinding

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
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val listUser = listOf(User("0", "Natu Nguyen", 19),
            User("1", "Tuna Nguyen", 19),
            User("2", "Natu Le", 20),
            User("3", "Tanu Tran", 29),
            User("1", "Tuna Nguyen", 19),
            User("2", "Natu Le", 20),
            User("3", "Tanu Tran", 29),
            User("1", "Tuna Nguyen", 19),
            User("2", "Natu Le", 20),
            User("3", "Tanu Tran", 29),
            User("1", "Tuna Nguyen", 19),
            User("2", "Natu Le", 20),
            User("3", "Tanu Tran", 29),
            User("1", "Tuna Nguyen", 19),
            User("2", "Natu Le", 20),
            User("3", "Tanu Tran", 29),
            User("1", "Tuna Nguyen", 19),
            User("2", "Natu Le", 20),
            User("3", "Tanu Tran", 29))
        adapter.submitList(listUser)
    }

}