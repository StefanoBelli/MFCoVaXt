package it.mobileflow.mfcovaxt.activity

import android.os.Bundle
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
        pb.setPadding(tenDp,tenDp, tenDp, tenDp)

        val tv = TextView(this)
        tv.text = getString(R.string.please_wait)
        tv.setPadding(0,(23 * scale + 0.5f).toInt(),0,0)

        ll.addView(pb)
        ll.addView(tv)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.loading_data))
        builder.setCancelable(false)
        builder.setView(ll)

        initialLoadingDialog = builder.create()
        initialLoadingDialog.show()
    }
}