package com.example.mobileapplicationoku

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.mobileapplicationoku.dataClass.AppFacilities
import com.example.mobileapplicationoku.dataClass.Facilities
import com.example.mobileapplicationoku.databinding.FragmentMapBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File


class MapFragment : Fragment(), LocationListener {
    private lateinit var mMap: GoogleMap
    private var placeID = ""
    private var fID = ""
    private var pID = ""
    private var latitude = 0.0
    private var longitude = 0.0
    private lateinit var latilongi: LatLng
    private var serviceLat: Double = 0.0
    private var serviceLng: Double = 0.0
    var name = ""
    lateinit var service: String
    var serviceList: MutableList<Place.Type>? = null
    private var desc = ""
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationReqCode = 1000
    private var camRequestCode = 1001
    private var storageRequestCode = 1002
    private lateinit var locationRequest: LocationRequest
    var hasMarker = false
    private lateinit var placeList: ArrayList<AppFacilities>
    private var markerList: ArrayList<MarkerOptions> = ArrayList()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var bmp: Bitmap? = null
    private var imgUri1: Uri? = null
    private lateinit var dbref : DatabaseReference
    private lateinit var dbref2 : DatabaseReference
    private lateinit var srref : StorageReference
    private var v_binding: FragmentMapBinding? = null
    private val binding get() = v_binding!!
    var category = 0

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        mMap = googleMap


        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as? AutocompleteSupportFragment

        // Specify the types of place data to return.
        if(!Places.isInitialized()){
            Places.initialize(requireActivity().applicationContext, getString(R.string.google_maps_key))
        }

        autocompleteFragment?.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                viewVisible()
                markerList.clear()
                Log.i(TAG, "Place: ${place.name}, ${place.id}, ${place.latLng}, ${place.types}")

                placeID = place.id.toString()
                longitude = place.latLng!!.longitude
                latitude = place.latLng!!.latitude
                name = place.name.toString()
                service = place.types!![0].toString()
                serviceList = place.types
                binding.include.tvPlaceName.text = name
                binding.include.tvService.text = service

                latilongi = LatLng(latitude, longitude)
                if(latilongi != null){
                    mMap.clear()
                    hasMarker = false
                }
                mMap.addMarker(MarkerOptions().position(latilongi).title("Marker in $name"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latilongi))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))
                hasMarker = true


            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(ContentValues.TAG, "An error occurred: $status")
            }
        })

        mMap.setOnMarkerClickListener {marker ->
            if(hasMarker){
                dbref2 = FirebaseDatabase.getInstance().getReference("AppFacilities")
                /*dbref2.child(placeID).get().addOnSuccessListener {
                    if(it.exists()){
                        binding.include.btnSubmit.text = "Update"

                    }

                    else
                    binding.include.btnSubmit.text = "Submit"
                }
                .addOnFailureListener(){

                }*/

                dbref2.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(appFac in snapshot.children){
                                if(placeID == appFac.child("id").value.toString()){
                                    binding.include.tvPlaceName.text = appFac.child("locationName").value.toString()
                                    binding.include.tvService.text = appFac.child("serviceList").child("0").value.toString()

                                    binding.include.textView2.textSize = 17f
                                    binding.include.textView2.text = "The wheelchair accessible in this place is " + appFac.child("description").value.toString()

                                    binding.include.btnSubmit.text = "Update"
                                    srref = FirebaseStorage.getInstance().reference.child("FacilitiesImg/AppFacilities/" + appFac.child("id").value.toString())
                                    val localfile = File.createTempFile("tempImg", "gif")
                                    srref.getFile(localfile).addOnSuccessListener {
                                        val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                                        binding.include.showImg.setImageBitmap(bitmap)

                                    }.addOnFailureListener(){
                                        binding.include.btnSubmit.text = "Submit"
                                        viewVisible()
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
                for(i in 0 until markerList.size){
                    mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                }

                for(i in 0 until placeList.size){
                    if(placeList[i].id  == marker.tag){

                        srref = FirebaseStorage.getInstance().reference.child("FacilitiesImg/AppFacilities/" + placeList[i].id)
                        val localfile = File.createTempFile("tempImg", "gif")
                        srref.getFile(localfile).addOnSuccessListener {
                            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                            binding.include.showImg.setImageBitmap(bitmap)

                        }.addOnFailureListener(){

                        }

                        binding.include.tvPlaceName.text = placeList[i].locationName.toString()
                        binding.include.tvService.text = placeList[i].serviceList?.get(0).toString()
                        binding.include.cvFull.visibility = View.GONE
                        binding.include.cvPartial.visibility = View.GONE
                        binding.include.cvNo.visibility = View.GONE
                        binding.include.textView2.textSize = 17f
                        binding.include.textView2.text = "The wheelchair accessible in this place is " + placeList[i].description.toString()
                        binding.include.btnAddImg.visibility = View.GONE
                        binding.include.cvCameraIcon.visibility = View.GONE
                        binding.include.btnSubmit.visibility = View.GONE

                    }

                }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                binding.include.showImg.setImageResource(0)
                return@setOnMarkerClickListener true
            }
            else{
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                return@setOnMarkerClickListener false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v_binding= FragmentMapBinding.inflate(inflater,  container ,false)
        /*val view = inflater.inflate(R.layout.fragment_maps, container, false)*/
        val fab = binding.fabLocation
        val btmSheet = binding.include.bottomSheet
        val btnAddImg = binding.include.btnAddImg
        val btnSubmit = binding.include.btnSubmit
        val btnBook = binding.include.btnTransport
        val showImg = binding.include.showImg
        val cvFull = binding.include.cvFull
        val cvPartial = binding.include.cvPartial
        val cvNo = binding.include.cvNo


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        bottomSheetBehavior = BottomSheetBehavior.from(btmSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        placeList = arrayListOf<AppFacilities>()
        fetchLocation()
        bottomBehavior()
        viewVisible()
        cvFull.setOnClickListener(){
            desc = binding.include.tvFull.text.toString()
        }

        cvPartial.setOnClickListener(){
            desc = binding.include.tvPartial.text.toString()
        }

        cvNo.setOnClickListener(){
            desc = binding.include.tvNo.text.toString()
        }

        fab.setOnClickListener {
            mMap.clear()
            fetchLocation()
        }

        btnAddImg.setOnClickListener(){
            selectImage()
        }

        btnSubmit.setOnClickListener(){
            dbref = FirebaseDatabase.getInstance().getReference("Facilities")
            dbref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        fID="F"+ "%04d".format(snapshot.childrenCount + 1)
                    }else{
                        fID="F0001"
                    }
                    srref = FirebaseStorage.getInstance().getReference("FacilitiesImg/Facilities/$fID")
                    val facility = Facilities(fID, placeID, name, latitude, longitude, serviceList, desc, "Pending")

                    if(imgUri1 != null){
                        srref.putFile(imgUri1!!)
                    }
                    dbref.child(fID).setValue(facility).addOnSuccessListener() {
                        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener(){
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            showImg.setImageResource(0)
            hasMarker = false
        }

        btnBook.setOnClickListener {
            Navigation.findNavController(it).navigate(MapFragmentDirections.actionMapFragmentToTransportFrangment())
        }

        binding.cvRest.setOnClickListener(){
            mMap.clear()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            category = 1
            compareService()
        }

        binding.cvTransport.setOnClickListener(){
            mMap.clear()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            category = 2
            compareService()
        }

        binding.cvShopping.setOnClickListener(){
            mMap.clear()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            category = 3
            compareService()
        }

        binding.cvTourism.setOnClickListener(){
            mMap.clear()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            category = 4
            compareService()
        }

        binding.cvEducation.setOnClickListener(){
            mMap.clear()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            category = 5
            compareService()
        }

        binding.cvHealth.setOnClickListener(){
            mMap.clear()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            category = 6
            compareService()
        }

        return binding.root
    }

    private fun viewVisible(){
        binding.include.cvFull.isVisible = true
        binding.include.cvPartial.isVisible = true
        binding.include.cvNo.isVisible = true
        binding.include.textView2.textSize = 16f
        binding.include.textView2.text = "How well is the wheelchair accessible in this place?"
        binding.include.btnAddImg.isVisible = true
        binding.include.cvCameraIcon.isVisible = true
        binding.include.btnSubmit.isVisible = true
        binding.include.showImg.visibility = View.VISIBLE
    }

    private fun bottomBehavior(){
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                /*when(newState){
                    BottomSheetBehavior.STATE_EXPANDED-> Toast.makeText(context, "STATE_EXPANDED", Toast.LENGTH_SHORT).show()
                    BottomSheetBehavior.STATE_DRAGGING -> Toast.makeText(context, "STATE_DRAGGING", Toast.LENGTH_SHORT).show()
                    BottomSheetBehavior.STATE_SETTLING -> Toast.makeText(context, "STATE_SETTLING", Toast.LENGTH_SHORT).show()
                    BottomSheetBehavior.STATE_HIDDEN -> Toast.makeText(context, "STATE_HIDDEN", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(context, "OTHER_STATE", Toast.LENGTH_SHORT).show()
                }*/
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }


        })
    }

    private fun selectImage() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Add Photo!")
        builder.setItems(options) { dialog, item ->
            if (options[item] == "Take Photo") {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, camRequestCode)
            } else if (options[item] == "Choose from Gallery") {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, storageRequestCode)
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == camRequestCode) {
            if (resultCode == Activity.RESULT_OK) {

                bmp = data?.extras!!["data"] as Bitmap?
                val stream = ByteArrayOutputStream()
                bmp!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val byteArray = stream.toByteArray()

                // convert byte array to Bitmap
                val bitmap = BitmapFactory.decodeByteArray(
                    byteArray, 0,
                    byteArray.size
                )

                val path = MediaStore.Images.Media.insertImage(context?.contentResolver, bitmap, "Title" + System.currentTimeMillis(), null)
                imgUri1 = Uri.parse(path.toString())
                /*view?.findViewById<ImageView>(R.id.showImg)?.setImageBitmap(bitmap)*/
                view?.findViewById<ImageView>(R.id.showImg)?.setImageURI(imgUri1)
            }
        } else if (requestCode == storageRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                imgUri1 = data?.data
                view?.findViewById<ImageView>(R.id.showImg)?.setImageURI(imgUri1)
            }
        }
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
                mMap.animateCamera(CameraUpdateFactory.zoomTo(8f))

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }

    override fun onDestroy() {
        super.onDestroy()
        v_binding = null
    }

    private fun compareService(){
        markerList.clear()
        dbref = FirebaseDatabase.getInstance().getReference("AppFacilities")
        dbref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(facilitySnapshot in snapshot.children){
                        for(child in facilitySnapshot.child("serviceList").children){
                            if(category == 1){
                                if(child.value.toString().uppercase() == "RESTAURANT" || child.value.toString().uppercase() == "CAFE" || child.value.toString().uppercase() == "FOOD" ||
                                    child.value.toString().uppercase() == "BAKERY" || child.value.toString().uppercase() == "MEAL_DELIVERY" || child.value.toString().uppercase() == "MEAL_TAKEAWAY"){ // need to categorize the category

                                    pID = facilitySnapshot.child("id").value.toString()
                                    placeList.add(facilitySnapshot.getValue(AppFacilities::class.java)!!)
                                    binding.include.tvPlaceName.text = facilitySnapshot.child("locationName").value.toString()
                                    binding.include.tvService.text = facilitySnapshot.child("serviceList").child("0").value.toString()
                                    serviceLat = facilitySnapshot.child("latitude").value.toString().toDouble()
                                    serviceLng = facilitySnapshot.child("longitude").value.toString().toDouble()
                                    var serviceLocation = LatLng(serviceLat, serviceLng)
                                    markerList.add(MarkerOptions().position(serviceLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)).title("$pID"))

                                    markerList.distinct()
                                    placeList.distinct()
                                    for(i in 0 until markerList.size){
                                        mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                        mMap.addMarker(markerList[i])
                                    }
                                    /*markerList.groupBy { it.title }
                                    for(i in 0 until markerList.size - 1){
                                        if(markerList[i].title.toString() == pID){
                                            markerList.removeAt(i)
                                        }
                                        mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                        mMap.addMarker(markerList[i])
                                    }

                                    for(i in 0 until placeList.size - 1){
                                        if(placeList[i].id == pID){
                                            placeList.removeAt(i)
                                        }
                                    }*/
                                    hasMarker = true
                                }
                            }

                            else if(category == 2){
                                if(child.value.toString().uppercase() == "BUS_STATION" || child.value.toString().uppercase() == "TRAIN_STATION" || child.value.toString().uppercase() == "TRANSIT_STATION" ||
                                    child.value.toString().uppercase() == "TAXI_STAND" || child.value.toString().uppercase() == "SUBWAY_STATION" || child.value.toString().uppercase() == "GAS_STATION" ||
                                    child.value.toString().uppercase() == "AIRPORT" || child.value.toString().uppercase() == "PARKING" || child.value.toString().uppercase() == "GAS_STATION"){ // need to categorize the category

                                    pID = facilitySnapshot.child("id").value.toString()
                                    placeList.add(facilitySnapshot.getValue(AppFacilities::class.java)!!)
                                    binding.include.tvPlaceName.text = facilitySnapshot.child("locationName").value.toString()
                                    binding.include.tvService.text = facilitySnapshot.child("serviceList").child("0").value.toString()
                                    serviceLat = facilitySnapshot.child("latitude").value.toString().toDouble()
                                    serviceLng = facilitySnapshot.child("longitude").value.toString().toDouble()
                                    var serviceLocation = LatLng(serviceLat, serviceLng)
                                    markerList.add(MarkerOptions().position(serviceLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title("$pID"))

                                    markerList.distinct()
                                    placeList.distinct()
                                    for(i in 0 until markerList.size){
                                        mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                        mMap.addMarker(markerList[i])
                                    }
                                    /*for(i in 0 until markerList.size - 1){
                                        if(markerList[i].title.toString() == pID){
                                            markerList.removeAt(i)
                                        }
                                        mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                        mMap.addMarker(markerList[i])

                                    }

                                    for(i in 0 until placeList.size - 1){
                                        if(placeList[i].id == pID){
                                            placeList.removeAt(i)
                                        }
                                    }*/

                                    hasMarker = true
                                }
                            }

                            else if(category == 3){
                                if(child.value.toString().uppercase() == "CLOTHING_STORE" || child.value.toString().uppercase() == "CONVENIENCE_STORE" || child.value.toString().uppercase() == "DEPARTMENT_STORE" ||
                                    child.value.toString().uppercase() == "ELECTRONIC_STORE" || child.value.toString().uppercase() == "FURNITURE_STORE" || child.value.toString().uppercase() == "HARDWARE_STORE" ||
                                    child.value.toString().uppercase() == "HOME_GOODS_STORE" || child.value.toString().uppercase() == "JEWELRY_STORE" || child.value.toString().uppercase() == "LIQUOR_STORE" ||
                                    child.value.toString().uppercase() == "PET_STORE" || child.value.toString().uppercase() == "SHOE_STORE" || child.value.toString().uppercase() == "SHOPPING_MALL" ||
                                    child.value.toString().uppercase() == "STORE" || child.value.toString().uppercase() == "SUPERMARKET"){ // need to categorize the category

                                    pID = facilitySnapshot.child("id").value.toString()
                                    placeList.add(facilitySnapshot.getValue(AppFacilities::class.java)!!)
                                    binding.include.tvPlaceName.text = facilitySnapshot.child("locationName").value.toString()
                                    binding.include.tvService.text = facilitySnapshot.child("serviceList").child("0").value.toString()
                                    serviceLat = facilitySnapshot.child("latitude").value.toString().toDouble()
                                    serviceLng = facilitySnapshot.child("longitude").value.toString().toDouble()
                                    var serviceLocation = LatLng(serviceLat, serviceLng)
                                    markerList.add(MarkerOptions().position(serviceLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("$pID"))
                                    markerList.groupBy { it.title }
                                    /*for(i in 0 until markerList.size - 1){
                                        if(markerList[i].title.toString() == pID){
                                            markerList.removeAt(i)
                                        }
                                        mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                        mMap.addMarker(markerList[i])
                                    }*/
                                    markerList.distinct()
                                    placeList.distinct()
                                    for(i in 0 until markerList.size){
                                        mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                        mMap.addMarker(markerList[i])
                                    }

                                    /*for(i in 0 until placeList.size - 1){
                                        if(placeList[i].id == pID){
                                            placeList.removeAt(i)
                                        }
                                    }*/
                                    hasMarker = true
                                }
                            }

                            else if(category == 4){
                                if(child.value.toString().uppercase() == "AIRPORT" || child.value.toString().uppercase() == "AMUSEMENT_PARK" || child.value.toString().uppercase() == "AQUARIUM" ||
                                    child.value.toString().uppercase() == "ART_GALLERY" || child.value.toString().uppercase() == "LODGING" || child.value.toString().uppercase() == "MUSEUM" ||
                                    child.value.toString().uppercase() == "TOURIST_ATTRACTION" || child.value.toString().uppercase() == "TRAVEL_AGENCY" || child.value.toString().uppercase() == "ZOO"){ // need to categorize the category

                                    pID = facilitySnapshot.child("id").value.toString()
                                    placeList.add(facilitySnapshot.getValue(AppFacilities::class.java)!!)
                                    binding.include.tvPlaceName.text = facilitySnapshot.child("locationName").value.toString()
                                    binding.include.tvService.text = facilitySnapshot.child("serviceList").child("0").value.toString()
                                    serviceLat = facilitySnapshot.child("latitude").value.toString().toDouble()
                                    serviceLng = facilitySnapshot.child("longitude").value.toString().toDouble()
                                    var serviceLocation = LatLng(serviceLat, serviceLng)
                                    markerList.add(MarkerOptions().position(serviceLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).title("$pID"))

                                    markerList.distinct()
                                    placeList.distinct()
                                    for(i in 0 until markerList.size){
                                        mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                        mMap.addMarker(markerList[i])
                                    }
                                    /*markerList.groupBy { it.title }
                                    for(i in 0 until markerList.size - 1){
                                        if(markerList[i].title.toString() == pID){
                                            markerList.removeAt(i)
                                        }
                                        mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                        mMap.addMarker(markerList[i])
                                    }

                                    for(i in 0 until placeList.size - 1){
                                        if(placeList[i].id == pID){
                                            placeList.removeAt(i)
                                        }
                                    }*/

                                    hasMarker = true
                                }
                            }

                            else if(category == 5){
                                if(child.value.toString().uppercase() == "BOOK_STORE" || child.value.toString().uppercase() == "LIBRARY" || child.value.toString().uppercase() == "PRIMARY_SCHOOL" ||
                                    child.value.toString().uppercase() == "SECONDARY_SCHOOL" || child.value.toString().uppercase() == "SCHOOL" || child.value.toString().uppercase() == "UNIVERSITY"){ // need to categorize the category

                                    pID = facilitySnapshot.child("id").value.toString()
                                    placeList.add(facilitySnapshot.getValue(AppFacilities::class.java)!!)
                                    binding.include.tvPlaceName.text = facilitySnapshot.child("locationName").value.toString()
                                    binding.include.tvService.text = facilitySnapshot.child("serviceList").child("0").value.toString()
                                    serviceLat = facilitySnapshot.child("latitude").value.toString().toDouble()
                                    serviceLng = facilitySnapshot.child("longitude").value.toString().toDouble()
                                    var serviceLocation = LatLng(serviceLat, serviceLng)
                                    markerList.add(MarkerOptions().position(serviceLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("$pID"))

                                    markerList.distinct()
                                    placeList.distinct()
                                    for(i in 0 until markerList.size){
                                        mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                        mMap.addMarker(markerList[i])
                                    }
                                    /*markerList.groupBy { it.title }
                                    for(i in 0 until markerList.size - 1){
                                        if(markerList[i].title.toString() == pID){
                                            markerList.removeAt(i)
                                        }
                                        mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                        mMap.addMarker(markerList[i])
                                    }

                                    for(i in 0 until placeList.size - 1){
                                        if(placeList[i].id == pID){
                                            placeList.removeAt(i)
                                        }
                                    }*/

                                    hasMarker = true
                                }
                            }

                            else if(category == 6){
                                if(child.value.toString().uppercase() == "DENTIST" || child.value.toString().uppercase() == "DRUGSTORE" || child.value.toString().uppercase() == "HOSPITAL" ||
                                    child.value.toString().uppercase() == "PHARMACY" || child.value.toString().uppercase() == "PHYSIOTHERAPIST" || child.value.toString().uppercase() == "VETERINARY_CARE" ||
                                    child.value.toString().uppercase() == "HEALTH"){ // need to categorize the category

                                    pID = facilitySnapshot.child("id").value.toString()
                                    placeList.add(facilitySnapshot.getValue(AppFacilities::class.java)!!)
                                    binding.include.tvPlaceName.text = facilitySnapshot.child("locationName").value.toString()
                                    binding.include.tvService.text = facilitySnapshot.child("serviceList").child("0").value.toString()
                                    serviceLat = facilitySnapshot.child("latitude").value.toString().toDouble()
                                    serviceLng = facilitySnapshot.child("longitude").value.toString().toDouble()
                                    var serviceLocation = LatLng(serviceLat, serviceLng)
                                    markerList.add(MarkerOptions().position(serviceLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)).title("$pID"))

                                    markerList.distinct()
                                    placeList.distinct()
                                    for(i in 0 until markerList.size){
                                        mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                        mMap.addMarker(markerList[i])
                                    }
                                    /*markerList.groupBy { it.title }
                                    for(i in 0 until markerList.size - 1){
                                        if(markerList[i].title.toString() == pID){
                                            markerList.removeAt(i)
                                        }
                                        mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                        mMap.addMarker(markerList[i])
                                    }

                                    for(i in 0 until placeList.size - 1){
                                        if(placeList[i].id == pID){
                                            placeList.removeAt(i)
                                        }
                                    }*/

                                    hasMarker = true
                                }
                            }

                            else{
                                hasMarker = false
                                Toast.makeText(context, "Nothing", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                else{
                    Toast.makeText(context, "Nothing", Toast.LENGTH_LONG).show()
                }

                Log.i(TAG, "Place: ${markerList}")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    /*private fun compareTransportService(){
        markerList.clear()
        dbref = FirebaseDatabase.getInstance().getReference("AppFacilities")
        dbref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    for(facilitySnapshot in snapshot.children){
                        for(child in facilitySnapshot.child("serviceList").children){
                            if(child.value.toString().uppercase() == "BUS_STATION" || child.value.toString().uppercase() == "TRAIN_STATION" || child.value.toString().uppercase() == "TRANSIT_STATION" ||
                                child.value.toString().uppercase() == "TAXI_STAND" || child.value.toString().uppercase() == "SUBWAY_STATION" || child.value.toString().uppercase() == "GAS_STATION" ||
                                child.value.toString().uppercase() == "AIRPORT" || child.value.toString().uppercase() == "PARKING" || child.value.toString().uppercase() == "GAS_STATION"){ // need to categorize the category

                                pID = facilitySnapshot.child("id").value.toString()
                                placeList.add(facilitySnapshot.getValue(AppFacilities::class.java)!!)
                                binding.include.tvPlaceName.text = facilitySnapshot.child("locationName").value.toString()
                                binding.include.tvService.text = facilitySnapshot.child("serviceList").child("0").value.toString()
                                serviceLat = facilitySnapshot.child("latitude").value.toString().toDouble()
                                serviceLng = facilitySnapshot.child("longitude").value.toString().toDouble()
                                var serviceLocation = LatLng(serviceLat, serviceLng)
                                markerList.add(MarkerOptions().position(serviceLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title("$pID"))

                                markerList.distinct()
                                placeList.distinct()
                                for(i in 0 until markerList.size){
                                    mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                    mMap.addMarker(markerList[i])
                                }
                                *//*for(i in 0 until markerList.size - 1){
                                    if(markerList[i].title.toString() == pID){
                                        markerList.removeAt(i)
                                    }
                                    mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                    mMap.addMarker(markerList[i])

                                }

                                for(i in 0 until placeList.size - 1){
                                    if(placeList[i].id == pID){
                                        placeList.removeAt(i)
                                    }
                                }*//*

                                hasMarker = true
                            }
                        }
                    }
                    Log.i(TAG, "Place: ${markerList}")
                }
                else{
                    Toast.makeText(context, "Nothing", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun compareShoppingService(){
        markerList.clear()
        dbref = FirebaseDatabase.getInstance().getReference("AppFacilities")
        dbref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    for(facilitySnapshot in snapshot.children){
                        for(child in facilitySnapshot.child("serviceList").children){
                            if(child.value.toString().uppercase() == "CLOTHING_STORE" || child.value.toString().uppercase() == "CONVENIENCE_STORE" || child.value.toString().uppercase() == "DEPARTMENT_STORE" ||
                                child.value.toString().uppercase() == "ELECTRONIC_STORE" || child.value.toString().uppercase() == "FURNITURE_STORE" || child.value.toString().uppercase() == "HARDWARE_STORE" ||
                                child.value.toString().uppercase() == "HOME_GOODS_STORE" || child.value.toString().uppercase() == "JEWELRY_STORE" || child.value.toString().uppercase() == "LIQUOR_STORE" ||
                                child.value.toString().uppercase() == "PET_STORE" || child.value.toString().uppercase() == "SHOE_STORE" || child.value.toString().uppercase() == "SHOPPING_MALL" ||
                                child.value.toString().uppercase() == "STORE" || child.value.toString().uppercase() == "SUPERMARKET"){ // need to categorize the category

                                pID = facilitySnapshot.child("id").value.toString()
                                placeList.add(facilitySnapshot.getValue(AppFacilities::class.java)!!)
                                binding.include.tvPlaceName.text = facilitySnapshot.child("locationName").value.toString()
                                binding.include.tvService.text = facilitySnapshot.child("serviceList").child("0").value.toString()
                                serviceLat = facilitySnapshot.child("latitude").value.toString().toDouble()
                                serviceLng = facilitySnapshot.child("longitude").value.toString().toDouble()
                                var serviceLocation = LatLng(serviceLat, serviceLng)
                                markerList.add(MarkerOptions().position(serviceLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("$pID"))
                                markerList.groupBy { it.title }
                                *//*for(i in 0 until markerList.size - 1){
                                    if(markerList[i].title.toString() == pID){
                                        markerList.removeAt(i)
                                    }
                                    mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                    mMap.addMarker(markerList[i])
                                }*//*
                                markerList.distinct()
                                placeList.distinct()
                                for(i in 0 until markerList.size){
                                    mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                    mMap.addMarker(markerList[i])
                                }

                                *//*for(i in 0 until placeList.size - 1){
                                    if(placeList[i].id == pID){
                                        placeList.removeAt(i)
                                    }
                                }*//*
                                hasMarker = true
                            }
                        }
                    }
                    Log.i(TAG, "Place: ${markerList}")
                }
                else{
                    Toast.makeText(context, "Nothing", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun compareTourismService(){
        markerList.clear()
        dbref = FirebaseDatabase.getInstance().getReference("AppFacilities")
        dbref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    for(facilitySnapshot in snapshot.children){
                        for(child in facilitySnapshot.child("serviceList").children){
                            if(child.value.toString().uppercase() == "AIRPORT" || child.value.toString().uppercase() == "AMUSEMENT_PARK" || child.value.toString().uppercase() == "AQUARIUM" ||
                                child.value.toString().uppercase() == "ART_GALLERY" || child.value.toString().uppercase() == "LODGING" || child.value.toString().uppercase() == "MUSEUM" ||
                                child.value.toString().uppercase() == "TOURIST_ATTRACTION" || child.value.toString().uppercase() == "TRAVEL_AGENCY" || child.value.toString().uppercase() == "ZOO"){ // need to categorize the category

                                pID = facilitySnapshot.child("id").value.toString()
                                placeList.add(facilitySnapshot.getValue(AppFacilities::class.java)!!)
                                binding.include.tvPlaceName.text = facilitySnapshot.child("locationName").value.toString()
                                binding.include.tvService.text = facilitySnapshot.child("serviceList").child("0").value.toString()
                                serviceLat = facilitySnapshot.child("latitude").value.toString().toDouble()
                                serviceLng = facilitySnapshot.child("longitude").value.toString().toDouble()
                                var serviceLocation = LatLng(serviceLat, serviceLng)
                                markerList.add(MarkerOptions().position(serviceLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).title("$pID"))

                                markerList.distinct()
                                placeList.distinct()
                                for(i in 0 until markerList.size){
                                    mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                    mMap.addMarker(markerList[i])
                                }
                                *//*markerList.groupBy { it.title }
                                for(i in 0 until markerList.size - 1){
                                    if(markerList[i].title.toString() == pID){
                                        markerList.removeAt(i)
                                    }
                                    mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                    mMap.addMarker(markerList[i])
                                }

                                for(i in 0 until placeList.size - 1){
                                    if(placeList[i].id == pID){
                                        placeList.removeAt(i)
                                    }
                                }*//*

                                hasMarker = true
                            }
                        }
                    }
                    Log.i(TAG, "Place: ${markerList}")
                }
                else{
                    Toast.makeText(context, "Nothing", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun compareEducationService(){
        markerList.clear()
        dbref = FirebaseDatabase.getInstance().getReference("AppFacilities")
        dbref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    for(facilitySnapshot in snapshot.children){
                        for(child in facilitySnapshot.child("serviceList").children){
                            if(child.value.toString().uppercase() == "BOOK_STORE" || child.value.toString().uppercase() == "LIBRARY" || child.value.toString().uppercase() == "PRIMARY_SCHOOL" ||
                                child.value.toString().uppercase() == "SECONDARY_SCHOOL" || child.value.toString().uppercase() == "SCHOOL" || child.value.toString().uppercase() == "UNIVERSITY"){ // need to categorize the category

                                pID = facilitySnapshot.child("id").value.toString()
                                placeList.add(facilitySnapshot.getValue(AppFacilities::class.java)!!)
                                binding.include.tvPlaceName.text = facilitySnapshot.child("locationName").value.toString()
                                binding.include.tvService.text = facilitySnapshot.child("serviceList").child("0").value.toString()
                                serviceLat = facilitySnapshot.child("latitude").value.toString().toDouble()
                                serviceLng = facilitySnapshot.child("longitude").value.toString().toDouble()
                                var serviceLocation = LatLng(serviceLat, serviceLng)
                                markerList.add(MarkerOptions().position(serviceLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("$pID"))

                                markerList.distinct()
                                placeList.distinct()
                                for(i in 0 until markerList.size){
                                    mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                    mMap.addMarker(markerList[i])
                                }
                                *//*markerList.groupBy { it.title }
                                for(i in 0 until markerList.size - 1){
                                    if(markerList[i].title.toString() == pID){
                                        markerList.removeAt(i)
                                    }
                                    mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                    mMap.addMarker(markerList[i])
                                }

                                for(i in 0 until placeList.size - 1){
                                    if(placeList[i].id == pID){
                                        placeList.removeAt(i)
                                    }
                                }*//*

                                hasMarker = true
                            }
                        }
                    }
                    Log.i(TAG, "Place: ${markerList}")
                }
                else{
                    Toast.makeText(context, "Nothing", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun compareHealthService(){
        markerList.clear()
        dbref = FirebaseDatabase.getInstance().getReference("AppFacilities")
        dbref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    for(facilitySnapshot in snapshot.children){
                        for(child in facilitySnapshot.child("serviceList").children){
                            if(child.value.toString().uppercase() == "DENTIST" || child.value.toString().uppercase() == "DRUGSTORE" || child.value.toString().uppercase() == "HOSPITAL" ||
                                child.value.toString().uppercase() == "PHARMACY" || child.value.toString().uppercase() == "PHYSIOTHERAPIST" || child.value.toString().uppercase() == "VETERINARY_CARE" ||
                                child.value.toString().uppercase() == "HEALTH"){ // need to categorize the category

                                pID = facilitySnapshot.child("id").value.toString()
                                placeList.add(facilitySnapshot.getValue(AppFacilities::class.java)!!)
                                binding.include.tvPlaceName.text = facilitySnapshot.child("locationName").value.toString()
                                binding.include.tvService.text = facilitySnapshot.child("serviceList").child("0").value.toString()
                                serviceLat = facilitySnapshot.child("latitude").value.toString().toDouble()
                                serviceLng = facilitySnapshot.child("longitude").value.toString().toDouble()
                                var serviceLocation = LatLng(serviceLat, serviceLng)
                                markerList.add(MarkerOptions().position(serviceLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)).title("$pID"))

                                markerList.distinct()
                                placeList.distinct()
                                for(i in 0 until markerList.size){
                                    mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                    mMap.addMarker(markerList[i])
                                }
                                *//*markerList.groupBy { it.title }
                                for(i in 0 until markerList.size - 1){
                                    if(markerList[i].title.toString() == pID){
                                        markerList.removeAt(i)
                                    }
                                    mMap.addMarker(markerList[i]).tag = markerList[i].title.toString()
                                    mMap.addMarker(markerList[i])
                                }

                                for(i in 0 until placeList.size - 1){
                                    if(placeList[i].id == pID){
                                        placeList.removeAt(i)
                                    }
                                }*//*

                                hasMarker = true
                            }
                        }
                    }
                    Log.i(TAG, "Place: ${markerList}")
                }
                else{
                    Toast.makeText(context, "Nothing", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }*/

    override fun onLocationChanged(location: Location) {

    }

}