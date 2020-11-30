package com.williamzabot.notascoloridas.repository

import com.williamzabot.notascoloridas.data.db.entity.Note

interface NoteRepository {

    suspend fun insertNote(title : String, description : String, favorite : Boolean, color : String): Long

    suspend fun updateNote(id: Long, title : String, description : String, favorite : Boolean, color : String)

    suspend fun deleteNote(id: Long)

    suspend fun getNotes(): List<Note>
}