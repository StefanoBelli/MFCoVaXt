package it.mobileflow.mfcovaxt.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import it.mobileflow.mfcovaxt.R
import it.mobileflow.mfcovaxt.databinding.ActivityDataPlotBinding
import it.mobileflow.mfcovaxt.holder.CommonDataHolder
import it.mobileflow.mfcovaxt.util.EzDateParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DataPlotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataPlotBinding
    private lateinit var areas: Array<String>
    private lateinit var chart: LineChart
    private val valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return EzDateParser.fmtLongToOnlyDateStr(value.toLong(), baseContext)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataPlotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chart = binding.plotLc

        areas = resources.getStringArray(R.array.areas)

        val vaxesOpts = setOf(getString(R.string.all_vaxes)) + CommonDataHolder.vaxes
        val ageRangesOpts = setOf(getString(R.string.all_ageranges)) + CommonDataHolder.ageRanges
        binding.byVaxSpinner.adapter = getSimpleArrayAdapter(vaxesOpts)
        binding.byAgeRangeSpinner.adapter = getSimpleArrayAdapter(ageRangesOpts)

        setLineChart()
        setListeners()
    }

    private fun setListeners() {
        binding.dataTypeSpinner.onItemSelectedListener = object : ItemSelectedListener() {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                binding.byAgeRangeSpinner.isEnabled = pos < 3
                super.onItemSelected(p, v, pos, id)
            }
        }

        binding.byAreaSpinner.onItemSelectedListener = ItemSelectedListener()
        binding.byAgeRangeSpinner.onItemSelectedListener = ItemSelectedListener()
        binding.byVaxSpinner.onItemSelectedListener = ItemSelectedListener()
    }

    private fun setLineChart() {
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
        chart.axisLeft.setDrawAxisLine(true)
        chart.axisLeft.labelCount = 6
        chart.xAxis.setDrawLabels(true)
        chart.xAxis.labelCount = 5
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.axisLeft.setDrawLabels(true)
        chart.axisRight.setDrawLabels(false)
        chart.xAxis.setDrawAxisLine(false)
        chart.xAxis.valueFormatter = valueFormatter
        chart.setScaleEnabled(false)
        chart.setNoDataText(getString(R.string.waiting_for_data))
        chart.isHighlightPerDragEnabled = false
        chart.defaultFocusHighlightEnabled = false
        chart.isHighlightPerTapEnabled = false
    }

    private fun getSimpleArrayAdapter(values: Set<String>): ArrayAdapter<String> {
        val adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            values.toTypedArray()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    private fun updatePlot(dataIndex: Int, area: Int, ageRange: String?, vax: String?) {
        lifecycleScope.launch(Dispatchers.Default) {
            when (dataIndex) {
                0 -> updatePlotTotalInjsData(area, ageRange, vax)
                1 -> updatePlotFirstInjsData(area, ageRange, vax)
                2 -> updatePlotSecondInjsData(area, ageRange, vax)
                3 -> updatePlotVaxDeliveryData(area, vax)
            }
        }
    }

    private fun updatePlotVaxDeliveryData(area: Int, vax: String?) {
        val filtered = CommonDataHolder.vaxDeliveries

        val dates: MutableSet<Date> = mutableSetOf()
        for (delivery in filtered) {
            dates += delivery.deliveryDate
        }

        val plotEntries : MutableList<Entry> = mutableListOf()
        val delivForDate : MutableMap<Date, Int> = mutableMapOf()
        var totalDeliveries = 0

        for (date in dates.sorted()) {
            filtered.filter {
                it.deliveryDate == date
            }.filter {
                if(vax != null) it.vaxName == vax else true
            }.filter {
                if(area > 0) areas[area] == it.areaName else true
            }.forEach {
                totalDeliveries += it.numOfVaxes
            }

            delivForDate[date] = totalDeliveries
            plotEntries.add(Entry(date.time.toFloat(), delivForDate[date]!!.toFloat()))
        }

        applyNewDataset(R.string.vax_availability, plotEntries)
    }

    private fun updatePlotSecondInjsData(area: Int, ageRange: String?, vax: String?) {
        val filtered = CommonDataHolder.vaxInjections

        val dates: MutableSet<Date> = mutableSetOf()
        for (injection in filtered) {
            dates += injection.injDate
        }

        val plotEntries : MutableList<Entry> = mutableListOf()
        val secondInjsForDate : MutableMap<Date, Int> = mutableMapOf()
        var totalSecondInjs = 0

        for (date in dates.sorted()) {
            filtered.filter {
                it.injDate == date
            }.filter {
                if(vax != null) it.vaxName == vax else true
            }.filter {
                if(area > 0) areas[area] == it.areaName else true
            }.filter {
                if(ageRange != null) it.ageRange == ageRange else true
            }.forEach {
                totalSecondInjs += it.secondInj
            }

            secondInjsForDate[date] = totalSecondInjs
            plotEntries.add(Entry(date.time.toFloat(), secondInjsForDate[date]!!.toFloat()))
        }

        applyNewDataset(R.string.second_injs, plotEntries)
    }

    private fun updatePlotFirstInjsData(area: Int, ageRange: String?, vax: String?) {
        val filtered = CommonDataHolder.vaxInjections

        val dates: MutableSet<Date> = mutableSetOf()
        for (injection in filtered) {
            dates += injection.injDate
        }

        val plotEntries : MutableList<Entry> = mutableListOf()
        val secondInjsForDate : MutableMap<Date, Int> = mutableMapOf()
        var totalFirstInjs = 0

        for (date in dates.sorted()) {
            filtered.filter {
                it.injDate == date
            }.filter {
                if(vax != null) it.vaxName == vax else true
            }.filter {
                if(area > 0) areas[area] == it.areaName else true
            }.filter {
                if(ageRange != null) it.ageRange == ageRange else true
            }.forEach {
                totalFirstInjs += it.firstInj
            }

            secondInjsForDate[date] = totalFirstInjs
            plotEntries.add(Entry(date.time.toFloat(), secondInjsForDate[date]!!.toFloat()))
        }

        applyNewDataset(R.string.first_injs, plotEntries)
    }

    private fun updatePlotTotalInjsData(area: Int, ageRange: String?, vax: String?) {
        val filtered = CommonDataHolder.vaxInjections

        val dates: MutableSet<Date> = mutableSetOf()
        for (injection in filtered) {
            dates += injection.injDate
        }

        val plotEntries : MutableList<Entry> = mutableListOf()
        val totalInjsForDate : MutableMap<Date, Int> = mutableMapOf()
        var totalInjs = 0

        for (date in dates.sorted()) {
            filtered.filter {
                it.injDate == date
            }.filter {
                if(vax != null) it.vaxName == vax else true
            }.filter {
                if(area > 0) areas[area] == it.areaName else true
            }.filter{
                if(ageRange != null) it.ageRange == ageRange else true
            }.forEach {
                totalInjs += it.firstInj + it.secondInj
            }

            totalInjsForDate[date] = totalInjs
            plotEntries.add(Entry(date.time.toFloat(), totalInjsForDate[date]!!.toFloat()))
        }

        applyNewDataset(R.string.tot_inj, plotEntries)
    }

    private fun applyNewDataset(labelRes: Int, entries: List<Entry>) {
        val dataset = LineDataSet(entries, getString(labelRes))
        dataset.color = getColor(R.color.blue)
        dataset.setDrawCircleHole(false)
        dataset.setDrawCircles(false)

        chart.data = LineData(dataset)
        chart.invalidate()
    }

    open inner class ItemSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
            updatePlot(
                binding.dataTypeSpinner.selectedItemPosition,
                binding.byAreaSpinner.selectedItemPosition,
                if(binding.byAgeRangeSpinner.selectedItemPosition == 0) null
                else (binding.byAgeRangeSpinner.selectedView as TextView).text.toString(),
                if(binding.byVaxSpinner.selectedItemPosition == 0) null
                else (binding.byVaxSpinner.selectedView as TextView).text.toString()
            )
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
}