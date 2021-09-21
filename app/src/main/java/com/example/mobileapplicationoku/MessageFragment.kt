package com.example.mobileapplicationoku

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.adapter.CaregiverAdapter
import com.example.mobileapplicationoku.adapter.MessageAdapter
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

class MessageFragment : Fragment() {
    private var messageList = mutableListOf<CaregiverApply>()
    private lateinit var dbref : DatabaseReference
    private var userID = ""
    private var parttimeDate = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification_message, container, false)

        val recyclerView :RecyclerView = view.findViewById<RecyclerView>(R.id.recyclerMessage)
        val auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser = auth.getCurrentUser() as FirebaseUser
        if (currentUser != null) {
            userID = auth.currentUser?.uid.toString()
        }

        dbref = FirebaseDatabase.getInstance().getReference("Users")
        dbref.child(userID).get().addOnSuccessListener {
            val userType = it.child("type").getValue().toString()

            if (userType == "Caregiver"){
                dbref = FirebaseDatabase.getInstance().getReference("Parttime")
                dbref.child(userID).get().addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        parttimeDate = task.result.child("date").value.toString()
/*                        parttimeDate = it.child("date").value.toString()*/
                        getMessage("Parttimer", recyclerView)
                    }else{
                        getMessage("Not", recyclerView)
                    }
                }
            }else{
                getMessage("Careperson", recyclerView)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
        }




        return view
    }

    private fun getMessage(str :String, recyclerView :RecyclerView){

        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        if(messageList != null){

            messageList.clear()
        }

        dbref = FirebaseDatabase.getInstance().getReference("Message")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    var messageID = dataSnapshot.child("messageID").getValue().toString()
                    var careUserID = dataSnapshot.child("careUserID").getValue().toString()
                    var applierUserID = dataSnapshot.child("applierUserID").getValue().toString()
                    var name = dataSnapshot.child("applierName").getValue().toString()
                    var status = dataSnapshot.child("status").getValue().toString()
                    var date = dataSnapshot.child("date").getValue().toString()
                    var type = dataSnapshot.child("type").getValue().toString()
                    var time = dataSnapshot.child("time").getValue().toString()
                    var from = dataSnapshot.child("from").getValue().toString()
                    var to = dataSnapshot.child("to").getValue().toString()

                    if(type == "parttime"){
                        if(str == "Parttimer"){
                            if (status == "pending") {
                                if (date.compareTo(parttimeDate) < 0) {
                                    val stat = mapOf<String, String>("status" to "expired")
                                    dbref.child(messageID).updateChildren(stat).addOnSuccessListener {

                                    }
                                }
                                messageList.add(CaregiverApply(messageID,careUserID,applierUserID,name,status,date,time,type,from,to))

                                messageList = messageList.filter { r -> r.careUserID == userID } as ArrayList<CaregiverApply>
                                recyclerView.layoutManager = linearLayoutManager
                                recyclerView.adapter = MessageAdapter(messageList,
                                    MessageAdapter.ViewListener { messageID ->
                                        val it = view
                                        if (it != null) {
                                            Navigation.findNavController(it).navigate(
                                                MessageFragmentDirections.actionMessageFragmentToMessageDetailFragment(messageID)
                                            )
                                        }
                                    })
                            }
                            if (messageList.size == 0) {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("No data found")
                                builder.setMessage("It seems like there are currently no message here")
                                builder.show()
                            }
                        }else{
                            if(status == "rejected" || status == "accepted"){
                                messageList.add(CaregiverApply(messageID,careUserID,applierUserID,name,status,date,time,type,from,to))
                                messageList = messageList.filter { r -> r.applierUserID == userID } as ArrayList<CaregiverApply>

                                /*if (messageList.size == 0) {
                                    val builder = AlertDialog.Builder(context)
                                    builder.setTitle("No data found")
                                    builder.setMessage("It seems like there are currently no message here")
                                    builder.show()
                                }*/
/*                            messageList = messageList.filter { r -> r.careUserID == userID } as ArrayList<CaregiverApply>*/

                                recyclerView.adapter = MessageAdapter(messageList,
                                    MessageAdapter.ViewListener { messageID ->
                                        val it = view
                                        if (it != null) {
                                            Navigation.findNavController(it).navigate(
                                                MessageFragmentDirections.actionMessageFragmentToMessageDetailFragment(messageID)
                                            )
                                        }
                                    })

                            }
                        }
                    }else if(type == "transport"){
                        if (status == "pending") {
                            if (date.compareTo(today) < 0) {
                                val stat = mapOf<String, String>("status" to "expired")
                                dbref.child(messageID).updateChildren(stat).addOnSuccessListener {

                                }
                            }
                            messageList.add(CaregiverApply(messageID,careUserID,applierUserID,name,status,date,time,type,from,to))

                            messageList = messageList.filter { r -> r.careUserID == userID } as ArrayList<CaregiverApply>
                            recyclerView.layoutManager = linearLayoutManager
                            recyclerView.adapter = MessageAdapter(messageList,
                                MessageAdapter.ViewListener { messageID ->
                                    val it = view
                                    if (it != null) {
                                        Navigation.findNavController(it).navigate(
                                            MessageFragmentDirections.actionMessageFragmentToMessageDetailFragment(messageID)
                                        )
                                    }
                                })
                        }
                    }


                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }



}