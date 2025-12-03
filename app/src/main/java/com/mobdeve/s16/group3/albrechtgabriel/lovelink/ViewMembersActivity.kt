package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ViewMembersPageBinding

class ViewMembersActivity : AppCompatActivity() {

    private lateinit var binding: ViewMembersPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewMembersPageBinding.inflate(layoutInflater)
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

        // 2. Hide the temporary test button
        binding.testViewProfileBtn.visibility = View.GONE

        // 3. Set up click listeners for all committee images
        setupCommitteeClickListeners()
    }

    private fun setupCommitteeClickListeners() {
        // Create a map to link each ImageView's ID to its committee name
        val committeeMap = mapOf(
            binding.imageView3.id to "CBSF",
            binding.imageView5.id to "Creatives",
            binding.imageView6.id to "Docu",
            binding.imageView7.id to "FIN",
            binding.imageView8.id to "LOGI",
            binding.imageView9.id to "MAE",
            binding.imageView10.id to "Marketing",
            binding.imageView11.id to "SECP",
            binding.imageView12.id to "SOFO"
        )

        // Create a single, reusable click listener
        val committeeClickListener = View.OnClickListener { view ->
            // Look up the committee name using the ID of the clicked view
            val committeeName = committeeMap[view.id]
            if (committeeName != null) {
                // If a name is found, open the MembersPageActivity
                openMembersPageFor(committeeName)
            }
        }

        // Apply this listener to each ImageView in the map
        committeeMap.keys.forEach { imageViewId ->
            findViewById<ImageView>(imageViewId).setOnClickListener(committeeClickListener)
        }
    }

    private fun openMembersPageFor(committeeName: String) {
        // Create an Intent to start MembersPageActivity
        val intent = Intent(this, MembersPageActivity::class.java).apply {
            // Pass the committee name as an "extra"
            putExtra("COMMITTEE_NAME", committeeName)
        }
        startActivity(intent)
    }
}
