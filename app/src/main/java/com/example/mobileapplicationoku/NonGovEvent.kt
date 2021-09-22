package com.example.mobileapplicationoku

import android.Manifest
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapplicationoku.adapter.MyAdapter
import com.example.mobileapplicationoku.dataClass.EventData
import com.example.mobileapplicationoku.databinding.FragmentEventBinding
import com.example.mobileapplicationoku.databinding.FragmentNonGovEventBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class NonGovEvent : Fragment() {

    private lateinit var mMap: GoogleMap
    private var placeID = ""
    private var latitude = 0.0
    private var longitude = 0.0
    private lateinit var latilongi: LatLng
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationReqCode = 1000
    var hasMarker = false
    private var camRequestCode = 1001
    private var storageRequestCode = 1002
    private var v_binding: FragmentNonGovEventBinding? =null
    private val binding get() = v_binding!!
    private lateinit var dataRef : DatabaseReference
    private lateinit var eventRecyclerView : RecyclerView
    private lateinit var eventArrayList : ArrayList<EventData>

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        v_binding = FragmentNonGovEventBinding.inflate(inflater,  container ,false)
        eventRecyclerView = binding.recyclerViewEvent
        eventRecyclerView.layoutManager = LinearLayoutManager(context)
        eventRecyclerView.setHasFixedSize(true)

        eventArrayList = arrayListOf<EventData>()

        getnoneventData()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity().applicationContext)

        fetchLocation()

        binding.goverment.setOnClickListener(){
            Navigation.findNavController(it).navigate(NonGovEventDirections.actionNonGovEventToEvent())
        }

        return binding.root
    }

    private fun getnoneventData(){

        dataRef = FirebaseDatabase.getInstance().getReference("Event/NonGovEvent")
        dataRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        val event = userSnapshot.getValue(EventData::class.java)
                        eventArrayList.add(event!!)
                    }

                    eventRecyclerView.adapter = MyAdapter(eventArrayList, MyAdapter.Viewlistener{ ID ->
                        val it = view
                        if (it != null){

                            // do something (when u need ur recycle view do something for btn)
                            Navigation.findNavController(it).navigate(NonGovEventDirections.actionNonGovEventToNonGovEventDetail(ID))
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun fetchLocation() {

        val task = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationReqCode

            )
            return
        }
        task.addOnSuccessListener {
            if(it != null){
                latitude = it.latitude
                longitude = it.longitude
                latilongi = LatLng(latitude, longitude)
                if(hasMarker){
                    mMap.clear()
                    hasMarker = false
                }
                mMap.addMarker(MarkerOptions().position(latilongi).title("You are here"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latilongi))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}