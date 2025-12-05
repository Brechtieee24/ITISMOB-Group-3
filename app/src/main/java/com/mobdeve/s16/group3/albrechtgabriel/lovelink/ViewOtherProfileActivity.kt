package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.UserController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ViewOtherProfilePageBinding
import kotlinx.coroutines.launch

class ViewOtherProfileActivity : AppCompatActivity() {
    private lateinit var binding: ViewOtherProfilePageBinding
    private lateinit var activityAdapter: ProfileActivityAdapter
//    private lateinit var activityList: ArrayList<String>
    private lateinit var monthlyResidencyAdapter: MonthlyResidencyAdapter
//    private lateinit var monthlyResidencyList: ArrayList<MonthlyResidency>
    private var isOfficer: Boolean = false

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

//        setupRecyclerViews()
        loadMemberData(memberEmail)

        binding.returnBtn.setOnClickListener {
            finish()
        }
    }

    // This new function safely initializes the adapters.
//    private fun setupRecyclerViews() {
//        // Initialize adapters with empty lists to prevent the crash
//        monthlyResidencyAdapter = MonthlyResidencyAdapter(arrayListOf())
//        binding.otherMonthlyResidencyRecyclerview.layoutManager = LinearLayoutManager(this)
//        binding.otherMonthlyResidencyRecyclerview.adapter = monthlyResidencyAdapter
//
//        activityAdapter = ProfileActivityAdapter(arrayListOf())
//        binding.otherActivitiesListRecyclerview.layoutManager = LinearLayoutManager(this)
//        binding.otherActivitiesListRecyclerview.adapter = activityAdapter
//    }

    private fun loadMemberData(email: String) {
        lifecycleScope.launch {
            val user = UserController.getUserByEmail(email)

            if (user != null) {
                binding.otherProfileNameTv.text = "${user.firstName} ${user.lastName} (${user.committee})"
                binding.otherProfileBioTv.text = user.aboutInfo

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
}