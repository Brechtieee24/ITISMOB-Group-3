package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.view.ViewGroup
import androidx.core.view.isVisible

object NavbarManager {
    fun setupNavBar(context: Context) {
        val activity = context as Activity

        val menuIconBtn = activity.findViewById<ImageButton>(R.id.menu_icon_nav_imgbtn)
        val navContainer = activity.findViewById<View>(R.id.nav_bar_container_lnr)

        val homeBtn = activity.findViewById<Button>(R.id.nav_home_btn)
        val residencyBtn = activity.findViewById<Button>(R.id.nav_residency_btn)
        val activitiesBtn = activity.findViewById<Button>(R.id.nav_acts_btn)
        val membersBtn = activity.findViewById<Button>(R.id.nav_members_btn)
        val aboutBtn = activity.findViewById<Button>(R.id.nav_about_btn)

        menuIconBtn?.setOnClickListener {
            if (navContainer != null) {
                navContainer.visibility = if (navContainer.isVisible) View.GONE else View.VISIBLE
            }
        }


        // HOME
        homeBtn?.setOnClickListener {
            if (context !is HomeActivity) {
                context.startActivity(Intent(context, HomeActivity::class.java))
            }
        }

        // RESIDENCY (Maps to Log/History or Residency Page)
        residencyBtn?.setOnClickListener {
            // Update this class to your actual Residency Activity
            if (context !is LogResidencyTimeInActivity) {
                context.startActivity(Intent(context, LogResidencyTimeInActivity::class.java))
            }
        }

        // ACTIVITIES
        activitiesBtn?.setOnClickListener {
            if (context !is ActivitiesPageActivity) {
                context.startActivity(Intent(context, ActivitiesPageActivity::class.java))
            }
        }

        // MEMBERS
        membersBtn?.setOnClickListener {
            if (context !is MembersPageActivity) {
                context.startActivity(Intent(context, MembersPageActivity::class.java))
            }
        }

        // ABOUT / PROFILE (Using "About" as Profile based on context, or change to AboutActivity)
        aboutBtn?.setOnClickListener {
            if (context !is ProfileActivity) {
                context.startActivity(Intent(context, ProfileActivity::class.java))
            }
        }
    }
}