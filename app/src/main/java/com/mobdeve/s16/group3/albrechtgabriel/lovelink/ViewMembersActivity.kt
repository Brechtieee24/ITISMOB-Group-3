package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.UserController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ViewMembersPageBinding
import kotlinx.coroutines.launch

class ViewMembersActivity : AppCompatActivity() {
    private lateinit var binding: ViewMembersPageBinding
    private var isOfficer: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewMembersPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Get isOfficer from intent if passed
        isOfficer = intent.getBooleanExtra("IS_OFFICER", false)

        // Check user role and load appropriate view
        checkUserRoleAndLoadMembers()

        // Show/hide nav bar options
        binding.navbar.menuIconNavImgbtn.setOnClickListener {
            val menuSection = binding.navbar.navBarContainerLnr
            menuSection.visibility =
                if (menuSection.visibility == View.VISIBLE) {
                    View.GONE
                } else View.VISIBLE
        }
    }

    private fun checkUserRoleAndLoadMembers() {
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

                    // Show/hide total hours header based on role
                    // Note: This requires updating members_page.xml to add IDs
                    // For now, we'll just show toast

                    // Load members with appropriate view
                    // TODO for Josh: implement the RecyclerView population
                    // Example:
                    // val members = UserController.filterByCommittee("MAE")
                    // val adapter = MemberAdapter(members.toMutableList(), isOfficer)
                    // binding.membersRecyclerView.adapter = adapter

                    Toast.makeText(
                        this@ViewMembersActivity,
                        if (isOfficer) "Officer View" else "Member View",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@ViewMembersActivity,
                        "User data not found",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@ViewMembersActivity,
                    "Error loading user data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}