package com.example.mobileapplicationoku

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsClient.getPackageName
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobileapplicationoku.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.util.regex.Pattern


class ProfileFragment : Fragment() {

    private var v_binding: FragmentProfileBinding? = null
    private val binding get() = v_binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref : DatabaseReference
    private var email =""
    private var fullName =""
    private var nric  = ""
    private var contactNo = ""
    private var gender = ""
    private var address = ""
    private var state = ""
    private var userType = ""
    private var okuCardNo = ""
    private var care = ""
    private var dialog = LoadingDialogFragment()
    private lateinit var ImageUri :Uri
    private lateinit var srref : StorageReference


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        auth = FirebaseAuth.getInstance()
        checkUser()
        v_binding= FragmentProfileBinding.inflate(inflater,  container ,false)
        getUserDetails()

        binding.ivCamera.setOnClickListener(){
            selectImage()
        }
        binding.btnSave.setOnClickListener(){
            uploadImage()
        }
        binding.btnCancel.setOnClickListener(){
            getUserDetails()
        }
        return binding.root
    }


    private fun checkUser() {
        val user: FirebaseUser? = auth.currentUser
        if(user == null) {
            Toast.makeText(context, "Please login first",
                Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileFragment_to_mainActivity)
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,100)
    }

    private fun uploadImage() {
        showProgressBar()
        srref = FirebaseStorage.getInstance().getReference("UserProfilePic/"+auth.currentUser?.uid)
        srref.putFile(ImageUri).addOnSuccessListener {
            Toast.makeText(context, "Uploaded the profile pic",
                Toast.LENGTH_SHORT).show()
            hideProgessBar()
            binding.btnSave.setVisibility(View.GONE)
            binding.btnCancel.setVisibility(View.GONE)
        }.addOnFailureListener{
            Toast.makeText(context, "Error fails to upload pic",
                Toast.LENGTH_SHORT).show()
            hideProgessBar()
            binding.btnSave.setVisibility(View.GONE)
            binding.btnCancel.setVisibility(View.GONE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK){
            ImageUri = data?.data!!
            binding.ivProfile.setImageURI(ImageUri)
            binding.btnSave.setVisibility(View.VISIBLE)
            binding.btnCancel.setVisibility(View.VISIBLE)
        }
    }

    private fun getUserDetails(){
        showProgressBar()
        binding.btnSave.setVisibility(View.GONE)
        binding.btnCancel.setVisibility(View.GONE)
        srref = FirebaseStorage.getInstance().reference.child("UserProfilePic/"+auth.currentUser?.uid)
        val localfile = File.createTempFile("tempImage","jpg")
        srref.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.ivProfile.setImageBitmap(bitmap)
        }.addOnFailureListener(){

        }
        val userID = auth.currentUser?.uid
        dbref = FirebaseDatabase.getInstance().getReference("Users")
        dbref.child(userID.toString()).get().addOnSuccessListener {
            if(it.exists()){
                fullName = it.child("fullName").value.toString()
                nric = it.child("nric").value.toString()
                email = auth.currentUser?.email.toString()
                contactNo = it.child("contactNo").value.toString()
                gender = it.child("gender").value.toString()
                address = it.child("address").value.toString()
                state = it.child("state").value.toString()
                userType = it.child("type").value.toString()
                okuCardNo = it.child("okucardNo").value.toString()
                care = it.child("care").value.toString()

                binding.tvName.text = fullName
                binding.tvIc2.text = nric
                binding.tvProfileEmail1.text = email
                binding.tvPhone2.text = contactNo
                binding.tvGender3.text = gender
                binding.tvAddress5.text = address
                binding.tvState3.text = state
                binding.tvUserType1.text = userType
                binding.tvOkuCardNo1.text = okuCardNo
                binding.tvCare1.text = care
                if(userType=="Caregiver"){
                    binding.tvOkuCardNo.setVisibility(View.GONE)
                    binding.tvOkuCardNo1.setVisibility(View.GONE)
                    binding.tvCare.text = "Take care:"
                    if(care.isEmpty()){
                        binding.tvCare.setVisibility(View.GONE)
                        binding.tvCare1.setVisibility(View.GONE)
                    }
                }else{
                    binding.tvCare.text = "Care by:"
                    if(care.isEmpty()){
                        binding.tvCare.setVisibility(View.GONE)
                        binding.tvCare1.setVisibility(View.GONE)
                    }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater!!.inflate(R.menu.profile_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item!!.itemId
        if (id==R.id.iChangePassword){
            findNavController().navigate(R.id.action_profileFragment_to_changePasswordFragment)
        }
        if (id==R.id.iSignOut){
            auth.signOut()
            Toast.makeText(context, "Logout successful",
                Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileFragment_to_mainActivity)
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onDestroy() {
        super.onDestroy()
        v_binding = null
    }

    private fun showProgressBar(){
        dialog.show(getChildFragmentManager(), "loadingDialog")
    }

    private fun hideProgessBar(){
        dialog.dismiss()
    }

}