package com.example.votingsystem

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        val electionName = intent.getStringExtra("ELECTION_NAME") ?: "Election"
        val timestamp = intent.getLongOf("TIMESTAMP", System.currentTimeMillis())
        val signature = intent.getStringExtra("SIGNATURE") ?: ""

        val tvElection = findViewById<TextView>(R.id.tvReceiptElectionName)
        val tvTime = findViewById<TextView>(R.id.tvReceiptTime)
        val ivQr = findViewById<ImageView>(R.id.ivReceiptQr)
        val btnDone = findViewById<Button>(R.id.btnReceiptDone)

        tvElection.text = electionName
        
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        tvTime.text = "Voted on: ${sdf.format(Date(timestamp))}"

        // Generate QR code containing the tamper-proof signature
        generateQr(signature, ivQr)

        btnDone.setOnClickListener {
            finish()
        }
    }

    private fun generateQr(data: String, imageView: ImageView) {
        try {
            val writer = MultiFormatWriter()
            val matrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val encoder = BarcodeEncoder()
            val bitmap: Bitmap = encoder.createBitmap(matrix)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Extension-like helper for Intent
    private fun android.content.Intent.getLongOf(key: String, default: Long): Long {
        return getLongExtra(key, default)
    }
}
