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
import java.lang.System.currentTimeMillis
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
            if (calendar.timeInMillis > currentTimeMillis()) {
                goToNotesFormulary(createTimeString(calendar))
            } else {
                Toast.makeText(context, "Data inválida", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createTimeString(
        calendar: Calendar
    ): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm")
        return formatter.format(calendar.time).replace(" ", "").replace("/", "")
            .replace(":", "")
    }

    private fun goToNotesFormulary(time: String) {
        val direction =
            DateDialogFragmentDirections.actionDialogToFormulary(
                args.note,
                time,
                args.title,
                args.description,
                args.color
            )
        findNavController().navigate(direction)
        Toast.makeText(context, "Ao salvar, a notificação será agendada", Toast.LENGTH_SHORT).show()
    }


}