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

class SignupPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_password)

        val pass1 = findViewById<EditText>(R.id.etPass1)
        val pass2 = findViewById<EditText>(R.id.etPass2)
        val btnSendOtp = findViewById<Button>(R.id.btnSendOtpSignup)
        val progressBar = findViewById<ProgressBar>(R.id.pbSignup)

        val email = intent.getStringExtra("USER_EMAIL")

        btnSendOtp.setOnClickListener {
            val p1 = pass1.text.toString()
            val p2 = pass2.text.toString()

            if (p1.isNotEmpty() && p1 == p2) {
                progressBar.visibility = View.VISIBLE
                btnSendOtp.isEnabled = false
                
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            val name = email?.substringBefore("@") ?: "User"
                            RetrofitClient.instance.signup(SignupRequest(name, email ?: "", p1))
                        }
                        
                        if (response.isSuccessful) {
                            try {
                                val otpResponse = withContext(Dispatchers.IO) {
                                    RetrofitClient.instance.sendOtp(OtpRequest(email ?: ""))
                                }
                                if (otpResponse.isSuccessful) {
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(this@SignupPasswordActivity, "OTP sent to your email", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this@SignupPasswordActivity, OtpVerifyActivity::class.java)
                                    intent.putExtra("FLOW_TYPE", "SIGNUP")
                                    intent.putExtra("USER_EMAIL", email)
                                    intent.putExtra("USER_PASSWORD", p1)
                                    startActivity(intent)
                                } else {
                                    progressBar.visibility = View.GONE
                                    btnSendOtp.isEnabled = true
                                    val errorMsg = otpResponse.errorBody()?.string() ?: "Could not send OTP email"
                                    Toast.makeText(this@SignupPasswordActivity, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                progressBar.visibility = View.GONE
                                btnSendOtp.isEnabled = true
                                Toast.makeText(this@SignupPasswordActivity, "OTP email failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            btnSendOtp.isEnabled = true
                            val errorMsg = response.errorBody()?.string() ?: response.message()
                            Toast.makeText(this@SignupPasswordActivity, "Signup failed: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        progressBar.visibility = View.GONE
                        btnSendOtp.isEnabled = true
                        Toast.makeText(this@SignupPasswordActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
