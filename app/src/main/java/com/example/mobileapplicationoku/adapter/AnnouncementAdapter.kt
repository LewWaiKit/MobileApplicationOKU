package com.example.mobileapplicationoku.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.R
import com.example.mobileapplicationoku.dataClass.Announcement
import com.example.mobileapplicationoku.databinding.AnnouncementItemBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class AnnouncementAdapter(private val announcementList:ArrayList<Announcement>, val clickListener: ViewListener) : RecyclerView.Adapter<AnnouncementAdapter.myViewHolder>() {

    private lateinit var srref : StorageReference

    class myViewHolder private constructor(val binding: AnnouncementItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: Announcement, clickListener: ViewListener){
            binding.announcement = item
            binding.executePendingBindings()
            binding.clickListener = clickListener
        }
        companion object{
            fun from(parent: ViewGroup): myViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AnnouncementItemBinding.inflate(layoutInflater, parent, false)
                return myViewHolder(binding)
            }
        }

        val title: TextView = binding.announceTitlle
        val date: TextView = binding.announceDate
        val desc: TextView = binding.announceDesc
        val img : ImageView = binding.ivAnnouncement

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.announcement_item, parent, false)
        return myViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val currentAnnouncement= announcementList [position]
        holder.title.text = currentAnnouncement.title
        holder.date.text = currentAnnouncement.date
        holder.desc.text = currentAnnouncement.description

        srref = FirebaseStorage.getInstance().reference.child("Announcement/"+currentAnnouncement.announcementID)
        val localfile = File.createTempFile("tempImage","jpg")
        srref.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            holder.img.setImageBitmap(bitmap)
        }.addOnFailureListener(){

        }
        holder.bind(currentAnnouncement!!, clickListener)
    }
    override fun getItemCount(): Int {
        return announcementList.size
    }

    class ViewListener(val clickListener: (announcementID: String) -> Unit) {
        fun onClick(announcement: Announcement) = clickListener(announcement.announcementID.toString())
    }
}