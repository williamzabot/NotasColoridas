package com.williamzabot.notascoloridas.ui.date

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.williamzabot.notascoloridas.R
import com.williamzabot.notascoloridas.ui.note.NoteFragmentArgs

class DateDialogFragment : DialogFragment() {

    private lateinit var confirmButton: FloatingActionButton
    private val args: NoteFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_fragment_date, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        confirmButton = view.findViewById(R.id.done_fab)

        clickConfirmButton()

    }

    private fun clickConfirmButton() {
        confirmButton.setOnClickListener {
            val direction = DateDialogFragmentDirections.actionDialogToFormulary(args.note)
            findNavController().navigate(direction)
        }
    }
}