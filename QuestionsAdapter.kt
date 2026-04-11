package com.brainify.quizapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brainify.quizapp.databinding.ItemQuestionReviewBinding
import com.brainify.quizapp.models.Question

class QuestionsAdapter(
    private val questions: List<Question>,
    private val userAnswers: List<Int>
) : RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position], position, userAnswers[position])
    }

    override fun getItemCount() = questions.size

    inner class QuestionViewHolder(private val binding: ItemQuestionReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: Question, position: Int, userAnswer: Int) {

            binding.tvQuestionNumber.text = "Q${position + 1}"
            binding.tvQuestion.text = question.questionText

            // Set CheckBox text
            binding.cbOptionA.text = "A. ${question.options[0]}"
            binding.cbOptionB.text = "B. ${question.options[1]}"
            binding.cbOptionC.text = "C. ${question.options[2]}"
            binding.cbOptionD.text = "D. ${question.options[3]}"

            // Reset background colors
            val checkBoxes = listOf(binding.cbOptionA, binding.cbOptionB, binding.cbOptionC, binding.cbOptionD)
            checkBoxes.forEach {
                it.setBackgroundColor(Color.TRANSPARENT)
                it.setTextColor(Color.BLACK)
                it.isChecked = false
            }

            // Highlight correct answer in green
            val correctBox = when (question.correctAnswer) {
                0 -> binding.cbOptionA
                1 -> binding.cbOptionB
                2 -> binding.cbOptionC
                else -> binding.cbOptionD
            }
            correctBox.setBackgroundColor(Color.parseColor("#4CAF50"))
            correctBox.setTextColor(Color.WHITE)
            correctBox.isChecked = true

            // Highlight user's wrong answer in red
            if (userAnswer != -1 && userAnswer != question.correctAnswer) {
                val userBox = when (userAnswer) {
                    0 -> binding.cbOptionA
                    1 -> binding.cbOptionB
                    2 -> binding.cbOptionC
                    3 -> binding.cbOptionD
                    else -> null
                }
                userBox?.setBackgroundColor(Color.parseColor("#F44336"))
                userBox?.setTextColor(Color.WHITE)
                userBox?.isChecked = true
            }

            // Explanation & Level
            binding.tvExplanation.text = question.explanation.ifEmpty { "No explanation available." }
            binding.tvDifficulty.text = "Level: ${question.level}"
        }
    }
}
