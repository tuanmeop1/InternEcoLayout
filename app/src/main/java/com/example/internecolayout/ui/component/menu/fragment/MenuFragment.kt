package com.example.internecolayout.ui.component.menu.fragment

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.navigation.fragment.findNavController
import com.example.internecolayout.R
import com.example.internecolayout.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = Html.fromHtml(getString(R.string.app_title_html), Html.FROM_HTML_MODE_COMPACT)
        registerListeners()
        binding.btnRemoveAds.width
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun registerListeners() {
        binding.btnLibrary.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_somethingFragment)
        }
        binding.btnBackground.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_testFragment)
        }
    }
}