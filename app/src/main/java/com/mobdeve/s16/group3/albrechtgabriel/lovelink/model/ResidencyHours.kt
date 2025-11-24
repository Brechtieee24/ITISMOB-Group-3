package com.mobdeve.s16.group3.albrechtgabriel.lovelink.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class ResidencyHours(
    @DocumentId
    val id: String = "",        // Firestore document ID (_id)
    val timeIn: Date = Date(),  // start time
    val timeOut: Date = Date(), // end time
    val memberId: String = ""   // reference to User ID
)
