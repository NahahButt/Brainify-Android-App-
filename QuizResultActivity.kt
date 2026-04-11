package com.brainify.quizapp

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.brainify.quizapp.databinding.ActivityQuizResultBinding
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.kernel.colors.ColorConstants
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class QuizResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizResultBinding
    private val handler = Handler(Looper.getMainLooper())
    private val random = Random(System.currentTimeMillis())
    private var isCelebrating = false
    private var cheatingDetected = false

    private fun getUserEmail(): String {
        val sp = getSharedPreferences("BrainifyPrefs", MODE_PRIVATE)
        return sp.getString("user_email", "Not Available") ?: "Not Available"
    }

    private fun getUserName(): String {
        val sp = getSharedPreferences("BrainifyPrefs", MODE_PRIVATE)
        return sp.getString("user_name", "Student") ?: "Student"
    }

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
        binding = ActivityQuizResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cheatingDetected = intent.getBooleanExtra("CHEATING_DETECTED", false)

        setupActionBar()
        displayResults()
        setupClickListeners()

        binding.root.postDelayed({
            startCelebration()
        }, 500)
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Quiz Results"
    }

    override fun onSupportNavigateUp(): Boolean {
        navigateToHome()
        return true
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun displayResults() {
        val score = intent.getIntExtra("SCORE", 0)
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val subject = intent.getStringExtra("SUBJECT_NAME") ?: "General"
        val topic = intent.getStringExtra("TOPIC_NAME") ?: ""
        val timeTakenSeconds = intent.getIntExtra("TIME_TAKEN_SECONDS", 0)

        val timeTakenString = intent.getStringExtra("TIME_TAKEN") ?: "00:00"
        binding.tvTime.text = timeTakenString

        val percentage = if (totalQuestions > 0) {
            (score.toFloat() / totalQuestions.toFloat() * 100).toInt()
        } else 0

        animateScoreCounter(score, totalQuestions, percentage)

        binding.tvCorrect.text = score.toString()
        binding.tvWrong.text = (totalQuestions - score).toString()

        val message = if (percentage >= 90) "EXCELLENT! 🎉"
        else if (percentage >= 70) "GREAT JOB! 👍"
        else if (percentage >= 50) "GOOD WORK! 😊"
        else "KEEP PRACTICING! 💪"
        
        binding.tvMessage.text = message

        saveToHistory(score, totalQuestions, timeTakenSeconds.toLong(), subject, topic, cheatingDetected)
    }

    private fun animateScoreCounter(score: Int, total: Int, percentage: Int) {
        val animator = ValueAnimator.ofInt(0, score)
        animator.duration = 2000
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Int
            binding.tvScore.text = "$animatedValue/$total"
            val currentPercent = if (total > 0) (animatedValue.toFloat() / total * 100).toInt() else 0
            updateScoreColor(currentPercent)
        }
        animator.start()
    }

    private fun updateScoreColor(percentage: Int) {
        val color = if (percentage >= 90) "#4CAF50"
        else if (percentage >= 70) "#2196F3"
        else if (percentage >= 50) "#FF9800"
        else "#F44336"
        
        binding.tvScore.setTextColor(Color.parseColor(color))
    }

    private fun startCelebration() {
        val score = intent.getIntExtra("SCORE", 0)
        val total = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val percentage = if (total > 0) (score * 100 / total) else 0
        if (percentage >= 50) startFullCelebration()
    }

    private fun startFullCelebration() {
        isCelebrating = true
        for (i in 1..8) {
            handler.postDelayed({ if(isCelebrating) createFloatingStar() }, (i * 400).toLong())
        }
    }

    private fun createFloatingStar() {
        val starView = ImageView(this).apply {
            setImageResource(R.drawable.ic_star)
            layoutParams = android.widget.FrameLayout.LayoutParams(30.dpToPx(), 30.dpToPx())
            x = random.nextFloat() * (binding.root.width - 100)
            y = binding.root.height.toFloat()
        }
        (binding.root as? android.view.ViewGroup)?.addView(starView)
        starView.animate().y(0f).rotationBy(720f).setDuration(3000).withEndAction {
            (binding.root as? android.view.ViewGroup)?.removeView(starView)
        }.start()
    }

    private fun saveToHistory(score: Int, totalQuestions: Int, timeTaken: Long, subject: String, topic: String, cheating: Boolean) {
        val email = getUserEmail()
        val sharedPref = getSharedPreferences("QuizHistory_$email", MODE_PRIVATE)
        val historyCount = sharedPref.getInt("history_count", 0) + 1
        
        val fullDateTime = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        sharedPref.edit().apply {
            putInt("history_count", historyCount)
            putInt("score_$historyCount", score)
            putInt("total_$historyCount", totalQuestions)
            putLong("time_$historyCount", timeTaken)
            putString("subject_$historyCount", subject)
            putString("topic_$historyCount", topic)
            putString("date_$historyCount", fullDateTime)
            putBoolean("cheating_$historyCount", cheating)
            apply()
        }
    }

    private fun setupClickListeners() {
        binding.btnHomeContainer.setOnClickListener { navigateToHome() }
        binding.btnShareContainer.setOnClickListener { shareResult() }
        binding.btnDownloadPdf.setOnClickListener {
            if (checkPermission()) {
                generatePDF()
            } else {
                requestPermission()
            }
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) true
        else ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
    }

    private fun generatePDF() {
        try {
            val score = intent.getIntExtra("SCORE", 0)
            val total = intent.getIntExtra("TOTAL_QUESTIONS", 0)
            val subject = intent.getStringExtra("SUBJECT_NAME") ?: "General"
            val topic = intent.getStringExtra("TOPIC_NAME") ?: ""
            val time = binding.tvTime.text.toString()
            val userName = getUserName()
            val userEmail = getUserEmail()

            val fileName = "Brainify_Result_${System.currentTimeMillis()}.pdf"
            var outputStream: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) outputStream = contentResolver.openOutputStream(uri)
            } else {
                val pdfFile = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                outputStream = java.io.FileOutputStream(pdfFile)
            }

            if (outputStream != null) {
                val writer = PdfWriter(outputStream)
                val pdfDoc = PdfDocument(writer)
                val document = Document(pdfDoc)

                document.add(Paragraph("BRAINIFY QUIZ APP - OFFICIAL REPORT")
                    .setBold().setFontSize(22f).setTextAlignment(TextAlignment.CENTER).setFontColor(ColorConstants.BLUE))
                
                document.add(Paragraph("\nStudent Details").setBold().setFontSize(14f))
                document.add(Paragraph("Name: $userName"))
                document.add(Paragraph("Email: $userEmail"))
                document.add(Paragraph("Exam Date & Time: ${SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date())}"))
                
                document.add(Paragraph("\nQuiz Information").setBold().setFontSize(14f))
                document.add(Paragraph("Subject: $subject"))
                document.add(Paragraph("Topic: $topic"))
                document.add(Paragraph("Score: $score / $total"))
                document.add(Paragraph("Percentage: ${String.format("%.1f%%", (score.toFloat()/total)*100)}"))
                document.add(Paragraph("Time Duration: $time"))
                
                document.add(Paragraph("\n--------------------------------------------------\n"))
                
                // Removing violation lines as requested
                
                document.add(Paragraph("(This is a computer-generated report from Brainify Quiz App)"))

                document.close()
                Toast.makeText(this, "PDF saved to Downloads folder", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "PDF Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareResult() {
        val score = intent.getIntExtra("SCORE", 0)
        val total = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val shareText = "🎯 I scored $score/$total on Brainify Quiz App!"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share Result"))
    }

    override fun onBackPressed() {
        navigateToHome()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
