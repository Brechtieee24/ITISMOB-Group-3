package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.LogResidencyTimeinBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.geofencing.GeofenceManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ResidencyHoursController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogResidencyTimeInActivity : AppCompatActivity() {

    private lateinit var binding: LogResidencyTimeinBinding
    private lateinit var geofenceManager: GeofenceManager
    private var isInsideGeofence = false
    private var isOfficer: Boolean = false

    // Define coordinates here so both Geofence and Manual Check use the same data
    // QUIRINO LOCATION
    private val TARGET_LAT = 14.571426282440354
    private val TARGET_LNG = 120.99130425300307
    private val TARGET_RADIUS = 200f

    // BRO CONNON HALL
    //private val TARGET_LAT = 14.563927754309345
    //private val TARGET_LNG = 120.99333652577326
    private val GEOLOCATION = "Quirino"
    // ---------------------

    // BroadcastReceiver to get geofence enter/exit updates (Passive Check)
    private val geofenceStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val inside = intent?.getBooleanExtra("inside", false) ?: false
            isInsideGeofence = inside
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
            checkCurrentLocation()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val userId = prefs.getString("user_id", null)

        binding = LogResidencyTimeinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isOfficer = UserPreferences.isOfficer(this)
        NavbarManager.setupNavBar(this, isOfficer)

        // --- GEOFENCE SETUP ---
        geofenceManager = GeofenceManager(this)
        checkPermissionsAndStartGeofence()

        // Register Receiver
        ContextCompat.registerReceiver(
            this,
            geofenceStatusReceiver,
            IntentFilter("GEOFENCE_STATUS"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // --- START CLOCK ---
        startRealTimeClock()

        // --- LOCATION TRACKING ---
        setupGeofence()
        checkCurrentLocation()

        // --- LOAD USER DATA ---
        if (userId != null) {
            val qrBitmap = generateQrCode(userId)
            if (qrBitmap != null) {
                binding.logResidencyQrHolderIv.setImageBitmap(qrBitmap)
            }

            lifecycleScope.launch {
                try {
                    val userSnapshot = FirebaseFirestore.getInstance()
                        .collection("User") // Verify exact collection name in Firebase
                        .document(userId)
                        .get()
                        .await()

                    val user = userSnapshot.toObject(User::class.java)
                    if (user != null) {
                        val fullName = "${user.firstName} ${user.lastName}"
                        val committeeText = if (user.committee.isNotEmpty()) user.committee else "No Committee"
                        binding.logResidencyNameLblTv.text = "$fullName ($committeeText)"
                    }

                    // Fetch Latest Residency
                    val latestResidency = ResidencyHoursController.getLatestMemberResidency(userId)

                    if (latestResidency != null) {
                        val dateFormatter = SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.US)
                        binding.timeInLblTv.text = "Time In: ${dateFormatter.format(latestResidency.timeIn)}"
                        binding.timeOutLblTv.text = "Time Out: ${dateFormatter.format(latestResidency.timeOut)}"

                        val diffMillis = latestResidency.timeOut.time - latestResidency.timeIn.time
                        val totalSeconds = diffMillis / 1000
                        val hours = totalSeconds / 3600
                        val minutes = (totalSeconds % 3600) / 60
                        val seconds = totalSeconds % 60

                        binding.totalHoursLblTv.text = "Total Hours: ${hours}h ${minutes}m ${seconds}s"
                    } else {
                        binding.timeInLblTv.text = "Time In: --"
                        binding.timeOutLblTv.text = "Time Out: --"
                        binding.totalHoursLblTv.text = "Total Hours: --"
                    }

                } catch (e: Exception) {
                    binding.logResidencyNameLblTv.text = "Error loading info"
                    e.printStackTrace()
                }
            }
        }

        // --- BUTTON: TIME IN ---
        binding.timeInBtn.setOnClickListener {
            if (userId == null) {
                Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Force a manual location check when button is clicked
            checkCurrentLocation()

            // 2. Check the flag (which might have just been updated)
            if (!isInsideGeofence) {
                Toast.makeText(this, "You must be inside the office to Time In!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 3. Proceed to Database
            lifecycleScope.launch {
                try {
                    val newResidency = ResidencyHoursController.createNewResidency(Date(), userId)
                    if (newResidency != null) {
                        Toast.makeText(this@LogResidencyTimeInActivity, "Time In Successful!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LogResidencyTimeInActivity, LogResidencyTimeOutActivity::class.java)
                        intent.putExtra("RESIDENCY_ID", newResidency.id)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LogResidencyTimeInActivity, "Failed to connect to database.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LogResidencyTimeInActivity, "An error occurred.", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }

        // --- NAVIGATION BUTTONS ---
        binding.residencyHistoryBtn.setOnClickListener {
            val intent = Intent(this, ResidencyHistoryActivity::class.java)
            intent.putExtra("CALLER_ACTIVITY", "LogResidencyTimeInActivity")
            startActivity(intent)
        }

        binding.activityHistoryBtn.setOnClickListener {
            val intent = Intent(this, ActivityHistoryActivity::class.java)
            intent.putExtra("CALLER_ACTIVITY", "LogResidencyTimeInActivity")
            startActivity(intent)
        }

        binding.returnBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Get the last known location (Fastest method)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val targetLocation = Location("target")
                targetLocation.latitude = TARGET_LAT
                targetLocation.longitude = TARGET_LNG

                val distance = location.distanceTo(targetLocation)

                // Update the flag based on actual calculation
                isInsideGeofence = distance <= TARGET_RADIUS

            }
        }
    }

    private fun startRealTimeClock() {
        lifecycleScope.launch {
            while (isActive) {
                val formatter = SimpleDateFormat("MMMM d, yyyy, hh:mm:ss a", Locale.US)
                val currentTime = formatter.format(Date())
                binding.presentTimeTv.text = currentTime
                delay(1000)
            }
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
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(geofenceStatusReceiver) } catch (e: Exception) {}
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
            checkCurrentLocation() // Check on startup
        }
    }

    private fun setupGeofence() {
        // Using the Constants defined at the top
        val geofence = Geofence.Builder()
            .setRequestId(GEOLOCATION)
            .setCircularRegion(
                TARGET_LAT,
                TARGET_LNG,
                TARGET_RADIUS
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