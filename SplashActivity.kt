package com.brainify.quizapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SplashActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_splash)

        val centerContainer = findViewById<LinearLayout>(R.id.centerContainer)
        val loadingText = findViewById<TextView>(R.id.txtLoading)

        val mainAnim = AnimationUtils.loadAnimation(this, R.anim.logo_anim)
        val lineAnim = AnimationUtils.loadAnimation(this, R.anim.line_anim)

        centerContainer.startAnimation(mainAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            loadingText.startAnimation(lineAnim)
        }, 1000)

        // ✅ Check Login Status
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPref = getSharedPreferences("BrainifyPrefs", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

            if (isLoggedIn) {
                // If logged in, go directly to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // If NOT logged in, go to NavigationActivity
                startActivity(Intent(this, NavigationActivity::class.java))
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3000)
    }
}
