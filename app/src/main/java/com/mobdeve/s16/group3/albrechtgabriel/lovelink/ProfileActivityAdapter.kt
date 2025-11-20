package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ProfileActivityItemBinding

class ProfileActivityAdapter(private val activityList: ArrayList<String>) :
    RecyclerView.Adapter<ProfileActivityAdapter.ProfileActivityViewHolder>() {

    inner class ProfileActivityViewHolder(private val binding: ProfileActivityItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(activityText: String) {
            binding.profileActivityNameTv.text = activityText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileActivityViewHolder {
        val binding = ProfileActivityItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProfileActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileActivityViewHolder, position: Int) {
        holder.bind(activityList[position])
    }

    override fun getItemCount(): Int = activityList.size
}