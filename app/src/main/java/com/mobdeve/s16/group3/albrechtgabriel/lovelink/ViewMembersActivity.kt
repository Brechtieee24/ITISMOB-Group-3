package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ViewMembersPageBinding

class ViewMembersActivity : AppCompatActivity() {

    private lateinit var binding: ViewMembersPageBinding
    private var isOfficer: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewMembersPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isOfficer = intent.getBooleanExtra("IS_OFFICER", false)

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

        setupCommitteeClickListeners()

        // 2. Hide the temporary test button
        binding.testViewProfileBtn.visibility = View.GONE
    }

    private fun setupCommitteeClickListeners() {
        val committeeMap = mapOf(
            binding.CBSF.id to "CBSF",
            binding.CREATIVES.id to "CREATIVES",
            binding.DOCU.id to "DOCU",
            binding.FIN.id to "FIN",
            binding.LOGI.id to "LOGI",
            binding.MAE.id to "MAE",
            binding.MARKETING.id to "MARKETING",
            binding.SECP.id to "SECP",
            binding.SoFo.id to "SoFo"
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

        committeeMap.keys.forEach { imageViewId ->
            findViewById<ImageView>(imageViewId).setOnClickListener(committeeClickListener)
        }
    }

    private fun openMembersPageFor(committeeName: String) {
        val intent = Intent(this, MembersPageActivity::class.java).apply {
            putExtra("COMMITTEE_NAME", committeeName)
            putExtra("IS_OFFICER", isOfficer)
        }
        startActivity(intent)
    }
}
