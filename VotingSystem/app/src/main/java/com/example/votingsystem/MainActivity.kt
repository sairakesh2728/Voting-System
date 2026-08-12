package com.example.votingsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // If already logged in (demo mode usually starts fresh, but for live we might check token)
        if (VotingApp.authToken != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val btnSignup = findViewById<Button>(R.id.btnGoToSignup)
        val btnLogin = findViewById<Button>(R.id.btnGoToLogin)

        btnSignup.setOnClickListener {
            val intent = Intent(this, SignupEmailActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
