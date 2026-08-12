package com.example.votingsystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JoinCodeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_code)

        val fullName = intent.getStringExtra("FULL_NAME") ?: ""
        val idNumber = intent.getStringExtra("ID_NUMBER") ?: ""

        val etCode = findViewById<TextInputEditText>(R.id.etElectionCode)
        val btnJoin = findViewById<Button>(R.id.btnJoinFinal)
        val progressBar = findViewById<ProgressBar>(R.id.pbJoining)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarJoinCode)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnJoin.setOnClickListener {
            val code = etCode.text.toString().trim().uppercase()

            if (code.length != 6) {
                Toast.makeText(this, "Enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnJoin.isEnabled = false

            val token = VotingApp.authToken ?: ""
            
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.instance.joinElection(
                            token,
                            JoinElectionRequest(code, fullName, idNumber)
                        )
                    }

                    if (response.isSuccessful) {
                        val intent = Intent(this@JoinCodeActivity, JoinSuccessActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        val error = response.errorBody()?.string() ?: response.message()
                        Toast.makeText(this@JoinCodeActivity, "Join failed: $error", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                        btnJoin.isEnabled = true
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@JoinCodeActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    btnJoin.isEnabled = true
                }
            }
        }
    }
}
