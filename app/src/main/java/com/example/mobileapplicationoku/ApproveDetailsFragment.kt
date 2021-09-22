package com.example.mobileapplicationoku

import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mobileapplicationoku.dataClass.SendEmail
import com.example.mobileapplicationoku.dataClass.User
import com.example.mobileapplicationoku.databinding.FragmentApproveDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import java.util.Properties
import javax.mail.*

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
    private var fullName = ""
    private var nric  = ""
    private var contactNo = ""
    private var gender  = ""
    private var address  = ""
    private var state  = ""
    private var userType = ""
    private var okuCardNo = ""
    private var newStatus = ""
    private var reason = ""
    private var dialog = LoadingDialogFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        getApproveDetail()
        auth = FirebaseAuth.getInstance()
        v_binding= FragmentApproveDetailsBinding.inflate(inflater,  container ,false)
        // Inflate the layout for this fragmentz

        binding.btnApprove.setOnClickListener(){
            showProgressBar()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        val Cuser = Firebase.auth.currentUser
                        val userID = auth.currentUser?.uid
                        val profileUpdates = userProfileChangeRequest {
                            displayName = "$fullName"
                            photoUri = null
                        }
                        val packageName = context?.getPackageName()
                        if(gender=="Male"){
                            imgUri = Uri.parse("android.resource://$packageName/${R.drawable.male}")
                        }else{
                            imgUri = Uri.parse("android.resource://$packageName/${R.drawable.female}")
                        }
                        uploadProfilePic()
                        Cuser!!.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Cuser!!.sendEmailVerification()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                dbref = FirebaseDatabase.getInstance().getReference("Users")
                                                val user = User(userID,fullName,nric,gender,"",address,state,contactNo,userType,okuCardNo)
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
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setTitle("Please enter reason")
            val view = layoutInflater.inflate(R.layout.dialog_rejected_reason,null)
            val reasonText: EditText = view.findViewById<EditText>(R.id.et_Reason)
            builder.setView(view)
            builder.setPositiveButton("Confirm", DialogInterface.OnClickListener { _, _ ->
                showProgressBar()
                reason = reasonText.text.toString()
                Toast.makeText(
                    context, "Complete",
                    Toast.LENGTH_SHORT
                ).show()
                newStatus = "Rejected"
                SendEmail().to(email)
                    .subject("Your account has been rejected")
                    .content("" +
                            "<p>Hello $fullName,</p>\n" +
                            "<p>Your registration has been rejected, here is the following reason.</p>\n" +
                            "<b>$reason</b>\n" +
                            "<p>Thanks</p>")
                    .isHtml()
                    .send(){
                        Toast.makeText(context, "Message send",
                            Toast.LENGTH_SHORT).show()
                    }
                updateStatus()
            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ -> })
            builder.show()


            /*val senderEmail = "yangdongdong31@gmail.com"
            val emailPass = "iluvchenhoe123"
            val props:Properties = Properties()

            props["mail.smtp.host"] = "smtp.gmail.com"
            props["mail.smtp.port"] = "587"
            props["mail.smtp.starttls.enable"] = "true"
            props["mail.smtp.auth"] = "true"

            val auth = object : Authenticator() {
                override fun getPasswordAuthentication() = PasswordAuthentication(senderEmail, emailPass)
            }

            val session = Session.getDefaultInstance(props, auth)

            val message:Message = MimeMessage(session)
            message.setFrom(InternetAddress(senderEmail))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
            message.setText("Your registration have been rejected")
            Transport.send(message)*/
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
                fullName = it.child("fullName").value.toString()
                nric = it.child("nric").value.toString()
                email = it.child("email").value.toString()
                contactNo = it.child("contactNo").value.toString()
                gender = it.child("gender").value.toString()
                address = it.child("address").value.toString()
                state = it.child("state").value.toString()
                userType = it.child("type").value.toString()
                okuCardNo = it.child("okucardNo").value.toString()
                password = it.child("pass").value.toString()

                binding.tvName3.text = fullName
                binding.tvNRIC2.text = nric
                binding.tvEmail2.text = email
                binding.tvContactNo2.text = contactNo
                binding.tvGender3.text = gender
                binding.tvAddress3.text = address
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

    private fun uploadProfilePic() {
        srref = FirebaseStorage.getInstance().getReference("UserProfilePic/"+auth.currentUser?.uid)
        srref.putFile(imgUri).addOnSuccessListener {
            Toast.makeText(context, "Uploaded the profile pic",
                Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(context, "Error fails to upload pic",
                Toast.LENGTH_SHORT).show()
        }
    }

}