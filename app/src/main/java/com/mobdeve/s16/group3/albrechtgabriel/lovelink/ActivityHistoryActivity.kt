package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // Required for background tasks
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ActivityHistoryPageBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ActivityParticipationController
import kotlinx.coroutines.launch

class ActivityHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryPageBinding
    private lateinit var activityAdapter: ActivityHistoryAdapter

    // FIX: Initialize immediately to avoid "UninitializedPropertyAccessException"
    private var activityList: ArrayList<ActivityItem> = ArrayList()

    private var callerActivity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide navbar menu initially
        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Get the caller activity name from intent
        callerActivity = intent.getStringExtra("CALLER_ACTIVITY")

        // 1. SETUP ADAPTER FIRST (Using the empty list initialized above)
        activityAdapter = ActivityHistoryAdapter(activityList)
        binding.activityHistoryRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.activityHistoryRecyclerview.adapter = activityAdapter

        // 2. LOAD DATA (This will update the list and refresh the adapter)
        loadActivityData()

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

    private fun loadActivityData() {
        // Get the User ID from Shared Preferences
        val userId = getSharedPreferences("prefs", MODE_PRIVATE).getString("user_id", null)

        if (userId != null) {
            lifecycleScope.launch {
                try {
                    // Fetch data from Controller (Background thread)
                    val events = ActivityParticipationController.getEventsOfUser(userId)

                    // Clear existing dummy data
                    activityList.clear()

                    // Loop through the events from Firestore
                    for (event in events) {
                        // Map the Firestore 'Event' object to your 'ActivityItem' UI model
                        // Note: Ensure your Event model has 'eventName' and 'date' fields
                        activityList.add(
                            ActivityItem(
                                event.eventName, // e.g. "Tree Planting"
                                event.date       // e.g. "September 12, 2025"
                            )
                        )
                    }

                    // Refresh RecyclerView
                    activityAdapter.notifyDataSetChanged()

                    // Optional: Show feedback if empty
                    if (activityList.isEmpty()) {
                        Toast.makeText(this@ActivityHistoryActivity, "No activity history found.", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@ActivityHistoryActivity, "Error loading activities.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_SHORT).show()
        }
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