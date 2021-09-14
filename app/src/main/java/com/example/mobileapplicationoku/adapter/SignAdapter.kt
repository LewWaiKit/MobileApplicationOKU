package com.example.mobileapplicationoku.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.R
import com.example.mobileapplicationoku.dataClass.Handsign


class SignAdapter(private val sign: List<Handsign>) :RecyclerView.Adapter<SignAdapter.myViewHolder>() {


    class myViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val handsignName: TextView = itemView.findViewById(R.id.tvName)
        val handsignImg: ImageView = itemView.findViewById(R.id.imgProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.handsign_item, parent, false
        )
        return myViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val currentsign = sign[position]

        holder.handsignName.text = currentsign.name
        holder.handsignImg.setImageBitmap(currentsign.img)
    }

    override fun getItemCount(): Int {
        return sign.size

    }


}