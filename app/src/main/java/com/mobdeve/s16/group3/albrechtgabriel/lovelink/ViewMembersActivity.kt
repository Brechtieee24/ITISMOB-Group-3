package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ViewMembersPageBinding

class ViewMembersActivity : AppCompatActivity() {
    private lateinit var binding: ViewMembersPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewMembersPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // show/hide nav bar options
        binding.navbar.menuIconNavImgbtn.setOnClickListener {
            val menuSection = binding.navbar.navBarContainerLnr
            menuSection.visibility =
                if (menuSection.visibility == View.VISIBLE) {
                    View.GONE
                } else View.VISIBLE
        }
    }
}