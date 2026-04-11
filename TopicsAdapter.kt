package com.brainify.quizapp.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brainify.quizapp.R
import com.brainify.quizapp.databinding.ItemTopicBinding

class TopicsAdapter(
    private val topicsList: List<String>,
    private val subjectColor: String,
    private val subjectName: String,
    private val onTopicClick: (String) -> Unit
) : RecyclerView.Adapter<TopicsAdapter.TopicViewHolder>() {

    inner class TopicViewHolder(val binding: ItemTopicBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = ItemTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topicName = topicsList[position]
        val context = holder.itemView.context

        holder.binding.tvTopicName.text = topicName
        holder.binding.tvTopicNumber.text = (position + 1).toString()

        try {
            holder.binding.tvTopicNumber.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(subjectColor))
        } catch (e: Exception) {}

        // ✅ Locking Logic for UI
        val isUnlocked = isTopicUnlocked(context, position)
        if (isUnlocked) {
            holder.binding.ivLock.setImageResource(R.drawable.ic_chevron_right)
            holder.binding.root.alpha = 1.0f
        } else {
            // Use a lock icon if you have one, or just dim it
            // holder.binding.ivLock.setImageResource(R.drawable.ic_lock) 
            holder.binding.root.alpha = 0.5f
        }

        holder.itemView.setOnClickListener {
            onTopicClick(topicName)
        }
    }

    private fun isTopicUnlocked(context: Context, position: Int): Boolean {
        if (position == 0) return true
        
        val previousTopic = topicsList[position - 1]
        val userSp = context.getSharedPreferences("BrainifyPrefs", Context.MODE_PRIVATE)
        val userEmail = userSp.getString("user_email", "default") ?: "default"
        val progressPrefs = context.getSharedPreferences("QuizProgress", Context.MODE_PRIVATE)

        val normalCompleted = progressPrefs.getBoolean("${userEmail}_${subjectName}_${previousTopic}_Normal_Completed", false)
        val hardCompleted = progressPrefs.getBoolean("${userEmail}_${subjectName}_${previousTopic}_Hard_Completed", false)

        return normalCompleted && hardCompleted
    }

    override fun getItemCount(): Int = topicsList.size
}
