package com.example.mobileapplicationoku

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.mobileapplicationoku.dataClass.CaregiverApply
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BookTransportFragment : Fragment() {
    private lateinit var dbref : DatabaseReference
    private var newID = ""
    private var status = ""
    private var type = ""
    private var applierID = ""
    private var temp = 0
    private val todayDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    private val todayTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    private var userID = ""
    private var careID = ""
    private var username = ""
    private var userType = ""
    private var userType_ = ""
    private var care = ""
    private var state = ""
    private var state_ = ""

    private var tempList = ArrayList<String>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book, container, false)

        val args = BookTransportFragmentArgs.fromBundle(requireArguments())
        val to = args.to
        val from = args.from

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val min = c.get(Calendar.MINUTE)
        val date = view.findViewById<TextView>(R.id.tvTransDate)
        val time = view.findViewById<TextView>(R.id.tvTransTime)

        getID()

        date.setText(todayDate)
        time.setText(todayTime)

        val auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser = auth.getCurrentUser() as FirebaseUser
        if (currentUser != null) {
            userID = auth.currentUser?.uid.toString()
            dbref = FirebaseDatabase.getInstance().getReference("Users")
            dbref.child(userID).get().addOnSuccessListener {
                username = it.child("fullName").value.toString()
                userType = it.child("type").value.toString()
                care = it.child("care").value.toString()
                state = it.child("state").value.toString()

                dbref = FirebaseDatabase.getInstance().getReference("Users")
                dbref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(dataSnapshot in snapshot.children){
                            state_ = dataSnapshot.child("state").getValue().toString()
                            careID = dataSnapshot.child("userID").getValue().toString()
                            userType_ = dataSnapshot.child("type").getValue().toString()

                            if(state == state_){
                                if(userType_ == "Caregiver"){
                                    tempList.add(careID)
                                }
                            }
                        }

                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                    }
                })

            }.addOnFailureListener {
                Toast.makeText(requireContext(),"Error", Toast.LENGTH_LONG).show()
            }



        }

        date.setOnClickListener{
            var dpd = DatePickerDialog(requireContext(),
                DatePickerDialog.OnDateSetListener{ view, mYear, mMonth, mDay->

                    c.set(Calendar.YEAR, mYear)
                    c.set(Calendar.MONTH, mMonth)
                    c.set(Calendar.DAY_OF_MONTH, mDay)
                    val myFormat = "dd/MM/yyyy" //In which you need put here
                    val sdf = SimpleDateFormat(myFormat, Locale.US)
                    date.setText(sdf.format(c.time))

                },year,month,day)
            dpd.show()
        }
        
        time.setOnClickListener {
            var tpd = TimePickerDialog(requireContext(),
            TimePickerDialog.OnTimeSetListener { view, mHour, mMin ->

                c.set(Calendar.HOUR_OF_DAY, mHour)
                c.set(Calendar.MINUTE, mMin)
                time.text = SimpleDateFormat("HH:mm").format(c.time)

            },hour,min,true)
            tpd.show()
        }

        view.findViewById<TextView>(R.id.tvTransFrom).text = from
        view.findViewById<TextView>(R.id.tvTransTo).text = to

        view.findViewById<Button>(R.id.btnTransConfirm).setOnClickListener {

            book(date.text.toString(),time.text.toString(),from,to)
            Navigation.findNavController(it).navigate(BookTransportFragmentDirections.actionTransportFrangmentToMapFragment())
        }


        return view
    }

    private fun book(date:String,time:String,from:String,to:String){
        if(tempList.size == 0){
            Toast.makeText(context,"There are currently no caregiver in your area",Toast.LENGTH_SHORT).show()
        }else{
            if (date.compareTo(todayDate) < 0){
                Toast.makeText(context,"Date invalid",Toast.LENGTH_SHORT).show()
            }else{
                dbref = FirebaseDatabase.getInstance().getReference("Message")
                dbref.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(dataSnapshot : DataSnapshot in snapshot.children){

                            if(dataSnapshot.exists()){
                                status = dataSnapshot.child("status").getValue().toString()
                                applierID = dataSnapshot.child("applierUserID").getValue().toString()
                                type = dataSnapshot.child("type").getValue().toString()
                                if(applierID == userID){
                                    if(type == "transport"){
                                        if(status == "pending"){
                                            temp = temp + 1
                                        }
                                    }

                                }
                            }

                        }
                        if(temp<=tempList.size){
                            if(userType == "Caregiver"){
                                if(care != ""){
                                    save(date,time,from,to)

                                }else{
                                    Toast.makeText(requireContext(),"You must have care people inserted", Toast.LENGTH_LONG).show()
                                }
                            }else{
                                save(date,time,from,to)

                            }
                        }

/*                    if(temp > tempList.size){
                        Toast.makeText(requireContext(),"You can only apply to a booking at a time", Toast.LENGTH_LONG).show()
                    }*/

                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            }


        }

    }

    private fun save(date: String,time: String,from: String,to: String){
        var save = CaregiverApply()
        var int = 0
        var tempID = ""
        for(i in 0..tempList.size-1) {
            int = Integer.valueOf(newID)+ (i+1)
            tempID="C"+ "%04d".format(int)
            save = CaregiverApply(
                tempID,
                tempList[i],
                userID,
                username,
                "pending",
                date,
                time,
                "transport",
                from,
                to
            )
            dbref.child(tempID).setValue(save)
                .addOnSuccessListener() {
                    Toast.makeText(
                        requireContext(),
                        "Success",
                        Toast.LENGTH_SHORT
                    ).show()

                }.addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun getID(){
        dbref = FirebaseDatabase.getInstance().getReference("Message")
        dbref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    /*newID="C"+ "%04d".format(snapshot.childrenCount + 1)*/
                    newID = snapshot.childrenCount.toString()
                }else{
                    newID="0"
                }

            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}