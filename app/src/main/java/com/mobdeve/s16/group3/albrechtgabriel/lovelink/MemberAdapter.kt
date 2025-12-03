package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.User

class MemberAdapter(private var memberList: MutableList<User>) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>(){
    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pfp: ImageView = itemView.findViewById(R.id.pfp)
        val memberName: TextView = itemView.findViewById(R.id.name)
        val totalHours: TextView = itemView.findViewById(R.id.totalHours)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        // Inflates your XML layout for each row
        val inflatedView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.members_page_recyler_view, parent, false)

        return MemberViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: MemberAdapter.MemberViewHolder, position: Int) {
        // Get the specific User object from the list
        val user: User = memberList[position]
        holder.memberName.text = "${user.firstName} ${user.lastName}"
        holder.totalHours.text = user.formattedResidencyTime ?: "00:00:00"
        holder.pfp.setImageResource(R.drawable.pfp_icon_holder)
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    fun updateUsers(newUsers: List<User>) {
        memberList.clear()
        memberList.addAll(newUsers)
        notifyDataSetChanged() //To be fixed
    }
}