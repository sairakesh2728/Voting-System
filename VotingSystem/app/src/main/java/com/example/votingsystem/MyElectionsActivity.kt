package com.example.votingsystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyElectionsActivity : AppCompatActivity() {

    private lateinit var adapter: ElectionAdapter
    private val electionsList = mutableListOf<Election>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_elections)

        val rv = findViewById<RecyclerView>(R.id.rvMyElections)
        val pb = findViewById<ProgressBar>(R.id.pbMyElections)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyElections)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.selectedItemId = R.id.nav_my_elections

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_my_elections -> true
                R.id.nav_cast_vote -> {
                    startActivity(Intent(this, JoinedElectionsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        rv.layoutManager = LinearLayoutManager(this)
        adapter = ElectionAdapter(electionsList) { election ->
            showElectionOptions(election)
        }
        rv.adapter = adapter

        fetchElections(pb, tvEmpty)
        syncOfflineVotes()
    }

    private fun showElectionOptions(election: Election) {
        val options = arrayOf("View Results", "Manage Participants", "Manage Schedule")
        AlertDialog.Builder(this)
            .setTitle(election.name)
            .setItems(options) { dialog, which ->
                dialog.dismiss()
                when (which) {
                    0 -> {
                        val intent = Intent(this, ResultsActivity::class.java)
                        intent.putExtra("ELECTION_ID", election.electionId)
                        intent.putExtra("ELECTION_NAME", election.name)
                        startActivity(intent)
                    }
                    1 -> {
                        val intent = Intent(this, ParticipantsActivity::class.java)
                        intent.putExtra("ELECTION_ID", election.electionId)
                        intent.putExtra("ELECTION_NAME", election.name)
                        startActivity(intent)
                    }
                    2 -> {
                        val intent = Intent(this, ManageElectionActivity::class.java)
                        intent.putExtra("ELECTION_ID", election.electionId)
                        intent.putExtra("ELECTION_NAME", election.name)
                        startActivity(intent)
                    }
                }
            }.show()
    }

    private fun fetchElections(pb: ProgressBar, tvEmpty: TextView) {
        pb.visibility = View.VISIBLE
        val token = VotingApp.authToken ?: ""

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getMyElections(token)
                }
                
                if (response.isSuccessful) {
                    val apiElections = response.body() ?: emptyList()
                    electionsList.clear()
                    apiElections.forEach { api ->
                        electionsList.add(Election().apply {
                            this.electionId = api.id ?: api.electionId
                            this.name = api.name
                            this.date = api.date
                            this.time = api.startTime ?: "TBD"
                            this.electionCode = api.electionCode
                        })
                    }
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyElectionsActivity, "Network Error", Toast.LENGTH_SHORT).show()
            } finally {
                pb.visibility = View.GONE
                tvEmpty.visibility = if (electionsList.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun syncOfflineVotes() {
        val realm = VotingApp.realm
        val token = VotingApp.authToken ?: ""
        if (realm == null || token.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            // Find votes that haven't been synced yet
            // (In a full implementation, we'd add a 'synced' boolean to Vote model)
            val unsyncedVotes = realm.query(Vote::class).find()
            unsyncedVotes.forEach { vote ->
                try {
                    val response = RetrofitClient.instance.castVote(
                        token,
                        VoteRequest(vote.electionId, vote.candidateName, vote.voterEmail, vote.timestamp, vote.signature)
                    )
                    // If success, we could mark as synced or delete
                } catch (e: Exception) { }
            }
        }
    }
}
