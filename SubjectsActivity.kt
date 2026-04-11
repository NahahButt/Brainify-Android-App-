package com.brainify.quizapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.brainify.quizapp.adapters.SubjectsAdapter
import com.brainify.quizapp.databinding.ActivitySubjectsBinding
import com.brainify.quizapp.models.Subject

class SubjectsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubjectsBinding
    private lateinit var subjectsAdapter: SubjectsAdapter

    // Updated list of subjects with new subjects
    private val subjectsList = listOf(
        Subject("Networking", R.drawable.ic_math, "#FF5722"), // یا Networking کے لیے نیا icon
        Subject("Machine Learning", R.drawable.ic_physics, "#2196F3"),
        Subject("Cloud Computing", R.drawable.ic_chemistry, "#4CAF50"),
        Subject("Parallel Computing", R.drawable.ic_biology, "#4CAF50"),
        Subject("Web Development", R.drawable.ic_computer, "#9C27B0"),
        Subject("Accounting", R.drawable.ic_english, "#FF9800"),
    )
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
        binding = ActivitySubjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide default action bar
        supportActionBar?.hide()

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Create adapter
        subjectsAdapter = SubjectsAdapter(subjectsList) { subject ->
            // When subject is clicked, open TopicsActivity
            val intent = Intent(this, TopicsActivity::class.java).apply {
                putExtra("SUBJECT_NAME", subject.name)
                putExtra("SUBJECT_COLOR", subject.color)
            }
            startActivity(intent)
        }

        // Setup RecyclerView
        binding.rvSubjects.apply {
            // Use Grid Layout (2 columns)
            layoutManager = GridLayoutManager(this@SubjectsActivity, 2)

            // Set adapter
            adapter = subjectsAdapter

            // Add padding and margins
            setPadding(16, 0, 16, 16)
            clipToPadding = false
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh RecyclerView when activity resumes
        subjectsAdapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}