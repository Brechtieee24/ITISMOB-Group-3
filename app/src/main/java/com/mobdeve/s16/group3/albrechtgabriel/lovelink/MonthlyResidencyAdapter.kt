package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.MonthlyResidencyItemBinding

class MonthlyResidencyAdapter(private val monthlyList: ArrayList<MonthlyResidency>) :
    RecyclerView.Adapter<MonthlyResidencyAdapter.MonthlyResidencyViewHolder>() {

    inner class MonthlyResidencyViewHolder(private val binding: MonthlyResidencyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(monthlyResidency: MonthlyResidency) {
            binding.monthlyResidencyTv.text = monthlyResidency.getFormattedTime()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthlyResidencyViewHolder {
        val binding = MonthlyResidencyItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MonthlyResidencyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonthlyResidencyViewHolder, position: Int) {
        holder.bind(monthlyList[position])
    }

    override fun getItemCount(): Int = monthlyList.size
}