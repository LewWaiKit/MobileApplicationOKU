package com.example.mobileapplicationoku

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.mobileapplicationoku.databinding.FragmentRegisterBinding
import java.util.regex.Pattern

class RegisterFragment : Fragment() {

    private var v_binding: FragmentRegisterBinding? = null
    private val binding get() = v_binding!!
    val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

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

    override fun onResume() {
        super.onResume()
        val userType = resources.getStringArray(R.array.UserType)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, userType)
        binding.iUserType.setAdapter(arrayAdapter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v_binding= FragmentRegisterBinding.inflate(inflater,  container ,false)
        binding.btnNext.setOnClickListener(){
            if(emailValidate()==true && passValidate()==true && cPassValidate()==true && userTypeValidate()==true){
                val email = binding.tfEmail.text.toString().trim()
                val pass = binding.tfPassword.text.toString().trim()
                val userType = binding.iUserType.text.toString().trim()
                val action = RegisterFragmentDirections.actionRegisterFragmentToRegister2Fragment(email,pass,userType)
                findNavController().navigate(action)
            }else {
                Toast.makeText(context, "Please check all information", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvLogin.setOnClickListener(){
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun emailValidate():Boolean{
        val email = binding.tfEmail.text.toString().trim()
        if (email.isEmpty()){
            binding.tfEmail.error = "Please fill in email"
            binding.tfEmail.requestFocus()
            return false
        }else if(!EMAIL_PATTERN.matcher(email).matches()){
            binding.tfEmail.error = "Please fill in valid email"
            binding.tfEmail.requestFocus()
            return false
        }
        return true
    }

    private fun passValidate():Boolean{
        val pass = binding.tfPassword.text.toString().trim()
        if (pass.isEmpty()){
            binding.tfPassword.error = "Please fill in password"
            binding.tfPassword.requestFocus()
            return false
        }else if(!PASSWORD_PATTERN.matcher(pass).matches()){
            binding.tfPassword.error = "Password too weak"
            binding.tfPassword.requestFocus()
            return false
        }
        return true
    }

    private fun cPassValidate():Boolean{
        val pass = binding.tfPassword.text.toString().trim()
        val confirmPass = binding.tfConfirmPassword.text.toString().trim()
        if (confirmPass.isEmpty()){
            binding.tfConfirmPassword.error = "Please fill in confirm password"
            binding.tfConfirmPassword.requestFocus()
            return false
        }else if(pass != confirmPass){
            binding.tfConfirmPassword.error = "Confirm password must same with the password"
            binding.tfConfirmPassword.requestFocus()
            return false
        }
        return true
    }

    private fun userTypeValidate():Boolean{
        val userType = binding.iUserType.text.toString()
        if (userType == "Please select"){
            binding.tiType.error = "Please select user type"
            binding.tiType.requestFocus()
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        v_binding = null
    }


}