package com.example.mobileapplicationoku

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.StrictMode
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.mobileapplicationoku.databinding.FragmentEventBinding
import com.example.mobileapplicationoku.databinding.FragmentEventDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.net.URL

class eventDetail : Fragment() {

    private lateinit var dataRef : DatabaseReference
    private lateinit var stoRef : StorageReference
    private var v_binding: FragmentEventDetailBinding? =null
    private val binding get() = v_binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        v_binding = FragmentEventDetailBinding.inflate(inflater, container, false)

        val args = eventDetailArgs.fromBundle(requireArguments())

        val eventID = args.ID
        val eventName = ""
        val eventLocation = ""
        val eventDate = ""
        val eventTime = ""

        getEventDetail(eventID)

        binding.btnRegisterEvent.setOnClickListener(){
            Navigation.findNavController(it).navigate(eventDetailDirections.actionEventDetailToRegisterEvent())
        }

        return binding.root
    }

    private fun getEventDetail(eventID : String){

        if(eventID != "") {
            stoRef = FirebaseStorage.getInstance().reference.child("GovEventImage/"+eventID)
            var localFileImage = File.createTempFile("tempImage", "jpg")

            stoRef.getFile(localFileImage).addOnSuccessListener {

                var bitmap = BitmapFactory.decodeFile(localFileImage.absolutePath)
                view?.findViewById<ImageView>(R.id.imageEvent)?.setImageBitmap(bitmap)
            }

            dataRef = FirebaseDatabase.getInstance().getReference("Event/GovEvent")
            dataRef.child(eventID).get().addOnSuccessListener {

                //val ID = it.child("ID").value.toString()
                val date = it.child("date").value.toString()
                val location = it.child("location").value.toString()
                val name = it.child("name").value.toString()
                val time = it.child("time").value.toString()

                val image = it.child("image").value.toString()
                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val url = URL(image)
                val img = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                view?.findViewById<ImageView>(R.id.imageEvent)?.setImageBitmap(img)

                view?.findViewById<TextView>(R.id.tvEventName)?.text = name
                view?.findViewById<TextView>(R.id.tvEventLocation)?.text = location
                view?.findViewById<TextView>(R.id.tvEventDate)?.text = date
                view?.findViewById<TextView>(R.id.tvEventTime)?.text = time

            }.addOnFailureListener {
                Toast.makeText(context, "Fail to get the event details", Toast.LENGTH_LONG).show()
            }
        }else {
            Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
        }
    }

}