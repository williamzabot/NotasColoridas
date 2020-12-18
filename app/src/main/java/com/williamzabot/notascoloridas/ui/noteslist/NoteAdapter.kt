package com.williamzabot.notascoloridas.ui.noteslist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.williamzabot.notascoloridas.R
import com.williamzabot.notascoloridas.data.db.entity.Note
import com.williamzabot.notascoloridas.extensions.*
import java.lang.System.currentTimeMillis
import java.util.*

class NoteAdapter(private val clickNote: (note: Note) -> Unit) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private lateinit var context: Context
    var notes = listOf<Note>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        context = parent.context
        return NoteViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_note, parent, false
            )
        )
    }

    override fun getItemCount() = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) =
        holder.bind(notes[position])

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemTitle = itemView.findViewById<TextView>(R.id.item_note_title)
        private val itemDescription = itemView.findViewById<TextView>(R.id.item_note_description)
        private val itemCardView = itemView.findViewById<CardView>(R.id.cardview_note)
        private val itemClock = itemView.findViewById<ImageView>(R.id.icon_clock)

        fun bind(note: Note) {
            itemTitle.text = note.title
            itemDescription.text = note.description
            itemCardView.background = transformDrawable(context, note.color)
            configureTime(note)

            itemView.setOnClickListener {
                clickNote.invoke(note)
            }
        }

        private fun configureTime(
            note: Note
        ) {
            val time = note.date
            if (time != null) {
                itemClock.visible()
                val calendar = Calendar.getInstance()
                calendar.set(
                    time.toYear(),
                    time.toMonth(),
                    time.toDay(),
                    time.toHour(),
                    time.toMinute(),
                    0
                )
                if (currentTimeMillis() > calendar.timeInMillis) {
                    note.date = null
                    itemClock.invisible()
                }
            } else {
                itemClock.invisible()
            }
        }

    }
}