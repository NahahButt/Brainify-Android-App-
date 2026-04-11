package com.brainify.quizapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brainify.quizapp.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(
    private val list: List<Note>,
    private val onClick: (Note) -> Unit,
    private val onDelete: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemNoteBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = list[position]

        // Set title
        holder.binding.tvTitle.text = note.title

        // Set content preview (first 100 characters)
        val contentPreview = if (note.content.length > 100) {
            note.content.substring(0, 100) + "..."
        } else {
            note.content
        }
        holder.binding.tvContent.text = contentPreview

        // Set date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        holder.binding.tvDate.text = dateFormat.format(Date(note.updatedAt))

        holder.binding.root.setOnClickListener { onClick(note) }
        holder.binding.btnDelete.setOnClickListener { onDelete(note) }
    }

    override fun getItemCount() = list.size
}