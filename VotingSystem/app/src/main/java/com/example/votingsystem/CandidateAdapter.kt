package com.example.votingsystem

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CandidateAdapter(private val candidateList: List<Candidate>) :
    RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder>() {

    class CandidateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumber: TextView = view.findViewById(R.id.tvCandidateNumber)
        val tvName: TextView = view.findViewById(R.id.tvCandidateName)
        val ivPhoto: ImageView = view.findViewById(R.id.ivCandidatePhoto)
        val ivSymbol: ImageView = view.findViewById(R.id.ivCandidateSymbol)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_candidate, parent, false)
        return CandidateViewHolder(view)
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        val candidate = candidateList[position]
        holder.tvNumber.text = "Candidate ${position + 1}"
        holder.tvName.text = candidate.name

        // Load Symbol from Base64
        if (candidate.symbolUrl.isNotEmpty()) {
            try {
                if (candidate.symbolUrl.startsWith("data:image")) {
                    val base64String = candidate.symbolUrl.substringAfter("base64,")
                    val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                    val decodedImage = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    if (decodedImage != null) {
                        holder.ivSymbol.setImageBitmap(decodedImage)
                        holder.ivSymbol.clearColorFilter()
                    } else {
                        holder.ivSymbol.setImageResource(android.R.drawable.ic_menu_gallery)
                        holder.ivSymbol.setColorFilter(android.graphics.Color.parseColor("#2C5364"))
                    }
                } else {
                    holder.ivSymbol.setImageURI(android.net.Uri.parse(candidate.symbolUrl))
                    holder.ivSymbol.clearColorFilter()
                }
            } catch (e: Exception) {
                holder.ivSymbol.setImageResource(android.R.drawable.ic_menu_gallery)
                holder.ivSymbol.setColorFilter(android.graphics.Color.parseColor("#2C5364"))
            }
        } else {
            holder.ivSymbol.setImageResource(android.R.drawable.ic_menu_gallery)
            holder.ivSymbol.setColorFilter(android.graphics.Color.parseColor("#2C5364"))
        }
    }

    override fun getItemCount() = candidateList.size
}