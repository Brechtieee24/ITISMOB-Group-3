package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Context
import android.content.SharedPreferences

object UserPreferences {
    private const val PREF_NAME = "LOVELink_Prefs"
    private const val KEY_IS_OFFICER = "is_officer"
    private const val KEY_USER_EMAIL = "user_email"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Save officer status
    fun setOfficerStatus(context: Context, isOfficer: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_IS_OFFICER, isOfficer).apply()
    }

    // Get officer status
    fun isOfficer(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_OFFICER, false)
    }
    fun setUserEmail(context: Context, email: String) {
        getPreferences(context).edit().putString(KEY_USER_EMAIL, email).apply()
    }
    fun getUserEmail(context: Context): String? {
        return getPreferences(context).getString(KEY_USER_EMAIL, null)
    }

    // Clear all preferences (on logout)
    fun clearAll(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}