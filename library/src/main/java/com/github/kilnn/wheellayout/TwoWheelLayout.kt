package com.github.kilnn.wheellayout

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.widget.LinearLayout
import com.github.kilnn.wheelview.OnWheelScrollListener
import com.github.kilnn.wheelview.WheelView

class TwoWheelLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), OnWheelScrollListener {

    private val wheelLayoutFirst: OneWheelLayout
    private val wheelLayoutSecond: OneWheelLayout

    init {
        orientation = HORIZONTAL
        wheelLayoutFirst = addOneWheelLayout().apply {
            addScrollingListener(this@TwoWheelLayout)
        }
        wheelLayoutSecond = addOneWheelLayout()
    }

    private var linkages: SparseArray<WheelIntAdapterKey?>? = null

    /**
     * @param wheelConfigFirst [wheelLayoutFirst]的配置
     * @param wheelConfigSecond [wheelLayoutSecond]的配置
     * @param linkages 当[wheelConfigFirst]改变值时，[wheelLayoutSecond]需要进行的联动改变Adapter
     */
    fun setConfig(
        wheelConfigFirst: WheelIntConfig,
        wheelConfigSecond: WheelIntConfig,
        linkages: SparseArray<WheelIntAdapterKey?>? = null,
    ) {
        wheelLayoutFirst.setConfig(wheelConfigFirst)
        wheelLayoutSecond.setConfig(wheelConfigSecond)
        this.linkages = linkages
    }

    override fun onScrollingStarted(wheel: WheelView) {
    }

    override fun onScrollingFinished(wheel: WheelView) {
        adjustLinkage()
    }

    private fun adjustLinkage() {
        val linkages = this.linkages
        if (linkages == null || linkages.size() <= 0) return
        val value = wheelLayoutFirst.getValue()
        val linkageAdapterKey = linkages[value]
        //第二个联动
        wheelLayoutSecond.setAdapterKey(linkageAdapterKey)
    }

    private fun getFirstValue(): Int {
        return wheelLayoutFirst.getValue()
    }

    private fun getSecondValue(): Int {
        //如果WheelView还在滚动的时候获取值，可能第一个WheelView值已经改变，但联动的第二个WheelView的Adapter还未更新，会导致值发生错误，所以需要检查
        val firstValue = wheelLayoutFirst.getValue()
        val adapterKey = linkages?.get(firstValue)
        return wheelLayoutSecond.getValue(adapterKey)
    }

    fun setValue(first: Int, second: Int) {
        wheelLayoutFirst.setValue(first)
        adjustLinkage()
        wheelLayoutSecond.setValue(second)
    }

    fun setValue(array: IntArray) {
        setValue(array[0], array[1])
    }

    fun getValue(): IntArray {
        return intArrayOf(getFirstValue(), getSecondValue())
    }
}