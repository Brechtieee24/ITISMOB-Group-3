package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.User
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.UserConstants
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object UserSeeder {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection(UserConstants.USERS_COLLECTION)

    suspend fun seedUsersFromJson(context: Context, jsonFileName: String) {
        try {
            // Read JSON from assets
            val jsonString = context.assets.open(jsonFileName)
                .bufferedReader()
                .use { it.readText() }

            val gson = Gson()
            val listType = object : TypeToken<List<UserJson>>() {}.type
            val users = gson.fromJson<List<UserJson>>(jsonString, listType)

            users.forEach { userJson ->
                val lastLoginDate = parseDate(userJson.lastLogin) ?: Date()
                val user = User(
                    firstName = userJson.firstName,
                    lastName = userJson.lastName,
                    email = userJson.email,
                    committee = userJson.committee ?: "",
                    isOfficer = userJson.isOfficer ?: false,
                    aboutInfo = userJson.aboutInfo ?: "Hello",
                    lastLogin = lastLoginDate,
                    formattedResidencyTime = userJson.formattedResidencyTime ?: "",
                    totalResidencyTime = userJson.totalResidencyTime ?: 0,
                )

                usersCollection.add(user).await()
            }

            println("✅ All users uploaded successfully!")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun migrateUsersToEmailId() {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        val snapshot = usersCollection.get().await()

        for (doc in snapshot.documents) {
            val user = doc.toObject(User::class.java) ?: continue
            val email = user.email.trim().lowercase()

            if (email.isBlank()) continue

            val newDocRef = usersCollection.document(email)

            // Prevent overwrite
            if (!newDocRef.get().await().exists()) {
                newDocRef.set(user).await()
            }

            // Delete old doc if different
            if (doc.id != email) {
                doc.reference.delete().await()
            }
        }

        println("Migration done!")
    }



    private fun parseDate(jsonDate: Map<String, String>?): Date? {
        return try {
            val dateString = jsonDate?.get("\$date") ?: return null
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    private data class UserJson(
        val firstName: String,
        val lastName: String,
        val email: String,
        val committee: String?,
        val isOfficer: Boolean?,
        val aboutInfo: String?,
        val lastLogin: Map<String, String>?,
        val formattedResidencyTime: String?,
        val totalResidencyTime: Long?,
        val photo: String?,
        val cloudinaryId: String?
    )
}
