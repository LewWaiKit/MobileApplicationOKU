package com.example.mobileapplicationoku

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.StrictMode
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.dataClass.Handsign
import com.example.mobileapplicationoku.adapter.SignAdapter
import com.google.firebase.database.FirebaseDatabase
import java.net.URL
import java.util.*

class HandsignFragment : Fragment(){
    val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://madoku-fa5a5-default-rtdb.asia-southeast1.firebasedatabase.app/")
    var myRef = database.getReference("Handsign")
    val imgList :MutableList<Handsign> = mutableListOf()

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val data: Intent? = result.data

            val text = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            view?.findViewById<TextView>(R.id.tvResult)?.text = text?.get(0)
            getImage(view?.findViewById<TextView>(R.id.tvResult)?.text.toString().lowercase())

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_handsign, container, false)

/*        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),10)*/

        view?.findViewById<Button>(R.id.btnSpeak)?.setOnClickListener{

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            resultLauncher.launch(intent)

        }

        return view
    }

/*    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }*/

    private fun getImage(str :String){
        val imgAdapter = SignAdapter(imgList)
        val phrase = str.filter { it.isLetterOrDigit() }
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Fetching handsign image..")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val recyclerView : RecyclerView = view?.findViewById(R.id.hansignRecycleView) as RecyclerView

        val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        if(imgList.isNotEmpty()){
            imgList.clear()
            imgAdapter.notifyDataSetChanged()
        }

        for(i in 0..(phrase.length-1)){
            val key = "sign" + phrase[i]

            myRef.child(key).get().addOnSuccessListener {
                val img = it.child("image").value.toString()
                val name = it.child("name").value.toString()

                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val url = URL(img)
                val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                imgList.add(Handsign(name,image))
                recyclerView.layoutManager = linearLayoutManager
                recyclerView.adapter = imgAdapter
                progressDialog.dismiss()

            }

        }


    }
}