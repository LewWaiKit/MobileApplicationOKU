package com.example.mobileapplicationoku.dataClass

import com.google.android.libraries.places.api.model.Place

data class AppFacilities(
    var id: String? = null,
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val serviceList: MutableList<Place.Type>? = null,
    val description: String? = null
)