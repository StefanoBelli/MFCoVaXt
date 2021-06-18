package it.mobileflow.mfcovaxt.activity

import android.os.Bundle
import android.renderscript.Sampler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.snackbar.Snackbar
import it.mobileflow.mfcovaxt.R
import it.mobileflow.mfcovaxt.databinding.ActivityInjectionsByAgeRangeBinding
import it.mobileflow.mfcovaxt.factory.VaxDataViewModelFactory
import it.mobileflow.mfcovaxt.scheduler.LudScheduler
import it.mobileflow.mfcovaxt.scheduler.LudSchedulerSubscriber
import it.mobileflow.mfcovaxt.util.volleyErrorHandler
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel

class InjectionsByAgeRangeActivity : AppCompatActivity(), LudSchedulerSubscriber {
    companion object {
        private const val VOLLEY_ERROR_MYMSG = "InjectionsByAgeRangeActivity.populateRightData()"
    }
    private lateinit var binding: ActivityInjectionsByAgeRangeBinding
    private lateinit var vaxDataViewModel: VaxDataViewModel

    //TODO handle screen rotation + already disabled fab button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInjectionsByAgeRangeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vaxDataViewModel = ViewModelProvider(this, VaxDataViewModelFactory.getInstance())
            .get(VaxDataViewModel::class.java)

        LudScheduler.subscriber = this

        setOnClickListeners()
        setObserver()
        populateRightVaxData()

        setBarChart(getInjByAgeRangeEntries())
    }

    private fun setOnClickListeners() {
        binding.refreshFab.setOnClickListener {
            LudScheduler.scheduleUpdate()
        }
    }

    private fun setObserver() {
        vaxDataViewModel.vaxInjectionsSummariesByAgeRange.observe(this, {
            setBarChart(getInjByAgeRangeEntries())
        })
    }

    private fun setBarChart(entries: ArrayList<BarEntry>) {
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
        chart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return vaxDataViewModel
                        .vaxInjectionsSummariesByAgeRange.value!![value.toInt()].ageRange
            }
        }
        chart.axisLeft.setDrawLabels(false)
        chart.axisRight.setDrawLabels(false)
        chart.xAxis.setDrawAxisLine(false)
        chart.setScaleEnabled(false)
        chart.animateY(2000)
        chart.setNoDataText(getString(R.string.waiting_for_data))

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
        val dataset = vaxDataViewModel.vaxInjectionsSummariesByAgeRange.value

        if(dataset != null) {
            for(i in dataset.indices) {
                arr.add(BarEntry(i.toFloat(), dataset[i].totalInj.toFloat()))
            }
        }

        return arr
    }

    //TODO IF NO CONN THEN SCHEDULE UPDATE()
    private fun populateRightVaxData() {
        vaxDataViewModel.populateVaxData(
                VaxDataViewModel.VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE,
                applicationContext)
        { volleyErrorHandler(applicationContext, it, VOLLEY_ERROR_MYMSG) }
    }

    override fun onLsuUpdateInProgress() {
        showSnackbar(R.string.an_lsu_update_already_in_progress)
        binding.refreshFab.isEnabled = false
    }

    override fun onLsuUpdateNoConnectivity() {
        showSnackbar(R.string.could_not_detect_internet_conn)
        binding.refreshFab.isEnabled = false
    }

    override fun onLsuUpdateOk(lsuSync: Boolean, dataSync: Boolean) {
        populateRightVaxData()
        val msg = if(lsuSync && dataSync) R.string.everything_up_to_date
        else if(lsuSync && !dataSync) R.string.lsu_up_to_date_maybe_no_data
        else R.string.new_data_available
        showSnackbar(msg)
        binding.refreshFab.isEnabled = true
    }

    private fun showSnackbar(strId: Int) {
        Snackbar.make(binding.cl, strId, Snackbar.LENGTH_LONG).show()
    }
}