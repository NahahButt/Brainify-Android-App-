package com.brainify.quizapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.brainify.quizapp.databinding.ActivityNotesBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private lateinit var tts: TextToSpeech
    private val notesList = mutableListOf<Note>()
    private var currentNote: Note? = null

    private val CHANNEL_ID = "notes_save_channel"
    private val NOTIFICATION_ID = 401
    override fun attachBaseContext(newBase: Context) {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(newBase)
        val langCode = prefs.getString("language", "en") ?: "en"
        val locale = java.util.Locale(langCode)
        java.util.Locale.setDefault(locale)

        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize TTS
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                tts.setSpeechRate(1.0f)
            }
        }

        // Create notification channel
        createNotificationChannel()

        setupActionBar()
        loadNotes()
        setupClickListeners()
        loadLastNote()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Notepad"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        saveCurrentNote()
        onBackPressed()
        return true
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveCurrentNote()
        }

        binding.btnClear.setOnClickListener {
            clearNote()
        }

        binding.btnNewNote.setOnClickListener {
            saveCurrentNote()
            clearNote()
        }
    }

    private fun loadLastNote() {
        val sharedPref = getSharedPreferences("NotesPrefs", MODE_PRIVATE)
        val lastNoteId = sharedPref.getLong("last_note_id", -1)

        if (lastNoteId != -1L) {
            currentNote = notesList.find { it.id == lastNoteId }
            currentNote?.let {
                binding.etTitle.setText(it.title)
                binding.etNotes.setText(it.content)
            }
        }
    }

    private fun saveCurrentNote() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etNotes.text.toString().trim()

        if (title.isEmpty() && content.isEmpty()) {
            return
        }

        if (currentNote == null) {
            // Create new note
            currentNote = Note(
                title = if (title.isNotEmpty()) title else "Untitled Note",
                content = content
            )
            notesList.add(currentNote!!)
        } else {
            // Update existing note
            currentNote!!.title = if (title.isNotEmpty()) title else "Untitled Note"
            currentNote!!.content = content
            currentNote!!.updatedAt = System.currentTimeMillis()
        }

        saveNotes()
        saveLastNoteId()
        showSuccessNotification(currentNote!!.title)

        Toast.makeText(this, "Note saved: ${currentNote!!.title}", Toast.LENGTH_SHORT).show()
        tts.speak("Note saved successfully", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun clearNote() {
        binding.etTitle.setText("")
        binding.etNotes.setText("")
        currentNote = null
        binding.etTitle.requestFocus()
        tts.speak("New note ready", TextToSpeech.QUEUE_FLUSH, null, null)
        Toast.makeText(this, "New note ready", Toast.LENGTH_SHORT).show()
    }

    private fun loadNotes() {
        val sharedPref = getSharedPreferences("NotesPrefs", MODE_PRIVATE)
        val json = sharedPref.getString("notes_list", "[]")
        val type = object : TypeToken<List<Note>>() {}.type
        notesList.clear()
        notesList.addAll(Gson().fromJson(json, type))
    }

    private fun saveNotes() {
        val sharedPref = getSharedPreferences("NotesPrefs", MODE_PRIVATE)
        sharedPref.edit()
            .putString("notes_list", Gson().toJson(notesList))
            .apply()
    }

    private fun saveLastNoteId() {
        currentNote?.let {
            val sharedPref = getSharedPreferences("NotesPrefs", MODE_PRIVATE)
            sharedPref.edit()
                .putLong("last_note_id", it.id)
                .apply()
        }
    }

    private fun showSuccessNotification(title: String) {
        val message = "Note '$title' saved successfully"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.brain_logo1)
            .setContentTitle("Brainify Notepad")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(this)
                    .notify(NOTIFICATION_ID, builder.build())
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    4001
                )
            }
        } else {
            NotificationManagerCompat.from(this)
                .notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notes Save Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifications when notes are saved"

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
}