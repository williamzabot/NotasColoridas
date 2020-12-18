package com.williamzabot.notascoloridas.ui.noteslist

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.williamzabot.notascoloridas.R
import com.williamzabot.notascoloridas.data.AppDatabase
import com.williamzabot.notascoloridas.data.db.entity.Note
import com.williamzabot.notascoloridas.extensions.navigateWithAnimations
import com.williamzabot.notascoloridas.repository.NoteRepository
import com.williamzabot.notascoloridas.repository.NoteRepositoryImpl


const val PREFERENCES = "preferences"
const val KEY_SHARED = "key"
const val FIRST_LOGIN = "first login"
const val PREF_LINEAR = "preference linear"
const val PREF_STAGGERED = "preference staggered"

class NotesListFragment : Fragment(R.layout.fragment_notes_list) {

    private lateinit var btLinear: MenuItem
    private lateinit var btStaggered: MenuItem
    private lateinit var recyclerNotes: RecyclerView
    private lateinit var txtAddNote: TextView
    private val notesAdapter by lazy {
        NoteAdapter { currentNote ->
            sendNoteToEdit(currentNote)
        }
    }

    private val viewModel: NotesListViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return NotesListViewModel(
                    NoteRepositoryImpl(
                        AppDatabase.getInstance(requireContext()).noteDAO
                    ) as NoteRepository
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        recyclerNotes = view.findViewById(R.id.recyclerview_noteslist)
        txtAddNote = view.findViewById(R.id.txt_add_note)
        observeEvents()
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getNotes()
    }

    private fun observeEvents() {
        viewModel.notesList.observe(viewLifecycleOwner) { notes ->

            recyclerNotes.apply {
                layoutManager = getLayout()
                adapter = notesAdapter
                itemTouchHelper().attachToRecyclerView(this)
            }
            notesAdapter.notes = notes
        }
    }

    private fun getLayout(): RecyclerView.LayoutManager {
        val preferences = requireContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val key = preferences.getString(KEY_SHARED, FIRST_LOGIN)
        return if (key == PREF_STAGGERED) {
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            LinearLayoutManager(requireContext())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.exibition_list_menu, menu)
        val preferences = requireContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val key = preferences.getString(KEY_SHARED, FIRST_LOGIN)
        btLinear = menu.findItem(R.id.ic_linear)
        btStaggered = menu.findItem(R.id.ic_staggered)
        if (key == PREF_LINEAR || key == FIRST_LOGIN) {
            btLinear.isVisible = false
            btStaggered.isVisible = true
        } else {
            btLinear.isVisible = true
            btStaggered.isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val preferences =
            requireContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        if (item.itemId == R.id.ic_linear) {
            editor.putString(KEY_SHARED, PREF_LINEAR)
            editor.apply()
            viewModel.getNotes()
            btLinear.isVisible = false
            btStaggered.isVisible = true
        } else if (item.itemId == R.id.ic_staggered) {
            editor.putString(KEY_SHARED, PREF_STAGGERED)
            editor.apply()
            viewModel.getNotes()
            btLinear.isVisible = true
            btStaggered.isVisible = false
        }
        return super.onOptionsItemSelected(item)
    }


    private fun sendNoteToEdit(note: Note) {
        val directions = NotesListFragmentDirections.actionNotelistToNoteadd(note)
        findNavController().navigateWithAnimations(directions)
    }

    private fun itemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val flags = ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
                return makeMovementFlags(0, flags)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                actionSwipeNote(viewHolder)
            }
        })
    }

    private fun actionSwipeNote(viewHolder: RecyclerView.ViewHolder) {
        val notes = viewModel.notesList.value
        val note = notes!![viewHolder.adapterPosition]
        deleteNote(note.id)
    }

    private fun deleteNote(id: Long) {
        viewModel.deleteNote(id).invokeOnCompletion {
            refreshNotes()
        }
    }

    private fun refreshNotes() {
        viewModel.getNotes().invokeOnCompletion {
            showMessageNoteDeleted()
        }
    }

    private fun showMessageNoteDeleted() =
        viewModel.messageEventData.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

    private fun setListeners() {
        txtAddNote.setOnClickListener {
            findNavController().navigateWithAnimations(R.id.action_notelist_to_noteadd)
        }
    }
}
