package com.brainify.quizapp

import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.brainify.quizapp.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private var isOldPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false
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
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupPasswordToggles()
        setupClickListeners()
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Change Password"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupPasswordToggles() {
        binding.ivEyeOldPassword.setOnClickListener {
            togglePasswordVisibility(binding.etOldPassword, isOldPasswordVisible)
            isOldPasswordVisible = !isOldPasswordVisible
        }

        binding.ivEyeNewPassword.setOnClickListener {
            togglePasswordVisibility(binding.etNewPassword, isNewPasswordVisible)
            isNewPasswordVisible = !isNewPasswordVisible
        }

        binding.ivEyeConfirmPassword.setOnClickListener {
            togglePasswordVisibility(binding.etConfirmPassword, isConfirmPasswordVisible)
            isConfirmPasswordVisible = !isConfirmPasswordVisible
        }
    }

    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean) {
        if (isVisible) {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
        } else {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }
        editText.setSelection(editText.text.length)
    }

    private fun setupClickListeners() {
        binding.btnChangePassword.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val oldPassword = binding.etOldPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (validateInputs(oldPassword, newPassword, confirmPassword)) {
            updatePassword(newPassword)
        }
    }

    private fun validateInputs(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        val sharedPref = getSharedPreferences("BrainifyPrefs", MODE_PRIVATE)
        val savedPassword = sharedPref.getString("user_password", "")

        return when {
            oldPassword.isEmpty() -> {
                binding.etOldPassword.error = "Enter old password"
                false
            }
            oldPassword != savedPassword -> {
                binding.etOldPassword.error = "Incorrect old password"
                false
            }
            newPassword.isEmpty() -> {
                binding.etNewPassword.error = "Enter new password"
                false
            }
            newPassword.length < 6 -> {
                binding.etNewPassword.error = "Password must be at least 6 characters"
                false
            }
            confirmPassword.isEmpty() -> {
                binding.etConfirmPassword.error = "Confirm new password"
                false
            }
            newPassword != confirmPassword -> {
                binding.etConfirmPassword.error = "Passwords don't match"
                false
            }
            else -> true
        }
    }

    private fun updatePassword(newPassword: String) {
        val sharedPref = getSharedPreferences("BrainifyPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_password", newPassword)
            apply()
        }

        Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }
}