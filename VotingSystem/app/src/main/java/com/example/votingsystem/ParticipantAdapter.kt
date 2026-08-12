package com.example.votingsystem

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ParticipantAdapter(
    private val participants: List<Participant>,
    private val isAdmin: Boolean = false,
    private val onAction: (Participant, String) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder>() {

    class ParticipantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvParticipantName)
        val tvId: TextView = view.findViewById(R.id.tvParticipantId)
        val tvTime: TextView = view.findViewById(R.id.tvParticipantTime)
        val layoutActions: View = view.findViewById(R.id.layoutActionButtons)
        val btnApprove: ImageButton = view.findViewById(R.id.btnApproveParticipant)
        val btnReject: ImageButton = view.findViewById(R.id.btnRejectParticipant)
        val tvStatus: TextView = view.findViewById(R.id.tvParticipantStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participant, parent, false)
        return ParticipantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val participant = participants[position]
        holder.tvName.text = participant.fullName
        holder.tvId.text = "ID: ${participant.idNumber}"
        
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        holder.tvTime.text = sdf.format(Date(participant.timestamp))

        if (isAdmin && participant.status == "pending") {
            holder.layoutActions.visibility = View.VISIBLE
            holder.tvStatus.visibility = View.GONE
        } else {
            holder.layoutActions.visibility = View.GONE
            holder.tvStatus.visibility = View.VISIBLE
            holder.tvStatus.text = participant.status.uppercase()
            
            when (participant.status) {
                "approved" -> holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                "rejected" -> holder.tvStatus.setTextColor(Color.parseColor("#F44336"))
                else -> holder.tvStatus.setTextColor(Color.parseColor("#FFC107"))
            }
        }

        holder.btnApprove.setOnClickListener { onAction(participant, "approved") }
        holder.btnReject.setOnClickListener { onAction(participant, "rejected") }
    }

    override fun getItemCount() = participants.size
}
