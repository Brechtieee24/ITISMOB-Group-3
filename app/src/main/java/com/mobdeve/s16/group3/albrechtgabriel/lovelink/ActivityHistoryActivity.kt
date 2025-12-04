package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ActivityHistoryPageBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ActivityParticipationController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.Event
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ActivityHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryPageBinding
    private lateinit var activityAdapter: ActivityHistoryAdapter
    private lateinit var activityList: ArrayList<ActivityItem>
    private var callerActivity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide navbar menu initially
        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Get the caller activity name from intent
        callerActivity = intent.getStringExtra("CALLER_ACTIVITY")

        // Initialize adapter with empty list
        activityList = arrayListOf()
        activityAdapter = ActivityHistoryAdapter(activityList)
        binding.activityHistoryRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.activityHistoryRecyclerview.adapter = activityAdapter

        // Load activity data from Firebase
        loadActivityDataFromFirebase()

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

    private fun loadActivityDataFromFirebase() {
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

                // Fetch events from ActivityParticipationController
                val events: List<Event> = ActivityParticipationController.getEventsOfUser(memberId)

                // Convert to ActivityItem format
                activityList.clear()
                events.forEach { event ->
                    activityList.add(
                        ActivityItem(
                            activityName = event.eventName,
                            activityDate = formatDate(event.date)
                        )
                    )
                }

                // Update adapter
                activityAdapter.notifyDataSetChanged()

                if (activityList.isEmpty()) {
                    Toast.makeText(
                        this@ActivityHistoryActivity,
                        "No activity history found",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@ActivityHistoryActivity,
                    "Error loading activity history: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun formatDate(dateString: String): String {
        // Handle different date formats from Firebase
        return try {
            // If already in readable format, return as is
            if (dateString.contains(",")) {
                dateString
            } else {
                // Otherwise format it
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: dateString
            }
        } catch (e: Exception) {
            dateString // Return original if parsing fails
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