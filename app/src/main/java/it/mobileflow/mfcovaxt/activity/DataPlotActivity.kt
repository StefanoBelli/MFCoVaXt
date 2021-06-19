package it.mobileflow.mfcovaxt.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.mobileflow.mfcovaxt.databinding.ActivityDataPlotBinding

class DataPlotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataPlotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataPlotBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}