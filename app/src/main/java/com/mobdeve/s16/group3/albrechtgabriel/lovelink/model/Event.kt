package com.mobdeve.s16.group3.albrechtgabriel.lovelink.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Event(
    @DocumentId
    val id: String = "",       // Firestore document ID (_id)
    var eventName: String = "",
    var date: String = "",    // store as Date for Firestore compatibility
    var description: String = "",
    val timestamp: Date = Date() // Add this field
)
