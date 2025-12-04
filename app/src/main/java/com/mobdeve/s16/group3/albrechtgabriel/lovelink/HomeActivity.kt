package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.UserPreferences
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.HomePageBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: HomePageBinding
    private var isOfficer: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NEW: Get isOfficer from Intent OR SharedPreferences (fallback)
        isOfficer = intent.getBooleanExtra("IS_OFFICER", false)
        if (!isOfficer) {
            // If not passed via intent, try SharedPreferences
            isOfficer = UserPreferences.isOfficer(this)
        }

        // Show correct button based on role
        if (isOfficer) {
            binding.logActivityHomeBtn.visibility = View.VISIBLE
            binding.activityHistoryHomeBtn.visibility = View.GONE
        } else {
            binding.logActivityHomeBtn.visibility = View.GONE
            binding.activityHistoryHomeBtn.visibility = View.VISIBLE
        }

        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Navigate to Profile Page
        binding.navbar.pfpHolderImgbtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Navigate to Log Residency Time In page
        binding.logResidencyHomeBtn.setOnClickListener {
            val intent = Intent(this, LogResidencyTimeInActivity::class.java)
            startActivity(intent)
        }

        // Navigate to Activity History
        binding.activityHistoryHomeBtn.setOnClickListener {
            val intent = Intent(this, ActivityHistoryActivity::class.java)
            intent.putExtra("CALLER_ACTIVITY", "HomeActivity")
            startActivity(intent)
        }

        // TODO for Josh: Navigate to Log Activity page
        binding.logActivityHomeBtn.setOnClickListener {
            // TODO: Add LogActivityActivity when implemented
            // val intent = Intent(this, LogActivityActivity::class.java)
            // startActivity(intent)
        }

        binding.viewActivitiesBtn.setOnClickListener {
            val intent = Intent(this, ViewActivitiesActivity::class.java)
            intent.putExtra("IS_OFFICER", isOfficer)
            startActivity(intent)
        }

        // Pass isOfficer to ViewMembersActivity
        binding.viewOtherMembersBtn.setOnClickListener {
            val intent = Intent(this, ViewMembersActivity::class.java)
            intent.putExtra("IS_OFFICER", isOfficer)
            startActivity(intent)
        }

        // UPDATED: Clear SharedPreferences on logout
        binding.returnBtn.setOnClickListener {
            // Clear SharedPreferences
            UserPreferences.clearAll(this)

            // Sign out from Firebase
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
}