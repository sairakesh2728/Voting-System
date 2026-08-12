package com.example.votingsystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ScheduleElectionActivity : AppCompatActivity() {

    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_election)

        val electionName = intent.getStringExtra("ELECTION_NAME") ?: ""
        val candidates = VotingApp.tempCandidateList

        val btnDate = findViewById<Button>(R.id.btnSelectDate)
        val btnCreate = findViewById<Button>(R.id.btnCreateFinal)
        val progressBar = findViewById<ProgressBar>(R.id.pbCreating)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarSchedule)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnDate.setOnClickListener {
            showDatePicker(btnDate)
        }

        btnCreate.setOnClickListener {
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ensure candidates still have names (Double check data integrity)
            if (candidates.any { it.name.isEmpty() }) {
                Toast.makeText(this, "Error: Candidate names were lost. Please add them again.", Toast.LENGTH_LONG).show()
                finish()
                return@setOnClickListener
            }

            createElectionLive(electionName, candidates, progressBar)
        }
    }

    private fun showDatePicker(btn: Button) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Election Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDate = format.format(calendar.time)
            
            // Display formatted date to user
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            btn.text = displayFormat.format(calendar.time)
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun createElectionLive(name: String, candidates: List<Candidate>, progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        val token = VotingApp.authToken ?: ""

        val apiCandidates = candidates.map {
            ApiCandidate(
                name = it.name, 
                photoUrl = it.photoUrl.ifEmpty { null }, 
                symbolUrl = it.symbolUrl.ifEmpty { null }
            )
        }
        val request = ElectionCreate(name, selectedDate, apiCandidates)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.createElection(token, request)
                }

                if (response.isSuccessful) {
                    val apiElection = response.body()
                    val electionCode = apiElection?.electionCode ?: "ERROR"
                    
                    // Clear temporary list
                    VotingApp.tempCandidateList.clear()
                    
                    progressBar.visibility = View.GONE
                    val intent = Intent(this@ScheduleElectionActivity, ElectionCodeActivity::class.java)
                    intent.putExtra("ELECTION_CODE", electionCode)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    val error = response.errorBody()?.string() ?: response.message()
                    Toast.makeText(this@ScheduleElectionActivity, "Create failed: $error", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@ScheduleElectionActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
        }
    }
}
