package com.example.mobileapplicationoku

import android.os.Bundle
import android.view.*
import android.widget.Adapter
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.database.Approve
import com.example.mobileapplicationoku.database.ApproveAdapter
import com.example.mobileapplicationoku.databinding.FragmentApproveListBinding
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList


class approveListFragment : Fragment() {

    private lateinit var dbref : DatabaseReference
    private lateinit var approveRecyclerView : RecyclerView
    private lateinit var approveArrayList : ArrayList<Approve>
    private var v_binding: FragmentApproveListBinding? = null
    private val binding get() = v_binding!!
    private var dialog = LoadingDialogFragment()
    private var dialog2 = NoDataDialogFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v_binding= FragmentApproveListBinding.inflate(inflater,  container ,false)
        approveRecyclerView = binding.rvApproveList
        approveRecyclerView.layoutManager = LinearLayoutManager(context)
        approveRecyclerView.setHasFixedSize(true)
        approveArrayList = arrayListOf<Approve>()
        getAprroveData()
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
                        val combineText = Approve.lastName
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

        return binding.root
    }

    private fun getAprroveData() {
        showProgressBar()
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

}