package com.example.mobileapplicationoku

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.mobileapplicationoku.databinding.FragmentAppFacilitiesDetailsBinding


class AppFacilitiesDetailsFragment : Fragment() {

    private var v_binding: FragmentAppFacilitiesDetailsBinding? = null
    private val binding get() = v_binding!!
    private val args: AppFacilitiesDetailsFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v_binding= FragmentAppFacilitiesDetailsBinding.inflate(inflater,  container ,false)
        // Inflate the layout for this fragment
        val facilityID = args.facilityID
        binding.tvFacilitiesID.text = "$facilityID"
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        v_binding = null
    }

}