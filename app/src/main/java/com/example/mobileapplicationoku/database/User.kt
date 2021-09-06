package com.example.mobileapplicationoku.database

import java.util.*

data class User(
    val userID: String?=null,
    val firstName: String?=null,
    val lastName: String?=null,
    val nRIC: String?=null,
    val email: String?=null,
    val contactNo: String?=null,
    val age: Int?=null,
    val type: String?=null,
    val oKUCardNo: String?=null
)
