package com.mobdeve.s16.group3.albrechtgabriel.lovelink.geofencing

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest

object GeofenceHelper {
    fun createGeofence(id: String, lat: Double, lng: Double, radius: Float, transitionTypes: Int): Geofence {
        return Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(lat, lng, radius)
            .setTransitionTypes(transitionTypes)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    fun createGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }
}
