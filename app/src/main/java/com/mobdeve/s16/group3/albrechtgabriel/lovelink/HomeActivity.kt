package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.UserController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.HomePageBinding
import kotlinx.coroutines.launch
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: HomePageBinding
    private var isOfficer: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get initial officer status from intent (optional fallback)
        val intentOfficerStatus = intent.getBooleanExtra("IS_OFFICER", false)
        isOfficer = intentOfficerStatus

        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Check if user is officer and show appropriate button
        checkUserRoleAndSetupUI()

        // Navigate to Profile Page when profile picture is clicked
        binding.navbar.pfpHolderImgbtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Navigate to Log Residency Time In page
        binding.logResidencyHomeBtn.setOnClickListener {
            val intent = Intent(this, LogResidencyTimeInActivity::class.java)
            startActivity(intent)
        }

        // TODO for Josh
        //  MEMBER: Navigate to Log Activity page
        binding.logActivityHomeBtn.setOnClickListener {
            // TODO: Add LogActivityActivity when implemented
            // val intent = Intent(this, LogActivityActivity::class.java)
            // startActivity(intent)
        }

        // MEMBER: Navigate to Activity History page
        binding.activityHistoryHomeBtn.setOnClickListener {
            val intent = Intent(this, ActivityHistoryActivity::class.java)
            intent.putExtra("CALLER_ACTIVITY", "HomeActivity")
            startActivity(intent)
        }

        binding.viewActivitiesBtn.setOnClickListener {
            val intent = Intent(this, ViewActivitiesActivity::class.java)
            intent.putExtra("IS_OFFICER", isOfficer)
            startActivity(intent)
        }

        // Show the View Members Page
        binding.viewOtherMembersBtn.setOnClickListener {
            val intent = Intent(this, ViewMembersActivity::class.java)
            intent.putExtra("IS_OFFICER", isOfficer)
            startActivity(intent)
        }

        // Signs out the user
        binding.returnBtn.setOnClickListener {
            // Sign out the current user
            FirebaseAuth.getInstance().signOut()

            // Navigate back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Show/hide nav bar options
        binding.navbar.menuIconNavImgbtn.setOnClickListener {
            val menuSection = binding.navbar.navBarContainerLnr
            menuSection.visibility =
                if (menuSection.visibility == View.VISIBLE) {
                    View.GONE
                } else View.VISIBLE
        }
    }
    private fun checkUserRoleAndSetupUI() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userEmail = currentUser.email
        if (userEmail.isNullOrEmpty()) {
            Toast.makeText(this, "User email not available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val user = UserController.getUserByEmail(userEmail)
                if (user != null) {
                    isOfficer = user.isOfficer

                    // Show correct button based on role
                    if (isOfficer) {
                        // Officer: Show "Log Activity" button
                        binding.logActivityHomeBtn.visibility = View.VISIBLE
                        binding.activityHistoryHomeBtn.visibility = View.GONE
                    } else {
                        // Member: Show "Activity History" button
                        binding.logActivityHomeBtn.visibility = View.GONE
                        binding.activityHistoryHomeBtn.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(
                        this@HomeActivity,
                        "User data not found",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@HomeActivity,
                    "Error loading user data",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}