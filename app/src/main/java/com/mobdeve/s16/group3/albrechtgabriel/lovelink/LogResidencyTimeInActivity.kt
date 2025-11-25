package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.LogResidencyTimeinBinding
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.geofencing.GeofenceManager

class LogResidencyTimeInActivity : AppCompatActivity() {
    private lateinit var binding: LogResidencyTimeinBinding
    private lateinit var geofenceManager: GeofenceManager

    // Permission launcher for foreground + background location
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false
        else true

        if (fineLocation && backgroundLocation) {
            setupGeofence()
        } else {
            Toast.makeText(this, "Location permissions are required for geofencing", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LogResidencyTimeinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        geofenceManager = GeofenceManager(this)
        checkPermissionsAndStartGeofence()

        // Residency History button
        binding.residencyHistoryBtn.setOnClickListener {
            val intent = android.content.Intent(this, ResidencyHistoryActivity::class.java)
            intent.putExtra("CALLER_ACTIVITY", "LogResidencyTimeInActivity")
            startActivity(intent)
        }

        // Activity History button
        binding.activityHistoryBtn.setOnClickListener {
            val intent = android.content.Intent(this, ActivityHistoryActivity::class.java)
            intent.putExtra("CALLER_ACTIVITY", "LogResidencyTimeInActivity")
            startActivity(intent)
        }

        // Return button - goes back to home
        binding.returnBtn.setOnClickListener {
            val intent = android.content.Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkPermissionsAndStartGeofence() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        val notGranted = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            locationPermissionLauncher.launch(notGranted.toTypedArray())
        } else {
            setupGeofence()
        }
    }

    private fun setupGeofence() {
        // Create geofence at HX7V+H8 Manila, 80m radius
        val geofence = Geofence.Builder()
            .setRequestId("HX7V_H8_Manila")
            .setCircularRegion(
                14.6042, // latitude
                120.9822, // longitude
                80f       // radius in meters
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        // Get PendingIntent from GeofenceManager
        val pendingIntent = geofenceManager.getGeofencePendingIntent()

        // Add the geofence
        geofenceManager.addGeofence(geofence, pendingIntent)
    }


}