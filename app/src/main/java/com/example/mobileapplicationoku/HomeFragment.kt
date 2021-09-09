package com.example.mobileapplicationoku

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class HomeFragment : Fragment() {
    private var backPressedTime = 0L
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        val user:FirebaseUser? = auth.currentUser
        val callback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (user!= null){
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
        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

}