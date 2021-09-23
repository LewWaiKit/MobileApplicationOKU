package com.example.mobileapplicationoku

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
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
import com.example.mobileapplicationoku.adapter.MessageAdapter
import com.example.mobileapplicationoku.dataClass.Approve
import com.example.mobileapplicationoku.dataClass.CaregiverApply
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.util.ArrayList

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
        var type = ""
        var from = ""
        var to = ""
        var time = ""
        var date = ""
        var phoneNo = ""

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
                to = it.child("to").value.toString()
                from = it.child("from").value.toString()
                date = it.child("date").value.toString()
                time = it.child("time").value.toString()
                careID = it.child("careUserID").value.toString()
                status = it.child("status").value.toString()
                type = it.child("type").value.toString()



                if(type == "parttime"){
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
                                dbref = FirebaseDatabase.getInstance().getReference("Users")
                                dbref.child(applierID).get().addOnSuccessListener {
                                    phoneNo = it.child("contactNo").value.toString()

                                    view.findViewById<TextView>(R.id.tvMessContain2).text = phoneNo

                                    view.findViewById<TextView>(R.id.tvMessContain2).setOnClickListener {
                                        val intent = Intent(Intent.ACTION_DIAL);
                                        intent.data = Uri.parse("tel:$phoneNo")
                                        startActivity(intent)
                                    }
                                }
                            }.addOnFailureListener {
                                Toast.makeText(context,"Failed to retrieve details", Toast.LENGTH_LONG).show()
                            }

                            dbref = FirebaseDatabase.getInstance().getReference("Message")
                            view.findViewById<Button>(R.id.btnMessAccept).setOnClickListener {
                                if(status_ != "booked"){
                                    val stat = mapOf<String,String>("status" to "accepted")
                                    dbref.child(messageID).updateChildren(stat).addOnSuccessListener {
                                        Toast.makeText(context,"Accepted", Toast.LENGTH_SHORT).show()
                                        dbref = FirebaseDatabase.getInstance().getReference("Parttime")
                                        val stat = mapOf<String,String>("status" to "booked")
                                        dbref.child(userID).updateChildren(stat).addOnSuccessListener {

                                        }
                                    }
                                    Navigation.findNavController(it).navigate(MessageDetailFragmentDirections.actionMessageDetailFragmentToMessageFragment())
                                }else{
                                    Toast.makeText(context,"You can only accept 1 request at a time", Toast.LENGTH_LONG).show()
                                }

                            }

                            view.findViewById<Button>(R.id.btnMessReject).setOnClickListener {
                                val stat = mapOf<String,String>("status" to "rejected")
                                dbref.child(messageID).updateChildren(stat).addOnSuccessListener {
                                    Toast.makeText(context,"Rejected", Toast.LENGTH_SHORT).show()
                                }
                                Navigation.findNavController(it).navigate(MessageDetailFragmentDirections.actionMessageDetailFragmentToMessageFragment())
                            }
                        }
                    }else if(status == "accepted" || status == "rejected"){
                        view.findViewById<Button>(R.id.btnMessAccept).visibility = View.GONE
                        view.findViewById<Button>(R.id.btnMessReject).visibility = View.GONE

                        var name = ""
                        var jobDate = ""

                        dbref = FirebaseDatabase.getInstance().getReference("Parttime")
                        dbref.child(careID).get().addOnSuccessListener {
                            jobDate = it.child("date").value.toString()
                            name = it.child("fullName").value.toString()
                            phoneNo = it.child("contactNo").value.toString()

                            view.findViewById<TextView>(R.id.tvMessContain).text = name + " has " + status + " your request on "+jobDate
                            if(status == "accepted"){
                                view.findViewById<TextView>(R.id.tvMessContain2).text = phoneNo

                                view.findViewById<TextView>(R.id.tvMessContain2).setOnClickListener {
                                    val intent = Intent(Intent.ACTION_DIAL);
                                    intent.data = Uri.parse("tel:$phoneNo")
                                    startActivity(intent)
                                }
                            }
                        }.addOnFailureListener {
                            Toast.makeText(context,"Failed to retrieve details", Toast.LENGTH_LONG).show()
                        }

                        srref = FirebaseStorage.getInstance().reference.child("UserProfilePic/"+careID)
                        var localfile = File.createTempFile("tempImage","jpg")
                        srref.getFile(localfile).addOnSuccessListener {
                            var bitmap = BitmapFactory.decodeFile(localfile.absolutePath)

                            view.findViewById<ImageView>(R.id.imgMessProfile).setImageBitmap(bitmap)
                            view.findViewById<TextView>(R.id.tvMessTitle).text = name

                        }

                    }
                }else{
                    if(status == "pending"){
                        view.findViewById<Button>(R.id.btnMessAccept).visibility = View.VISIBLE
                        view.findViewById<Button>(R.id.btnMessReject).visibility = View.VISIBLE

                        srref = FirebaseStorage.getInstance().reference.child("UserProfilePic/"+applierID)
                        var localfile = File.createTempFile("tempImage","jpg")
                        srref.getFile(localfile).addOnSuccessListener {
                            var bitmap = BitmapFactory.decodeFile(localfile.absolutePath)

                            view.findViewById<ImageView>(R.id.imgMessProfile).setImageBitmap(bitmap)
                            view.findViewById<TextView>(R.id.tvMessTitle).text = appliername

                            view.findViewById<TextView>(R.id.tvMessContain).text = appliername + " would like to hire you for the transportation on " +to+" from "+from+ " on "+date+" on "+time
                            dbref = FirebaseDatabase.getInstance().getReference("Users")
                            dbref.child(applierID).get().addOnSuccessListener {
                                phoneNo = it.child("contactNo").value.toString()

                                    view.findViewById<TextView>(R.id.tvMessContain2).text = phoneNo

                                    view.findViewById<TextView>(R.id.tvMessContain2).setOnClickListener {
                                        val intent = Intent(Intent.ACTION_DIAL);
                                        intent.data = Uri.parse("tel:$phoneNo")
                                        startActivity(intent)
                                    }
                                }

                            dbref = FirebaseDatabase.getInstance().getReference("Message")
                            view.findViewById<Button>(R.id.btnMessAccept).setOnClickListener {
                                var tempList = ArrayList<CaregiverApply>()
                                    val stat = mapOf<String,String>("status" to "accepted")
                                    dbref.child(messageID).updateChildren(stat).addOnSuccessListener {
                                        Toast.makeText(context,"Accepted", Toast.LENGTH_SHORT).show()

                                        dbref = FirebaseDatabase.getInstance().getReference("Message")
                                        dbref.addValueEventListener(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                for (dataSnapshot: DataSnapshot in snapshot.children) {
                                                    var message = dataSnapshot.getValue(CaregiverApply::class.java)

                                                    if(message?.type == "transport"){
                                                        tempList.add(message!!)
                                                        tempList= tempList.filter{ r -> r.applierUserID == applierID} as ArrayList<CaregiverApply>
                                                        tempList= tempList.filter { r -> r.date == date } as ArrayList<CaregiverApply>
                                                        tempList= tempList.filter { r -> r.time == time } as ArrayList<CaregiverApply>
                                                        tempList= tempList.filter { r -> r.status == "pending" } as ArrayList<CaregiverApply>

                                                    }
                                                }
                                                for(i in 0..tempList.size-1){
                                                    var temp = tempList[0].messageID.toString()
                                                    val stat = mapOf<String,String>("status" to "expired")
                                                    dbref.child(temp).updateChildren(stat).addOnSuccessListener {

                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {

                                            }
                                        })
                                    }
                                Navigation.findNavController(it).navigate(MessageDetailFragmentDirections.actionMessageDetailFragmentToMessageFragment())

                            }

                            view.findViewById<Button>(R.id.btnMessReject).setOnClickListener {
                                val stat = mapOf<String,String>("status" to "rejected")
                                dbref.child(messageID).updateChildren(stat).addOnSuccessListener {
                                    Toast.makeText(context,"Rejected", Toast.LENGTH_SHORT).show()
                                }
                                Navigation.findNavController(it).navigate(MessageDetailFragmentDirections.actionMessageDetailFragmentToMessageFragment())
                            }
                        }
                    }else if (status == "accepted" || status == "rejected"){
                        view.findViewById<Button>(R.id.btnMessAccept).visibility = View.GONE
                        view.findViewById<Button>(R.id.btnMessReject).visibility = View.GONE

                        var name = ""

                        dbref = FirebaseDatabase.getInstance().getReference("Users")
                        dbref.child(careID).get().addOnSuccessListener {
                            name = it.child("fullName").value.toString()
                            phoneNo = it.child("contactNo").value.toString()

                            view.findViewById<TextView>(R.id.tvMessContain).text = name + " has " + status + " your transport request to " +to+" from "+from+ " on "+date+" in "+time

                            if(status == "accepted"){
                                view.findViewById<TextView>(R.id.tvMessContain2).text = phoneNo

                                view.findViewById<TextView>(R.id.tvMessContain2).setOnClickListener {
                                    val intent = Intent(Intent.ACTION_DIAL);
                                    intent.data = Uri.parse("tel:$phoneNo")
                                    startActivity(intent)
                                }
                            }


                        }.addOnFailureListener {
                            Toast.makeText(context,"Failed to retrieve details", Toast.LENGTH_LONG).show()
                        }

                        srref = FirebaseStorage.getInstance().reference.child("UserProfilePic/"+careID)
                        var localfile = File.createTempFile("tempImage","jpg")
                        srref.getFile(localfile).addOnSuccessListener {
                            var bitmap = BitmapFactory.decodeFile(localfile.absolutePath)

                            view.findViewById<ImageView>(R.id.imgMessProfile).setImageBitmap(bitmap)
                            view.findViewById<TextView>(R.id.tvMessTitle).text = name

                        }

                    }
                }



        }.addOnFailureListener {
                Toast.makeText(context,"Failed to retrieve details", Toast.LENGTH_LONG).show()
            }

    }


        return view

    }
}
