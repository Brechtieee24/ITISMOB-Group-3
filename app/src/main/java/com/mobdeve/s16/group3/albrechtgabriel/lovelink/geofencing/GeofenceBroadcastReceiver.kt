package com.mobdeve.s16.group3.albrechtgabriel.lovelink.geofencing

import com.mobdeve.s16.group3.albrechtgabriel.lovelink.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.os.Build
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            Log.e("GEOFENCE", "Error code: ${geofencingEvent.errorCode}")
            return
        }

        val transition = geofencingEvent.geofenceTransition

        when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                sendGeofenceStatus(context, true)
                showNotification(context, "You have ENTERED the geofence")
            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                sendGeofenceStatus(context, false)
                showNotification(context, "You have EXITED the geofence")
            }

            else -> {
                Log.d("GEOFENCE", "Unknown geofence transition: $transition")
            }
        }
    }

    private fun sendGeofenceStatus(context: Context, inside: Boolean) {
        val intent = Intent("GEOFENCE_STATUS")
        intent.putExtra("inside", inside)
        context.sendBroadcast(intent)
    }

    private fun showNotification(context: Context, message: String) {
        val channelId = "geofence_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Geofence Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Notification disabled for now
        /*
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_dialog_map)
            .setContentTitle("Geofence Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(0, notification)
        */
    }
}
