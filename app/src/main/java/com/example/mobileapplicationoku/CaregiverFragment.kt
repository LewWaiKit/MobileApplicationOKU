package com.example.mobileapplicationoku

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.adapter.ApproveAdapter
import com.example.mobileapplicationoku.adapter.CaregiverAdapter
import com.example.mobileapplicationoku.dataClass.CareGiver
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CaregiverFragment :Fragment() {
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance("https://madoku-fa5a5-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private var myRef = database.getReference("Parttime")
    private lateinit var srref : StorageReference
    private var caregiverList = mutableListOf<CareGiver>()
    private lateinit var dbref : DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_caregiver, container, false)
        val recyclerView : RecyclerView = view.findViewById(R.id.profileRecyclerView)
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val btnApply = view.findViewById<Button>(R.id.btnApply)
        var location = ""
        var userType = ""
        var username = ""

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val date = view.findViewById<TextView>(R.id.tvSelectDate)

        date.setText(today)

        val auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser = auth.getCurrentUser() as FirebaseUser
        if (currentUser != null) {
            val userID = auth.currentUser?.uid.toString()
            dbref = FirebaseDatabase.getInstance().getReference("Users")
            dbref.child(userID).get().addOnSuccessListener {
                location = it.child("state").value.toString()
                userType = it.child("type").value.toString()
                username = it.child("fullName").value.toString()

                if(userType != "Caregiver"){
                    btnApply.visibility = View.GONE
                }else{
                    btnApply.visibility = View.VISIBLE
                }

                getList(location, date.text.toString(), username, recyclerView, today)
            }
                .addOnFailureListener {
                    Toast.makeText(context,"Error", Toast.LENGTH_LONG).show()
                }

        }

        date.setOnClickListener{
            var dpd = DatePickerDialog(requireContext(),DatePickerDialog.OnDateSetListener{view,mYear,mMonth,mDay->

                c.set(Calendar.YEAR, mYear);
                c.set(Calendar.MONTH, mMonth);
                c.set(Calendar.DAY_OF_MONTH, mDay);
                val myFormat = "dd/MM/yyyy" //In which you need put here
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                date.setText(sdf.format(c.time))

                getList(location, date.text.toString(), username, recyclerView, today)
            },year,month,day)
            dpd.show()
        }

        btnApply.setOnClickListener {
            Navigation.findNavController(view).navigate(CaregiverFragmentDirections.actionCaregiverFragmentToApplyFragment())
        }

        return view
    }

    private fun getList(location:String, date:String, username:String, recyclerView:RecyclerView, today:String){
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading list...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        if(caregiverList != null){

            caregiverList.clear()
        }


        val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(dataSnapshot : DataSnapshot in snapshot.children){
                    var temp = dataSnapshot.child("userID").getValue().toString()

                    srref = FirebaseStorage.getInstance().reference.child("UserProfilePic/"+temp)
                    var localfile = File.createTempFile("tempImage","jpg")
                    srref.getFile(localfile).addOnSuccessListener {
                        var bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                        var name = dataSnapshot.child("fullName").getValue().toString()
                        var desc = dataSnapshot.child("desc").getValue().toString()
                        var location_ = dataSnapshot.child("state").getValue().toString()
                        var date_ = dataSnapshot.child("date").getValue().toString()
                        var userID = dataSnapshot.child("userID").getValue().toString()
                        var status = dataSnapshot.child("status").getValue().toString()


                        if(status == "pending"){
                            if(date_.compareTo(today) < 0){
                                val stat = mapOf<String,String>("status" to "expired")
                                myRef.child(userID).updateChildren(stat).addOnSuccessListener {

                                }
                            }
                            if (location_ == location){
                                if(date_ == date){
                                    caregiverList.add(CareGiver(name,desc,bitmap,location,date_,userID))
                                }
                                if(caregiverList.size==0){
                                    val builder = AlertDialog.Builder(context)
                                    builder.setTitle("No data found")
                                    builder.setMessage("It seems like there are currently no part-timer here")
                                    builder.show()
                                }
                                caregiverList = caregiverList.filter { r -> r.name != username } as ArrayList<CareGiver>
                                recyclerView.layoutManager = linearLayoutManager
                                recyclerView.adapter = CaregiverAdapter(caregiverList,
                                    CaregiverAdapter.ViewListener{ userID ->
                                    val it = view
                                    if (it != null) {
                                        Navigation.findNavController(it).navigate(CaregiverFragmentDirections.actionCaregiverFragmentToParttimeDetailsFragment(userID))
                                    }
                                })
                                progressDialog.dismiss()
                            }
                        }/*else{
                            if(caregiverList.size==0){
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("No data found")
                                builder.setMessage("It seems like there are currently no part-timer here")
                                builder.show()
                                progressDialog.dismiss()
                            }
                        }*/
                    }.addOnFailureListener(){
                        Toast.makeText(context,"Failed to retrieve list", Toast.LENGTH_LONG).show()
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


}