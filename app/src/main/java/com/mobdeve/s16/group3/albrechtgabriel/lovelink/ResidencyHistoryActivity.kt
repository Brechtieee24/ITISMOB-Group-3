package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ResidencyHistoryPageBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ResidencyHours
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ResidencyHoursController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class ResidencyHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ResidencyHistoryPageBinding
    private lateinit var residencyAdapter: ResidencyHistoryAdapter
    private lateinit var residencyList: ArrayList<ResidencyItem>
    private var callerActivity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ResidencyHistoryPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide navbar menu initially
        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Get the caller activity name from intent
        callerActivity = intent.getStringExtra("CALLER_ACTIVITY")

        // Initialize adapter with empty list
        residencyList = arrayListOf()
        residencyAdapter = ResidencyHistoryAdapter(residencyList)
        binding.residencyHistoryRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.residencyHistoryRecyclerview.adapter = residencyAdapter

        // Load residency data from Firebase
        loadResidencyDataFromFirebase()

        // Show/hide nav bar options
        binding.navbar.menuIconNavImgbtn.setOnClickListener {
            val menuSection = binding.navbar.navBarContainerLnr
            menuSection.visibility =
                if (menuSection.visibility == View.VISIBLE) {
                    View.GONE
                } else View.VISIBLE
        }

        // Return button goes back to the caller activity
        binding.returnBtn.setOnClickListener {
            returnToCaller()
        }
    }

    private fun loadResidencyDataFromFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        val userEmail = currentUser.email
        if (userEmail.isNullOrEmpty()) {
            Toast.makeText(this, "User email not available", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Get member's ID (using email as doc ID)
                val memberId = userEmail

                // Fetch residency records from ResidencyHoursController
                val records: List<ResidencyHours> = ResidencyHoursController.getMemberResidency(memberId)

                // Convert to ResidencyItem format
                residencyList.clear()
                records.forEach { record ->
                    residencyList.add(
                        ResidencyItem(
                            date = formatDate(record.timeIn),
                            timeIn = formatTime(record.timeIn),
                            timeOut = formatTime(record.timeOut),
                            total = calculateDuration(record.timeIn, record.timeOut)
                        )
                    )
                }

                // Update adapter
                residencyAdapter.notifyDataSetChanged()

                if (residencyList.isEmpty()) {
                    Toast.makeText(
                        this@ResidencyHistoryActivity,
                        "No residency history found",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@ResidencyHistoryActivity,
                    "Error loading residency history: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun formatDate(date: java.util.Date): String {
        val sdf = SimpleDateFormat("MMM dd,\nyyyy", Locale.getDefault())
        return sdf.format(date)
    }

    private fun formatTime(date: java.util.Date): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(date)
    }

    private fun calculateDuration(timeIn: java.util.Date, timeOut: java.util.Date): String {
        val durationMillis = timeOut.time - timeIn.time
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        return "$minutes\nminutes"
    }

    private fun returnToCaller() {
        when (callerActivity) {
            "LogResidencyTimeInActivity" -> {
                val intent = Intent(this, LogResidencyTimeInActivity::class.java)
                startActivity(intent)
                finish()
            }
            "LogResidencyTimeOutActivity" -> {
                val intent = Intent(this, LogResidencyTimeOutActivity::class.java)
                startActivity(intent)
                finish()
            }
            // Add more cases for Log Activity pages when they're ready
            else -> {
                // Default: go back to home if caller is unknown
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}