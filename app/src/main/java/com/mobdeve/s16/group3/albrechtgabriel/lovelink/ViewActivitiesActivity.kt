package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.adapter.ActivitiesAdapter
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.controller.EventController
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ViewActivitiesPageBinding
import kotlinx.coroutines.launch

class ViewActivitiesActivity : AppCompatActivity() {
    private lateinit var binding: ViewActivitiesPageBinding

    companion object {
        const val EVENT_ID_KEY = "EVENT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewActivitiesPageBinding.inflate(layoutInflater)
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

        binding.returnbtn.setOnClickListener {
            finish()
        }

        val activitiesAdapter = ActivitiesAdapter(mutableListOf()) { event ->
            val intent = Intent(this, ActivitiesPageActivity::class.java)
            intent.putExtra(EVENT_ID_KEY, event.id) // Pass the document ID
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