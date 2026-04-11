package com.brainify.quizapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brainify.quizapp.databinding.ItemLeaderboardBinding
import com.brainify.quizapp.models.QuizResult
import java.text.SimpleDateFormat
import java.util.Locale

class LeaderboardAdapter(
    private val results: List<QuizResult>
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    // Updated format to show Date and Time
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(results[position], position + 1)
    }

    override fun getItemCount() = results.size

    inner class LeaderboardViewHolder(private val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: QuizResult, rank: Int) {
            binding.tvRank.text = "#$rank"
            binding.tvScore.text = "${result.score}/${result.total}"
            binding.tvPercentage.text = String.format("%.1f%%", result.percentage)

            val subjectText = if (result.topic.isNotEmpty()) {
                "${result.subject} - ${result.topic}"
            } else {
                result.subject.ifEmpty { "General" }
            }
            binding.tvSubject.text = subjectText

            // Show full date and time in leaderboard
            binding.tvDate.text = dateTimeFormat.format(result.date)

            if (result.cheatingDetected) {
                binding.tvSubject.setTextColor(Color.RED)
                binding.tvSubject.text = "${binding.tvSubject.text} (Suspicious 🚩)"
            } else {
                binding.tvSubject.setTextColor(Color.BLACK)
            }

            when (rank) {
                1 -> binding.tvRank.setTextColor(Color.parseColor("#FFD700")) // Gold
                2 -> binding.tvRank.setTextColor(Color.parseColor("#C0C0C0")) // Silver
                3 -> binding.tvRank.setTextColor(Color.parseColor("#CD7F32")) // Bronze
                else -> binding.tvRank.setTextColor(Color.parseColor("#757575"))
            }

            val minutes = result.timeTaken / 60
            val seconds = result.timeTaken % 60
            binding.tvTime.text = String.format("%02d:%02d", minutes, seconds)
        }
    }
}
