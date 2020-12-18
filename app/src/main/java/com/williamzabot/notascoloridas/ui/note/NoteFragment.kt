package com.williamzabot.notascoloridas.ui.note

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.work.Data
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.williamzabot.notascoloridas.R
import com.williamzabot.notascoloridas.data.AppDatabase
import com.williamzabot.notascoloridas.data.db.entity.Note
import com.williamzabot.notascoloridas.extensions.*
import com.williamzabot.notascoloridas.repository.NoteRepository
import com.williamzabot.notascoloridas.repository.NoteRepositoryImpl
import com.williamzabot.notascoloridas.ui.colors.*
import com.williamzabot.notascoloridas.ui.colors.model.Color
import com.williamzabot.notascoloridas.ui.notify.work.NotifyWork
import java.lang.System.currentTimeMillis
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.*

const val TITLE = "title"
const val DESCRIPTION = "description"

class NoteFragment : Fragment(R.layout.fragment_note) {

    private lateinit var edtNoteTitle: EditText
    private lateinit var edtNoteDescription: EditText
    private lateinit var constraintNotes: ConstraintLayout
    private lateinit var recyclerColors: RecyclerView
    private lateinit var buttonNotify: Button

    private val viewModel: NoteViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return NoteViewModel(
                    NoteRepositoryImpl(
                        AppDatabase.getInstance(requireContext()).noteDAO
                    ) as NoteRepository
                ) as T
            }
        }
    }

    private var receivedDate: String? = null
    private var currentColor = COR_PADRAO
    private val colors = arrayListOf<Color>()
    private val args: NoteFragmentArgs by navArgs()
    private lateinit var instanceWorkManager: WorkManager
    private var notifyActivated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        instanceWorkManager = WorkManager.getInstance(requireContext())
        initView(view)

        args.date?.let {
            receivedDate = it
        }

        args.note?.let { note ->
            (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.edit_note)
            edtNoteTitle.setText(note.title)
            edtNoteDescription.setText(note.description)
            constraintNotes.background = transformDrawable(requireContext(), note.color)
            currentColor = note.color
            configureButtonNotify(note)
        }

        args.title?.let {
            edtNoteTitle.setText(it)
        }

        args.description?.let {
            edtNoteDescription.setText(it)
        }

        args.color?.let {
            currentColor = it
            constraintNotes.background = transformDrawable(requireContext(), it)
        }
        observeEvents()
        configureColorAdapter()
        clickButtonNotify()

    }

    private fun initView(view: View) {
        edtNoteTitle = view.findViewById(R.id.formulary_note_title)
        edtNoteDescription = view.findViewById(R.id.formulary_note_description)
        constraintNotes = view.findViewById(R.id.constraint_notes_formulary)
        recyclerColors = view.findViewById(R.id.recyclerview_colors)
        buttonNotify = view.findViewById(R.id.button_notifications)
    }

    private fun clickButtonNotify() {
        buttonNotify.setOnClickListener {
            if (notifyActivated) {
                cancelNotification()
            } else {
                goToDialogFragment()
            }
        }
    }

    private fun goToDialogFragment() {
        val direction = NoteFragmentDirections.actionShowDialogDate(
            args.note,
            edtNoteTitle.text.toString(),
            edtNoteDescription.text.toString(),
            currentColor
        )
        findNavController().navigate(direction)
    }

    private fun cancelNotification() {
        if (args.note?.date != null) {
            instanceWorkManager.cancelUniqueWork(args.note!!.date!!)
            args.note?.date = null
            configureButtonNotify(args.note)
        }
    }

    private fun configureButtonNotify(note: Note?) {
        if (dateIsValid(note?.date)) {
            notifyActivated = true
            buttonNotify.text = getString(R.string.cancel)
        } else {
            note?.date = null
            notifyActivated = false
            buttonNotify.text = getString(R.string.avise_me)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.formulary_save_note_menu, menu)


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save_note_icon) {
            saveNote()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveNote() {
        val title = edtNoteTitle.text.toString()
        val description = edtNoteDescription.text.toString()
        if (title.isNotEmpty() && description.isNotEmpty()) {
            if (!dateIsValid(receivedDate)) {
                receivedDate = null
            } else {
                val titleData = Data.Builder().putString(TITLE, title).build()
                val descriptionData = Data.Builder().putString(DESCRIPTION, description).build()
                scheduleNotification(receivedDate!!, titleData, descriptionData)
            }
            viewModel.addOrUpdateNote(
                title,
                description,
                currentColor,
                args.note?.id ?: 0,
                receivedDate ?: args.note?.date
            )
        } else {
            Toast.makeText(
                requireContext(),
                R.string.empty_fields,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun dateIsValid(time: String?): Boolean {
        return when (time) {
            null -> false
            else -> currentTimeMillis() < getCalendar(time).timeInMillis
        }
    }

    private fun getCalendar(time: String): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(
            time.toYear(),
            time.toMonth(),
            time.toDay(),
            time.toHour(),
            time.toMinute(),
            0
        )
        return calendar
    }

    private fun scheduleNotification(
        receivedDate: String,
        title: Data,
        description: Data
    ) {
        val time = getCalendar(receivedDate)
        val delay = time.timeInMillis - currentTimeMillis()
        val notificationWork = OneTimeWorkRequest.Builder(NotifyWork::class.java)
            .setInitialDelay(delay, MILLISECONDS)
            .setInputData(title)
            .setInputData(description)
            .build()
        instanceWorkManager.beginUniqueWork(receivedDate, REPLACE, notificationWork).enqueue()
    }

    private fun observeEvents() {
        viewModel.noteEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is NoteViewModel.NoteState.Inserted,
                is NoteViewModel.NoteState.Updated,
                is NoteViewModel.NoteState.Deleted -> {
                    clearFields()
                    hideKeyboard()
                    findNavController().popBackStack()
                }
            }
        }

        viewModel.messageEventData.observe(viewLifecycleOwner) { stringResId ->
            Toast.makeText(requireContext(), stringResId, Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() = (requireActivity() as? AppCompatActivity)?.hideKeyboard()


    private fun clearFields() {
        edtNoteTitle.text?.clear()
        edtNoteDescription.text?.clear()
    }

    private fun configureColorAdapter() {
        addColors()
        val colorsAdapter = ColorsAdapter(colors) { color ->
            constraintNotes.background =
                transformDrawable(requireContext(), color.drawableBackground)
            currentColor = color.drawableBackground
            args.note?.color = currentColor
        }
        recyclerColors.adapter = colorsAdapter
    }

    private fun addColors() {
        newColor(AZUL, AZUL_FUNDO)
        newColor(BRANCO, COR_PADRAO)
        newColor(VERMELHO, VERMELHO_FUNDO)
        newColor(VERDE, VERDE_FUNDO)
        newColor(AMARELO, AMARELO_FUNDO)
        newColor(LILAS, LILAS_FUNDO)
        newColor(CINZA, CINZA_FUNDO)
        newColor(MARROM, MARROM_FUNDO)
    }

    private fun newColor(circle: String, background: String) = colors.add(Color(circle, background))
}