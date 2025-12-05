package com.mobdeve.s16.group3.albrechtgabriel.lovelink.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

object ResidencySeeder {

    private val db = FirebaseFirestore.getInstance()
    private val residencyCollection = db.collection(ResidencyHoursConstants.RESIDENCY_HOURS_COLLECTION)

    /**
     * Seeds residency hours for three test users across multiple months
     * Call this from MainActivity or HomeActivity ONCE, then comment out
     */
    suspend fun seedResidencyHours() {
        try {
            println("Starting residency seeding...")

            // User IDs from your Firebase screenshots
            val users = mapOf(
                "BBSyxn8W3cxaoo6CCRhe" to "Maria Angelica",
                "8vamSuRr4HfBSy1Iiu2a" to "Josh",
                "utHqH92KVJjKQjm5MhEZ" to "Albrecht"
            )

            val userSessions = mapOf(
                "BBSyxn8W3cxaoo6CCRhe" to listOf(
                    // October 2025
                    Triple(2025, Calendar.OCTOBER, listOf(1, 3, 5, 8, 10, 12, 15, 17, 19, 22, 24, 26, 29)),
                    // November 2025
                    Triple(2025, Calendar.NOVEMBER, listOf(2, 5, 7, 9, 12, 14, 16, 19, 21, 23, 26, 28)),
                    // December 2025 (current month)
                    Triple(2025, Calendar.DECEMBER, listOf(1, 3, 5))
                ),
                "8vamSuRr4HfBSy1Iiu2a" to listOf(
                    Triple(2025, Calendar.OCTOBER, listOf(2, 4, 6, 9, 11, 13, 16, 18, 20, 23, 25, 27, 30)),
                    Triple(2025, Calendar.NOVEMBER, listOf(1, 4, 6, 8, 11, 13, 15, 18, 20, 22, 25, 27, 29)),
                    Triple(2025, Calendar.DECEMBER, listOf(2, 4))
                ),
                "utHqH92KVJjKQjm5MhEZ" to listOf(
                    Triple(2025, Calendar.OCTOBER, listOf(1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 15, 16, 17, 18, 19, 22, 23, 24)),
                    Triple(2025, Calendar.NOVEMBER, listOf(1, 2, 5, 6, 8, 9, 12, 13, 15, 16, 19, 20, 22, 23, 26, 27, 29, 30)),
                    Triple(2025, Calendar.DECEMBER, listOf(1, 2, 3, 4, 5))
                )
            )

            val calendar = Calendar.getInstance()
            var totalEntriesAdded = 0

            userSessions.forEach { (userId, monthsData) ->
                val userName = users[userId] ?: "Unknown"
                println("Seeding residency for: $userName")

                monthsData.forEach { (year, month, days) ->
                    days.forEach { day ->
                        // Set check-in time (morning: 8-10 AM)
                        calendar.set(year, month, day, (8..10).random(), (0..59).random(), 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        val timeIn = calendar.time

                        // Set check-out time (5-8 hours later)
                        calendar.add(Calendar.HOUR_OF_DAY, (5..8).random())
                        calendar.set(Calendar.MINUTE, (0..59).random())
                        val timeOut = calendar.time

                        val residencyHours = ResidencyHours(
                            memberId = userId,
                            timeIn = timeIn,
                            timeOut = timeOut
                        )

                        // Convert to map for Firestore
                        val residencyData = hashMapOf(
                            ResidencyHoursConstants.MEMBER_ID_FIELD to residencyHours.memberId,
                            ResidencyHoursConstants.TIME_IN_FIELD to residencyHours.timeIn,
                            ResidencyHoursConstants.TIME_OUT_FIELD to residencyHours.timeOut
                        )

                        residencyCollection.add(residencyData).await()
                        totalEntriesAdded++

                        println("  ✓ $userName: $day/${month + 1}/$year")
                    }
                }
            }

            println("Residency seeding complete! Added $totalEntriesAdded entries.")

        } catch (e: Exception) {
            println("Error seeding residency hours: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Seeds a single test entry for quick debugging
     */
    suspend fun seedTestEntry() {
        try {
            val calendar = Calendar.getInstance()
            calendar.set(2025, Calendar.DECEMBER, 5, 9, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val timeIn = calendar.time

            calendar.set(2025, Calendar.DECEMBER, 5, 17, 0, 0)
            val timeOut = calendar.time

            val residencyData = hashMapOf(
                ResidencyHoursConstants.MEMBER_ID_FIELD to "BBSyxn8W3cxaoo6CCRhe",
                ResidencyHoursConstants.TIME_IN_FIELD to timeIn,
                ResidencyHoursConstants.TIME_OUT_FIELD to timeOut
            )

            residencyCollection.add(residencyData).await()
            println("Test entry added successfully")

        } catch (e: Exception) {
            println("Error adding test entry: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Clears all residency hours (use with caution!)
     */
    suspend fun clearAllResidencyHours() {
        try {
            println("Clearing all residency hours...")
            val snapshot = residencyCollection.get().await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            println("Cleared ${snapshot.documents.size} residency entries.")
        } catch (e: Exception) {
            println("Error clearing residency hours: ${e.message}")
            e.printStackTrace()
        }
    }
}