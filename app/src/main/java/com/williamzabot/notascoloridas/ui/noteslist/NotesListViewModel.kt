package com.williamzabot.notascoloridas.ui.noteslist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamzabot.notascoloridas.R
import com.williamzabot.notascoloridas.data.db.entity.Note
import com.williamzabot.notascoloridas.repository.NoteRepository
import com.williamzabot.notascoloridas.ui.note.ERROR
import kotlinx.coroutines.launch

class NotesListViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _notesList = MutableLiveData<List<Note>>()
    val notesList: LiveData<List<Note>> get() = _notesList

    private val _messageEventData = MutableLiveData<Int>()
    val messageEventData: LiveData<Int> get() = _messageEventData

    fun getNotes() = viewModelScope.launch {
            _notesList.postValue(noteRepository.getNotes())
        }


    fun deleteNote(id : Long) = viewModelScope.launch {
        try {
            if(id > 0){
                noteRepository.deleteNote(id)
                _messageEventData.postValue(R.string.note_deleted)
            }
        }catch (ex : Exception){
            _messageEventData.postValue(R.string.error_delete)
        }
    }

   fun updateNote(
        note : Note,
        favorite: Boolean
    ) = viewModelScope.launch {
        try {
            noteRepository.updateNote(note.id, note.title, note.description, favorite, note.color)
        } catch (ex: Exception) {
            Log.e(ERROR, ex.toString())
        }
    }


}