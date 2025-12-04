package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.MembersPageBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.UserController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.adapter.MemberAdapter
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.UserPreferences
import kotlinx.coroutines.launch

class MembersPageActivity : AppCompatActivity() {

    private lateinit var binding: MembersPageBinding
    private var isOfficer: Boolean = false
    private lateinit var memberAdapter: MemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MembersPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NEW: Get isOfficer from Intent OR SharedPreferences (fallback)
        isOfficer = intent.getBooleanExtra("IS_OFFICER", false)
        if (!isOfficer) {
            // If not passed via intent, try SharedPreferences
            isOfficer = UserPreferences.isOfficer(this)
        }

        memberAdapter = MemberAdapter(mutableListOf(), isOfficer)
        binding.membersRecyclerView.adapter = memberAdapter
        binding.membersRecyclerView.layoutManager = LinearLayoutManager(this)

        if (!isOfficer) {
            binding.totalHoursHeader.visibility = View.GONE
            binding.filterMembersbtn.visibility = View.GONE
        }

        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Show/hide nav bar options
        binding.navbar.menuIconNavImgbtn.setOnClickListener {
            val menuSection = binding.navbar.navBarContainerLnr
            menuSection.visibility =
                if (menuSection.visibility == View.VISIBLE) {
                    View.GONE
                } else View.VISIBLE
        }

        binding.returnbtn.setOnClickListener {
            finish()
        }

        // Get the committee name
        val committeeName = intent.getStringExtra("COMMITTEE_NAME")
        binding.committeeName.text = committeeName ?: "Members"

        if (committeeName != null) {
            lifecycleScope.launch {
                val userList = UserController.filterByCommittee(committeeName)
                memberAdapter.updateUsers(userList)
                setupFilterDialog(committeeName)
            }
        }
    }

    private fun loadMembers(committeeName: String, minHours: Int? = null, maxHours: Int? = null) {
        lifecycleScope.launch {
            val userList = UserController.filterByCommitteeAndHour(committeeName, minHours, maxHours)
            memberAdapter.updateUsers(userList)
        }
    }

    private fun setupFilterDialog(committeeName: String) {
        val dialogContainer = binding.dialogFilterMembersContainer
        val dialogView = binding.filterMembersDialog

        binding.filterMembersbtn.setOnClickListener {
            dialogContainer.visibility = View.VISIBLE
        }

        dialogView.closebtn.setOnClickListener {
            dialogContainer.visibility = View.GONE
        }

        dialogView.confirmbtn.setOnClickListener {
            val minHoursText = dialogView.minHoursEditText.text.toString()
            val maxHoursText = dialogView.maxHoursEditText.text.toString()

            val minHours = minHoursText.toIntOrNull()
            val maxHours = maxHoursText.toIntOrNull()

            if (minHours != null && maxHours != null && minHours > maxHours) {
                Toast.makeText(
                    this,
                    "Min hours cannot be greater than Max hours.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            loadMembers(committeeName, minHours, maxHours)

            dialogContainer.visibility = View.GONE
        }
    }
}