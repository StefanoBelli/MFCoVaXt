package it.mobileflow.mfcovaxt.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import it.mobileflow.mfcovaxt.R
import it.mobileflow.mfcovaxt.databinding.ActivityInjectionsByAgeRangeBinding
import it.mobileflow.mfcovaxt.entity.VaxInjectionsSummaryByAgeRange

class InjectionsByAgeRangeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInjectionsByAgeRangeBinding
    private lateinit var data: Array<VaxInjectionsSummaryByAgeRange>
    private val valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return data[value.toInt()].ageRange
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInjectionsByAgeRangeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setBarChart(getInjByAgeRangeEntries())
    }

    private fun getChart() : BarChart {
        val chart = binding.totInjByAgeRangeBc

        chart.axisRight.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(false)
        chart.xAxis.setDrawGridLines(false)
        chart.setDrawBorders(false)
        chart.setTouchEnabled(true)
        chart.isClickable = false
        chart.isDoubleTapToZoomEnabled = false
        chart.isDoubleTapToZoomEnabled = false
        chart.setDrawBorders(false)
        chart.setDrawGridBackground(false)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.setDrawAxisLine(false)
        chart.axisLeft.setDrawAxisLine(false)
        chart.xAxis.setDrawLabels(true)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.labelCount = 9
        chart.xAxis.valueFormatter = valueFormatter
        chart.axisLeft.setDrawLabels(false)
        chart.axisRight.setDrawLabels(false)
        chart.xAxis.setDrawAxisLine(false)
        chart.setScaleEnabled(false)
        chart.animateY(2000)
        chart.setNoDataText(getString(R.string.waiting_for_data))

        return chart
    }

    private fun setBarChart(entries: ArrayList<BarEntry>) {
        val chart = getChart()

        val dataset = BarDataSet(entries, getString(R.string.tot_inj_by_age_range))
        dataset.color = getColor(R.color.blue)
        dataset.highLightAlpha = 0

        val data = BarData(dataset)
        data.barWidth = 0.9f
        chart.data = data
        chart.invalidate()
    }

    private fun getInjByAgeRangeEntries() : ArrayList<BarEntry> {
        val arr = ArrayList<BarEntry>()

        for(i in data.indices) {
            arr.add(BarEntry(i.toFloat(), data[i].totalInj.toFloat()))
        }

        return arr
    }
}