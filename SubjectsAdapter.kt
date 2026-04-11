package com.brainify.quizapp.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brainify.quizapp.databinding.ItemSubjectBinding
import com.brainify.quizapp.models.Subject

class SubjectsAdapter(
    private val subjectsList: List<Subject>,
    private val onItemClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>() {

    inner class SubjectViewHolder(val binding: ItemSubjectBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjectsList[position]

        holder.binding.apply {
            tvSubjectName.text = subject.name
            ivSubjectIcon.setImageResource(subject.iconResId)
            ivSubjectIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
            pbSubjectProgress.progress = subject.progress
            tvProgressPercent.text = "${subject.progress}% Completed"

            try {
                val themeColor = Color.parseColor(subject.color)
                iconContainer.setCardBackgroundColor(themeColor)
                pbSubjectProgress.progressTintList = ColorStateList.valueOf(themeColor)
            } catch (e: Exception) {
                iconContainer.setCardBackgroundColor(Color.GRAY)
            }

            root.setOnClickListener { onItemClick(subject) }
        }
    }

    override fun getItemCount(): Int = subjectsList.size
}
