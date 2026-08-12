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
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JoinedElectionsActivity : AppCompatActivity() {

    private lateinit var adapter: ParticipantElectionAdapter
    private val joinedList = mutableListOf<Participant>()
    private val electionsCache = mutableMapOf<String, List<Candidate>>()
    private val electionsMetadata = mutableMapOf<String, ApiElection>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joined_elections)

        val rv = findViewById<RecyclerView>(R.id.rvJoinedElections)
        val pb = findViewById<ProgressBar>(R.id.pbJoinedElections)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyJoined)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.selectedItemId = R.id.nav_cast_vote

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_my_elections -> {
                    startActivity(Intent(this, MyElectionsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_cast_vote -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        rv.layoutManager = LinearLayoutManager(this)
        adapter = ParticipantElectionAdapter(joinedList) { participant ->
            val intent = Intent(this, VotingActivity::class.java)
            intent.putExtra("ELECTION_ID", participant.electionId)
            intent.putExtra("ELECTION_NAME", participant.electionName)
            
            // Pass timings from metadata if available
            // We can store these in the Participant model or a metadata map
            val electionMetadata = electionsMetadata[participant.electionId]
            if (electionMetadata != null) {
                intent.putExtra("START_TIME", electionMetadata.startTime)
                intent.putExtra("END_TIME", electionMetadata.endTime)
            }
            
            // Pass the candidates from cache using global shared list to avoid TransactionTooLargeException
            val candidates = electionsCache[participant.electionId]
            if (candidates != null) {
                VotingApp.tempCandidateList.clear()
                VotingApp.tempCandidateList.addAll(candidates)
            }
            startActivity(intent)
        }
        rv.adapter = adapter

        fetchElections(pb, tvEmpty)
    }

    private fun fetchElections(pb: ProgressBar, tvEmpty: TextView) {
        pb.visibility = View.VISIBLE
        val token = VotingApp.authToken ?: ""

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getJoinedElections(token)
                }
                if (response.isSuccessful) {
                    val apiElections = response.body() ?: emptyList()
                    joinedList.clear()
                    electionsCache.clear()
                    electionsMetadata.clear()
                    apiElections.forEach { api ->
                        val electionId = api.id ?: api.electionId
                        electionsMetadata[electionId] = api
                        joinedList.add(Participant().apply {
                            this.electionId = electionId
                            this.electionName = api.name
                            this.electionCode = api.electionCode
                        })
                        
                        // Map ApiCandidate to local Candidate model
                        val candidates = api.candidates.map { 
                            Candidate(it.name, it.photoUrl ?: "", it.symbolUrl ?: "") 
                        }
                        electionsCache[electionId] = candidates
                    }
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(this@JoinedElectionsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                pb.visibility = View.GONE
                tvEmpty.visibility = if (joinedList.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
