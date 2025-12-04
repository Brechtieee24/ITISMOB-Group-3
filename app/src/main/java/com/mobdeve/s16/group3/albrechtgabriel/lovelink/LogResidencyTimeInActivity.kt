package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat // REQUIRED IMPORT
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.Geofence
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.LogResidencyTimeinBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.geofencing.GeofenceManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ResidencyHoursController
import kotlinx.coroutines.launch
import java.util.Date
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
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
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val userId = prefs.getString("user_id", null)
        Log.d("SESSION", "User session: $userId")

        binding = LogResidencyTimeinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        geofenceManager = GeofenceManager(this)
        checkPermissionsAndStartGeofence()

        // --- REGISTER BROADCAST RECEIVER (FIXED FOR ANDROID 14) ---
        // Using ContextCompat handles the API version check automatically.
        // RECEIVER_NOT_EXPORTED ensures only YOUR app can send this broadcast.
        ContextCompat.registerReceiver(
            this,
            geofenceStatusReceiver,
            IntentFilter("GEOFENCE_STATUS"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Generate QR
        if (userId != null) {
            val qrBitmap = generateQrCode(userId)
            if (qrBitmap != null) {
                binding.logResidencyQrHolderIv.setImageBitmap(qrBitmap)
            }
        }

        // --- TIME IN BUTTON ---
        binding.timeInBtn.setOnClickListener {
            if (userId == null) {
                Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Geofence Check
            /* if (!isInsideGeofence) {
                Toast.makeText(this, "You must be inside the office to Time In!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            */

            // 2. Database Operation
            lifecycleScope.launch {
                try {
                    // Create the record in Firestore
                    val newResidency = ResidencyHoursController.createNewResidency(Date(), userId)

                    if (newResidency != null) {
                        Toast.makeText(this@LogResidencyTimeInActivity, "Time In Successful!", Toast.LENGTH_SHORT).show()

                        // Pass the new ID to the next activity so we know which one to close later
                        val intent = Intent(this@LogResidencyTimeInActivity, LogResidencyTimeOutActivity::class.java)
                        intent.putExtra("RESIDENCY_ID", newResidency.id)
                        startActivity(intent)
                        finish() // Finish this activity so user can't go back easily
                    } else {
                        Toast.makeText(this@LogResidencyTimeInActivity, "Failed to connect to database.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("TimeInError", "Error during Time In", e)
                    Toast.makeText(this@LogResidencyTimeInActivity, "An error occurred.", Toast.LENGTH_SHORT).show()
                }
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
        // Unregister to prevent memory leaks
        try {
            unregisterReceiver(geofenceStatusReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver might not be registered
        }
    }

    private fun generateQrCode(content: String): Bitmap? {
        val width = 512
        val height = 512
        val writer = QRCodeWriter()
        return try {
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    // Set pixel color: Black for data, White for background
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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