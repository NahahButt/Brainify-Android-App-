package com.brainify.quizapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.brainify.quizapp.fragments.*

object NavigationManager {

    fun goToSubjects(activity: Activity) {
        val intent = Intent(activity, SubjectsActivity::class.java)
        activity.startActivity(intent)
    }

    fun goToTopics(activity: Activity, subjectName: String) {
        val intent = Intent(activity, TopicsActivity::class.java)
        intent.putExtra("SUBJECT_NAME", subjectName)
        activity.startActivity(intent)
    }

    fun goToQuiz(activity: Activity, topicName: String) {
        val intent = Intent(activity, QuizActivity::class.java)
        intent.putExtra("TOPIC_NAME", topicName)
        activity.startActivity(intent)
    }

    fun goToProfile(activity: Activity) {
        if (activity is MainActivity) {
            // ✅ Fixed typo: Changed 'activity.ai binding' to 'activity.binding'
            activity.binding.bottomNavigation.selectedItemId = R.id.nav_profile
        } else {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra("SHOW_PROFILE", true)
            activity.startActivity(intent)
        }
    }

    fun goToNotes(activity: Activity) {
        val intent = Intent(activity, NotesListActivity::class.java)
        activity.startActivity(intent)
    }

    fun goToSettings(activity: Activity) {
        val intent = Intent(activity, SettingsActivity::class.java)
        activity.startActivity(intent)
    }

    fun goToLeaderboard(activity: Activity) {
        val intent = Intent(activity, LeaderboardActivity::class.java)
        activity.startActivity(intent)
    }

    fun logout(activity: Activity) {
        val sharedPref = activity.getSharedPreferences("BrainifyPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("isLoggedIn", false).apply()

        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }
}
