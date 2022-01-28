package com.github.kilnn.wheellayout

interface WheelIntFormatter {
    /**
     * @param index  The value index
     * @param value The value
     */
    fun format(index: Int, value: Int): String
}