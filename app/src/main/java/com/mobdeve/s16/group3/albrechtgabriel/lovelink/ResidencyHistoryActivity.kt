package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ResidencyHistoryPageBinding

class ResidencyHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ResidencyHistoryPageBinding
    private lateinit var residencyAdapter: ResidencyHistoryAdapter
    private lateinit var residencyList: ArrayList<ResidencyItem>
    private var callerActivity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ResidencyHistoryPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide navbar menu initially
        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Get the caller activity name from intent
        callerActivity = intent.getStringExtra("CALLER_ACTIVITY")

        // Initialize sample data
        loadResidencyData()

        // Setup RecyclerView
        residencyAdapter = ResidencyHistoryAdapter(residencyList)
        binding.residencyHistoryRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.residencyHistoryRecyclerview.adapter = residencyAdapter

        // Show/hide nav bar options
        binding.navbar.menuIconNavImgbtn.setOnClickListener {
            val menuSection = binding.navbar.navBarContainerLnr
            menuSection.visibility =
                if (menuSection.visibility == View.VISIBLE) {
                    View.GONE
                } else View.VISIBLE
        }

        // Return button goes back to the caller activity
        binding.returnBtn.setOnClickListener {
            returnToCaller()
        }
    }

    private fun loadResidencyData() {
        // Sample data - replace with actual data from database later
        residencyList = arrayListOf(
            ResidencyItem("June 24,\n2025", "09:00:01", "19:03:11", "133\nminutes"),
            ResidencyItem("June 23,\n2025", "09:00:01", "19:03:11", "133\nminutes"),
            ResidencyItem("June 22,\n2025", "09:00:01", "19:03:11", "133\nminutes"),
            ResidencyItem("June 21,\n2025", "09:00:01", "19:03:11", "133\nminutes"),
            ResidencyItem("June 20,\n2025", "09:00:01", "19:03:11", "133\nminutes")
        )
    }

    private fun returnToCaller() {
        when (callerActivity) {
            "LogResidencyTimeInActivity" -> {
                val intent = Intent(this, LogResidencyTimeInActivity::class.java)
                startActivity(intent)
                finish()
            }
            "LogResidencyTimeOutActivity" -> {
                val intent = Intent(this, LogResidencyTimeOutActivity::class.java)
                startActivity(intent)
                finish()
            }
            // Add more cases for Log Activity pages when they're ready
            else -> {
                // Default: go back to home if caller is unknown
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}