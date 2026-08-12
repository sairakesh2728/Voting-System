package com.example.votingsystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParticipantsActivity : AppCompatActivity() {

    private lateinit var adapter: ParticipantAdapter
    private val participantsList = mutableListOf<Participant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_participants)

        val token = VotingApp.authToken
        if (token == null) {
            // Safety check: if token is lost, don't crash, just go to login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        val electionId = intent.getStringExtra("ELECTION_ID") ?: ""
        val electionName = intent.getStringExtra("ELECTION_NAME") ?: "Participants"

        findViewById<TextView>(R.id.tvParticipantsTitle).text = electionName

        val rv = findViewById<RecyclerView>(R.id.rvParticipants)
        val pb = findViewById<ProgressBar>(R.id.pbParticipants)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyParticipants)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarParticipants)

        toolbar.setNavigationOnClickListener { 
            onBackPressedDispatcher.onBackPressed()
        }

        rv.layoutManager = LinearLayoutManager(this)
        adapter = ParticipantAdapter(participantsList, isAdmin = true) { participant, newStatus ->
            updateStatus(participant, newStatus, pb)
        }
        rv.adapter = adapter

        fetchParticipants(electionId, pb, tvEmpty)
    }

    private fun fetchParticipants(electionId: String, pb: ProgressBar, tvEmpty: TextView) {
        pb.visibility = View.VISIBLE
        val token = VotingApp.authToken ?: ""

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getParticipants(token, electionId)
                }
                
                if (response.isSuccessful) {
                    val apiParticipants = response.body() ?: emptyList()
                    participantsList.clear()
                    apiParticipants.forEach { api ->
                        participantsList.add(Participant().apply {
                            this.fullName = api.fullName
                            this.idNumber = api.idNumber
                            this.status = api.status
                            this.userUid = api.id // Reusing id as UID for model
                        })
                    }
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ParticipantsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                pb.visibility = View.GONE
                tvEmpty.visibility = if (participantsList.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateStatus(participant: Participant, newStatus: String, pb: ProgressBar) {
        pb.visibility = View.VISIBLE
        val token = VotingApp.authToken ?: ""

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.updateParticipantStatus(
                        token, participant.userUid, ParticipantStatusUpdate(newStatus)
                    )
                }

                if (response.isSuccessful) {
                    Toast.makeText(this@ParticipantsActivity, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                    // Refresh
                    participant.status = newStatus
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ParticipantsActivity, "Failed to update", Toast.LENGTH_SHORT).show()
            } finally {
                pb.visibility = View.GONE
            }
        }
    }
}
