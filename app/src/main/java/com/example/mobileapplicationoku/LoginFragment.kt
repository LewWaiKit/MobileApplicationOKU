package com.example.mobileapplicationoku

import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mobileapplicationoku.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginFragment : Fragment() {
    private var v_binding: FragmentLoginBinding? = null
    private val binding get() = v_binding!!
    private lateinit var auth: FirebaseAuth
    private val args: LoginFragmentArgs by navArgs()
    private var dialog = LoadingDialogFragment()
    private var backPressedTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var afterRegister = args.afterRegister
        if(afterRegister == true){
            val dialog = RegisterDialogFragmnent()
            dialog.show(getChildFragmentManager(), "registerDialog")
            afterRegister = false
        }
        val callback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(backPressedTime + 2000 > System.currentTimeMillis()){
                    getActivity()?.finish()
                    handleOnBackPressed()
                }else{
                    Toast.makeText(context, "Press back again to exit",
                        Toast.LENGTH_SHORT).show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)

        auth = FirebaseAuth.getInstance()
        // Inflate the layout for this fragment
        v_binding= FragmentLoginBinding.inflate(inflater,  container ,false)
        binding.btnLogin.setOnClickListener(){
            val email = binding.tfEmail.text.toString().trim()
            val pass = binding.tfPassword.text.toString().trim()
            if(checkIsEmpty()==true){
                showProgressBar()
                if(email=="admin"&&pass=="admin123"){
                    hideProgessBar()
                    findNavController().navigate(R.id.action_loginFragment_to_adminBottomNavActivity)
                    getActivity()?.finish()
                }else{
                    auth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener() { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                val user = auth.currentUser
                                updateUI(user)
                            } else {
                                hideProgessBar()
                                Toast.makeText(context, "Invalid email or password, please try again later",
                                    Toast.LENGTH_SHORT).show()
                                updateUI(null)
                            }
                        }
                }
            }
        }
        binding.tvSignUp.setOnClickListener(){
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        return binding.root
    }

    /*public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser : FirebaseUser? = auth.currentUser
        updateUI(currentUser)
    }*/

    private fun updateUI(currentUser:FirebaseUser?){
        if(currentUser!=null){
            if(currentUser.isEmailVerified){
                hideProgessBar()
                findNavController().navigate(R.id.action_loginFragment_to_bottomNavActivity)
            }else{
                hideProgessBar()
                Toast.makeText(context, "Please verify your email address",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkIsEmpty():Boolean{
        val email = binding.tfEmail.text.toString().trim()
        val pass = binding.tfPassword.text.toString().trim()
        if(email.isEmpty()){
            binding.tfEmail.error = "Email require"
            binding.tfEmail.requestFocus()
            return false
        }else if(pass.isEmpty()){
            binding.tfPassword.error = "Password require"
            binding.tfPassword.requestFocus()
            return false
        }else{
            return true
        }
    }

    private fun showProgressBar(){
        dialog.show(getChildFragmentManager(), "loadingDialog")
    }

    private fun hideProgessBar(){
        dialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        v_binding = null
    }
    
}