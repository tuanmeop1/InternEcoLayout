package com.example.internecolayout.ui.component.something.decorator

import android.content.Context
import android.os.Build
import android.text.style.ImageSpan
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class StickerDecorator(private val context: Context) : DayViewDecorator {
    // Map lưu trữ ngày và resource id của sticker tương ứng
    private val stickers = mutableMapOf<CalendarDay, Int>()
    // Biến tạm để lưu ngày hiện tại đang được xử lý
    private var currentDay: CalendarDay? = null

    // Thêm sticker cho một ngày cụ thể
    fun addSticker(day: CalendarDay, stickerResId: Int) {
        stickers[day] = stickerResId
    }

    // Kiểm tra xem ngày có cần decorator không
    override fun shouldDecorate(day: CalendarDay): Boolean {
        // Lưu lại ngày đang xử lý để dùng trong decorate()
        currentDay = day
        return stickers.containsKey(day)
    }

    // Trang trí cho ngày đó (thêm sticker)
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun decorate(view: DayViewFacade) {
        // Sử dụng currentDay thay vì view.day
        currentDay?.let { day ->
            stickers[day]?.let { resId ->
                // Tạo drawable từ resource
                val drawable = ContextCompat.getDrawable(context, resId)
                // Set kích thước cho drawable
                drawable?.setBounds(0, 0, 60, 60)  // Có thể điều chỉnh kích thước này
                // Tạo span để hiển thị drawable
                val imageSpan = ImageSpan(drawable!!, ImageSpan.ALIGN_CENTER)
                // Thêm span vào view
                view.addSpan(imageSpan)
            }
        }
    }
}