package com.example.mobileapplicationoku.dataClass

data class EventData(
    val ID :String ?=null,
    val name :String ?=null,
    val destination :String ?=null,
    val date :String ?=null,
    val location :String ?=null,
    val time :String ?=null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

