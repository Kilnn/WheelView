package com.github.kilnn.wheellayout

import android.util.SparseArray
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes
import java.math.BigDecimal
import java.util.*

/**
 * @param id 子视图Id
 */
@Suppress("UNCHECKED_CAST")
internal fun <T : View> View.adapterHolder(@IdRes id: Int): T {
    var viewHolder = this.tag as SparseArray<View>?
    if (viewHolder == null) {
        viewHolder = SparseArray<View>()
        this.tag = viewHolder
    }
    var view = viewHolder.get(id)
    if (view == null) {
        view = this.findViewById(id)
        viewHolder.put(id, view)
    }
    return view as T
}

internal fun LinearLayout.addOneWheelLayout(): OneWheelLayout {
    val view = OneWheelLayout(context)
    val layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    addView(view, layoutParams)
    return view
}

private fun Float.roundHalfUp(): Float {
    var bigDecimal = BigDecimal(this.toDouble())
    bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP)
    return bigDecimal.toFloat()
}

private fun Float.intPart(): Int {
    return this.toInt()
}

private fun Float.floatPart(): Int {
    return ((this * 10).toInt()) % 10
}

fun TwoWheelLayout.setFloatConfig(floatConfig: WheelFloatConfig) {
    val min = floatConfig.min.roundHalfUp()
    val max = floatConfig.max.roundHalfUp()
    val configFirst = WheelIntConfig(min.intPart(), max.intPart(), floatConfig.isIntPartCyclic, floatConfig.intPartDes, floatConfig.intPartFormatter)
    val configSecond = WheelIntConfig(0, 9, floatConfig.isFloatPartCyclic, floatConfig.floatPartDes, floatConfig.floatPartFormatter)

    val linkages: SparseArray<WheelIntAdapterKey?> = SparseArray()
    val minFloatPart = min.floatPart()
    if (minFloatPart > 0) {
        linkages.put(min.intPart(), WheelIntAdapterKey(minFloatPart, 9, false))
    }
    val maxFloatPart = max.floatPart()
    if (maxFloatPart < 9) {
        linkages.put(max.intPart(), WheelIntAdapterKey(0, maxFloatPart, false))
    }

    setConfig(configFirst, configSecond, linkages)
}

fun TwoWheelLayout.setFloatValue(v: Float) {
    val value = v.roundHalfUp()
    val intPart = value.toInt()
    val floatPart = ((value * 10).toInt()) % 10
    setValue(intPart, floatPart)
}

fun TwoWheelLayout.getFlowValue(): Float {
    val values = getValue()
    return values[0] + values[1] / 10.0f
}


/**
 * 将年月日的数组转换为Date
 * 如[2018,1,31] 即 代表 2018年1月31号
 * @return date数据
 */
fun IntArray.toDate(c: GregorianCalendar? = null): Date {
    val calendar = c ?: GregorianCalendar()
    calendar.set(Calendar.YEAR, this[0])
    calendar.set(Calendar.MONTH, this[1] - 1)
    calendar.set(Calendar.DAY_OF_MONTH, this[2])
    return calendar.time
}

/**
 * 将Date转换为年月日数组
 */
fun Date.toIntArray(c: GregorianCalendar? = null): IntArray {
    val calendar = c ?: GregorianCalendar()
    val values = IntArray(3)
    calendar.time = this
    values[0] = calendar.get(Calendar.YEAR)
    values[1] = calendar.get(Calendar.MONTH) + 1
    values[2] = calendar.get(Calendar.DAY_OF_MONTH)
    return values
}

fun WheelIntAdapterKey.toWheelIntConfig(des: String?, formatter: WheelIntFormatter?): WheelIntConfig {
    return WheelIntConfig(this.min, this.max, this.isCyclic, des, formatter)
}