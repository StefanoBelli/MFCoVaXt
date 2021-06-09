package it.mobileflow.mfcovaxt.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.room.Room
import com.android.volley.VolleyError
import it.mobileflow.mfcovaxt.database.VaxInjectionsStatsDatabase
import it.mobileflow.mfcovaxt.databinding.ActivityMainBinding
import it.mobileflow.mfcovaxt.listener.OnGenericListener
import it.mobileflow.mfcovaxt.viewmodel.VaxDataViewModel
import javax.xml.transform.ErrorListener

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
        vaxDataViewModel.lastUpdateDataset(this, object : OnGenericListener<Boolean> {
            override fun onEvent(arg: Boolean) {
                Toast.makeText(baseContext, "llalalalalla", Toast.LENGTH_SHORT).show()
            }
        }, object : OnGenericListener<VolleyError> {
            override fun onEvent(arg: VolleyError) {

            }
        })

        binding.button.setOnClickListener {

            Toast.makeText(baseContext, "clicked", Toast.LENGTH_SHORT).show()
            vaxDataViewModel.populateVaxData(
                VaxDataViewModel.VaxData.VAX_DELIVERIES,
                this,
                object : OnGenericListener<VolleyError> {
                    override fun onEvent(arg: VolleyError) {

                    }
                })
        }
    }
}