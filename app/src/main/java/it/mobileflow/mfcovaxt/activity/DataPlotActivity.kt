package it.mobileflow.mfcovaxt.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import it.mobileflow.mfcovaxt.databinding.ActivityDataPlotBinding
import it.mobileflow.mfcovaxt.factory.VaxDataViewModelFactory
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel

class DataPlotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataPlotBinding
    private lateinit var vaxDataViewModel: VaxDataViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataPlotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vaxDataViewModel = ViewModelProvider(this, VaxDataViewModelFactory.getInstance())
                .get(VaxDataViewModel::class.java)
    }
}