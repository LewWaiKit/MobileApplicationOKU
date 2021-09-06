package com.example.mobileapplicationoku

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.mobileapplicationoku.databinding.FragmentDialogNoDataBinding

class NoDataDialogFragment: DialogFragment() {

    private var v_binding: FragmentDialogNoDataBinding? = null
    private val binding get() = v_binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v_binding= FragmentDialogNoDataBinding.inflate(inflater,  container ,false)
        binding.btnOK.setOnClickListener(){
            dismiss()
        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        v_binding = null
    }

}