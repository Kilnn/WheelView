package com.github.kilnn.wheellayout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.kilnn.wheelview.R
import com.github.kilnn.wheelview.adapters.AbstractWheelAdapter

class WheelIntAdapter(
    val min: Int,
    val max: Int,
    private val formatter: WheelIntFormatter? = null,
) : AbstractWheelAdapter() {

    override fun getItemsCount(): Int {
        return max - min + 1
    }

    override fun getItem(index: Int, convertView: View?, parent: ViewGroup): View? {
        if (index < 0 || index >= this.itemsCount) return null
        val resultView = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.item_default_wheel_int, parent, false)
        val textView: TextView = resultView.adapterHolder(android.R.id.text1)
        textView.text = getText(index, min + index)
        return resultView
    }

    private fun getText(index: Int, value: Int): String {
        return formatter?.format(index, value) ?: value.toString()
    }

    /**
     * 获取最长的文字，用于占位。
     * 一般认为最大值，格式化后的文字最长
     */
    fun getLongestText(): String {
        return getText(0, max)
    }

    public override fun notifyDataChangedEvent() {
        super.notifyDataChangedEvent()
    }

    public override fun notifyDataInvalidatedEvent() {
        super.notifyDataInvalidatedEvent()
    }
}