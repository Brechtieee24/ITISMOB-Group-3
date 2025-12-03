package com.mobdeve.s16.group3.albrechtgabriel.lovelink.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.R
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.Event

class ActivitiesAdapter (private var activitiesList: MutableList<Event>, private val onItemClicked: (Event) -> Unit) : RecyclerView.Adapter<ActivitiesAdapter.ActivitiesViewHolder>(){
    inner class ActivitiesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val activityPoster: ImageView = itemView.findViewById(R.id.activityPoster)
        val activityTitle: TextView = itemView.findViewById(R.id.activityTitle)
        val activityDate: TextView = itemView.findViewById(R.id.activityDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivitiesViewHolder {
        val inflatedView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_activities_page_recycler_view, parent, false)

        return ActivitiesViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ActivitiesViewHolder, position: Int) {
        val currentEvent = activitiesList[position]
        holder.activityTitle.text = currentEvent.eventName
        holder.activityDate.text = currentEvent.date
        holder.activityPoster.setImageResource(R.drawable.activities_poster)
        // No image yet
        holder.itemView.setOnClickListener {
            onItemClicked(currentEvent)
        }
    }

    override fun getItemCount(): Int {
        return activitiesList.size
    }

    fun updateEvents(newEvents: List<Event>) {
        activitiesList.clear()
        activitiesList.addAll(newEvents)
        notifyDataSetChanged()
    }
}