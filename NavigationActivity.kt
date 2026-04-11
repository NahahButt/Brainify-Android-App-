package com.brainify.quizapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.brainify.quizapp.databinding.ActivityNavigationBinding
import java.util.Locale

class NavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding

    override fun attachBaseContext(newBase: Context) {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(newBase)
        val langCode = prefs.getString("language", "en") ?: "en"
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // All buttons redirect to login with a toast as requested
        binding.btnSubjects.setOnClickListener { navigateToLogin() }
        binding.btnProfile.setOnClickListener { navigateToLogin() }
        binding.btnNotes.setOnClickListener { navigateToLogin() }
        binding.btnSettings.setOnClickListener { navigateToLogin() }
    }

    private fun navigateToLogin() {
        Toast.makeText(this, "Please login first to access this feature", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
