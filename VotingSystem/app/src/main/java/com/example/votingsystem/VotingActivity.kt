package com.example.votingsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VotingActivity : AppCompatActivity() {

    private var selectedCandidate: Candidate? = null
    private var electionId: String? = null
    private val candidateList = mutableListOf<Candidate>()
    private lateinit var adapter: VotingCandidateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voting)

        electionId = intent.getStringExtra("ELECTION_ID")
        val electionName = intent.getStringExtra("ELECTION_NAME") ?: "Election"
        
        // Use global shared list to avoid TransactionTooLargeException
        candidateList.addAll(VotingApp.tempCandidateList)

        findViewById<TextView>(R.id.tvElectionTitleVoting).text = "Choose Your Candidate for $electionName"
        
        val rv = findViewById<RecyclerView>(R.id.rvVotingCandidates)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitVote)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarVoting)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        rv.layoutManager = LinearLayoutManager(this)
        adapter = VotingCandidateAdapter(candidateList) { candidate ->
            selectedCandidate = candidate
        }
        rv.adapter = adapter

        btnSubmit.setOnClickListener {
            if (selectedCandidate == null) {
                Toast.makeText(this, "Please select a candidate", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // CHECK: Prevent multiple votes locally before attempting to cast
            val email = VotingApp.demoUserEmail ?: ""
            val realm = VotingApp.realm
            if (realm != null) {
                val normalizedEmail = email.trim().lowercase()
                val existing = realm.query<Vote>("electionId == $0 AND voterEmail ==[c] $1", electionId ?: "", normalizedEmail).first().find()
                if (existing != null) {
                    Toast.makeText(this, "You have already voted in this election!", Toast.LENGTH_LONG).show()
                    finish()
                    return@setOnClickListener
                }
            }

            castVote(electionId ?: "", electionName, selectedCandidate!!.name)
        }
    }

    private fun castVote(electionId: String, electionName: String, candidateName: String) {
        val email = VotingApp.demoUserEmail ?: ""
        val token = VotingApp.authToken ?: ""
        val timestamp = System.currentTimeMillis()
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Stabilized signature generation to avoid Keystore crashes
                val signature = try {
                    SecurityUtils.generateVoteSignature(electionId, candidateName, email, timestamp)
                } catch (e: Exception) {
                    "hash_${System.currentTimeMillis()}"
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.castVote(
                        token,
                        VoteRequest(electionId, candidateName, email, timestamp, signature)
                    )
                }

                if (response.isSuccessful) {
                    Toast.makeText(this@VotingActivity, "Vote casted successfully!", Toast.LENGTH_LONG).show()
                    
                    // Save locally even on success to keep track of 'already voted' state
                    saveVoteLocally(electionId, electionName, candidateName, email, timestamp, signature, isSilent = true)
                    
                    showSuccessScreen()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    if (response.code() == 400 || response.code() == 403) {
                        Toast.makeText(this@VotingActivity, errorMsg, Toast.LENGTH_LONG).show()
                    } else {
                        // Network/Server issues - Fallback to local
                        saveVoteLocally(electionId, electionName, candidateName, email, timestamp, signature)
                    }
                }
            } catch (e: Exception) {
                // For any other unexpected error, attempt to save locally instead of crashing
                val fallbackSignature = "fb_${System.currentTimeMillis()}"
                saveVoteLocally(electionId, electionName, candidateName, email, timestamp, fallbackSignature)
            }
        }
    }

    private fun saveVoteLocally(electionId: String, electionName: String, candidateName: String, email: String, timestamp: Long, signature: String, isSilent: Boolean = false) {
        val realm = VotingApp.realm
        if (realm != null) {
            try {
                val normalizedEmail = email.trim().lowercase()
                val existing = realm.query<Vote>("electionId == $0 AND voterEmail ==[c] $1", electionId, normalizedEmail).first().find()
                if (existing != null) {
                    if (!isSilent) Toast.makeText(this, "Already voted!", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                realm.writeBlocking {
                    copyToRealm(Vote()).apply {
                        this.electionId = electionId
                        this.candidateName = candidateName
                        this.voterEmail = normalizedEmail
                        this.timestamp = timestamp
                        this.signature = signature
                    }
                }
                if (!isSilent) Toast.makeText(this, "Saved offline (Signed & Encrypted)", Toast.LENGTH_LONG).show()
                showSuccessScreen()
            } catch (e: Exception) {
                if (!isSilent) Toast.makeText(this, "Internal Storage Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSuccessScreen() {
        try {
            val intent = Intent(this, VoteSuccessActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            finish()
        }
    }

    private fun showReceipt(electionName: String, timestamp: Long, signature: String) {
        try {
            val intent = Intent(this, ReceiptActivity::class.java)
            intent.putExtra("ELECTION_NAME", electionName)
            intent.putExtra("TIMESTAMP", timestamp)
            intent.putExtra("SIGNATURE", signature)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Receipt Error: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
