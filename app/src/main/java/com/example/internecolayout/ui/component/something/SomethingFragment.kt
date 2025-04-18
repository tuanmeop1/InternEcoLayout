package com.example.internecolayout.ui.component.something

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.internecolayout.R
import com.example.internecolayout.databinding.FragmentMenuBinding
import com.example.internecolayout.databinding.FragmentSomethingBinding
import com.example.internecolayout.ui.component.something.decorator.CalendarDecorator
import com.example.internecolayout.ui.component.something.decorator.StickerDecorator
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class SomethingFragment : Fragment() {
    private var _binding: FragmentSomethingBinding? = null
    private val binding get() = _binding!!
    private var selectedDate: CalendarDay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSomethingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Decorator cho hình tròn phía trên số ngày
        binding.calendarView.addDecorator(object : DayViewDecorator {
            private val circleDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_calendar_day_normal)
            private val selectedDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_calendar_day_selected)
            private var currentDay: CalendarDay? = null
            private val stickers = mapOf(
                CalendarDay.from(2025, 3, 6) to R.drawable.img_flower,
                CalendarDay.from(2025, 3, 11) to R.drawable.img_smile,
                CalendarDay.from(2025, 3, 17) to R.drawable.img_flower,
                CalendarDay.from(2025, 3, 20) to R.drawable.img_smile
            )


            override fun shouldDecorate(day: CalendarDay): Boolean {
                currentDay = day
                return true // Áp dụng cho tất cả các ngày
            }

            override fun decorate(view: DayViewFacade) {
                currentDay.let { day ->
                    val spannableString = SpannableString("\n\n")

                    val drawable = when {
                        stickers.containsKey(day) -> ContextCompat.getDrawable(requireContext(), stickers[day]!!)
                        day == selectedDate -> selectedDrawable
                        else -> circleDrawable
                    }

                    drawable?.setBounds(0, 0, 60, 60)
                    val imageSpan = ImageSpan(drawable!!, ImageSpan.ALIGN_BOTTOM)
                    spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // Thêm span vào view
                    view.addSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            selectedDate = day
                            binding.calendarView.invalidateDecorators()
                        }
                    })
                    view.addSpan(imageSpan)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}