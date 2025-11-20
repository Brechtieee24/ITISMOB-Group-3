package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ProfilePageBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ProfilePageBinding
    private lateinit var activityAdapter: ProfileActivityAdapter
    private lateinit var activityList: ArrayList<String>
    private lateinit var monthlyResidencyAdapter: MonthlyResidencyAdapter
    private lateinit var monthlyResidencyList: ArrayList<MonthlyResidency>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide navbar menu initially
        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Load sample data
        loadProfileData()

        // Setup RecyclerView for monthly residency hours
        monthlyResidencyAdapter = MonthlyResidencyAdapter(monthlyResidencyList)
        binding.monthlyResidencyRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.monthlyResidencyRecyclerview.adapter = monthlyResidencyAdapter

        // Setup RecyclerView for activities list
        activityAdapter = ProfileActivityAdapter(activityList)
        binding.activitiesListRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.activitiesListRecyclerview.adapter = activityAdapter

        // Show/hide nav bar options
        binding.navbar.menuIconNavImgbtn.setOnClickListener {
            val menuSection = binding.navbar.navBarContainerLnr
            menuSection.visibility =
                if (menuSection.visibility == View.VISIBLE) {
                    View.GONE
                } else View.VISIBLE
        }

        // Update Bio button (placeholder for now)
        binding.updateBioBtn.setOnClickListener {
            // TODO: Implement update bio functionality
            // Show/hide the edit options (Save Bio, Change Photo, Cancel)
        }

        // Return button always goes back to Home
        binding.returnBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadProfileData() {
        // SAMPLE DATA - replace with actual data from database later
        binding.profileNameTv.text = "Jules Dela Cruz (MAE)"
        binding.profileBioTv.text = "Hi! My name is Jules."

        // Calculate monthly residency from residency history
        monthlyResidencyList = calculateMonthlyResidency()

        activityList = arrayListOf(
            "Event Activity # 4 (9/12/25)",
            "Event Activity # 3 (9/11/25)",
            "Event Activity # 2 (9/10/25)",
            "Event Activity # 1 (9/9/25)"
        )
    }

    private fun calculateMonthlyResidency(): ArrayList<MonthlyResidency> {
        // SAMPLE DATA FOR CALCULATION - in real app, fetch from ResidencyHistory database
        // and group by month, then sum up total minutes

        // Sample data shows each entry is 133 minutes, example:
        // - October: 5 entries x 133 minutes = 665 minutes
        // - September: 4 entries x 133 minutes = 532 minutes
        // - August: 3 entries x 133 minutes = 399 minutes

        return arrayListOf(
            MonthlyResidency("Oct", 490),  // 8 hours 10 minutes = 490 minutes
            MonthlyResidency("Sep", 490),  // 8 hours 10 minutes = 490 minutes
            MonthlyResidency("Aug", 490)   // 8 hours 10 minutes = 490 minutes
        )
    }

    // TODO: Future function to calculate from actual database
    private fun calculateMonthlyResidencyFromDB(): ArrayList<MonthlyResidency> {
        // Pseudocode for when you implement database:
        // 1. Fetch all ResidencyItems for this user
        // 2. Group by month (extract month from date)
        // 3. For each month group:
        //    - Parse timeIn and timeOut
        //    - Calculate duration in minutes
        //    - Sum all durations for that month
        // 4. Create MonthlyResidency objects with month name and total minutes

        return arrayListOf()
    }
}