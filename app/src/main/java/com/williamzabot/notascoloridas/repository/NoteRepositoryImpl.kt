package com.williamzabot.notascoloridas.repository

import com.williamzabot.notascoloridas.data.db.dao.NoteDAO
import com.williamzabot.notascoloridas.data.db.entity.Note

class NoteRepositoryImpl(private val noteDAO: NoteDAO) : NoteRepository {

    override suspend fun insertNote(
        title: String,
        description: String,
        color: String,
        date: String?
    ): Long = noteDAO.insert(
        Note(
            title = title,
            description = description,
            color = color,
            date = date
        )
    )

    override suspend fun updateNote(
        id: Long,
        title: String,
        description: String,
        color: String,
        date: String?
    ) = noteDAO.update(Note(id, title, description, color, date))

    override suspend fun deleteNote(id: Long) = noteDAO.delete(id)

    override suspend fun getNotes(): List<Note> = noteDAO.getNotes()
}