package com.mobdeve.s16.group3.albrechtgabriel.lovelink.model

import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.UserController
import kotlinx.coroutines.tasks.await
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.TextStyle
import java.util.Date
import java.util.Locale

object ResidencyHoursController {

    private val db = FirebaseFirestore.getInstance()
    private val residencyCollection = db.collection(ResidencyHoursConstants.RESIDENCY_HOURS_COLLECTION)

    // Add new ongoing residency
    suspend fun createNewResidency(timeIn: Date, memberId: String): ResidencyHours? {
        return try {
            val newResidency = ResidencyHours(
                timeIn = timeIn,
                timeOut = Date(0), // null not allowed in Firestore, use epoch as placeholder
                memberId = memberId
            )

            val docRef = residencyCollection.add(newResidency).await()
            newResidency.copy(id = docRef.id).also {
                println("Successfully added an ongoing residency! Member: $memberId, Time In: $timeIn")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Get ongoing residency (timeOut == null / epoch)
    suspend fun getOngoingResidency(memberId: String): ResidencyHours? {
        return try {
            val snapshot = residencyCollection
                .whereEqualTo(ResidencyHoursConstants.MEMBER_ID_FIELD, memberId)
                .whereEqualTo(ResidencyHoursConstants.TIME_OUT_FIELD, Date(0))
                .get()
                .await()

            snapshot.documents.firstOrNull()?.toObject(ResidencyHours::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Close the ongoing residency
    suspend fun setOngoingResidency(residencyId: String): ResidencyHours? {
        return try {
            residencyCollection.document(residencyId)
                .update(ResidencyHoursConstants.TIME_OUT_FIELD, Date())
                .await()

            residencyCollection.document(residencyId)
                .get()
                .await()
                .toObject(ResidencyHours::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Get all completed residencies of a member
    suspend fun getMemberResidency(memberId: String): List<ResidencyHours> {
        return try {
            // 1. Fetch ALL logs for this user (Removed .whereNotEqualTo to avoid index error)
            val snapshot = residencyCollection
                .whereEqualTo(ResidencyHoursConstants.MEMBER_ID_FIELD, memberId)
                .get()
                .await()

            // 2. Map to objects and Filter in Kotlin
            snapshot.documents.mapNotNull { it.toObject(ResidencyHours::class.java) }
                .filter {
                    // Only keep records where timeOut is NOT the placeholder (0)
                    it.timeOut != null && it.timeOut.time > 0L
                }
                .sortedByDescending { it.timeIn } // Sort latest first

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Get latest residency
    suspend fun getLatestMemberResidency(memberId: String): ResidencyHours? {
        return try {
            // 1. Get ALL records for this user
            val snapshot = residencyCollection
                .whereEqualTo(ResidencyHoursConstants.MEMBER_ID_FIELD, memberId)
                .get()
                .await()

            // 2. Map to objects
            val allLogs = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ResidencyHours::class.java)
            }

            // 3. Filter in Kotlin:
            val latestCompleted = allLogs
                .sortedByDescending { it.timeIn } // Ensure latest is first
                .firstOrNull {
                    // Check if timeOut exists and is not the "ongoing" placeholder (0)
                    it.timeOut != null && it.timeOut.time > 0L
                }

            latestCompleted

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Compute total residency in seconds
    suspend fun computeTotalResidency(memberId: String): Long {
        return try {
            val records = getMemberResidency(memberId)
            records.sumOf { (it.timeOut.time - it.timeIn.time) / 1000 }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    // Update total residency for a list of members (pass member IDs)
    suspend fun updateTotalResidencyForMembers(memberIds: List<String>) {
        try {
            memberIds.forEach { memberId ->
                val totalSeconds = computeTotalResidency(memberId)

                val userSnapshot = UserController.getUserById(memberId)
                if (userSnapshot != null) {
                    db.collection(UserConstants.USERS_COLLECTION)
                        .document(memberId)
                        .update(UserConstants.TOTAL_RESIDENCY_TIME_FIELD, totalSeconds)
                        .await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Compute monthly residency for a member
    suspend fun computeMonthlyResidency(
        memberId: String,
        year: Int = 2025,
        months: List<String> = listOf("may", "june", "july")
    ): Map<String, String> {
        val logs = getMemberResidency(memberId)
        val monthlyResidency = mutableMapOf<String, Long>()

        for (log in logs) {
            val zdt: ZonedDateTime = Instant.ofEpochMilli(log.timeIn.time)
                .atZone(ZoneId.systemDefault())
            val logYear = zdt.year
            val logMonth = zdt.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase()

            if (logYear == year && months.contains(logMonth)) {
                val diff = log.timeOut.time - log.timeIn.time
                monthlyResidency[logMonth] = (monthlyResidency[logMonth] ?: 0) + diff
            }
        }

        // Format output
        return months.associateWith { month ->
            val totalMs = monthlyResidency[month] ?: 0
            val totalSec = totalMs / 1000
            val hours = totalSec / 3600
            val minutes = (totalSec % 3600) / 60
            "$hours hours and $minutes minutes"
        }
    }
}
