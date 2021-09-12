package com.example.mobileapplicationoku

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.mobileapplicationoku.databinding.FragmentChangePasswordBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser

class ChangePasswordFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    val PASSWORD_PATTERN = Pattern.compile("^" +
            "(?=.*[0-9])" +       //at least 1 digit
            "(?=.*[a-z])" +       //at least 1 lower case letter
            "(?=.*[A-Z])" +       //at least 1 upper case letter
            //"(?=.*[a-zA-Z])" +    //any letter
            "(?=.*[;@#$%^&+=])" +  //at least 1 special character
            "(?=\\S+$)" +         //no white spaces
            ".{8,20}" +             //at least 8 characters max 20
            "$"
    )

    private var v_binding: FragmentChangePasswordBinding? = null
    private val binding get() = v_binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        checkUser()
        v_binding= FragmentChangePasswordBinding.inflate(inflater,  container ,false)
        // Inflate the layout for this fragment
        binding.etNewPass.isEnabled = false
        binding.etConfirmPass.isEnabled = false
        binding.btnChangePass.text = "Next"

        binding.btnChangePass.setOnClickListener(){
            if(binding.btnChangePass.text == "Next"){
                checkCurrentPass()
            }else{
                changePassword()
            }
        }
        return binding.root
    }

    private fun checkUser() {
        val user: FirebaseUser? = auth.currentUser
        if(user == null) {
            Toast.makeText(context, "Please login first",
                Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_changePasswordFragment_to_mainActivity)
        }
    }

    private fun changePassword() {
        val newPass = binding.etNewPass.text.toString().trim()
        val confirmPass = binding.etConfirmPass.text.toString().trim()
        if(newPass.isEmpty()){
            binding.etNewPass.error = "Please fill in new password"
            binding.etNewPass.requestFocus()
        }else{
            if(!PASSWORD_PATTERN.matcher(newPass).matches()) {
                binding.etNewPass.error = "Password too weak"
                binding.etNewPass.requestFocus()
            }else{
                if(confirmPass.isEmpty()){
                    binding.etConfirmPass.error = "Please fill in confirm password"
                    binding.etConfirmPass.requestFocus()
                }else{
                    if(newPass!=confirmPass){
                        binding.etConfirmPass.error = "Must match with new password"
                        binding.etConfirmPass.requestFocus()
                    }else{
                        val user: FirebaseUser? = auth.currentUser
                        user?.updatePassword(newPass)?.addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Toast.makeText(context, "Password changed successfully, please login again",
                                    Toast.LENGTH_SHORT).show()
                                auth.signOut()
                                findNavController().navigate(R.id.action_changePasswordFragment_to_mainActivity)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkCurrentPass() {
        val currentPass = binding.etCurrentPass.text.toString().trim()
        if (currentPass.isEmpty()){
            binding.etCurrentPass.error = "Please fill in current password"
            binding.etCurrentPass.requestFocus()
        }else{
            val user: FirebaseUser? = auth.currentUser
            if(user != null && user.email != null){
                val credentail = EmailAuthProvider.getCredential(user.email!!,currentPass)
                user?.reauthenticate(credentail)?.addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(context, "Current password matched",
                            Toast.LENGTH_SHORT).show()
                        binding.btnChangePass.text = "Change Password"
                        binding.tvhint.text = "*Please enter new password and confirm password"
                        binding.etCurrentPass.isEnabled = false
                        binding.etNewPass.isEnabled = true
                        binding.etConfirmPass.isEnabled = true
                    }else{
                        Toast.makeText(context, "Password doest not match with current password",
                            Toast.LENGTH_SHORT).show()
                        binding.etCurrentPass.error = "Doest not match with current password"
                        binding.etCurrentPass.requestFocus()
                    }
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        v_binding = null
    }


}