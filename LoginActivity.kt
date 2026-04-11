package com.brainify.quizapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.brainify.quizapp.database.QuizDbHelper
import com.brainify.quizapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var tts: TextToSpeech? = null
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(newBase)
        val langCode = prefs.getString("language", "en") ?: "en"
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogle()
        setupTTS()
        setupClickListeners()

        binding.loginFormContainer.visibility = View.VISIBLE
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_from_bag)
        binding.loginFormContainer.startAnimation(slideUp)
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isNotEmpty() && password.length >= 6) {
                val dbHelper = QuizDbHelper(this)
                val userData = dbHelper.verifyUser(email, password)
                if (userData != null) {
                    val name = userData["username"] ?: "User"
                    val phone = userData["phone"] ?: ""
                    saveUserSession(name, email, phone, password)
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter valid email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ Fixed: Forgot Password Functionality
        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.cardGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleSignInClient.signInIntent)
            }
        }
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        val etForgotEmail = dialogView.findViewById<EditText>(R.id.etForgotEmail)

        builder.setView(dialogView)
        builder.setTitle("Recover Password")
        builder.setPositiveButton("Find") { _, _ ->
            val email = etForgotEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                val dbHelper = QuizDbHelper(this)
                val recoveredPassword = dbHelper.getPasswordByEmail(email)

                if (recoveredPassword != null) {
                    AlertDialog.Builder(this)
                        .setTitle("Password Found")
                        .setMessage("Your password is: $recoveredPassword")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    Toast.makeText(this, "Email not found!", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.create().show()
    }

    private fun saveUserSession(username: String, email: String, phone: String, pass: String) {
        val sp = getSharedPreferences("BrainifyPrefs", MODE_PRIVATE)
        sp.edit().apply {
            putBoolean("isLoggedIn", true)
            putString("user_name", username)
            putString("user_email", email)
            putString("user_phone", phone)
            putString("user_password", pass)
            apply()
        }
        tts?.speak("Welcome $username", TextToSpeech.QUEUE_FLUSH, null, null)
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }, 1500)
    }

    private val googleLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                account?.let { saveUserSession(it.displayName ?: "User", it.email ?: "", "", "") }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale.US
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
