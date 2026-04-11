package com.brainify.quizapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.brainify.quizapp.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
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
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserData()

        binding.btnSaveChanges.setOnClickListener {
            val newUsername = binding.etUsername.text.toString().trim()
            val newPassword = binding.etPassword.text.toString().trim()

            if(newUsername.isEmpty()) {
                binding.etUsername.error = "Username required"
                return@setOnClickListener
            }

            if(newPassword.length < 6) {
                binding.etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            val pref = getSharedPreferences("BrainifyPrefs", MODE_PRIVATE)
            pref.edit().apply {
                putString("user_name", newUsername)
                putString("user_password", newPassword)
                apply()
            }

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            finish() // return to ProfileFragment
        }
    }

    private fun loadUserData() {
        val pref = getSharedPreferences("BrainifyPrefs", MODE_PRIVATE)
        binding.etUsername.setText(pref.getString("user_name",""))
        binding.etPassword.setText(pref.getString("user_password",""))
    }
}
