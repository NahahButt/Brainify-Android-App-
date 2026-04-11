package com.brainify.quizapp

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.brainify.quizapp.database.QuizDbHelper
import com.brainify.quizapp.databinding.ActivityQuizBinding
import com.brainify.quizapp.models.Question
import java.util.Locale

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedOption = -1
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeft: Long = 900000 //  Updated to 15 Minutes (15 * 60 * 1000)
    private var quizFinished = false
    private var cheatingDetected = false
    private var cheatType = ""

    private var questions: ArrayList<Question> = ArrayList()
    private var currentSubjectName = ""
    private var currentTopicName = ""
    private var currentLevel = "Normal"

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)

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

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentSubjectName = intent.getStringExtra("SUBJECT_NAME") ?: "General"
        currentTopicName = intent.getStringExtra("TOPIC_NAME") ?: ""
        currentLevel = intent.getStringExtra("LEVEL") ?: "Normal"
        // Line 65 ka error yahan fix ho raha hai
        val dbHelper = QuizDbHelper(this)
        questions = dbHelper.getQuestions(currentSubjectName, currentTopicName, currentLevel)
        if (questions.isEmpty()) {
            Toast.makeText(this, "No $currentLevel questions found for $currentTopicName", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupClickListeners()
        setupQuestion(false)
        startTimer()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && !quizFinished) {
            cheatingDetected = true
            cheatType = "Screenshot Attempt / App Switch"
            Toast.makeText(this, "Security Alert: Unauthorized action detected!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }
            override fun onFinish() { finishQuiz() }
        }.start()
    }

    private fun setupQuestion(withAnimation: Boolean) {
        if (questions.isEmpty()) return
        val question = questions[currentQuestionIndex]
        if (withAnimation) {
            val outAnim = AnimationUtils.loadAnimation(this, R.anim.question_out)
            val inAnim = AnimationUtils.loadAnimation(this, R.anim.question_in)
            binding.quizScroll.startAnimation(outAnim)
            outAnim.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(p0: android.view.animation.Animation?) {}
                override fun onAnimationEnd(p0: android.view.animation.Animation?) {
                    bindQuestionData(question)
                    binding.quizScroll.startAnimation(inAnim)
                }
                override fun onAnimationRepeat(p0: android.view.animation.Animation?) {}
            })
        } else {
            bindQuestionData(question)
        }
    }

    private fun bindQuestionData(question: Question) {
        binding.tvQuestion.text = question.questionText
        binding.tvQuestionNumber.text = "$currentLevel Quiz: ${currentQuestionIndex + 1}/${questions.size}"
        binding.cbOptionA.text = question.options[0]
        binding.cbOptionB.text = question.options[1]
        binding.cbOptionC.text = question.options[2]
        binding.cbOptionD.text = question.options[3]
        resetOptions()
        when (question.selectedAnswer) {
            0 -> binding.cbOptionA.isChecked = true
            1 -> binding.cbOptionB.isChecked = true
            2 -> binding.cbOptionC.isChecked = true
            3 -> binding.cbOptionD.isChecked = true
        }
        selectedOption = question.selectedAnswer
        updateNavigationButtons()
    }

    private fun resetOptions() {
        binding.cbOptionA.isChecked = false
        binding.cbOptionB.isChecked = false
        binding.cbOptionC.isChecked = false
        binding.cbOptionD.isChecked = false
    }

    private fun updateNavigationButtons() {
        binding.btnNext.visibility = if (currentQuestionIndex == questions.size - 1) View.GONE else View.VISIBLE
        binding.btnSubmit.visibility = if (currentQuestionIndex == questions.size - 1) View.VISIBLE else View.GONE
        binding.btnPrevious.visibility = if (currentQuestionIndex == 0) View.GONE else View.VISIBLE
    }

    private fun setupClickListeners() {
        val checkBoxes = listOf(binding.cbOptionA, binding.cbOptionB, binding.cbOptionC, binding.cbOptionD)
        checkBoxes.forEachIndexed { index, checkBox ->
            checkBox.setOnClickListener {
                checkBoxes.forEach { it.isChecked = false }
                checkBox.isChecked = true
                selectedOption = index
                questions[currentQuestionIndex].selectedAnswer = index
            }
        }
        binding.btnNext.setOnClickListener {
            if (selectedOption != -1) {
                currentQuestionIndex++
                setupQuestion(true)
            } else Toast.makeText(this, "Select an option", Toast.LENGTH_SHORT).show()
        }
        binding.btnPrevious.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                setupQuestion(true)
            }
        }
        binding.btnSubmit.setOnClickListener { finishQuiz() }
    }

    private fun finishQuiz() {
        if (quizFinished) return
        quizFinished = true
        if (::countDownTimer.isInitialized) countDownTimer.cancel()

        score = 0
        questions.forEach { if (it.selectedAnswer == it.correctAnswer) score++ }
        val percentage = (score * 100) / questions.size

        val userSp = getSharedPreferences("BrainifyPrefs", Context.MODE_PRIVATE)
        val userEmail = userSp.getString("user_email", "default") ?: "default"
        val progressPrefs = getSharedPreferences("QuizProgress", Context.MODE_PRIVATE)

        //  Mark this specific level as completed for the topic
        val completionKey = "${userEmail}_${currentSubjectName}_${currentTopicName}_${currentLevel}_Completed"
        progressPrefs.edit().putBoolean(completionKey, true).apply()

        // Update overall subject progress percentage
        val progressKey = "${userEmail}_$currentSubjectName"
        if (percentage > progressPrefs.getInt(progressKey, 0)) {
            progressPrefs.edit().putInt(progressKey, percentage).apply()
        }

        val spentMillis = 900000 - timeLeft
        val timeTakenString = String.format("%02d:%02d", spentMillis/60000, (spentMillis%60000)/1000)

        val intent = Intent(this, QuizResultActivity::class.java)
        intent.putExtra("SCORE", score)
        intent.putExtra("TOTAL_QUESTIONS", questions.size)
        intent.putExtra("TIME_TAKEN", timeTakenString)
        intent.putExtra("TIME_TAKEN_SECONDS", (spentMillis/1000).toInt())
        intent.putExtra("SUBJECT_NAME", currentSubjectName)
        intent.putExtra("TOPIC_NAME", currentTopicName)
        intent.putExtra("LEVEL", currentLevel)
        intent.putExtra("CHEATING_DETECTED", cheatingDetected)
        intent.putExtra("CHEAT_TYPE", cheatType)
        startActivity(intent)
        finish()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!quizFinished) {
            cheatingDetected = true
            cheatType = "Minimized App / Switched Window"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator.release()
    }
}
