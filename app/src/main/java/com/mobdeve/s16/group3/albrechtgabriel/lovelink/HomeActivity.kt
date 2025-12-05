package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.HomePageBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ActivityParticipationController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.EventConstants
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ResidencyHoursController
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: HomePageBinding
    private var isOfficer: Boolean = false

    // CameraX Variables
    private lateinit var cameraExecutor: ExecutorService

    // Tracks unique IDs scanned in the current open session
    private val scannedSessionIds = mutableSetOf<String>()

    // Tracks IDs currently being processed to prevent spam
    private val processingIds = mutableSetOf<String>()

    // NEW: Stores the ID of the event found via search
    private var currentEventId: String? = null

    // Permission Launcher
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission required.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Camera Executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        isOfficer = intent.getBooleanExtra("IS_OFFICER", false)
        if (!isOfficer) {
            isOfficer = UserPreferences.isOfficer(this)
        }

        NavbarManager.setupNavBar(this, isOfficer)
        logActivityDialogFlow()

        // Setup Buttons based on Role
        if (isOfficer) {
            binding.logActivityHomeBtn.visibility = View.VISIBLE
            binding.activityHistoryHomeBtn.visibility = View.GONE
        } else {
            binding.logActivityHomeBtn.visibility = View.GONE
            binding.activityHistoryHomeBtn.visibility = View.VISIBLE
        }
        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Navigation
        binding.navbar.pfpHolderImgbtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.logResidencyHomeBtn.setOnClickListener {
            handleResidencyNavigation()
        }
        binding.activityHistoryHomeBtn.setOnClickListener {
            val intent = Intent(this, ActivityHistoryActivity::class.java)
            intent.putExtra("CALLER_ACTIVITY", "HomeActivity")
            startActivity(intent)
        }
        binding.viewActivitiesBtn.setOnClickListener {
            val intent = Intent(this, ViewActivitiesActivity::class.java)
            intent.putExtra("IS_OFFICER", isOfficer)
            startActivity(intent)
        }
        binding.viewOtherMembersBtn.setOnClickListener {
            val intent = Intent(this, ViewMembersActivity::class.java)
            intent.putExtra("IS_OFFICER", isOfficer)
            startActivity(intent)
        }
        binding.returnBtn.setOnClickListener {
            handleSignOut()
        }
    }

    private fun logActivityDialogFlow() {
        val logActivityContainer = binding.dialogLogActivityContainer
        val qrFrameContainer = binding.dialogQrFrameContainer

        // Open Menu
        binding.logActivityHomeBtn.setOnClickListener {
            logActivityContainer.visibility = View.VISIBLE
        }

        // Close Menu
        binding.logActivityDialog.closebtn.setOnClickListener {
            logActivityContainer.visibility = View.GONE
        }
        binding.logActivityDialog.confirmbtn.setOnClickListener {
            logActivityContainer.visibility = View.GONE
        }

        // --- START SCANNING (Modified Logic) ---
        binding.logActivityDialog.scanQrBtn.setOnClickListener {

            // 1. Get the Activity Name from the EditText
            val inputName = binding.logActivityDialog.activityNameAutoCompleteTextView.text.toString().trim()

            if (inputName.isEmpty()) {
                Toast.makeText(this, "Please enter an Activity Name first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Search Database for this Event
            lifecycleScope.launch {
                val foundEventId = findEventByName(inputName)

                if (foundEventId != null) {
                    // Success: Store the ID
                    currentEventId = foundEventId
                    Toast.makeText(this@HomeActivity, "Event Found! Starting Scanner...", Toast.LENGTH_SHORT).show()

                    // 3. Switch Views
                    logActivityContainer.visibility = View.GONE
                    qrFrameContainer.visibility = View.VISIBLE

                    // 4. Reset Lists
                    scannedSessionIds.clear()
                    processingIds.clear()
                    binding.qrFrameDialog.scannedParticipantsLabel.text = "Scanned Participants: 0"

                    // 5. Open Camera
                    checkPermissionAndStartCamera()
                } else {
                    // Fail: Show Error
                    Toast.makeText(this@HomeActivity, "Event '$inputName' not found.", Toast.LENGTH_LONG).show()
                }
            }
        }

        // --- CLOSE SCANNER ---
        binding.qrFrameDialog.closebtn.setOnClickListener {
            qrFrameContainer.visibility = View.GONE
            stopCamera()
            currentEventId = null // Reset event ID
        }

        // --- CONFIRM BUTTON ---
        binding.qrFrameDialog.confirmbtn.setOnClickListener {
            qrFrameContainer.visibility = View.GONE
            stopCamera()
            Toast.makeText(this, "Session finished. Total: ${scannedSessionIds.size}", Toast.LENGTH_SHORT).show()
            currentEventId = null // Reset event ID
        }
    }

    // --- HELPER: Search Event by Name ---
    private suspend fun findEventByName(name: String): String? {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection(EventConstants.EVENTS_COLLECTION)
                .whereEqualTo("eventName", name) // Ensure field name matches DB exactly
                .get()
                .await()

            if (!snapshot.isEmpty) {
                // Return the ID of the first match
                snapshot.documents[0].id
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun checkPermissionAndStartCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.qrFrameDialog.cameraPreview.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QrCodeAnalyzer { qrCodeValue ->
                        runOnUiThread {
                            // Check duplication & processing status
                            if (!scannedSessionIds.contains(qrCodeValue) && !processingIds.contains(qrCodeValue)) {

                                processingIds.add(qrCodeValue)

                                // Verify user exists, then save using currentEventId
                                lifecycleScope.launch {
                                    val userExists = checkUserExists(qrCodeValue)

                                    if (userExists) {
                                        scannedSessionIds.add(qrCodeValue)
                                        binding.qrFrameDialog.scannedParticipantsLabel.text = "Scanned Participants: ${scannedSessionIds.size}"
                                        Toast.makeText(this@HomeActivity, "ID Scanned & Logged!", Toast.LENGTH_SHORT).show()

                                        // Save to DB
                                        saveActivityParticipation(qrCodeValue)
                                    } else {
                                        Toast.makeText(this@HomeActivity, "Invalid QR Code", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e("CAMERA", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }, ContextCompat.getMainExecutor(this))
    }

    private suspend fun checkUserExists(userId: String): Boolean {
        return try {
            val doc = FirebaseFirestore.getInstance().collection("User").document(userId).get().await()
            doc.exists()
        } catch (e: Exception) { false }
    }

    private class QrCodeAnalyzer(private val onQrFound: (String) -> Unit) : ImageAnalysis.Analyzer {
        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val scanner = BarcodeScanning.getClient()
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            barcode.rawValue?.let { onQrFound(it) }
                        }
                    }
                    .addOnCompleteListener { imageProxy.close() }
            } else {
                imageProxy.close()
            }
        }
    }

    // --- DATABASE LOGIC ---
    private fun saveActivityParticipation(memberId: String) {
        // We use the ID we found earlier
        val eventId = currentEventId

        if (eventId != null) {
            lifecycleScope.launch {
                try {
                    val result = ActivityParticipationController.addEventParticipation(memberId, eventId)
                    if (result != null) {
                        Log.d("SCAN", "Saved $memberId linked to event $eventId")
                    } else {
                        Toast.makeText(this@HomeActivity, "Failed to save to database.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            Toast.makeText(this, "Error: No Event selected.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleResidencyNavigation() {
        val userId = getSharedPreferences("prefs", MODE_PRIVATE).getString("user_id", null)
        if (userId != null) {
            lifecycleScope.launch {
                val ongoing = ResidencyHoursController.getOngoingResidency(userId)
                val targetActivity = if (ongoing != null) LogResidencyTimeOutActivity::class.java
                else LogResidencyTimeInActivity::class.java

                val intent = Intent(this@HomeActivity, targetActivity)
                if (ongoing != null) intent.putExtra("RESIDENCY_ID", ongoing.id)
                startActivity(intent)
            }
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSignOut() {
        UserPreferences.clearAll(this)
        FirebaseAuth.getInstance().signOut()
        getSharedPreferences("prefs", MODE_PRIVATE).edit().clear().apply()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}