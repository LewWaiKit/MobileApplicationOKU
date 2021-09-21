package com.example.mobileapplicationoku

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mobileapplicationoku.dataClass.AppFacilities
import com.example.mobileapplicationoku.databinding.FragmentAppFacilitiesDetailsBinding
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class AppFacilitiesDetailsFragment : Fragment() {

    private var v_binding: FragmentAppFacilitiesDetailsBinding? = null
    private val binding get() = v_binding!!
    private lateinit var dbref : DatabaseReference
    private lateinit var srref : StorageReference
    private val args: AppFacilitiesDetailsFragmentArgs by navArgs()
    private var newStatus = ""
    private var dialog = LoadingDialogFragment()
    private var facilityID = ""
    private var placeID: String? = null
    private var locationName: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var serviceString: ArrayList<String>? = null
    private var service: String? = null
    var serviceList: MutableList<Place.Type>? = null
    private var description: String? = null
    private var hasImage = false
    private lateinit var ImageUri : Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v_binding= FragmentAppFacilitiesDetailsBinding.inflate(inflater,  container ,false)
        // Inflate the layout for this fragment
        facilityID = args.facilityID
        getFacilitiesDetail()


        binding.btnApprove1.setOnClickListener {
            showProgressBar()
            dbref = FirebaseDatabase.getInstance().getReference("AppFacilities")
            val facilities = AppFacilities(placeID,locationName,latitude,longitude,serviceList,description)
            if (placeID != null) {
                dbref.child(placeID!!).setValue(facilities).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            context, "Complete",
                            Toast.LENGTH_SHORT
                        ).show()
                        newStatus = "Approved"
                        if(hasImage == true){
                            uploadImage()
                        }
                        updateStatus()
                    } else {
                        Toast.makeText(
                            context, "Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.btnReject1.setOnClickListener(){
            showProgressBar()
            Toast.makeText(
                context, "Complete",
                Toast.LENGTH_SHORT
            ).show()
            newStatus = "Rejected"
            updateStatus()
        }


        return binding.root
    }

    private fun uploadImage() {
        srref = FirebaseStorage.getInstance().getReference("FacilitiesImg/AppFacilities/"+placeID)
        srref.putFile(ImageUri).addOnSuccessListener {
            Toast.makeText(context, "Uploaded the profile pic",
                Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(context, "Error fails to upload pic",
                Toast.LENGTH_SHORT).show()
        }
    }


    private fun getFacilitiesDetail() {
        showProgressBar()
        /*srref = FirebaseStorage.getInstance().reference.child("UserProfilePic/4aiXGkwziTewZQ4rlngfL5iotTh2")
        val localfile = File.createTempFile("tempImage","jpg")
        srref.getFile(localfile).addOnSuccessListener {
            Toast.makeText(context, "Success",
                Toast.LENGTH_SHORT).show()
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.ivFacilityImg.setImageBitmap(bitmap)
            binding.ivFacilityImg.setVisibility(View.VISIBLE)
            hasImage = true
        }.addOnFailureListener(){
            Toast.makeText(context, "Error",
                Toast.LENGTH_SHORT).show()
        }*/
        dbref = FirebaseDatabase.getInstance().getReference("Facilities")
        dbref.child(facilityID).get().addOnSuccessListener {
            if(it.exists()){
                binding.tvFacilityID.text = facilityID
                placeID = it.child("placeID").value.toString()
                locationName = it.child("locationName").value.toString()
                latitude = it.child("latitude").value.toString().toDouble()
                longitude = it.child("longitude").value.toString().toDouble()
                serviceList = it.child("serviceList").value as MutableList<Place.Type>?
                /*val service = it.child("serviceList").child("0").value.toString()
                val service1 = it.child("serviceList").child("1").value.toString()
                serviceList?.add(service)
                serviceList?.add(service1)*/
                description = it.child("description").value.toString()
                binding.tvLocationName.text = locationName
                binding.tvServiceList.text = serviceList.toString()
                binding.tvDescription.text = description
                binding.tvLat.text = latitude.toString()
                binding.tvLong.text = longitude.toString()

                //binding.settext
                // println("loadPost:onCancelled ${databaseError.toException()}")
                srref = FirebaseStorage.getInstance().reference.child("FacilitiesImg/Facilities/$facilityID")
                val localfile = File.createTempFile("tempImage","jpg")
                srref.getFile(localfile).addOnSuccessListener {
                    Toast.makeText(context, "Success",
                        Toast.LENGTH_SHORT).show()
                    val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                    binding.ivFacilityImg.setImageBitmap(bitmap)
                    binding.ivFacilityImg.setVisibility(View.VISIBLE)
                    val path = MediaStore.Images.Media.insertImage(context?.contentResolver, bitmap, "Title"+ System.currentTimeMillis(), null)
                    ImageUri = Uri.parse(path.toString())
                    hasImage = true
                }.addOnFailureListener(){
                    Toast.makeText(context, "Error",
                        Toast.LENGTH_SHORT).show()
                }

                hideProgessBar()
            }else{
                Toast.makeText(context, "Error",
                    Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(context, "Failed",
                Toast.LENGTH_SHORT).show()
        }


    }


    private fun updateStatus() {
        dbref = FirebaseDatabase.getInstance().getReference("Facilities")
        val facilityID = args.facilityID

        val facilities = mapOf<String,String>(
            "status" to newStatus
        )
        dbref.child(facilityID).updateChildren(facilities).addOnSuccessListener {
            hideProgessBar()
            findNavController().navigate(AppFacilitiesDetailsFragmentDirections.actionAppFacilitiesDetailsFragmentToApproveListFragment())
        }.addOnFailureListener {
            Toast.makeText(context, "Error on update status",
                Toast.LENGTH_SHORT).show()
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