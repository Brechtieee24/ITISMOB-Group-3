// HINDI NA ATA NEED ITO
package com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.*
import java.util.Date

object MyDatabaseHelper {

    // Firestore and Storage instances
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storageReference: StorageReference by lazy { FirebaseStorage.getInstance().reference }

    // Collection references
    val usersRef: CollectionReference
        get() = firestoreInstance.collection(UserConstants.USERS_COLLECTION)
    val eventsRef: CollectionReference
        get() = firestoreInstance.collection(EventConstants.EVENTS_COLLECTION)
    val residencyHoursRef: CollectionReference
        get() = firestoreInstance.collection(ResidencyHoursConstants.RESIDENCY_HOURS_COLLECTION)
    val activityParticipationRef: CollectionReference
        get() = firestoreInstance.collection(ActivityParticipationConstants.ACTIVITY_PARTICIPATION_COLLECTION)

    // Helper functions for document references
    fun getUserDoc(userId: String): DocumentReference = usersRef.document(userId)
    fun getEventDoc(eventId: String): DocumentReference = eventsRef.document(eventId)
    fun getResidencyHoursDoc(residencyId: String): DocumentReference = residencyHoursRef.document(residencyId)
    fun getActivityParticipationDoc(participationId: String): DocumentReference = activityParticipationRef.document(participationId)

    // Optional: Storage reference for images
    fun getStorageRef(path: String): StorageReference = storageReference.child(path)
}
