package com.example.votingsystem

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ManageElectionActivity : AppCompatActivity() {

    private var startTime = ""
    private var endTime = ""
    private var electionId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_election)

        electionId = intent.getStringExtra("ELECTION_ID") ?: ""
        val electionName = intent.getStringExtra("ELECTION_NAME") ?: "Election"

        findViewById<TextView>(R.id.tvElectionNameManage).text = electionName

        val btnStart = findViewById<Button>(R.id.btnManageStartTime)
        val btnEnd = findViewById<Button>(R.id.btnManageEndTime)
        val btnSave = findViewById<Button>(R.id.btnSaveSchedule)
        val pb = findViewById<ProgressBar>(R.id.pbSavingSchedule)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarManage)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnStart.setOnClickListener {
            showTimePicker { time ->
                startTime = time
                btnStart.text = "Starts at: $time"
            }
        }

        btnEnd.setOnClickListener {
            showTimePicker { time ->
                endTime = time
                btnEnd.text = "Ends at: $time"
            }
        }

        btnSave.setOnClickListener {
            if (startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(this, "Please set both times", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startTime >= endTime) {
                Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveSchedule(pb)
        }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Time (24h)")
            .build()

        picker.addOnPositiveButtonClickListener {
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", picker.hour, picker.minute)
            onTimeSelected(formattedTime)
        }
        picker.show(supportFragmentManager, "TIME_PICKER")
    }

    private fun saveSchedule(pb: ProgressBar) {
        pb.visibility = View.VISIBLE
        val token = VotingApp.authToken ?: ""

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.updateElectionSchedule(
                        token, electionId, ElectionTimeUpdate(startTime, endTime)
                    )
                }
                if (response.isSuccessful) {
                    Toast.makeText(this@ManageElectionActivity, "Schedule saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ManageElectionActivity, "Failed to save", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ManageElectionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                pb.visibility = View.GONE
            }
        }
    }
}
