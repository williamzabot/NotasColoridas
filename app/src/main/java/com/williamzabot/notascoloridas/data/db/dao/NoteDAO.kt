package com.williamzabot.notascoloridas.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.williamzabot.notascoloridas.data.db.entity.Note

@Dao
interface NoteDAO {

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM notes")
    suspend fun getNotes(): List<Note>

}
