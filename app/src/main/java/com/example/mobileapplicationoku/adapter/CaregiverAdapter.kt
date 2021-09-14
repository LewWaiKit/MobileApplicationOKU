package com.example.mobileapplicationoku.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.R
import com.example.mobileapplicationoku.dataClass.CareGiver
import com.example.mobileapplicationoku.databinding.CaregiverItemBinding

class CaregiverAdapter(private val caregiver: List<CareGiver>, val clickListener: CaregiverAdapter.ViewListener) : RecyclerView.Adapter<CaregiverAdapter.myViewHolder>() {

    class myViewHolder private constructor(val binding: CaregiverItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: CareGiver, clickListener: CaregiverAdapter.ViewListener){

            binding.caregiver = item
            binding.executePendingBindings()
            binding.clickListener = clickListener
        }
        companion object{
            fun from(parent: ViewGroup): myViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CaregiverItemBinding.inflate(layoutInflater, parent, false)
                return myViewHolder(binding)
            }
        }

        val caregiverName: TextView = binding.tvName
        val caregiverDesc: TextView = binding.tvDesc
        val caregiverProfile: ImageView = binding.imgProfile
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaregiverAdapter.myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.caregiver_item, parent, false)
        return CaregiverAdapter.myViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: CaregiverAdapter.myViewHolder, position: Int) {
        val currentCaregiver= caregiver [position]
        holder.caregiverName.text = currentCaregiver.name
        holder.caregiverDesc.text = currentCaregiver.desc
        holder.caregiverProfile.setImageBitmap(currentCaregiver.profile)
        holder.bind(currentCaregiver!!, clickListener)
    }
    override fun getItemCount(): Int {
        return caregiver.size
    }

    class ViewListener(val clickListener: (userID: String) -> Unit) {
        fun onClick(caregiver: CareGiver) = clickListener(caregiver.userID.toString())
    }



}