package com.example.mobileapplicationoku.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.R
import com.example.mobileapplicationoku.dataClass.Approve
import com.example.mobileapplicationoku.databinding.ApproveItemBinding

class ApproveAdapter(private val approveList:ArrayList<Approve>, val clickListener: ViewListener) : RecyclerView.Adapter<ApproveAdapter.myViewHolder>() {

    class myViewHolder private constructor(val binding: ApproveItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: Approve, clickListener: ViewListener){
            binding.approve = item
            binding.executePendingBindings()
            binding.clickListener = clickListener
        }
        companion object{
            fun from(parent: ViewGroup): myViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ApproveItemBinding.inflate(layoutInflater, parent, false)
                return myViewHolder(binding)
            }
        }

        val fullName: TextView = binding.tvFullName
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.approve_item, parent, false)
        return myViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val currentApprove= approveList [position]
        holder.fullName.text = currentApprove.fullName
        holder.bind(currentApprove!!, clickListener)
    }
    override fun getItemCount(): Int {
        return approveList.size
    }

    class ViewListener(val clickListener: (approveID: String) -> Unit) {
        fun onClick(approve: Approve) = clickListener(approve.approveID.toString())
    }
}