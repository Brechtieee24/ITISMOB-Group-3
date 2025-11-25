package com.mobdeve.s16.group3.albrechtgabriel.lovelink.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object ActivityParticipationController {

    private val db = FirebaseFirestore.getInstance()
    private val participationCollection =
        db.collection(ActivityParticipationConstants.ACTIVITY_PARTICIPATION_COLLECTION)
    private val eventsCollection = db.collection(EventConstants.EVENTS_COLLECTION)

    // Add a new event participation
    suspend fun addEventParticipation(memberId: String, eventId: String): ActivityParticipation? {
        return try {
            val newParticipation = ActivityParticipation(
                memberId = memberId,
                eventId = eventId
            )

            // Add to Firestore
            val docRef = participationCollection.add(newParticipation).await()

            // Return the object with generated Firestore ID
            newParticipation.copy(id = docRef.id).also {
                println("Successfully added event participation! Member: $memberId, Event: $eventId")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Get all events a user has participated in
    suspend fun getEventsOfUser(memberId: String): List<Event> {
        return try {
            // Query all participations for the member using constant
            val snapshot = participationCollection
                .whereEqualTo(ActivityParticipationConstants.MEMBER_ID_FIELD, memberId)
                .get()
                .await()

            val participations = snapshot.documents.mapNotNull {
                it.toObject(ActivityParticipation::class.java)
            }

            // Fetch the corresponding Event objects using EVENT_ID_FIELD constant
            val events = participations.mapNotNull { participation ->
                eventsCollection.document(participation.eventId).get().await().toObject(Event::class.java)
            }

            events
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
