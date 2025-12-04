package com.mobdeve.s16.group3.albrechtgabriel.lovelink.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class User(
    @DocumentId            // 2. Add this annotation
    val id: String = "",

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

