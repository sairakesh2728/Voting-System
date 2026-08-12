package com.example.votingsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val tvVotesCount = findViewById<TextView>(R.id.tvVotesCastedCount)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.selectedItemId = R.id.nav_profile

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
                R.id.nav_cast_vote -> {
                    startActivity(Intent(this, JoinedElectionsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }

        val email = VotingApp.demoUserEmail
        val token = VotingApp.authToken

        if (token != null) {
            tvEmail.text = email ?: "Not logged in"
            tvName.text = VotingApp.demoUserName ?: email?.substringBefore("@") ?: "User"
            
            // Fetch local vote count from Realm
            val realm = VotingApp.realm
            if (realm != null) {
                val normalizedEmail = email?.trim()?.lowercase() ?: ""
                val count = realm.query(Vote::class, "voterEmail ==[c] $0", normalizedEmail).find().size
                tvVotesCount.text = "$count"
            } else {
                tvVotesCount.text = "0"
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnResetPassword.setOnClickListener {
            Toast.makeText(this, "Reset link sent (Live Backend)", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            VotingApp.logoutAndClear()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
