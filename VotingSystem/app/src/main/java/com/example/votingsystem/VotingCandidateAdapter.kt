package com.example.votingsystem

import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class VotingCandidateAdapter(
    private val candidateList: List<Candidate>,
    private val onSelected: (Candidate) -> Unit
) : RecyclerView.Adapter<VotingCandidateAdapter.ViewHolder>() {

    private var selectedPosition = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardVotingCandidate)
        val tvName: TextView = view.findViewById(R.id.tvVoteCandidateName)
        val ivPhoto: ImageView = view.findViewById(R.id.ivVoteCandidatePhoto)
        val ivSymbol: ImageView = view.findViewById(R.id.ivVoteCandidateSymbol)
        val rbSelect: RadioButton = view.findViewById(R.id.rbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voting_candidate, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val candidate = candidateList[position]
        holder.tvName.text = candidate.name
        
        // Hide Photo View since we are only keeping symbols
        holder.ivPhoto.visibility = View.GONE

        // Load Symbol from Base64
        if (candidate.symbolUrl.isNotEmpty()) {
            try {
                if (candidate.symbolUrl.startsWith("data:image")) {
                    val base64String = if (candidate.symbolUrl.contains(",")) {
                        candidate.symbolUrl.substringAfter(",")
                    } else {
                        candidate.symbolUrl
                    }
                    val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    if (decodedImage != null) {
                        holder.ivSymbol.setImageBitmap(decodedImage)
                        holder.ivSymbol.clearColorFilter()
                    } else {
                        holder.ivSymbol.setImageResource(android.R.drawable.ic_menu_help)
                        holder.ivSymbol.setColorFilter(Color.parseColor("#2C5364"))
                    }
                } else {
                    holder.ivSymbol.setImageURI(Uri.parse(candidate.symbolUrl))
                    holder.ivSymbol.clearColorFilter()
                }
            } catch (e: Exception) {
                holder.ivSymbol.setImageResource(android.R.drawable.ic_menu_help)
                holder.ivSymbol.setColorFilter(Color.parseColor("#2C5364"))
            }
        } else {
            holder.ivSymbol.setImageResource(android.R.drawable.ic_menu_help)
            holder.ivSymbol.setColorFilter(Color.parseColor("#2C5364"))
        }

        holder.rbSelect.isChecked = position == selectedPosition
        
        if (position == selectedPosition) {
            holder.card.strokeColor = Color.parseColor("#2C5364")
        } else {
            holder.card.strokeColor = Color.TRANSPARENT
        }

        holder.itemView.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            onSelected(candidate)
        }
    }

    override fun getItemCount() = candidateList.size
}
