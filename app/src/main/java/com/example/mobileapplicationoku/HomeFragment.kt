package com.example.mobileapplicationoku

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.adapter.AnnouncementAdapter
import com.example.mobileapplicationoku.dataClass.Announcement
import com.example.mobileapplicationoku.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class HomeFragment : Fragment() {
    private var backPressedTime = 0L
    private lateinit var auth: FirebaseAuth
    private var v_binding: FragmentHomeBinding? = null
    private val binding get() = v_binding!!
    private lateinit var dbref : DatabaseReference
    private lateinit var announcementRecyclerView : RecyclerView
    private lateinit var announcementArrayList : ArrayList<Announcement>
    private var dialog = LoadingDialogFragment()
    private var dialog2 = NoDataDialogFragment()
    private lateinit var imgUri : Uri
    private lateinit var srref : StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v_binding= FragmentHomeBinding.inflate(inflater,  container ,false)
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
                }else{
                    findNavController().navigate(R.id.action_homeFragment_to_mainActivity)
                    getActivity()?.finish()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
        // Inflate the layout for this fragment
        getAnnouncement()

        binding.tvEvent1.setOnClickListener() {
            //addAnnouncementImage()
        }

        binding.ivService.setOnClickListener {
            Navigation.findNavController(it).navigate(HomeFragmentDirections.actionHomeFragmentToServiceHomeFragment())
        }

        binding.ivTravel.setOnClickListener {
            Navigation.findNavController(it).navigate(HomeFragmentDirections.actionHomeFragmentToMessageFragment())
        }
        return binding.root
    }

    private fun getAnnouncement() {
        showProgressBar()
        announcementRecyclerView = binding.rvAnnoucement
        announcementRecyclerView.layoutManager = LinearLayoutManager(context)
        announcementRecyclerView.setHasFixedSize(true)
        announcementArrayList = arrayListOf<Announcement>()

        dbref = FirebaseDatabase.getInstance().getReference("Announcement")
        dbref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(announcementSnapshot in snapshot.children){
                        val announcement = announcementSnapshot.getValue(Announcement::class.java)
                        announcementArrayList.add(announcement!!)
                    }
                    hideProgessBar()
                    if(announcementArrayList.size==0){
                        noDataDialog()
                    }
                    announcementRecyclerView.adapter = AnnouncementAdapter(announcementArrayList,
                        AnnouncementAdapter.ViewListener{ announcementID ->
                        val it = view
                        if (it != null) {
                            //do something
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun showProgressBar(){
        dialog.show(getChildFragmentManager(), "loadingDialog")
    }

    private fun hideProgessBar(){
        dialog.dismiss()
    }

    private fun noDataDialog(){
        dialog2.show(getChildFragmentManager(), "noDataDialog")
    }

    override fun onDestroy() {
        super.onDestroy()
        v_binding = null
    }

    private fun addAnnouncementImage() {
        /*val packageName = context?.getPackageName()
        imgUri = Uri.parse("android.resource://$packageName/${R.drawable.an04}")
        srref = FirebaseStorage.getInstance().getReference("Announcement/AN002")
        srref.putFile(imgUri).addOnSuccessListener {
            Toast.makeText(context, "Uploaded the profile pic",
                Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(context, "Error fails to upload pic",
                Toast.LENGTH_SHORT).show()
        }*/
    }

}


