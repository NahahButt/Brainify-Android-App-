package com.brainify.quizapp

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.brainify.quizapp.databinding.ActivityMainBinding
import com.brainify.quizapp.fragments.*
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(newBase)
        val langCode = prefs.getString("language", "en") ?: "en"
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupBottomNavigation()

            val fromLogin = intent.getBooleanExtra("FROM_LOGIN", false)
            val showProfile = intent.getBooleanExtra("SHOW_PROFILE", false)

            if (fromLogin || showProfile) {
                loadFragment(ProfileFragment())
                binding.bottomNavigation.selectedItemId = R.id.nav_profile
            } else {
                loadFragment(SubjectsFragment())
                binding.bottomNavigation.selectedItemId = R.id.nav_subjects
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback content view if binding fails
            setContentView(R.layout.activity_main)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            val sharedPref = getSharedPreferences("BrainifyPrefs", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

            if (!isLoggedIn && item.itemId != R.id.nav_subjects) {
                Toast.makeText(this, "Please login first to access this feature", Toast.LENGTH_SHORT).show()
                return@setOnNavigationItemSelectedListener false
            }

            when (item.itemId) {
                R.id.nav_subjects -> {
                    loadFragment(SubjectsFragment())
                    true
                }
                R.id.nav_leaderboard -> {
                    loadFragment(LeaderboardFragment())
                    true
                }
                R.id.nav_notes -> {
                    startActivity(Intent(this, NotesListActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }
}
