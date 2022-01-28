package com.github.kilnn.wheellayout

/**
 * 格式化Int值
 */
interface WheelIntFormatter {
    /**
     * @param index  The value index
     * @param value The value
     */
    fun format(index: Int, value: Int): String
}