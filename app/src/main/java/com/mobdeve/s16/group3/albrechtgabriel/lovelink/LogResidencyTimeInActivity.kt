package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.LogResidencyTimeinBinding
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.geofencing.GeofenceManager

class LogResidencyTimeInActivity : AppCompatActivity() {

    private lateinit var binding: LogResidencyTimeinBinding
    private lateinit var geofenceManager: GeofenceManager

    // Status if user is inside the 80m area
    private var isInsideGeofence = false

    // BroadcastReceiver to get geofence enter/exit updates
    private val geofenceStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val inside = intent?.getBooleanExtra("inside", false) ?: false
            isInsideGeofence = inside

            if (inside) {
                Toast.makeText(this@LogResidencyTimeInActivity,
                    "You are INSIDE the residency area.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@LogResidencyTimeInActivity,
                    "You are OUTSIDE the residency area.", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        Log.d("SESSION", "User session: ${getSharedPreferences("prefs", MODE_PRIVATE).getString("user_id", "none")}")

        super.onCreate(savedInstanceState)
        binding = LogResidencyTimeinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        geofenceManager = GeofenceManager(this)
        checkPermissionsAndStartGeofence()

        // REGISTER BROADCAST RECEIVER
        registerReceiver(geofenceStatusReceiver, IntentFilter("GEOFENCE_STATUS"))

        // TIME IN BUTTON
        binding.timeInBtn.setOnClickListener {
            if (isInsideGeofence) {
                Toast.makeText(this, "Time In SUCCESSFUL!", Toast.LENGTH_SHORT).show()
                // Perform time-in logic here
            } else {
                Toast.makeText(this, "You must be inside the office to Time In!", Toast.LENGTH_LONG).show()
            }
        }

        // Residency History button
        binding.residencyHistoryBtn.setOnClickListener {
            val intent = Intent(this, ResidencyHistoryActivity::class.java)
            intent.putExtra("CALLER_ACTIVITY", "LogResidencyTimeInActivity")
            startActivity(intent)
        }

        // Activity History button
        binding.activityHistoryBtn.setOnClickListener {
            val intent = Intent(this, ActivityHistoryActivity::class.java)
            intent.putExtra("CALLER_ACTIVITY", "LogResidencyTimeInActivity")
            startActivity(intent)
        }

        // Return button
        binding.returnBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(geofenceStatusReceiver)
    }

    private fun checkPermissionsAndStartGeofence() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

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
        val geofence = Geofence.Builder()
            .setRequestId("HX7V_H8_Manila")
            .setCircularRegion(
                14.60355,     // latitude
                120.98227,    // longitude
                80f           // radius in meters
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .build()

        val pendingIntent = geofenceManager.getGeofencePendingIntent()
        geofenceManager.addGeofence(geofence, pendingIntent)
    }
}
