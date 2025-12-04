package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.HomePageBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ResidencyHoursController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: HomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val isOfficer = intent.getBooleanExtra("IS_OFFICER", false)

        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Navigate to Profile Page when profile picture is clicked
        binding.navbar.pfpHolderImgbtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Navigate to Log Residency Time In page
        binding.logResidencyHomeBtn.setOnClickListener {
            // 1. Get the current User ID
            val userId = getSharedPreferences("prefs", MODE_PRIVATE).getString("user_id", null)

            if (userId != null) {
                // 2. Use lifecycleScope to perform the database check asynchronously
                lifecycleScope.launch {
                    // Check if user has a session where timeOut is 0 (ongoing)
                    val ongoing = ResidencyHoursController.getOngoingResidency(userId)

                    if (ongoing != null) {
                        // Ongoing Session Exists -> Go to TIME OUT ---
                        val intent = Intent(this@HomeActivity, LogResidencyTimeOutActivity::class.java)
                        // Pass the ID so TimeOutActivity doesn't have to search for it again immediately
                        intent.putExtra("RESIDENCY_ID", ongoing.id)
                        startActivity(intent)
                    } else {
                        // No Session -> Go to TIME IN ---
                        val intent = Intent(this@HomeActivity, LogResidencyTimeInActivity::class.java)
                        startActivity(intent)
                    }
                }
            } else {
                Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_SHORT).show()
            }
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

        // Show the View Members Page
        binding.viewOtherMembersBtn.setOnClickListener {
            val intent = Intent(this, ViewMembersActivity::class.java)
            intent.putExtra("IS_OFFICER", isOfficer)
            startActivity(intent)
        }

        // Signs out the user
        binding.returnBtn.setOnClickListener {
            // 1. Sign out the current Firebase user
            FirebaseAuth.getInstance().signOut()

            // 2. Clear SharedPreferences (Destroy local user data)
            val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear() // Wipes user_id and any other saved settings
            editor.apply()

            // 3. Navigate to MainActivity and CLEAR TASK
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
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