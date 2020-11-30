package com.williamzabot.notascoloridas.ui.note

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.williamzabot.notascoloridas.R
import com.williamzabot.notascoloridas.data.AppDatabase
import com.williamzabot.notascoloridas.extensions.hideKeyboard
import com.williamzabot.notascoloridas.extensions.transformDrawable
import com.williamzabot.notascoloridas.repository.NoteRepository
import com.williamzabot.notascoloridas.repository.NoteRepositoryImpl
import com.williamzabot.notascoloridas.ui.colors.*
import com.williamzabot.notascoloridas.ui.colors.model.Color
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_note.*

class NoteFragment : Fragment(R.layout.fragment_note) {

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

    private var currentColor = COR_PADRAO
    private val colors = arrayListOf<Color>()
    private val args: NoteFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.note?.let { note ->
            requireActivity().app_toolbar.title = getString(R.string.edit_note)
            formulary_note_title.setText(note.title)
            formulary_note_description.setText(note.description)
            constraint_notes_formulary.background = transformDrawable(requireContext(), note.color)
            currentColor = note.color
        }
        observeEvents()
        configureColorAdapter()
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
        val title = formulary_note_title.text.toString()
        val description = formulary_note_description.text.toString()
        if (title.isNotEmpty() && description.isNotEmpty()) {
            viewModel.addOrUpdateNote(
                title,
                description,
                args.note?.favorite ?: false,
                currentColor,
                args.note?.id ?: 0
            )
        } else {
            Toast.makeText(
                requireContext(),
                R.string.empty_fields,
                Toast.LENGTH_SHORT
            ).show()
        }
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
        formulary_note_title.text?.clear()
        formulary_note_description.text?.clear()
    }

    private fun configureColorAdapter() {
        addColors()
        val colorsAdapter = ColorsAdapter(colors) { color ->
            constraint_notes_formulary.background =
                transformDrawable(requireContext(), color.drawableBackground)
            currentColor = color.drawableBackground
        }
        recyclerview_colors.adapter = colorsAdapter
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