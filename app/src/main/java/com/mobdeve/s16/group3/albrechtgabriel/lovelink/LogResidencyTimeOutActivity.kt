package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.LogResidencyTimeoutBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ResidencyHoursController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogResidencyTimeOutActivity : AppCompatActivity() {
    private lateinit var binding: LogResidencyTimeoutBinding
    private var currentResidencyId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LogResidencyTimeoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = getSharedPreferences("prefs", MODE_PRIVATE).getString("user_id", null)
        currentResidencyId = intent.getStringExtra("RESIDENCY_ID")

        if (userId == null) {
            Toast.makeText(this, "User ID not found in session", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // --- START REALTIME CLOCK ---
        startRealTimeClock()
        // ---------------------------

        val qrBitmap = generateQrCode(userId)
        if (qrBitmap != null) {
            binding.logResidencyQrHolderIv.setImageBitmap(qrBitmap)
        }

        lifecycleScope.launch {
            // A. FETCH USER INFO
            try {
                val userSnapshot = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(userId)
                    .get()
                    .await()

                val user = userSnapshot.toObject(User::class.java)

                if (user != null) {
                    val fullName = "${user.firstName} ${user.lastName}"
                    val committeeText = if (user.committee.isNotEmpty()) user.committee else "No Committee"
                    binding.logResidencyNameLblTv.text = "$fullName ($committeeText)"
                }
            } catch (e: Exception) {
                binding.logResidencyNameLblTv.text = "User Info Unavailable"
                e.printStackTrace()
            }

            // B. FETCH RESIDENCY INFO
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

    // --- NEW FUNCTION FOR REALTIME CLOCK ---
    private fun startRealTimeClock() {
        lifecycleScope.launch {
            while (isActive) {
                val formatter = SimpleDateFormat("MMMM d, yyyy, hh:mm:ss a", Locale.US)
                val currentTime = formatter.format(Date())

                binding.presentTimeTv.text = currentTime

                delay(1000) // Updates every second
            }
        }
    }
    // ---------------------------------------

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
        } catch (e: Exception) { null }
    }
}