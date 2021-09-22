package com.example.mobileapplicationoku.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.R
import com.example.mobileapplicationoku.databinding.EventlistBinding
import com.example.mobileapplicationoku.dataClass.EventData

class MyAdapter(private val eventListShow: List<EventData>, val clickListener: Viewlistener) :RecyclerView.Adapter<MyAdapter.myViewHolder>(){

    class myViewHolder private constructor(val binding: EventlistBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: EventData, clickListener: Viewlistener){

            binding.eventData = item
            binding.executePendingBindings()
            binding.clickListener = clickListener
        }
        
        companion object{
            fun from(parent: ViewGroup): myViewHolder{
             val layoutInflater = LayoutInflater.from((parent.context))
             val binding = EventlistBinding.inflate(layoutInflater, parent, false)
             return myViewHolder(binding)
            }
        }

        val eventName: TextView = binding.tvEventName
        //val eventDestination: TextView = binding.tvEventDestination

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.eventlist, parent, false)

        return MyAdapter.myViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {

        val currentEvent = eventListShow[position]

        holder.eventName.text = currentEvent.name
        //holder.eventDestination.text = currentEvent.destination

        holder.bind(currentEvent!!, clickListener)
    }

    override fun getItemCount(): Int {

        return eventListShow.size
    }

    class Viewlistener(val clickListener: (ID: String) -> Unit){

        fun onClick(eventData: EventData)= clickListener(eventData.ID.toString())
    }
}