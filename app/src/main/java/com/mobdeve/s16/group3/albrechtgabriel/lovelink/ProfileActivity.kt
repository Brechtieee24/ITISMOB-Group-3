package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.UserController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ProfilePageBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ActivityParticipationController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ResidencyHoursController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ProfilePageBinding
    private lateinit var activityAdapter: ProfileActivityAdapter
    private lateinit var monthlyResidencyAdapter: MonthlyResidencyAdapter
    private var activityList: ArrayList<String> = arrayListOf()
    private var monthlyResidencyList: ArrayList<MonthlyResidency> = arrayListOf()
    private var originalBioText: String = ""
    private var selectedImageUri: Uri? = null

    // Photo picker launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.profilePictureIv.setImageURI(it)
            // FOR FUTURE RELEASE
            Toast.makeText(this, "Photo updated! Remember to save changes.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide navbar menu initially
        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Initialize adapters
        monthlyResidencyAdapter = MonthlyResidencyAdapter(monthlyResidencyList)
        binding.monthlyResidencyRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.monthlyResidencyRecyclerview.adapter = monthlyResidencyAdapter

        activityAdapter = ProfileActivityAdapter(activityList)
        binding.activitiesListRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.activitiesListRecyclerview.adapter = activityAdapter

        // Load profile data from Firestore
        loadProfileData()

        // Navbar toggle
        binding.navbar.menuIconNavImgbtn.setOnClickListener {
            binding.navbar.navBarContainerLnr.visibility =
                if (binding.navbar.navBarContainerLnr.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // Profile picture click
        binding.profilePictureIv.setOnClickListener { changeProfilePhoto() }

        // Bio buttons
        binding.updateBioBtn.setOnClickListener { enterEditMode() }
        binding.saveBioBtn.setOnClickListener { saveBioChanges() }
        binding.cancelChangesBtn.setOnClickListener { cancelChanges() }

        // Return button
        binding.returnBtn.setOnClickListener {
            finish()
        }
    }

    private fun loadProfileData() {
        // 1. Get User ID from SharedPreferences
        val userId = getSharedPreferences("prefs", MODE_PRIVATE).getString("user_id", null)

        if (userId == null) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                // --- A. Fetch User Info ---
                val user = UserController.getUserById(userId)

                if (user != null) {
                    binding.profileNameTv.text = getString(
                        R.string.profile_name_template,
                        user.firstName,
                        user.lastName,
                        if(user.committee.isNotEmpty()) user.committee else "No Committee"
                    )

                    val bio = if(user.aboutInfo.isNotEmpty()) user.aboutInfo else "No bio yet."
                    binding.profileBioTv.text = bio
                    binding.profileBioEt.setText(bio)

                    originalBioText = bio

                    // --- B. Fetch Residency Data (Dynamic Calculation) ---
                    loadResidencyData(userId)

                    // --- C. Fetch Activity Data ---
                    loadActivityData(userId)

                } else {
                    Toast.makeText(this@ProfileActivity, "User data not found in DB.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ProfileActivity, "Error loading user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun loadResidencyData(userId: String) {
        // Fetch raw logs
        val logs = ResidencyHoursController.getMemberResidency(userId)

        // Clear list
        monthlyResidencyList.clear()

        if (logs.isNotEmpty()) {
            // Map to store month -> totalMinutes
            val monthlyTotals = mutableMapOf<String, Int>()
            val monthFormatter = SimpleDateFormat("MMM", Locale.US) // "Jan", "Feb"

            for (log in logs) {
                // Only process completed logs
                if (log.timeOut.time > 0) {
                    val monthName = monthFormatter.format(log.timeIn)

                    val diffMillis = log.timeOut.time - log.timeIn.time
                    val minutes = (diffMillis / (1000 * 60)).toInt()

                    val currentTotal = monthlyTotals.getOrDefault(monthName, 0)
                    monthlyTotals[monthName] = currentTotal + minutes
                }
            }

            // Convert Map to List and Add to Adapter
            monthlyTotals.forEach { (month, minutes) ->
                monthlyResidencyList.add(MonthlyResidency(month, minutes))
            }
        } else {
            // Optional: Add empty state placeholders if needed
            monthlyResidencyList.add(MonthlyResidency("No Data", 0))
        }

        monthlyResidencyAdapter.notifyDataSetChanged()
    }

    private suspend fun loadActivityData(userId: String) {
        val events = ActivityParticipationController.getEventsOfUser(userId)

        activityList.clear()

        for (event in events) {
            activityList.add("${event.eventName} (${event.date})")
        }

        if (activityList.isEmpty()) {
            activityList.add("No activities yet.")
        }

        activityAdapter.notifyDataSetChanged()
    }

    private fun enterEditMode() {
        originalBioText = binding.profileBioTv.text.toString()
        binding.profileBioEt.setText(originalBioText)

        binding.profileBioTv.visibility = View.GONE
        binding.profileBioEt.visibility = View.VISIBLE
        binding.viewModeContainer.visibility = View.GONE
        binding.editModeContainer.visibility = View.VISIBLE
        binding.returnBtn.visibility = View.GONE

        binding.profileBioEt.requestFocus()
        binding.profileBioEt.setSelection(binding.profileBioEt.text.length)
    }

    private fun saveBioChanges() {
        val newBioText = binding.profileBioEt.text.toString().trim()
        if (newBioText.isEmpty()) {
            Toast.makeText(this, "Bio cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = getSharedPreferences("prefs", MODE_PRIVATE).getString("user_id", null)

        if (userId == null) {
            Toast.makeText(this, "Session expired.", Toast.LENGTH_SHORT).show()
            return
        }

        // Save to Firestore using coroutine
        lifecycleScope.launch {
            try {
                // Using the updated Controller function that takes docId
                val updatedUser = UserController.updateAboutInfo(userId, newBioText)

                if (updatedUser != null) {
                    binding.profileBioTv.text = newBioText
                    originalBioText = newBioText
                    Toast.makeText(this@ProfileActivity, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                    exitEditMode() // Only exit if save was successful
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ProfileActivity, "Error updating profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cancelChanges() {
        binding.profileBioEt.setText(originalBioText)
        if (selectedImageUri != null) {
            binding.profilePictureIv.setImageResource(R.drawable.pfp_icon_holder)
            selectedImageUri = null
        }
        exitEditMode()
        Toast.makeText(this, "Changes cancelled", Toast.LENGTH_SHORT).show()
    }

    private fun exitEditMode() {
        binding.profileBioTv.visibility = View.VISIBLE
        binding.profileBioEt.visibility = View.GONE
        binding.viewModeContainer.visibility = View.VISIBLE
        binding.editModeContainer.visibility = View.GONE
        binding.returnBtn.visibility = View.VISIBLE
    }

    private fun changeProfilePhoto() {
        pickImageLauncher.launch("image/*")
    }
}