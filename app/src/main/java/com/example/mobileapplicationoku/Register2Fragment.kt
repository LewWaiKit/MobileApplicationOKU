package com.example.mobileapplicationoku

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mobileapplicationoku.database.Approve
import com.example.mobileapplicationoku.database.ApproveAdapter
import com.example.mobileapplicationoku.database.User
import com.example.mobileapplicationoku.databinding.FragmentRegister2Binding
import com.google.firebase.database.*
import java.util.regex.Pattern


class Register2Fragment : Fragment() {


    private var v_binding: FragmentRegister2Binding? = null
    private val binding get() = v_binding!!
    private val args: Register2FragmentArgs by navArgs()
    private lateinit var dbref : DatabaseReference

    val MalaysiaPhoneNo_PATTERN = Pattern.compile("^" +
            "(\\+?6?01)[02-46-9]-*[0-9]{7}$|^(\\+?6?01)[1]-*[0-9]{8}$" //Malaysia phone number format 011 must 11 digit
    )

    val cardNo_PATTERN = Pattern.compile("^" +
            ".{12}" +             //must 12 digit
            "$"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v_binding= FragmentRegister2Binding.inflate(inflater,  container ,false)
        val userType = args.userType
        // Inflate the layout for this fragment
        if(userType!="Carepeople"){
            binding.tvOKUNo.setVisibility(View.GONE)
            binding.tfOKUNo.setVisibility(View.GONE)
        }
        binding.btnRegister.setOnClickListener(){
            if(checkIsEmpty()==true){
                if(checkPhoneNo()==true&&checkCardNo()==true){
                    val age = binding.tfAge.text.toString().toInt()
                    if(userType!="Carepeople") {
                        if (age < 18 || age > 50) {
                            binding.tfAge.error = "Caregiver must between 18-50 year old"
                            binding.tfAge.requestFocus()
                        }else{
                            complete()
                        }
                    }else{
                        if (age < 0 || age > 80) {
                            binding.tfAge.error = "Carepeople must between 0-80 year old"
                            binding.tfAge.requestFocus()
                        }else{
                            complete()
                        }
                    }
                }
            }else{
                Toast.makeText(context, "Please check all information", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    private fun complete(){
        var newID = ""
        val email = args.email
        val pass = args.pass
        val userType = args.userType
        val age = binding.tfAge.text.toString().toInt()
        val firstName = binding.tfFirstName.text.toString().trim()
        val lastName = binding.tfLastName.text.toString().trim()
        val nric = binding.tfNRIC.text.toString().trim()
        val contactNo = binding.tfContactNo.text.toString().trim()
        var okuNo = binding.tfOKUNo.text.toString().trim()

        dbref = FirebaseDatabase.getInstance().getReference("Approve")
        dbref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    newID="A"+ "%04d".format(snapshot.childrenCount + 1)
                }else{
                    newID="A0001"
                }
                val approve = Approve(newID,firstName,lastName,nric,email,contactNo,age,userType,okuNo,"Pending",pass)
                dbref.child(newID).setValue(approve).addOnSuccessListener() {
                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }
        })
        val action = Register2FragmentDirections.actionRegister2FragmentToLoginFragment(true)
        findNavController().navigate(action)
    }

    private fun checkIsEmpty():Boolean{
        val userType = args.userType
        val age = binding.tfAge.text.toString()
        val firstName = binding.tfFirstName.text.toString().trim()
        val lastName = binding.tfLastName.text.toString().trim()
        val nric = binding.tfNRIC.text.toString().trim()
        val contactNo = binding.tfContactNo.text.toString().trim()
        val okuNo = binding.tfOKUNo.text.toString().trim()
        if (firstName.isEmpty()){
            binding.tfFirstName.error = "Please fill in first name"
            binding.tfFirstName.requestFocus()
            return false
        }else if(lastName.isEmpty()){
            binding.tfLastName.error = "Please fill in last name"
            binding.tfLastName.requestFocus()
            return false
        }else if(nric.isEmpty()){
            binding.tfNRIC.error = "Please fill in NRIC"
            binding.tfNRIC.requestFocus()
            return false
        }else if(contactNo.isEmpty()){
            binding.tfContactNo.error = "Please fill in contact no"
            binding.tfContactNo.requestFocus()
            return false
        }else if(age.isEmpty()){
            binding.tfAge.error = "Please fill in age"
            binding.tfAge.requestFocus()
            return false
        }else if(userType=="Carepeople"){
            if(okuNo.isEmpty()){
                binding.tfOKUNo.error = "Please fill in oku No"
                binding.tfOKUNo.requestFocus()
                return false
            }
        }
        return true
    }

    private fun checkPhoneNo():Boolean{
        val contactNo = binding.tfContactNo.text.toString().trim()
        if(!MalaysiaPhoneNo_PATTERN.matcher(contactNo).matches()){
            binding.tfContactNo.error = "Invalid phone number"
            binding.tfContactNo.requestFocus()
            return false
        }
        return true
    }

    private fun checkCardNo():Boolean{
        val okuNo = binding.tfOKUNo.text.toString().trim()
        val nric = binding.tfNRIC.text.toString().trim()
        val userType = args.userType
        if(!cardNo_PATTERN.matcher(nric).matches()){
            binding.tfNRIC.error = "Invalid nric"
            binding.tfNRIC.requestFocus()
            return false
        }else if(userType=="Carepeople"){
            if(!cardNo_PATTERN.matcher(okuNo).matches()){
                binding.tfOKUNo.error = "Invalid oku No"
                binding.tfOKUNo.requestFocus()
                return false
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        v_binding = null
    }

}