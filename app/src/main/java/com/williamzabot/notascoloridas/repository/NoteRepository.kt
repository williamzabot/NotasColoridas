package com.williamzabot.notascoloridas.repository

import com.williamzabot.notascoloridas.data.db.entity.Note

interface NoteRepository {

    suspend fun insertNote(title : String, description : String, color : String, date : String?): Long

    suspend fun updateNote(id: Long, title : String, description : String, color : String, date : String?)

    suspend fun deleteNote(id: Long)

    suspend fun getNotes(): List<Note>
}