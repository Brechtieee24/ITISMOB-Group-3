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
    private var isScanning = false // Flag to prevent multiple detections

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
        val qrFrameContainer = binding.dialogQrFrameContainer // The container covering the screen

        // Note: Make sure you added <PreviewView id="@+id/cameraPreview"> to your included XML
        val previewView = binding.qrFrameDialog.cameraPreview

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

        // --- START SCANNING ---
        binding.logActivityDialog.scanQrBtn.setOnClickListener {
            logActivityContainer.visibility = View.GONE
            qrFrameContainer.visibility = View.VISIBLE

            checkPermissionAndStartCamera()
        }

        // --- CLOSE SCANNER ---
        binding.qrFrameDialog.closebtn.setOnClickListener {
            qrFrameContainer.visibility = View.GONE
            stopCamera() // Stop processing to save battery
        }

        // Confirm Button (Manual override if needed)
        binding.qrFrameDialog.confirmbtn.setOnClickListener {
            qrFrameContainer.visibility = View.GONE
            stopCamera()
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

            // 1. Preview (The visual feed)
            val preview = Preview.Builder().build().also {
                // Attach the feed to your PreviewView in the layout
                it.setSurfaceProvider(binding.qrFrameDialog.cameraPreview.surfaceProvider)
            }

            // 2. Image Analysis (The QR Reader)
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QrCodeAnalyzer { qrCodeValue ->
                        // CALLBACK WHEN QR FOUND
                        if (!isScanning) {
                            isScanning = true // Prevent duplicate triggers
                            runOnUiThread {
                                stopCamera() // Freeze camera
                                binding.dialogQrFrameContainer.visibility = View.GONE
                                Toast.makeText(this, "Scanned: $qrCodeValue", Toast.LENGTH_SHORT).show()

                                // Save to DB
                                saveActivityParticipation(qrCodeValue)
                            }
                        }
                    })
                }

            // Select Back Camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                // Bind lifecycle
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
                isScanning = false // Reset flag when camera starts
            } catch (exc: Exception) {
                Log.e("CAMERA", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll() // Stops the camera
        }, ContextCompat.getMainExecutor(this))
    }

    // --- QR ANALYZER CLASS (Inner Class or Separate File) ---
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
                            barcode.rawValue?.let { value ->
                                onQrFound(value) // Trigger callback
                                // Don't close imageProxy here, we want to stop analyzing
                            }
                        }
                    }
                    .addOnFailureListener {
                        // Handle failure
                    }
                    .addOnCompleteListener {
                        imageProxy.close() // Important: Close frame to allow next one
                    }
            } else {
                imageProxy.close()
            }
        }
    }

    // --- DATABASE LOGIC ---
    private fun saveActivityParticipation(memberId: String) {
        lifecycleScope.launch {
            try {
                // 1. Get a valid Event ID (Just grabbing the first one for now)
                val eventsSnapshot = FirebaseFirestore.getInstance()
                    .collection(EventConstants.EVENTS_COLLECTION)
                    .get()
                    .await()

                val eventId = eventsSnapshot.documents.firstOrNull()?.id

                if (eventId != null) {
                    val result = ActivityParticipationController.addEventParticipation(memberId, eventId)
                    if (result != null) {
                        Toast.makeText(this@HomeActivity, "Activity Logged!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@HomeActivity, "Failed to save.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@HomeActivity, "No Events found.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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