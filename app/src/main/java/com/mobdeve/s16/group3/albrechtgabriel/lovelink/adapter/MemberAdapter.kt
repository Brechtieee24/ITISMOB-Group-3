package com.mobdeve.s16.group3.albrechtgabriel.lovelink.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.R
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.ViewOtherProfileActivity
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.model.User

class MemberAdapter(
    private var memberList: MutableList<User>,
    private val isOfficerView: Boolean = false // Add parameter to control visibility
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pfp: ImageView = itemView.findViewById(R.id.pfp)
        val memberName: TextView = itemView.findViewById(R.id.name)
        val totalHours: TextView = itemView.findViewById(R.id.totalHours)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val inflatedView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.members_page_recyler_view, parent, false)
        return MemberViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val user: User = memberList[position]
        holder.memberName.text = "${user.firstName} ${user.lastName}"
        holder.pfp.setImageResource(R.drawable.pfp_icon_holder)

        // Show/hide total hours based on user role
        if (isOfficerView) {
            holder.totalHours.visibility = View.VISIBLE
            holder.totalHours.text = user.formattedResidencyTime ?: "00:00:00"
        } else {
            holder.totalHours.visibility = View.GONE
        }

        // Add click listener to open ViewOtherProfileActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ViewOtherProfileActivity::class.java)
            intent.putExtra("MEMBER_EMAIL", user.email)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    fun updateUsers(newUsers: List<User>) {
        memberList.clear()
        memberList.addAll(newUsers)
        notifyDataSetChanged()
    }
}