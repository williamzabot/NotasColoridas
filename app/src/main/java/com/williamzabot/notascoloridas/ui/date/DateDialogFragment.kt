package com.williamzabot.notascoloridas.ui.date

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.williamzabot.notascoloridas.R
import com.williamzabot.notascoloridas.data.db.entity.Note
import com.williamzabot.notascoloridas.ui.note.NoteFragmentArgs
import java.text.SimpleDateFormat
import java.util.*

class DateDialogFragment : DialogFragment() {


    private val args: DateDialogFragmentArgs by navArgs()
    private lateinit var confirmButton: FloatingActionButton
    private lateinit var datePicker: DatePicker
    private lateinit var timePicker: TimePicker


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
        datePicker = view.findViewById(R.id.date_picker)
        timePicker = view.findViewById(R.id.time_picker)

        clickConfirmButton()

    }

    private fun clickConfirmButton() {
        confirmButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                timePicker.hour,
                timePicker.minute,
                0
            )
            val timeInMillis = calendar.timeInMillis
            val currentTime = System.currentTimeMillis()
            if (timeInMillis > currentTime) {
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm")
                val time = formatter.format(calendar.time).replace(" ", "").replace("/", "")
                    .replace(":", "")
                val direction =
                    DateDialogFragmentDirections.actionDialogToFormulary(
                        args.note,
                        time,
                        args.title,
                        args.description,
                        args.color
                    )
                findNavController().navigate(direction)
            } else {
                Toast.makeText(context, "Data inválida", Toast.LENGTH_LONG).show()
            }
        }
    }


}