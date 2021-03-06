package it.mobileflow.mfcovaxt.activity

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Process.killProcess
import android.os.Process.myPid
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import it.mobileflow.mfcovaxt.BuildConfig
import it.mobileflow.mfcovaxt.R
import it.mobileflow.mfcovaxt.database.VaxInjectionsStatsDatabase
import it.mobileflow.mfcovaxt.databinding.ActivityMainBinding
import it.mobileflow.mfcovaxt.holder.CommonDataHolder
import it.mobileflow.mfcovaxt.scheduler.LudScheduler
import it.mobileflow.mfcovaxt.scheduler.LudSchedulerSubscriber
import it.mobileflow.mfcovaxt.util.EzDateParser
import it.mobileflow.mfcovaxt.util.EzNumberFormatting
import it.mobileflow.mfcovaxt.util.volleyErrorHandler
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), LudSchedulerSubscriber {
    companion object {
        private const val WAS_INTERNET_CONNECTED_KEY = "was_internet_connected"
        private const val ALREADY_STARTED_KEY = "already_started"
        private const val FIRST_TIME_KEY = "first_time"
        private const val SHPREFS = "it.mobileflow.mfcovaxt_rand493872414267186"
        private const val VOLLEY_ERROR_MYMSG = "MainActivity.populateRightVaxData()"
        private const val DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z"
    }

    private lateinit var binding: ActivityMainBinding
    private val vaxDataViewModel : VaxDataViewModel by viewModels()
    private lateinit var initialLoadingDialog: AlertDialog
    private var needInternetDialog: AlertDialog? = null
    private var wasInternetConnected = true
    private var plotBtnEnableCnt = 3
    private lateinit var infoMsg : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setInfoMsg()

        vaxDataViewModel.db = VaxInjectionsStatsDatabase.getInstance(applicationContext)

        LudScheduler.appContext = applicationContext
        LudScheduler.viewModel = vaxDataViewModel
        LudScheduler.subscriber = this

        showInitialLoadingDialog()
        setLiveDataObservers()
        setOnClickListeners()

        if(savedInstanceState == null) {
            LudScheduler.scheduleUpdate()
        } else {
            wasInternetConnected = savedInstanceState.getBoolean(WAS_INTERNET_CONNECTED_KEY)
            binding.refreshFab.isEnabled = wasInternetConnected
        }
    }

    private fun setInfoMsg() {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault(Locale.Category.FORMAT))
        val buildDateTimeFormatBasedOnUserLocale =
            dateFormat.format(Date(BuildConfig.BUILD_TIMESTAMP))
        infoMsg = "${getString(R.string.basic_info)}\n\n" +
                " * ${getString(R.string.project_src_link)}: ${getString(R.string.project_url)}\n" +
                " * ${getString(R.string.build_compiled)}: ${buildDateTimeFormatBasedOnUserLocale}\n" +
                " * ${getString(R.string.build_type)}: ${BuildConfig.BUILD_TYPE}"
    }

    private fun setOnClickListeners() {
        binding.refreshFab.setOnClickListener {
            LudScheduler.scheduleUpdate()
        }

        binding.totInjByAgeRangeBtn.setOnClickListener {
            launchActivity(InjectionsByAgeRangeActivity::class.java)
        }

        binding.injLocationsBtn.setOnClickListener {
            launchActivity(InjectionLocationActivity::class.java)
        }

        binding.plotBtn.setOnClickListener {
            launchActivity(DataPlotActivity::class.java)
        }

        binding.coccardaIv.setOnLongClickListener {

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.appinfo))
                .setIcon(R.drawable.ic_baseline_info_24)
                .setCancelable(false)
                .setMessage(infoMsg)
                .setPositiveButton(R.string.ok) { actualDialog: DialogInterface, _: Int ->
                    actualDialog.cancel()
                }
                .show() //Dialog
                .getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(getColor(R.color.blue))
            true
        }
    }

    private fun setLiveDataObservers() {
        vaxDataViewModel.lastUpdateDatasetDate.observe(this, {
            lifecycleScope.launch(Dispatchers.Default) {
                setLastUpdateDatasetDate()
            }
            dismissInitialLoadingDialogIfShowing()
        })

        vaxDataViewModel.vaxInjections.observe(this, {
            lifecycleScope.launch(Dispatchers.Default) {
                setTotalInjsAndTotalVaxed()
                CommonDataHolder.vaxInjections = it
                unlockPlotBtn()
            }
            dismissInitialLoadingDialogIfShowing()
        })

        vaxDataViewModel.vaxStatsSummariesByArea.observe(this, {
            lifecycleScope.launch(Dispatchers.Default) {
                setAreaStatsInjsTable()
            }
            dismissInitialLoadingDialogIfShowing()
        })

        vaxDataViewModel.physicalInjectionLocations.observe(this, {
            CommonDataHolder.physicalInjectionLocations = it
            binding.injLocationsBtn.isEnabled = true
            dismissInitialLoadingDialogIfShowing()
        })

        vaxDataViewModel.vaxInjectionsSummariesByAgeRange.observe(this, {
            lifecycleScope.launch(Dispatchers.Default) {
                CommonDataHolder.vaxInjectionsSummaryByAgeRanges = it
                withContext(Dispatchers.Main) {
                    binding.totInjByAgeRangeBtn.isEnabled = true
                }
                CommonDataHolder.ageRanges = setOf()
                for (summaryByAgeRange in it) {
                    CommonDataHolder.ageRanges += summaryByAgeRange.ageRange
                }
                unlockPlotBtn()
            }
            dismissInitialLoadingDialogIfShowing()
        })

        vaxDataViewModel.vaxDeliveries.observe(this, {
            lifecycleScope.launch(Dispatchers.Default)  {
                CommonDataHolder.vaxDeliveries = it
                CommonDataHolder.vaxes = setOf()
                for (vax in it) {
                    CommonDataHolder.vaxes += vax.vaxName
                }
                unlockPlotBtn()
            }
            dismissInitialLoadingDialogIfShowing()
        })
    }

    private suspend fun setAreaStatsInjsTable() {
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

        for(i in 0 until 62 step 3) {
            val iDivThree = i / 3
            totalInjsItaly += arrData[iDivThree].totalInjs
            totalDeliveriesItaly += arrData[iDivThree].totalDelivVaxes

            val fmtTotalInjs = EzNumberFormatting.format(this, arrData[iDivThree].totalInjs)
            val fmtTotalDeliv = EzNumberFormatting.format(this,
                    arrData[iDivThree].totalDelivVaxes)
            val fmtPercInjs = String.format(
                    baseContext.resources.configuration.locales[0],
                    "%.1f", arrData[iDivThree].percInjs)

            withContext(Dispatchers.Main) {
                arrView[i].text = fmtTotalInjs
                arrView[i + 1].text = fmtTotalDeliv
                arrView[i + 2].text = fmtPercInjs
            }
        }

        val fmtTotalInjsItaly = EzNumberFormatting.format(this, totalInjsItaly)
        val fmtTotalDelivItaly = EzNumberFormatting.format(this, totalDeliveriesItaly)
        val fmtPercInjsItaly = String.format(
                baseContext.resources.configuration.locales[0],
                "%.1f", (100f * totalInjsItaly) / totalDeliveriesItaly)

        withContext(Dispatchers.Main) {
            binding.italyInjTv.text = fmtTotalInjsItaly
            binding.italyDelivTv.text = fmtTotalDelivItaly
            binding.italyPercTv.text = fmtPercInjsItaly
            binding.totalInjNumTv.text = fmtTotalInjsItaly
        }
    }

    private suspend fun setTotalInjsAndTotalVaxed() {
        val injs = vaxDataViewModel.vaxInjections.value!!
        var totVaxed = 0

        for(inj in injs) {
            totVaxed += if(inj.vaxName == "Janssen") inj.firstInj else inj.secondInj
        }

        val fmtTotVaxed = EzNumberFormatting.format(baseContext, totVaxed)

        withContext(Dispatchers.Main) {
            binding.fullyVaxedNumTv.text = fmtTotVaxed
        }
    }

    private suspend fun setLastUpdateDatasetDate() {
        val fmtLastUpdatedDate = String.format(getString(R.string.last_updated_on_fmt),
                EzDateParser.dateTimeToStr(
                        vaxDataViewModel.lastUpdateDatasetDate.value!!,
                        this))

        withContext(Dispatchers.Main) {
            binding.lastUpdatedOnTv.text = fmtLastUpdatedDate
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ALREADY_STARTED_KEY, true)
        outState.putBoolean(WAS_INTERNET_CONNECTED_KEY, wasInternetConnected)
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

    private fun dismissInitialLoadingDialogIfShowing() {
        if(initialLoadingDialog.isShowing) {
            initialLoadingDialog.dismiss()
        }
    }

    private fun populateRightVaxData() {
        vaxDataViewModel.populateVaxData(
                VaxDataViewModel.VaxData.VAX_INJECTIONS, applicationContext
        ) { volleyErrorHandler(this, it,
                "$VOLLEY_ERROR_MYMSG [VaxInjections]") }
        vaxDataViewModel.populateVaxData(
                VaxDataViewModel.VaxData.VAX_STATS_SUMMARIES_BY_AREA, applicationContext
        ) { volleyErrorHandler(this, it,
                "$VOLLEY_ERROR_MYMSG [VaxStatsSummariesByArea]") }
        vaxDataViewModel.populateVaxData(
            VaxDataViewModel.VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE, applicationContext
        ) { volleyErrorHandler(this, it,
            "$VOLLEY_ERROR_MYMSG [VaxInjectionSummariesByAgeRange]") }
        vaxDataViewModel.populateVaxData(
            VaxDataViewModel.VaxData.PHYSICAL_INJECTION_LOCATIONS, applicationContext
        ) { volleyErrorHandler(this, it,
            "$VOLLEY_ERROR_MYMSG [PhysicalInjectionLocations]") }
        vaxDataViewModel.populateVaxData(
            VaxDataViewModel.VaxData.VAX_DELIVERIES, applicationContext
        ) { volleyErrorHandler(this, it,
            "$VOLLEY_ERROR_MYMSG [VaxDeliveries]") }
    }

    override fun onLsuUpdateOk(lsuSync: Boolean, dataSync: Boolean) {
        wasInternetConnected = true
        if(needInternetDialog != null && needInternetDialog?.isShowing == true) {
            needInternetDialog!!.dismiss()
        }

        populateRightVaxData()
        getSharedPreferences(SHPREFS, MODE_PRIVATE).edit().putBoolean(FIRST_TIME_KEY, false).apply()
        val msg = if(lsuSync) R.string.everything_up_to_date
            else R.string.new_data_available
        showSnackbar(msg)
        binding.refreshFab.isEnabled = true
    }

    override fun onLsuUpdateInProgress() {
        showSnackbar(R.string.an_lsu_update_already_in_progress)
        binding.refreshFab.isEnabled = false
    }

    override fun onLsuUpdateNoConnectivity() {
        wasInternetConnected = false
        if (getSharedPreferences(SHPREFS, MODE_PRIVATE).getBoolean(FIRST_TIME_KEY, true)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
            dismissInitialLoadingDialogIfShowing()
            needInternetDialog = AlertDialog.Builder(this@MainActivity)
                    .setTitle(R.string.need_internet)
                    .setMessage(R.string.no_data_need_internet)
                    .setCancelable(false)
                    .setNeutralButton(R.string.exit_app)
                    { _: DialogInterface, _: Int -> killProcess(myPid()) }
                    .setOnDismissListener()
                    { requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR }
                    .create()
            needInternetDialog!!.show()
        } else {
            populateRightVaxData()
            showSnackbar(R.string.could_not_detect_internet_conn)
            binding.refreshFab.isEnabled = false
        }
    }

    private fun showSnackbar(strId: Int) {
        Snackbar.make(binding.sv, strId, Snackbar.LENGTH_SHORT).show()
    }

    private fun <T> launchActivity(cls: Class<T>) {
        val intent = Intent(this, cls)
        startActivity(intent)
    }

    private suspend fun unlockPlotBtn() {
        if(plotBtnEnableCnt > 0) {
            if (plotBtnEnableCnt == 1) {
                withContext(Dispatchers.Main) {
                    binding.plotBtn.isEnabled = true
                }
            }

            --plotBtnEnableCnt
        }
    }
}