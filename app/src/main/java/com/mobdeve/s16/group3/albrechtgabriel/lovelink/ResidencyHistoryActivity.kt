package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ResidencyHistoryPageBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.ResidencyHoursController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ResidencyHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ResidencyHistoryPageBinding
    private lateinit var residencyAdapter: ResidencyHistoryAdapter

    // FIX: Initialize this immediately. Do not use 'lateinit'.
    private var residencyList: ArrayList<ResidencyItem> = ArrayList()

    private var callerActivity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ResidencyHistoryPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navbar.navBarContainerLnr.visibility = View.GONE
        callerActivity = intent.getStringExtra("CALLER_ACTIVITY")

        residencyAdapter = ResidencyHistoryAdapter(residencyList)
        binding.residencyHistoryRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.residencyHistoryRecyclerview.adapter = residencyAdapter

        loadResidencyData()

        binding.navbar.menuIconNavImgbtn.setOnClickListener {
            val menuSection = binding.navbar.navBarContainerLnr
            menuSection.visibility =
                if (menuSection.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        binding.returnBtn.setOnClickListener {
            returnToCaller()
        }
    }

    private fun loadResidencyData() {
        val userId = getSharedPreferences("prefs", MODE_PRIVATE).getString("user_id", null)

        if (userId != null) {
            lifecycleScope.launch {
                try {
                    // Fetch data
                    val rawLogs = ResidencyHoursController.getMemberResidency(userId)

                    // Clear the existing list before adding new data
                    residencyList.clear()

                    val dateFormat = SimpleDateFormat("MMMM d,\nyyyy", Locale.US)
                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

                    for (log in rawLogs) {
                        val dateStr = dateFormat.format(log.timeIn)
                        val timeInStr = timeFormat.format(log.timeIn)

                        val timeOutStr = if (log.timeOut.time > 0) {
                            timeFormat.format(log.timeOut)
                        } else {
                            "Ongoing"
                        }

                        val durationStr = if (log.timeOut.time > 0) {
                            val diffMillis = log.timeOut.time - log.timeIn.time
                            val totalMinutes = diffMillis / (1000 * 60)
                            "$totalMinutes\nminutes"
                        } else {
                            "Processing"
                        }

                        residencyList.add(
                            ResidencyItem(dateStr, timeInStr, timeOutStr, durationStr)
                        )
                    }

                    // Update the UI
                    residencyAdapter.notifyDataSetChanged()

                    if (residencyList.isEmpty()) {
                        Toast.makeText(this@ResidencyHistoryActivity, "No history found.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@ResidencyHistoryActivity, "Error loading data.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_SHORT).show()
        }
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
            else -> {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}