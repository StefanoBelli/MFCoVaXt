package it.mobileflow.mfcovaxt.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
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
        private const val DB_NAME = "mfcovaxtdb"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var vaxDataViewModel: VaxDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vaxDataViewModel = ViewModelProvider(this, VaxDataViewModelFactory.getInstance())
                .get(VaxDataViewModel::class.java)

        val db = Room.databaseBuilder(
                applicationContext,
                VaxInjectionsStatsDatabase::class.java, DB_NAME
        ).build()

        /*
        vaxDataViewModel.db = db
        vaxDataViewModel.lastUpdateDataset(this, { Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()}, {})
        */
    }
}