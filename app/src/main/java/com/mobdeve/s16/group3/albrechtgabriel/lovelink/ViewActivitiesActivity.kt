package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.adapter.ActivitiesAdapter
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.EventController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ViewActivitiesPageBinding
import kotlinx.coroutines.launch

class ViewActivitiesActivity : AppCompatActivity() {
    private lateinit var binding: ViewActivitiesPageBinding
    private lateinit var activitiesAdapter: ActivitiesAdapter
    private var isOfficer: Boolean = false

    companion object {
        const val EVENT_ID_KEY = "EVENT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewActivitiesPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isOfficer = intent.getBooleanExtra("IS_OFFICER", false)
        val dialogView = binding.addEventDialog


        if (!isOfficer) {
            binding.addEventbtn.visibility = View.GONE
        }

        binding.addEventbtn.setOnClickListener {
            binding.dialogAddEventContainer.visibility = View.VISIBLE
        }

        dialogView.closebtn.setOnClickListener {
            binding.dialogAddEventContainer.visibility = View.GONE
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

        dialogView.confirmbtn.setOnClickListener {
            val eventName = dialogView.activityNameEditText.text.toString().trim()
            val eventDate = dialogView.dateEditText.text.toString().trim()

            if (eventName.isNotEmpty() && eventDate.isNotEmpty()) {
                lifecycleScope.launch {
                    val success = EventController.addEvent(eventName, eventDate)
                    if (success) {
                        Toast.makeText(this@ViewActivitiesActivity, "Event added!", Toast.LENGTH_SHORT).show()
                        val eventList = EventController.getEvents()
                        activitiesAdapter.updateEvents(eventList)
                    } else {
                        Toast.makeText(this@ViewActivitiesActivity, "Failed to add event.", Toast.LENGTH_SHORT).show()
                    }

                    // Clear fields and hide the dialog
                    dialogView.activityNameEditText.text.clear()
                    dialogView.dateEditText.text.clear()
                    binding.dialogAddEventContainer.visibility = View.GONE
                }
            } else {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.returnbtn.setOnClickListener {
            finish()
        }

        val activitiesAdapter = ActivitiesAdapter(mutableListOf()) { event ->
            val intent = Intent(this, ActivitiesPageActivity::class.java)
            intent.putExtra(EVENT_ID_KEY, event.id)
            intent.putExtra("IS_OFFICER", isOfficer)
            startActivity(intent)
        }

        binding.activitiesRecyclerView.adapter = activitiesAdapter
        binding.activitiesRecyclerView.layoutManager = GridLayoutManager(this, 2)

        lifecycleScope.launch {
            val eventList = EventController.getEvents()
            activitiesAdapter.updateEvents(eventList)
        }
    }
}