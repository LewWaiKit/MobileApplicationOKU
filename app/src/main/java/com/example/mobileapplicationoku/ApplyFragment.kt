package com.example.mobileapplicationoku

import android.app.DatePickerDialog
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
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import com.example.mobileapplicationoku.dataClass.CaregiverSave
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ApplyFragment : Fragment() {
    private lateinit var dbref : DatabaseReference
    private lateinit var srref : StorageReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_apply_parttime, container, false)

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val date = view.findViewById<TextView>(R.id.tvProDate)
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        date.setText(today)

        var contact = ""
        var gender = ""
        var care = ""

        date.setOnClickListener{
            var dpd = DatePickerDialog(requireContext(),
                DatePickerDialog.OnDateSetListener{ view, mYear, mMonth, mDay->

                    c.set(Calendar.YEAR, mYear);
                    c.set(Calendar.MONTH, mMonth);
                    c.set(Calendar.DAY_OF_MONTH, mDay);
                    val myFormat = "dd/MM/yyyy" //In which you need put here
                    val sdf = SimpleDateFormat(myFormat, Locale.US)
                    date.setText(sdf.format(c.time))

                },year,month,day)
            dpd.show()
        }

        val auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser = auth.getCurrentUser() as FirebaseUser
        if (currentUser != null) {
            val userID = auth.currentUser?.uid.toString()
            dbref = FirebaseDatabase.getInstance().getReference("Users")
            dbref.child(userID).get().addOnSuccessListener {
                view.findViewById<TextView>(R.id.tvProLocation).text = it.child("state").value.toString()
                view.findViewById<TextView>(R.id.tvProName).text = it.child("fullName").value.toString()
                contact = it.child("contactNo").value.toString()
                gender = it.child("gender").value.toString()
                care = it.child("care").value.toString()

                srref = FirebaseStorage.getInstance().reference.child("UserProfilePic/"+userID)
                val localfile = File.createTempFile("tempImage","jpg")
                srref.getFile(localfile).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                    view.findViewById<ImageView>(R.id.imageProfile).setImageBitmap(bitmap)
                }.addOnFailureListener(){
                    Toast.makeText(context,"Error",Toast.LENGTH_SHORT).show()
                }

            }

            dbref = FirebaseDatabase.getInstance().getReference("Parttime")
            dbref.child(userID).get().addOnSuccessListener {
                if(it.exists()){
                    if (care == ""){
                        if(it.child("status").value.toString() != "cancel" && it.child("status").value.toString() != "expired" && it.child("status").value.toString() != "booked"){
                            view.findViewById<Button>(R.id.btnProApply).visibility = View.GONE
                            view.findViewById<Button>(R.id.btnProRemove).visibility = View.VISIBLE

                            view.findViewById<TextView>(R.id.tvProDesc).text = it.child("desc").value.toString()
                            view.findViewById<TextView>(R.id.tvProDate).text = it.child("date").value.toString()
                        }else{
                            view.findViewById<Button>(R.id.btnProApply).visibility = View.VISIBLE
                            view.findViewById<Button>(R.id.btnProRemove).visibility = View.GONE

                            view.findViewById<Button>(R.id.btnProApply).setOnClickListener {
                                val name = view.findViewById<TextView>(R.id.tvProName).text.toString()
                                val date_ = view.findViewById<TextView>(R.id.tvProDate).text.toString()
                                val loc = view.findViewById<TextView>(R.id.tvProLocation).text.toString()
                                val desc = view.findViewById<TextView>(R.id.tvProDesc).text.toString()

                                if (date_.compareTo(today) < 0){
                                    Toast.makeText(context,"Date invalid",Toast.LENGTH_SHORT).show()
                                }else{
                                    val c = CaregiverSave(name,desc,loc,date_,userID,contact,"pending",gender)
                                    dbref.child(userID).setValue(c)
                                    Toast.makeText(context,"Apply Success",Toast.LENGTH_SHORT).show()
                                }
                                Navigation.findNavController(it).navigate(ApplyFragmentDirections.actionApplyFragmentToCaregiverFragment())

                            }
                        }
                    }else{
                        Toast.makeText(context,"You cannot apply as part-timer while you have the care person",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    view.findViewById<Button>(R.id.btnProApply).visibility = View.VISIBLE
                    view.findViewById<Button>(R.id.btnProRemove).visibility = View.GONE

                    view.findViewById<Button>(R.id.btnProApply).setOnClickListener {
                        val name = view.findViewById<TextView>(R.id.tvProName).text.toString()
                        val date_ = view.findViewById<TextView>(R.id.tvProDate).text.toString()
                        val loc = view.findViewById<TextView>(R.id.tvProLocation).text.toString()
                        val desc = view.findViewById<TextView>(R.id.tvProDesc).text.toString()

                        if (date_.compareTo(today) < 0){
                            Toast.makeText(context,"Date invalid",Toast.LENGTH_SHORT).show()
                        }else{
                            val c = CaregiverSave(name,desc,loc,date_,userID,contact,"pending",gender)
                            dbref.child(userID).setValue(c)
                            Toast.makeText(context,"Apply Success",Toast.LENGTH_SHORT).show()
                        }
                        Navigation.findNavController(it).navigate(ApplyFragmentDirections.actionApplyFragmentToCaregiverFragment())

                    }
                }



            }

            view.findViewById<Button>(R.id.btnProRemove).setOnClickListener {
                val stat = mapOf<String,String>("status" to "cancel")
                dbref.child(userID).updateChildren(stat).addOnSuccessListener {
                }
                Toast.makeText(context,"Remove Success",Toast.LENGTH_SHORT).show()
                Navigation.findNavController(it).navigate(ApplyFragmentDirections.actionApplyFragmentToCaregiverFragment())
                }



        }else{
            Toast.makeText(context,"Error",Toast.LENGTH_SHORT).show()
        }

        return view
    }

}