package com.example.mobileapplicationoku.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.R
import com.example.mobileapplicationoku.dataClass.CaregiverApply
import com.example.mobileapplicationoku.databinding.MessageItemBinding

class MessageAdapter (private val message: List<CaregiverApply>, val clickListener: MessageAdapter.ViewListener) : RecyclerView.Adapter<MessageAdapter.myViewHolder>() {
    class myViewHolder private constructor(val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: CaregiverApply, clickListener: MessageAdapter.ViewListener){

            binding.message = item
            binding.executePendingBindings()
            binding.clickListener = clickListener
        }
        companion object{
            fun from(parent: ViewGroup): myViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MessageItemBinding.inflate(layoutInflater, parent, false)
                return myViewHolder(binding)
            }
        }

        val messageTitle: TextView = binding.tvTitle
        val messageContain: TextView = binding.tvMessage

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.message_item, parent, false)
        return MessageAdapter.myViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return message.size
    }

    class ViewListener(val clickListener: (messageID: String) -> Unit) {
        fun onClick(message: CaregiverApply) = clickListener(message.messageID.toString())
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val currentMessage= message [position]

        holder.messageTitle.text = currentMessage.applierName
        holder.messageContain.text = "Request for "+currentMessage.type +" on " +currentMessage.date
        holder.bind(currentMessage!!, clickListener)
    }


}