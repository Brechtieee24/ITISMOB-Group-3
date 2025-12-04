package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import kotlin.jvm.java

class ViewOtherProfileActivity : AppCompatActivity() {
    private lateinit var binding: ViewOtherProfilePageBinding
    private lateinit var activityAdapter: ProfileActivityAdapter
    private lateinit var activityList: ArrayList<String>
    private lateinit var monthlyResidencyAdapter: MonthlyResidencyAdapter
    private lateinit var monthlyResidencyList: ArrayList<MonthlyResidency>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewOtherProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide navbar menu initially
        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Get member email from intent
        val memberEmail = intent.getStringExtra("MEMBER_EMAIL")

        if (memberEmail.isNullOrEmpty()) {
            Toast.makeText(this, "Member information not available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize adapters with empty lists
        monthlyResidencyList = arrayListOf()
        activityList = arrayListOf()

        monthlyResidencyAdapter = MonthlyResidencyAdapter(monthlyResidencyList)
        binding.otherMonthlyResidencyRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.otherMonthlyResidencyRecyclerview.adapter = monthlyResidencyAdapter

        activityAdapter = ProfileActivityAdapter(activityList)
        binding.otherActivitiesListRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.otherActivitiesListRecyclerview.adapter = activityAdapter

        // Load member data from Firebase
        loadMemberDataFromFirebase(memberEmail)

        // Show/hide nav bar options
        binding.navbar.menuIconNavImgbtn.setOnClickListener {
            val menuSection = binding.navbar.navBarContainerLnr
            menuSection.visibility =
                if (menuSection.visibility == View.VISIBLE) {
                    View.GONE
                } else View.VISIBLE
        }

        // Return button always goes back to View Members Page
        binding.returnBtn.setOnClickListener {
            val intent = Intent(this, ViewMembersActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadMemberDataFromFirebase(memberEmail: String) {
        lifecycleScope.launch {
            try {
                // Get user data
                val user = UserController.getUserByEmail(memberEmail)
                if (user != null) {
                    // Set profile info
                    binding.otherProfileNameTv.text = getString(
                        R.string.profile_name_template,
                        user.firstName,
                        user.lastName,
                        user.committee
                    )
                    binding.otherProfileBioTv.text = user.aboutInfo

                    // Load monthly residency
                    val monthlyData = ResidencyHoursController.computeMonthlyResidency(
                        memberId = memberEmail,
                        year = 2025,
                        months = listOf("october", "september", "august")
                    )

                    monthlyResidencyList.clear()
                    monthlyData.forEach { (month, formattedTime) ->
                        // Parse formatted string back to minutes
                        val minutes = parseFormattedTimeToMinutes(formattedTime)
                        monthlyResidencyList.add(
                            MonthlyResidency(
                                month = month.capitalize(Locale.ROOT).substring(0, 3),
                                totalMinutes = minutes
                            )
                        )
                    }
                    monthlyResidencyAdapter.notifyDataSetChanged()

                    // Load activity participation
                    val events = ActivityParticipationController.getEventsOfUser(memberEmail)
                    activityList.clear()
                    events.forEach { event ->
                        val dateFormat = SimpleDateFormat("M/d/yy", Locale.getDefault())
                        val formattedDate = try {
                            dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(event.date))
                        } catch (e: Exception) {
                            event.date
                        }
                        activityList.add("${event.eventName} ($formattedDate)")
                    }
                    activityAdapter.notifyDataSetChanged()

                } else {
                    Toast.makeText(
                        this@ViewOtherProfileActivity,
                        "Member not found",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@ViewOtherProfileActivity,
                    "Error loading member data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun parseFormattedTimeToMinutes(formattedTime: String): Int {
        return try {
            // Format is "X hours and Y minutes"
            val parts = formattedTime.split(" ")
            val hours = parts[0].toIntOrNull() ?: 0
            val minutes = parts.getOrNull(3)?.toIntOrNull() ?: 0
            (hours * 60) + minutes
        } catch (e: Exception) {
            0
        }
    }
}