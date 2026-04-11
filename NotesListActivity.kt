package com.brainify.quizapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainify.quizapp.databinding.ActivityNotesListBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class NotesListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesListBinding
    private lateinit var adapter: NotesAdapter
    private lateinit var tts: TextToSpeech
    private val notes = mutableListOf<Note>()
    
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
        binding = ActivityNotesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "My Notes"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                tts.setSpeechRate(1.0f)
            }
        }

        adapter = NotesAdapter(notes,
            onClick = { note ->
                val intent = Intent(this, NoteEditorActivity::class.java)
                intent.putExtra("note_id", note.id)
                startActivity(intent)
                if (::tts.isInitialized) tts.speak("Opening ${note.title}", TextToSpeech.QUEUE_FLUSH, null, null)
            },
            onDelete = { note ->
                showDeleteConfirmationDialog(note)
            }
        )

        binding.recyclerNotes.layoutManager = LinearLayoutManager(this)
        binding.recyclerNotes.adapter = adapter

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, NoteEditorActivity::class.java)
            startActivity(intent)
            if (::tts.isInitialized) tts.speak("New note", TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun showDeleteConfirmationDialog(note: Note) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Delete")
        builder.setMessage("Are you sure you want to delete this note: '${note.title}'?")
        builder.setIcon(android.R.drawable.ic_menu_delete)

        builder.setPositiveButton("Yes, Delete") { dialog, _ ->
            notes.remove(note)
            saveNotes()
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Note deleted successfully", Toast.LENGTH_SHORT).show()
            if (::tts.isInitialized) {
                tts.speak("Note deleted", TextToSpeech.QUEUE_FLUSH, null, null)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun loadNotes() {
        val prefs = getSharedPreferences(getPrefsName(), MODE_PRIVATE)
        val json = prefs.getString("notes", "[]")
        val type = object : TypeToken<List<Note>>() {}.type
        notes.clear()
        val loadedNotes: List<Note> = Gson().fromJson(json, type)
        notes.addAll(loadedNotes)
        notes.sortByDescending { it.updatedAt }
        adapter.notifyDataSetChanged()
    }

    private fun saveNotes() {
        val prefs = getSharedPreferences(getPrefsName(), MODE_PRIVATE)
        prefs.edit().putString("notes", Gson().toJson(notes)).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
}
