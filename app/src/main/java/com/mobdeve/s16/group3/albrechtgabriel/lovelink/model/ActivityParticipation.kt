package com.mobdeve.s16.group3.albrechtgabriel.lovelink.model

import com.google.firebase.firestore.DocumentId

data class ActivityParticipation(
    @DocumentId
    val id: String = "",       // Firestore document ID (_id)
    val memberId: String = "", // reference to User ID
    val eventId: String = ""   // reference to Event ID
)
