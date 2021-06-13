package it.mobileflow.mfcovaxt.util

import android.content.Context
import java.text.DecimalFormat
import java.text.NumberFormat

class EzNumberFormatting {
    companion object {
        fun format(context: Context, v: Int) : String{
            val formatter = NumberFormat.getInstance(
                    context.resources.configuration.locales[0]) as DecimalFormat
            return formatter.format(v)
        }
    }
}