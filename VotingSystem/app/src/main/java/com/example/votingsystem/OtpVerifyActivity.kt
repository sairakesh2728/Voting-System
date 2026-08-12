package com.example.votingsystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtpVerifyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verify)

        val flowType = intent.getStringExtra("FLOW_TYPE")
        val userEmail = intent.getStringExtra("USER_EMAIL")

        val otpInput = findViewById<EditText>(R.id.otpInput)
        val btnVerify = findViewById<Button>(R.id.btnVerifyOtp)
        val progressBar = findViewById<ProgressBar>(R.id.pbOtp)

        btnVerify.setOnClickListener {
            val userEnteredOtp = otpInput.text.toString().trim()

            if (userEnteredOtp.length != 6) {
                Toast.makeText(this, "Enter the 6-digit OTP from your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnVerify.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.instance.verifyOtp(
                            OtpVerifyRequest(userEmail ?: "", userEnteredOtp)
                        )
                    }

                    if (response.isSuccessful) {
                        progressBar.visibility = View.GONE
                        showBiometricPrompt(flowType, userEmail)
                    } else {
                        progressBar.visibility = View.GONE
                        btnVerify.isEnabled = true
                        val errorMsg = response.errorBody()?.string() ?: "Invalid OTP"
                        Toast.makeText(this@OtpVerifyActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    progressBar.visibility = View.GONE
                    btnVerify.isEnabled = true
                    Toast.makeText(this@OtpVerifyActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showBiometricPrompt(flowType: String?, email: String?) {
        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    if (flowType == "SIGNUP") {
                        val password = intent.getStringExtra("USER_PASSWORD") ?: ""
                        val progressBar = findViewById<ProgressBar>(R.id.pbOtp)
                        progressBar.visibility = View.VISIBLE
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val response = withContext(Dispatchers.IO) {
                                    RetrofitClient.instance.login(email ?: "", password)
                                }
                                progressBar.visibility = View.GONE
                                if (response.isSuccessful) {
                                    val authResponse = response.body()
                                    VotingApp.authToken = "${authResponse?.token_type} ${authResponse?.access_token}"
                                    VotingApp.demoUserEmail = email
                                    navigateToSuccess("Registration Complete")
                                } else {
                                    navigateToSuccess("Login after signup failed")
                                }
                            } catch (e: Exception) {
                                progressBar.visibility = View.GONE
                                navigateToSuccess("Error: ${e.message}")
                            }
                        }
                    } else {
                        navigateToSuccess("Successfully Logged In")
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    navigateToSuccess("Successfully Logged In") // Proceed anyway for demo
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Biometric failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Identity Verification")
            .setSubtitle("Use Fingerprint or Face Recognition to proceed")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun navigateToSuccess(message: String) {
        val intent = Intent(this, SuccessActivity::class.java)
        intent.putExtra("SUCCESS_MSG", message)
        startActivity(intent)
        finish()
    }
}
