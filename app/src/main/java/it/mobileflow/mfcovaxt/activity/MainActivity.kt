package it.mobileflow.mfcovaxt.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.room.Room
import com.android.volley.VolleyError
import it.mobileflow.mfcovaxt.database.VaxInjectionsStatsDatabase
import it.mobileflow.mfcovaxt.databinding.ActivityMainBinding
import it.mobileflow.mfcovaxt.listener.OnGenericListener
import it.mobileflow.mfcovaxt.util.EzAppDataUpdateTracker
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel

class MainActivity : AppCompatActivity() {
    companion object {
        private const val DB_NAME = "mfcovaxtdb"
    }

    private lateinit var binding : ActivityMainBinding
    private val vaxDataViewModel : VaxDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = Room.databaseBuilder(
            applicationContext,
            VaxInjectionsStatsDatabase::class.java, DB_NAME
        ).build()

        var shouldRun = false
        vaxDataViewModel.db = db
        val err = vaxDataViewModel.lastUpdateDataset(this, object : OnGenericListener<Boolean> {
            override fun onEvent(arg: Boolean) {
                Toast.makeText(baseContext, "llalalalalla", Toast.LENGTH_SHORT).show()
            }
        }, object : OnGenericListener<VolleyError> {
            override fun onEvent(arg: VolleyError) {

            }
        })

        val msg : String
        if(err == VaxDataViewModel.LudError.NO_CONNECTIVITY)
            msg = "no connectivity"
        else if(err == VaxDataViewModel.LudError.UPDATE_IN_PROGRESS)
            msg = "update in progress"
        else
            msg = "ok"

        Log.e("luderror", msg)

        binding.button.setOnClickListener {
            Toast.makeText(baseContext, "clicked", Toast.LENGTH_SHORT).show()

            vaxDataViewModel.populateVaxData(
                VaxDataViewModel.VaxData.VAX_DELIVERIES,
                this,
                object : OnGenericListener<VolleyError> {
                    override fun onEvent(arg: VolleyError) {

                    }
                })

            vaxDataViewModel.populateVaxData(
                VaxDataViewModel.VaxData.VAX_INJECTIONS,
                this,
                object : OnGenericListener<VolleyError> {
                    override fun onEvent(arg: VolleyError) {

                    }
                })

            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.PARTS_OF_VAXABLE_POPULATION,
                    this,
                    object : OnGenericListener<VolleyError> {
                        override fun onEvent(arg: VolleyError) {

                        }
                    })

            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.PHYSICAL_INJECTION_LOCATIONS,
                    this,
                    object : OnGenericListener<VolleyError> {
                        override fun onEvent(arg: VolleyError) {

                        }
                    })

            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE,
                    this,
                    object : OnGenericListener<VolleyError> {
                        override fun onEvent(arg: VolleyError) {

                        }
                    })

            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA,
                    this,
                    object : OnGenericListener<VolleyError> {
                        override fun onEvent(arg: VolleyError) {

                        }
                    })

            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.VAX_STATS_SUMMARIES_BY_AREA,
                    this,
                    object : OnGenericListener<VolleyError> {
                        override fun onEvent(arg: VolleyError) {

                        }
                    })
        }
    }
}