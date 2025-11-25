package com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller

import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.User
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.UserConstants
import kotlinx.coroutines.tasks.await
import java.util.Date

object UserController {

    private val db get() = FirebaseFirestore.getInstance()
    private val usersCollection get() = db.collection(UserConstants.USERS_COLLECTION)

    // Get user by email and update last login
    suspend fun getUserByEmail(email: String): User? = try {
        val snapshot = usersCollection
            .whereEqualTo(UserConstants.EMAIL_FIELD, email)
            .get()
            .await()

        val user = snapshot.documents.firstOrNull()?.toObject(User::class.java) ?: return null

        // Update last login
        usersCollection.document(user.id)
            .update(UserConstants.LAST_LOGIN_FIELD, Date())
            .await()

        user
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    // Get user by ID
    suspend fun getUserById(userId: String): User? = try {
        usersCollection.document(userId).get().await().toObject(User::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    // Update about info
    suspend fun updateAboutInfo(email: String, aboutInfo: String): User? = try {
        val snapshot = usersCollection.whereEqualTo(UserConstants.EMAIL_FIELD, email)
            .get().await()
        val doc = snapshot.documents.firstOrNull() ?: return null
        doc.reference.update(UserConstants.ABOUT_INFO_FIELD, aboutInfo).await()
        doc.toObject(User::class.java)?.apply { this.aboutInfo = aboutInfo }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    // Get about info for a user
    suspend fun userAboutInfo(userId: String): User? = getUserById(userId)

    // Filter members by committee
    suspend fun filterByCommittee(committeeName: String): List<User> = try {
        usersCollection
            .whereEqualTo(UserConstants.COMMITTEE_FIELD, committeeName)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(User::class.java) }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
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
                usersCollection.document(member.id)
                    .update(UserConstants.FORMATTED_RESIDENCY_TIME_FIELD, formatted)
                    .await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    // Update profile picture
    suspend fun updateUserProfilePic(email: String, imageUrl: String): User? = try {
        val snapshot = usersCollection.whereEqualTo(UserConstants.EMAIL_FIELD, email)
            .get().await()
        val doc = snapshot.documents.firstOrNull() ?: return null
        doc.reference.update("photo", imageUrl).await()
        doc.toObject(User::class.java)?.apply { this.id = doc.id }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
