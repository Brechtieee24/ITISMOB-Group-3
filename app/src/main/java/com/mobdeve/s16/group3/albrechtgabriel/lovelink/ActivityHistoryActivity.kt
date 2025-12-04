package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ActivityHistoryPageBinding

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

        // Initialize sample data
        loadActivityData()

        // Setup RecyclerView
        activityAdapter = ActivityHistoryAdapter(activityList)
        binding.activityHistoryRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.activityHistoryRecyclerview.adapter = activityAdapter

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
        // SAMPLE DATA - replace with actual data from database later
        activityList = arrayListOf(
            ActivityItem("Event Activity 4", "September 12, 2025"),
            ActivityItem("Event Activity 3", "September 11, 2025"),
            ActivityItem("Event Activity 2", "September 10, 2025"),
            ActivityItem("Event Activity 1", "September 9, 2025")
        )
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