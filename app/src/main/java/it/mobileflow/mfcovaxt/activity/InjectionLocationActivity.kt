package it.mobileflow.mfcovaxt.activity

import android.content.DialogInterface
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import it.mobileflow.mfcovaxt.R
import it.mobileflow.mfcovaxt.adapter.PhysicalInjectionLocationAdapter
import it.mobileflow.mfcovaxt.databinding.ActivityInjectionLocationBinding
import it.mobileflow.mfcovaxt.holder.DataHolder

class InjectionLocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInjectionLocationBinding
    private var currentFiltering = 0
    private val spinnerItemClick = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            currentFiltering = position
            supportActionBar!!.title = getString(R.string.inj_locations)
            if(currentFiltering > 0) {
                val areaName = (view as TextView).text
                supportActionBar!!.title = "${supportActionBar!!.title} [${areaName}]"
                binding.rv.adapter = PhysicalInjectionLocationAdapter(
                    DataHolder.physicalInjectionLocations.filter {
                        it.areaName == areaName.toString()
                    }.toTypedArray())
            } else {
                binding.rv.adapter = PhysicalInjectionLocationAdapter(
                    DataHolder.physicalInjectionLocations)
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInjectionLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.adapter = PhysicalInjectionLocationAdapter(DataHolder.physicalInjectionLocations)
        setSupportActionBar(binding.tb)
        supportActionBar!!.title = getString(R.string.inj_locations)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_filter, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val scale = resources.displayMetrics.density
        val tenDp = (20 * scale + 0.5f).toInt()

        val ll = LinearLayout(this)

        val tv = TextView(this)
        tv.text = getString(R.string.filter_message)
        tv.setPadding(tenDp, tenDp, tenDp, 0)

        val sp = Spinner(this)
        sp.setPadding(tenDp, tenDp, tenDp, tenDp)
        val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.areas))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp.adapter = adapter
        sp.setSelection(currentFiltering)
        sp.onItemSelectedListener = spinnerItemClick

        ll.orientation = LinearLayout.VERTICAL
        ll.addView(tv)
        ll.addView(sp)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.filter_by_area))
        builder.setView(ll)
        builder.setCancelable(false)
        builder.setNeutralButton(R.string.ok)
        { d: DialogInterface, _: Int -> d.dismiss() }
        builder.create().show()

        return super.onOptionsItemSelected(item)
    }
}