package com.example.votingsystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ParticipantElectionAdapter(
    private val joinedElections: List<Participant>,
    private val onItemClick: (Participant) -> Unit
) : RecyclerView.Adapter<ParticipantElectionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvElectionItemName)
        val tvCode: TextView = view.findViewById(R.id.tvElectionItemCode)
        val tvDate: TextView = view.findViewById(R.id.tvElectionItemDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_election, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = joinedElections[position]
        holder.tvName.text = item.electionName
        holder.tvCode.text = "Code: ${item.electionCode}"
        holder.tvDate.text = ""
        
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = joinedElections.size
}