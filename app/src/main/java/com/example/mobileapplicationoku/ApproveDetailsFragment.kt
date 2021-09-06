package com.example.mobileapplicationoku

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mobileapplicationoku.database.User
import com.example.mobileapplicationoku.databinding.FragmentApproveDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class ApproveDetailsFragment : Fragment() {

    private var v_binding: FragmentApproveDetailsBinding? = null
    private val binding get() = v_binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref : DatabaseReference
    private lateinit var srref : StorageReference
    private lateinit var imgUri : Uri
    private val args: ApproveDetailsFragmentArgs by navArgs()
    private var password = ""
    private var email =""
    private var firstName = ""
    private var lastName = ""
    private var nric  = ""
    private var contactNo = ""
    private var age = ""
    private var userType = ""
    private var okuCardNo = ""
    private var newStatus = ""
    private var dialog = LoadingDialogFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getApproveDetail()
        auth = FirebaseAuth.getInstance()
        v_binding= FragmentApproveDetailsBinding.inflate(inflater,  container ,false)
        // Inflate the layout for this fragment

        binding.btnApprove.setOnClickListener(){
            showProgressBar()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        val Cuser = Firebase.auth.currentUser
                        val userID = auth.currentUser?.uid
                        val profileUpdates = userProfileChangeRequest {
                            displayName = "$firstName $lastName"
                            photoUri =
                                Uri.parse("android.resource://com.example.mobileapplicationoku/drawable/ic_base_profile_pic")
                        }
                        Cuser!!.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Cuser!!.sendEmailVerification()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                dbref = FirebaseDatabase.getInstance().getReference("User")
                                                val user = User(
                                                    userID,
                                                    firstName,
                                                    lastName,
                                                    nric,
                                                    email,
                                                    contactNo,
                                                    age.toInt(),
                                                    userType,
                                                    okuCardNo
                                                )
                                                if (userID != null) {
                                                    dbref.child(userID).setValue(user).addOnCompleteListener {
                                                        if (it.isSuccessful) {

                                                            Toast.makeText(
                                                                context, "Complete",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            newStatus = "Approved"
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
                                        }
                                }
                            }
                    } else {
                        hideProgessBar()
                        Toast.makeText(context, "Error: this email/user already exsits in the system",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            auth.signOut()
        }
        binding.btnReject.setOnClickListener(){
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

    private fun updateStatus() {
        dbref = FirebaseDatabase.getInstance().getReference("Approve")
        val approveID = args.approveID

        val approve = mapOf<String,String>(
            "status" to newStatus
        )
        dbref.child(approveID).updateChildren(approve).addOnSuccessListener {
            hideProgessBar()
            findNavController().navigate(ApproveDetailsFragmentDirections.actionApproveDetailsFragmentToApproveListFragment())
        }.addOnFailureListener {
            Toast.makeText(context, "Error on update status",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun getApproveDetail() {
        val approveID = args.approveID
        dbref = FirebaseDatabase.getInstance().getReference("Approve")
        dbref.child(approveID).get().addOnSuccessListener {
            if(it.exists()){
                firstName = it.child("firstName").value.toString()
                lastName = it.child("lastName").value.toString()
                nric = it.child("nric").value.toString()
                email = it.child("email").value.toString()
                contactNo = it.child("contactNo").value.toString()
                age = it.child("age").value.toString()
                userType = it.child("type").value.toString()
                okuCardNo = it.child("okucardNo").value.toString()
                password = it.child("pass").value.toString()

                binding.tvFirstName2.text = firstName
                binding.tvLastName2.text = lastName
                binding.tvNRIC2.text = nric
                binding.tvEmail2.text = email
                binding.tvContactNo2.text = contactNo
                binding.tvAge2.text = age
                binding.tvType2.text = userType
                binding.tvOKUNo2.text = okuCardNo
                if(userType=="Caregiver"){
                    binding.tvOKUNo1.setVisibility(View.GONE)
                    binding.tvOKUNo2.setVisibility(View.GONE)
                }
            }else{
                Toast.makeText(context, "Error",
                    Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(context, "Failed",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadProfilePic() {
        imgUri =Uri.parse("android.resource://com.example.mobileapplicationoku/drawable/ic_base_profile_pic")
        srref = FirebaseStorage.getInstance().getReference("User"+auth.currentUser?.uid)
        srref.putFile(imgUri).addOnSuccessListener {
            Toast.makeText(context, "Uploaded the profile pic",
                Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(context, "Error fails to upload pic",
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