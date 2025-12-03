package com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller

import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.User
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.UserConstants
import kotlinx.coroutines.tasks.await
import java.util.Date

object UserController {

    private val db get() = FirebaseFirestore.getInstance()
    private val usersCollection get() = db.collection(UserConstants.USERS_COLLECTION)

    // Get user by email (now using query instead of doc ID)
    suspend fun getUserByEmail(email: String): User? {
        return try {
            val snapshot = usersCollection
                .whereEqualTo(UserConstants.EMAIL_FIELD, email)
                .get()
                .await()

            if (snapshot.isEmpty) return null

            val doc = snapshot.documents[0]

            // Update last login
            doc.reference.update(UserConstants.LAST_LOGIN_FIELD, Date()).await()

            doc.toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Get user by doc ID
    suspend fun getUserById(docId: String): User? {
        return try {
            usersCollection.document(docId).get().await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Update about info
    suspend fun updateAboutInfo(email: String, aboutInfo: String): User? {
        return try {
            val snapshot = usersCollection
                .whereEqualTo(UserConstants.EMAIL_FIELD, email)
                .get()
                .await()

            if (snapshot.isEmpty) return null

            val doc = snapshot.documents[0]
            doc.reference.update(UserConstants.ABOUT_INFO_FIELD, aboutInfo).await()

            doc.toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Update profile picture
    suspend fun updateUserProfilePic(email: String, imageUrl: String): User? {
        return try {
            val snapshot = usersCollection
                .whereEqualTo(UserConstants.EMAIL_FIELD, email)
                .get()
                .await()

            if (snapshot.isEmpty) return null

            val doc = snapshot.documents[0]
            doc.reference.update("photo", imageUrl).await()

            doc.toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Filter members by committee
    suspend fun filterByCommittee(committeeName: String): List<User> {
        return try {
            usersCollection
                .whereEqualTo(UserConstants.COMMITTEE_FIELD, committeeName)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(User::class.java) }
                .distinctBy { it.email }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Filter members by committee and minimum hours
    suspend fun filterByCommitteeAndHour(committeeName: String, hours: Int): List<User> {
        val minSeconds = hours * 3600
        return try {
            filterByCommittee(committeeName)
                .filter { it.totalResidencyTime >= minSeconds }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Update formatted residency for all members of a committee
    suspend fun updateFormattedResidency(committee: String) {
        try {
            val members = filterByCommittee(committee)

            members.forEach { member ->
                val totalSeconds = member.totalResidencyTime.toInt()
                val formatted = String.format(
                    "%02d:%02d:%02d",
                    totalSeconds / 3600,
                    (totalSeconds % 3600) / 60,
                    totalSeconds % 60
                )

                // Query the document by email
                val snapshot = usersCollection
                    .whereEqualTo(UserConstants.EMAIL_FIELD, member.email)
                    .get()
                    .await()

                // Check if the query returned any documents
                if (snapshot.documents.isNotEmpty()) {
                    val docRef = snapshot.documents[0].reference
                    docRef.update(UserConstants.FORMATTED_RESIDENCY_TIME_FIELD, formatted)
                        .await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
