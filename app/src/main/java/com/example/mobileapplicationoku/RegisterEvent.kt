package com.example.mobileapplicationoku

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.mobileapplicationoku.databinding.FragmentRegisterEventBinding

class RegisterEvent : Fragment() {

    private var v_binding:FragmentRegisterEventBinding ? = null
    private val binding get() = v_binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v_binding = FragmentRegisterEventBinding.inflate(inflater, container, false)
        binding.btnDone.setOnClickListener(){

            Navigation.findNavController(it).navigate(RegisterEventDirections.actionRegisterEventToEvent())
        }
        return binding.root
    }

/*    override fun onDestroy() {
        super.onDestroy()
        v_binding = null
    }*/

}