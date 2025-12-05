package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.view.isVisible

object NavbarManager {
    fun setupNavBar(context: Context, isOfficer: Boolean) {
        val activity = context as? Activity ?: return

        val menuIconBtn = activity.findViewById<ImageButton>(R.id.menu_icon_nav_imgbtn)
        val navContainer = activity.findViewById<View>(R.id.nav_bar_container_lnr)
        val pfpButton = activity.findViewById<ImageButton>(R.id.pfp_holder_imgbtn)

        val homeBtn = activity.findViewById<ImageView>(R.id.nav_home_btn)
        val activitiesBtn = activity.findViewById<ImageView>(R.id.nav_acts_btn)
        val membersBtn = activity.findViewById<ImageView>(R.id.nav_members_btn)

        // The menu button should always work
        menuIconBtn?.setOnClickListener {
            navContainer?.visibility = if (navContainer.isVisible) View.GONE else View.VISIBLE
        }

        pfpButton?.setOnClickListener {
            if (context !is ProfileActivity) {
                val intent = Intent(context, ProfileActivity::class.java).apply {
                    putExtra("IS_OFFICER", isOfficer)
                }
                context.startActivity(intent)
            }
        }

        // HOME
        homeBtn?.setOnClickListener {
            if (context !is HomeActivity) {
                // Use flags to clear back stack and go to a fresh HomeActivity
                val intent = Intent(context, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("IS_OFFICER", isOfficer)
                }
                context.startActivity(intent)
            }
        }

        // ACTIVITIES
        activitiesBtn?.setOnClickListener {
            if (context !is ViewActivitiesActivity) {
                val intent = Intent(context, ViewActivitiesActivity::class.java).apply {
                    putExtra("IS_OFFICER", isOfficer)
                }
                context.startActivity(intent)
            }
        }

        // MEMBERS
        membersBtn?.setOnClickListener {
            if (context !is ViewMembersActivity) {
                val intent = Intent(context, ViewMembersActivity::class.java).apply {
                    putExtra("IS_OFFICER", isOfficer)
                }
                context.startActivity(intent)
            }
        }
    }
}

