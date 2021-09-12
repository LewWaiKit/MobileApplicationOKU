package com.example.mobileapplicationoku.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.R
import com.example.mobileapplicationoku.dataClass.Facilities
import com.example.mobileapplicationoku.databinding.FacilitiesItemBinding

class FacilitiesAdapter(private val facilitiesList:ArrayList<Facilities>, val clickListener: ViewListener) : RecyclerView.Adapter<FacilitiesAdapter.myViewHolder>() {

    class myViewHolder private constructor(val binding: FacilitiesItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: Facilities, clickListener: ViewListener){
            binding.facilities = item
            binding.executePendingBindings()
            binding.clickListener = clickListener
        }
        companion object{
            fun from(parent: ViewGroup): myViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FacilitiesItemBinding.inflate(layoutInflater, parent, false)
                return myViewHolder(binding)
            }
        }

        val facilitiesName: TextView = binding.tvFacilitiesName
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.facilities_item, parent, false)
        return myViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val currentFacilities= facilitiesList [position]
        holder.facilitiesName.text = currentFacilities.locationName
        holder.bind(currentFacilities!!, clickListener)
    }
    override fun getItemCount(): Int {
        return facilitiesList.size
    }

    class ViewListener(val clickListener: (facilityID: String) -> Unit) {
        fun onClick(facilities: Facilities) = clickListener(facilities.facilityID.toString())
    }
}