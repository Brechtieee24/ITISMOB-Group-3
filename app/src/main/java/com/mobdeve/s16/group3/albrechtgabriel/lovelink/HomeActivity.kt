package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.HomePageBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: HomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navbar.navBarContainerLnr.visibility = View.GONE

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

        // TODO for Josh: Navigate to Log Activity page
        binding.logActivityHomeBtn.setOnClickListener {
            // TODO: Add LogActivityActivity when implemented
            // val intent = Intent(this, LogActivityActivity::class.java)
            // startActivity(intent)
        }

        // TODO for Josh: Navigate to View Activities page
        binding.viewActivitiesBtn.setOnClickListener {
            // TODO: Add ViewActivitiesActivity when implemented
            // val intent = Intent(this, ViewActivitiesActivity::class.java)
            // startActivity(intent)
        }

        // Show the View Members Page
        binding.viewOtherMembersBtn.setOnClickListener {
            val intent = Intent(this, ViewMembersActivity::class.java)
            startActivity(intent)
        }

        // Signs out the user
        binding.returnBtn.setOnClickListener {
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