package com.example.internecolayout.ui.component.something.decorator

import android.content.Context
import android.os.Build
import android.text.style.ImageSpan
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.internecolayout.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class CalendarDecorator(private val context: Context) : DayViewDecorator {
    private val stickers = mutableMapOf<CalendarDay, Int>()
    private var currentDay: CalendarDay? = null
    private var selectedDate: CalendarDay? = null

    // Tạo drawable cho ngày thường
    private val normalBackground = ContextCompat.getDrawable(context, R.drawable.ic_calendar_day_normal)
    // Tạo drawable cho ngày được chọn
    private val selectedBackground = ContextCompat.getDrawable(context, R.drawable.ic_calendar_day_selected)

    fun addSticker(day: CalendarDay, stickerResId: Int) {
        stickers[day] = stickerResId
    }

    fun setSelectedDate(date: CalendarDay) {
        selectedDate = date
    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        currentDay = day
        return true // Áp dụng cho tất cả các ngày
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun decorate(view: DayViewFacade) {
        currentDay?.let { day ->
            // Luôn set background hình tròn cho tất cả các ngày
            if (day == selectedDate) {
                view.setBackgroundDrawable(selectedBackground!!)
            } else {
                view.setBackgroundDrawable(normalBackground!!)
            }

            // Thêm sticker nếu có
            stickers[day]?.let { resId ->
                val drawable = ContextCompat.getDrawable(context, resId)
                drawable?.setBounds(0, 0, 40, 40)  // Giảm kích thước sticker
                val imageSpan = ImageSpan(drawable!!, ImageSpan.ALIGN_CENTER)
                view.addSpan(imageSpan)
            }


        }
    }
}