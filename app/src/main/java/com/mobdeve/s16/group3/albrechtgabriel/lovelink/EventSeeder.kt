package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.Event
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.EventConstants
import kotlinx.coroutines.tasks.await

object EventSeeder {

    private val db get() = FirebaseFirestore.getInstance()
    private val eventsCollection get() = db.collection(EventConstants.EVENTS_COLLECTION)

    suspend fun seedEventsFromJson(context: Context, jsonFileName: String) {
        try {
            // Read JSON file from assets
            val jsonString = context.assets.open(jsonFileName)
                .bufferedReader()
                .use { it.readText() }

            val gson = Gson()
            val listType = object : TypeToken<List<EventJson>>() {}.type
            val events = gson.fromJson<List<EventJson>>(jsonString, listType)

            events.forEach { eventJson ->
                val event = Event(
                    eventName = eventJson.eventName ?: "",
                    date = eventJson.date ?: ""
                )
                eventsCollection.add(event).await()
            }

            println("✅ All events uploaded successfully!")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private data class EventJson(
        val eventName: String?,
        val date: String? // store as string compatible with Firestore
    )
}
