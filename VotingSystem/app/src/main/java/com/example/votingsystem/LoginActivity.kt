package com.example.votingsystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLoginNext)
        val emailInput = findViewById<EditText>(R.id.etLoginEmail)
        val passwordInput = findViewById<EditText>(R.id.etLoginPass)
        val progressBar = findViewById<ProgressBar>(R.id.pbLogin)

        btnLogin.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                btnLogin.isEnabled = false
                
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.instance.login(email, password)
                        }

                        if (response.isSuccessful) {
                            val authResponse = response.body()
                            VotingApp.authToken = "${authResponse?.token_type} ${authResponse?.access_token}"
                            VotingApp.demoUserEmail = email
                            VotingApp.demoUserName = authResponse?.user?.name

                            try {
                                val otpResponse = withContext(Dispatchers.IO) {
                                    RetrofitClient.instance.sendOtp(OtpRequest(email))
                                }

                                if (otpResponse.isSuccessful) {
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(this@LoginActivity, "OTP sent to your email", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this@LoginActivity, OtpVerifyActivity::class.java)
                                    intent.putExtra("FLOW_TYPE", "LOGIN")
                                    intent.putExtra("USER_EMAIL", email)
                                    startActivity(intent)
                                } else {
                                    progressBar.visibility = View.GONE
                                    btnLogin.isEnabled = true
                                    val errorMsg = otpResponse.errorBody()?.string() ?: "Could not send OTP email"
                                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                progressBar.visibility = View.GONE
                                btnLogin.isEnabled = true
                                Toast.makeText(this@LoginActivity, "OTP email failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            btnLogin.isEnabled = true
                            val errorMsg = response.errorBody()?.string() ?: response.message()
                            Toast.makeText(this@LoginActivity, "Login failed: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true
                        Toast.makeText(this@LoginActivity, "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
