package com.example.votingsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupEmailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_email)

        val emailInput = findViewById<EditText>(R.id.etEmail)
        val btnNext = findViewById<Button>(R.id.btnNextEmail)
        val progressBar = findViewById<ProgressBar>(R.id.emailProgress)

        btnNext.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isEmpty() || !email.contains("@")) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Move to password screen
            val intent = Intent(this, SignupPasswordActivity::class.java)
            intent.putExtra("USER_EMAIL", email)
            startActivity(intent)
        }
    }
}
