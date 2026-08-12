package com.example.votingsystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ElectionAdapter(
    private val elections: List<Election>,
    private val onItemClick: (Election) -> Unit
) : RecyclerView.Adapter<ElectionAdapter.ElectionViewHolder>() {

    class ElectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvElectionItemName)
        val tvCode: TextView = view.findViewById(R.id.tvElectionItemCode)
        val tvDate: TextView = view.findViewById(R.id.tvElectionItemDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_election, parent, false)
        return ElectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ElectionViewHolder, position: Int) {
        val election = elections[position]
        holder.tvName.text = election.name
        holder.tvCode.text = "Code: ${election.electionCode}"
        holder.tvDate.text = "${election.date} at ${election.time}"
        holder.itemView.setOnClickListener { onItemClick(election) }
    }

    override fun getItemCount() = elections.size
}