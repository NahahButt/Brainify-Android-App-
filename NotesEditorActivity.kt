package com.brainify.quizapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.brainify.quizapp.databinding.ActivityNoteEditorBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class NoteEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditorBinding
    private lateinit var tts: TextToSpeech
    private var noteId: Long = -1
    private val notes = mutableListOf<Note>()
    private var currentNote: Note? = null

    private val CHANNEL_ID = "note_editor_channel"
    private val NOTIFICATION_ID = 501

    private fun getUserEmail(): String {
        val sp = getSharedPreferences("BrainifyPrefs", MODE_PRIVATE)
        return sp.getString("user_email", "default_user") ?: "default_user"
    }

    private fun getPrefsName(): String = "notes_prefs_${getUserEmail()}"

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
        binding = ActivityNoteEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                tts.setSpeechRate(1.0f)
            }
        }

        createNotificationChannel()

        noteId = intent.getLongExtra("note_id", -1)
        loadNotes()

        if (noteId != -1L) {
            currentNote = notes.find { it.id == noteId }
            currentNote?.let {
                binding.etTitle.setText(it.title)
                binding.etNote.setText(it.content)
                supportActionBar?.title = "Edit: ${it.title}"
            }
        } else {
            supportActionBar?.title = "New Note"
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSave.setOnClickListener {
            saveNote()
        }

        binding.btnDelete.setOnClickListener {
            deleteNote()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadNotes() {
        val prefs = getSharedPreferences(getPrefsName(), MODE_PRIVATE)
        val json = prefs.getString("notes", "[]")
        val type = object : TypeToken<List<Note>>() {}.type
        notes.clear()
        notes.addAll(Gson().fromJson(json, type))
    }

    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etNote.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTitle.error = "Please enter title"
            binding.etTitle.requestFocus()
            return
        }

        if (noteId == -1L) {
            val newNote = Note(title = title, content = content)
            notes.add(newNote)
            currentNote = newNote
        } else {
            currentNote?.let {
                it.title = title
                it.content = content
                it.updatedAt = System.currentTimeMillis()
            }
        }

        saveNotes()
        showSuccessNotification(title)
        Toast.makeText(this, "Note saved: $title", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteNote() {
        if (currentNote != null) {
            notes.remove(currentNote)
            saveNotes()
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveNotes() {
        val prefs = getSharedPreferences(getPrefsName(), MODE_PRIVATE)
        prefs.edit().putString("notes", Gson().toJson(notes)).apply()
    }

    private fun showSuccessNotification(title: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.brain_logo1)
            .setContentTitle("Brainify Notepad")
            .setContentText("Note '$title' saved successfully")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Note Editor Notifications", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
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
