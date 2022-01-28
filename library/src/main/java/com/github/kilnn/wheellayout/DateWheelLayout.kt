package com.github.kilnn.wheellayout

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.github.kilnn.wheelview.OnWheelScrollListener
import com.github.kilnn.wheelview.WheelView
import java.util.*

class DateWheelLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), OnWheelScrollListener {

    private val calendar = GregorianCalendar()

    private val wheelLayoutYear: OneWheelLayout
    private val wheelLayoutMonth: OneWheelLayout
    private val wheelLayoutDay: OneWheelLayout

    init {
        orientation = HORIZONTAL
        wheelLayoutYear = addOneWheelLayout().apply {
            addScrollingListener(this@DateWheelLayout)
        }
        wheelLayoutMonth = addOneWheelLayout().apply {
            addScrollingListener(this@DateWheelLayout)
        }
        wheelLayoutDay = addOneWheelLayout()
    }

    private var startYear = 0
    private var endYear = 0
    private var limitMonthAtStart = 0
    private var limitMonthAtEnd = 0
    private var limitDayAtStart = 0
    private var limitDayAtEnd = 0

    fun setConfig(
        start: Date? = null,
        end: Date? = null,
        yearDes: String? = null,
        monthDes: String? = null,
        dayDes: String? = null
    ) {
        val startDate = start ?: intArrayOf(1900, 1, 1).toDate(calendar)
        val endDate = end ?: Date()
        check(startDate <= endDate) { "error:startDate after endData" }

        startYear = startDate.getYear() + 1900;
        limitMonthAtStart = startDate.getMonth() + 1;//如为7，则开始年份中，月的选择为7-12月
        limitDayAtStart = startDate.getDate();//如为7，则开始年月中，日的选择为7-31号

        endYear = endDate.getYear() + 1900;
        limitMonthAtEnd = endDate.getMonth() + 1;//如为7，则结束年份中，月的选择为1-7月
        limitDayAtEnd = endDate.getDate();//如为7，则结束年月中，月的选择为1-7号

        wheelLayoutYear.setConfig(WheelIntConfig(startYear, endYear, false, yearDes, null))
        wheelLayoutMonth.setConfig(getMonthAdapterKey(startYear).toWheelIntConfig(monthDes, null))
        wheelLayoutDay.setConfig(getDayAdapterKey(startYear, limitMonthAtStart).toWheelIntConfig(dayDes, null))
    }

    private fun getMonthAdapterKey(year: Int): WheelIntAdapterKey {
        return when {
            year == startYear -> WheelIntAdapterKey(limitMonthAtStart, 12, limitMonthAtStart == 1)
            year < endYear -> WheelIntAdapterKey(1, 12, true)
            else -> WheelIntAdapterKey(1, limitMonthAtEnd, limitMonthAtEnd == 12)
        }
    }

    private fun getDayAdapterKey(year: Int, month: Int): WheelIntAdapterKey {
        val dayCount = getDayCount(year, month)
        return if (year == startYear && month == limitMonthAtStart) {
            WheelIntAdapterKey(limitDayAtStart, dayCount, limitDayAtStart == 1)
        } else if (year == endYear && month == limitMonthAtEnd) {
            WheelIntAdapterKey(1, limitDayAtEnd, limitDayAtEnd == dayCount)
        } else {
            WheelIntAdapterKey(1, dayCount, true)
        }
    }

    private fun getDayCount(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> {
                if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
                    29
                } else {
                    28
                }
            }
            else -> {
                throw  IllegalArgumentException()
            }
        }
    }

    override fun onScrollingStarted(wheel: WheelView) {

    }

    override fun onScrollingFinished(wheel: WheelView) {
        if (wheel == wheelLayoutYear.wheelView) {
            adjustMonthDay()
        } else if (wheel === wheelLayoutMonth.wheelView) {
            adjustDay()
        }
    }

    private fun adjustMonthDay() {
        val year = wheelLayoutYear.getValue()
        wheelLayoutMonth.setAdapterKey(getMonthAdapterKey(year))
        val month = wheelLayoutMonth.getValue()
        wheelLayoutDay.setAdapterKey(getDayAdapterKey(year, month))
    }

    private fun adjustDay() {
        val year = wheelLayoutYear.getValue()
        val month = wheelLayoutMonth.getValue()
        wheelLayoutDay.setAdapterKey(getDayAdapterKey(year, month))
    }

    fun getDate(): IntArray {
        val year = wheelLayoutYear.getValue()
        val month = wheelLayoutMonth.getValue(getMonthAdapterKey(year))
        val day = wheelLayoutDay.getValue(getDayAdapterKey(year, month))
        return intArrayOf(year, month, day)
    }

    fun setDate(year: Int, month: Int, day: Int) {
        wheelLayoutYear.setValue(year)
        adjustMonthDay()
        wheelLayoutMonth.setValue(month)
        adjustDay()
        wheelLayoutDay.setValue(day)
    }

}