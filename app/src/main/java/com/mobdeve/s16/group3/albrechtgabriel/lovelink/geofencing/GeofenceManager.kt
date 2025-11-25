package com.mobdeve.s16.group3.albrechtgabriel.lovelink.geofencing

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices

class GeofenceManager(private val context: Context) {

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    /**
     * Adds a geofence using the provided Geofence object and PendingIntent.
     * Make sure location permissions are granted before calling this method.
     */
    fun addGeofence(geofence: Geofence, pendingIntent: PendingIntent) {
        val request = GeofenceHelper.createGeofencingRequest(geofence)

        // Check for foreground and background location permissions
        val hasForeground = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasBackground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        if (hasForeground && hasBackground) {
            geofencingClient.addGeofences(request, pendingIntent)
                .addOnSuccessListener {
                    Log.d("GEOFENCE", "Geofence added successfully: ${geofence.requestId}")
                }
                .addOnFailureListener { e ->
                    Log.e("GEOFENCE", "Failed to add geofence: ${e.message}")
                }
        } else {
            Log.e("GEOFENCE", "Location permissions not granted")
        }
    }

    /**
     * Removes geofences associated with the given PendingIntent
     */
    fun removeGeofence(pendingIntent: PendingIntent) {
        geofencingClient.removeGeofences(pendingIntent)
            .addOnSuccessListener { Log.d("GEOFENCE", "Geofence removed") }
            .addOnFailureListener { e -> Log.e("GEOFENCE", "Failed to remove geofence: ${e.message}") }
    }

    /**
     * Creates and returns a PendingIntent for the GeofenceBroadcastReceiver
     */
    fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}
