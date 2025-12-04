package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.LogResidencyTimeoutBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ResidencyHoursController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

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

        // 2. Fetch the residency details to get the 'timeIn' Date for display
        if (userId != null) {
            lifecycleScope.launch {
                // Fetch the object so we can access the 'timeIn' field
                val ongoing = ResidencyHoursController.getOngoingResidency(userId)

                if (ongoing != null) {
                    // Ensure ID is set (in case intent was null)
                    currentResidencyId = ongoing.id

                    // --- FORMAT AND DISPLAY TIME ---
                    val formatter = SimpleDateFormat("MMMM d, yyyy HH:mm:ss", Locale.US)
                    val formattedDate = formatter.format(ongoing.timeIn)

                    binding.timeInLblTv.text = "Time In: $formattedDate"
                    // -------------------------------

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
                // Update DB with Time Out
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
}