package com.example.mobileapplicationoku.dataClass

data class AppFacilities(
    var id: String? = null,
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val serviceList: MutableList<String>? = null,
    val description: String? = null
)