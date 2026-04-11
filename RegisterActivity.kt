package com.brainify.quizapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.brainify.quizapp.database.QuizDbHelper
import com.brainify.quizapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    
    override fun attachBaseContext(newBase: Context) {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(newBase)
        val langCode = prefs.getString("language", "en") ?: "en"
        val locale = java.util.Locale(langCode)
        java.util.Locale.setDefault(locale)

        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validate(username, email, phone, password, confirmPassword)) {
                saveUserToDatabase(username, email, phone, password)
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validate(
        username: String,
        email: String,
        phone: String,
        password: String,
        confirm: String
    ): Boolean {
        return when {
            username.isEmpty() -> {
                binding.tilUsername.error = "Username is required"
                false
            }
            email.isEmpty() -> {
                binding.tilEmail.error = "Email is required"
                false
            }
            phone.isEmpty() -> {
                binding.tilPhone.error = "Phone number is required"
                false
            }
            password.length < 6 -> {
                binding.tilPassword.error = "Min 6 characters"
                false
            }
            password != confirm -> {
                binding.tilConfirmPassword.error = "Passwords do not match"
                false
            }
            else -> {
                binding.tilUsername.error = null
                binding.tilEmail.error = null
                binding.tilPhone.error = null
                binding.tilPassword.error = null
                binding.tilConfirmPassword.error = null
                true
            }
        }
    }

    private fun saveUserToDatabase(username: String, email: String, phone: String, password: String) {
        val dbHelper = QuizDbHelper(this)

        // Register in SQLite (Allows multiple users)
        val isRegistered = dbHelper.registerUser(username, email, phone, password)

        if (isRegistered) {
            // Clear previous session data for the new user
            getSharedPreferences("QuizProgress", MODE_PRIVATE).edit().clear().apply()
            getSharedPreferences("QuizHistory", MODE_PRIVATE).edit().clear().apply()
            getSharedPreferences("notes_prefs", MODE_PRIVATE).edit().clear().apply()

            // Set current session
            val pref = getSharedPreferences("BrainifyPrefs", MODE_PRIVATE)
            pref.edit().apply {
                putString("user_name", username)
                putString("user_email", email)
                putString("user_phone", phone)
                putString("user_password", password)
                putBoolean("isLoggedIn", true)
                apply()
            }

            Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Email already registered or Database Error", Toast.LENGTH_LONG).show()
        }
    }
}
