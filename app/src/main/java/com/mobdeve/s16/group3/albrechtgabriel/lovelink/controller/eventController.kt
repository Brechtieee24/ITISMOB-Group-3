package com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller

import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.Event
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.EventConstants
import kotlinx.coroutines.tasks.await

object EventController {

    // Get collection reference when needed, no static FirebaseFirestore field
    private fun eventsCollection() = FirebaseFirestore.getInstance().collection(EventConstants.EVENTS_COLLECTION)

    // Add a new event
    suspend fun addEvent(eventName: String, date: String, description: String = ""): Boolean {
        return try {
            val event = Event(eventName = eventName, date = date, description = description)
            eventsCollection()
                .add(event)
                .await()  // suspend until complete
            println("Successfully added a new event")
            true
        } catch (e: Exception) {
            println("Error adding event: ${e.message}")
            false
        }
    }

    // Get all events
    suspend fun getEvents(): List<Event> {
        return try {
            val snapshot = eventsCollection()
                .orderBy("eventName")
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
        } catch (e: Exception) {
            println("Error fetching events: ${e.message}")
            emptyList()
        }
    }

    // Optional: get single event by ID
    suspend fun getEventById(eventId: String): Event? {
        return try {
            val snapshot = eventsCollection()
                .document(eventId)
                .get()
                .await()
            snapshot.toObject(Event::class.java)
        } catch (e: Exception) {
            println("Error fetching event by ID: ${e.message}")
            null
        }
    }

    suspend fun updateEventDescription(eventId: String, newDescription: String): Boolean {
        return try {
            eventsCollection().document(eventId)
                .update("description", newDescription)
                .await()
            println("Successfully updated event description")
            true
        } catch (e: Exception) {
            println("Error updating event description: ${e.message}")
            false
        }
    }
}
