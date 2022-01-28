package com.github.kilnn.wheelview.sample

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDialogFragment
import com.github.kilnn.wheellayout.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TwoWheelDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("身高Inch")
            .setNegativeButton(android.R.string.cancel, null)
        val twoWheelLayout = TwoWheelLayout(requireContext())
        val config = WheelFloatConfig(20.5f, 350.1f)
        config.floatPartDes = "kg"
        config.isFloatPartCyclic = false
        twoWheelLayout.setFloatConfig(config)
        twoWheelLayout.setFloatValue(160f)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            Log.e("Kilnn", "Select:" + twoWheelLayout.getFlowValue())
        }
        return builder.setView(twoWheelLayout).create()
    }

}