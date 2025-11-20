package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.LogResidencyTimeinBinding

class LogResidencyTimeInActivity : AppCompatActivity() {
    private lateinit var binding: LogResidencyTimeinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LogResidencyTimeinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Residency History button - pass caller activity name
        binding.residencyHistoryBtn.setOnClickListener {
            val intent = Intent(this, ResidencyHistoryActivity::class.java)
            intent.putExtra("CALLER_ACTIVITY", "LogResidencyTimeInActivity")
            startActivity(intent)
        }

        // Activity History button - pass caller activity name
        binding.activityHistoryBtn.setOnClickListener {
            val intent = Intent(this, ActivityHistoryActivity::class.java)
            intent.putExtra("CALLER_ACTIVITY", "LogResidencyTimeInActivity")
            startActivity(intent)
        }

        // Return button - goes back to home
        binding.returnBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}