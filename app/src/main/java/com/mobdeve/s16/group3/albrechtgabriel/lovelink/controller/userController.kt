package com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller

import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.User
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.UserConstants
import kotlinx.coroutines.tasks.await
import java.util.Date

object UserController {

    private val db get() = FirebaseFirestore.getInstance()
    private val usersCollection get() = db.collection(UserConstants.USERS_COLLECTION)

    // ✅ Get user by email (now docId) and update last login
    suspend fun getUserByEmail(email: String): User? {
        return try {
            val docRef = usersCollection.document(email)
            val doc = docRef.get().await()

            if (!doc.exists()) return null

            docRef.update(UserConstants.LAST_LOGIN_FIELD, Date()).await()

            doc.toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ✅ Get user by ID (same as email now)
    suspend fun getUserById(email: String): User? {
        return try {
            usersCollection.document(email).get().await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ✅ Update about info
    suspend fun updateAboutInfo(email: String, aboutInfo: String): User? {
        return try {
            val docRef = usersCollection.document(email)
            val doc = docRef.get().await()

            if (!doc.exists()) return null

            docRef.update(UserConstants.ABOUT_INFO_FIELD, aboutInfo).await()

            doc.toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ✅ Get about info for a user
    suspend fun userAboutInfo(email: String): User? {
        return getUserById(email)
    }

    // ✅ Filter members by committee
    suspend fun filterByCommittee(committeeName: String): List<User> {
        return try {
            usersCollection
                .whereEqualTo(UserConstants.COMMITTEE_FIELD, committeeName)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(User::class.java) }
                .distinctBy { it.email } // This removes duplicates based on email
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ✅ Update formatted residency for all members of a committee
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

                usersCollection
                    .document(member.email)  // use email instead of id
                    .update(UserConstants.FORMATTED_RESIDENCY_TIME_FIELD, formatted)
                    .await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ✅ Filter members by committee and minimum hours
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

    // ✅ Update profile picture
    suspend fun updateUserProfilePic(email: String, imageUrl: String): User? {
        return try {
            val docRef = usersCollection.document(email)
            val doc = docRef.get().await()

            if (!doc.exists()) return null

            docRef.update("photo", imageUrl).await()

            doc.toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
