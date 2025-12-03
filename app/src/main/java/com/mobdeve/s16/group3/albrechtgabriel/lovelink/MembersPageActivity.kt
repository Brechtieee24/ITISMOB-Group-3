package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.MembersPageBinding

class MembersPageActivity : AppCompatActivity() {

    private lateinit var binding: MembersPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MembersPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Show/hide nav bar options
        binding.navbar.menuIconNavImgbtn.setOnClickListener {
            val menuSection = binding.navbar.navBarContainerLnr
            menuSection.visibility =
                if (menuSection.visibility == View.VISIBLE) {
                    View.GONE
                } else View.VISIBLE
        }

        // Get the committee name passed from ViewMembersActivity
        val committeeName = intent.getStringExtra("COMMITTEE_NAME")

        // Use binding to access the TextView and set its text
        binding.textView.text = committeeName ?: "Members" // Display the committee name as the title

        // --- TODO: Add RecyclerView and Firebase logic here in the next step ---
    }
}
