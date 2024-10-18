package com.github.kilnn.wheelview.sample

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDialogFragment
import com.github.kilnn.wheellayout.DateWheelLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DateWheelDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("身高Inch")
            .setNegativeButton(android.R.string.cancel, null)
        val dateWheelLayout = DateWheelLayout(requireContext(), order = false)
        dateWheelLayout.setConfig(
            start = null,
            end = null,
            yearDes = "年",
            monthDes = "月",
            dayDes = "日"
        )
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            Log.e("Kilnn", "Select:" + dateWheelLayout.getDate().contentToString())
        }
        return builder.setView(dateWheelLayout).create()
    }

}