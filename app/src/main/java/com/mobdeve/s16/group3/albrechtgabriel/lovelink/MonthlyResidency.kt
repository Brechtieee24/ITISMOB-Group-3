package com.mobdeve.s16.group3.albrechtgabriel.lovelink

data class MonthlyResidency(
    val month: String,      // e.g., "Oct", "Sep", "Aug"
    val totalMinutes: Int
) {
    // Convert total minutes to "X hours and Y minutes" format
    fun getFormattedTime(): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "$month: $hours hours and $minutes minutes"
    }
}