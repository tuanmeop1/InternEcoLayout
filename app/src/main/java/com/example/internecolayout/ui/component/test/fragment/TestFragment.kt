package com.example.internecolayout.ui.component.test.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem // Cần import MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.ActionBarDrawerToggle // Import quan trọng
import androidx.appcompat.app.AppCompatActivity // Import quan trọng
import androidx.core.view.GravityCompat // Import quan trọng
import androidx.fragment.app.Fragment
import com.example.internecolayout.R
import com.example.internecolayout.databinding.FragmentTestBinding // Import lớp Binding được tạo tự động

class TestFragment : Fragment() {

    // Khai báo biến binding (nullable)
    private var _binding: FragmentTestBinding? = null
    // Tạo một getter non-null để truy cập binding an toàn sau onCreateView
    // Chỉ nên dùng getter này giữa onCreateView và onDestroyView
    private val binding get() = _binding!!

    private lateinit var toggle: ActionBarDrawerToggle


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { // Trả về View (non-null) thay vì View?
        // Inflate layout bằng ViewBinding
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        // Trả về root view của layout đã được inflate
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 1. Thiết lập Toolbar ---
        // Cần lấy Activity và ép kiểu thành AppCompatActivity để dùng setSupportActionBar
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar) // Sử dụng ID toolbar từ binding

        // Hiển thị nút hamburger/back (quan trọng để toggle hoạt động khi click icon)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setHomeButtonEnabled(true) // Đảm bảo nút home có thể click

        // --- 2. Thiết lập ActionBarDrawerToggle ---
        toggle = ActionBarDrawerToggle(
            requireActivity(),              // Context (Activity chứa Fragment)
            binding.drawerLayout,           // DrawerLayout từ binding
            binding.toolbar,                // Toolbar từ binding (để tự động xử lý icon)
            R.string.navigation_drawer_open, // Chuỗi mô tả (cần tạo trong strings.xml)
            R.string.navigation_drawer_close // Chuỗi mô tả (cần tạo trong strings.xml)
        )

        // Thêm listener vào DrawerLayout
        binding.drawerLayout.addDrawerListener(toggle)

        // Đồng bộ trạng thái của toggle (hiển thị icon hamburger ban đầu)
        toggle.syncState() // RẤT QUAN TRỌNG

        // --- 3. Thiết lập Listener cho NavigationView ---
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            // Xử lý khi một item trong drawer được chọn
            handleNavigationItemSelected(menuItem)
            // Đóng drawer sau khi chọn
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true // Return true để báo hiệu sự kiện đã được xử lý
        }

        binding.webView.settings.javaScriptEnabled = true  // Nếu website cần JavaScript


        binding.webView.loadUrl("https://www.google.com")   // Load trang web
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()

                // Ví dụ: mở link bên ngoài nếu là link YouTube
                return if (url.contains("youtube.com")) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    view?.context?.startActivity(intent)
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun handleNavigationItemSelected(menuItem: MenuItem) {
        // Xử lý logic dựa trên ID của menuItem được click
        when (menuItem.itemId) {
            R.id.nav_library -> { // Thay R.id.nav_item1 bằng ID thực tế trong drawer_menu.xml
                Log.d("TestFragment", "Navigation Library selected")
                // Ví dụ: Chuyển Fragment, mở Activity mới, thực hiện hành động...
                // findNavController().navigate(R.id.action_testFragment_to_someOtherFragment)
            }

            R.id.nav_about -> { // Thay R.id.nav_item2 bằng ID thực tế
                Log.d("TestFragment", "Navigation About selected")
            }
            // Thêm các case khác cho các menu item của bạn
            else -> {
                Log.d("TestFragment", "Unknown navigation item selected")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Rất quan trọng: Dọn dẹp binding để tránh memory leak
        // Có thể cần remove listener nếu toggle không được quản lý tốt bởi lifecycle
        // binding.drawerLayout.removeDrawerListener(toggle) // Cẩn thận vì _binding lúc này là null
        _binding = null
    }

    // Lưu ý quan trọng: Việc xử lý nút back của hệ thống để đóng DrawerLayout
    // (nếu nó đang mở) thường được thực hiện trong Activity chứa Fragment.
    // Activity sẽ override onBackPressed() và kiểm tra binding.drawerLayout.isDrawerOpen(GravityCompat.START)
}