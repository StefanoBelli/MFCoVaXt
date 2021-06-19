package it.mobileflow.mfcovaxt.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import it.mobileflow.mfcovaxt.R
import it.mobileflow.mfcovaxt.databinding.ActivityDataPlotBinding
import it.mobileflow.mfcovaxt.holder.CommonDataHolder

class DataPlotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataPlotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataPlotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.byVaxSpinner.adapter = getSimpleArrayAdapter(CommonDataHolder.vaxes)
        binding.byAgeRangeSpinner.adapter = getSimpleArrayAdapter(CommonDataHolder.ageRanges)

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
        val chart = binding.plotLc

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
        chart.xAxis.setDrawLabels(true)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.axisLeft.setDrawLabels(true)
        chart.axisRight.setDrawLabels(false)
        chart.xAxis.setDrawAxisLine(false)
        chart.setScaleEnabled(false)
        chart.animateY(2000)
        chart.setNoDataText(getString(R.string.waiting_for_data))
        chart.isHighlightPerDragEnabled = false
        chart.defaultFocusHighlightEnabled = false
        chart.isHighlightPerTapEnabled = false

        /*
        val entries = mutableListOf(Entry(1f,2f), Entry(2f,4f), Entry(3f,2f), Entry(4f,5f))
        val dataset = LineDataSet(entries, "ciao")

        dataset.color = getColor(R.color.blue)
        dataset.setCircleColor(getColor(R.color.darker_blue))
        chart.data = LineData(dataset)*/
    }

    private fun getSimpleArrayAdapter(values: Set<String>) : ArrayAdapter<String> {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            values.toTypedArray())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    private fun updatePlot(dataIndex: Int, areaIndex: Int, ageRangeIndex: Int, vaxIndex: Int) {

    }

    open inner class ItemSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
            updatePlot(
                binding.dataTypeSpinner.selectedItemPosition,
                binding.byAreaSpinner.selectedItemPosition,
                binding.byAgeRangeSpinner.selectedItemPosition,
                binding.byVaxSpinner.selectedItemPosition)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
}