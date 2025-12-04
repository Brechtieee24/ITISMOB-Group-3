package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.MembersPageBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.UserController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.adapter.MemberAdapter
import kotlinx.coroutines.launch

class MembersPageActivity : AppCompatActivity() {

    private lateinit var binding: MembersPageBinding
    private var isOfficer: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MembersPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isOfficer = intent.getBooleanExtra("IS_OFFICER", false)

        val memberAdapter = MemberAdapter(mutableListOf(), isOfficer)
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

        // Get the committee name passed from ViewMembersActivity
        val committeeName = intent.getStringExtra("COMMITTEE_NAME")
        binding.committeeName.text = committeeName ?: "Members"

        if (committeeName != null) {
            lifecycleScope.launch {
                val userList = UserController.filterByCommittee(committeeName)
                memberAdapter.updateUsers(userList)
            }
        }
    }
}
