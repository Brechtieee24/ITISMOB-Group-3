package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.UserController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.LogResidencyTimeoutBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ResidencyHoursController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.User
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class LogResidencyTimeOutActivity : AppCompatActivity() {
    private lateinit var binding: LogResidencyTimeoutBinding
    private var currentResidencyId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LogResidencyTimeoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = getSharedPreferences("prefs", MODE_PRIVATE).getString("user_id", null)

        // 1. Get ID from Intent (if available)
        currentResidencyId = intent.getStringExtra("RESIDENCY_ID")

        val currentUser = FirebaseAuth.getInstance().currentUser

        // --- GENERATE QR CODE ---
        if (userId != null) {
            val qrBitmap = generateQrCode(userId)
            if (qrBitmap != null) {
                binding.logResidencyQrHolderIv.setImageBitmap(qrBitmap)
            }
        }

        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val userEmail = currentUser.email
        if (userEmail.isNullOrEmpty()) {
            Toast.makeText(this, "User email not available", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // 2. Fetch data (User details AND Residency details)
        if (userId != null) {
            lifecycleScope.launch {
                val user = UserController.getUserByEmail(userEmail)
                // --- A. FETCH USER INFO & UPDATE TEXTVIEW ---
                try {

                    if (user != null) {
                        val fullName = "${user.firstName} ${user.lastName}"
                        // Handle empty committee case gracefully
                        val committeeText = if (user.committee.isNotEmpty()) user.committee else "No Committee"

                        // Update the label: "Name - Committee"
                        binding.logResidencyNameLblTv.text = "$fullName ($committeeText)"
                    }
                } catch (e: Exception) {
                    // Fallback in case of network error
                    binding.logResidencyNameLblTv.text = "User Info Unavailable"
                    e.printStackTrace()
                }
                // ---------------------------------------------


                // --- B. FETCH RESIDENCY INFO (Existing Logic) ---
                val ongoing = ResidencyHoursController.getOngoingResidency(userId)

                if (ongoing != null) {
                    currentResidencyId = ongoing.id

                    val formatter = SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.US)
                    val formattedDate = formatter.format(ongoing.timeIn)

                    binding.timeInLblTv.text = "Time In: $formattedDate"
                } else {
                    Toast.makeText(this@LogResidencyTimeOutActivity, "No active residency found.", Toast.LENGTH_LONG).show()
                    binding.timeInLblTv.text = "Time In: --"
                }
            }
        }

        // --- TIME OUT BUTTON ---
        binding.timeOutBtn.setOnClickListener {
            if (currentResidencyId == null) {
                Toast.makeText(this, "Error: No active session to close.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = ResidencyHoursController.setOngoingResidency(currentResidencyId!!)

                if (result != null) {
                    Toast.makeText(this@LogResidencyTimeOutActivity, "Time Out Successful!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@LogResidencyTimeOutActivity, "Failed to update record.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Residency History button
        binding.residencyHistoryBtn.setOnClickListener {
            val intent = Intent(this, ResidencyHistoryActivity::class.java)
            intent.putExtra("CALLER_ACTIVITY", "LogResidencyTimeOutActivity")
            startActivity(intent)
        }

        // Activity History button
        binding.activityHistoryBtn.setOnClickListener {
            val intent = Intent(this, ActivityHistoryActivity::class.java)
            intent.putExtra("CALLER_ACTIVITY", "LogResidencyTimeOutActivity")
            startActivity(intent)
        }

        // Return button
        binding.returnBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Generate QR Code
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
}