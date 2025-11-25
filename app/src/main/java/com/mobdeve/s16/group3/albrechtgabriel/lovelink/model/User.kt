package com.mobdeve.s16.group3.albrechtgabriel.lovelink.model

import java.util.Date

data class User(
    val firstName: String = "",
    val lastName: String = "",
    @get:JvmName("getEmail") val email: String = "",

    var committee: String = "",
    var isOfficer: Boolean = false,
    var aboutInfo: String = "Hello",
    var lastLogin: Date = Date(),
    var formattedResidencyTime: String = "",
    var totalResidencyTime: Long = 0
)

