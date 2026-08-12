package com.example.votingsystem

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.io.ByteArrayOutputStream

class CreateElectionActivity : AppCompatActivity() {

    private lateinit var candidateAdapter: CandidateAdapter
    private var currentPickType: String? = null
    private var tempSymbolBase64: String = ""

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val base64 = convertUriToBase64(it)
            if (base64 != null) {
                tempSymbolBase64 = "data:image/png;base64,$base64"
                Toast.makeText(this, "Symbol uploaded", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun convertUriToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            // Compress to keep Base64 size reasonable for a student project
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_election)

        // Clear previous list if any
        VotingApp.tempCandidateList.clear()

        val etElectionName = findViewById<EditText>(R.id.etElectionName)
        val rvCandidates = findViewById<RecyclerView>(R.id.rvCandidates)
        val btnAddCandidate = findViewById<Button>(R.id.btnAddCandidate)
        val btnNext = findViewById<MaterialButton>(R.id.btnNextCreate)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        candidateAdapter = CandidateAdapter(VotingApp.tempCandidateList)
        rvCandidates.layoutManager = LinearLayoutManager(this)
        rvCandidates.adapter = candidateAdapter

        btnAddCandidate.setOnClickListener {
            showAddCandidateDialog()
        }

        btnNext.setOnClickListener {
            val name = etElectionName.text.toString().trim()
            if (name.length < 2) {
                Toast.makeText(this, "Election name must be at least 2 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val intent = Intent(this, ScheduleElectionActivity::class.java)
            intent.putExtra("ELECTION_NAME", name)
            startActivity(intent)
        }
    }

    private fun showAddCandidateDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_candidate, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etName = dialogView.findViewById<EditText>(R.id.etCandidateName)
        val btnSymbol = dialogView.findViewById<Button>(R.id.btnUploadSymbol)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmAdd)

        tempSymbolBase64 = ""

        btnSymbol.setOnClickListener {
            currentPickType = "SYMBOL"
            pickImageLauncher.launch("image/*")
        }

        btnConfirm.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter candidate name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (tempSymbolBase64.isEmpty()) {
                Toast.makeText(this, "Please upload a symbol", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newCandidate = Candidate(
                name = name,
                photoUrl = "",
                symbolUrl = tempSymbolBase64,
            )
            VotingApp.tempCandidateList.add(newCandidate)
            candidateAdapter.notifyItemInserted(VotingApp.tempCandidateList.size - 1)
            
            checkNextButtonVisibility()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkNextButtonVisibility() {
        val btnNext = findViewById<MaterialButton>(R.id.btnNextCreate)
        if (VotingApp.tempCandidateList.size >= 2) {
            btnNext.visibility = View.VISIBLE
        } else {
            btnNext.visibility = View.GONE
        }
    }
}
