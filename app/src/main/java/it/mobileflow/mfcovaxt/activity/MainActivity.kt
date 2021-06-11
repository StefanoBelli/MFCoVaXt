package it.mobileflow.mfcovaxt.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import it.mobileflow.mfcovaxt.database.VaxInjectionsStatsDatabase
import it.mobileflow.mfcovaxt.databinding.ActivityMainBinding
import it.mobileflow.mfcovaxt.factory.VaxDataViewModelFactory
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel

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

        vaxDataViewModel.db = db

        /*
        val err = vaxDataViewModel.lastUpdateDataset(this, {}, {})

        binding.button.setOnClickListener {
            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.VAX_DELIVERIES,
                    this, {})

            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.VAX_INJECTIONS,
                    this, {})

            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.PARTS_OF_VAXABLE_POPULATION,
                    this, {})

            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.PHYSICAL_INJECTION_LOCATIONS,
                    this, {})

            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.VAX_INJECTIONS_SUMMARIES_BY_AGE_RANGE,
                    this, {})

            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.VAX_INJECTIONS_SUMMARIES_BY_DAY_AND_AREA,
                    this, {})

            vaxDataViewModel.populateVaxData(
                    VaxDataViewModel.VaxData.VAX_STATS_SUMMARIES_BY_AREA,
                    this, {})
        }*/
    }
}