package it.mobileflow.mfcovaxt.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import it.mobileflow.mfcovaxt.databinding.ActivityInjectionsByAgeRangeBinding
import it.mobileflow.mfcovaxt.factory.VaxDataViewModelFactory
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel

class InjectionsByAgeRangeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInjectionsByAgeRangeBinding
    private lateinit var vaxDataViewModel: VaxDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInjectionsByAgeRangeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vaxDataViewModel = ViewModelProvider(this, VaxDataViewModelFactory.getInstance())
            .get(VaxDataViewModel::class.java)

        setBarChart()
    }

    private fun setBarChart() {
        val bChart = binding.totInjByAgeRangeBc

        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(15f, 420f))
        entries.add(BarEntry(25f, 475f))
        entries.add(BarEntry(35f, 508f))

        val barDataSet = BarDataSet(entries, "Total injections")

        val barData = BarData(barDataSet)

        barData.barWidth = 5f
        barData.setDrawValues(false)
        bChart.data = barData
        bChart.axisRight.setDrawGridLines(false)
        bChart.axisLeft.setDrawGridLines(false)
        bChart.xAxis.setDrawGridLines(false)
        bChart.setDrawBorders(false)
        bChart.setTouchEnabled(true);
        bChart.setClickable(false);
        bChart.setDoubleTapToZoomEnabled(false);
        bChart.setDoubleTapToZoomEnabled(false);

        bChart.setDrawBorders(false);
        bChart.setDrawGridBackground(false);

        bChart.getDescription().setEnabled(false);
        bChart.getLegend().setEnabled(false);
        bChart.getAxisRight().setDrawAxisLine(false);

        bChart.getAxisLeft().setDrawAxisLine(false);
        bChart.getXAxis().setDrawLabels(false)
        bChart.getAxisLeft().setDrawLabels(false)
        bChart.axisRight.setDrawLabels(false)
        bChart.getXAxis().setDrawAxisLine(false);

        bChart.animateY(2000)
    }
}