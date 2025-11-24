package com.mobdeve.s16.group3.albrechtgabriel.lovelink.model

import com.google.firebase.firestore.DocumentId

data class Event(
    @DocumentId
    val id: String = "",       // Firestore document ID (_id)
    var eventName: String = "",
    var date: String = ""    // store as Date for Firestore compatibility
)
