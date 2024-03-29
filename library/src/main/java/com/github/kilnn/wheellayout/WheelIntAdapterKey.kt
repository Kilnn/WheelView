package com.github.kilnn.wheellayout

/**
 * 在Map中映射[WheelIntAdapter]
 */
open class WheelIntAdapterKey(
    /**
     * 最小值
     */
    val min: Int,

    /**
     * 最大值
     */
    val max: Int,

    /**
     * 是否可以循环
     */
    val isCyclic: Boolean
) {
    init {
        check(min <= max) {
            "min 不能大于 max"
        }
    }

    /**
     * 判断相等，只需要[min]和[max]即可，因为[isCyclic]不影响Adapter的数据
     */
    override fun hashCode(): Int {
        return min.hashCode() * 31 + max.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is WheelIntAdapterKey) return false
        return min == other.min && max == other.max
    }
}

class WheelIntConfig(
    min: Int, max: Int, isCyclic: Boolean,

    /**
     * 描述
     */
    val des: String?,

    /**
     * Formatter
     */
    val formatter: WheelIntFormatter?
) : WheelIntAdapterKey(min, max, isCyclic)

/**
 * [TwoWheelLayout]配置Float值的便捷类
 */
class WheelFloatConfig(
    /**
     * 最小值
     */
    val min: Float,
    /**
     * 最大值
     */
    val max: Float
) {
    /**
     * 整数部分是否可以循环
     */
    var isIntPartCyclic = false

    /**
     * 整数部分描述
     */
    var intPartDes: String? = null

    /**
     * 整数部分formatter
     */
    var intPartFormatter: WheelIntFormatter? = null

    /**
     * 小数部分是否可以循环
     */
    var isFloatPartCyclic = true

    /**
     * 小数部分描述
     */
    var floatPartDes: String? = null

    /**
     * 小数部分formatter
     */
    var floatPartFormatter: WheelIntFormatter? = null

    init {
        check(min <= max) {
            "min 不能大于 max"
        }
    }
}