package com.example.mobileapplicationoku

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.dataClass.Approve
import com.example.mobileapplicationoku.adapter.ApproveAdapter
import com.example.mobileapplicationoku.adapter.FacilitiesAdapter
import com.example.mobileapplicationoku.dataClass.Facilities
import com.example.mobileapplicationoku.databinding.FragmentApproveListBinding
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList


class approveListFragment : Fragment() {

    private lateinit var dbref : DatabaseReference
    private lateinit var approveRecyclerView : RecyclerView
    private lateinit var approveArrayList : ArrayList<Approve>
    private lateinit var facilitiesArrayList : ArrayList<Facilities>
    private var v_binding: FragmentApproveListBinding? = null
    private val binding get() = v_binding!!
    private var dialog = LoadingDialogFragment()
    private var dialog2 = NoDataDialogFragment()
    private var itemSelected = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val callback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_approveListFragment_to_homeFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
        v_binding= FragmentApproveListBinding.inflate(inflater,  container ,false)

        getAprroveData()
        search()

        return binding.root
    }

    private fun search() {
        if (itemSelected==1){
            var displayList = approveArrayList

            binding.svApprove.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {

                    if (newText!!.isNotEmpty()) {
                        displayList.clear()
                        val search = newText.lowercase(Locale.getDefault())
                        for (Approve in approveArrayList) {
                            val combineText = Approve.fullName
                            if (combineText?.lowercase(Locale.getDefault())?.contains(search) == true) {
                                displayList.add(Approve)
                            }
                        }
                        approveRecyclerView.adapter = ApproveAdapter(displayList,ApproveAdapter.ViewListener{ approveID ->
                            val it = view
                            if (it != null) {
                                Navigation.findNavController(it).navigate(approveListFragmentDirections.actionApproveListFragmentToApproveDetailsFragment(approveID))
                            }
                        })
                        binding.rvApproveList.adapter = approveRecyclerView.adapter
                        binding.rvApproveList.adapter!!.notifyDataSetChanged()

                    } else {
                        displayList.clear()
                        displayList.addAll(approveArrayList)

                        /*binding.rvApproveList.adapter!!.notifyDataSetChanged()*/
                    }

                    return true
                }

            })
        }else{
            var displayList = facilitiesArrayList

            binding.svApprove.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {

                    if (newText!!.isNotEmpty()) {
                        displayList.clear()
                        val search = newText.lowercase(Locale.getDefault())
                        for (Facilities in facilitiesArrayList) {
                            val combineText = Facilities.locationName
                            if (combineText?.lowercase(Locale.getDefault())?.contains(search) == true) {
                                displayList.add(Facilities)
                            }
                        }
                        approveRecyclerView.adapter = FacilitiesAdapter(displayList,FacilitiesAdapter.ViewListener{ facilityID ->
                            val it = view
                            if (it != null) {
                                Navigation.findNavController(it).navigate(approveListFragmentDirections.actionApproveListFragmentToAppFacilitiesDetailsFragment(facilityID))
                            }
                        })
                        binding.rvAppFacilitiesList.adapter = approveRecyclerView.adapter
                        binding.rvAppFacilitiesList.adapter!!.notifyDataSetChanged()

                    } else {
                        displayList.clear()
                        displayList.addAll(facilitiesArrayList)

                        /*binding.rvApproveList.adapter!!.notifyDataSetChanged()*/
                    }

                    return true
                }

            })
        }
    }

    private fun getAprroveData() {
        showProgressBar()
        if (itemSelected == 1) {
            binding.tvApproveTitle.text = "User Approve List"
            approveRecyclerView = binding.rvApproveList
            approveRecyclerView.layoutManager = LinearLayoutManager(context)
            approveRecyclerView.setHasFixedSize(true)
            binding.rvAppFacilitiesList.setVisibility(View.GONE)
            binding.rvApproveList.setVisibility(View.VISIBLE)
            approveArrayList = arrayListOf<Approve>()
            dbref = FirebaseDatabase.getInstance().getReference("Approve")
            dbref.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(approveSnapshot in snapshot.children){
                            val approve = approveSnapshot.getValue(Approve::class.java)
                            approveArrayList.add(approve!!)
                            approveArrayList= approveArrayList.filter{ s -> s.status == "Pending"} as ArrayList<Approve>
                        }
                        hideProgessBar()
                        if(approveArrayList.size==0){
                            noDataDialog()
                        }
                        approveRecyclerView.adapter = ApproveAdapter(approveArrayList,ApproveAdapter.ViewListener{ approveID ->
                            val it = view
                            if (it != null) {
                                Navigation.findNavController(it).navigate(approveListFragmentDirections.actionApproveListFragmentToApproveDetailsFragment(approveID))
                            }
                        })
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
        }else{
            binding.tvApproveTitle.text = "Facilities Approve List"
            approveRecyclerView = binding.rvAppFacilitiesList
            approveRecyclerView.layoutManager = LinearLayoutManager(context)
            approveRecyclerView.setHasFixedSize(true)
            binding.rvApproveList.setVisibility(View.GONE)
            binding.rvAppFacilitiesList.setVisibility(View.VISIBLE)
            facilitiesArrayList = arrayListOf<Facilities>()
            dbref = FirebaseDatabase.getInstance().getReference("Facilities")
            dbref.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(approveSnapshot in snapshot.children){
                            val facilities = approveSnapshot.getValue(Facilities::class.java)
                            facilitiesArrayList.add(facilities!!)
                            facilitiesArrayList= facilitiesArrayList.filter{ s -> s.status == "Pending"} as ArrayList<Facilities>
                        }
                        hideProgessBar()
                        if(facilitiesArrayList.size==0){
                            noDataDialog()
                        }
                        approveRecyclerView.adapter = FacilitiesAdapter(facilitiesArrayList,FacilitiesAdapter.ViewListener{ facilityID ->
                            val it = view
                            if (it != null) {
                                Navigation.findNavController(it).navigate(approveListFragmentDirections.actionApproveListFragmentToAppFacilitiesDetailsFragment(facilityID))
                            }
                        })
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })

        }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater!!.inflate(R.menu.approve_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val inflater = MenuInflater(context)
        if(itemSelected == 1 ){
            menu.findItem(R.id.iUser).setVisible(false)
            menu.findItem(R.id.iFacilities).setVisible(true)
        }else{
            menu.findItem(R.id.iUser).setVisible(true)
            menu.findItem(R.id.iFacilities).setVisible(false)
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item!!.itemId
        if (id==R.id.iUser){
            itemSelected = 1
        }
        if (id==R.id.iFacilities){
            itemSelected = 2
        }
        binding.svApprove.setQuery("",false)
        getAprroveData()
        search()
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.svApprove.setQuery("",false)
    }

    override fun onDestroy() {
        super.onDestroy()
        v_binding = null
    }

}