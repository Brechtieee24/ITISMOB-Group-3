package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ResidencyHistoryItemBinding

class ResidencyHistoryAdapter(private val residencyList: ArrayList<ResidencyItem>) :
    RecyclerView.Adapter<ResidencyHistoryAdapter.ResidencyHistoryViewHolder>() {

    inner class ResidencyHistoryViewHolder(private val binding: ResidencyHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(residency: ResidencyItem) {
            binding.residencyDateTv.text = residency.date
            binding.residencyTimeInTv.text = residency.timeIn
            binding.residencyTimeOutTv.text = residency.timeOut
            binding.residencyTotalTv.text = residency.total
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResidencyHistoryViewHolder {
        val binding = ResidencyHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResidencyHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResidencyHistoryViewHolder, position: Int) {
        holder.bind(residencyList[position])
    }

    override fun getItemCount(): Int = residencyList.size
}