package com.example.mobileapplicationoku

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation

class ServiceHomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_service_home, container, false)

        val imgButton1 :ImageButton = view.findViewById(R.id.imageButton1)
        val imgButton2 :ImageButton = view.findViewById(R.id.imageButton2)
        val imgButton3 :ImageButton = view.findViewById(R.id.imageButton3)
        val imgButton4 :ImageButton = view.findViewById(R.id.imageButton4)
        val imgButton5 :ImageButton = view.findViewById(R.id.imageButton5)

        imgButton1.setOnClickListener{
/*            val intent = Intent(requireContext(), eventTest::class.java)
            startActivity(intent)*/
        }
        imgButton2.setOnClickListener{
            val openURL = Intent(Intent.ACTION_VIEW)

            openURL.data = Uri.parse("https://qmed.asia/covid-19/book/406/2622")
            startActivity(openURL)
        }
        imgButton3.setOnClickListener{
            val openURL2 = Intent(Intent.ACTION_VIEW)

            openURL2.data = Uri.parse("https://journals.sagepub.com/psychology-counseling")
            startActivity(openURL2)
        }
        imgButton4.setOnClickListener{
            Navigation.findNavController(view).navigate(ServiceHomeFragmentDirections.actionServiceHomeFragmentToCaregiverFragment())
        }
        imgButton5.setOnClickListener{
            Navigation.findNavController(view).navigate(ServiceHomeFragmentDirections.actionServiceHomeFragmentToHandsignFragment())
        }


        return view
    }
}