package com.example.mobileapplicationoku

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mobileapplicationoku.dataClass.Approve
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

    override fun onResume() {
        super.onResume()
        val gender = resources.getStringArray(R.array.Gender)
        val genderAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, gender)
        binding.iGender.setAdapter(genderAdapter)
        val state = resources.getStringArray(R.array.State)
        val stateAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, state)
        binding.iState.setAdapter(stateAdapter)
    }

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
                    complete()
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
        val fullName = binding.tfName1.text.toString().trim()
        val nric = binding.tfNRIC.text.toString().trim()
        val gender = binding.iGender.text.toString().trim()
        val address = binding.tfAddress.text.toString().trim()
        val state = binding.iState.text.toString().trim()
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
                val approve = Approve(newID,fullName,nric,gender,address,state,contactNo,userType,okuNo,"Pending",email,pass)
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
        val fullName = binding.tfName1.text.toString().trim()
        val nric = binding.tfNRIC.text.toString().trim()
        val contactNo = binding.tfContactNo.text.toString().trim()
        val gender = binding.iGender.text.toString().trim()
        val address = binding.tfAddress.text.toString().trim()
        val state = binding.iState.text.toString().trim()
        val okuNo = binding.tfOKUNo.text.toString().trim()
        if (fullName.isEmpty()){
            binding.tfName1.error = "Please fill in your name"
            binding.tfName1.requestFocus()
            return false
        }else if(nric.isEmpty()){
            binding.tfNRIC.error = "Please fill in NRIC"
            binding.tfNRIC.requestFocus()
            return false
        }else if(contactNo.isEmpty()){
            binding.tfContactNo.error = "Please fill in contact no"
            binding.tfContactNo.requestFocus()
            return false
        }else if(gender=="Please select"){
            binding.tiGender.error = "Please select gender"
            binding.tiGender.requestFocus()
            return false
        }else if(address.isEmpty()){
            binding.tfAddress.error = "Please fill in your address"
            binding.tfAddress.requestFocus()
            return false
        }else if(state=="Please select"){
            binding.tiGender.error = "Please select state"
            binding.tiGender.requestFocus()
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