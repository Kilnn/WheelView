package com.github.kilnn.wheelview.sample

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDialogFragment
import com.github.kilnn.wheellayout.OneWheelLayout
import com.github.kilnn.wheellayout.WheelIntConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class OneWheelDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("身高CM")
            .setNegativeButton(android.R.string.cancel, null)
        val oneWheelLayout = OneWheelLayout(requireContext())
        val config = WheelIntConfig(20, 242, false, "cm", null)
        oneWheelLayout.setConfig(config)
        oneWheelLayout.setValue(180)//默认180
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            Log.e("Kilnn", "Select:" + oneWheelLayout.getValue())
        }
        return builder.setView(oneWheelLayout).create()
    }

}