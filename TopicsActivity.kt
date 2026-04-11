package com.brainify.quizapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainify.quizapp.adapters.TopicsAdapter
import com.brainify.quizapp.databinding.ActivityTopicsBinding

class TopicsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicsBinding
    private lateinit var topicsAdapter: TopicsAdapter
    
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

        binding = ActivityTopicsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "Networking"
        val subjectColor = intent.getStringExtra("SUBJECT_COLOR") ?: "#6200EE"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.tvToolbarTitle.text = subjectName

        try {
            binding.toolbar.setBackgroundColor(Color.parseColor(subjectColor))
        } catch (e: Exception) {
            binding.toolbar.setBackgroundColor(Color.parseColor("#6200EE"))
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        setupRecyclerView(subjectName, subjectColor)
    }

    private fun setupRecyclerView(subjectName: String, color: String) {
        val topicsList = when (subjectName) {
            "Networking" -> listOf("OSI Model", "TCP/IP Protocol", "IP Addressing", "Network Topology", "Routing & Switching", "Network Security")
            "Cloud Computing" -> listOf("IaaS, PaaS, SaaS", "Deployment Models", "Virtualization", "AWS Basics", "Azure Basics", "Cloud Security")
            "Machine Learning" -> listOf("Supervised Learning", "Unsupervised Learning", "Neural Networks", "Decision Trees", "NLP Basics", "Reinforcement Learning")
            "Parallel Computing" -> listOf("Parallel Architecture", "Shared Memory Systems", "Distributed Computing", "Pthreads & OpenMP", "MPI Programming", "CUDA & GPU Computing")
            "Web Development" -> listOf("HTML5 & CSS3", "JavaScript Essentials", "Responsive Design", "React Basics", "Node.js & Express", "RESTful APIs")
            "Accounting" -> listOf("Accounting Principles", "Journal & Ledger", "Balance Sheets", "Cash Flow Statements", "Financial Auditing", "Cost Accounting")
            else -> listOf("Introduction", "Core Concepts", "Advanced Theory", "Practical Quiz")
        }

        // ✅ Updated: Added subjectName parameter to adapter
        topicsAdapter = TopicsAdapter(topicsList, color, subjectName) { topic ->
            if (isTopicUnlocked(subjectName, topic, topicsList)) {
                showLevelDialog(subjectName, topic)
            } else {
                Toast.makeText(this, "Complete previous topic (Normal & Hard) to unlock!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvTopics.layoutManager = LinearLayoutManager(this)
        binding.rvTopics.adapter = topicsAdapter
    }

    private fun isTopicUnlocked(subject: String, topic: String, allTopics: List<String>): Boolean {
        val index = allTopics.indexOf(topic)
        if (index == 0) return true 

        val previousTopic = allTopics[index - 1]
        val userSp = getSharedPreferences("BrainifyPrefs", Context.MODE_PRIVATE)
        val userEmail = userSp.getString("user_email", "default") ?: "default"
        val progressPrefs = getSharedPreferences("QuizProgress", Context.MODE_PRIVATE)

        val normalCompleted = progressPrefs.getBoolean("${userEmail}_${subject}_${previousTopic}_Normal_Completed", false)
        val hardCompleted = progressPrefs.getBoolean("${userEmail}_${subject}_${previousTopic}_Hard_Completed", false)

        return normalCompleted && hardCompleted
    }

    private fun showLevelDialog(subject: String, topic: String) {
        val levels = arrayOf("Normal", "Hard")
        AlertDialog.Builder(this)
            .setTitle("Select Difficulty Level")
            .setItems(levels) { _, which ->
                val selectedLevel = levels[which]
                val intent = Intent(this, QuizActivity::class.java)
                intent.putExtra("TOPIC_NAME", topic)
                intent.putExtra("SUBJECT_NAME", subject)
                intent.putExtra("LEVEL", selectedLevel)
                startActivity(intent)
            }
            .show()
    }
}
