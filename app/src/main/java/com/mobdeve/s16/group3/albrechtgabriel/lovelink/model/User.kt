package com.mobdeve.s16.group3.albrechtgabriel.lovelink.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class User(
    @DocumentId
    val id: String = "",           // Firestore document ID (_id)
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    var committee: String = "",
    var isOfficer: Boolean = false,
    var aboutInfo: String = "Hello",
    var lastLogin: Date = Date(),   // current time if not provided
    var formattedResidencyTime: String = "",  // "00:18:42"
    var totalResidencyTime: Long = 0          // total time in seconds (1122)
)
