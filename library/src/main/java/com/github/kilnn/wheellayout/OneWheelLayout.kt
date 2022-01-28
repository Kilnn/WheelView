package com.github.kilnn.wheellayout

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import com.github.kilnn.wheelview.OnWheelChangedListener
import com.github.kilnn.wheelview.OnWheelScrollListener
import com.github.kilnn.wheelview.R
import com.github.kilnn.wheelview.WheelView

class OneWheelLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttr), OnWheelChangedListener {

    val wheelView: WheelView
    private val tvPlaceholder: TextView
    private val tvDes: TextView
    private val adapterCache: HashMap<WheelIntAdapterKey, WheelIntAdapter> by lazy { HashMap(5) }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_default_one_wheel, this)
        wheelView = findViewById(R.id.wheel_view)
        wheelView.addChangingListener(this)
        tvPlaceholder = findViewById(R.id.tv_place_holder)
        tvDes = findViewById(R.id.tv_des)
    }

    private var config: WheelIntConfig? = null

    /**
     * 初始配置
     * @param config
     */
    fun setConfig(config: WheelIntConfig) {
        this.config = config
        setAdapterKey(config)
        if (!TextUtils.isEmpty(config.des)) {
            tvPlaceholder.visibility = INVISIBLE
            tvDes.visibility = VISIBLE
            tvPlaceholder.text = getCurrentAdapter()?.getLongestText()
            tvDes.text = config.des
        }
    }

    /**
     * 切换Adapter
     * @param adapterKey 如果为null，那么表示需要使用[config]的adapter
     */
    fun setAdapterKey(adapterKey: WheelIntAdapterKey?) {
        val key = adapterKey ?: config ?: return
        val adapter = getAdapter(key)
        wheelView.isCyclic = key.isCyclic
        if (adapter != wheelView.viewAdapter) {
            wheelView.viewAdapter = adapter
            adjustWheelViewToCorrectPosition()
        }
    }

    private fun adjustWheelViewToCorrectPosition() {
        val adapter = getCurrentAdapter() ?: return
        val current = wheelView.currentItem
        if (current < 0) {
            wheelView.currentItem = 0
        } else if (current >= adapter.itemsCount) {
            wheelView.currentItem = adapter.itemsCount - 1
        }
        adapter.notifyDataChangedEvent()
    }

    /**
     * 获取当前Adapter的值
     */
    fun getValue(): Int {
        val adapter = getCurrentAdapter() ?: return 0
        return wheelView.currentItem + adapter.min
    }

    fun setValue(value: Int) {
        val adapter = getCurrentAdapter() ?: return
        val set = when {
            value < adapter.min -> adapter.min
            value > adapter.max -> adapter.max
            else -> value
        }
        wheelView.currentItem = set - adapter.min
    }

    /**
     * 获取的值需要在[adapterKey]的范围之内
     */
    fun getValue(adapterKey: WheelIntAdapterKey?): Int {
        var value = getValue()
        val key = adapterKey ?: config ?: return value
        if (value < key.min) {
            value = key.min
        } else if (value > key.max) {
            value = key.max
        }
        return value
    }

    fun addScrollingListener(listener: OnWheelScrollListener) {
        wheelView.addScrollingListener(listener)
    }

    override fun onChanged(wheel: WheelView, oldValue: Int, newValue: Int) {
        getCurrentAdapter()?.notifyDataChangedEvent()
    }

    private fun getAdapter(key: WheelIntAdapterKey): WheelIntAdapter {
        var adapter = adapterCache[key]
        if (adapter == null) {
            adapter = WheelIntAdapter(key.min, key.max, config?.formatter)
            adapterCache[key] = adapter
        }
        return adapter
    }

    /**
     * 获取[WheelIntAdapter]的快捷方法
     */
    private fun getCurrentAdapter(): WheelIntAdapter? {
        return wheelView.viewAdapter as WheelIntAdapter?
    }

}