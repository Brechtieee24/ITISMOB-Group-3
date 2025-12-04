package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.LogResidencyTimeoutBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ResidencyHoursController
import kotlinx.coroutines.launch

class LogResidencyTimeOutActivity : AppCompatActivity() {
    private lateinit var binding: LogResidencyTimeoutBinding
    private var currentResidencyId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LogResidencyTimeoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = getSharedPreferences("prefs", MODE_PRIVATE).getString("user_id", null)

        // 1. Try to get ID from Intent
        currentResidencyId = intent.getStringExtra("RESIDENCY_ID")

        // 2. Fail-safe: If app crashed/restarted, fetch the open session from DB
        if (currentResidencyId == null && userId != null) {
            lifecycleScope.launch {
                val ongoing = ResidencyHoursController.getOngoingResidency(userId)
                if (ongoing != null) {
                    currentResidencyId = ongoing.id
                    Toast.makeText(this@LogResidencyTimeOutActivity, "Resumed active session.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LogResidencyTimeOutActivity, "No active residency found.", Toast.LENGTH_LONG).show()
                    // Optional: You might want to disable the TimeOut button here if no session exists
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

                    // Return to Home
                    val intent = Intent(this@LogResidencyTimeOutActivity, HomeActivity::class.java)
                    startActivity(intent)
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