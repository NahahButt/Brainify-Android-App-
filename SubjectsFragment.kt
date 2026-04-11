package com.brainify.quizapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainify.quizapp.adapters.SubjectsAdapter
import com.brainify.quizapp.databinding.FragmentSubjectsBinding
import com.brainify.quizapp.models.Subject
import com.brainify.quizapp.TopicsActivity
import com.brainify.quizapp.LoginActivity
import com.brainify.quizapp.R

class SubjectsFragment : Fragment() {

    private var _binding: FragmentSubjectsBinding? = null
    private val binding get() = _binding!!
    private lateinit var subjectsAdapter: SubjectsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLoginAction()
        setupRecyclerView()
    }

    private fun setupLoginAction() {
        val sharedPref = requireContext().getSharedPreferences("BrainifyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)


    }

    override fun onResume() {
        super.onResume()
        setupLoginAction()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val subjectsList = getSubjectsWithProgress()

        subjectsAdapter = SubjectsAdapter(subjectsList) { subject ->
            val sharedPref = requireContext().getSharedPreferences("BrainifyPrefs", Context.MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

            if (isLoggedIn) {
                val intent = Intent(requireContext(), TopicsActivity::class.java).apply {
                    putExtra("SUBJECT_NAME", subject.name)
                    putExtra("SUBJECT_COLOR", subject.color)
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Please login first to view topics", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvSubjects.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = subjectsAdapter
        }
    }

    private fun getSubjectsWithProgress(): List<Subject> {
        val userPrefs = requireContext().getSharedPreferences("BrainifyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = userPrefs.getBoolean("isLoggedIn", false)
        val userEmail = userPrefs.getString("user_email", "default") ?: "default"

        val progressPrefs = requireContext().getSharedPreferences("QuizProgress", Context.MODE_PRIVATE)

        fun getProgress(subject: String): Int {
            if (!isLoggedIn) return 0

            val userSpecificKey = "${userEmail}_$subject"
            return if (progressPrefs.contains(userSpecificKey)) {
                progressPrefs.getInt(userSpecificKey, 0)
            } else {
                progressPrefs.getInt(subject, 0)
            }
        }

        return listOf(
            Subject("Networking", R.drawable.ic_math, "#FF5722", getProgress("Networking")),
            Subject("Machine Learning", R.drawable.ic_physics, "#2196F3", getProgress("Machine Learning")),
            Subject("Cloud Computing", R.drawable.ic_chemistry, "#4CAF50", getProgress("Cloud Computing")),
            Subject("Parallel Computing", R.drawable.ic_biology, "#1A237E", getProgress("Parallel Computing")),
            Subject("Web Development", R.drawable.ic_computer1, "#9C27B0", getProgress("Web Development")),
            Subject("Accounting", R.drawable.ic_english, "#FF9800", getProgress("Accounting"))
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
