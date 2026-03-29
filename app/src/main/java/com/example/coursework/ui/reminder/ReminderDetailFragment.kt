package com.example.coursework.ui.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.coursework.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class ReminderDetailFragment : Fragment() {

    private val viewModel: ReminderDetailViewModel by viewModels()
    private val args: ReminderDetailFragmentArgs by navArgs()

    private lateinit var etTitle: EditText
    private lateinit var btnDate: MaterialButton
    private lateinit var btnTime: MaterialButton
    private lateinit var btnSave: Button
    private lateinit var tvLastModified: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var toggleImportant: ToggleButton

    private var technicalDate: String = ""
    private var selectedTime: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etTitle = view.findViewById(R.id.etTitle)
        btnDate = view.findViewById(R.id.btnDate)
        btnTime = view.findViewById(R.id.btnTime)
        btnSave = view.findViewById(R.id.btnSave)
        tvLastModified = view.findViewById(R.id.tvLastModified)
        btnBack = view.findViewById(R.id.btnBack)
        toggleImportant = view.findViewById(R.id.toggleImportant)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.loadReminder(args.reminderId)

        lifecycleScope.launch {
            viewModel.reminder.collect { reminder ->
                reminder?.let {
                    etTitle.setText(it.title)
                    technicalDate = it.date
                    selectedTime = it.time
                    toggleImportant.isChecked = it.isImportant
                    
                    // Display friendly date
                    try {
                        val parts = it.date.split("-")
                        if (parts.size == 3) {
                            btnDate.text = "${parts[2]}/${parts[1].toInt()}/${parts[0]}"
                        } else {
                            btnDate.text = it.date
                        }
                    } catch (e: Exception) {
                        btnDate.text = it.date
                    }
                    
                    btnTime.text = it.time
                }
            }
        }

        val calendar = Calendar.getInstance()

        btnDate.setOnClickListener {
            DatePickerDialog(
                requireContext(), { _, y, m, d ->
                    technicalDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                    btnDate.text = "$d/${m + 1}/$y"
                    btnDate.error = null
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnTime.setOnClickListener {
            TimePickerDialog(
                requireContext(), { _, h, min ->
                    selectedTime = String.format(Locale.US, "%02d:%02d", h, min)
                    btnTime.text = selectedTime
                    btnTime.error = null
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            var isValid = true

            if (title.isBlank()) {
                etTitle.error = "Required"
                isValid = false
            }

            if (technicalDate.isBlank()) {
                btnDate.error = "Required"
                isValid = false
            }

            if (selectedTime.isBlank()) {
                btnTime.error = "Required"
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            val currentReminder = viewModel.reminder.value
            if (currentReminder != null) {
                val updatedReminder = currentReminder.copy(
                    title = title,
                    date = technicalDate,
                    time = selectedTime,
                    isImportant = toggleImportant.isChecked
                )
                viewModel.updateReminder(updatedReminder)
                findNavController().navigateUp()
            }
        }
    }
}
