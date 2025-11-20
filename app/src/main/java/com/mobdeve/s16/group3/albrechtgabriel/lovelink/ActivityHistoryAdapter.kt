package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ActivityHistoryItemBinding

class ActivityHistoryAdapter(private val activityList: ArrayList<ActivityItem>) :
    RecyclerView.Adapter<ActivityHistoryAdapter.ActivityHistoryViewHolder>() {

    inner class ActivityHistoryViewHolder(private val binding: ActivityHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: ActivityItem) {
            binding.activityNameTv.text = activity.activityName
            binding.activityDateTv.text = activity.activityDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityHistoryViewHolder {
        val binding = ActivityHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActivityHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityHistoryViewHolder, position: Int) {
        holder.bind(activityList[position])
    }

    override fun getItemCount(): Int = activityList.size
}