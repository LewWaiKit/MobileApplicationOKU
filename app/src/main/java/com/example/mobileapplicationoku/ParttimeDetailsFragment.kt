package com.example.mobileapplicationoku

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class ParttimeDetailsFragment : Fragment() {
    private lateinit var dbref : DatabaseReference
    private lateinit var srref : StorageReference
    private val args: ParttimeDetailsFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_parttime_details, container, false)

        var user = ""
        var userType = ""
        var care = ""

        getCaregiverDetails()

        val auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser = auth.getCurrentUser() as FirebaseUser
        if (currentUser != null) {
            val userID = auth.currentUser?.uid.toString()
            dbref = FirebaseDatabase.getInstance().getReference("Users")
            dbref.child(userID).get().addOnSuccessListener {
                user = it.child("userID").value.toString()
                userType = it.child("type").value.toString()
                care = it.child("care").value.toString()

            }.addOnFailureListener {
                Toast.makeText(context,"Error", Toast.LENGTH_LONG).show()
            }

        }
        view.findViewById<Button>(R.id.btnDetApply).setOnClickListener() {

            if(userType == "Caregiver"){
                if(care != ""){

                }else{
                    Toast.makeText(context,"You must have care people inserted", Toast.LENGTH_LONG).show()
                }
            }else{

            }
        }

        return view
    }

    private fun getCaregiverDetails(){
        val userID = args.userID

        if(userID != ""){
            srref = FirebaseStorage.getInstance().reference.child("UserProfilePic/"+userID)
            var localfile = File.createTempFile("tempImage","jpg")
            srref.getFile(localfile).addOnSuccessListener {
                var bitmap = BitmapFactory.decodeFile(localfile.absolutePath)

                view?.findViewById<ImageView>(R.id.imgDetProfile)?.setImageBitmap(bitmap)
            }
            dbref = FirebaseDatabase.getInstance().getReference("Parttime")
            dbref.child(userID).get().addOnSuccessListener {
                val contactNo = it.child("contactNo").value.toString()
                val date = it.child("date").value.toString()
                val desc = it.child("desc").value.toString()
                val gender = it.child("gender").value.toString()
                val name = it.child("fullName").value.toString()


                view?.findViewById<TextView>(R.id.tvDetName)?.text = name
                view?.findViewById<TextView>(R.id.tvDetDesc)?.text = desc
                view?.findViewById<TextView>(R.id.tvDetContact)?.text = contactNo
                view?.findViewById<TextView>(R.id.tvDetGender)?.text = gender
                view?.findViewById<TextView>(R.id.tvDetDate)?.text = date

            }.addOnFailureListener {
                Toast.makeText(context,"Failed to retrieve details", Toast.LENGTH_LONG).show()
            }
        }

    }
}