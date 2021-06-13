package it.mobileflow.mfcovaxt.activity

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import it.mobileflow.mfcovaxt.R
import it.mobileflow.mfcovaxt.database.VaxInjectionsStatsDatabase
import it.mobileflow.mfcovaxt.databinding.ActivityMainBinding
import it.mobileflow.mfcovaxt.factory.VaxDataViewModelFactory
import it.mobileflow.mfcovaxt.util.EzDateParser
import it.mobileflow.mfcovaxt.util.EzNumberFormatting
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel

/**
 * define procedure update():
 *  \@async check for updates
 *  if success then
 *    reschedule next update call in 30 mins
 *  else if no connectivity then
 *    block any update request
 *    schedule next update call when device is online
 *    show snackbar with warning
 *  else // update already in progress
 *    reschedule next update call in 30s
 *    show a toast with warning
 * endproc
 *
 * show dialog saying data is loading...
 * load data from local storage
 * when at least one observer is called, then hide dialog
 * update()
 * setFabClickListener() -> { update() }
 */
class MainActivity : AppCompatActivity() {
    companion object {
        private const val ALREADY_STARTED_KEY = "already_started"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var vaxDataViewModel: VaxDataViewModel
    private lateinit var initialLoadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vaxDataViewModel = ViewModelProvider(this, VaxDataViewModelFactory.getInstance())
                .get(VaxDataViewModel::class.java)
        vaxDataViewModel.db = VaxInjectionsStatsDatabase.getInstance(applicationContext)

        showInitialLoadingDialog()
        setLiveDataObservers()
        startTargetedUpdate()
    }

    private fun setLiveDataObservers() {
        vaxDataViewModel.lastUpdateDatasetDate.observe(this, {
            setLastUpdateDatasetDate()
            dismissDialogIfShowing()
        })

        vaxDataViewModel.vaxInjectionsSummariesByAgeRange.observe(this, {
            setTotalInjsAndTotalVaxed()
            dismissDialogIfShowing()
        })

        vaxDataViewModel.vaxStatsSummariesByArea.observe(this, {
            setAreaStatsInjsTable()
            dismissDialogIfShowing()
        })
    }

    private fun setAreaStatsInjsTable() {
        val abr = vaxDataViewModel.vaxStatsSummariesByArea.value!![0]
        val bas = vaxDataViewModel.vaxStatsSummariesByArea.value!![1]
        val cal = vaxDataViewModel.vaxStatsSummariesByArea.value!![2]
        val cam = vaxDataViewModel.vaxStatsSummariesByArea.value!![3]
        val emr = vaxDataViewModel.vaxStatsSummariesByArea.value!![4]
        val fvg = vaxDataViewModel.vaxStatsSummariesByArea.value!![5]
        val laz = vaxDataViewModel.vaxStatsSummariesByArea.value!![6]
        val lig = vaxDataViewModel.vaxStatsSummariesByArea.value!![7]
        val lom = vaxDataViewModel.vaxStatsSummariesByArea.value!![8]
        val mar = vaxDataViewModel.vaxStatsSummariesByArea.value!![9]
        val mol = vaxDataViewModel.vaxStatsSummariesByArea.value!![10]
        val pab = vaxDataViewModel.vaxStatsSummariesByArea.value!![11]
        val pat = vaxDataViewModel.vaxStatsSummariesByArea.value!![12]
        val pie = vaxDataViewModel.vaxStatsSummariesByArea.value!![13]
        val pug = vaxDataViewModel.vaxStatsSummariesByArea.value!![14]
        val sar = vaxDataViewModel.vaxStatsSummariesByArea.value!![15]
        val sic = vaxDataViewModel.vaxStatsSummariesByArea.value!![16]
        val tos = vaxDataViewModel.vaxStatsSummariesByArea.value!![17]
        val umb = vaxDataViewModel.vaxStatsSummariesByArea.value!![18]
        val vda = vaxDataViewModel.vaxStatsSummariesByArea.value!![19]
        val ven = vaxDataViewModel.vaxStatsSummariesByArea.value!![20]

        val arrData = arrayOf(abr, bas, cal, cam, emr, fvg, laz, lig, lom, mar, mol, pab, pat,
                pie, pug, sar, sic, tos, umb, vda, ven)

        val arrView = arrayOf(
                binding.abrInjTv, binding.abrDelivTv, binding.abrPercTv,
                binding.basInjTv, binding.basDelivTv, binding.basPercTv,
                binding.calInjTv, binding.calDelivTv, binding.calPercTv,
                binding.camInjTv, binding.camDelivTv, binding.camPercTv,
                binding.emrInjTv, binding.emrDelivTv, binding.emrPercTv,
                binding.fvgInjTv, binding.fvgDelivTv, binding.fvgPercTv,
                binding.lazInjTv, binding.lazDelivTv, binding.lazPercTv,
                binding.ligInjTv, binding.ligDelivTv, binding.ligPercTv,
                binding.lomInjTv, binding.lomDelivTv, binding.lomPercTv,
                binding.marInjTv, binding.marDelivTv, binding.marPercTv,
                binding.molInjTv, binding.molDelivTv, binding.molPercTv,
                binding.pabInjTv, binding.pabDelivTv, binding.pabPercTv,
                binding.patInjTv, binding.patDelivTv, binding.patPercTv,
                binding.pieInjTv, binding.pieDelivTv, binding.piePercTv,
                binding.pugInjTv, binding.pugDelivTv, binding.pugPercTv,
                binding.sarInjTv, binding.sarDelivTv, binding.sarPercTv,
                binding.sicInjTv, binding.sicDelivTv, binding.sicPercTv,
                binding.tosInjTv, binding.tosDelivTv, binding.tosPercTv,
                binding.umbInjTv, binding.umbDelivTv, binding.umbPercTv,
                binding.vdaInjTv, binding.vdaDelivTv, binding.vdaPercTv,
                binding.venInjTv, binding.venDelivTv, binding.venPercTv)

        var totalInjsItaly = 0
        var totalDeliveriesItaly = 0
        var totalPercItaly = 0f

        for(i in 0 until 62 step 3) {
            val iDivThree = i / 3
            totalInjsItaly += arrData[iDivThree].totalInjs
            totalDeliveriesItaly += arrData[iDivThree].totalDelivVaxes
            totalPercItaly += arrData[iDivThree].percInjs

            arrView[i].text = EzNumberFormatting.format(this, arrData[iDivThree].totalInjs)
            arrView[i + 1].text = EzNumberFormatting.format(this,
                    arrData[iDivThree].totalDelivVaxes)
            arrView[i + 2].text = arrData[iDivThree].percInjs.toString()
        }

        binding.italyInjTv.text = EzNumberFormatting.format(this, totalInjsItaly)
        binding.italyDelivTv.text = EzNumberFormatting.format(this, totalDeliveriesItaly)
        binding.italyPercTv.text = (totalPercItaly / 21).toString()
    }

    private fun setTotalInjsAndTotalVaxed() {
        val ageRanges = vaxDataViewModel.vaxInjectionsSummariesByAgeRange.value!!
        var totInjs = 0
        var totVaxed = 0

        for(ageRange in ageRanges) {
            totInjs += ageRange.totalInj
            totVaxed += ageRange.secondInjs
        }

        binding.totalInjNumTv.text = EzNumberFormatting.format(this,totInjs)
        binding.fullyVaxedNumTv.text = EzNumberFormatting.format(this, totVaxed)
    }

    private fun setLastUpdateDatasetDate() {
        binding.lastUpdatedOnTv.text =
                String.format(getString(R.string.last_updated_on_fmt),
                        EzDateParser.dateTimeToStr(
                                vaxDataViewModel.lastUpdateDatasetDate.value!!,
                                this))
    }

    private fun startTargetedUpdate() {
        if(vaxDataViewModel.lastUpdateDataset(applicationContext, {
            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE, applicationContext, {})
            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.VAX_STATS_SUMMARIES_BY_AREA, applicationContext, {})
        },{}) == VaxDataViewModel.LudError.NO_CONNECTIVITY) {
            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE, applicationContext, {})
            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.VAX_STATS_SUMMARIES_BY_AREA, applicationContext, {})
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ALREADY_STARTED_KEY, true)
    }

    private fun showInitialLoadingDialog() {
        val scale = resources.displayMetrics.density
        val tenDp = (10 * scale + 0.5f).toInt()

        val ll = LinearLayout(this)

        val pb = ProgressBar(this)
        pb.isIndeterminate = true
        pb.setPadding(tenDp, tenDp, tenDp, tenDp)

        val tv = TextView(this)
        tv.text = getString(R.string.please_wait)
        tv.setPadding(0, (23 * scale + 0.5f).toInt(), 0, 0)

        ll.addView(pb)
        ll.addView(tv)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.loading_data))
        builder.setCancelable(false)
        builder.setView(ll)

        initialLoadingDialog = builder.create()
        initialLoadingDialog.show()
    }

    private fun dismissDialogIfShowing() {
        if(initialLoadingDialog.isShowing) {
            initialLoadingDialog.dismiss()
        }
    }
}