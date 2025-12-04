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
import com.google.firebase.auth.FirebaseAuth
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.UserController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ProfilePageBinding
import kotlinx.coroutines.launch

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
            Toast.makeText(this, "Photo updated! Remember to save changes.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide navbar menu initially
        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Initialize adapters with empty lists first
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
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val userEmail = currentUser.email
        if (userEmail.isNullOrEmpty()) {
            Toast.makeText(this, "User email not available", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val user = UserController.getUserByEmail(userEmail)
                if (user != null) {
                    // Populate profile info safely
                    binding.profileNameTv.text = getString(
                        R.string.profile_name_template,
                        user.firstName ?: "",
                        user.lastName ?: "",
                        user.committee ?: ""
                    )
                    binding.profileBioTv.text = user.aboutInfo ?: ""
                    binding.profileBioEt.setText(user.aboutInfo ?: "")

                    originalBioText = binding.profileBioTv.text.toString()

                    // Populate residency list (sample data, replace with DB later)
                    monthlyResidencyList.clear()
                    monthlyResidencyList.addAll(calculateMonthlyResidency())
                    monthlyResidencyAdapter.notifyDataSetChanged()

                    // Populate activity list (sample data)
                    // Change this to sample data
                    activityList.clear()
                    activityList.addAll(
                        arrayListOf(
                            "Event Activity # 4 (9/12/25)",
                            "Event Activity # 3 (9/11/25)",
                            "Event Activity # 2 (9/10/25)",
                            "Event Activity # 1 (9/9/25)"
                        )
                    )
                    activityAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@ProfileActivity, "User data not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ProfileActivity, "Error loading user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateMonthlyResidency(): ArrayList<MonthlyResidency> {
        return arrayListOf(
            MonthlyResidency("Oct", 490),
            MonthlyResidency("Sep", 490),
            MonthlyResidency("Aug", 490)
        )
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

        binding.profileBioTv.text = newBioText

        // Get current user email safely
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email
        if (userEmail == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        // Save to Firestore using coroutine
        lifecycleScope.launch {
            try {
                val updatedUser = UserController.updateAboutInfo(userEmail, newBioText)
                if (updatedUser != null) {
                    Toast.makeText(this@ProfileActivity, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                    originalBioText = newBioText
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ProfileActivity, "Error updating profile", Toast.LENGTH_SHORT).show()
            }
        }

        exitEditMode()
        Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
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
