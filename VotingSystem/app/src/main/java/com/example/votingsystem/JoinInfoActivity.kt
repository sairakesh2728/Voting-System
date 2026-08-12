package com.example.votingsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class JoinInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_info)

        val etFullName = findViewById<TextInputEditText>(R.id.etFullName)
        val etIdNumber = findViewById<TextInputEditText>(R.id.etIdNumber)
        val btnNext = findViewById<Button>(R.id.btnNextJoin)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarJoinInfo)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnNext.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val id = etIdNumber.text.toString().trim()

            if (name.isEmpty() || id.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, JoinCodeActivity::class.java)
            intent.putExtra("FULL_NAME", name)
            intent.putExtra("ID_NUMBER", id)
            startActivity(intent)
        }
    }
}