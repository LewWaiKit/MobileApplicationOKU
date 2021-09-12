package com.example.mobileapplicationoku.dataClass

import com.google.android.libraries.places.api.model.Place

data class Facilities(
    var facilityID: String? = null,
    var placeID: String? = null,
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val serviceList: MutableList<Place.Type>? = null,
    val description: String? = null,
    val status: String? = null
)
