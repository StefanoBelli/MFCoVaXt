package it.mobileflow.mfcovaxt.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.mobileflow.mfcovaxt.R
import it.mobileflow.mfcovaxt.entity.PhysicalInjectionLocation

class PhysicalInjectionLocationAdapter(private val physInjs : Array<PhysicalInjectionLocation>) :
    RecyclerView.Adapter<PhysicalInjectionLocationAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var phyInjTv = itemView.findViewById<TextView>(R.id.phy_inj_tv)
        private var areaTv = itemView.findViewById<TextView>(R.id.area_tv)
        private var typeTv = itemView.findViewById<TextView>(R.id.type_tv)

        fun setPhysicalInjectionLocation(d: PhysicalInjectionLocation) {
            phyInjTv.text = d.locationName
            areaTv.text = d.areaName
            typeTv.text = d.type
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_phys_inj_loc, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setPhysicalInjectionLocation(physInjs[position])
    }

    override fun getItemCount(): Int {
        return physInjs.size
    }
}