package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.UserController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ViewOtherProfilePageBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ActivityParticipationController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ResidencyHoursController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ViewOtherProfileActivity : AppCompatActivity() {
    private lateinit var binding: ViewOtherProfilePageBinding
    private lateinit var activityAdapter: ProfileActivityAdapter
//    private lateinit var activityList: ArrayList<String>
    private lateinit var monthlyResidencyAdapter: MonthlyResidencyAdapter
//    private lateinit var monthlyResidencyList: ArrayList<MonthlyResidency>
    private var isOfficer: Boolean = false
    private var activityList: ArrayList<String> = arrayListOf()
    private var monthlyResidencyList: ArrayList<MonthlyResidency> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewOtherProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isOfficer = UserPreferences.isOfficer(this)
        NavbarManager.setupNavBar(this, isOfficer)

        val memberEmail = intent.getStringExtra("MEMBER_EMAIL")

        if (memberEmail == null) {
            Toast.makeText(this, "Error: Member not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

//        // Hide navbar menu initially
//        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // PLACEHOLDER PA MEMBER- Get member data from intent
//        val memberName = intent.getStringExtra("MEMBER_NAME") ?: "Jules Dela Cruz (MAE)"

        // Setup RecyclerView for monthly residency hours
//        monthlyResidencyAdapter = MonthlyResidencyAdapter(monthlyResidencyList)
//        binding.otherMonthlyResidencyRecyclerview.layoutManager = LinearLayoutManager(this)
//        binding.otherMonthlyResidencyRecyclerview.adapter = monthlyResidencyAdapter
//
//        // Setup RecyclerView for activities list
//        activityAdapter = ProfileActivityAdapter(activityList)
//        binding.otherActivitiesListRecyclerview.layoutManager = LinearLayoutManager(this)
//        binding.otherActivitiesListRecyclerview.adapter = activityAdapter

//        // Show/hide nav bar options
//        binding.navbar.menuIconNavImgbtn.setOnClickListener {
//            val menuSection = binding.navbar.navBarContainerLnr
//            menuSection.visibility =
//                if (menuSection.visibility == View.VISIBLE) {
//                    View.GONE
//                } else View.VISIBLE
//        }

        // Initialize adapters
        setupRecyclerViews()
        loadMemberData(memberEmail)

        binding.returnBtn.setOnClickListener {
            finish()
        }
    }

    // This new function safely initializes the adapters.
    private fun setupRecyclerViews() {
        monthlyResidencyAdapter = MonthlyResidencyAdapter(monthlyResidencyList)
        binding.otherMonthlyResidencyRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.otherMonthlyResidencyRecyclerview.adapter = monthlyResidencyAdapter

        activityAdapter = ProfileActivityAdapter(activityList)
        binding.otherActivitiesListRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.otherActivitiesListRecyclerview.adapter = activityAdapter
    }


    private fun loadMemberData(email: String) {
        lifecycleScope.launch {
            val user = UserController.getUserByEmail(email)

            if (user != null) {
                binding.otherProfileNameTv.text = "${user.firstName} ${user.lastName} (${user.committee})"
                binding.otherProfileBioTv.text = user.aboutInfo

                // FIX: Use the user's Firestore document ID, not email
                // The user.id should contain the document ID from Firestore
                val userId = user.id

                if (userId.isNotEmpty()) {
                    // Load residency data using the Firestore document ID
                    loadResidencyData(userId)

                    // Load activity data using the Firestore document ID
                    loadActivityData(userId)
                } else {
                    Toast.makeText(
                        this@ViewOtherProfileActivity,
                        "User ID not found.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Create the dummy lists
//                val monthlyResidencyList = arrayListOf(
//                    MonthlyResidency("Oct", 490),
//                    MonthlyResidency("Sep", 490),
//                    MonthlyResidency("Aug", 490)
//                )
//
//                val activityList = arrayListOf(
//                    "Event Activity # 4 (9/12/25)",
//                    "Event Activity # 3 (9/11/25)",
//                    "Event Activity # 2 (9/10/25)",
//                    "Event Activity # 1 (9/9/25)"
//                )

//                monthlyResidencyAdapter.updateData(monthlyResidencyList)
//                activityAdapter.updateData(activityList)
            } else {
                Toast.makeText(this@ViewOtherProfileActivity, "Could not load profile.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private suspend fun loadResidencyData(userId: String) {
        try {
            val logs = ResidencyHoursController.getMemberResidency(userId)

            monthlyResidencyList.clear()

            if (logs.isNotEmpty()) {
                val monthlyTotals = mutableMapOf<String, Int>()
                val monthFormatter = SimpleDateFormat("MMM yyyy", Locale.US)

                for (log in logs) {
                    if (log.timeOut.time > 0) {
                        val monthKey = monthFormatter.format(log.timeIn)
                        val diffMillis = log.timeOut.time - log.timeIn.time
                        val minutes = (diffMillis / (1000 * 60)).toInt()

                        val currentTotal = monthlyTotals.getOrDefault(monthKey, 0)
                        monthlyTotals[monthKey] = currentTotal + minutes
                    }
                }

                // Sort by date (most recent first)
                val sortedMonths = monthlyTotals.entries.sortedByDescending {
                    monthFormatter.parse(it.key)?.time ?: 0L
                }

                for (entry in sortedMonths) {
                    val monthAbbr = entry.key.split(" ")[0]
                    monthlyResidencyList.add(MonthlyResidency(monthAbbr, entry.value))
                }
            }

            if (monthlyResidencyList.isEmpty()) {
                monthlyResidencyList.add(MonthlyResidency("No Data", 0))
            }

            monthlyResidencyAdapter.notifyDataSetChanged()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this@ViewOtherProfileActivity, "Error loading residency data", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun loadActivityData(userId: String) {
        try {
            val events = ActivityParticipationController.getEventsOfUser(userId)

            activityList.clear()

            for (event in events) {
                activityList.add("${event.eventName} (${event.date})")
            }

            if (activityList.isEmpty()) {
                activityList.add("No activities yet.")
            }

            activityAdapter.notifyDataSetChanged()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this@ViewOtherProfileActivity, "Error loading activity data", Toast.LENGTH_SHORT).show()
        }
    }

}