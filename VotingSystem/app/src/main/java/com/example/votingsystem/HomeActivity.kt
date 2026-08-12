package com.example.votingsystem

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val cardCreate = findViewById<MaterialCardView>(R.id.cardCreate)
        val cardJoin = findViewById<MaterialCardView>(R.id.cardJoin)
        bottomNav = findViewById(R.id.bottom_navigation)

        cardCreate.setOnClickListener {
            val intent = Intent(this, CreateElectionActivity::class.java)
            startActivity(intent)
        }

        cardJoin.setOnClickListener {
            val intent = Intent(this, JoinInfoActivity::class.java)
            startActivity(intent)
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    true
                }
                R.id.nav_my_elections -> {
                    val intent = Intent(this, MyElectionsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_cast_vote -> {
                    val intent = Intent(this, JoinedElectionsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.nav_dashboard
    }
}
