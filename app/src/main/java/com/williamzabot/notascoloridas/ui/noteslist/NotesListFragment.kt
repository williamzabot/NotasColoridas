package com.williamzabot.notascoloridas.ui.noteslist

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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
import com.williamzabot.notascoloridas.extensions.transformDrawable
import com.williamzabot.notascoloridas.repository.NoteRepository
import com.williamzabot.notascoloridas.repository.NoteRepositoryImpl
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_notes_list.*
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_note.view.*

const val FAKE_ID_FAVORITES: Long = 5000
const val FAKE_ID_NOT_FAVORITES: Long = 3000
const val FAVORITES = "Favoritos"
const val NOT_FAVORITES = "Não Favoritos"
const val PREFERENCES = "preferences"
const val KEY_SHARED = "key"
const val FIRST_LOGIN = "first login"
const val PREF_LINEAR = "preference linear"
const val PREF_STAGGERED = "preference staggered"

class NotesListFragment : Fragment(R.layout.fragment_notes_list) {

    private var completeList = arrayListOf<NoteItem>()
    private var groupAdapter = GroupAdapter<ViewHolder>()
    private lateinit var btLinear: MenuItem
    private lateinit var btStaggered: MenuItem

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
        observeEvents()
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getNotes()
    }

    private fun observeEvents() {
        viewModel.notesList.observe(viewLifecycleOwner) { notes ->
            groupAdapter.clear()
            completeList.clear()

            val favoritesSection = createSection(FAKE_ID_FAVORITES, FAVORITES)
            populateSection(FAKE_ID_FAVORITES, notes, favoritesSection)
            groupAdapter.add(favoritesSection)

            val notFavoritesSection = createSection(FAKE_ID_NOT_FAVORITES, NOT_FAVORITES)
            populateSection(FAKE_ID_NOT_FAVORITES, notes, notFavoritesSection)
            groupAdapter.add(notFavoritesSection)

            recyclerview_noteslist.apply {
                layoutManager = getLayout()
                adapter = groupAdapter
            }
            itemTouchHelper().attachToRecyclerView(recyclerview_noteslist)
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

    private fun populateSection(
        fakeId: Long,
        notes: List<Note>,
        section: Section
    ) {
        for (note in notes) {
            val condition = createConditionToPopulateList(fakeId, note)
            if (condition) {
                val noteItem = NoteItem(note, object : OnNoteClickListener {
                    override fun onItemClick() {
                        sendNoteToEdit(note)
                    }

                    override fun onFavoriteClick() {
                        if (note.favorite) {
                            updateNote(note, false)
                        } else {
                            updateNote(note, true)
                        }
                    }
                })
                completeList.add(noteItem)
                section.add(noteItem)
            }
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

    private fun updateNote(note: Note, favorite: Boolean) {
        viewModel.updateNote(note, favorite).invokeOnCompletion {
            viewModel.getNotes()
        }
    }

    private fun createConditionToPopulateList(
        fakeId: Long,
        note: Note
    ): Boolean {
        return if (fakeId == FAKE_ID_FAVORITES) {
            note.favorite
        } else {
            !note.favorite
        }
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
        val itemNote = completeList[viewHolder.adapterPosition]
        val id = itemNote.currentNote.id
        if (id != FAKE_ID_NOT_FAVORITES && id != FAKE_ID_FAVORITES && id > 0) {
            deleteNote(id)
        } else {
            groupAdapter.notifyDataSetChanged()
        }
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

    private fun setListeners() =
        txt_add_note.setOnClickListener {
            findNavController().navigateWithAnimations(R.id.action_notelist_to_noteadd)
        }

    private fun createSection(fakeId: Long, title: String): Section {
        val section = Section()
        val headerItem = HeaderItem(title)
        section.setHeader(headerItem)
        val noteItem = NoteItem(Note(fakeId, title, "", false, ""))
        completeList.add(noteItem)
        return section
    }

    class NoteItem() : Item() {

        lateinit var currentNote: Note
        private lateinit var onNoteClickListener: OnNoteClickListener

        constructor(pNote: Note, pOnNoteClickListener: OnNoteClickListener) : this() {
            this.currentNote = pNote
            this.onNoteClickListener = pOnNoteClickListener
        }

        constructor(pNote: Note) : this() {
            this.currentNote = pNote
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.apply {
                item_note_title.text = currentNote.title
                item_note_description.text = currentNote.description
                cardview_note.background = transformDrawable(context, currentNote.color)
                setOnClickListener {
                    onNoteClickListener.onItemClick()
                }
                configStar()
            }
        }

        private fun View.configStar() {
            favorite_star.apply {
                setOnClickListener {
                    onNoteClickListener.onFavoriteClick()
                }
                if (currentNote.favorite) {
                    setBackgroundResource(R.drawable.yellowstar)
                } else {
                    setBackgroundResource(R.drawable.blackstar)
                }
            }
        }

        override fun getLayout() = R.layout.item_note
    }

    class HeaderItem() : Item() {

        private var title: String = ""

        constructor(pTitle: String) : this() {
            this.title = pTitle
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.header_title.text = title
        }

        override fun getLayout() = R.layout.item_header
    }

    interface OnNoteClickListener {
        fun onItemClick()
        fun onFavoriteClick()
    }
}
