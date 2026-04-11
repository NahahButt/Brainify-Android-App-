package com.brainify.quizapp.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainify.quizapp.adapters.LeaderboardAdapter
import com.brainify.quizapp.databinding.FragmentLeaderboardBinding
import com.brainify.quizapp.models.QuizResult
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var leaderboardAdapter: LeaderboardAdapter

    private fun getUserEmail(): String {
        val sp = requireContext().getSharedPreferences("BrainifyPrefs", android.content.Context.MODE_PRIVATE)
        return sp.getString("user_email", "default") ?: "default"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        setupRecyclerView()
    }

    private fun setupChart() {
        val entries = ArrayList<Entry>()
        val sharedPref = requireContext().getSharedPreferences("QuizHistory_${getUserEmail()}", android.content.Context.MODE_PRIVATE)
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
            layoutManager = LinearLayoutManager(requireContext())
            adapter = leaderboardAdapter
        }
    }

    private fun getQuizHistory(): List<QuizResult> {
        val results = mutableListOf<QuizResult>()
        val sharedPref = requireContext().getSharedPreferences("QuizHistory_${getUserEmail()}", android.content.Context.MODE_PRIVATE)
        val historyCount = sharedPref.getInt("history_count", 0)
        
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
