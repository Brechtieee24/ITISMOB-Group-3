package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.EventController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ActivityPageBinding
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.Event
import kotlinx.coroutines.launch

class ActivitiesPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPageBinding
    private var isOfficer: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPageBinding.inflate(layoutInflater)
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

        val eventId = intent.getStringExtra(ViewActivitiesActivity.EVENT_ID_KEY)

        if (eventId != null) {
            lifecycleScope.launch {
                val event = EventController.getEventById(eventId)
                if (event != null) {
                    populateUi(event)
                }
            }
        }
    }

    private fun populateUi(event: Event) {
        binding.activityTitle.text = event.eventName
        binding.activityDate.text = event.date
        binding.activityDescription.text = event.description.ifEmpty { "No description provided." }
        // Implement image
    }
}
