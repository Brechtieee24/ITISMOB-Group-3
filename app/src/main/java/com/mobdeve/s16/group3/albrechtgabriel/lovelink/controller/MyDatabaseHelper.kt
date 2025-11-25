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

    /**
     * Call this once on app first launch to seed initial data
     */
    fun seedDatabaseIfEmpty() {
        seedCollectionIfEmpty(usersRef) {
            val mockUser = User(
                id = "user1",
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com",
                committee = "Tech",
                isOfficer = true
            )
            it.document(mockUser.id).set(mockUser)
        }

        seedCollectionIfEmpty(eventsRef) {
            val mockEvent = Event(
                id = "event1",
                eventName = "Orientation Day",
                date = "2025-12-01"
            )
            it.document(mockEvent.id).set(mockEvent)
        }

        seedCollectionIfEmpty(residencyHoursRef) {
            val mockResidency = ResidencyHours(
                id = "res1",
                timeIn = Date(),
                timeOut = Date(),
                memberId = "user1"
            )
            it.document(mockResidency.id).set(mockResidency)
        }

        seedCollectionIfEmpty(activityParticipationRef) {
            val mockParticipation = ActivityParticipation(
                id = "part1",
                memberId = "user1",
                eventId = "event1"
            )
            it.document(mockParticipation.id).set(mockParticipation)
        }
    }

    // Generic helper function to seed mock data if a collection is empty
    private fun seedCollectionIfEmpty(
        collection: CollectionReference,
        seedAction: (CollectionReference) -> Unit
    ) {
        collection.limit(1).get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                seedAction(collection)
            }
        }
    }
}
