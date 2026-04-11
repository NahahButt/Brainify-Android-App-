package com.brainify.quizapp

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.brainify.quizapp.databinding.ActivitySettingsBinding
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ LANGUAGE AND FONT APPLY FIRST BEFORE SUPER.ONCREATE
        applySavedSettings()

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        checkNotificationPermission()
        createNotificationChannel()
        setupSettingsFragment()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Settings"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupSettingsFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }

    // 🔔 ANDROID 13+ PERMISSION
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "LOGOUT_CHANNEL",
                "App Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    // ✅ APPLY SAVED LANGUAGE & FONT SIZE
    private fun applySavedSettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        // 1. Language Apply
        val langCode = prefs.getString("language", "en") ?: "en"
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)

        // 2. Font Size Apply
        val size = prefs.getString("font_size", "16") ?: "16"
        val scale = when (size) {
            "14" -> 0.85f
            "16" -> 1.0f
            "18" -> 1.15f
            "20" -> 1.3f
            else -> 1.0f
        }
        config.fontScale = scale

        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // ===================== FRAGMENT =====================

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            setupLanguage() // ✅ Naya Function
            setupFontSize()
            setupHelpFeedback()
            setupLogout()
        }

        // ✅ LANGUAGE SWITCH FUNCTIONAL
        private fun setupLanguage() {
            val langPref = findPreference<ListPreference>("language")
            langPref?.setOnPreferenceChangeListener { _, newValue ->
                val langCode = newValue.toString()

                // Save and Apply Locale
                val locale = Locale(langCode)
                Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)

                @Suppress("DEPRECATION")
                resources.updateConfiguration(config, resources.displayMetrics)

                Toast.makeText(context, "Language updated to $langCode", Toast.LENGTH_SHORT).show()

                // Restart activity to apply language immediately
                requireActivity().recreate()
                true
            }
        }

        private fun setupFontSize() {
            val fontPref = findPreference<ListPreference>("font_size")
            fontPref?.setOnPreferenceChangeListener { _, newValue ->
                applyFontSize(newValue.toString())
                true
            }
        }

        private fun applyFontSize(size: String) {
            val scale = when (size) {
                "14" -> 0.85f
                "16" -> 1.0f
                "18" -> 1.15f
                "20" -> 1.3f
                else -> 1.0f
            }

            val config = resources.configuration
            config.fontScale = scale

            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)

            requireActivity().recreate()
        }

        private fun notificationsEnabled(): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
            return prefs.getBoolean("notifications", true)
        }

        private fun setupHelpFeedback() {
            findPreference<Preference>("help")?.setOnPreferenceClickListener {
                showSupportOptionsDialog()
                true
            }
        }

        private fun showSupportOptionsDialog() {
            val options = arrayOf("Send Feedback", "Get Help / Support", "Cancel")

            AlertDialog.Builder(requireContext())
                .setTitle("How can we help?")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> sendEmail("Feedback")
                        1 -> sendEmail("Support Request")
                        2 -> dialog.dismiss()
                    }
                }
                .show()
        }

        private fun sendEmail(subjectType: String) {
            val deviceModel = Build.MODEL
            val androidVersion = Build.VERSION.RELEASE

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:talha.skg2@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Brainify - $subjectType")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "--- Device Info ---\nModel: $deviceModel\nAndroid: $androidVersion\n\nWrite your message here:\n"
                )
            }

            try {
                startActivity(Intent.createChooser(emailIntent, "Choose Email App"))
            } catch (e: Exception) {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }

        private fun setupLogout() {
            findPreference<Preference>("logout")?.setOnPreferenceClickListener {
                showLogoutDialog()
                true
            }
        }

        private fun showLogoutDialog() {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Logout") { _, _ ->
                    if (notificationsEnabled()) {
                        showLogoutNotification()
                        showBottomToast()
                    }
                    performLogout()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun showLogoutNotification() {
            val builder = NotificationCompat.Builder(requireContext(), "LOGOUT_CHANNEL")
                .setSmallIcon(R.drawable.brain_logo1)
                .setContentTitle("Brainify")
                .setContentText("Logged out successfully")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)

            val manager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(1, builder.build())
        }

        private fun showBottomToast() {
            Toast.makeText(requireContext(), "You have been logged out", Toast.LENGTH_SHORT).show()
        }

        private fun performLogout() {
            requireActivity()
                .getSharedPreferences("BrainifyPrefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("isLoggedIn", false)
                .apply()

            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}