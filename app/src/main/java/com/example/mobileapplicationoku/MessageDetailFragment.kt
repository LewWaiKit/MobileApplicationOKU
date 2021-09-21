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

class MessageDetailFragment : Fragment() {
    private lateinit var srref : StorageReference
    private lateinit var dbref : DatabaseReference
    private var userID = ""
    private val args: MessageDetailFragmentArgs by navArgs()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_message_details, container, false)

        val messageID = args.messageID
        var applierID = ""
        var appliername = ""
        var careID = ""
        var status = ""
        var status_ = ""

        val auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser = auth.getCurrentUser() as FirebaseUser
        if (currentUser != null) {
            userID = auth.currentUser?.uid.toString()
        }

        if(messageID != ""){

            dbref = FirebaseDatabase.getInstance().getReference("Message")
            dbref.child(messageID).get().addOnSuccessListener {
                applierID = it.child("applierUserID").value.toString()
                appliername = it.child("applierName").value.toString()
                careID = it.child("careUserID").value.toString()
                status = it.child("status").value.toString()


                if(status == "pending"){
                    view.findViewById<Button>(R.id.btnMessAccept).visibility = View.VISIBLE
                    view.findViewById<Button>(R.id.btnMessReject).visibility = View.VISIBLE

                    srref = FirebaseStorage.getInstance().reference.child("UserProfilePic/"+applierID)
                    var localfile = File.createTempFile("tempImage","jpg")
                    srref.getFile(localfile).addOnSuccessListener {
                        var bitmap = BitmapFactory.decodeFile(localfile.absolutePath)

                        view.findViewById<ImageView>(R.id.imgMessProfile).setImageBitmap(bitmap)
                        view.findViewById<TextView>(R.id.tvMessTitle).text = appliername

                        dbref = FirebaseDatabase.getInstance().getReference("Parttime")
                        dbref.child(userID).get().addOnSuccessListener {
                            val jobDate = it.child("date").value.toString()
                            status_ = it.child("status").value.toString()

                            view.findViewById<TextView>(R.id.tvMessContain).text = appliername + " would like to hire you as their part-time caregiver on " + jobDate
                        }.addOnFailureListener {
                            Toast.makeText(requireContext(),"Failed to retrieve details", Toast.LENGTH_LONG).show()
                        }

                        dbref = FirebaseDatabase.getInstance().getReference("Message")
                        view.findViewById<Button>(R.id.btnMessAccept).setOnClickListener {
                            if(status_ != "booked"){
                                val stat = mapOf<String,String>("status" to "accepted")
                                dbref.child(messageID).updateChildren(stat).addOnSuccessListener {
                                    Toast.makeText(requireContext(),"Accepted", Toast.LENGTH_SHORT).show()
                                    dbref = FirebaseDatabase.getInstance().getReference("Parttime")
                                    val stat = mapOf<String,String>("status" to "booked")
                                    dbref.child(userID).updateChildren(stat).addOnSuccessListener {

                                    }
                                }
                            }else{
                                Toast.makeText(requireContext(),"You can only accept 1 request at a time", Toast.LENGTH_LONG).show()
                            }

                        }

                        view.findViewById<Button>(R.id.btnMessReject).setOnClickListener {
                            val stat = mapOf<String,String>("status" to "rejected")
                            dbref.child(messageID).updateChildren(stat).addOnSuccessListener {
                                Toast.makeText(requireContext(),"Rejected", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }else{
                    view.findViewById<Button>(R.id.btnMessAccept).visibility = View.GONE
                    view.findViewById<Button>(R.id.btnMessReject).visibility = View.GONE

                    var name = ""
                    var jobDate = ""

                    dbref = FirebaseDatabase.getInstance().getReference("Parttime")
                    dbref.child(careID).get().addOnSuccessListener {
                        jobDate = it.child("date").value.toString()
                        name = it.child("fullName").value.toString()

                        view.findViewById<TextView>(R.id.tvMessContain).text = name + " has " + status + " your request on "+jobDate
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(),"Failed to retrieve details", Toast.LENGTH_LONG).show()
                    }

                    srref = FirebaseStorage.getInstance().reference.child("UserProfilePic/"+careID)
                    var localfile = File.createTempFile("tempImage","jpg")
                    srref.getFile(localfile).addOnSuccessListener {
                        var bitmap = BitmapFactory.decodeFile(localfile.absolutePath)

                        view.findViewById<ImageView>(R.id.imgMessProfile).setImageBitmap(bitmap)
                        view.findViewById<TextView>(R.id.tvMessTitle).text = name

                }

            }


        }.addOnFailureListener {
                Toast.makeText(requireContext(),"Failed to retrieve details", Toast.LENGTH_LONG).show()
            }

    }


        return view

    }
}
