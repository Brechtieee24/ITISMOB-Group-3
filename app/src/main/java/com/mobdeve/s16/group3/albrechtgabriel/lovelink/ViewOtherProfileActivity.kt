package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ViewOtherProfilePageBinding
import kotlin.jvm.java

class ViewOtherProfileActivity : AppCompatActivity() {
    private lateinit var binding: ViewOtherProfilePageBinding
    private lateinit var activityAdapter: ProfileActivityAdapter
    private lateinit var activityList: ArrayList<String>
    private lateinit var monthlyResidencyAdapter: MonthlyResidencyAdapter
    private lateinit var monthlyResidencyList: ArrayList<MonthlyResidency>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewOtherProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide navbar menu initially
        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // PLACEHOLDER PA MEMBER- Get member data from intent
        val memberName = intent.getStringExtra("MEMBER_NAME") ?: "Jules Dela Cruz (MAE)"

        // Load member data
        loadMemberData(memberName)

        // Setup RecyclerView for monthly residency hours
        monthlyResidencyAdapter = MonthlyResidencyAdapter(monthlyResidencyList)
        binding.otherMonthlyResidencyRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.otherMonthlyResidencyRecyclerview.adapter = monthlyResidencyAdapter

        // Setup RecyclerView for activities list
        activityAdapter = ProfileActivityAdapter(activityList)
        binding.otherActivitiesListRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.otherActivitiesListRecyclerview.adapter = activityAdapter

        // Show/hide nav bar options
        binding.navbar.menuIconNavImgbtn.setOnClickListener {
            val menuSection = binding.navbar.navBarContainerLnr
            menuSection.visibility =
                if (menuSection.visibility == View.VISIBLE) {
                    View.GONE
                } else View.VISIBLE
        }

        // Return button always goes back to View Members Page
        binding.returnBtn.setOnClickListener {
            val intent = Intent(this, ViewMembersActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadMemberData(memberName: String) {
        // SAMPLE DATA - replace with actual data from database later
        binding.otherProfileNameTv.text = memberName
        binding.otherProfileBioTv.text = "Hi! My name is Jules."

        monthlyResidencyList = arrayListOf(
            MonthlyResidency("Oct", 490),
            MonthlyResidency("Sep", 490),
            MonthlyResidency("Aug", 490)
        )

        activityList = arrayListOf(
            "Event Activity # 4 (9/12/25)",
            "Event Activity # 3 (9/11/25)",
            "Event Activity # 2 (9/10/25)",
            "Event Activity # 1 (9/9/25)"
        )
    }
}