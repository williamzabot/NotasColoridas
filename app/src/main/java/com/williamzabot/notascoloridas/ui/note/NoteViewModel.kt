package com.williamzabot.notascoloridas.ui.note

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamzabot.notascoloridas.R
import com.williamzabot.notascoloridas.repository.NoteRepository
import kotlinx.coroutines.launch

const val ERROR = "ERROR DATABASE"

class NoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {

    private val _noteEvent = MutableLiveData<NoteState>()
    val noteEvent: LiveData<NoteState> get() = _noteEvent

    private val _messageEventData = MutableLiveData<Int>()
    val messageEventData: LiveData<Int> get() = _messageEventData

    fun addOrUpdateNote(
        title: String,
        description: String,
        color: String,
        id: Long = 0,
        date : String?
    ) =
        viewModelScope.launch {
            if (id > 0) {
                updateNote(id, title, description, color, date)
            } else {
               insertNote(title, description, color, date)
            }
        }

    private fun updateNote(
        id: Long,
        title: String,
        description: String,
        color: String,
        date: String?
    ) = viewModelScope.launch {
        try {
            noteRepository.updateNote(id, title, description, color, date)
            _noteEvent.value = NoteState.Updated
            _messageEventData.value = R.string.note_updated
        } catch (ex: Exception) {
            _messageEventData.value = R.string.error_update
            Log.e(ERROR, ex.toString())
        }
    }

    private fun insertNote(
        title: String,
        description: String,
        color: String,
        date: String?
    ) = viewModelScope.launch {
        try {
            val id = noteRepository.insertNote(title, description, color, date)
            if (id > 0) {
                _noteEvent.value = NoteState.Inserted
                _messageEventData.value = R.string.note_added
            }
        } catch (ex: Exception) {
            _messageEventData.value = R.string.error_add
            Log.e(ERROR, ex.toString())
        }
    }

    sealed class NoteState {
        object Inserted : NoteState()
        object Updated : NoteState()
        object Deleted : NoteState()
    }
}
