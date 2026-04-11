package com.brainify.quizapp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainify.quizapp.adapters.LeaderboardAdapter
import com.brainify.quizapp.databinding.ActivityLeaderboardBinding
import com.brainify.quizapp.models.QuizResult
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    
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
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupChart()
        setupRecyclerView()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Leaderboard"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupChart() {
        val entries = ArrayList<Entry>()
        val sharedPref = getSharedPreferences("QuizHistory", MODE_PRIVATE)
        val historyCount = sharedPref.getInt("history_count", 0)

        for (i in 1..historyCount) {
            val score = sharedPref.getInt("score_$i", 0)
            val total = sharedPref.getInt("total_$i", 1)
            val percentage = if (total > 0) (score.toFloat() / total) * 100 else 0f
            entries.add(Entry(i.toFloat(), percentage))
        }

        val dataSet = LineDataSet(entries, "Performance Trend")
        dataSet.color = Color.parseColor("#6200EE")
        dataSet.valueTextColor = Color.parseColor("#6200EE")
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(Color.parseColor("#6200EE"))

        val lineData = LineData(dataSet)
        binding.chart.data = lineData
        binding.chart.legend.isEnabled = true
        binding.chart.description.isEnabled = false
        binding.chart.invalidate()
    }

    private fun setupRecyclerView() {
        val results = getQuizHistory()
        leaderboardAdapter = LeaderboardAdapter(results)
        binding.rvLeaderboard.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = leaderboardAdapter
        }
    }

    private fun getQuizHistory(): List<QuizResult> {
        val results = mutableListOf<QuizResult>()
        val sharedPref = getSharedPreferences("QuizHistory", MODE_PRIVATE)
        val historyCount = sharedPref.getInt("history_count", 0)
        
        // Match the format used in QuizResultActivity
        val dateTimeFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())

        for (i in 1..historyCount) {
            val score = sharedPref.getInt("score_$i", 0)
            val total = sharedPref.getInt("total_$i", 0)
            val time = sharedPref.getLong("time_$i", 0).toInt()
            val subject = sharedPref.getString("subject_$i", "General") ?: "General"
            val topic = sharedPref.getString("topic_$i", "") ?: ""
            val dateString = sharedPref.getString("date_$i", "") ?: ""
            val cheating = sharedPref.getBoolean("cheating_$i", false)
            
            val date = try {
                if (dateString.isNotEmpty()) dateTimeFormat.parse(dateString) ?: Date() else Date()
            } catch (e: Exception) {
                Date()
            }

            results.add(
                QuizResult(
                    subject = subject,
                    topic = topic,
                    score = score,
                    total = total,
                    date = date,
                    timeTaken = time,
                    cheatingDetected = cheating
                )
            )
        }

        return results.sortedByDescending { it.score }
    }
}
