package com.example.mobileapplicationoku

import android.app.AlertDialog
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
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.mobileapplicationoku.adapter.CaregiverAdapter
import com.example.mobileapplicationoku.dataClass.Approve
import com.example.mobileapplicationoku.dataClass.CareGiver
import com.example.mobileapplicationoku.dataClass.CaregiverApply
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ParttimeDetailsFragment : Fragment() {
    private lateinit var dbref : DatabaseReference
    private lateinit var srref : StorageReference
    private val args: ParttimeDetailsFragmentArgs by navArgs()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_parttime_details, container, false)

        val careID = args.userID
        var userID = ""
        var username = ""
        var userType = ""
        var care = ""

        getCaregiverDetails(careID)

        val auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser = auth.getCurrentUser() as FirebaseUser
        if (currentUser != null) {
            userID = auth.currentUser?.uid.toString()
            dbref = FirebaseDatabase.getInstance().getReference("Users")
            dbref.child(userID).get().addOnSuccessListener {
                username = it.child("fullName").value.toString()
                userType = it.child("type").value.toString()
                care = it.child("care").value.toString()

            }.addOnFailureListener {
                Toast.makeText(requireContext(),"Error", Toast.LENGTH_LONG).show()
            }

        }
        view.findViewById<Button>(R.id.btnDetApply).setOnClickListener() {
            var newID = ""
            var status = ""
            var type = ""
            var applierID = ""
            val date = view.findViewById<TextView>(R.id.tvDetDate).text.toString()
            var temp = false
/*            val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())*/

            dbref = FirebaseDatabase.getInstance().getReference("Message")
            dbref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(dataSnapshot : DataSnapshot in snapshot.children){
                        status = dataSnapshot.child("status").getValue().toString()
                        applierID = dataSnapshot.child("applierUserID").getValue().toString()
                        type = dataSnapshot.child("type").getValue().toString()

                        if(dataSnapshot.exists()){
                            if(applierID == userID){
                                if(type == "parttime"){
                                    if(status == "pending"){
                                        temp = true
                                    }
                                }

                            }
                        }

                    }
                    if(temp == false){
                        if(userType == "Caregiver"){
                            if(care != ""){
                                dbref = FirebaseDatabase.getInstance().getReference("Message")
                                dbref.addListenerForSingleValueEvent(object: ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if(snapshot.exists()){
                                            newID="C"+ "%04d".format(snapshot.childrenCount + 1)
                                        }else{
                                            newID="C0001"
                                        }
                                        val save = CaregiverApply(newID,careID,userID,username,"pending",date,"","parttime","","")
                                        dbref.child(newID).setValue(save).addOnSuccessListener() {
                                            Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                                        }.addOnFailureListener {
                                            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }else{
                                Toast.makeText(requireContext(),"You must have care people inserted", Toast.LENGTH_LONG).show()
                            }
                        }else{
                            dbref = FirebaseDatabase.getInstance().getReference("Message")
                            dbref.addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.exists()){
                                        newID="C"+ "%04d".format(snapshot.childrenCount + 1)
                                    }else{
                                        newID="C0001"
                                    }
                                    val save = CaregiverApply(newID,careID,userID,username,"pending",date,"","parttime","","")
                                    dbref.child(newID).setValue(save).addOnSuccessListener() {
                                        Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                                    }.addOnFailureListener {
                                        Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                                }
                            })


                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })



        }

        return view
    }

    private fun getCaregiverDetails(careID :String){

        if(careID != ""){
            srref = FirebaseStorage.getInstance().reference.child("UserProfilePic/"+careID)
            var localfile = File.createTempFile("tempImage","jpg")
            srref.getFile(localfile).addOnSuccessListener {
                var bitmap = BitmapFactory.decodeFile(localfile.absolutePath)

                view?.findViewById<ImageView>(R.id.imgDetProfile)?.setImageBitmap(bitmap)
            }
            dbref = FirebaseDatabase.getInstance().getReference("Parttime")
            dbref.child(careID).get().addOnSuccessListener {
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